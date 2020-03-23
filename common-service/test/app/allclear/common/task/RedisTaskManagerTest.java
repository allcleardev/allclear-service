package app.allclear.common.task;

import org.junit.*;

import app.allclear.junit.redis.RedisServerRule;
import app.allclear.common.errors.ErrorInfo;
import app.allclear.common.redis.*;

/** Function test that verifies the TaskManager and the RedisClient.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class RedisTaskManagerTest extends TaskManagerTest
{
	@ClassRule
	public static final RedisServerRule SERVER = new RedisServerRule();

	@Before
	public void up() throws Exception
	{
		var client = new RedisClient(new RedisConfig("localhost", 6378, 200L, 5));
		client.clear();	// Always start from scratch.

		reset();
		queue = new RedisQueue(client);
		manager = new TaskManager(queue, 1,
			operator = new TaskOperator<ErrorInfo>(QUEUE_NAME, x -> { System.out.println(x.stacktrace); return true; }, ErrorInfo.class));
		managerX = new TaskManager(queue, 1,
			operatorX = new TaskOperator<ErrorInfo>(QUEUE_NAME,
				new TaskCallback<ErrorInfo>() {
					@Override public boolean process(final ErrorInfo x) { System.out.println(x.stacktrace); return true; }
					@Override public void onSuccess(final TaskRequest<ErrorInfo> req) { System.out.println("On Success"); onSuccess++; }
				},
				ErrorInfo.class));
	}
}
