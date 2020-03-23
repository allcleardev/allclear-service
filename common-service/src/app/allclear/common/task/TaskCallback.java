package app.allclear.common.task;

/** Interface that performs the individual background task operation.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@FunctionalInterface
public interface TaskCallback<T>
{
	/** Entry point to perform the individual task.
	 * 
	 * @param request
	 * @return TRUE if the item was successfully processed. FALSE if the item was skipped and should be put back on the back of the queue.
	 * @throws Exception
	 */
	public boolean process(T request) throws Exception;

	/** Optional handler called after successful processing. Includes the full TaskRequest with underlying payload.
	 *  Default implementation does not additional work.
	 *
	 * @param request
	 */
	default public void onSuccess(final TaskRequest<T> request) {}
}
