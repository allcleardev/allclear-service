package app.allclear.common.value;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the response to a Health status request.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class HealthResponse implements Serializable
{
	public static final long serialVersionUID = 1L;

	public final Date timestamp;
	public final String node;
	public final String status;
	public final String message;
	public final String debug;

	public HealthResponse(final String node) { this(node, "success", null); }
	public HealthResponse(final String node, final String error) { this(node, "fail", error); }
	public HealthResponse(final String node, final String status, final String message)
	{
		this(new Date(), node, status, message, null);
	}
	public HealthResponse(@JsonProperty("timestamp") final Date timestamp,
		@JsonProperty("node") final String node,
		@JsonProperty("status") final String status,
		@JsonProperty("message") final String message,
		@JsonProperty("debug") final String debug)
	{
		this.timestamp = timestamp;
		this.node = node;
		this.status = status;
		this.message = message;
		this.debug = debug;
	}
}
