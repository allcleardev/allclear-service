package app.allclear.twilio.client;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test class that verifies the TwilioException construction.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/27/2020
 *
 */

public class TwilioExceptionTest
{
	public static Stream<Arguments> create()
	{
		return Stream.of(
			arguments(404, "Not Found"),
			arguments(429, "Too many request"),
			arguments(503, "Unavailable server"));
	}

	@ParameterizedTest
	@MethodSource
	public void create(final int status, final String message)
	{
		var o = new TwilioException(status, message);
		Assertions.assertEquals(status, o.status, "Check status");
		Assertions.assertNull(o.errorCode, "Check errorCode");
		Assertions.assertEquals(message, o.getMessage(), "Check message");
	}

	public static Stream<Arguments> create_with_errorCode()
	{
		return Stream.of(
			arguments(201, 2221, "Bad phone number"),
			arguments(404, 4440, "Not Found"),
			arguments(429, null, "Too many request"),
			arguments(503, 5353, "Unavailable server"));
	}

	@ParameterizedTest
	@MethodSource
	public void create_with_errorCode(final int status, final Integer errorCode, final String message)
	{
		var o = new TwilioException(status, errorCode, message);
		Assertions.assertEquals(status, o.status, "Check status");
		Assertions.assertEquals(errorCode, o.errorCode, "Check errorCode");
		Assertions.assertEquals(message, o.getMessage(), "Check message");
	}
}
