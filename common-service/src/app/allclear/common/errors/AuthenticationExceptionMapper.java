package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.allclear.common.mediatype.UTF8MediaType;

/** Maps an NotAuthenticatedException to HTTP error output. Used primarily to indicate that the client header is missing.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<NotAuthenticatedException>
{
	private final Logger logger = LoggerFactory.getLogger(AuthenticationExceptionMapper.class);

	/** Populator. */
	public AuthenticationExceptionMapper() {}

	@Override
	public Response toResponse(NotAuthenticatedException ex)
	{
		logger.debug(ex.getMessage(), ex);	// No need to log this exception.

		return Response.status(Response.Status.FORBIDDEN.getStatusCode()).type(UTF8MediaType.APPLICATION_JSON).entity(ex).build();
	}
}
