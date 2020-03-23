package app.allclear.common.errors;

/** Exception class that indicates that the current user does not have permission to perform the operation.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class NotAuthorizedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/** Default/empty. */
	public NotAuthorizedException() { super(); }

	/** Populator.
	 * 
	 * @param ex
	 */
	public NotAuthorizedException(final Throwable ex) { super(ex); }

	/** Populator.
	 * 
	 * @param message
	 */
	public NotAuthorizedException(final String message) { super(message); }
}
