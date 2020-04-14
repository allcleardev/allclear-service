package app.allclear.common.task;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
	protected TaskOperator<ErrorInfo> operatorInvalid = null;

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

	@BeforeEach
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

		operatorInvalid = new TaskOperator<ErrorInfo>(QUEUE_NAME, x -> {
			if ("Error 4".equals(x.message))
				throw new RuntimeException();

			System.out.println(x.stacktrace); return true;
		}, ErrorInfo.class, 1);
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
	public static Stream<Arguments> testPower()
	{
		return Stream.of(
			arguments(1L, 2L, 0),
			arguments(2L, 2L, 1),
			arguments(4L, 2L, 2),
			arguments(8L, 2L, 3),
			arguments(16L, 2L, 4),
			arguments(32L, 2L, 5),
			arguments(64L, 2L, 6),
			arguments(128L, 2L, 7),
			arguments(256L, 2L, 8),
			arguments(512L, 2L, 9),
			arguments(1024L, 2L, 10),

			arguments(1L, 5L, 0),
			arguments(5L, 5L, 1),
			arguments(25L, 5L, 2),
			arguments(125L, 5L, 3),
			arguments(25L * 25L, 5L, 4),
			arguments(25L * 25L * 5L, 5L, 5),
			arguments(125L * 125L, 5L, 6),
			arguments(125L * 125L * 5L, 5L, 7),
			arguments(25L * 25L * 25L * 25L, 5L, 8),
			arguments(125L * 125L * 125L, 5L, 9),
			arguments(125L * 125L * 25L * 25L, 5L, 10));
	}

	@ParameterizedTest
	@MethodSource
	public void testPower(final long expected, final long input, final int exponent)
	{
		Assertions.assertEquals(expected, TaskManager.power(input, exponent));
	}

	@Test
	public void testRun()
	{
		manager.turnOff();	// Ensure that the manager is unavailable otherwise the 'run' method will never exit.
		Assertions.assertEquals(0, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(0, afterRun_, "Check afterRun");

		manager.run();
		Assertions.assertEquals(0, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(0, afterRun_, "Check afterRun");

		manager.withBeforeRun(beforeRun).run();
		Assertions.assertEquals(1, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(0, afterRun_, "Check afterRun");

		manager.withAfterRun(afterRun).run();
		Assertions.assertEquals(2, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(1, afterRun_, "Check afterRun");

		manager.withBeforeRun(null).run();
		Assertions.assertEquals(2, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(2, afterRun_, "Check afterRun");

		manager.withAfterRun(null).run();
		Assertions.assertEquals(2, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(2, afterRun_, "Check afterRun");
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

		manager.removeOperator(QUEUE_NAME);
		manager.addOperator(operatorInvalid);

		Assertions.assertEquals(count, queue.getQueueSize(QUEUE_NAME), "Process Update Job Request: check queue size");
		checkStats(new OperatorStats(QUEUE_NAME, count, 0, 0, 0, 0));

		long ranAt = processRequests(0, 1);

		var values = manager.listRequests(TaskOperator.dlq(QUEUE_NAME));
		Assertions.assertEquals(1, values.size(), "List requests size");
		var value = values.get(0);
		Assertions.assertNotNull(value.nextRunAt, "nextRunAt exists");
		assertNear("nextRunAt should be about a minute out", ranAt + 60000L, value.nextRunAt, 6000L);
		Assertions.assertEquals(1, value.tries, "Number of tries");
		var request = value.value;
		assertThat(request).as("Remaining error instanceOf Exception").isInstanceOf(ErrorInfo.class);
		Assertions.assertEquals("Error 4", ((ErrorInfo) request).message, "Remaining error");
		checkStats(new OperatorStats(QUEUE_NAME, 0, 1, count - 1, 0, 1));

		processRequests(0, 1);
		checkStats(new OperatorStats(QUEUE_NAME, 0, 1, count - 1, 0, 1));	// Since request is on the DLQ, it is NOT processed to generate the error.

		// Move the DLQ items back to the operational queue.
		final int total = manager.countRequests(QUEUE_NAME);
		Assertions.assertEquals(1, manager.moveRequests(QUEUE_NAME), "Check moved requests");
		Assertions.assertEquals(total + 1, manager.countRequests(QUEUE_NAME), "Check moved count");
		Assertions.assertEquals(0, manager.countRequests(TaskOperator.dlq(QUEUE_NAME)), "Check moved count");
		checkStats(new OperatorStats(QUEUE_NAME, 1, 0, count - 1, 0, 1));
	}

	@Test
	public void testClearingOfRequests() throws Exception
	{
		addRequests();
		checkRequests();
		Assertions.assertEquals(SIZE, queue.getQueueSize(QUEUE_NAME), "Check the size of the Request queue");
		Assertions.assertEquals(SIZE, manager.clearRequests(QUEUE_NAME), "Clear the Request queue");
		Assertions.assertEquals(0, queue.getQueueSize(QUEUE_NAME), "Check the size of the Request queue after clearing");

		checkStats();	// Empty now.
	}

	@Test
	public void testProcessAll() throws Exception
	{
		manager.turnOn();	// Make sure that it's available to process manually though.
		addRequests();
		checkRequests();
		Assertions.assertEquals(SIZE, manager.process(QUEUE_NAME), "Check count");
		Assertions.assertEquals(0, queue.getQueueSize(QUEUE_NAME), "Check count");

		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, SIZE, 0, 0));
	}

	@Test
	public void testProcessOne() throws Exception
	{
		manager.turnOn();	// Make sure that it's available to process manually though.
		addRequests();
		checkRequests();

		int i = 0;
		for (var request : manager.listRequests(QUEUE_NAME))
		{
			Assertions.assertTrue(manager.process(QUEUE_NAME, request.id), "Check " + (++i));
			Assertions.assertEquals(SIZE - i, queue.getQueueSize(QUEUE_NAME), "Check count: " + i);
			checkStats(new OperatorStats(QUEUE_NAME, SIZE - i, 0, i, 0, 0));
		}

		Assertions.assertEquals(0, queue.getQueueSize(QUEUE_NAME), "Check count");
		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, SIZE, 0, 0));
	}

	@Test
	public void testModifyRequest() throws Exception
	{
		manager.turnOn();	// Make sure that it's available to process manually though.
		addRequests();
		checkRequests();

		int i = 0;
		var oldIds = new HashSet<String>(SIZE);
		var requests = manager.listRequests(QUEUE_NAME);
		for (var request : requests)
		{
			i++;
			var newRequest = manager.modifyRequest(QUEUE_NAME, new TaskRequest<String>(request.id,
				String.format(REQUEST_JSON, i), 0, null));

			// Make sure that the old and new IDs do NOT match.
			Assertions.assertNotEquals(request.id, newRequest.id, "Check IDs");
			oldIds.add(request.id);

			// Should NOT change size.
			Assertions.assertEquals(SIZE, queue.getQueueSize(QUEUE_NAME), "Check count");
		}

		// Make sure that there are still the initial number of requests.
		Assertions.assertEquals(SIZE, queue.getQueueSize(QUEUE_NAME), "Check count");

		// Make sure that the none of the new task request IDs match the prior old IDs.
		i = 0;
		requests = manager.listRequests(QUEUE_NAME);
		for (var request : requests)
			Assertions.assertFalse(oldIds.contains(request.id), "Check " + (++i));

		checkStats(new OperatorStats(QUEUE_NAME, SIZE, 0, 0, 0, 0));

		// Make sure that the modified requests are process-able.
		manager.process(QUEUE_NAME);
		Assertions.assertEquals(0, queue.getQueueSize(QUEUE_NAME), "Check count");
		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, SIZE, 0, 0));
	}

	private int addRequests() throws Exception
	{
		int i = 0;
		for (var request : REQUESTS)
		{
			queue.pushTask(QUEUE_NAME, new TaskRequest<ErrorInfo>(request));
			Assertions.assertEquals(++i, queue.getQueueSize(QUEUE_NAME), "Add Update Job Request: check queue size");
		}

		return i;
	}

	private void checkRequests() throws Exception
	{
		checkStats(new OperatorStats(QUEUE_NAME, SIZE, 0, 0, 0, 0));

		int i = 1;
		List<TaskRequest<?>> values = manager.listRequests(QUEUE_NAME);
		Assertions.assertEquals(SIZE, values.size());

		for (TaskRequest<?> value : values)
		{
			String assertId = "List Request(" + i++ + "): ";
			assertThat(value.value).as(assertId + "instanceOf check").isInstanceOf(ErrorInfo.class);
			Assertions.assertTrue(REQUESTS.contains(value.value), assertId + "contains check");
		}
	}

	private void checkStats()
	{
		checkStats(new OperatorStats(QUEUE_NAME, 0, 0, 0, 0, 0));
	}

	private void checkStats(final OperatorStats expected)
	{
		var values = manager.stats();
		Assertions.assertNotNull(values, "Exists");
		Assertions.assertEquals(1, values.size(), "Check size");

		var actual = values.get(0);
		Assertions.assertEquals(expected.name, actual.name, "Check name");
		Assertions.assertEquals(expected.queueSize, actual.queueSize, "Check queueSize");
		Assertions.assertEquals(expected.dlqSize, actual.dlqSize, "Check dlqSize");
		Assertions.assertEquals(expected.successes, actual.successes, "Check successes");
		Assertions.assertEquals(expected.skips, actual.skips, "Check skips");
		Assertions.assertEquals(expected.errors, actual.errors, "Check errors");
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

		var assertId = "Process Requests: ";
		Assertions.assertEquals(threadsCount + 1, Thread.activeCount(), assertId + "thread count while running");

		manager.stop();	// Should stop successfully.

		System.gc();	// Cleans up thread before checking the count below.
		Assertions.assertEquals(remainingRequests, queue.getQueueSize(QUEUE_NAME), assertId + "check queue size");
		Assertions.assertEquals(dlqRequests, queue.getQueueSize(TaskOperator.dlq(QUEUE_NAME)), assertId + "check DLQ size");

		// Thread.sleep(3000L);	// NOT needed anymore. "stop" method does not exit until all threads are dead.	// Wait a second for the thread to be cleared out.
		Assertions.assertEquals(threadsCount, Thread.activeCount(), assertId + "thread count after shutdown");

		return runAt;
	}

	@Test
	public void processX() throws Exception
	{
		Assertions.assertEquals(0, onSuccess, "Check onSuccess");

		addRequests();

		managerX.start();
		ThreadUtils.sleep(2000L);
		managerX.stop();

		Assertions.assertEquals(8, onSuccess, "Check onSuccess");
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
		Assertions.assertEquals(10, queue.getQueueSize(queueName), "Check queueSize: before");
		Assertions.assertEquals(4, manager.process(), "Check process");
		Assertions.assertEquals(6, queue.getQueueSize(queueName), "Check queueSize: before");
	}
}
