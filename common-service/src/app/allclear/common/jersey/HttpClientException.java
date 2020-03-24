package app.allclear.common.jersey;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Exception throws from the QSClient component on error.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class HttpClientException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public final String url;
	public final int status;

	public HttpClientException(@JsonProperty("message") final String message,
		@JsonProperty("url") final String url,
		@JsonProperty("status") final int status)
	{
		super(message);

		this.url = url;
		this.status = status;
	}
}
