package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Maps an NotAuthorizedException to HTTP error output. Used primarily to indicate a lack of permissions to
 *  perform the current operation.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class AuthorizationExceptionMapper implements ExceptionMapper<NotAuthorizedException>
{
	private final Logger logger = LoggerFactory.getLogger(AuthorizationExceptionMapper.class);

	/** Populator. */
	public AuthorizationExceptionMapper() {}

	@Override
	public Response toResponse(NotAuthorizedException ex)
	{
		logger.error(ex.getMessage(), ex);

		return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
	}
}
