package app.allclear.platform.value;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.allclear.common.ThreadUtils;

/** Unit test class that verifies the PeopleValue POJO.
 * 
 * @author smalleyd
 * @version 1.0.12
 * @since 
 *
 */

public class PeopleValueTest
{
	private static Date HOUR_AGO = hourAgo();
	private static Date HOUR_AHEAD = hourAhead();

	private PeopleValue init(final boolean active, final Date value)
	{
		return new PeopleValue().withActive(active)
			.withAuthAt(value)
			.withPhoneVerifiedAt(value)
			.withEmailVerifiedAt(value)
			.withCreatedAt(value)
			.withUpdatedAt(value);
	}

	public static Stream<Arguments> create()
	{
		return Stream.of(
			arguments(true, null),
			arguments(true, new Date()),
			arguments(true, HOUR_AGO),
			arguments(true, HOUR_AHEAD),
			arguments(false, null),
			arguments(false, new Date()),
			arguments(false, HOUR_AGO),
			arguments(false, HOUR_AHEAD));
	}

	@ParameterizedTest
	@MethodSource
	public void create(final boolean active, final Date value)
	{
		var o = init(active, value);

		Assertions.assertEquals(active, o.active, "Check active");
		Assertions.assertEquals(value, o.authAt, "Check authAt");
		Assertions.assertEquals(value, o.phoneVerifiedAt, "Check phoneVerifiedAt");
		Assertions.assertEquals(value, o.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(value, o.createdAt, "Check createdAt");
		Assertions.assertEquals(value, o.updatedAt, "Check updatedAt");
	}

	@ParameterizedTest
	@MethodSource("create")
	public void initDates(final boolean active, final Date value)
	{
		ThreadUtils.sleep(10L);

		var now = new Date();
		var o = init(active, value);
		o.initDates();

		Assertions.assertEquals(active, o.active, "Check active");
		Assertions.assertEquals(value, o.authAt, "Check authAt");
		Assertions.assertEquals(value, o.phoneVerifiedAt, "Check phoneVerifiedAt");
		Assertions.assertEquals(value, o.emailVerifiedAt, "Check emailVerifiedAt");
		assertThat(o.createdAt).as("Check createdAt").isCloseTo(now, 100L).isNotEqualTo(value);
		assertThat(o.updatedAt).as("Check updatedAt").isCloseTo(now, 100L).isNotEqualTo(value);
	}

	@ParameterizedTest
	@MethodSource("create")
	public void register(final boolean active, final Date value)
	{
		ThreadUtils.sleep(10L);

		var now = new Date();
		var o = init(active, value).registered();

		Assertions.assertTrue(o.active, "Check active");
		assertThat(o.authAt).as("Check authAt").isCloseTo(now, 100L).isNotEqualTo(value);
		Assertions.assertEquals(value, o.phoneVerifiedAt, "Check phoneVerifiedAt");
		Assertions.assertEquals(value, o.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(value, o.createdAt, "Check createdAt");
		Assertions.assertEquals(value, o.updatedAt, "Check updatedAt");
	}

	@ParameterizedTest
	@MethodSource("create")
	public void registerByPhone(final boolean active, final Date value)
	{
		ThreadUtils.sleep(10L);

		var now = new Date();
		var o = init(active, value).registeredByPhone();

		Assertions.assertTrue(o.active, "Check active");
		assertThat(o.authAt).as("Check authAt").isCloseTo(now, 100L).isNotEqualTo(value);
		assertThat(o.phoneVerifiedAt).as("Check phoneVerifiedAt").isCloseTo(now, 100L).isNotEqualTo(value);
		Assertions.assertNull(o.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(value, o.createdAt, "Check createdAt");
		Assertions.assertEquals(value, o.updatedAt, "Check updatedAt");
	}

	@ParameterizedTest
	@MethodSource("create")
	public void registerByPhoneAndInitDates(final boolean active, final Date value)
	{
		ThreadUtils.sleep(10L);

		var now = new Date();
		var o = init(active, value).registeredByPhone();
		o.initDates();

		Assertions.assertTrue(o.active, "Check active");
		assertThat(o.authAt).as("Check authAt").isCloseTo(now, 100L).isNotEqualTo(value);
		assertThat(o.phoneVerifiedAt).as("Check phoneVerifiedAt").isCloseTo(now, 100L).isNotEqualTo(value);
		Assertions.assertNull(o.emailVerifiedAt, "Check emailVerifiedAt");
		assertThat(o.createdAt).as("Check createdAt").isCloseTo(now, 100L).isNotEqualTo(value);
		assertThat(o.updatedAt).as("Check updatedAt").isCloseTo(now, 100L).isNotEqualTo(value);
	}
}
