package app.allclear.common.redis;

import app.allclear.redis.FakeJedisPool;

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
		super(new FakeJedisPool());
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
