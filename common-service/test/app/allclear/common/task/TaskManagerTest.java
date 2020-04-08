package app.allclear.common.task;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.*;

import org.junit.*;

import app.allclear.common.ThreadUtils;
import app.allclear.common.errors.ErrorInfo;
import app.allclear.common.errors.ThrottledException;

/** Functional test class for the TaskManager background processor.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TaskManagerTest
{
	protected TaskQueue queue = null;
	protected TaskManager manager = null;
	protected TaskManager managerX = null;	// Test extra handlers like onSuccess.
	protected TaskOperator<ErrorInfo> operator = null;
	protected TaskOperator<ErrorInfo> operatorX = null;	// Test extra handlers like onSuccess.

	/** Data. */
	protected static final String QUEUE_NAME = "errorLog";
	private static final List<ErrorInfo> REQUESTS =
		IntStream.rangeClosed(1, 8).mapToObj(i -> new ErrorInfo(new Exception("Error " + i))).collect(Collectors.toList());
	private static final String REQUEST_JSON = "{ \"error\": \"Warning %d\", \"stacktrace\": \"Stack Trace %<d\" }";
	private static final int SIZE = 8;

	private static int beforeRun_ = 0;
	private static int afterRun_ = 0;
	protected static int onSuccess = 0;
	private static final Runnable beforeRun = () -> beforeRun_++;
	private static final Runnable afterRun = () -> afterRun_++;

	/** Must reset counts since also used by RedisTaskManagerTest. */
	protected static void reset()
	{
		beforeRun_ = 0;
		afterRun_ = 0;
		onSuccess = 0;
	}

	@Before
	public void up() throws Exception
	{
		reset();
		queue = new LocalQueue();
		manager = new TaskManager(queue, 1,
			operator = new TaskOperator<ErrorInfo>(QUEUE_NAME, x -> { System.out.println(x.stacktrace); return true; }, ErrorInfo.class));
		managerX = new TaskManager(queue, 1,
			operatorX = new TaskOperator<ErrorInfo>(QUEUE_NAME,
				new TaskCallback<ErrorInfo>() {
					@Override public boolean process(final ErrorInfo x) { System.out.println(x.stacktrace); return true; }
					@Override public void onSuccess(final TaskRequest<ErrorInfo> req) { System.out.println("On Success"); onSuccess++; }
				},
				ErrorInfo.class));

		checkStats();
	}

	@Test
	public void testCalcNextRunAt()
	{
		assertNear("First try", System.currentTimeMillis() + 60000L, TaskManager.calcNextRunAt(1, 3600L), 10L);	// Should actually be with 1L millisecond, but this is good enough. DLS on 12/24/2014.
		assertNear("Second try", System.currentTimeMillis() + 120000L, TaskManager.calcNextRunAt(2, 3600L), 10L);
		assertNear("Third try", System.currentTimeMillis() + 240000L, TaskManager.calcNextRunAt(3, 3600L), 10L);
		assertNear("Fourth try", System.currentTimeMillis() + 480000L, TaskManager.calcNextRunAt(4, 3600L), 10L);
		assertNear("Fifth try", System.currentTimeMillis() + 960000L, TaskManager.calcNextRunAt(5, 3600L), 10L);
		assertNear("Sixth try", System.currentTimeMillis() + 1920000L, TaskManager.calcNextRunAt(6, 3600L), 10L);
		assertNear("Seventh try", System.currentTimeMillis() + 3600000L, TaskManager.calcNextRunAt(7, 3600L), 10L);
		assertNear("Eighth try", System.currentTimeMillis() + 3600000L, TaskManager.calcNextRunAt(8, 3600L), 10L);
		assertNear("Ninth try", System.currentTimeMillis() + 3600000L, TaskManager.calcNextRunAt(9, 3600L), 10L);
		assertNear("Tenth try", System.currentTimeMillis() + 3600000L, TaskManager.calcNextRunAt(10, 3600L), 10L);
		assertNear("Eleventh try", System.currentTimeMillis() + 3600000L, TaskManager.calcNextRunAt(11, 3600L), 10L);
	}

	private void assertNear(String message, long expected, long actual, long range)
	{
		assertThat(actual).isGreaterThanOrEqualTo(expected - range).isLessThanOrEqualTo(expected + range).as(message);
	}

	@Test
	public void testPower()
	{
		Assert.assertEquals("Power of 2 ^ 0", 1L, TaskManager.power(2L, 0));
		Assert.assertEquals("Power of 2 ^ 1", 2L, TaskManager.power(2L, 1));
		Assert.assertEquals("Power of 2 ^ 2", 4L, TaskManager.power(2L, 2));
		Assert.assertEquals("Power of 2 ^ 3", 8L, TaskManager.power(2L, 3));
		Assert.assertEquals("Power of 2 ^ 4", 16L, TaskManager.power(2L, 4));
		Assert.assertEquals("Power of 2 ^ 5", 32L, TaskManager.power(2L, 5));
		Assert.assertEquals("Power of 2 ^ 6", 64L, TaskManager.power(2L, 6));
		Assert.assertEquals("Power of 2 ^ 7", 128L, TaskManager.power(2L, 7));
		Assert.assertEquals("Power of 2 ^ 8", 256L, TaskManager.power(2L, 8));
		Assert.assertEquals("Power of 2 ^ 9", 512L, TaskManager.power(2L, 9));
		Assert.assertEquals("Power of 2 ^ 10", 1024L, TaskManager.power(2L, 10));

		Assert.assertEquals("Power of 5 ^ 0", 1L, TaskManager.power(5L, 0));
		Assert.assertEquals("Power of 5 ^ 1", 5L, TaskManager.power(5L, 1));
		Assert.assertEquals("Power of 5 ^ 2", 25L, TaskManager.power(5L, 2));
		Assert.assertEquals("Power of 5 ^ 3", 125L, TaskManager.power(5L, 3));
		Assert.assertEquals("Power of 5 ^ 4", 25L * 25L, TaskManager.power(5L, 4));
		Assert.assertEquals("Power of 5 ^ 5", 25L * 25L * 5L, TaskManager.power(5L, 5));
		Assert.assertEquals("Power of 5 ^ 6", 125L * 125L, TaskManager.power(5L, 6));
		Assert.assertEquals("Power of 5 ^ 7", 125L * 125L * 5L, TaskManager.power(5L, 7));
		Assert.assertEquals("Power of 5 ^ 8", 25L * 25L * 25L * 25L, TaskManager.power(5L, 8));
		Assert.assertEquals("Power of 5 ^ 9", 125L * 125L * 125L, TaskManager.power(5L, 9));
		Assert.assertEquals("Power of 5 ^ 10", 125L * 125L * 25L * 25L, TaskManager.power(5L, 10));
	}

	@Test
	public void testRun()
	{
		manager.turnOff();	// Ensure that the manager is unavailable otherwise the 'run' method will never exit.
		Assert.assertEquals("Check beforeRun", 0, beforeRun_);
		Assert.assertEquals("Check afterRun", 0, afterRun_);

		manager.run();
		Assert.assertEquals("Check beforeRun", 0, beforeRun_);
		Assert.assertEquals("Check afterRun", 0, afterRun_);

		manager.withBeforeRun(beforeRun).run();
		Assert.assertEquals("Check beforeRun", 1, beforeRun_);
		Assert.assertEquals("Check afterRun", 0, afterRun_);

		manager.withAfterRun(afterRun).run();
		Assert.assertEquals("Check beforeRun", 2, beforeRun_);
		Assert.assertEquals("Check afterRun", 1, afterRun_);

		manager.withBeforeRun(null).run();
		Assert.assertEquals("Check beforeRun", 2, beforeRun_);
		Assert.assertEquals("Check afterRun", 2, afterRun_);

		manager.withAfterRun(null).run();
		Assert.assertEquals("Check beforeRun", 2, beforeRun_);
		Assert.assertEquals("Check afterRun", 2, afterRun_);
	}

	@Test
	public void testSuccessfulProcessing() throws Exception
	{
		addRequests();
		checkRequests();

		processRequests(0);
		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, SIZE, 0, 0));
	}

	@Test
	public void testUnsuccessfulProcessing() throws Exception
	{
		final int count = addRequests();

		checkRequests();

		operator.maxTries = 1;	// Testing moving of failed requests to the DLQ. DLS on 7/14/2016.
		operator.callback = x -> {
			if ("Error 4".equals(x.message))
				throw new RuntimeException();

			System.out.println(x.stacktrace); return true;
		};
		Assert.assertEquals("Process Update Job Request: check queue size", count, queue.getQueueSize(QUEUE_NAME));
		checkStats(new OperatorStats(QUEUE_NAME, count, 0, 0, 0, 0));

		long ranAt = processRequests(0, 1);

		List<TaskRequest<?>> values = manager.listRequests(TaskOperator.dlq(QUEUE_NAME));
		Assert.assertEquals("List requests size", 1, values.size());
		TaskRequest<?> value = values.get(0);
		Assert.assertNotNull("nextRunAt exists", value.nextRunAt);
		assertNear("nextRunAt should be about a minute out", ranAt + 60000L, value.nextRunAt, 6000L);
		Assert.assertEquals("Number of tries", 1, value.tries);
		Object request = value.value;
		Assert.assertTrue("Remaining error instanceOf Exception", request instanceof ErrorInfo);
		Assert.assertEquals("Remaining error", "Error 4", ((ErrorInfo) request).message);
		checkStats(new OperatorStats(QUEUE_NAME, 0, 1, count - 1, 0, 1));

		processRequests(0, 1);
		checkStats(new OperatorStats(QUEUE_NAME, 0, 1, count - 1, 0, 1));	// Since request is on the DLQ, it is NOT processed to generate the error.

		// Move the DLQ items back to the operational queue.
		final int total = manager.countRequests(QUEUE_NAME);
		Assert.assertEquals("Check moved requests", 1, manager.moveRequests(QUEUE_NAME));
		Assert.assertEquals("Check moved count", total + 1, manager.countRequests(QUEUE_NAME));
		Assert.assertEquals("Check moved count", 0, manager.countRequests(TaskOperator.dlq(QUEUE_NAME)));
		checkStats(new OperatorStats(QUEUE_NAME, 1, 0, count - 1, 0, 1));
	}

	@Test
	public void testClearingOfRequests() throws Exception
	{
		addRequests();
		checkRequests();
		Assert.assertEquals("Check the size of the Request queue", SIZE, queue.getQueueSize(QUEUE_NAME));
		Assert.assertEquals("Clear the Request queue", SIZE, manager.clearRequests(QUEUE_NAME));
		Assert.assertEquals("Check the size of the Request queue after clearing", 0, queue.getQueueSize(QUEUE_NAME));

		checkStats();	// Empty now.
	}

	@Test
	public void testProcessAll() throws Exception
	{
		manager.turnOn();	// Make sure that it's available to process manually though.
		addRequests();
		checkRequests();
		Assert.assertEquals("Check count", SIZE, manager.process(QUEUE_NAME));
		Assert.assertEquals("Check count", 0, queue.getQueueSize(QUEUE_NAME));

		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, SIZE, 0, 0));
	}

	@Test
	public void testProcessOne() throws Exception
	{
		manager.turnOn();	// Make sure that it's available to process manually though.
		addRequests();
		checkRequests();

		int i = 0;
		List<TaskRequest<?>> requests = manager.listRequests(QUEUE_NAME);
		for (TaskRequest<?> request : requests)
		{
			Assert.assertTrue("Check " + (++i), manager.process(QUEUE_NAME, request.id));
			Assert.assertEquals("Check count: " + i, SIZE - i, queue.getQueueSize(QUEUE_NAME));
			checkStats(new OperatorStats(QUEUE_NAME, SIZE - i, 0, i, 0, 0));
		}

		Assert.assertEquals("Check count", 0, queue.getQueueSize(QUEUE_NAME));
		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, SIZE, 0, 0));
	}

	@Test
	public void testModifyRequest() throws Exception
	{
		manager.turnOn();	// Make sure that it's available to process manually though.
		addRequests();
		checkRequests();

		int i = 0;
		Set<String> oldIds = new HashSet<>(SIZE);
		List<TaskRequest<?>> requests = manager.listRequests(QUEUE_NAME);
		for (TaskRequest<?> request : requests)
		{
			i++;
			TaskRequest<?> newRequest = manager.modifyRequest(QUEUE_NAME, new TaskRequest<String>(request.id,
				String.format(REQUEST_JSON, i), 0, null));

			// Make sure that the old and new IDs do NOT match.
			Assert.assertNotEquals("Check IDs", request.id, newRequest.id);
			oldIds.add(request.id);

			// Should NOT change size.
			Assert.assertEquals("Check count", SIZE, queue.getQueueSize(QUEUE_NAME));
		}

		// Make sure that there are still the initial number of requests.
		Assert.assertEquals("Check count", SIZE, queue.getQueueSize(QUEUE_NAME));

		// Make sure that the none of the new task request IDs match the prior old IDs.
		i = 0;
		requests = manager.listRequests(QUEUE_NAME);
		for (TaskRequest<?> request : requests)
			Assert.assertFalse("Check " + (++i), oldIds.contains(request.id));

		checkStats(new OperatorStats(QUEUE_NAME, SIZE, 0, 0, 0, 0));

		// Make sure that the modified requests are process-able.
		manager.process(QUEUE_NAME);
		Assert.assertEquals("Check count", 0, queue.getQueueSize(QUEUE_NAME));
		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, SIZE, 0, 0));
	}

	private int addRequests() throws Exception
	{
		int i = 0;
		String assertId = "Add Update Job Request: check queue size";
		for (ErrorInfo request : REQUESTS)
		{
			queue.pushTask(QUEUE_NAME, new TaskRequest<ErrorInfo>(request));
			Assert.assertEquals(assertId, ++i, queue.getQueueSize(QUEUE_NAME));
		}

		return i;
	}

	private void checkRequests() throws Exception
	{
		checkStats(new OperatorStats(QUEUE_NAME, SIZE, 0, 0, 0, 0));

		int i = 1;
		List<TaskRequest<?>> values = manager.listRequests(QUEUE_NAME);
		Assert.assertEquals(SIZE, values.size());

		for (TaskRequest<?> value : values)
		{
			String assertId = "List Request(" + i++ + "): ";
			Assert.assertTrue(assertId + "instanceOf check", value.value instanceof ErrorInfo);
			Assert.assertTrue(assertId + "contains check", REQUESTS.contains(value.value));
		}
	}

	private void checkStats()
	{
		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, 0, 0, 0));
	}

	private void checkStats(final OperatorStats expected)
	{
		final List<OperatorStats> values = manager.stats();
		Assert.assertNotNull("Exists", values);
		Assert.assertEquals("Check size", 1, values.size());

		final OperatorStats actual = values.get(0);
		Assert.assertEquals("Check name", expected.name, actual.name);
		Assert.assertEquals("Check queueSize", expected.queueSize, actual.queueSize);
		Assert.assertEquals("Check dlqSize", expected.dlqSize, actual.dlqSize);
		Assert.assertEquals("Check successes", expected.successes, actual.successes);
		Assert.assertEquals("Check skips", expected.skips, actual.skips);
		Assert.assertEquals("Check errors", expected.errors, actual.errors);
	}

	private long processRequests(int remainingRequests) throws Exception
	{
		return processRequests(remainingRequests, 0);
	}

	private long processRequests(int remainingRequests, int dlqRequests) throws Exception
	{
		int threadsCount = Thread.activeCount();  // Get the thread count before creation of the task-manager thread.
		manager.start();	// Try this in the background.
		long runAt = System.currentTimeMillis();	// Calc closest to processing.

		Thread.sleep(2000L);	// After two seconds all the items should be processed since JSClient is mocked.

		String assertId = "Process Requests: ";
		Assert.assertEquals(assertId + "thread count while running", threadsCount + 1, Thread.activeCount());

		manager.stop();	// Should stop successfully.

		System.gc();	// Cleans up thread before checking the count below.
		Assert.assertEquals(assertId + "check queue size", remainingRequests, queue.getQueueSize(QUEUE_NAME));
		Assert.assertEquals(assertId + "check DLQ size", dlqRequests, queue.getQueueSize(TaskOperator.dlq(QUEUE_NAME)));

		// Thread.sleep(3000L);	// NOT needed anymore. "stop" method does not exit until all threads are dead.	// Wait a second for the thread to be cleared out.
		Assert.assertEquals(assertId + "thread count after shutdown", threadsCount, Thread.activeCount());

		return runAt;
	}

	@Test
	public void processX() throws Exception
	{
		Assert.assertEquals("Check onSuccess", 0, onSuccess);

		addRequests();

		managerX.start();
		ThreadUtils.sleep(2000L);
		managerX.stop();

		Assert.assertEquals("Check onSuccess", 8, onSuccess);
	}

	@Test
	public void testThrottling() throws Exception
	{
		var i = new int[] { 0 };
		var queueName = "throttled";
		var operator = new TaskOperator<String>(queueName, x -> { System.out.println("Throttle: " + (++i[0])); if (5 == i[0]) throw new ThrottledException(x); return true; }, String.class);
		var manager = new TaskManager(queue, 1, operator);

		for (int j = 0; j < 10; j++) queue.pushTask(queueName, new TaskRequest<String>("Request: " + j));

		manager.turnOn();
		Assert.assertEquals("Check queueSize: before", 10, queue.getQueueSize(queueName));
		Assert.assertEquals("Check process", 4, manager.process());
		Assert.assertEquals("Check queueSize: before", 6, queue.getQueueSize(queueName));
	}
}
