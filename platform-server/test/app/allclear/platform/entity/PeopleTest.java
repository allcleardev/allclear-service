package app.allclear.platform.entity;

import static org.fest.assertions.api.Assertions.assertThat;
import static app.allclear.platform.type.PeopleStature.*;
import static app.allclear.testing.TestingUtils.*;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.*;

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

	private static People create()
	{
		return new People("123", "max", "888-555-1000", "max@gmail.com", "Max", "Power", DOB,
			"1", INFLUENCER.id, "0", "3", LAT, LNG, "Omaha, NE", true, true, AUTH_AT, PHONE_VERIFIED_AT, EMAIL_VERIFIED_AT,
			ALERTED_OF, ALERTED_AT, CREATED_AT);
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
