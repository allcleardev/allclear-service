package app.allclear.common.errors;

import java.util.List;
import java.util.stream.Collectors;

/** Exception class that indicates a validation problem.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ValidationException extends RuntimeException
{
	/** Constant - serial version UID. */
	private static final long serialVersionUID = 1L;

	public List<FieldError> errors = null;

	/** Default/empty. */
	public ValidationException() { super(); }

	/** Populator - provides a message. */
	public ValidationException(final String message) { super(message); }

	/** Populator - provides an underlying cause. */
	public ValidationException(final Throwable cause) { super(cause); }

	/** Populator - provides a message and an underlying cause. */
	public ValidationException(final String message, final Throwable cause) { super(message, cause); }

	/** Populator - provides a single field error.
	 * 
	 * @param code
	 * @param field
	 * @param message
	 */
	public ValidationException(final String code, final String field, final String message)
	{
		this(new FieldError(code, field, message));
	}

	/** Populator - provides a single field error.
	 * 
	 * @param field
	 * @param message
	 */
	public ValidationException(final String field, final String message)
	{
		this(new FieldError(field, message));
	}

	/** Populator - provides a single field error. */
	public ValidationException(final FieldError error)
	{
		super(error.message);

		this.errors = List.of(error);
	}

	/** Populator - provides the list of field errors. */
	public ValidationException(final List<FieldError> errors)
	{
		super(toString(errors));

		this.errors = errors;
	}

	/** Converts a list of FieldError objects to a message. */
	public static String toString(final List<FieldError> errors)
	{
		return errors.stream().map(o -> o.message).collect(Collectors.joining("\n"));
	}
}
