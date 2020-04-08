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
public class NotFoundExceptionMapper extends ExMapper<ObjectNotFoundException>
{
	@Override protected int status() { return Response.Status.NOT_FOUND.getStatusCode(); }
}
