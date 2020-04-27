package app.allclear.common.redis;

import redis.clients.jedis.*;

/** RedisClient implementation that uses non-remote data structures. Used for unit tests.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class FakeRedisClient extends RedisClient
{
	public FakeRedisClient()
	{
		this(new JedisPool() {
			private final FakeJedis jedis = new FakeJedis();
			@Override public Jedis getResource() { return jedis; }
		});
	}

	private FakeRedisClient(final JedisPool pool)
	{
		super(pool);
	}

	@Override
	public void close()
	{
		clear();
	}

	/** Sends a heart beat check to the Redis server.
	 * 
	 * @return textual heart beat message.
	 * @throws Exception
	 */
	@Override
	public String ping() throws Exception
	{
		return "REDIS: ping successful";
	}
}
