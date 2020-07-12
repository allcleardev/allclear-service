package app.allclear.platform.entity;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.platform.type.Condition.*;
import static app.allclear.platform.type.Exposure.*;
import static app.allclear.platform.type.HealthWorkerStatus.*;
import static app.allclear.platform.type.Stature.*;
import static app.allclear.platform.type.Symptom.*;
import static app.allclear.platform.type.Visibility.*;
import static app.allclear.testing.TestingUtils.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.*;
import app.allclear.platform.value.PeopleValue;

/** Unit test class that verifies the People entity.
 * 
 * @author smalleyd
 * @version 1.0.74
 * @since 4/8/2020
 *
 */

public class PeopleTest
{
	private static final Date DOB = utc(1999, 4, 7);
	private static final Date DOB_1 = utc(2000, 4, 7);
	private static final Date AUTH_AT = utc(2001, 4, 7);
	private static final Date AUTH_AT_1 = utc(2002, 4, 7);
	private static final Date PHONE_VERIFIED_AT = utc(2003, 4, 7);
	private static final Date PHONE_VERIFIED_AT_1 = utc(2004, 4, 7);
	private static final Date EMAIL_VERIFIED_AT = utc(2005, 4, 7);
	private static final Date EMAIL_VERIFIED_AT_1 = utc(2006, 4, 7);
	private static final int ALERTED_OF = 3;
	private static final int ALERTED_OF_1 = 5;
	private static final Date ALERTED_AT = utc(2005, 4, 14);
	private static final Date ALERTED_AT_1 = utc(2006, 4, 14);
	private static final BigDecimal LAT = new BigDecimal("20");
	private static final BigDecimal LAT_1 = new BigDecimal("21");
	private static final BigDecimal LNG = new BigDecimal("22");
	private static final BigDecimal LNG_1 = new BigDecimal("23");
	private static final Date CREATED_AT = new Date();
	private static final Date CREATED_AT_1 = new Date(CREATED_AT.getTime() + 2000L);

	private static final List<Condition> CONDITIONS = List.of(CARDIO_RESPIRATORY_DISEASE, KIDNEY_CIRRHOSIS, PREGNANT);
	private static final List<Exposure> EXPOSURES = List.of(CLOSE_CONTACT, UNSURE);
	private static final List<Symptom> SYMPTOMS = List.of(DRY_COUGH, MUSCLE_ACHE, RUNNY_NOSE);

	private static final List<CreatedValue> CONDITIONS_ = CONDITIONS.stream().map(o -> new CreatedValue(o.id, o.name, null)).collect(toList());
	private static final List<CreatedValue> EXPOSURES_ = EXPOSURES.stream().map(o -> new CreatedValue(o.id, o.name, null)).collect(toList());
	private static final List<CreatedValue> SYMPTOMS_ = SYMPTOMS.stream().map(o -> new CreatedValue(o.id, o.name, null)).collect(toList());

	private static People create()
	{
		return new People("123", "max", "888-555-1000", "max@gmail.com", null, "Max", "Power", DOB,
			"1", INFLUENCER.id, "0", NEITHER.id, LAT, LNG, "Omaha, NE", true, true, AUTH_AT, PHONE_VERIFIED_AT, EMAIL_VERIFIED_AT,
			ALERTED_OF, ALERTED_AT, CREATED_AT);
	}

	private static People createX()
	{
		var o = create();
		o.setConditions(CONDITIONS.stream().map(t -> new Conditions(t.id)).collect(toList()));
		o.setExposures(EXPOSURES.stream().map(t -> new Exposures(t.id)).collect(toList()));
		o.setSymptoms(SYMPTOMS.stream().map(t -> new Symptoms(t.id)).collect(toList()));

		return o;
	}

