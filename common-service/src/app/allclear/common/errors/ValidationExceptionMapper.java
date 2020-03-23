package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.allclear.common.errors.ValidationException;

/** Exception mapper class that provides the correct error status and message.
 *  
 *  This implementation output the ValidationException object as the appropriate
 *  media type.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException>
{
	private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

	/** Populator. */
	public ValidationExceptionMapper() { super(); }

	/** ExceptionMapper method - provides the response details. */
	public Response toResponse(ValidationException ex)
	{
		// ValidationException(s) are not as serious as RuntimeException(s) nor ObjectNotFoundException(s).
		if (logger.isDebugEnabled())
			logger.warn(ex.getMessage(), ex);
		else
			logger.warn(ex.getMessage());

		// Serialize the entire exception for the client UIs.
		return Response.status(422).entity(ex).build();
	}
}
