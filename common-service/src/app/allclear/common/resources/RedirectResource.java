package app.allclear.common.resources;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Redirect root path to given uri.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 * 
 */

@Path("")
@Api(hidden=true)
public class RedirectResource
{
	private final URI redirectUri;

	public RedirectResource(final String redirectUri)
	{
		this.redirectUri = URI.create(redirectUri);
	}

	@GET
	@Path("/")
	public Response redirect() { return Response.seeOther(redirectUri).build(); }
}
