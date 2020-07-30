package app.allclear.platform.value;

import static java.util.stream.Collectors.toList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.allclear.common.ThreadUtils;
import app.allclear.platform.type.HealthWorkerStatus;
import app.allclear.platform.type.Symptom;

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
			.withAlertedOf(4)
			.withAlertedAt(value)
			.withCreatedAt(value)
			.withUpdatedAt(value);
	}

	public static Stream<Arguments> associatedWith()
	{
		var empty = new PeopleValue();
		var one = new PeopleValue().withAssociations(new FacilityValue(1L));
		var two = new PeopleValue().withAssociations(new FacilityValue(2L));
		var three = new PeopleValue().withAssociations(new FacilityValue(3L));
		var nullish = new PeopleValue().withAssociations(new FacilityValue());
		var oneAndTwo = new PeopleValue().withAssociations(new FacilityValue(1L), new FacilityValue(2L));

		return Stream.of(
			arguments(empty, 1L, false),
			arguments(empty, 2L, false),
			arguments(empty, null, false),
			arguments(nullish, 1L, false),
			arguments(nullish, 2L, false),
			arguments(nullish, null, false),
			arguments(one, 1L, true),
			arguments(one, 2L, false),
			arguments(one, null, false),
			arguments(two, 1L, false),
			arguments(two, 2L, true),
			arguments(two, null, false),
			arguments(three, 1L, false),
			arguments(three, 2L, false),
			arguments(three, null, false),
			arguments(oneAndTwo, 1L, true),
			arguments(oneAndTwo, 2L, true),
			arguments(oneAndTwo, null, false));
	}

	@ParameterizedTest
	@MethodSource
	public void associatedWith(final PeopleValue value, final Long facilityId, final boolean expected)
	{
		Assertions.assertEquals(expected, value.associatedWith(facilityId));
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
		Assertions.assertEquals(4, o.alertedOf, "Check alertedOf");
		Assertions.assertEquals(value, o.alertedAt, "Check alertedAt");
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
		Assertions.assertEquals(4, o.alertedOf, "Check alertedOf");
		Assertions.assertEquals(value, o.alertedAt, "Check alertedAt");
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
		Assertions.assertNull(o.alertedOf, "Check alertedOf");
		Assertions.assertNull(o.alertedAt, "Check alertedAt");
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
		assertThat(o.phoneVerifiedAt).as("Check phoneVerifiedAt").isCloseTo(now, 100L).isNotEqualTo(value).isEqualTo(o.authAt);
		Assertions.assertNull(o.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertNull(o.alertedOf, "Check alertedOf");
		Assertions.assertNull(o.alertedAt, "Check alertedAt");
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
		assertThat(o.phoneVerifiedAt).as("Check phoneVerifiedAt").isCloseTo(now, 100L).isNotEqualTo(value).isEqualTo(o.authAt);
		Assertions.assertNull(o.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertNull(o.alertedOf, "Check alertedOf");
		Assertions.assertNull(o.alertedAt, "Check alertedAt");
		assertThat(o.createdAt).as("Check createdAt").isCloseTo(now, 100L).isNotEqualTo(value);
		assertThat(o.updatedAt).as("Check updatedAt").isCloseTo(now, 100L).isNotEqualTo(value);
	}

	public static Stream<Arguments> healthWorker()
	{
		return Stream.of(
			arguments(null, false),
			arguments(HealthWorkerStatus.LIVE_WITH, false),
			arguments(HealthWorkerStatus.HEALTH_WORKER, true));
	}

	@ParameterizedTest
	@MethodSource
	public void healthWorker(final HealthWorkerStatus status, final boolean expected)
	{
		Assertions.assertEquals(expected, new PeopleValue().withHealthWorkerStatus(status).healthWorker());
	}

	public static Stream<Arguments> meetsCdcPriority3()
	{
		return Stream.of(
			arguments(null, null, false),
			arguments(null, List.of(), false),
			arguments(null, List.of(Symptom.DIARRHEA), false),
			arguments(null, List.of(Symptom.DIARRHEA, Symptom.RUNNY_NOSE), false),
			arguments(null, List.of(Symptom.DIARRHEA, Symptom.FEVER, Symptom.RUNNY_NOSE), true),
			arguments(null, List.of(Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), false),
			arguments(null, List.of(Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE, Symptom.FEVER), true),
			arguments(null, List.of(Symptom.FEVER, Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), true),
			arguments(null, List.of(Symptom.DIARRHEA, Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), false),
			arguments(null, Symptom.LIST, true),
			arguments(HealthWorkerStatus.LIVE_WITH, null, false),
			arguments(HealthWorkerStatus.LIVE_WITH, List.of(), false),
			arguments(HealthWorkerStatus.LIVE_WITH, List.of(Symptom.DIARRHEA), false),
			arguments(HealthWorkerStatus.LIVE_WITH, List.of(Symptom.DIARRHEA, Symptom.RUNNY_NOSE), false),
			arguments(HealthWorkerStatus.LIVE_WITH, List.of(Symptom.DIARRHEA, Symptom.FEVER, Symptom.RUNNY_NOSE), true),
			arguments(HealthWorkerStatus.LIVE_WITH, List.of(Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), false),
			arguments(HealthWorkerStatus.LIVE_WITH, List.of(Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE, Symptom.FEVER), true),
			arguments(HealthWorkerStatus.LIVE_WITH, List.of(Symptom.FEVER, Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), true),
			arguments(HealthWorkerStatus.LIVE_WITH, List.of(Symptom.DIARRHEA, Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), false),
			arguments(HealthWorkerStatus.LIVE_WITH, Symptom.LIST, true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, null, true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, List.of(), true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, List.of(Symptom.DIARRHEA), true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, List.of(Symptom.DIARRHEA, Symptom.RUNNY_NOSE), true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, List.of(Symptom.DIARRHEA, Symptom.FEVER, Symptom.RUNNY_NOSE), true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, List.of(Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, List.of(Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE, Symptom.FEVER), true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, List.of(Symptom.FEVER, Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, List.of(Symptom.DIARRHEA, Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), true),
			arguments(HealthWorkerStatus.HEALTH_WORKER, Symptom.LIST, true));
	}

	@ParameterizedTest
	@MethodSource
	public void meetsCdcPriority3(final HealthWorkerStatus status, final List<Symptom> symptoms, final boolean expected)
	{
		var items = (null == symptoms) ? null : symptoms.stream().map(o -> o.created()).collect(toList());
		Assertions.assertEquals(expected, new PeopleValue().withHealthWorkerStatus(status).withSymptoms(items).meetsCdcPriority3());
	}

	public static Stream<Arguments> symptomatic()
	{
		return Stream.of(
			arguments(null, false),
			arguments(List.of(), false),
			arguments(List.of(Symptom.DIARRHEA), false),
			arguments(List.of(Symptom.DIARRHEA, Symptom.RUNNY_NOSE), false),
			arguments(List.of(Symptom.DIARRHEA, Symptom.FEVER, Symptom.RUNNY_NOSE), true),
			arguments(List.of(Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), false),
			arguments(List.of(Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE, Symptom.FEVER), true),
			arguments(List.of(Symptom.FEVER, Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), true),
			arguments(List.of(Symptom.DIARRHEA, Symptom.DRY_COUGH, Symptom.SORE_THROAT, Symptom.FATIGUE), false),
			arguments(Symptom.LIST, true));
	}

	@ParameterizedTest
	@MethodSource
	public void symptomatic(final List<Symptom> symptoms, final boolean expected)
	{
		var items = (null == symptoms) ? null : symptoms.stream().map(o -> o.created()).collect(toList());
		Assertions.assertEquals(expected, new PeopleValue().withSymptoms(items).symptomatic());
	}
}
