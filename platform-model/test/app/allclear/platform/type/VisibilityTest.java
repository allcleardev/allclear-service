package app.allclear.platform.type;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test class that verifies the Visibility type.
 * 
 * @author smalleyd
 * @version 1.1.38
 * @since 5/6/2020
 *
 */

public class VisibilityTest
{
	public static Stream<Arguments> available()
	{
		return Stream.of(
			arguments(Visibility.ALL, Visibility.ALL, true),
			arguments(Visibility.ALL, Visibility.FRIENDS, true),
			arguments(Visibility.ALL, Visibility.ME, true),
			arguments(Visibility.FRIENDS, Visibility.ALL, false),
			arguments(Visibility.FRIENDS, Visibility.FRIENDS, true),
			arguments(Visibility.FRIENDS, Visibility.ME, true),
			arguments(Visibility.ME, Visibility.ALL, false),
			arguments(Visibility.ME, Visibility.FRIENDS, false),
			arguments(Visibility.ME, Visibility.ME, true));
	}

	@ParameterizedTest
	@MethodSource
	public void available(final Visibility field, final Visibility who, final boolean expected)
	{
		Assertions.assertEquals(expected, field.available(who));
	}
}
