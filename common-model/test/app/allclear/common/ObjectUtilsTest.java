package app.allclear.common;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.allclear.common.value.CreatedValue;

/** Unit test class that verifies the ObjectUtils.
 * 
 * @author smalleyd
 * @version 1.1.57
 * @since 5/13/2020
 *
 */

public class ObjectUtilsTest
{
	public static Stream<Arguments> testToString()
	{
		return Stream.of(
			arguments(new CreatedValue(null), "{}"),
			arguments(new CreatedValue("2", null, null), "{\"id\":\"2\"}"),
			arguments(new CreatedValue("3", "Three", null), "{\"id\":\"3\",\"name\":\"Three\"}"),
			arguments(new CreatedValue("1", "One", utc(2020, 5, 13)), "{\"id\":\"1\",\"name\":\"One\",\"createdAt\":\"Wed May 13 00:00:00 UTC 2020\"}"));
	}

	@ParameterizedTest
	@MethodSource
	public void testToString(final Object value, final String expected)
	{
		Assertions.assertEquals(expected, ObjectUtils.toString(value));
	}
}
