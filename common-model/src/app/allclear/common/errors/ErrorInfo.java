package app.allclear.common.errors;

import java.io.*;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Simple POJO holding information about a Throwable as Strings.
 * 
 *  @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 */

public class ErrorInfo implements Serializable
{
	public static final long serialVersionUID = 1L;

	public final String message;
	public final String stacktrace;

	public ErrorInfo(final String message) { this(message, null); }
	public ErrorInfo(@JsonProperty("message") final String message,
		@JsonProperty("stacktrace") final String stacktrace)
	{
		this.message = message;
		this.stacktrace = stacktrace;
	}

	public ErrorInfo(final Throwable t)
	{
		this.message = t.getMessage();

		var o = new StringWriter();
		t.printStackTrace(new PrintWriter(o));
		this.stacktrace = o.toString();
	}

	@Override
	public int hashCode() { return (null != message) ? message.hashCode() : 0; }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof ErrorInfo)) return false;

		var v = (ErrorInfo) o;
		return message.equals(v.message) && stacktrace.equals(v.stacktrace);
	}
}
