package app.allclear.twilio.client;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import app.allclear.twilio.model.*;

/** Jersey HTTP client to access the Twilio API.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/27/2020
 *
 */

public class TwilioClient implements AutoCloseable
{
	public static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
	private static final ObjectMapper mapper = new ObjectMapper()
		.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
		.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.setDateFormat(FORMAT_DATE);

	public final String FIELD_TO = "To";
	public final String FIELD_BODY = "Body";
	public final String FIELD_FROM = "From";

	private final Client client;
	private final Invocation.Builder message;

	public TwilioClient(final TwilioConfig conf)
	{
		this.client = ClientBuilder.newBuilder()
			.connectTimeout(20L, TimeUnit.SECONDS)	
			.readTimeout(20L, TimeUnit.SECONDS)	
			.build()
			.register(new JacksonJsonProvider(mapper))
			.register(HttpAuthenticationFeature.basic(conf.accountId, conf.authToken));

		var home = this.client.target(conf.baseUrl).path("Accounts").path(conf.accountId);
		this.message = home.path("Messages.json").request(MediaType.APPLICATION_JSON_TYPE);
	}

	/** Sends an SMS message.
	 * 
	 * @param request
	 * @return never NULL
	 * @throws TwilioException
	 */
	public SMSResponse send(final SMSRequest request) throws TwilioException
	{
		var params = new MultivaluedStringMap(3);
		params.add(FIELD_FROM, request.from);
		params.add(FIELD_BODY, request.body);
		params.add(FIELD_TO, request.to);

		var response = message.post(Entity.form(params));
		var status = response.getStatus();
		if (400 <= status) throw new TwilioException(status, response.readEntity(String.class));

		var o = response.readEntity(SMSResponse.class);
		if (null != o.error_message) throw new TwilioException(status, o.error_code, o.error_message);

		return o;
	}

	@Override
	public void close()
	{
		client.close();
	}
}
