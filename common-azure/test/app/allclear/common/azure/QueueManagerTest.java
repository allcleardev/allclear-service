package app.allclear.common.azure;

import static org.fest.assertions.api.Assertions.assertThat;
import static app.allclear.testing.TestingUtils.timestamp;

import java.util.Base64;
import java.util.Date;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.*;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import app.allclear.common.errors.ThrottledException;
import app.allclear.common.task.TaskOperator;

/** Functional test that verifies the AWSQueue component.
 * 
 * @author smalleyd
 * @version 2.1.18
 * @since 5/19/2016
 *
 */

@Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class QueueManagerTest
{
	public static final String QUEUE_NAME = "test-queue-manager";

	private static QueueClient queue;
	private static QueueManager queuer = null;
	private static Base64.Encoder encoder = Base64.getEncoder();
	private static final String connectionString = System.getenv("AZURE_QUEUE_CONNECTION_STRING");

	private static int beforeRun_ = 0;
	private static int afterRun_ = 0;
	private static final Runnable beforeRun = () -> beforeRun_++;
	private static final Runnable afterRun = () -> afterRun_++;

	@BeforeAll
	public static void up() throws Exception
	{
		queuer = new QueueManager(connectionString, 1, new TaskOperator<Request>(QUEUE_NAME,
			r -> { assertThat(r.numOfFruits).isGreaterThan(2); Assertions.assertEquals(timestamp("2017-02-23T11:11:0" + r.numOfFruits + "+0000"), r.expiration, "Check expiration: " + r.numOfFruits); return true; },
			Request.class, 10));
		queue = queuer.queue(QUEUE_NAME);
	}

	@Test
	public void addMessage() throws Exception
	{
		Assertions.assertNotNull(queue.sendMessage("{ \"numOfFruits\": 3, \"expiration\": \"2017-02-23T11:11:03+0000\" }"));
		Assertions.assertNotNull(queue.sendMessage("{ \"numOfFruits\": 4, \"expiration\": \"2017-02-23T11:11:04.000+0000\" }"));
		Assertions.assertNotNull(queue.sendMessage("{ \"numOfFruits\": 5, \"expiration\": \"2017-02-23T11:11:05.000-0000\" }"));
		Assertions.assertNotNull(queue.sendMessage(encoder.encodeToString("{ \"numOfFruits\": 6, \"expiration\": \"2017-02-23T11:11:06-0000\" }".getBytes())));
		Assertions.assertNotNull(queue.sendMessage("{ \"numOfFruits\": 7, \"expiration\": \"2017-02-23T11:11:07Z\" }"));
		Assertions.assertNotNull(queue.sendMessage("{ \"numOfFruits\": 8, \"expiration\": " + timestamp("2017-02-23T11:11:08+0000").getTime() + " }"));
		Assertions.assertNotNull(queue.sendMessage(QueueManager.MAPPER.writeValueAsString(new Request(9, timestamp("2017-02-23T11:11:09+0000")))));
	}

	@Test
	public void runQueue() throws Exception
	{
		var stats = queuer.stats();
		assertThat(stats).as("Check stats: before").hasSize(1);
		Assertions.assertEquals(7, stats.get(0).queueSize, "Check stats[0].queueSize: before");

		Assertions.assertEquals(0, queuer.process());	// Not turned on yet.

		queuer.turnOn();	// Make available for processing.

		Assertions.assertEquals(7, queuer.process());	// Process again.

		stats = queuer.stats();
		assertThat(stats).as("Check stats: after").hasSize(1);
		Assertions.assertEquals(0, stats.get(0).queueSize, "Check stats[0].queueSize: after");
	}

	@Test
	public void runQueue_checkEmpty() throws Exception
	{
		assertThat(Assertions.assertThrows(NoSuchElementException.class, () -> queue.receiveMessage())).hasMessage("Source was empty");
	}

	@Test
	public void testRun()
	{
		queuer.turnOff();	// Ensure that the manager is unavailable otherwise the 'run' method will never exit.
		Assertions.assertEquals(0, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(0, afterRun_, "Check afterRun");

		queuer.run();
		Assertions.assertEquals(0, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(0, afterRun_, "Check afterRun");

		queuer.withBeforeRun(beforeRun).run();
		Assertions.assertEquals(1, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(0, afterRun_, "Check afterRun");

		queuer.withAfterRun(afterRun).run();
		Assertions.assertEquals(2, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(1, afterRun_, "Check afterRun");

		queuer.withBeforeRun(null).run();
		Assertions.assertEquals(2, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(2, afterRun_, "Check afterRun");

		queuer.withAfterRun(null).run();
		Assertions.assertEquals(2, beforeRun_, "Check beforeRun");
		Assertions.assertEquals(2, afterRun_, "Check afterRun");
	}

	@Test
	public void testThrottling() throws Exception
	{
		var i = new int[] { 0 };
		var operator = new TaskOperator<Request>(QUEUE_NAME, x -> { System.out.println("Throttle: " + (++i[0])); if (5 == i[0]) throw new ThrottledException(x.toString()); return true; }, Request.class);
		var queuer_ = new QueueManager(connectionString, 1, operator).withBatchSize(1);

		var queue_ = queuer_.queue(QUEUE_NAME);
		for (int j = 0; j < 10; j++) queue_.sendMessage("{ \"numOfFruits\": " + j + " }");

		queuer_.turnOn();
		Assertions.assertEquals(4, queuer_.process(), "Check process");	// breaks after 4.

		int count = 0;
		for (var message : queue_.receiveMessages(10))
		{
			count++;
			queue_.deleteMessage(message.getMessageId(), message.getPopReceipt());
		}
		Assertions.assertEquals(6 - 1, count, "Check queueSize: before");	// One message will be in limbo when not processed after the ThrottledException is thrown. DLS on 2/14/2020.

		queue_.clearMessages();
	}

	/** Value object that represents a test request. */
	public static class Request
	{
		public final int numOfFruits;
		public final Date expiration;

		public Request(@JsonProperty("numOfFruits") final int numOfFruits,
			@JsonProperty("expiration") final Date expiration)
		{
			this.numOfFruits = numOfFruits;
			this.expiration = expiration;
		}

		@Override
		public String toString()
		{
			return new StringBuilder("{ numOfFruits: ").append(numOfFruits)
				.append(", expiration: ").append(expiration)
				.append(" }").toString();
		}
	}
}
