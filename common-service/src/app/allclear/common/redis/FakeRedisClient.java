package app.allclear.common.redis;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;

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
	/** Default/empty. */
	public FakeRedisClient()
	{
		super();
	}

	/** Internal structures. */
	private final Map<String, String> map = new HashMap<>();
	private final Map<String, Integer> expirations = new HashMap<>();
	private final Map<String, Queue<String>> queues = new HashMap<>();
	private final Map<String, Set<String>> sets = new HashMap<>();
	private final Map<String, Map<String, String>> maps = new HashMap<>();

	@Override
	public void close()
	{
		clear();
	}

	/*******************************************************************************************************************
	 * 
	 * Map methods
	 * 
	 ******************************************************************************************************************/

	@Override
	public void clear()
	{
		map.clear();
		queues.clear();
		sets.clear();
		maps.clear();
	}

	@Override
	public boolean containsKey(Object key) { return map.containsKey(key) || maps.containsKey(key) || sets.containsKey(key) || queues.containsKey(key); }

	@Override
	public boolean containsValue(Object value) { return map.containsValue(value); }

	@Override
	public Set<Map.Entry<String, String>> entrySet() { return map.entrySet(); }

	@Override
	public String get(Object key) { return map.get((String) key); }

	@Override
	public int hashCode() { return map.hashCode(); }

	@Override
	public boolean equals(final Object o) { return map.equals(o); }

	@Override
	public boolean isEmpty() { return 0 == size(); }

	@Override
	public Set<String> keySet()
	{
		Set<String> values = new HashSet<>(size());
		values.addAll(map.keySet());
		values.addAll(queues.keySet());
		values.addAll(sets.keySet());
		values.addAll(maps.keySet());

		return values;
	}

    /** Gets a set of keys by the specified search prefix.
     *
     * @param startsWith wildcard search prefix.
     * @return never NULL
     */
	@Override
    public Set<String> keys(final String startsWith)
    {
    	return keySet().stream().filter(v -> v.startsWith(startsWith)).collect(Collectors.toSet());
    }

	@Override
	public String put(String key, String value) { return map.put(key, value); }

	/** Put a key/value pair with an timeout.
	 * 
	 * @param key
	 * @param value
	 * @param seconds numbers later to expire the key/value pair.
	 */
	@Override
	public String put(String key, String value, int seconds)
	{
		expirations.put(key, seconds);

		return map.put(key, value);
	}

	/** Sets a new expiration value for an existing key.
	 * 
	 * @param key
	 * @param seconds numbers later to expire the key/value pair.
	 * @return TRUE indicates that the key exists and the expiration was set.
	 */
	@Override
	public boolean expire(String key, int seconds)
	{
		expirations.put(key, seconds);
		return true;
	}

	/** Gets the number of seconds till expiration for the specified key.
	 * 
	 * @param key
	 * @return NULL if the key is invalid or does not have an expiration.
	 */
	@Override
	public Integer expires(String key) { return expirations.get(key); }

	/** Gets the number of seconds till expiration for the specified key.
	 * 
	 * @param key
	 * @return a negative number of the key is invalid or does not have an expiration.
	 */
	@Override
	public Long ttl(String key)
	{
		Integer value = expirations.get(key);
		return (null != value) ? value.longValue() : 0L;
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> newValues) { map.putAll(newValues); }

	@Override
	public String remove(Object key)
	{
		var v = map.remove(key);
		if (null != v) return v;

		var vv = maps.remove(key);
		if (null != vv) return vv.toString();

		return null;
	}

	@Override
	public int size()
	{
		return map.size() + queues.size() + sets.size() + maps.size();
	}

	@Override
	public Collection<String> values() { return null; /* NOT implemented. */ }

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
	@Override
	public void push(String queueName, String value)
	{
		queues.computeIfAbsent(queueName, k -> new LinkedList<>()).offer(value);
	}

	/** Retrieves and removes a textual value from the specified Redis queue. */
	@Override
	public String pop(String queueName)
	{
		var queue = queues.get(queueName);
		if (null == queue) return null;

		return queue.poll();
	}

	/** Partially lists items from the specified Redis queue.
	 * 
	 * @param queueName
	 * @return NULL if empty.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> list(String queueName)
	{
		return (List<String>) queues.get(queueName);
	}

	/** Partially lists items from the specified Redis queue.
	 * 
	 * @param queueName
	 * @param page
	 * @param pageSize
	 * @return NULL if empty.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> list(String queueName, int page, int pageSize)
	{
		List<String> values = (List<String>) queues.get(queueName);
		int size = values.size();
		if (0 == size)
			return null;

		final long[] firstAndLast = listIndexes(size, page, pageSize);
		return values.subList((int) firstAndLast[0], ((int) firstAndLast[1]) + 1);
	}

	/** Clears the entire specified Redis queue.
	 * 
	 * @param queueName
	 * @return the number of items removed.
	 */
	@Override
	public int unqueue(String queueName)
	{
		Queue<String> values = queues.get(queueName);
		if (null == values)
			return 0;

		queues.remove(queueName);
		return values.size();
	}

	/** Removes items from the queue that match the value. */
	@Override
	public boolean unqueue(String queueName, String value)
	{
		return unqueue(queueName, value, 1L);
	}

	/** Removes the specified number of items from the queue that match the value. */
	@Override
	public boolean unqueue(String queueName, String value, long count)
	{
		var values = queues.get(queueName);
		return (null == values) ? false : values.remove(value);
	}

	/** Gets the number of items in the specified Redis queue.
	 * 
	 * @param queueName
	 * @return zero if none found.
	 */
	@Override
	public int queueSize(String queueName)
	{
		var values = queues.get(queueName);
		return (null != values) ? values.size() : 0;
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
	@Override
	public void set(String key, String value)
	{
		sets.computeIfAbsent(key, k -> new HashSet<>()).add(value);
	}

	/** Gets a textual value from the specified Redis queue. */
	@Override
	public Set<String> set(String key)
	{
		return sets.get(key);
	}

	/** Gets the size of the set.
	 * 
	 * @param key
	 * @return zero if the set does not exist.
	 */
	@Override
	public int setSize(String key)
	{
		var values = sets.get(key);
		return (null != values) ? values.size() : 0;
	}

	/** Removes one or more items from the specified Redis set.
	 * 
	 * @param key
	 * @param items
	 */
	@Override
	public void unset(String key, String... items)
	{
		var values = sets.get(key);
		if (null == values) return;

		for (var i : items)
			values.remove(i);
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
	@Override
	public void hash(String key, String field, String value)
	{
		maps.computeIfAbsent(key, k -> new HashMap<>()).put(field, value);
	}

	/** Gets a value from the hash/map by key and field.
	 * 
	 * @param key
	 * @param field
	 * @return NULL if not found.
	 */
	@Override
	public String hash(String key, String field)
	{
		var values = maps.get(key);
		return (null != values) ? values.get(field) : null;
	}

	/** Gets the full hash/map by the key.
	 * 
	 * @param key
	 * @return NULL if not found.
	 */
	@Override
	public Map<String, String> hash(String key)
	{
		return maps.get(key);
	}

	/** Adds key-value pairs to a hash/map.
	 * 
	 * @param key
	 * @param field
	 * @param value
	 */
	@Override
	public void hash(final String key, final Map<String, String> values)
	{
		if (MapUtils.isEmpty(values)) return;

		var map = maps.computeIfAbsent(key, k -> new HashMap<>());
		values.forEach((k, v) -> map.put(k, v));
	}

	/** Gets the number of key-value pairs in the hash/map.
	 * 
	 * @param key
	 * @return zero if none found.
	 */
	@Override
	public int hashSize(String key)
	{
		var values = maps.get(key);
		return (null != values) ? values.size() : 0;
	}

	/** Removes a key-value pairs from a hash/map.
	 * 
	 * @param key
	 * @param field
	 */
	@Override
	public void unhash(String key, String... fields)
	{
		var values = maps.get(key);
		if (null == values) return;

		for (var f : fields)
			values.remove(f);
	}

	/** Runs the specified function with a Jedis cache. Used to perform multi Redis cache actions with the same connection.
	 * 
	 * @param <R>
	 * @param fx
	 * @return value of the supplied function.
	 */
	@Override
	public <R> R operation(final Function<Jedis, R> fx)
	{
		@SuppressWarnings("resource") var me = this;
		return fx.apply(new Jedis() {
			@Override public Long del(final String key) { me.remove(key); return 1L; }
			@Override public Boolean exists(final String key) { return me.containsKey(key); }
			@Override public Long expire(final String key, final int seconds) { me.expire(key, seconds); return 1L; }
			@Override public String get(final String key) { return me.get(key); }
			@Override public ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor, final ScanParams params) { return new ScanResult<Map.Entry<String, String>>("0", new ArrayList<>(me.maps.get(key).entrySet())); };
			@Override public Map<String, String> hgetAll(final String key) { return me.hash(key); }
			@Override public Long hset(final String key, final Map<String, String> values) { me.hash(key, values); return 1L; }
			@Override public ScanResult<String> scan(final String cursor, final ScanParams params) {
				var keys = me.keySet();
				var m = new HashMap<byte[], String>(2);
				var l = new ArrayList<>(params.getParams());
				for (int i = 0; i < l.size(); i+= 2)
					m.put(l.get(i), new String(l.get(i + 1)));

				var key = m.get(Protocol.Keyword.MATCH.raw);
				if ((null != key) && key.endsWith("*")) key = key.substring(0, key.length() - 1);	// Remove trailing asterisk (*).
				var k = key;

				return new ScanResult<>("0", keys.stream().filter(v -> (null == k) || v.startsWith(k)).collect(Collectors.toList()));
			}
			@Override public String setex(final String key, final int seconds, final String value) { return me.put(key, value, seconds); }
			@Override public Long ttl(final String key) {
				var o = me.expirations.get(key);
				return (null != o) ? o.longValue() : null;
			}
		});
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
	@Override
	public String ping() throws Exception
	{
		return "REDIS: ping successful";
	}
}
