package app.allclear.common.errors;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/** Maps an NotAuthorizedException to HTTP error output. Used primarily to indicate a lack of permissions to
 *  perform the current operation.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Provider
public class AuthorizationExceptionMapper extends ExMapper<NotAuthorizedException>
{
	@Override protected int status() { return Response.Status.UNAUTHORIZED.getStatusCode(); }
	@Override protected ErrorInfo log(final String message, final NotAuthorizedException ex) { log.error(message, ex); return new ErrorInfo(message); }
	@Override protected ErrorInfo log(final NotAuthorizedException ex) { log.error(ex.getMessage(), ex); return new ErrorInfo(ex); }
}
