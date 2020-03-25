package app.allclear.common.errors;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/** Simple validator utility that maintains a list of errors to be thrown.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class Validator
{
	public static final BigDecimal MAX_GEO_COORD = new BigDecimal(180);
	public static final BigDecimal MIN_GEO_COORD = new BigDecimal(-180);
	public static final Pattern PATTERN_LOWER_CASE = Pattern.compile("[a-z]");
	public static final Pattern PATTERN_NUMERIC = Pattern.compile("[\\d]");
	public static final Pattern PATTERN_PASSWORD_SYMBOLS = Pattern.compile("[\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\-\\_\\+\\=]");
	public static final Pattern PATTERN_PHONE = Pattern.compile("[\\d\\-]");
	public static final Pattern PATTERN_UPPER_CASE = Pattern.compile("[A-Z]");
	public static final Pattern PATTERN_PASSWORD_INVALID = Pattern.compile("[^a-zA-Z\\d\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\-\\_\\+\\=]");

	public static final String CODE_MISSING_FIELD = "VC-101";
	public static final String CODE_MISSING_TOO_LONG = "VC-102";
	public static final String CODE_MISSING_TOO_SHORT = "VC-103";
	public static final String CODE_INVALID_CREDENTIALS = "AP-101";
	public static final String CODE_INVALID_LOGIN_ID = "AP-102";
	public static final String CODE_MISSING_EMAIL_ADDRESS = "AP-103";
	public static final String CODE_NEW_PASSWORD_MISMATCH = "AP-104";
	public static final String CODE_CANNOT_CHANGE_PASSWORD = "AP-105";
	public static final String CODE_INVALID_CURRENT_PASSWORD = "AP-106";
	public static final String CODE_PASSWORD_TOO_SHORT = "AP-107";
	public static final String CODE_PASSWORD_TOO_LONG = "AP-108";
	public static final String CODE_PASSWORD_MISSING_LOWER_CASE_CHAR = "AP-109";
	public static final String CODE_PASSWORD_MISSING_UPPER_CASE_CHAR = "AP-110";
	public static final String CODE_PASSWORD_MISSING_NUMBER = "AP-111";
	public static final String CODE_PASSWORD_MISSING_SYMBOL = "AP-112";
	public static final String CODE_PASSWORD_INVALID_CHAR = "AP-113";

	private static final String NOT_SET = "%s is not set.";
	private static final String TOO_SHORT = "%s cannot be shorter than %d characters.";
	private static final String TOO_LONG = "%s cannot be longer than %d characters.";

	public boolean hasErrors() { return CollectionUtils.isNotEmpty(errors); }
	public List<FieldError> getErrors() { return errors; }
	private List<FieldError> errors = null;

	/** Ensure that the field value exists.
	 * 
	 * @param name
	 * @param caption
	 * @param value
	 * @return SELF
	 */
	public Validator ensureExists(String name, String caption, Object value)
	{
		if (null == value)
			return addWithCode(CODE_MISSING_FIELD, name, NOT_SET, caption);

		return this;
	}

	/** Ensure that the field value exists and is less than the specified length.
	 * 
	 * @param name
	 * @param caption
	 * @param value
	 * @param max
	 * @return SELF
	 */
	public Validator ensureExistsAndLength(String name, String caption, String value, int max)
	{
		if (null == value)
			return addWithCode(CODE_MISSING_FIELD, name, NOT_SET, caption);

		return ensureLength_(name, caption, value, max);
	}

	/** Ensures that a string is not NULL and between the specified lengths.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @param min
	 * @param max
	 * @return SELF
	 */
	public Validator ensureExistsAndLength(String name, String caption, String value, int min, int max)
	{
		if (null == value)
			addWithCode(CODE_MISSING_FIELD, name, NOT_SET, caption);
		else if (min > value.length())
			addWithCode(CODE_MISSING_TOO_SHORT, name, TOO_SHORT, caption, min);
		else if (max < value.length())
			addWithCode(CODE_MISSING_TOO_LONG, name, TOO_LONG, caption, max);

		return this;
	}

	/** Ensure that the field value is less than the specified length.
	 * 
	 * @param name
	 * @param caption
	 * @param value
	 * @param max
	 * @return SELF
	 */
	public Validator ensureLength(String name, String caption, String value, int max)
	{
		if (null == value)
			return this;

		return ensureLength_(name, caption, value, max);
	}

	/** Ensures that a non-nullable string is between the specified lengths.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @param min
	 * @param max
	 * @return SELF
	 */
	public Validator ensureLength(String name, String caption, String value, int min, int max)
	{
		if (null == value)
			return this;

		if (min > value.length())
			addWithCode(CODE_MISSING_TOO_SHORT, name, TOO_SHORT, caption, min);
		else if (max < value.length())
			addWithCode(CODE_MISSING_TOO_LONG, name, TOO_LONG, caption, max);

		return this;
	}

	/** Ensure that the field value is less than the specified length and that it matches the specified pattern.
	 * 
	 * @param name
	 * @param caption
	 * @param value
	 * @param max
	 * @param pattern
	 * @return SELF
	 */
	public Validator ensureLength(String name, String caption, String value, int max, Pattern pattern)
	{
		if (null == value)
			return this;

		return ensureLength_(name, caption, value, max).ensurePattern_(name, caption, value, pattern);
	}

	/** Helper method - check the length of the string.
	 * 
	 * @param name
	 * @param caption
	 * @param value
	 * @param max
	 * @return SELF
	 */
	private Validator ensureLength_(final String name, final String caption, final String value, final int max)
	{
		if (max < value.length())
			return addWithCode(CODE_MISSING_TOO_LONG, name, "%s '%s' is longer than the expected size of %d.", caption, shrink(value), max);

		return this;
	}

	/** Ensures the value matches the RegEx pattern.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @param pattern
	 * @return SELF
	 */
	public Validator ensurePattern(String name, String caption, String value, Pattern pattern)
	{
		if (null == value)
			return this;

		return ensurePattern_(name, caption, value, pattern);
	}

	/** Ensures the value matches the RegEx pattern.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @param pattern
	 * @return SELF
	 */
	private Validator ensurePattern_(String name, String caption, String value, Pattern pattern)
	{
		if (!pattern.matcher(value).matches())
			add(name, "%s '%s' does not match a valid pattern.", caption, shrink(value));

		return this;
	}

	/** Ensures the minimum value.
	 * 
	 * @param name
	 * @param caption
	 * @param value
	 * @param min
	 * @return SELF
	 */
	public Validator ensureMinimum(String name, String caption, int value, int min)
	{
		if (min > value)
			add(name, "The %s cannot be less than %d.", caption, min);

		return this;
	}

	/** Ensures the minimum value. Does NOT validate NULL values.
	 * 
	 * @param name
	 * @param caption
	 * @param value
	 * @param min
	 * @return SELF
	 */
	public Validator ensureMinimum(String name, String caption, Integer value, int min)
	{
		if (null == value)
			return this;

		return ensureMinimum(name, caption, value.intValue(), min);
	}

	/** Ensures that the Price exists and is not negative.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @return SELF
	 */
	public Validator ensureExistsAndPrice(String name, String caption, BigDecimal value)
	{
		if (null == value)
			return addWithCode(CODE_MISSING_FIELD, name, NOT_SET, caption);

		return ensurePrice_(name, caption, value);
	}
	
	/** Ensures that the Price is not negative.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @return SELF
	 */
	public Validator ensurePrice(String name, String caption, BigDecimal value)
	{
		if (null != value)
			return ensurePrice_(name, caption, value);

		return this;
	}
	
	/** Ensures that the Price is not negative. Allow for a price that is zero or free.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @return SELF
	 */
	private Validator ensurePrice_(String name, String caption, BigDecimal value)
	{
		if (0 > value.signum())
			add(name, "The %s, %s, cannot be a negative number.", caption, value);

		return this;
	}

	/** Ensures that a Rating value is between 1 and 5 inclusive.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @return SELF
	 */
	public Validator ensureRating(String name, String caption, Integer value)
	{
		if (null != value)
			return ensureRating_(name, caption, value.intValue());

		return this;
	}

	/** Ensures that a Rating value is between 1 and 5 inclusive.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @return SELF
	 */
	private Validator ensureRating_(String name, String caption, int value)
	{
		if (1 > value)
			add(name, "The %s, %d, cannot be less than 1.", caption, value);
		else if (5 < value)
			add(name, "The %s, %d, cannot be greater than 5.", caption, value);

		return this;
	}

	/** Ensures that a Geo coordinate exists and is within the valid range.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @return SELF
	 */
	public Validator ensureExistsAndGeoCoord(String name, String caption, BigDecimal value)
	{
		if (null == value)
			addWithCode(CODE_MISSING_FIELD, name, NOT_SET, caption);
		else
			ensureGeoCoord_(name, caption, value);

		return this;
	}

	/** Ensures that a Geo coordinate is within the valid range.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @return SELF
	 */
	public Validator ensureGeoCoord(String name, String caption, BigDecimal value)
	{
		if (null != value)
			ensureGeoCoord_(name, caption, value);

		return this;
	}

	/** Helper method - validates the Geo coord.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 */
	private void ensureGeoCoord_(String name, String caption, BigDecimal value)
	{
		ensureRange_(name, caption, value, MIN_GEO_COORD, MAX_GEO_COORD);
	}

	/** Ensures that a value exists and is within the valid range.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @param min
	 * @param max
	 * @return SELF
	 */
	public <T> Validator ensureExistsAndRange(String name, String caption, T value, Comparable<T> min, Comparable<T> max)
	{
		if (null == value)
			addWithCode(CODE_MISSING_FIELD, name, NOT_SET, caption);
		else
			ensureRange_(name, caption, value, min, max);

		return this;
	}

	/** Ensures that a value is within the valid range.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @param min
	 * @param max
	 * @return SELF
	 */
	public <T> Validator ensureRange(String name, String caption, T value, Comparable<T> min, Comparable<T> max)
	{
		if (null != value)
			ensureRange_(name, caption, value, min, max);

		return this;
	}

	/** Helper method - validates the numeric range.
	 *
	 * @param name
	 * @param caption
	 * @param value
	 * @param min
	 * @param max
	 */
	private <T> void ensureRange_(String name, String caption, T value, Comparable<T> min, Comparable<T> max)
	{
		if (0 > max.compareTo(value))
			add(name, "%s '%s' is greater than the accepted value of %s.", caption, value, max);
		else if (0 < min.compareTo(value))
			add(name, "%s '%s' is less than the accepted value of %s.", caption, value, min);
	}

	/** Ensures that the password meets minimum requirements. The password must meet the following requirements:
	 *  <ol>
	 *    <li>Have at least 8 characters.</li>
	 *    <li>Have at most 20 characters.</li>
	 *    <li>Have at least 1 upper case character.</li>
	 *    <li>Have at least 1 lower case character.</li>
	 *    <li>Have at least 1 numeric character.</li>
	 *    <li></li>
	 *    <li></li>
	 *  </ol>
	 * 
	 * @param name
	 * @param caption
	 * @param password
	 * @return SELF
	 */
	public Validator ensurePassword(final String name, final String caption, final String value)
	{
		if (null == value)
			addWithCode(CODE_MISSING_FIELD, name, NOT_SET, caption);
		else
		{
			if (8 > value.length())
				addWithCode(CODE_PASSWORD_TOO_SHORT, name, TOO_SHORT, caption, 8);
			else if (20 < value.length())
				addWithCode(CODE_PASSWORD_TOO_LONG, name, TOO_LONG, caption, 20);

			if (!PATTERN_LOWER_CASE.matcher(value).find())
				addWithCode(CODE_PASSWORD_MISSING_LOWER_CASE_CHAR, name, "%s must contain at least one lower case character (a-z).", caption);
			if (!PATTERN_UPPER_CASE.matcher(value).find())
				addWithCode(CODE_PASSWORD_MISSING_UPPER_CASE_CHAR, name, "%s must contain at least one upper case character (A-Z).", caption);
			if (!PATTERN_NUMERIC.matcher(value).find())
				addWithCode(CODE_PASSWORD_MISSING_NUMBER, name, "%s must contain at least one numeric character (0-9).", caption);
			if (!PATTERN_PASSWORD_SYMBOLS.matcher(value).find())
				addWithCode(CODE_PASSWORD_MISSING_SYMBOL, name, "%s must contain at least one special character (~ ! @ # $ %% ^ & * ( ) - _ + =).", caption);

			if (PATTERN_PASSWORD_INVALID.matcher(value).find())	// Ensure that there are no invalid characters.
				addWithCode(CODE_PASSWORD_INVALID_CHAR, name, "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", caption);
		}

		return this;
	}

	/** Check the error status and throw a validation exception if errors exist.
	 * 
	 * @return SELF if no validation errors exist.
	 * @throws ValidationException
	 */
	public Validator check() throws ValidationException
	{
		if (null != errors)
			throw new ValidationException(errors);

		return this;
	}

	/** Helper method - adds a field error.
	 * 
	 * @param name field name
	 * @param message
	 * @param args
	 * @return SELF
	 */
	public Validator add(final String name, final String message, final Object... args)
	{
		return add(name, String.format(message, args));
	}

	/** Helper method - adds a field error.
	 * 
	 * @param name field name
	 * @param message
	 * @return SELF
	 */
	public Validator add(final String name, final String message)
	{
		if (null == errors)
			errors = new LinkedList<>();

		errors.add(new FieldError(name, message));

		return this;
	}

	/** Helper method - adds a field error.
	 * 
	 * @param code error code
	 * @param name field name
	 * @param message
	 * @param args
	 * @return SELF
	 */
	public Validator addWithCode(final String code, final String name, final String message, final Object... args)
	{
		return addWithCode(code, name, String.format(message, args));
	}

	/** Helper method - adds a field error.
	 * 
	 * @param code error code
	 * @param name field name
	 * @param message
	 * @return SELF
	 */
	public Validator addWithCode(final String code, final String name, final String message)
	{
		if (null == errors)
			errors = new LinkedList<>();

		errors.add(new FieldError(code, name, message));

		return this;
	}

	/** Helper method - make sure that the 'value' property is not longer than 200 characters.
	 * 
	 * @param value
	 * @return never NULL.
	 */
	private String shrink(String value)
	{
		return StringUtils.abbreviate(value, 200);
	}

	/** Resets the validator by NULLing out the list of errors.
	 * 
	 * @return SELF
	 */
	public Validator clear()
	{
		errors = null;

		return this;
	}

	@Override
	public String toString()
	{
		if (null == errors)
			return null;

		return ValidationException.toString(errors);
	}
}
