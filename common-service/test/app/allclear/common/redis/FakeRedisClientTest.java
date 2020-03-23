package app.allclear.common.redis;

import org.junit.*;
import org.junit.runners.MethodSorters;

/** Functional test class that verifies the RedisClient class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FakeRedisClientTest extends AbstractRedisClientTest
{
	@BeforeClass
	public static void up()
	{
		RedisClientTest.client = new FakeRedisClient();
		init();
	}
}
