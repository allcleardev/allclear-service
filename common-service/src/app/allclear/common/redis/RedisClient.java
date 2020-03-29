package app.allclear.common.redis;

import java.io.Closeable;
import java.util.*;
import java.util.function.Function;

import redis.clients.jedis.*;

import com.codahale.metrics.health.HealthCheck;

/** Class that provides access to Redis through the standard Java Map interface.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class RedisClient extends HealthCheck implements Closeable, Map<String, String>
{
	/** Represents the Redis failure code. */
	public static final Long FAIL = Long.valueOf(0L);

	/** Represents the return value from the "remove" method if no keys were removed. */
	public static final String EMPTY = "0";

	/** Jedis connection pool. */
	private final JedisPool pool;

	/** Default/empty - called from derived classes. */
	protected RedisClient()
	{
		pool = null;
	}

	/** Populator.
	 * 
	 * @param conf
	 */
	public RedisClient(final RedisConfig conf)
	{
		final JedisPoolConfig config = new JedisPoolConfig();
		config.setTestWhileIdle(conf.testWhileIdle);
		if (null != conf.poolSize) config.setMaxTotal(conf.poolSize);
		if (null != conf.timeout) config.setMaxWaitMillis(conf.timeout);

		pool = new JedisPool(config, conf.host, conf.port, 500, conf.password, conf.ssl);
	}

	@Override
	public void close()
	{
		pool.close();
	}

	/*******************************************************************************************************************
	 * 
	 * Map methods
	 * 
	 ******************************************************************************************************************/

	@Override
	public void clear() { try (var cache = pool.getResource()) { cache.flushDB(); } }

	@Override
	public boolean containsKey(Object key) { try (var cache = pool.getResource()) { return cache.exists((String) key); } }

	@Override
	public boolean containsValue(Object value) { return false;	/* Not implemented. */ }

	@Override
	public Set<Map.Entry<String, String>> entrySet() { return Set.of(); /* NOT implemented. */ }

	@Override
	public String get(Object key) { try (var cache = pool.getResource()) { return cache.get((String) key); } }

	@Override
	public int hashCode() { return pool.hashCode(); }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof RedisClient)) return false;

		return ((RedisClient) o).pool == pool;
	}

	@Override
	public boolean isEmpty() { return 0 == size(); }

	@Override
	public Set<String> keySet() { try (var cache = pool.getResource()) { return cache.keys("*"); } }

    /** Gets a set of keys by the specified search prefix.
     *
     * @param startsWith wildcard search prefix.
     * @return never NULL
     */
    public Set<String> keys(String startsWith) { try (var cache = pool.getResource()) { return cache.keys(startsWith + "*"); } }

	@Override
	public String put(String key, String value) { try (var cache = pool.getResource()) { return cache.set(key, value); } }

	/** Put a key/value pair with an timeout.
	 * 
	 * @param key
	 * @param value
	 * @param seconds numbers later to expire the key/value pair.
	 */
	public String put(String key, String value, int seconds)
	{
		try (var cache = pool.getResource()) { return cache.setex(key, seconds, value); }
	}

	/** Sets a new expiration value for an existing key.
	 * 
	 * @param key
	 * @param seconds numbers later to expire the key/value pair.
	 * @return TRUE indicates that the key exists and the expiration was set.
	 */
	public boolean expire(String key, int seconds)
	{
		try (var cache = pool.getResource()) { return !FAIL.equals(cache.expire(key, seconds)); }
	}

	/** Gets the number of seconds till expiration for the specified key.
	 * 
	 * @param key
	 * @return NULL if the key is invalid or does not have an expiration.
	 */
	public Integer expires(String key)
	{
		Long value = ttl(key);

		return (0L <= value) ? value.intValue() : null;
	}

	/** Gets the number of seconds till expiration for the specified key.
	 * 
	 * @param key
	 * @return a negative number of the key is invalid or does not have an expiration.
	 */
	public Long ttl(String key)
	{
		try (var cache = pool.getResource()) { return cache.ttl(key); }
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> newValues)
	{
		try (var cache = pool.getResource())
		{
			for (Map.Entry<? extends String, ? extends String> entry : newValues.entrySet())
				cache.set(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String remove(Object key) { try (var cache = pool.getResource()) { return cache.del((String) key) + ""; } }

	@Override
	public int size() { try (var cache = pool.getResource()) { return cache.dbSize().intValue(); } }

	@Override
	public Collection<String> values() { return List.of(); /* NOT implemented. */ }

	/*******************************************************************************************************************
	 * 
	 * List/queue methods
	 * 
	 ******************************************************************************************************************/

	/** Adds a textual value to the specified Redis queue.
	 * 
	 * @param queueName
	 * @param value
	 */
	public void push(String queueName, String value)
	{
		try (var cache = pool.getResource()) { cache.rpush(queueName, value); }
	}

	/** Retrieves and removes a textual value from the specified Redis queue. */
	public String pop(String queueName)
	{
		try (var cache = pool.getResource()) { return cache.lpop(queueName); }
	}

	/** Partially lists items from the specified Redis queue.
	 * 
	 * @param queueName
	 * @return NULL if empty.
	 */
	public List<String> list(String queueName)
	{
		try (var cache = pool.getResource()) { return cache.lrange(queueName, 0, queueSize(queueName) - 1); }
	}

	/** Partially lists items from the specified Redis queue.
	 * 
	 * @param queueName
	 * @param page
	 * @param pageSize
	 * @return NULL if empty.
	 */
	public List<String> list(String queueName, int page, int pageSize)
	{
		try (var cache = pool.getResource())
		{
			final long size = cache.llen(queueName);
			if (0L == size)
				return null;

			final long[] firstAndLast = listIndexes(size, page, pageSize);
			return cache.lrange(queueName, firstAndLast[0], firstAndLast[1]);
		}
	}

	/** Helper method - gets the start and end list indexes. */
	protected long[] listIndexes(long size, int page, int pageSize)
	{
		long end = (long) (page * pageSize);
		if (end > size)
			end = size;

		return new long[] { (long) ((page - 1) * pageSize), end - 1L };
	}

	/** Clears the entire specified Redis queue.
	 * 
	 * @param queueName
	 * @return the number of items removed.
	 */
	public int unqueue(String queueName)
	{
		/** The value returned from the "del" command is not the count of items in the queue. */
		try (var cache = pool.getResource())
		{
			int size = cache.llen(queueName).intValue();
			if (0 == size)
				return 0;

			cache.del(queueName);

			return size;
		}
	}

	/** Removes items from the queue that match the value. */
	public boolean unqueue(String queueName, String value)
	{
		return unqueue(queueName, value, 1L);
	}

	/** Removes the specified number of items from the queue that match the value. */
	public boolean unqueue(String queueName, String value, long count)
	{
		try (var cache = pool.getResource())
		{
			return (0L < cache.lrem(queueName, count, value));
		}
	}

	/** Gets the number of items in the specified Redis queue.
	 * 
	 * @param queueName
	 * @return zero if none found.
	 */
	public int queueSize(String queueName)
	{
		try (var cache = pool.getResource()) { return cache.llen(queueName).intValue(); }
	}

	/*******************************************************************************************************************
	 * 
	 * Set methods
	 * 
	 ******************************************************************************************************************/

	/** Adds a textual item to the specified Redis set.
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, String value)
	{
		try (var cache = pool.getResource()) { cache.sadd(key, value); }
	}

	/** Gets a textual value from the specified Redis queue. */
	public Set<String> set(String key)
	{
		try (var cache = pool.getResource()) { return cache.smembers(key); }
	}

	/** Gets the size of the set.
	 * 
	 * @param key
	 * @return zero if the set does not exist.
	 */
	public int setSize(String key)
	{
		try (var cache = pool.getResource()) { return cache.scard(key).intValue(); }
	}

	/** Removes one or more items from the specified Redis set.
	 * 
	 * @param key
	 * @param values
	 */
	public void unset(String key, String... values)
	{
		try (var cache = pool.getResource()) { cache.srem(key, values); }
	}

	/*******************************************************************************************************************
	 * 
	 * Hash/map methods
	 * 
	 ******************************************************************************************************************/

	/** Adds key-value pairs to a hash/map.
	 * 
	 * @param key
	 * @param field
	 * @param value
	 */
	public void hash(final String key, final String field, final String value)
	{
		try (var cache = pool.getResource()) { cache.hset(key, field, value); }
	}

	/** Gets a value from the hash/map by key and field.
	 * 
	 * @param key
	 * @param field
	 * @return NULL if not found.
	 */
	public String hash(final String key, final String field)
	{
		try (var cache = pool.getResource()) { return cache.hget(key, field); }
	}

	/** Gets the full hash/map by the key.
	 * 
	 * @param key
	 * @return NULL if not found.
	 */
	public Map<String, String> hash(final String key)
	{
		try (var cache = pool.getResource()) { return cache.hgetAll(key); }
	}

	/** Adds key-value pairs to a hash/map.
	 * 
	 * @param key
	 * @param field
	 * @param value
	 */
	public void hash(final String key, final Map<String, String> values)
	{
		try (var cache = pool.getResource()) { cache.hset(key, values); }
	}

	/** Gets the number of key-value pairs in the hash/map.
	 * 
	 * @param key
	 * @return zero if none found.
	 */
	public int hashSize(final String key)
	{
		try (var cache = pool.getResource()) { return cache.hlen(key).intValue(); }
	}

	/** Removes a key-value pairs from a hash/map.
	 * 
	 * @param key
	 * @param field
	 */
	public void unhash(final String key, final String... fields)
	{
		try (var cache = pool.getResource()) { cache.hdel(key, fields); }
	}

	/** Runs the specified function with a Jedis cache. Used to perform multi Redis cache actions with the same connection.
	 * 
	 * @param <R>
	 * @param fx
	 * @return value of the supplied function.
	 */
	public <R> R operation(final Function<Jedis, R> fx)
	{
		try (var cache = pool.getResource()) { return fx.apply(cache); }
	}

	/*******************************************************************************************************************
	 * 
	 * HealthCheck methods
	 * 
	 ******************************************************************************************************************/

	/** Sends a heart beat check to the Redis server.
	 * 
	 * @return textual heart beat message.
	 * @throws Exception
	 */
	public String ping() throws Exception
	{
		try (var cache = pool.getResource())
		{
			cache.ping();

			return "REDIS: ping successful";
		}
	}

	@Override
	public Result check() throws Exception
	{
		return Result.healthy(ping());
	}
}
