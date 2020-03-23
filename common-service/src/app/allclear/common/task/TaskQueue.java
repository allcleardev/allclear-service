package app.allclear.common.task;

import java.util.List;

/** An interface that represents the queue from which the task manager pulls
 *  new requests to process.
 *
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public interface TaskQueue
{
	/** Pushes a request onto the queue.
	 * 
	 * @param queueName
	 * @param value
	 */
	public void pushTask(String queueName, TaskRequest<?> value) throws Exception;

	/** Pulls a request from the queue. */
	public <T> TaskRequest<T> popTask(String queueName, Class<T> clazz) throws Exception;

	/** Gets the size of the queue. */
	public int getQueueSize(String queueName) throws Exception;

	/** Lists the task requests in a queue. */
	public List<TaskRequest<?>> listRequests(String queueName) throws Exception;

	/** Lists a subset of the task requests in a queue.
	 * 
	 * @param queueName
	 * @param page 1-based page number.
	 * @param pageSize number of items on a page.
	 * @return NULL if none found.
	 * @throws Exception
	 */
	public List<TaskRequest<?>> listRequests(String queueName, int page, int pageSize) throws Exception;

	/** Lists the task requests in a queue. */
	public <T> List<TaskRequest<T>> listRequests(String queueName, Class<T> clazz) throws Exception;

	/** Removes a single request from the specified queue.
	 * 
	 * @param queueName
	 * @param id
	 * @return TRUE if found AND removed.
	 */
	public boolean removeRequest(String queueName, String id);

	/** Completely clears the queue of all the requests.
	 * 
	 * @param queueName
	 * @return number of requests removed.
	 * @throws Exception
	 */
	public int clearRequests(String queueName) throws Exception;

	/** Moves requests from one queue to another. Used to pop DLQ requests back onto the operational queue.
	 * 
	 * @param fromQueue source queue (usually DLQ)
	 * @param toQueue destination queue (usually the operational queue)
	 * @param clazz type of object in the request.
	 * @return the number of requests moved.
	 * @throws Exception
	 */
	public <T> int moveRequests(String fromQueue, String toQueue, Class<T> clazz) throws Exception;
}
