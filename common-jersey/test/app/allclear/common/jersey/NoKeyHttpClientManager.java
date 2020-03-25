package app.allclear.common.jersey;

import java.util.function.Function;

import javax.ws.rs.client.Invocation;

import app.allclear.common.entity.Country;
import app.allclear.common.value.OperationResponse;

/** An implementation of the HttpClientManager that does not provide an access key in the header.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class NoKeyHttpClientManager extends HttpClientManager
{
	public NoKeyHttpClientManager()
	{
		super(new HttpClientConfig("http://noKey.test", false, null, true));
	}

	public Function<Invocation.Builder, Invocation.Builder> wrapBuilder() { return b -> b; }	// Does nothing.

	public OperationResponse deleteCountry(String id)
	{
		return delete(client.target("countries").path(id), OperationResponse.class);
	}

	public OperationResponse deleteCountry(Country value)
	{
		return delete(client.target("countries"), value, OperationResponse.class);
	}
}
