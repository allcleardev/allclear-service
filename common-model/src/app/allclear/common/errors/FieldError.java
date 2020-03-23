package app.allclear.common.errors;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the validation problem at the field level.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class FieldError implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String code;
	public final String name;
	public final String message;

	public FieldError()
	{
		this(null, null, null);
	}

	public FieldError(final String name, final String message)
	{
		this(null, name, message);
	}

	public FieldError(@JsonProperty("code") final String code,
		@JsonProperty("name") final String name,
		@JsonProperty("message") final String message)
	{
		this.code = code;
		this.name = name;
		this.message = message;
	}

	@Override
	public String toString()
	{
		return new StringBuilder(name).append(": ").append(message).toString();
	}
}
