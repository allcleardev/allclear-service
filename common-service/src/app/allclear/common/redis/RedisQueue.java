package app.allclear.common.redis;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import app.allclear.common.task.TaskQueue;
import app.allclear.common.task.TaskRequest;

/** Represents an in-memory Task Queue.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class RedisQueue implements TaskQueue
{
	private final RedisClient client;

	/** Populator.
	 * 
	 * @param client
	 */
	public RedisQueue(final RedisClient client)
	{
		this.client = client;
	}

	@Override
	public void pushTask(final String queueName, final TaskRequest<?> value)
		throws Exception
	{
		// Use Java serialization instead of JSON because of the TaskRequest generics.
		client.push(queueName, serialize(value));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> TaskRequest<T> popTask(final String queueName, final Class<T> clazz)
		throws Exception
	{
		// Use Java deserialization instead of JSON because of the TaskRequest generics.
		var value = client.pop(queueName);
		if (null != value)
			return (TaskRequest<T>) deserialize(value);

		return null;
	}

	@Override
	public int getQueueSize(final String queueName) throws Exception
	{
		return client.queueSize(queueName);
	}

	@Override
	public List<TaskRequest<?>> listRequests(final String queueName) throws Exception
	{
		var values = client.list(queueName);
		if (CollectionUtils.isEmpty(values))
			return null;

		return values.stream().map(v -> (TaskRequest<?>) deserialize(v)).collect(Collectors.toList());
	}

	@Override
	public List<TaskRequest<?>> listRequests(final String queueName, final int page,
		int pageSize) throws Exception
	{
		var values = client.list(queueName, page, pageSize);
		if (null == values)
			return null;

		return values.stream().map(v -> (TaskRequest<?>) deserialize(v)).collect(Collectors.toList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<TaskRequest<T>> listRequests(final String queueName,
		final Class<T> clazz) throws Exception
	{
		var values = client.list(queueName);
		if (CollectionUtils.isEmpty(values))
			return null;

		return values.stream().map(v -> (TaskRequest<T>) deserialize(v)).collect(Collectors.toList());
	}

	@Override
	public boolean removeRequest(final String queueName, final String id)
	{
		// Get the list of actual values. Needed to propertly perform the removal from the list/queue.
		var values = client.list(queueName);
		if (CollectionUtils.isEmpty(values))
			return false;

		// Loop throug and, deserialize one at a time to compare the IDs. Most likely will NOT need to deserialize the entire list. DLS on 10/9/2015.
		for (var value : values)
		{
			var request = (TaskRequest<?>) deserialize(value);
			if (id.equals(request.id))
			{
				client.unqueue(queueName, value);
				return true;
			}
		}

		return false;
	}

	@Override
	public int clearRequests(final String queueName) throws Exception
	{
		return client.unqueue(queueName);
	}

	@Override
	public <T> int moveRequests(final String fromQueue, final String toQueue, final Class<T> clazz) throws Exception
	{
		int count = 0;
		TaskRequest<T> value = null;
		while (null != (value = popTask(fromQueue, clazz)))
		{
			value.tries = 0;	// MUST reset tries count.
			pushTask(toQueue, value);
			count++;
		}

		return count;
	}

	/** Helper method - serialize the class to a String. */
	private String serialize(final Serializable value) throws IOException
	{
		var out = new ByteArrayOutputStream();
		(new ObjectOutputStream(out)).writeObject(value);

		return Base64.getEncoder().encodeToString(out.toByteArray());
	}

	/** Helper method - deserialize a String to a class. */
	private Object deserialize(final String value)
	{
		// Throw Runtime so that deserialize can be used within a Lambda.
		try
		{
			return (new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(value)))).readObject();
		}
		catch (final IOException ex) { throw new RuntimeException(ex); }
		catch (final ClassNotFoundException ex) { throw new RuntimeException(ex); }
	}
}
