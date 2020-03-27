package app.allclear.twilio.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents an error that occurred when calling the Twilio API.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/27/2020
 *
 */

public class TwilioException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public final int status;
	public final Integer errorCode;

	public TwilioException(final int status, final String message) { this(status, null, message); }
	public TwilioException(@JsonProperty("status") final int status,
		@JsonProperty("errorCode") final Integer errorCode,
		@JsonProperty("message") final String message)
	{
		super(message);

		this.status = status;
		this.errorCode = errorCode;
	}
}
