package app.allclear.common.jersey;

import app.allclear.common.entity.User;
import app.allclear.common.value.OperationResponse;

/** An implementation of the HttpClientManager that provides the default security token in the header.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class DefaultHttpClientManager extends HttpClientManager
{
	public DefaultHttpClientManager()
	{
		super(new HttpClientConfig("https://default.test", false, "123-abc", true));
	}

	public User getUser(String id, boolean full)
	{
		return get(client.target("users").path(id).queryParam("full", "" + full), User.class);
	}

	public User putUser(User value)
	{
		return put(client.target("users"), value, User.class);
	}

	public OperationResponse activateUser(String id)
	{
		return put(client.target("users").path(id).path("activate"), OperationResponse.class);
	}

	public OperationResponse deactivateUser(String id)
	{
		return post(client.target("users").path(id).path("deactivate"), OperationResponse.class);
	}
}
