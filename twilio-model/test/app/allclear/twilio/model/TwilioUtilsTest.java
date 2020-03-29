package app.allclear.twilio.model;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test class that verifies the TwilioUtils static methods.
 * 
 * @author smalleyd
 * @version 1.0.1
 * @since 3/29/2020
 *
 */

public class TwilioUtilsTest
{
	public static Stream<Arguments> normalize()
	{
		return Stream.of(
			arguments("22", "", null),
			arguments("22", "a", null),
			arguments("22", null, null),
			arguments("22", " \t \n ", null),
			arguments("22", "bgdurj", null),
			arguments("22", "(888) 541-2092", "+228885412092"),
			arguments("22", "+1(888) 541-2092", "+18885412092"),
			arguments("22", "+20(888) 541-2092", "+208885412092"),
			arguments("22", "(888) 541-2092 ext: BGDR", "+228885412092"),
			arguments("22", "  +  \t \n 30 (888) 541-2092 ext: BGDR", "+308885412092"));
	}

	@ParameterizedTest
	@MethodSource
	public void normalize(final String countryCode, final String value, final String expected)
	{
		Assertions.assertEquals(expected, TwilioUtils.normalize(countryCode, value));
	}

	public static Stream<Arguments> normalizeUS()
	{
		return Stream.of(
			arguments("", null),
			arguments("a", null),
			arguments(null, null),
			arguments(" \t \n ", null),
			arguments("bgdurj", null),
			arguments("(888) 541-2092", "+18885412092"),
			arguments("+1(888) 541-2092", "+18885412092"),
			arguments("+20(888) 541-2092", "+208885412092"),
			arguments("(888) 541-2092 ext: BGDR", "+18885412092"),
			arguments("  +  \t \n 30 (888) 541-2092 ext: BGDR", "+308885412092"));
	}

	@ParameterizedTest
	@MethodSource
	public void normalizeUS(final String value, final String expected)
	{
		Assertions.assertEquals(expected, TwilioUtils.normalize(value));
	}
}
