package app.allclear.twilio.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents a request to the SMS Message API.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/26/2020
 *
 */

public class SMSRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	@JsonProperty("From")  public final String from;
	@JsonProperty("Body") public final String body;
	@JsonProperty("To") public final String to;

	public SMSRequest(@JsonProperty("From") final String from,
		@JsonProperty("Body") final String body,
		@JsonProperty("To") final String to)
	{
		this.from = from;
		this.body = body;
		this.to = to;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ from: ").append(from)
			.append(", body: ").append(body)
			.append(", to: ").append(to)
			.append(" }").toString();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof SMSRequest)) return false;
	
		var v = (SMSRequest) o;

		return Objects.equals(from, v.from) && Objects.equals(body, v.body) && Objects.equals(to, v.to);
	}
}
