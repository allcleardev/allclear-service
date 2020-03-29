package app.allclear.twilio.model;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/** Contains static helper methods for phone based operations.
 * 
 * @author smalleyd
 * @version 1.0.1
 * @since 3/29/2020
 *
 */

public class TwilioUtils
{
	public static final String CODE_US = "1";
	public static final String FORMAT_NORMALIZE = "+%s%s";
	public static final Pattern PATTERN_INVALID_DIGITS = Pattern.compile("[^\\d\\+]");

	/** Normalizes a phone number for sending messages and storage. */
	public static String normalize(final String countryCode, final String value)
	{
		if (StringUtils.isEmpty(value)) return null;

		var v = PATTERN_INVALID_DIGITS.matcher(value).replaceAll("");
		if (StringUtils.isEmpty(v)) return null;

		return v.startsWith("+") ? v : String.format(FORMAT_NORMALIZE, countryCode, v);
	}

	/** Normalizes a phone number for sending messages and storage. Defaults to the US country code.
	 * 
	 * @param value
	 * @return NULL if value is empty, null, or contains no numbers.
	 */
	public static String normalize(final String value) { return normalize(CODE_US, value); }
}
