package app.allclear.common.task;

import java.io.Serializable;

/** Value object that represents the metadata associated with a background task.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TaskOperator<T> implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Constant - DLQ prefix. */
	public static final String DLQ = "dlq:";
	public static String dlq(final String name) { return DLQ + name; }

	public String name = null;	// Represents the name of the queue from which to retrieve each item.
	public String dlq = null;	// Represents the name of the dead letter queue to which requests that have exceeded their maximum number tries are moved.
	public TaskCallback<T> callback = null;	// Represents the callback that performs the task.
	public Class<T> clazz = null;	// Represents the class of the task request.
	public int maxTries = 10;	// Represents the maximum number of attempts to process a request.
	public int successes = 0;	// Represents the total number of requests that have been processed successfully.
	public int errors = 0;	// Represents the total number of errors that have occurred with this operation.
	public int skips = 0;	// Represents the total number of skips that have occurred with this operation.
	public boolean available = true;	// Indicates that the process is not yet taken by a background thread.

	public TaskOperator<T> withAvailable(final boolean newValue) { available = newValue; return this; }	// Set to false to make unavailable for processing but available for reporting. DLS on 8/4/2017.

	/** Default/empty. */
	public TaskOperator() { super(); }

	/** Populator.
	 * 
	 * @param name
	 * @param callback
	 * @param clazz
	 */
	public TaskOperator(final String name, final TaskCallback<T> callback, final Class<T> clazz, final int maxTries)
	{
		this.name = name;
		this.dlq = dlq(name);
		this.callback = callback;
		this.clazz = clazz;
		this.maxTries = maxTries;
	}

	/** Populator.
	 * 
	 * @param name
	 * @param callback
	 * @param clazz
	 */
	public TaskOperator(final String name, final TaskCallback<T> callback, final Class<T> clazz)
	{
		this(name, callback, clazz, 10);
	}
}
