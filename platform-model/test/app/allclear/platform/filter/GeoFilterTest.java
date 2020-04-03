package app.allclear.platform.filter;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.common.value.Constants.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test class that verifies the GeoFilter POJO.
 * 
 * @author smalleyd
 * @version 1.0.33
 * @since 4/3/2020
 *
 */

public class GeoFilterTest
{
	public static Stream<Arguments> valid()
	{
		return Stream.of(
			arguments(null, null, null, null, false, null),
			arguments("12", null, null, null, false, null),
			arguments(null, "12", null, null, false, null),
			arguments(null, null, 10, null, false, milesToMeters(10)),
			arguments(null, null, null, 10, false, kmToMeters(10)),
			arguments(null, null, 10, 10, false, milesToMeters(10)),
			arguments("30", "40", null, null, false, null),
			arguments(null, "40", 10, null, false, milesToMeters(10)),
			arguments("30", null, 10, null, false, milesToMeters(10)),
			arguments(null, "40", null, 10, false, kmToMeters(10)),
			arguments("30", null, null, 10, false, kmToMeters(10)),
			arguments("30", "40", 10, null, true, milesToMeters(10)),
			arguments("30", "40", null, 10, true, kmToMeters(10)),
			arguments("30", "40", 10, 10, true, milesToMeters(10)));
	}

	@ParameterizedTest
	@MethodSource
	public void valid(final String latitude, final String longitude, final Integer miles, final Integer km, final boolean expected, final Long expectedMeters)
	{
		var o = new GeoFilter(null != latitude ? new BigDecimal(latitude) : null,
			null != longitude ? new BigDecimal(longitude) : null,
			miles,
			km);

		Assertions.assertEquals(expected, o.valid(), "Check valid()");
		if ((null != miles) || (null != km))
			Assertions.assertEquals(expectedMeters, o.meters(), "Check meters()");
	}
}
