package app.allclear.common.errors;

/** Represents an exception that is thrown when an external resource has denied the current
 *  request due to overuse. This indicates to the background operator to temporarily stop
 *  processing the current requests/queue.
 *
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ThrottledException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ThrottledException(final Exception ex) { super(ex); }
	public ThrottledException(final String message) { super(message); }
	public ThrottledException(final String message, final Exception ex) { super(message, ex); }
}
