package app.allclear.common.task;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the details of a single background task.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TaskRequest<T> implements Serializable
{
	/** Constant - serial version UID. */
	public static final long serialVersionUID = 1L;

	// Members
	public final String id;
	public T value;
	public int tries = 0;
	public Long nextRunAt = null;

	// Mutators
	public int incrementTries() { return ++tries; }

	/** Represents the earliest time that the request should be processed. Used to delay
	 *  subsequent attempts to process the request. NULL indicates to run ASAP.
	 */
	public TaskRequest<T> withNextRunAt(Long newValue) { nextRunAt = newValue; return this; }
	public boolean ready() { return ((null == nextRunAt) || (System.currentTimeMillis() >= nextRunAt)); }

	/** Default/empty. */
	public TaskRequest()
	{
		super();
		this.id = UUID.randomUUID().toString();
	}

	/** Populator.
	 * 
	 * @param value
	 */
	public TaskRequest(final T value)
	{
		this();
		this.value = value;
	}

	/** Populator.
	 * 
	 * @param id
	 * @param value
	 */
	public TaskRequest(final String id, final T value)
	{
		super();
		this.id = id;
		this.value = value;
	}

	/** Populator - deserialized from JSON. */
	public TaskRequest(@JsonProperty("id") String id,
		@JsonProperty("value") T value,
		@JsonProperty("tries") int tries,
		@JsonProperty("nextRunAt") Long nextRunAt)
	{
		this.id = id;
		this.value = value;
		this.tries = tries;
		this.nextRunAt = nextRunAt;
	}

	@Override
	public int hashCode() { return id.hashCode(); }

	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object value)
	{
		if (!(value instanceof TaskRequest)) return false;

		return id.equals(((TaskRequest) value).id);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", value: ").append(value)
			.append(", tries: ").append(tries)
			.append(", nextRunAt: ").append(nextRunAt)
			.append(" }").toString();
	}
}
