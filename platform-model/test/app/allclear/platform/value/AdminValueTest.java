package app.allclear.platform.value;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test class that verifies the AdminValue POJO.
 * 
 * @author smalleyd
 * @version 1.1.14
 * @since 4/30/2020
 *
 */

public class AdminValueTest
{
	public static Stream<Arguments> canAdmin()
	{
		return Stream.of(
			arguments(false, false, true),
			arguments(true, false, true),
			arguments(false, true, false),
			arguments(true, true, true));
	}

	@ParameterizedTest
	@MethodSource
	public void canAdmin(final boolean supers, final boolean editor, final boolean expected)
	{
		Assertions.assertEquals(expected, new AdminValue().withSupers(supers).withEditor(editor).canAdmin());
	}

	public static Stream<Arguments> type()
	{
		return Stream.of(
			arguments(false, false, "Admin"),
			arguments(false, true, "Editor"),
			arguments(true, false, "Super"),
			arguments(true, true, "Super"));
	}

	@ParameterizedTest
	@MethodSource
	public void type(final boolean supers, final boolean editor, final String expected)
	{
		Assertions.assertEquals(expected, new AdminValue("first", supers, editor).type());
	}
}
