package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/** Maps an ObjectNotFoundException to HTTP error output.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class ThrowableExceptionMapper extends ExMapper<Throwable>
{
	public ThrowableExceptionMapper() {}

	@Override protected int status() { return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(); }
	@Override protected ErrorInfo log(final String message) { log.error(message); return new ErrorInfo(message); }
	@Override protected ErrorInfo log(final Throwable ex) { log.error(ex.getMessage()); return new ErrorInfo(ex); }
}
