package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.allclear.common.mediatype.UTF8MediaType;

/** Base exception mapper class.
 *  
 * @author smalleyd
 * @version 1.0.72
 * @since 4/7/2020
 *
 */

@Provider
public abstract class ExMapper<E extends Throwable> implements ExceptionMapper<E>
{
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public ExMapper() { super(); }

	protected abstract int status();
	protected ErrorInfo log(final String message, final Throwable ex) { log.warn(message); return new ErrorInfo(message); }
	protected ErrorInfo log(final Throwable ex) { log.warn(ex.getMessage(), ex); return new ErrorInfo(ex); }

	@Override
	public Response toResponse(final E ex)
	{
		return Response.status(status())
			.type(UTF8MediaType.APPLICATION_JSON_TYPE)
			.entity((log.isDebugEnabled()) ? log(ex) : log(ex.getMessage(), ex))
			.build();
	}
}
