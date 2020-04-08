package app.allclear.common.errors;

import javax.ws.rs.ext.Provider;

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
public class ValidationExceptionMapper extends ExMapper<ValidationException>
{
	public ValidationExceptionMapper() { super(); }

	@Override protected int status() { return 422; }
}
