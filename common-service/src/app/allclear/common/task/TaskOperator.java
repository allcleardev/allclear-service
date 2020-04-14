package app.allclear.common.task;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	public final String name;	// Represents the name of the queue from which to retrieve each item.
	public final String dlq;	// Represents the name of the dead letter queue to which requests that have exceeded their maximum number tries are moved.
	public final TaskCallback<T> callback;	// Represents the callback that performs the task.
	public final Class<T> clazz;	// Represents the class of the task request.
	public final int maxTries;	// Represents the maximum number of attempts to process a request.
	public final int timeout;	// Represents the duration of a process attempt before it times out.
	public final int delay;	// Delay between retries in seconds.
	public final int maxDelay;	// Maximum delay between retries in seconds with an exponential back off algo.
	public int successes = 0;	// Represents the total number of requests that have been processed successfully.
	public int errors = 0;	// Represents the total number of errors that have occurred with this operation.
	public int skips = 0;	// Represents the total number of skips that have occurred with this operation.
	public boolean available = true;	// Indicates that the process is not yet taken by a background thread.

	public long delayMS() { return (long) (delay * 1000); }	// Converted to milliseconds
	public long maxDelayMS() { return (long) (maxDelay * 1000); }	// Converted to milliseconds

	public TaskOperator<T> withAvailable(final boolean newValue) { available = newValue; return this; }	// Set to false to make unavailable for processing but available for reporting. DLS on 8/4/2017.

	/** Populator.
	 * 
	 * @param name
	 * @param callback
	 * @param clazz
	 * @param maxRetries
	 * @param delay in seconds
	 * @param maxDelay in seconds
	 */
	public TaskOperator(@JsonProperty("name") final String name,
		@JsonProperty("callback") final TaskCallback<T> callback,
		@JsonProperty("clazz") final Class<T> clazz,
		@JsonProperty("maxTries") final int maxTries,
		@JsonProperty("timeout") final int timeout,
		@JsonProperty("delay") final int delay,
		@JsonProperty("maxDelay") final int maxDelay)
	{
		this.name = name;
		this.dlq = dlq(name);
		this.callback = callback;
		this.clazz = clazz;
		this.maxTries = maxTries;
		this.timeout = timeout;
		this.delay = delay;
		this.maxDelay = maxDelay;
	}

	/** Populator.
	 * 
	 * @param name
	 * @param callback
	 * @param clazz
	 * @param maxTries
	 */
	public TaskOperator(final String name, final TaskCallback<T> callback, final Class<T> clazz, final int maxTries)
	{
		this(name, callback, clazz, maxTries, 30, 60, 3600);
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
