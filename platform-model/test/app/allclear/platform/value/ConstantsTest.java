package app.allclear.platform.value;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import app.allclear.common.value.Constants;

/** Unit test class to verify the Constants static helper.
 * 
 * @author smalleyd
 * @version 1.1.141
 * @since 11/10/2020
 *
 */

public class ConstantsTest
{
	@ParameterizedTest
	@CsvSource({"1,1000", "2,2000", "5,5000", "122,122000"})
	public void kmToMeters(final int km, final long expected)
	{
		Assertions.assertEquals(expected, Constants.kmToMeters(km));
	}

	@ParameterizedTest
	@CsvSource({"1,1609", "2,3218", "5,8045", "122,196298"})
	public void milesToMeters(final int miles, final long expected)
	{
		Assertions.assertEquals(expected, Constants.milesToMeters(miles));
	}

	@ParameterizedTest
	@CsvSource({"1,60000", "2,120000", "5,300000", "122,7320000"})
	public void minutes(final int value, final long expected)
	{
		Assertions.assertEquals(expected, Constants.minutes(value));
	}

	@ParameterizedTest
	@CsvSource({"1,3600000", "2,7200000", "5,18000000", "122,439200000"})
	public void hours(final int value, final long expected)
	{
		Assertions.assertEquals(expected, Constants.hours(value));
	}

	@Test
	public void lockedTill()
	{
		assertThat(Constants.lockedTill()).isCloseTo(new Date(System.currentTimeMillis() + 300000L), 100L);
	}

	@Test
	public void reviewedFrom()
	{
		assertThat(Constants.reviewedFrom()).isCloseTo(new Date(System.currentTimeMillis() - (3L * 24L * 60L * 60L * 1000L)), 100L);
	}
}
