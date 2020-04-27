package app.allclear.redis;

import org.junit.jupiter.api.*;

import redis.clients.jedis.JedisPool;

/** Functional test class that verifies the FakeJedis component.
 * 
 * @author smalleyd
 * @version 1.1.5
 * @since 4/27/2020
 *
 */

public class FakeJedisTest
{
	private static final JedisPool pool = new JedisConfig(true).pool();

	@Test
	public void add()
	{
		try (var j = pool.getResource())
		{
			Assertions.assertNull(j.get("first"));
			Assertions.assertFalse(j.exists("first"));

			j.set("first", "123");
		}
	}

	@Test
	public void check()
	{
		try (var j = pool.getResource())
		{
			Assertions.assertEquals("123", j.get("first"));
			Assertions.assertTrue(j.exists("first"));
		}
	}
}
