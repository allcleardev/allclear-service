package app.allclear.common.value;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents a simple boolean response. Includes a message
 *  attribute when the result is FALSE.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class OperationResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final OperationResponse SUCCESS = new OperationResponse(true);

	public final boolean operation;
	public final String message;

	public OperationResponse()
	{
		this.operation = true;
		this.message = null;
	}

	public OperationResponse(final boolean operation)
	{
		this.operation = operation;
		this.message = null;
	}

	public OperationResponse(final String message)
	{
		this.operation = false;
		this.message = message;
	}

	public OperationResponse(@JsonProperty("operation") final boolean operation,
		@JsonProperty("message") final String message)
	{
		this.operation = operation;
		this.message = message;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof OperationResponse)) return false;

		var v = (OperationResponse) o;
		return (operation == v.operation) && Objects.equals(message, v.message);
	}

	@Override
	public int hashCode() { return (null != message) ? message.hashCode() : 1; }

	@Override
	public String toString()
	{
		return new StringBuilder("{ operation: ").append(operation)
			.append(", message: ").append(message)
			.append(" }").toString();
	}
}
