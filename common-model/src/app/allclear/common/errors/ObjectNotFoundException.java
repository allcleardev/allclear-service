package app.allclear.common.errors;

/** Exception class that represents when a data item lookup fails.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ObjectNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/** Default/empty. */
	public ObjectNotFoundException() { super(); }

	/** Populator.
	 * 
	 * @param message
	 */
	public ObjectNotFoundException(final String message) { super(message); }
}
