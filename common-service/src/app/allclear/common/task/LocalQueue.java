package app.allclear.common.task;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Represents an in-memory Task Queue.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class LocalQueue implements TaskQueue
{
	private final Map<String, Queue<TaskRequest<?>>> queues = new HashMap<>();

	@Override
	public void pushTask(final String queueName, final TaskRequest<?> value)
		throws Exception
	{
		var queue = queues.get(queueName);
		if (null == queue)
			queues.put(queueName, queue = new ConcurrentLinkedQueue<TaskRequest<?>>());

		queue.offer(value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> TaskRequest<T> popTask(final String queueName, final Class<T> clazz)
		throws Exception
	{
		var queue = queues.get(queueName);
		if (null != queue)
			return (TaskRequest<T>) queue.poll();

		return null;
	}

	@Override
	public int getQueueSize(final String queueName) throws Exception
	{
		var queue = queues.get(queueName);
		if (null != queue)
			return queue.size();

		return 0;
	}

	@Override
	public List<TaskRequest<?>> listRequests(String queueName) throws Exception
	{
		var queue = queues.get(queueName);
		if (null != queue)
			return new LinkedList<>(queue);

		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TaskRequest<?>> listRequests(String queueName, int page,
		int pageSize) throws Exception
	{
		int size = 0;
		var queue = listRequests(queueName);
		if ((null == queue) || (0 == (size = queue.size())))
			return null;

		int start = (page - 1) * pageSize;
		if (size < start + 1)	// Past end.
			return Collections.EMPTY_LIST;

		int end = page * pageSize;	// Past end.
		if (size < end)
			end = size;

		return queue.subList(start, end);
	}

	@Override
	@SuppressWarnings(value={"rawtypes", "unchecked"})
	public <T> List<TaskRequest<T>> listRequests(String queueName,
		Class<T> clazz) throws Exception
	{
		return (List) listRequests(queueName);
	}

	@Override
	public boolean removeRequest(String queueName, String id)
	{
		var queue = queues.get(queueName);
		if (null == queue)
			return false;

		return queue.remove(new TaskRequest<Object>(id, null));
	}

	@Override
	public int clearRequests(String queueName) throws Exception
	{
		int size = 0;
		var queue = queues.get(queueName);
		if ((null == queue) || (0 == (size = queue.size())))
			return 0;

		queues.remove(queueName);

		return size;
	}

	@Override
	public <T> int moveRequests(String fromQueue, String toQueue, Class<T> clazz) throws Exception
	{
		var _from = queues.get(fromQueue);
		if (null == _from)
			throw new IllegalArgumentException("The source queue, " + fromQueue + ", is invalid.");
		var _to = queues.get(toQueue);
		if (null == _to)
			throw new IllegalArgumentException("The destination queue, " + toQueue + ", is invalid.");

		// Do one at a time to ensure that each item is removed from the source queue
		// before being added to the destination queue.
		int count = 0;;
		TaskRequest<?> o = null;
		while (null != (o = _from.poll()))
		{
			o.tries = 0;	// MUST reset tries count.
			_to.offer(o);
			count++;
		}

		return count;
	}
}
