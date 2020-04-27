package app.allclear.common.redis;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;

import redis.clients.jedis.*;

/** Implements the Jedis interface with internal Java collections. Primarily used for testing.
 * 
 * @author smalleyd
 * @version 1.1.5
 * @since 4/27/2020
 *
 */

public class FakeJedis extends Jedis
{
	public FakeJedis()
	{
		// super(); NOT necessary
	}

	/** Internal structures. */
	private final Map<String, String> map = new HashMap<>();
	private final Map<String, Integer> expirations = new HashMap<>();
	private final Map<String, Queue<String>> queues = new HashMap<>();
	private final Map<String, Set<String>> sets = new HashMap<>();
	private final Map<String, Map<String, String>> maps = new HashMap<>();

	@Override
	public void close() { /** Do NOT clear the cache since this is called after each usage of the fake Jedis pool. */ }

	@Override
	public String get(final String key) { return map.get(key); }

	@Override
	public String set(final String key, final String value) { return map.put(key, value); }

	@Override
	public String setex(final String key, final int seconds, final String value)
	{
		expirations.put(key, seconds);

		return map.put(key, value);
	}

	@Override
	public Long ttl(final String key)
	{
		var o = expirations.get(key);
		return (null != o) ? o.longValue() : null;
	}

	@Override
	public Long del(final String key)
	{
		if (null != map.remove(key)) return 1L;
		if (null != maps.remove(key)) return 1L;
		if (null != sets.remove(key)) return 1L;
		if (null != queues.remove(key)) return 1L;

		return 0L;
	}

	@Override
	public Boolean exists(final String key)
	{
		return map.containsKey(key) || maps.containsKey(key) || sets.containsKey(key) || queues.containsKey(key);
	}

	@Override
	public Long expire(final String key, final int seconds)
	{
		expirations.put(key, seconds);
		return 1L;
	}

	@Override
	public Long incr(final String key)
	{
		return Long.valueOf(map.compute(key, (k, v) -> (((null != v) ? Long.valueOf(v) : 0L) + 1L) + ""));
	}

	@Override
	public Long decr(final String key)
	{
		return Long.valueOf(map.compute(key, (k, v) -> (((null != v) ? Long.valueOf(v) : 0L) - 1L) + ""));
	}

	@Override
	public ScanResult<String> scan(final String cursor, final ScanParams params)
	{
		var keys = keySet();
		var m = new HashMap<byte[], String>(2);
		var l = new ArrayList<>(params.getParams());
		for (int i = 0; i < l.size(); i+= 2)
			m.put(l.get(i), new String(l.get(i + 1)));

		var key = m.get(Protocol.Keyword.MATCH.raw);
		if ((null != key) && key.endsWith("*")) key = key.substring(0, key.length() - 1);	// Remove trailing asterisk (*).
		var k = key;

		return new ScanResult<>("0", keys.stream().filter(v -> (null == k) || v.startsWith(k)).collect(Collectors.toList()));
	}

	@Override
	public Long dbSize()
	{
		return (long) size();
	}

	public int size()
	{
		return map.size() + queues.size() + sets.size() + maps.size();
	}

	@Override
	public Set<String> keys(final String pattern)
	{
		return keySet().stream().filter(v -> v.contains(pattern)).collect(Collectors.toSet());
	}

	public Set<String> keySet()
	{
		Set<String> values = new HashSet<>(size());
		values.addAll(map.keySet());
		values.addAll(queues.keySet());
		values.addAll(sets.keySet());
		values.addAll(maps.keySet());

		return values;
	}

	@Override
	public String flushDB()
	{
		map.clear();
		queues.clear();
		sets.clear();
		maps.clear();

		return "";
	}

	@Override
	public String type(final String key)
	{
		return map.containsKey(key) ? "string" : (queues.containsKey(key) ? "list" : (sets.containsKey(key) ? "set" : (maps.containsKey(key) ? "hash" : "none")));
	}

	@Override
	public Long lpush(final String key, final String... values)
	{
		var o = queues.computeIfAbsent(key, k -> new LinkedList<>());

		return Arrays.stream(values).filter(v -> o.offer(v)).count();
	}

	@Override
	public String lpop(final String key)
	{
		var queue = queues.get(key);
		if (null == queue) return null;

		return queue.poll();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> lrange(final String key, final long start, final long stop)
	{
		var stop_ = (int) stop;
		var values = (List<String>) queues.get(key);
		if (0L > start) throw new IllegalArgumentException("The start parameter cannot be less than zero.");
		if (values.size() < stop_) stop_ = values.size();

		return values.subList((int) start, stop_);
	}

	public Long lclear(final String key)
	{
		Queue<String> values = queues.get(key);
		if (null == values) return 0L;

		queues.remove(key);
		return (long) values.size();
	}

	public Long lrem(final String key, final String value)
	{
		return lrem(key, 1L, value);
	}

	@Override
	public Long lrem(final String key, final long count, final String value)
	{
		var values = queues.get(key);
		if (null == values) return 0L;

		long count_ = 0L;
		for (long i = 0L; i < count; i++)
		{
			if (values.remove(key)) count_++;
			else return count_;
		}

		return count_;
	}

	@Override
	public Long llen(final String key)
	{
		var values = queues.get(key);
		return (null != values) ? (long) values.size() : 0L;
	}

	@Override
	public Long sadd(final String key, final String... values)
	{
		var o = sets.computeIfAbsent(key, k -> new HashSet<>());

		return Arrays.stream(values).filter(v -> o.add(v)).count();
	}

	@Override
	public Set<String> smembers(final String key)
	{
		return sets.get(key);
	}

	@Override
	public Long scard(final String key)
	{
		var values = sets.get(key);
		return (null != values) ? (long) values.size() : 0L;
	}

	@Override
	public Long srem(final String key, final String... items)
	{
		var values = sets.get(key);
		if (null == values) return 0L;

		return Arrays.stream(items).filter(i -> values.remove(i)).count();
	}

	@Override
	public Long hset(final String key, final String field, final String value)
	{
		return (null != maps.computeIfAbsent(key, k -> new HashMap<>()).put(field, value)) ? 1L : 0L;
	}

	@Override
	public String hget(final String key, final String field)
	{
		var values = maps.get(key);
		return (null != values) ? values.get(field) : null;
	}

	@Override
	public Map<String, String> hgetAll(final String key)
	{
		return maps.get(key);
	}

	@Override
	public Long hset(final String key, final Map<String, String> values)
	{
		if (MapUtils.isEmpty(values)) return 0L;

		var map = maps.computeIfAbsent(key, k -> new HashMap<>());
		values.forEach((k, v) -> map.put(k, v));

		return (long) values.size();
	}

	@Override
	public Long hlen(final String key)
	{
		var o = maps.get(key);
		return (null != o) ? (long) o.size() : 0L;
	}

	@Override
	public Long hdel(final String key, final String... fields)
	{
		var o = maps.get(key);
		if (null == o) return 0L;

		return Arrays.stream(fields).filter(v -> (null != o.remove(v))).count();
	}

	@Override
	public ScanResult<Map.Entry<String, String>> hscan(final String key, final String cursor, final ScanParams params)
	{
		return new ScanResult<Map.Entry<String, String>>("0", new ArrayList<>(maps.get(key).entrySet()));
	}
}
