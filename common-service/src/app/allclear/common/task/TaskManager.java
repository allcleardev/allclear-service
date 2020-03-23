package app.allclear.common.task;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.*;

import io.dropwizard.lifecycle.Managed;

import com.fasterxml.jackson.databind.ObjectMapper;
import app.allclear.common.errors.ThrottledException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.jackson.JacksonUtils;

/** A Dropwizard managed component that operates a background thread to process,
 *  asynchronously, items on the persisted queue.
 *
 *  NOTE: with some future release of Dropwizard, I hope that they include a
 *        better mechanism to queue & process tasks.
 *
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 * 
 * from-qs-version 2.0.0
 * from-qs-since 12/20/2014
 *
 */
public class TaskManager implements Managed, Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	private static final ObjectMapper MAPPER = JacksonUtils.createMapper();	// Needed to modify a task.

	/** Indicates that the background thread should continue operation. This flag is used within
	 *  the "run" method to indicate when to exit. The property should be checked frequently in
	 *  the "run" method to exit as soon as possible.
	 */
	public boolean isAvailable() { return available; }
	private boolean available = false;

	/** Represents the queue from which the task requests are pulled. */
	public TaskQueue getQueue() { return queue; }
	private final TaskQueue queue;

	/** Represents the list of task operators used to process the background requests. */
	public Map<String, TaskOperator<?>> getOperators() { return operators; }
	private Map<String, TaskOperator<?>> operators = null;
	public void addOperator(TaskOperator<?> newValue) { operators.put(newValue.name, newValue); }
	public TaskOperator<?> removeOperator(final String name) { return operators.remove(name); }

	/** Represents the optional prefix to the queue. */
	public String getQueuePrefix() { return queuePrefix; }
	private String queuePrefix = "";
	public TaskManager withQueuePrefix(final String newValue) { queuePrefix = newValue; return this; }

	/** Represents the duration that the thread should sleep between processing. */
	public long getSleep() { return sleep; }
	private long sleep = 0L;
	public void setSleep(final long newValue) { sleep = newValue; }

	/** Represents the number of threads available to process the queue items. */
	public int getThreads() { return threads; }
	private int threads = 1;
	public void setThreads(final int newValue) { threads = newValue; }

	/** Represents the maximum time in seconds to delay the retry of a request. Leave open to externally configure. */
	private long maxDelay = 60L * 60L;	// One hour.

	private boolean noSuffocating = true;	// By default ensure that a single TaskOperator does not monopolize all the threads. DLS on 8/23/2017.
	public TaskManager withNoSuffocating(final boolean newValue) { noSuffocating = newValue; return this; } 

	private Runnable beforeRun = null;	// Action to run at the start of each thread. DLS on 5/5/2018.
	public TaskManager withBeforeRun(final Runnable newValue) { beforeRun = newValue; return this; }

	private Runnable afterRun = null;	// Action to run at the end of each thread. DLS on 5/5/2018.
	public TaskManager withAfterRun(final Runnable newValue) { afterRun = newValue; return this; }

	/** Executor of the threads. */
	private List<Thread> executor = null;

	/** Populator.
	 * 
	 * @param queue
	 * @param operators
	 */
	public TaskManager(final TaskQueue queue, final int threads, final TaskOperator<?>...operators)
	{
		this.queue = queue;
		this.operators = new HashMap<>(operators.length);
		for (TaskOperator<?> o : operators)
			this.operators.put(o.name, o);

		this.threads = threads;
		this.executor = new ArrayList<>(threads);
	}

	/** Populator.
	 * 
	 * @param queue
	 * @param sleep
	 * @param operators
	 */
	public TaskManager(final TaskQueue queue, final long sleep, final TaskOperator<?>...operators)
	{
		this(queue, sleep, 1, operators);
	}

	/** Populator.
	 * 
	 * @param queue
	 * @param sleep
	 * @param threads
	 * @param operators
	 */
	public TaskManager(final TaskQueue queue, final long sleep, final int threads, final TaskOperator<?>...operators)
	{
		this(queue, threads, operators);
		this.sleep = sleep;
	}

	@Override
	public void start() throws Exception
	{
		available = true;
		Thread thread = null;
		for (int i = 0; i < threads; i++)
		{
			executor.add(thread = new Thread(this));
			thread.setDaemon(true);
			thread.setName("TaskManager-" + i);
			thread.start();
		}
	}

	/** Makes the task manager available for operation during integration tests. */
	public void turnOn()
	{
		available = true;
	}

	/** Makes the task manager unavailable for operation during integration tests. */
	public void turnOff()
	{
		available = false;
	}

	@Override
	public void stop() throws Exception
	{
		// Stop the thread when the current operation is over.
		available = false;

		// Do NOT exit until all threads have terminated.
		while (executor.stream().anyMatch(Thread::isAlive))
			Thread.sleep(100L);

		executor.clear();
	}

	@Override
	public void run()
	{
		if (null != beforeRun) beforeRun.run();

		while (available)
		{
			try
			{
				process();

				// Give CPU a short break otherwise pegs the processor. DLS on 11/29/2016.
				if (0L < sleep) Thread.sleep(sleep);
			}
			catch (Throwable ex)	// Use Throwable to ensure that the thread never bombs out. DLS on 6/27/2016.
			{
				logger.error(ex.getMessage(), ex);
			}
		}

		if (null != afterRun) afterRun.run();
	}

	/** Processes all the queues. */
	public int process() throws Exception
	{
		int count = 0;

		for (var entry : operators.entrySet())
		{
			// Check "available" property to ensure earliest possible exit when requested.
			if (!available)
				return count;

			var operator = entry.getValue();
			if (operator.available)	// Only one thread per instance of the TaskManager should be leveraged for a single task. Anymore could suffocate other tasks if a single task is way backed up. DLS on 8/3/2017.
			{
				if (noSuffocating)
					operator.available = false;

				try { count+= process(operator, operator.clazz); }
				finally { operator.available = true; }	// No matter what MUST make this call after processing (even on error) or it will become permanently unavailable! DLS on 8/3/2017.
			}
		}

		return count;
	}

	/** Retrieves a list of Operator statistics. */
	public List<OperatorStats> stats()
	{
		return operators.entrySet().stream().map(e -> {
			var v = e.getValue();
			int queueSize = 0, dlqSize = 0;
			try
			{
				queueSize = queue.getQueueSize(v.name);
				dlqSize = queue.getQueueSize(v.dlq);
			}
			catch (final RuntimeException ex) { throw ex; }
			catch (final Exception ex) { throw new RuntimeException(ex); }
			return new OperatorStats(v.name, queueSize, dlqSize, v.successes, v.skips, v.errors);
		})
		.collect(Collectors.toList());
	}

	/** Helper method - converts a name to a queue name. */
	private String queueName(final String name)
	{
		return queuePrefix + name;
	}

	/** Helper method - converts a name to a dead-letter-queue name. */
	private String dlqName(final String name)
	{
		return TaskOperator.dlq(queueName(name));
	}

	/** Helper method - gets the task-operator by external name (without prefix). */
	private TaskOperator<?> getOperator(final String name) throws ValidationException
	{
		TaskOperator<?> value = operators.get(queueName(name));
		if (null == value)
			throw new ValidationException("queueName", "The queue '" + name + "' does not exist.");

		return value;
	}

	/** Helper method - gets the task-operator by the internal name (with prefix). */
	private TaskOperator<?> getOperator_(final String name) throws ValidationException
	{
		TaskOperator<?> value = operators.get(name);
		if (null == value)
			throw new ValidationException("queueName", "The queue '" + name + "' does not exist.");

		return value;
	}

	/** Counts the number of requests in a queue.
	 * 
	 * @param name
	 * @return zero if the queue does not exist.
	 * @throws Exception
	 */
	public int countRequests(final String name) throws Exception
	{
		return queue.getQueueSize(queueName(name));
	}

	/** List all the requests in a queue. */
	public List<TaskRequest<?>> listRequests(final String name) throws Exception
	{
		return queue.listRequests(queueName(name));
	}

	/** List a subset of the requests based of the paging information.
	 * 
	 * @param name
	 * @param page 1-based.
	 * @param pageSize
	 * @return NULL if none found.
	 * @throws Exception
	 */
	public List<TaskRequest<?>> listRequests(final String name, final int page, final int pageSize) throws Exception
	{
		return queue.listRequests(queueName(name), page, pageSize);
	}

	/** Counts the number of requests in a dead-letter-queue.
	 * 
	 * @param name
	 * @return zero if the queue does not exist.
	 * @throws Exception
	 */
	public int countDLQ(final String name) throws Exception
	{
		return queue.getQueueSize(dlqName(name));
	}

	/** List all the requests in a dead-letter-queue. */
	public List<TaskRequest<?>> listDLQ(final String name) throws Exception
	{
		return queue.listRequests(dlqName(name));
	}

	/** List a subset of the requests in a dead-letter-queue based of the paging information.
	 * 
	 * @param name
	 * @param page 1-based.
	 * @param pageSize
	 * @return NULL if none found.
	 * @throws Exception
	 */
	public List<TaskRequest<?>> listDLQ(final String name, final int page, final int pageSize) throws Exception
	{
		return queue.listRequests(dlqName(name), page, pageSize);
	}

	/** Modifies the contents of a request. Payload MUST string because it is coming from a generic endpoint
	 *  that does not know the exact class until TaskOperator is retrieved within this method.
	 */
	public TaskRequest<?> modifyRequest(final String name, final TaskRequest<String> request) throws Exception
	{
		// Make sure that there is a Task Request identifier.
		if (null == request.id)
			throw new ValidationException("id", "The ID field must be supplied.");

		// Get the operator in order to get the expected class of the request.
		var queueName = queueName(name);
		return addRequestAndRemovePrior(queueName, request.value, request.id, getOperator_(queueName).clazz);
	}

	/** Modifies the contents of a request. Payload MUST string because it is coming from a generic endpoint
	 *  that does not know the exact class until TaskOperator is retrieved within this method.
	 */
	public TaskRequest<?> modifyDLQ(final String name, final TaskRequest<String> request) throws Exception
	{
		// Make sure that there is a Task Request identifier.
		if (null == request.id)
			throw new ValidationException("id", "The ID field must be supplied.");

		// Get the operator in order to get the expected class of the request.
		var queueName = dlqName(name);
		return addRequestAndRemovePrior(queueName, request.value, request.id, getOperator_(queueName(name)).clazz);
	}

	/** Generate a new task request based on the string payload. */
	private <T> TaskRequest<T> addRequestAndRemovePrior(final String name, final String payload, final String oldId, final Class<T> clazz) throws Exception
	{
		// First make sure that that the payload can be deserialized correctly.
		var request = new TaskRequest<>(MAPPER.readValue(payload, clazz));

		// Remove the old request.
		queue.removeRequest(name, oldId);

		// Add the new request.
		queue.pushTask(name, request);

		return request;
	}

	/** Removes a single request from the specified queue.
	 * 
	 * @param name
	 * @param id
	 * @return TRUE if found AND removed.
	 */
	public boolean removeRequest(final String name, final String id)
	{
		return queue.removeRequest(queueName(name), id);
	}

	/** Removes a single DLQ request from the specified queue.
	 * 
	 * @param name
	 * @param id
	 * @return TRUE if found AND removed.
	 */
	public boolean removeDLQ(final String name, final String id)
	{
		return queue.removeRequest(dlqName(name), id);
	}

	/** Clears the requests from the specified queue.
	 * 
	 * @param name
	 * @return number of requests removed.
	 * @throws Exception
	 */
	public int clearRequests(final String name) throws Exception
	{
		return queue.clearRequests(queueName(name));
	}

	/** Clears the requests from the specified DLQ.
	 * 
	 * @param name
	 * @return number of requests removed.
	 * @throws Exception
	 */
	public int clearDLQ(final String name) throws Exception
	{
		return queue.clearRequests(dlqName(name));
	}

	/** Moves the requests stored in the DLQ back to the operational queue.
	 * 
	 * @param name
	 * @return number of requests moved.
	 * @throws Exception
	 */
	public int moveRequests(final String name) throws Exception
	{
		var operator = getOperator(name);

		return queue.moveRequests(operator.dlq, operator.name, operator.clazz);
	}

	/** Processes the entire queue by name. */
	public int process(final String name) throws Exception
	{
		var operator = getOperator(name);

		return process(operator, operator.clazz);
	}

	/** Processes a single item in a queue. */
	public boolean process(final String name, final String id) throws Exception
	{
		// Get all the requests in the queue.
		var queueName = queueName(name);
		var requests = queue.listRequests(queueName);
		if (CollectionUtils.isEmpty(requests))
			throw new ValidationException("queueName", "No items in queue '" + name + "'.");

		// Find the request.
		var request = requests.stream().filter(v -> id.equals(v.id)).findFirst();
		if (!request.isPresent())
			throw new ValidationException("id", String.format("The item '%s' was not found in queue '%s'.", id, name));

		// Process request.
		var operator = getOperator_(queueName);
		boolean result = process(operator, request.get(), operator.clazz);

		// Remove after successful processing of the request.
		if (result) queue.removeRequest(queueName, id);

		return result;
	}

	/** Processes a single item in a queue. */
	public boolean processDLQ(final String name, final String id) throws Exception
	{
		// Get all the requests in the queue.
		var queueName = dlqName(name);
		var requests = queue.listRequests(queueName);
		if (CollectionUtils.isEmpty(requests))
			throw new ValidationException("queueName", "No items in queue '" + name + "'.");

		// Find the request.
		var request = requests.stream().filter(v -> id.equals(v.id)).findFirst();
		if (!request.isPresent())
			throw new ValidationException("id", String.format("The item '%s' was not found in DLQ '%s'.", id, name));

		// Process request.
		var operator = getOperator_(queueName(name));
		boolean result = process(operator, request.get(), operator.clazz);

		// Remove after successful processing of the request.
		if (result) queue.removeRequest(queueName, id);

		return result;
	}

	/** Helper method - processes a single request. */
	@SuppressWarnings("unchecked")
	private <T> boolean process(final TaskOperator<?> operator, TaskRequest<?> request, final Class<T> clazz) throws Exception
	{
		try
		{
			var req = (TaskRequest<T>) request;
			var op = (TaskOperator<T>) operator;
			var success = op.callback.process(req.value);
			if (success)
			{
				op.callback.onSuccess(req);
				operator.successes++;
			}
			else
				operator.skips++;

			return success;
		}
		catch (Exception ex)
		{
			operator.errors++;

			throw ex;
		}
	}

	/** Processes the entire queue. */
	@SuppressWarnings("unchecked")
	public <T> int process(final TaskOperator<?> operator, final Class<T> clazz) throws Exception
	{
		int count = 0;
		TaskRequest<T> request = null;
		var op = (TaskOperator<T>) operator;
		var dlq = new LinkedList<TaskRequest<T>>();	// Dead letter queue for requests that have exceeded their maximum number of tries. DLS on 7/14/2016.
		var skipped = new LinkedList<TaskRequest<T>>();

		// Include "available" property to ensure earliest possible exit when requested.
		while (available && (null != (request = queue.popTask(operator.name, clazz))))
		{
			// If the request has run the maximum number of tries, put in the dead letter queue (DLQ). DLS on 7/14/2016.
			if (operator.maxTries <= request.tries)
			{
				dlq.add(request);
				continue;
			}

			// If the request is not ready for processing, skip it.
			else if (!request.ready())
			{
				skipped.add(request);
				continue;
			}

			try
			{
				final long time = System.currentTimeMillis();

				// Put back on queue if skipped.
				if (op.callback.process(request.value))
				{
					op.callback.onSuccess(request);
					op.successes++;
				}
				else
				{
					op.skips++;
					skipped.add(request);
				}

				if (logger.isDebugEnabled())
					logger.debug("Processed {} on thread '{}' in {} ms.", op.name, Thread.currentThread().getName(), System.currentTimeMillis() - time);

				count++;
			}

			catch (final ThrottledException ex)
			{
				logger.warn("Throttled: {} - {}. Temporarily deferring execution.", op.name, ex.getMessage());
				skipped.add(request.withNextRunAt(calcNextRunAt(request.incrementTries())));
				break;
			}

			catch (final Exception ex)
			{
				op.errors++;

				// Only log an error if this is at least the second try. Sometimes there is a timing issue with the first try
				// before its originating transaction is complete.
				if (0 < request.tries)
					logger.error(ex.getMessage(), ex);
				else
					logger.warn(ex.getMessage());

				// Increment the try count & set a backoff delay.
				// Also, put back on the queue.
				skipped.add(request.withNextRunAt(calcNextRunAt(request.incrementTries())));
			}
		}

		// Re-add skipped items.
		if (!dlq.isEmpty())
			for (var q : dlq)
				queue.pushTask(operator.dlq, q);

		// Move items that have exceeded their maximum tries to the DLQ. DLS on 7/14/2016.
		if (!skipped.isEmpty())
			for (var skip : skipped)
				queue.pushTask(operator.name, skip);

		return count;
	}

	/** Helper method - calculates the next run time based on a backoff algo as milliseconds. */
	private long calcNextRunAt(final int tries) { return calcNextRunAt(tries, maxDelay); }

	/** Helper method - static version to be unit tested. */
	public static long calcNextRunAt(final int tries, final long maxDelay)
	{
		long seconds = 60L * power(2L, tries - 1);
		if (maxDelay < seconds)
			seconds = maxDelay;

		return (seconds * 1000L) + System.currentTimeMillis();
	}

	/** Recursive algo to calc exponents with long and int. */
	public static long power(final long x, final int y)
	{
		if (0 == y) return 1L;
		if (1 == y) return x;
		if (0 == (y % 2)) return power(x * x, y / 2);	// Even

		return x * power(x * x, y / 2);	// Odd
	}
}
