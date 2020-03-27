package app.allclear.twilio.client;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the Value object that represents the Twilio HTTP client configuration..
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since Fri Mar 27 06:36:33 EDT 2020
 * 
 */

public class TwilioConfig implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String BASE_URL = "https://api.twilio.com/2010-04-01";

	public final String baseUrl;
	public final String accountId;
	public final String authToken;

	public static String env(final String name, final String defaultValue)
	{
		var o = System.getenv(name);
		return StringUtils.isNotEmpty(o) ? o : defaultValue;
	}

	public static TwilioConfig test()
	{
		return new TwilioConfig(BASE_URL, env("TWILIO_ACCOUNT_ID", "123"), env("TWILIO_AUTH_TOKEN", "token"));
	}

	public TwilioConfig(
		@JsonProperty("baseUrl") final String baseUrl,
		@JsonProperty("accountId") final String accountId,
		@JsonProperty("authToken") final String authToken)
	{
		this.baseUrl = baseUrl;
		this.accountId = accountId;
		this.authToken = authToken;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof TwilioConfig)) return false;

		var v = (TwilioConfig) o;
		return Objects.equals(baseUrl, v.baseUrl) &&
			Objects.equals(accountId, v.accountId) &&
			Objects.equals(authToken, v.authToken);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ baseUrl: ").append(baseUrl)
			.append(", accountId: ").append(accountId)
			.append(", authToken: ").append(authToken)
			.append(" }").toString();
	}
}
