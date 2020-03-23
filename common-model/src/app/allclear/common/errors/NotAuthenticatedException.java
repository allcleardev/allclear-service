package app.allclear.common.errors;

/** Exception class that indicates that a client ID header is missing or invalid.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class NotAuthenticatedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/** Default/empty. */
	public NotAuthenticatedException() { super(); }

	/** Populator.
	 * 
	 * @param ex
	 */
	public NotAuthenticatedException(final Throwable ex) { super(ex); }

	/** Populator.
	 * 
	 * @param message
	 */
	public NotAuthenticatedException(final String message) { super(message); }
}
