package app.allclear.google.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import app.allclear.google.model.*;

/** Jersey RESTful client that wraps the Google Maps endpoints.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public class MapClient implements AutoCloseable
{
	public static final String BASE_URL = "https://maps.googleapis.com/maps/api";
	public static final String PATH_GEOCODE = "geocode";
	public static final String KEY = "GOOGLE_MAP_KEY";
	public static final String FORMAT = "json";

	public static final String PARAM_KEY = "key";
	public static final String PARAM_ADDRESS = "address";

	private static final ObjectMapper mapper = new ObjectMapper()
		.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
		.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	private final String key;
	private final Client client;
	private final WebTarget geocode;

	public MapClient() { this(System.getenv(KEY)); }	// Construct with key in environment variable.
	public MapClient(final String key)
	{
		this.key = StringUtils.trimToNull(key);
		if (null == this.key) throw new IllegalArgumentException("The key is required.");

		this.client = ClientBuilder.newBuilder()
			.connectTimeout(20L, TimeUnit.SECONDS)
			.readTimeout(20L, TimeUnit.SECONDS)
			.register(new JacksonJsonProvider(mapper))	// Does NOT work for some reason - must use Jackson outside of Jersey. DLS on 4/6/2020.
			.build();
		this.geocode = client.target(BASE_URL).path(PATH_GEOCODE).path(FORMAT);
	}

	@Override
	public void close()
	{
		this.client.close();
	}

	/** Sends a textual address to Google Maps API to geocode.
	 * 
	 * @param address
	 * @return never NULL
	 * @throws MapException
	 */
	public GeocodeResponse geocode(final String address) throws MapException
	{
		return response(request(geocode.queryParam(PARAM_ADDRESS, address)).get(), GeocodeResponse.class);
	}

	private Invocation.Builder request(final WebTarget target) throws MapException
	{
		return target.queryParam(PARAM_KEY, key).request(MediaType.APPLICATION_JSON_TYPE);
	}

	private <T extends MapResponse> T response(final Response response, final Class<T> clazz) throws MapException
	{
		int status = response.getStatus();
		var payload = response.readEntity(String.class);
		if (400 <= status) throw new MapException(status, payload);

		try
		{
			var o = mapper.readValue(payload, clazz);
			if (!(o.ok() || o.zeroResults())) throw new MapException(status, o.status, o.errorMessage);
	
			return o;
		}
		catch (final IOException ex) { throw new RuntimeException(ex); }
	}
}
