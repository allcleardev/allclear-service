package app.allclear.platform.model;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test class that verifies the PeopleFindRequest POJO.
 * 
 * @author smalleyd
 * @version 1.1.9
 * @since 4/29/2020
 *
 */

public class PeopleFindRequestTest
{
	public static Stream<Arguments> create()
	{
		return Stream.of(
			arguments(null, null, 400L, null, null, 20L, false),
			arguments(List.of(), null, 0L, null, null, 20L, false),
			arguments(null, List.of(), null, null, null, 20L, false),
			arguments(null, List.of("", " \n \r \t ", "  "), 65L, null, List.of(), 65L, false),
			arguments(List.of("", " \n \r \t ", "  "), null, 65L, List.of(), null, 65L, false),
			arguments(List.of("a", "b", "c", "  ", " \t\t e \n \r", "\r \t \n", "g"), List.of("z"," y", "x"), null, List.of("a", "b", "c", "e", "g"), List.of(), 20L, true),
			arguments(List.of("a", "b", "c", "e", "g"), List.of("z888-555#4444", "\t \n \r", " 888-55y33333", "q", " +18885552222"), 5L, List.of("a", "b", "c", "e", "g"), List.of("+18885554444", "+18885533333", "+18885552222"), 5L, true),
			arguments(Arrays.asList(" \t \n ", "  ", null, "  ", " \r \r \n "), Arrays.asList("z888-555#4444", null, " 888-55y33333", "q", " +18885552222"), 5L, List.of(), List.of("+18885554444", "+18885533333", "+18885552222"), 5L, true),
			arguments(null, Arrays.asList("z888-555#4444", null, " 888-55y33333", "q", " +18885552222"), 2L, null, List.of("+18885554444", "+18885533333"), 2L, true),
			arguments(List.of("a", "b", "c", "  ", " \t\t e \n \r", "\r \t \n", "g"), List.of("z"," y", "x"), 4L, List.of("a", "b", "c", "e"), List.of(), 4L, true)
			);
	}

	@ParameterizedTest
	@MethodSource
	public void create(final List<String> names, final List<String> phones, final Long pageSize,
		final List<String> expectedNames, final List<String> expectedPhones, final long expectedPageSize, final boolean expectedValid)
	{
		var o = new PeopleFindRequest(names, phones, pageSize);
		Assertions.assertEquals(expectedNames, o.names, "Check names");
		Assertions.assertEquals(expectedPhones, o.phones, "Check phones");
		Assertions.assertEquals(expectedPageSize, o.pageSize, "Check pageSize");
		Assertions.assertEquals(expectedValid, o.valid(), "Check valid");
	}
}
