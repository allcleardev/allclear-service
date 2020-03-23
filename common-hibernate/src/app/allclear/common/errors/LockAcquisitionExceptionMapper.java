package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.exception.LockAcquisitionException;

import app.allclear.common.mediatype.UTF8MediaType;

/** Exception mapper class that provides the correct error status and message.
 *  
 *  Handles Hibernate Lock-Acquisition exception. Reports a Retry (599) HTTP status code.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class LockAcquisitionExceptionMapper implements ExceptionMapper<LockAcquisitionException>
{
	private static final Logger logger = LoggerFactory.getLogger(LockAcquisitionExceptionMapper.class);

	/** Populator. */
	public LockAcquisitionExceptionMapper() { super(); }

	/** ExceptionMapper method - provides the response details. */
	public Response toResponse(LockAcquisitionException ex)
	{
		logger.warn(ex.getMessage(), ex);

		// Serialize the entire exception for the client UIs.
		return Response.status(599).type(UTF8MediaType.APPLICATION_JSON).entity(ex).build();
	}
}
