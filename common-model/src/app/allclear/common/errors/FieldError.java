package app.allclear.common.errors;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

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
	public boolean equals(final Object o)
	{
		if (!(o instanceof FieldError)) return false;

		var v = (FieldError) o;
		return Objects.equals(code, v.code) && Objects.equals(name, v.name) && Objects.equals(message, v.message);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
