package app.allclear.common.errors;

/** A runtime exception class that indicates to completely abort an operation without further retries.
 * 
 * @author smalleyd
 * @version 1.0.109
 * @since 4/14/2020
 *
 */

public class AbortException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public AbortException(final String message) { super(message); }
}