	private static People createWithPassword()
	{
		return new People("123", "max", "888-555-1000", "max@gmail.com", "Password_1", "Max", "Power", DOB,
			"1", INFLUENCER.id, "0", NEITHER.id, LAT, LNG, "Omaha, NE", true, true, null, PHONE_VERIFIED_AT, EMAIL_VERIFIED_AT,
			ALERTED_OF, ALERTED_AT, CREATED_AT);
	}

	@Test
	public void auth()
	{
		var o = create();
		Assertions.assertFalse(o.checkPassword("Password_1"), "checkPassword: Without password");
		Assertions.assertEquals(AUTH_AT, o.authAt, "checkPassword: authAt");
		Assertions.assertFalse(o.auth("Password_1"), "auth: Without password");
		Assertions.assertEquals(AUTH_AT, o.authAt, "auth: authAt");

		o = createWithPassword();
		Assertions.assertTrue(o.checkPassword("Password_1"), "checkPassword: With password");
		Assertions.assertNull(o.authAt, "checkPassword: authAt");
		Assertions.assertFalse(o.checkPassword("Password"), "checkPassword: With password");
		Assertions.assertNull(o.authAt, "checkPassword: authAt");
		Assertions.assertTrue(o.auth("Password_1"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isCloseTo(new Date(), 100L).isNotEqualTo(AUTH_AT);

		var authAt = o.authAt;
		Assertions.assertFalse(o.auth("Password"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isEqualTo(authAt);
	}

	@Test
	public void auth_after_update()
	{
		var o = create().update(new PeopleValue().withPassword("Password_1"), false);
		Assertions.assertFalse(o.checkPassword("Password_1"), "checkPassword: Without password");
		Assertions.assertEquals(AUTH_AT, o.authAt, "checkPassword: authAt");
		Assertions.assertFalse(o.auth("Password_1"), "auth: Without password");
		Assertions.assertEquals(AUTH_AT, o.authAt, "auth: authAt");

		o = create().update(new PeopleValue().withPassword("Password_1"), true);
		Assertions.assertTrue(o.checkPassword("Password_1"), "checkPassword: Without password");
		Assertions.assertNull(o.authAt, "checkPassword: authAt");
		Assertions.assertTrue(o.auth("Password_1"), "auth: Without password");
		assertThat(o.authAt).as("auth: authAt").isCloseTo(new Date(), 100L).isNotEqualTo(AUTH_AT);

		o = createWithPassword().update(new PeopleValue().withPassword("Password_2"), false);
		Assertions.assertTrue(o.checkPassword("Password_1"), "checkPassword: With password");
		Assertions.assertFalse(o.checkPassword("Password_2"), "checkPassword: With password");
		Assertions.assertNull(o.authAt, "checkPassword: authAt");
		Assertions.assertTrue(o.auth("Password_1"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isCloseTo(new Date(), 100L).isNotEqualTo(AUTH_AT);

		var authAt = o.authAt;
		Assertions.assertFalse(o.auth("Password_2"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isEqualTo(authAt);

		o = createWithPassword().update(new PeopleValue().withPassword("Password_2"), true);
		Assertions.assertFalse(o.checkPassword("Password_1"), "checkPassword: With password");
		Assertions.assertTrue(o.checkPassword("Password_2"), "checkPassword: With password");
		Assertions.assertNull(o.authAt, "checkPassword: authAt");
		Assertions.assertFalse(o.auth("Password_1"), "auth: With password");
		Assertions.assertNull(o.authAt, "checkPassword: authAt");
		Assertions.assertTrue(o.auth("Password_2"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isCloseTo(new Date(), 100L).isNotEqualTo(AUTH_AT);

		o = createWithPassword().update(new PeopleValue().withPassword(null), false);
		Assertions.assertTrue(o.checkPassword("Password_1"), "checkPassword: With password");
		Assertions.assertFalse(o.checkPassword("Password_2"), "checkPassword: With password");
		Assertions.assertNull(o.authAt, "checkPassword: authAt");
		Assertions.assertTrue(o.auth("Password_1"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isCloseTo(new Date(), 100L).isNotEqualTo(AUTH_AT);

		authAt = o.authAt;
		Assertions.assertFalse(o.auth("Password_2"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isEqualTo(authAt);

		o = createWithPassword().update(new PeopleValue().withPassword(null), true);
		Assertions.assertTrue(o.checkPassword("Password_1"), "checkPassword: With password");
		Assertions.assertFalse(o.checkPassword("Password_2"), "checkPassword: With password");
		Assertions.assertNull(o.authAt, "checkPassword: authAt");
		Assertions.assertTrue(o.auth("Password_1"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isCloseTo(new Date(), 100L).isNotEqualTo(AUTH_AT);

		authAt = o.authAt;
		Assertions.assertFalse(o.auth("Password_2"), "auth: With password");
		assertThat(o.authAt).as("auth: authAt").isEqualTo(authAt);
	}

	public static Stream<Arguments> toValue()
	{
		return Stream.of(
			arguments(ALL, ALL.id, true),
			arguments(ALL, FRIENDS.id, false),
			arguments(ALL, ME.id, false),
			arguments(FRIENDS, ALL.id, true),
			arguments(FRIENDS, FRIENDS.id, true),
			arguments(FRIENDS, ME.id, false),
			arguments(ME, ALL.id, true),
			arguments(ME, FRIENDS.id, true),
			arguments(ME, ME.id, true));
	}

	@ParameterizedTest
	@MethodSource
	public void toValue(final Visibility who,
		final String visibilityId,
		final boolean available)
	{
		var o = create();
		o.setField(new PeopleField(o.id, visibilityId, null, null, null));

		var v = o.toValue(who);
		Assertions.assertEquals("123", v.id, "Check id");
		Assertions.assertEquals("max", v.name, "Check name");
		Assertions.assertEquals("888-555-1000", v.phone, "Check phone");
		Assertions.assertEquals("max@gmail.com", v.email, "Check email");
		Assertions.assertNull(v.password, "Check password");
		Assertions.assertEquals("Max", v.firstName, "Check firstName");
		Assertions.assertEquals("Power", v.lastName, "Check lastName");
		Assertions.assertEquals(DOB, v.dob, "Check dob");
		Assertions.assertEquals("1", v.statusId, "Check statusId");
		Assertions.assertEquals(INFLUENCER.id, v.statureId, "Check statureId");
		Assertions.assertEquals("0", v.sexId, "Check sexId");
		Assertions.assertEquals(LAT, v.latitude, "Check latitude");
		Assertions.assertEquals(LNG, v.longitude, "Check longitude");
		Assertions.assertEquals("Omaha, NE", v.locationName, "Check locationName");
		Assertions.assertTrue(v.alertable, "Check alertable");
		Assertions.assertTrue(v.active, "Check active");
		Assertions.assertEquals(AUTH_AT, v.authAt, "Check authAt");
		Assertions.assertEquals(PHONE_VERIFIED_AT, v.phoneVerifiedAt, "Check phoneVerifiedAt");
		Assertions.assertEquals(EMAIL_VERIFIED_AT, v.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(ALERTED_OF, v.alertedOf, "Check alertedOf");
		Assertions.assertEquals(ALERTED_AT, v.alertedAt, "Check alertedAt");
		Assertions.assertEquals(CREATED_AT, v.createdAt, "Check createdAt");
		Assertions.assertEquals(CREATED_AT, v.updatedAt, "Check updatedAt");

		if (available)
		{
			Assertions.assertEquals(NEITHER.id, v.healthWorkerStatusId, "Check healthWorkerStatusId");
			Assertions.assertEquals(NEITHER, v.healthWorkerStatus, "Check healthWorkerStatus");
		}
		else
		{
			Assertions.assertNull(v.healthWorkerStatusId, "Check healthWorkerStatusId");
			Assertions.assertNull(v.healthWorkerStatus, "Check healthWorkerStatus");
		}
	}

	public static Stream<Arguments> toValueX()
	{
		return Stream.of(
			arguments(ALL, ALL.id, ALL.id, ALL.id, ALL.id, true, true, true, true),
			arguments(FRIENDS, ALL.id, ALL.id, ALL.id, ALL.id, true, true, true, true),
			arguments(ME, ALL.id, ALL.id, ALL.id, ALL.id, true, true, true, true),
			arguments(ALL, ALL.id, FRIENDS.id, ME.id, ALL.id, true, false, false, true),
			arguments(FRIENDS, ALL.id, FRIENDS.id, ME.id, ALL.id, true, true, false, true),
			arguments(ME, ALL.id, FRIENDS.id, ME.id, ALL.id, true, true, true, true),
			arguments(ALL, FRIENDS.id, FRIENDS.id, FRIENDS.id, FRIENDS.id, false, false, false, false),
			arguments(FRIENDS, FRIENDS.id, FRIENDS.id, FRIENDS.id, FRIENDS.id, true, true, true, true),
			arguments(ME, FRIENDS.id, FRIENDS.id, FRIENDS.id, FRIENDS.id, true, true, true, true),
			arguments(ALL, ME.id, FRIENDS.id, FRIENDS.id, ALL.id, false, false, false, true),
			arguments(FRIENDS, ME.id, FRIENDS.id, FRIENDS.id, ALL.id, false, true, true, true),
			arguments(ME, ME.id, FRIENDS.id, FRIENDS.id, ALL.id, true, true, true, true),
			arguments(ALL, ME.id, ME.id, ME.id, ME.id, false, false, false, false),
			arguments(FRIENDS, ME.id, ME.id, ME.id, ME.id, false, false, false, false),
			arguments(ME, ME.id, ME.id, ME.id, ME.id, true, true, true, true),
			arguments(ALL, FRIENDS.id, ME.id, ALL.id, ME.id, false, false, true, false),
			arguments(FRIENDS, FRIENDS.id, ME.id, ALL.id, ME.id, true, false, true, false),
			arguments(ME, ME.id, FRIENDS.id, ME.id, ALL.id, true, true, true, true));
	}

	@ParameterizedTest
	@MethodSource
	public void toValueX(final Visibility who,
		final String visibilityHealthWorkerStatusId,
		final String visibilityConditions,
		final String visibilityExposures,
		final String visibilitySymptoms,
		final boolean hasHealthWorkerStatus,
		final boolean hasConditions,
		final boolean hasExposures,
		final boolean hasSymptoms)
	{
		var o = createX();
		o.setField(new PeopleField(o.id, visibilityHealthWorkerStatusId, visibilityConditions, visibilityExposures, visibilitySymptoms));

		var v = o.toValueX(who);
		Assertions.assertEquals("123", v.id, "Check id");
		Assertions.assertEquals("max", v.name, "Check name");
		Assertions.assertEquals("888-555-1000", v.phone, "Check phone");
		Assertions.assertEquals("max@gmail.com", v.email, "Check email");
		Assertions.assertNull(v.password, "Check password");
		Assertions.assertEquals("Max", v.firstName, "Check firstName");
		Assertions.assertEquals("Power", v.lastName, "Check lastName");
		Assertions.assertEquals(DOB, v.dob, "Check dob");
		Assertions.assertEquals("1", v.statusId, "Check statusId");
		Assertions.assertEquals(INFLUENCER.id, v.statureId, "Check statureId");
		Assertions.assertEquals("0", v.sexId, "Check sexId");
		Assertions.assertEquals(LAT, v.latitude, "Check latitude");
		Assertions.assertEquals(LNG, v.longitude, "Check longitude");
		Assertions.assertEquals("Omaha, NE", v.locationName, "Check locationName");
		Assertions.assertTrue(v.alertable, "Check alertable");
		Assertions.assertTrue(v.active, "Check active");
		Assertions.assertEquals(AUTH_AT, v.authAt, "Check authAt");
		Assertions.assertEquals(PHONE_VERIFIED_AT, v.phoneVerifiedAt, "Check phoneVerifiedAt");
		Assertions.assertEquals(EMAIL_VERIFIED_AT, v.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(ALERTED_OF, v.alertedOf, "Check alertedOf");
		Assertions.assertEquals(ALERTED_AT, v.alertedAt, "Check alertedAt");
		Assertions.assertEquals(CREATED_AT, v.createdAt, "Check createdAt");
		Assertions.assertEquals(CREATED_AT, v.updatedAt, "Check updatedAt");

		if (hasHealthWorkerStatus)
		{
			Assertions.assertEquals(NEITHER.id, v.healthWorkerStatusId, "Check healthWorkerStatusId");
			Assertions.assertEquals(NEITHER, v.healthWorkerStatus, "Check healthWorkerStatus");
		}
		else
		{
			Assertions.assertNull(v.healthWorkerStatusId, "Check healthWorkerStatusId");
			Assertions.assertNull(v.healthWorkerStatus, "Check healthWorkerStatus");
		}

		if (hasConditions)
			Assertions.assertEquals(CONDITIONS_, v.conditions, "Check conditions");
		else
			Assertions.assertNull(v.conditions, "Check conditions");

		if (hasExposures)
			Assertions.assertEquals(EXPOSURES_, v.exposures, "Check exposures");
		else
			Assertions.assertNull(v.exposures, "Check exposures");

		if (hasSymptoms)
			Assertions.assertEquals(SYMPTOMS_, v.symptoms, "Check symptoms");
		else
			Assertions.assertNull(v.symptoms, "Check symptoms");
	}

	@ParameterizedTest
	@MethodSource("toValue")
	public void toValueX_conditions(final Visibility who,
		final String visibilityId,
		final boolean available)
	{
		var o = createX();
		o.setField(new PeopleField(o.id, visibilityId, visibilityId, visibilityId, visibilityId));

		var v = o.toValueX(who);
		if (available)
			Assertions.assertEquals(CONDITIONS_, v.conditions, "Check conditions");
		else
			Assertions.assertNull(v.conditions, "Check conditions");
	}

	@ParameterizedTest
	@MethodSource("toValue")
	public void toValueX_exposures(final Visibility who,
		final String visibilityId,
		final boolean available)
	{
		var o = createX();
		o.setField(new PeopleField(o.id, visibilityId, visibilityId, visibilityId, visibilityId));

		var v = o.toValueX(who);
		if (available)
			Assertions.assertEquals(EXPOSURES_, v.exposures, "Check exposures");
		else
			Assertions.assertNull(v.exposures, "Check exposures");
	}

	@ParameterizedTest
	@MethodSource("toValue")
	public void toValueX_symptoms(final Visibility who,
		final String visibilityId,
		final boolean available)
	{
		var o = createX();
		o.setField(new PeopleField(o.id, visibilityId, visibilityId, visibilityId, visibilityId));

		var v = o.toValueX(who);
		if (available)
			Assertions.assertEquals(SYMPTOMS_, v.symptoms, "Check symptoms");
		else
			Assertions.assertNull(v.symptoms, "Check symptoms");
	}

	@ParameterizedTest
	@MethodSource("toValue")
	public void toValue_withPassword(final Visibility who,
		final String visibilityId,
		final boolean available)
	{
		var o = createWithPassword();
		o.setField(new PeopleField(o.id, visibilityId, visibilityId, visibilityId, visibilityId));

		Assertions.assertNull(o.toValue(who).password);
	}

	@ParameterizedTest
	@MethodSource("toValue")
	public void toValueX_withPassword(final Visibility who,
		final String visibilityId,
		final boolean available)
	{
		var o = createWithPassword();
		o.setField(new PeopleField(o.id, visibilityId, visibilityId, visibilityId, visibilityId));

		Assertions.assertNull(o.toValueX(who).password);
	}

	@Test
	public void update()
	{
		var o = create();
		var v = new PeopleValue("456", "min", "888-555-1001", "minnie@gmail.com", "Minnie", "Mouse", DOB_1,
			"11", null, "12", null, "10", null, "13", null, LAT_1, LNG_1, "Detroit, MI", false, false,
			AUTH_AT_1, PHONE_VERIFIED_AT_1, EMAIL_VERIFIED_AT_1, ALERTED_OF_1, ALERTED_AT_1, CREATED_AT_1, null);
		Assertions.assertEquals(CREATED_AT, o.updatedAt, "Check updatedAt: before");

		o.update(v, false);
		Assertions.assertEquals("123", o.id, "Check id");
		Assertions.assertEquals("min", o.name, "Check name");
		Assertions.assertEquals("888-555-1001", o.phone, "Check phone");
		Assertions.assertEquals("minnie@gmail.com", o.email, "Check email");
		Assertions.assertNull(v.password, "Check password");
		Assertions.assertEquals("Minnie", o.firstName, "Check firstName");
		Assertions.assertEquals("Mouse", o.lastName, "Check lastName");
		Assertions.assertEquals(DOB_1, o.dob, "Check dob");
		Assertions.assertEquals("11", o.statusId, "Check statusId");
		Assertions.assertEquals(INFLUENCER.id, o.statureId, "Check statureId");	// Ignored for non-admins.
		Assertions.assertEquals("10", o.sexId, "Check sexId");
		Assertions.assertEquals("13", o.healthWorkerStatusId, "Check healthWorkerStatusId");
		Assertions.assertEquals(LAT_1, o.latitude, "Check latitude");
		Assertions.assertEquals(LNG_1, o.longitude, "Check longitude");
		Assertions.assertEquals("Detroit, MI", o.locationName, "Check locationName");
		Assertions.assertFalse(o.alertable, "Check alertable");
		Assertions.assertTrue(o.active, "Check active");	// Ignored for non-admins.
		Assertions.assertEquals(AUTH_AT, o.authAt, "Check authAt");	// Ignored for non-admins.
		Assertions.assertEquals(PHONE_VERIFIED_AT, o.phoneVerifiedAt, "Check phoneVerifiedAt");	// Ignored for non-admins.
		Assertions.assertEquals(EMAIL_VERIFIED_AT, o.emailVerifiedAt, "Check emailVerifiedAt");	// Ignored for non-admins.
		Assertions.assertEquals(ALERTED_OF, o.alertedOf, "Check alertedOf");	// Ignored for non-admins.
		Assertions.assertEquals(ALERTED_AT, o.alertedAt, "Check alertedAt");	// Ignored for non-admins.
		Assertions.assertEquals(ALERTED_AT, o.alertedAt(), "Check alertedAt(): before");
		Assertions.assertEquals(CREATED_AT, o.createdAt, "Check createdAt");	// NOT set during update
		assertThat(o.updatedAt).as("Check updatedAt: after").isAfterOrEqualsTo(CREATED_AT);

		// Value object also reverted.
		Assertions.assertTrue(o.active, "Check active: value");
		Assertions.assertEquals(AUTH_AT, v.authAt, "Check authAt: value");
		Assertions.assertEquals(PHONE_VERIFIED_AT, v.phoneVerifiedAt, "Check phoneVerifiedAt: value");
		Assertions.assertEquals(EMAIL_VERIFIED_AT, v.emailVerifiedAt, "Check emailVerifiedAt: value");
		Assertions.assertEquals(ALERTED_OF, v.alertedOf, "Check alertedOf: value");
		Assertions.assertEquals(ALERTED_AT, v.alertedAt, "Check alertedAt: value");

		// Check alertedAt() method.
		o.setAlertedAt(null);
		assertThat(o.alertedAt()).as("Check alertedAt(): after").isEqualTo(AUTH_AT).isNotEqualTo(ALERTED_AT);	// Fails over to authAt if alertedAt is null.
	}

	@Test
	public void update_as_admin()
	{
		var o = create();
		var v = new PeopleValue("456", "min", "888-555-1001", "minnie@gmail.com", "Minnie", "Mouse", DOB_1,
			"11", null, "12", null, "10", null, "13", null, LAT_1, LNG_1, "Detroit, MI", false, false,
			AUTH_AT_1, PHONE_VERIFIED_AT_1, EMAIL_VERIFIED_AT_1, ALERTED_OF_1, ALERTED_AT_1, CREATED_AT_1, null);
		Assertions.assertEquals(CREATED_AT, o.updatedAt, "Check updatedAt: before");

		o.update(v, true);
		Assertions.assertEquals("123", o.id, "Check id");
		Assertions.assertEquals("min", o.name, "Check name");
		Assertions.assertEquals("888-555-1001", o.phone, "Check phone");
		Assertions.assertEquals("minnie@gmail.com", o.email, "Check email");
		Assertions.assertNull(v.password, "Check password");
		Assertions.assertEquals("Minnie", o.firstName, "Check firstName");
		Assertions.assertEquals("Mouse", o.lastName, "Check lastName");
		Assertions.assertEquals(DOB_1, o.dob, "Check dob");
		Assertions.assertEquals("11", o.statusId, "Check statusId");
		Assertions.assertEquals("12", o.statureId, "Check statureId");
		Assertions.assertEquals("10", o.sexId, "Check sexId");
		Assertions.assertEquals("13", o.healthWorkerStatusId, "Check healthWorkerStatusId");
		Assertions.assertEquals(LAT_1, o.latitude, "Check latitude");
		Assertions.assertEquals(LNG_1, o.longitude, "Check longitude");
		Assertions.assertEquals("Detroit, MI", o.locationName, "Check locationName");
		Assertions.assertFalse(o.alertable, "Check alertable");
		Assertions.assertFalse(o.active, "Check active");
		Assertions.assertEquals(AUTH_AT_1, o.authAt, "Check authAt");
		Assertions.assertEquals(PHONE_VERIFIED_AT_1, o.phoneVerifiedAt, "Check phoneVerifiedAt");
		Assertions.assertEquals(EMAIL_VERIFIED_AT_1, o.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(ALERTED_OF_1, o.alertedOf, "Check alertedOf");
		Assertions.assertEquals(ALERTED_AT_1, o.alertedAt, "Check alertedAt");
		Assertions.assertEquals(ALERTED_AT_1, o.alertedAt(), "Check alertedAt(): before");
		Assertions.assertEquals(CREATED_AT, o.createdAt, "Check createdAt");	// NOT set during update
		assertThat(o.updatedAt).as("Check updatedAt: after").isAfterOrEqualsTo(CREATED_AT);

		// Value object NOT reverted.
		Assertions.assertFalse(o.active, "Check active: value");
		Assertions.assertEquals(AUTH_AT_1, v.authAt, "Check authAt: value");
		Assertions.assertEquals(PHONE_VERIFIED_AT_1, v.phoneVerifiedAt, "Check phoneVerifiedAt: value");
		Assertions.assertEquals(EMAIL_VERIFIED_AT_1, v.emailVerifiedAt, "Check emailVerifiedAt: value");
		Assertions.assertEquals(ALERTED_OF_1, v.alertedOf, "Check alertedOf: value");
		Assertions.assertEquals(ALERTED_AT_1, v.alertedAt, "Check alertedAt: value");

		// Check alertedAt() method.
		o.setAlertedAt(null);
		assertThat(o.alertedAt()).as("Check alertedAt(): after").isEqualTo(AUTH_AT_1).isNotEqualTo(ALERTED_AT_1);	// Fails over to authAt if alertedAt is null.
	}

	@Test
	public void update_with_null_stature()
	{
		var o = create();
		o.setStatureId(null);
		o.setAlertedOf(null);

		var v = new PeopleValue("456", "min", "888-555-1001", "minnie@gmail.com", "Minnie", "Mouse", DOB_1,
			"11", null, "12", null, "10", null, "13", null, LAT_1, LNG_1, "Detroit, MI", false, false,
			AUTH_AT_1, PHONE_VERIFIED_AT_1, EMAIL_VERIFIED_AT_1, ALERTED_OF_1, ALERTED_AT_1, CREATED_AT_1, null);
		Assertions.assertEquals(CREATED_AT, o.updatedAt, "Check updatedAt: before");

		o.update(v, false);
		Assertions.assertEquals("123", o.id, "Check id");
		Assertions.assertEquals("min", o.name, "Check name");
		Assertions.assertEquals("888-555-1001", o.phone, "Check phone");
		Assertions.assertEquals("minnie@gmail.com", o.email, "Check email");
		Assertions.assertNull(v.password, "Check password");
		Assertions.assertEquals("Minnie", o.firstName, "Check firstName");
		Assertions.assertEquals("Mouse", o.lastName, "Check lastName");
		Assertions.assertEquals(DOB_1, o.dob, "Check dob");
		Assertions.assertEquals("11", o.statusId, "Check statusId");
		Assertions.assertNull(o.statureId, "Check statureId");	// Ignored for non-admins.
		Assertions.assertEquals("10", o.sexId, "Check sexId");
		Assertions.assertEquals("13", o.healthWorkerStatusId, "Check healthWorkerStatusId");
		Assertions.assertEquals(LAT_1, o.latitude, "Check latitude");
		Assertions.assertEquals(LNG_1, o.longitude, "Check longitude");
		Assertions.assertEquals("Detroit, MI", o.locationName, "Check locationName");
		Assertions.assertFalse(o.alertable, "Check alertable");
		Assertions.assertTrue(o.active, "Check active");	// Ignored for non-admins.
		Assertions.assertEquals(AUTH_AT, o.authAt, "Check authAt");	// Ignored for non-admins.
		Assertions.assertEquals(PHONE_VERIFIED_AT, o.phoneVerifiedAt, "Check phoneVerifiedAt");	// Ignored for non-admins.
		Assertions.assertEquals(EMAIL_VERIFIED_AT, o.emailVerifiedAt, "Check emailVerifiedAt");	// Ignored for non-admins.
		Assertions.assertNull(o.alertedOf, "Check alertedOf");	// Ignored for non-admins.
		Assertions.assertEquals(ALERTED_AT, o.alertedAt, "Check alertedAt");	// Ignored for non-admins.
		Assertions.assertEquals(ALERTED_AT, o.alertedAt(), "Check alertedAt(): before");
		Assertions.assertEquals(CREATED_AT, o.createdAt, "Check createdAt");	// NOT set during update
		assertThat(o.updatedAt).as("Check updatedAt: after").isAfterOrEqualsTo(CREATED_AT);

		// Value object also reverted.
		Assertions.assertTrue(o.active, "Check active: value");
		Assertions.assertEquals(AUTH_AT, v.authAt, "Check authAt: value");
		Assertions.assertEquals(PHONE_VERIFIED_AT, v.phoneVerifiedAt, "Check phoneVerifiedAt: value");
		Assertions.assertEquals(EMAIL_VERIFIED_AT, v.emailVerifiedAt, "Check emailVerifiedAt: value");
		Assertions.assertNull(v.alertedOf, "Check alertedOf");
		Assertions.assertEquals(ALERTED_AT, v.alertedAt, "Check alertedAt: value");

		// Check alertedAt() method.
		o.setAlertedAt(null);
		assertThat(o.alertedAt()).as("Check alertedAt(): after").isEqualTo(AUTH_AT).isNotEqualTo(ALERTED_AT);	// Fails over to authAt if alertedAt is null.
	}
}
