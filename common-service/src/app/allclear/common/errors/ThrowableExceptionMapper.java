package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.allclear.common.mediatype.UTF8MediaType;

/** Maps an ObjectNotFoundException to HTTP error output.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable>
{
	private final Logger logger = LoggerFactory.getLogger(ThrowableExceptionMapper.class);

	/** Populator. */
	public ThrowableExceptionMapper() {}

	@Override
	public Response toResponse(Throwable ex)
	{
		logger.error(ex.getMessage(), ex);

		return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).type(UTF8MediaType.APPLICATION_JSON).entity(new ErrorInfo(ex)).build();
	}
}
