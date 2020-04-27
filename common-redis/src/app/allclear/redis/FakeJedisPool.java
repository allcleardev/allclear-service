package app.allclear.redis;

import redis.clients.jedis.*;

/** Implements the JedisPool interface with internal Java collections. Primarily used for testing.
 * 
 * @author smalleyd
 * @version 1.1.5
 * @since 4/27/2020
 *
 */

public class FakeJedisPool extends JedisPool
{
	private final FakeJedis resource = new FakeJedis();

	@Override public Jedis getResource() { return resource; }
}
