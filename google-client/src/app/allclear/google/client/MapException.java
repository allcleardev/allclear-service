package app.allclear.google.client;

/** Represents a Google Maps error.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public class MapException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public final int status;
	public final String code;

	public MapException(final int status, final String message)
	{
		this(status, null, message);
	}

	public MapException(final int status, final String code, final String message)
	{
		super(message);

		this.status = status;
		this.code = code;
	}
}
