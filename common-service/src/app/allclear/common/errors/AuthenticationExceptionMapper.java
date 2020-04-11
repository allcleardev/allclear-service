package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/** Maps an NotAuthenticatedException to HTTP error output. Used primarily to indicate that the client header is missing.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class AuthenticationExceptionMapper extends ExMapper<NotAuthenticatedException>
{
	@Override protected int status() { return Response.Status.FORBIDDEN.getStatusCode(); }
	@Override protected ErrorInfo log(final String message, final Throwable ex) { log.debug(message); return new ErrorInfo(message); }
	@Override protected ErrorInfo log(final Throwable ex) { log.debug(ex.getMessage(), ex); return new ErrorInfo(ex); }
}
