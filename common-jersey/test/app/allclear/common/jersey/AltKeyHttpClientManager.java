package app.allclear.common.jersey;

import java.util.List;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import app.allclear.common.entity.Country;

/** An implementation of the HttpClientManager that provides multiple security tokens in the header.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class AltKeyHttpClientManager extends HttpClientManager
{
	private static final GenericType<List<Country>> TYPE_LIST_COUNTRY = new GenericType<List<Country>>() {};

	private final String accessKey;
	private final String secretKey;

	public AltKeyHttpClientManager(final String accessKey, final String secretKey)
	{
		super(new HttpClientConfig("http://altKey.test", false, null, true));

		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	public String getAccessKeyName() { return "accessKey"; }
	public String getSecretKeyName() { return "secretKey"; }

	@Override
	public Invocation.Builder wrapBuilder(final Invocation.Builder b)
	{
		return b.header(getAccessKeyName(), accessKey).header(getSecretKeyName(), secretKey);
	}

	public Country postCountry(Country value)
	{
		return post(client.target("countries"), value, Country.class);
	}

	public List<Country> findCountriesByPartialNameSearch(String name)
	{
		return get(client.target("countries").queryParam("search", name), TYPE_LIST_COUNTRY);
	}
}
