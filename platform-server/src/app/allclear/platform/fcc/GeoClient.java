package app.allclear.platform.fcc;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import app.allclear.google.client.MapException;

/** Jersey RESTful client that wraps the FCC Block endpoints.
 * 
 * @author smalleyd
 * @version 1.1.113
 * @since 8/2/2020
 * @see https://geo.fcc.gov/api/census/#!/block/get_block_find
 *
 */

public class GeoClient implements AutoCloseable
{
	public static final String BASE_URL = "https://geo.fcc.gov/api/census/block/find";
	public static final String FORMAT = "json";

	public static final String PARAM_FORMAT = "format";
	public static final String PARAM_LATITUDE = "latitude";
	public static final String PARAM_LONGITUDE = "longitude";
	public static final String PARAM_SHOWALL = "showall";

	private static final ObjectMapper mapper = new ObjectMapper()
		.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
		.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

	private final Client client;
	private final WebTarget target;

	public GeoClient()
	{
		this.client = ClientBuilder.newBuilder()
			.connectTimeout(20L, TimeUnit.SECONDS)
			.readTimeout(20L, TimeUnit.SECONDS)
			.register(new JacksonJsonProvider(mapper))	// Does NOT work for some reason - must use Jackson outside of Jersey. DLS on 4/6/2020.
			.build();
		this.target = client.target(BASE_URL);
	}

	@Override
	public void close()
	{
		this.client.close();
	}

	/** Sends a latitude and longitude to FCC Block API to geocode.
	 * 
	 * @param latitude
	 * @param longitude
	 * @return never NULL
	 * @throws MapException
	 */
	public GeoResponse find(final BigDecimal latitude, final BigDecimal longitude) throws MapException
	{
		return response(request(target.queryParam(PARAM_LATITUDE, latitude).queryParam(PARAM_LONGITUDE, longitude)).get());
	}

	private Invocation.Builder request(final WebTarget target)
	{
		return target.queryParam(PARAM_SHOWALL, false).queryParam(PARAM_FORMAT, FORMAT).request(MediaType.APPLICATION_JSON_TYPE);
	}

	private GeoResponse response(final Response response) throws MapException
	{
		int status = response.getStatus();
		var payload = response.readEntity(String.class);
		if (400 <= status) throw new MapException(status, payload);

		var o = toJSON(payload, GeoResponse.class);
		if (!o.ok())
		{
			var message = o.message();
			throw new MapException(status, o.status, (null != message) ? message : payload);
		}

		return o;
	}

	private <T> T toJSON(final String payload, final Class<T> clazz)
	{
		try { return mapper.readValue(payload, clazz); }
		catch (final IOException ex) { throw new RuntimeException(ex); }
	}
}
