package app.allclear.common.errors;

import java.io.*;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Simple POJO holding information about a Throwable as Strings.
 * 
 *  @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 */

@JsonPropertyOrder({"error","stacktrace"})
public class ErrorInfo implements Serializable
{
	public static final long serialVersionUID = 1L;

	public final String error;
	public final String stacktrace;

	/** Populator.
	 * 
	 * @param error
	 * @param stacktrace
	 */
	public ErrorInfo(@JsonProperty("error") final String error,
		@JsonProperty("stacktrace") final String stacktrace)
	{
		this.error = error;
		this.stacktrace = stacktrace;
	}

	/** Populator.
	 * 
	 * @param t
	 * @param message
	 */
	public ErrorInfo(final Throwable t, final String message)
	{
		var m = StringUtils.trimToNull(message);
		this.error = (null != m) ? m : t.getMessage();

		var o = new StringWriter();
		t.printStackTrace(new PrintWriter(o));
		this.stacktrace = o.toString();
	}

	/** Populator.
	 * 
	 * @param t
	 */
	public ErrorInfo(final Throwable t) { this(t, null); }

	@Override
	public int hashCode() { return (null != error) ? error.hashCode() : 0; }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof ErrorInfo)) return false;

		var v = (ErrorInfo) o;
		return error.equals(v.error) && stacktrace.equals(v.stacktrace);
	}
}
