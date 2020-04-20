package app.allclear.common.azure;

import static java.util.stream.Collectors.toList;
import static com.azure.storage.common.policy.RetryPolicyType.EXPONENTIAL;

import java.io.IOException;
import java.util.*;

import org.slf4j.*;

import io.dropwizard.lifecycle.Managed;

import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.queue.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.allclear.common.errors.AbortException;
import app.allclear.common.errors.ThrottledException;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.task.OperatorStats;
import app.allclear.common.task.TaskOperator;

/** A Dropwizard managed component that operates a background thread to process,
 *  asynchronously, items on AWS's Simple Queue Service.
 *
 * @author smalleyd
 * @version 2.1.18
 * @since 5/19/2016
 * 
 */
public class QueueManager implements Managed, Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(QueueManager.class);
	static final ObjectMapper MAPPER = JacksonUtils.createMapperMS();
	private static final Base64.Decoder decoder = Base64.getDecoder();

	// private static final int DELAY_AFTER_ERROR = 5 * 60;	// 5 minutes.

	/** Indicates that the background thread should continue operation. This flag is used within
	 *  the "run" method to indicate when to exit. The property should be checked frequently in
	 *  the "run" method to exit as soon as possible.
	 */
	public boolean isAvailable() { return available; }
	private boolean available = false;

	public final String connectionString;

	final Map<String, QueueClient> queues;
	public QueueClient queue(final String name) { return queues.get(name); }

	/** Represents the list of task operators used to process the background requests. */
	public Map<String, TaskOperator<?>> getOperators() { return operators; }
	private Map<String, TaskOperator<?>> operators = null;
	public QueueManager addOperator(TaskOperator<?> o)
	{
		operators.put(o.name, o);
		this.queues.put(o.name, new QueueClientBuilder().connectionString(connectionString)
			.retryOptions(new RequestRetryOptions(EXPONENTIAL, o.maxTries, o.timeout, o.delayMS(), o.maxDelayMS(), null)).queueName(o.name).buildClient());
		return this;
	}
	public TaskOperator<?> removeOperator(final String name) { return operators.remove(name); }

	public List<OperatorStats> stats()
	{
		return operators.values()
				.stream()
				.map(v -> new OperatorStats(v.name, queue(v.name).getProperties().getApproximateMessagesCount(), 0, v.successes, v.skips, v.errors))
				.collect(toList());
	}

	/** Represents the duration that the thread should sleep between processing. */
	public long getSleep() { return sleep; }
	private long sleep = 0L;
	public void setSleep(final long newValue) { sleep = newValue; }

	/** Represents the number of threads available to process the queue items. */
	public int getThreads() { return threads; }
	private int threads = 1;
	public void setThreads(final int newValue) { threads = newValue; }

	public int getBatchSize() { return batchSize; }
	private int batchSize = 10;
	public QueueManager withBatchSize(final int newValue) { batchSize = newValue; return this; }

	private Runnable beforeRun = null;	// Action to run at the start of each thread. DLS on 5/5/2018.
	public QueueManager withBeforeRun(final Runnable newValue) { beforeRun = newValue; return this; }

	private Runnable afterRun = null;	// Action to run at the end of each thread. DLS on 5/5/2018.
	public QueueManager withAfterRun(final Runnable newValue) { afterRun = newValue; return this; }

	/** Executor of the threads. */
	private List<Thread> executor = null;

	/** Populator.
	 * 
	 * @param queue
	 * @param operators
	 */
	public QueueManager(final String connectionString, final int threads, final TaskOperator<?>...operators)
	{
		this.connectionString = connectionString;
		this.queues = new HashMap<>(operators.length);
		this.operators = new HashMap<>(operators.length);
		for (var o : operators) addOperator(o);

		this.threads = threads;
		this.executor = new ArrayList<>(threads);
	}

	/** Populator.
	 * 
	 * @param queue
	 * @param sleep
	 * @param operators
	 */
	public QueueManager(final String connectionString, final long sleep, final TaskOperator<?>...operators)
	{
		this(connectionString, sleep, 1, operators);
	}

	/** Populator.
	 * 
	 * @param queue
	 * @param sleep
	 * @param threads
	 * @param operators
	 */
	public QueueManager(final String connectionString, final long sleep, final int threads, final TaskOperator<?>...operators)
	{
		this(connectionString, threads, operators);
		this.sleep = sleep;
	}

	@Override
	public void start() throws Exception
	{
		available = true;
		Thread thread = null;
		for (int i = 0; i < threads; i++)
		{
			executor.add(thread = new Thread(this, "QueueManager-" + i));
			thread.setDaemon(true);
			thread.start();
		}
	}

	/** Makes the Azure Queue manager available for operation during integration tests. */
	public void turnOn()
	{
		available = true;
	}

	/** Makes the Azure Queue manager unavailable for operation during integration tests. */
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
		logger.info("RUN");

		if (null != beforeRun) beforeRun.run();

		while (available)
		{
			try
			{
				process();

				// Give CPU a short break otherwise pegs the processor. DLS on 11/29/2016.
				if (0L < sleep)
					Thread.sleep(sleep);
			}
			catch (Throwable ex)	// Use Throwable to ensure that the thread never bombs out. DLS on 6/27/2016.
			{
				logger.error(ex.getMessage(), ex);
			}
		}

		if (null != afterRun) afterRun.run();

		logger.info("DONE: {}", available);
	}

	/** Processes all the queues. */
	public int process() throws Exception
	{
		int count = 0;
		logger.debug("PROCESS: {}, {}", available, operators.size());

		for (var entry : operators.entrySet())
		{
			// Check "available" property to ensure earliest possible exit when requested.
			if (!available)
				return count;

			var operator = entry.getValue();
			count+= process(operator, operator.clazz);
		}

		logger.debug("PROCESSED: {}, {}", available, count);
		return count;
	}

	/** Processes the entire queue. */
	@SuppressWarnings("unchecked")
	public <T> int process(final TaskOperator<?> operator, final Class<T> clazz) throws Exception
	{
		int count = 0;
		var op = (TaskOperator<T>) operator;
		var queue = queues.get(op.name);

		// Include "available" property to ensure earliest possible exit when requested.
		while (available)
		{
			boolean throttled = false;
			final int count_ = count;
			for (var request : queue.receiveMessages(batchSize))
			{
				if (logger.isDebugEnabled())
					logger.debug("Process {}", request.getMessageText());
	
				try
				{
					final long time = System.currentTimeMillis();
	
					// Remove from queue if successful.
					if (op.callback.process(deserialize(request.getMessageText(), clazz)))
					{
						op.successes++;
						queue.deleteMessage(request.getMessageId(), request.getPopReceipt());
					}
					else
						op.skips++;
	
					if (logger.isDebugEnabled())
						logger.debug("Processed {} on thread '{}' in {} ms - {}.", op.name, Thread.currentThread().getName(), System.currentTimeMillis() - time, request.getMessageText());
	
					count++;
				}
	
				catch (final ThrottledException ex)
				{
					logger.warn("Throttled: {} - {}. Temporarily deferring execution.", op.name, ex.getMessage());
					// queue.delayMessage(operator.name, request.getReceiptHandle(), DELAY_AFTER_ERROR);

					throttled = true;
					op.errors++;
					break;
				}

				catch (final AbortException ex)
				{
					op.errors++;
					logger.warn("Aborted: {} - {}.", op.name, ex.getMessage());
					queue.deleteMessage(request.getMessageId(), request.getPopReceipt());
				}

				catch (final JsonParseException ex)
				{
					op.errors++;
					logger.warn("UNPARSEABLE: {} - {}.", op.name, ex.getMessage());
					queue.deleteMessage(request.getMessageId(), request.getPopReceipt());
				}

				catch (final Exception ex)
				{
					// Only log an error if this is at least the second try. Sometimes there is a timing issue with the first try
					// before its originating transaction is complete.
					if (1L < request.getDequeueCount())
						logger.error("{} ({}): {} - {}", operator.name, clazz, ex.getMessage(), ex);
					else
						logger.warn("{} ({}): {}", operator.name, clazz, ex.getMessage());
	
					op.errors++;
					// Increment the try count[0] & set a backoff delay.
					// Also, put back on the queue.
					// queue.delayMessage(operator.name, request.getReceiptHandle(), DELAY_AFTER_ERROR);
				}
			}

			if (throttled || (count_ == count)) break;	// No new messages
		}

		return count;
	}

	<T> T deserialize(final String value, final Class<T> clazz) throws JsonParseException, IOException
	{
		try { return MAPPER.readValue(value, clazz); }
		catch (final JsonParseException ex)
		{
			logger.warn("PARSE_RETRY: {}", value);

			return MAPPER.readValue(decoder.decode(value), clazz);
		}
	}
}
