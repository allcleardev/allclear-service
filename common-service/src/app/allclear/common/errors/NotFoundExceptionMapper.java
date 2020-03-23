package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Maps an ObjectNotFoundException to HTTP error output.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<ObjectNotFoundException>
{
	private final Logger logger = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

	/** Populator. */
	public NotFoundExceptionMapper() {}

	@Override
	public Response toResponse(ObjectNotFoundException ex)
	{
		logger.warn(ex.getMessage(), ex);

		return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(ex).build();
	}
}
