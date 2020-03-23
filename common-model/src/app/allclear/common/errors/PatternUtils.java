package app.allclear.common.errors;

import java.util.regex.Pattern;

/** Utilities class with methods and constants to help with RegEx validations.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class PatternUtils
{
	/** Constant - valid RegEx pattern for AWS SQS names. */
	public static final Pattern PATTERN_AWS_SQS_NAME = Pattern.compile("[\\w.-]+");

	/** Constant - regular expression to separate the parts of a coordinate. */
	public static final Pattern PATTERN_COORD_SEPARATOR = Pattern.compile(",");

	/** Constant - valid RegEx pattern for credit card numbers. */
	public static final Pattern PATTERN_CREDIT_CARD_NUMBER = Pattern.compile("\\d{15,16}");

	/** Constant - valid RegEx pattern for credit card numbers. */
	public static final Pattern PATTERN_CREDIT_CARD_CVV = Pattern.compile("\\d{3,4}");

	/** Constant - valid RegEx pattern for domain names. */
	public static final Pattern PATTERN_DOMAIN_NAME = Pattern.compile("^[\\w-]+[\\.][a-z]{2,4}$");

	/** Constant - valid RegEx pattern for email. */
	public static final Pattern PATTERN_EMAIL = Pattern.compile("^[\\+\\w.-]+@[\\w-\\.]+[\\.][a-z]{2,4}$");
	
	/** Constant - valid RegEx pattern for new lines. */
	public static final Pattern PATTERN_NEW_LINE = Pattern.compile("[\\n\\r]");

	/** Constant - valid RegEx pattern for passwords. */
	public static final Pattern PATTERN_PASSWORD = Pattern.compile("^[\\w#$!-@%*]+$");

	/** Constant - valid RegEx pattern for phone numbers. */
	public static final Pattern PATTERN_PHONE = Pattern.compile("^[\\d-]+$");

	/** Constant - valid RegEx pattern for URLs. */
	public static final Pattern PATTERN_URL = Pattern.compile("^(http|https)://[\\w\\.\\-/!:#?=&]+$");

	/** Constant - valid RegEx pattern for URLs paths. */
	public static final Pattern PATTERN_URL_PATH = Pattern.compile("^[\\w\\.\\-/!:#?=&]+$");

	/** Does the supplied string match the supplied pattern. */
	public static boolean valid(final String value, final Pattern pattern)
	{
		return pattern.matcher(value).matches();
	}
}
