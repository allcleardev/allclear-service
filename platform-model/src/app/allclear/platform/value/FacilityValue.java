package app.allclear.platform.value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.*;

/**********************************************************************************
*
*	Value object class that represents the facility table.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
**********************************************************************************/

public class FacilityValue implements Auditable, Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "facility";
	public static final int MAX_LEN_NAME = 128;
	public static final int MAX_LEN_ADDRESS = 128;
	public static final int MAX_LEN_CITY = 128;
	public static final int MAX_LEN_STATE = 128;
	public static final int MAX_LEN_PHONE = 32;
	public static final int MAX_LEN_APPOINTMENT_PHONE = 32;
	public static final int MAX_LEN_EMAIL = 128;
	public static final int MAX_LEN_URL = 255;
	public static final int MAX_LEN_APPOINTMENT_URL = 255;
	public static final int MAX_LEN_HOURS = 65535;
	public static final int MAX_LEN_TYPE_ID = 2;
	public static final int MAX_LEN_TEST_CRITERIA_ID = 2;
	public static final int MAX_LEN_OTHER_TEST_CRITERIA = 65535;
	public static final int MAX_LEN_DOCTOR_REFERRAL_CRITERIA = 65535;
	public static final int MAX_LEN_INSURANCE_PROVIDERS_ACCEPTED = 65535;
	public static final int MAX_LEN_NOTES = 65535;

	// Members
	public Long id = null;
	public String name = null;
	public String address = null;
	public String city = null;
	public String state = null;
	public BigDecimal latitude = null;
	public BigDecimal longitude = null;
	public String phone = null;
	public String appointmentPhone = null;
	public String email = null;
	public String url = null;
	public String appointmentUrl = null;
	public String hours = null;
	public String typeId = null;
	public FacilityType type = null;
	public boolean driveThru;
	public Boolean appointmentRequired = null;
	public Boolean acceptsThirdParty = null;
	public boolean referralRequired;
	public String testCriteriaId = null;
	public TestCriteria testCriteria = null;
	public String otherTestCriteria = null;
	public Integer testsPerDay = null;
	public boolean governmentIdRequired;
	public Integer minimumAge = null;
	public String doctorReferralCriteria = null;
	public boolean firstResponderFriendly;
	public boolean telescreeningAvailable;
	public boolean acceptsInsurance;
	public String insuranceProvidersAccepted = null;
	public boolean freeOrLowCost;
	public boolean canDonatePlasma = false;
	public boolean resultNotificationEnabled = false;	// ALLCLEAR-602: DLS on 7/5/2020.
	public String notes = null;
	public boolean active;
	public Date activatedAt = null;	// ALLCLEAR-533: DLS on 5/12/2020.
	public Date createdAt = null;
	public Date updatedAt = null;
	public Long meters = null;	// A result included on distance searches. DLS on 4/3/2020
	public Boolean restricted = null;	// Is the current user restricted from accessing this site. Populated in FacilityResource.search. DLS on 4/8/2020.
	public Boolean favorite = null;	// ALLCLEAR-259: Has this facility been bookmarked/favorited by the current user? DLS on 4/15/2020.
	public List<CreatedValue> testTypes = null;	// ALLCLEAR-463: DLS on 5/9/2020.
	public List<CreatedValue> people = null;	// ALLCLEAR-582: DLS on 7/5/2020.

	// Accessors
	public boolean restricted() { return (null != testCriteria) && testCriteria.restricted; }

	// Auditable methods
	@Override public String id() { return id.toString(); }
	@Override public String tableName() { return TABLE; }
	@Override public Date updatedAt() { return updatedAt; }

	// Mutators
	public FacilityValue withId(final Long newValue) { id = newValue; return this; }
	public FacilityValue withName(final String newValue) { name = newValue; return this; }
	public FacilityValue withAddress(final String newValue) { address = newValue; return this; }
	public FacilityValue withCity(final String newValue) { city = newValue; return this; }
	public FacilityValue withState(final String newValue) { state = newValue; return this; }
	public FacilityValue withLatitude(final BigDecimal newValue) { latitude = newValue; return this; }
	public FacilityValue withLongitude(final BigDecimal newValue) { longitude = newValue; return this; }
	public FacilityValue withPhone(final String newValue) { phone = newValue; return this; }
	public FacilityValue withAppointmentPhone(final String newValue) { appointmentPhone = newValue; return this; }
	public FacilityValue withEmail(final String newValue) { email = newValue; return this; }
	public FacilityValue withUrl(final String newValue) { url = newValue; return this; }
	public FacilityValue withAppointmentUrl(final String newValue) { appointmentUrl = newValue; return this; }
	public FacilityValue withHours(final String newValue) { hours = newValue; return this; }
	public FacilityValue withTypeId(final String newValue) { typeId = newValue; return this; }
	public FacilityValue withType(final FacilityType newValue) { type = newValue; return this; }
	public FacilityValue withDriveThru(final boolean newValue) { driveThru = newValue; return this; }
	public FacilityValue withAppointmentRequired(final Boolean newValue) { appointmentRequired = newValue; return this; }
	public FacilityValue withAcceptsThirdParty(final Boolean newValue) { acceptsThirdParty = newValue; return this; }
	public FacilityValue withReferralRequired(final boolean newValue) { referralRequired = newValue; return this; }
	public FacilityValue withTestCriteriaId(final String newValue) { testCriteriaId = newValue; return this; }
	public FacilityValue withTestCriteria(final TestCriteria newValue) { testCriteria = newValue; return this; }
	public FacilityValue withOtherTestCriteria(final String newValue) { otherTestCriteria = newValue; return this; }
	public FacilityValue withTestsPerDay(final Integer newValue) { testsPerDay = newValue; return this; }
	public FacilityValue withGovernmentIdRequired(final boolean newValue) { governmentIdRequired = newValue; return this; }
	public FacilityValue withMinimumAge(final Integer newValue) { minimumAge = newValue; return this; }
	public FacilityValue withDoctorReferralCriteria(final String newValue) { doctorReferralCriteria = newValue; return this; }
	public FacilityValue withFirstResponderFriendly(final boolean newValue) { firstResponderFriendly = newValue; return this; }
	public FacilityValue withTelescreeningAvailable(final boolean newValue) { telescreeningAvailable = newValue; return this; }
	public FacilityValue withAcceptsInsurance(final boolean newValue) { acceptsInsurance = newValue; return this; }
	public FacilityValue withInsuranceProvidersAccepted(final String newValue) { insuranceProvidersAccepted = newValue; return this; }
	public FacilityValue withFreeOrLowCost(final boolean newValue) { freeOrLowCost = newValue; return this; }
	public FacilityValue withCanDonatePlasma(final boolean newValue) { canDonatePlasma = newValue; return this; }
	public FacilityValue withResultNotificationEnabled(final boolean newValue) { resultNotificationEnabled = newValue; return this; }
	public FacilityValue withNotes(final String newValue) { notes = newValue; return this; }
	public FacilityValue withActive(final boolean newValue) { active = newValue; return this; }
	public FacilityValue withActivatedAt(final Date newValue) { activatedAt = newValue; return this; }
	public FacilityValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public FacilityValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }
	public FacilityValue withMeters(final Long newValue) { meters = newValue; return this; }
	public FacilityValue withRestricted(final Boolean newValue) { restricted = newValue; return this; }
	public FacilityValue withFavorite(final Boolean newValue) { favorite = newValue; return this; }
	public FacilityValue favorite(final List<Long> ids) { favorite = ids.contains(id); return this; }
	public FacilityValue nullTestTypes() { testTypes = null; return this; }
	public FacilityValue emptyTestTypes() { testTypes = List.of(); return this; }
	public FacilityValue withTestTypes(final List<CreatedValue> newValues) { testTypes = newValues; return this; }
	public FacilityValue withTestTypes(final TestType... newValues) { return withTestTypes(Arrays.stream(newValues).map(o -> o.created()).collect(Collectors.toList())); }
	public FacilityValue nullPeople() { people = null; return this; }
	public FacilityValue emptyPeople() { people = List.of(); return this; }
	public FacilityValue withPeople(final List<CreatedValue> newValues) { people = newValues; return this; }
	public FacilityValue withPeople(final PeopleValue... newValues) { return withPeople(Arrays.stream(newValues).map(o -> o.created()).collect(Collectors.toList())); }

	public FacilityValue() {}

	public FacilityValue(final int i)	// For tests
	{
		this(i, "City" + i, "State " + i, i, i, true);
	}

	public FacilityValue(final int i, final String city, final String state, final int lat, final int lng, final boolean active)	// For tests
	{
		this("Test Center " + i, "Address " + i, city, state, new BigDecimal(lat), new BigDecimal(lng), false, false, false, false, false, false, false, false, false, active);
	}

	public FacilityValue(
		final String name,
		final String address,
		final boolean driveThru,
		final boolean referralRequired,
		final boolean governmentIdRequired,
		final boolean firstResponderFriendly,
		final boolean telescreeningAvailable,
		final boolean acceptsInsurance,
		final boolean freeOrLowCost,
		final boolean canDonatePlasma,
		final boolean resultNotificationEnabled,
		final boolean active)
	{
		this(name, address, null, null, null, null, driveThru, referralRequired, governmentIdRequired,
			firstResponderFriendly, telescreeningAvailable, acceptsInsurance, freeOrLowCost, canDonatePlasma,
			resultNotificationEnabled, active);
	}

	public FacilityValue(
		final String name,
		final String address,
		final String city,
		final String state,
		final BigDecimal latitude,
		final BigDecimal longitude,
		final boolean driveThru,
		final boolean referralRequired,
		final boolean governmentIdRequired,
		final boolean firstResponderFriendly,
		final boolean telescreeningAvailable,
		final boolean acceptsInsurance,
		final boolean freeOrLowCost,
		final boolean canDonatePlasma,
		final boolean resultNotificationEnabled,
		final boolean active)
	{
		this(name, address, city, state, latitude, longitude, null, null, null, null, null, null, null,
				driveThru, null, null, referralRequired, null, null, null,
				governmentIdRequired, null, null, firstResponderFriendly, telescreeningAvailable, acceptsInsurance,
				null, freeOrLowCost, canDonatePlasma, resultNotificationEnabled, null, active);
	}

	public FacilityValue(
		final String name,
		final String address,
		final String city,
		final String state,
		final BigDecimal latitude,
		final BigDecimal longitude,
		final String phone,
		final String appointmentPhone,
		final String email,
		final String url,
		final String appointmentUrl,
		final String hours,
		final String typeId,
		final boolean driveThru,
		final Boolean appointmentRequired,
		final Boolean acceptsThirdParty,
		final boolean referralRequired,
		final String testCriteriaId,
		final String otherTestCriteria,
		final Integer testsPerDay,
		final boolean governmentIdRequired,
		final Integer minimumAge,
		final String doctorReferralCriteria,
		final boolean firstResponderFriendly,
		final boolean telescreeningAvailable,
		final boolean acceptsInsurance,
		final String insuranceProvidersAccepted,
		final boolean freeOrLowCost,
		final boolean canDonatePlasma,
		final boolean resultNotificationEnabled,
		final String notes,
		final boolean active)
	{
		this(null, name, address, city, state, latitude, longitude, phone, appointmentPhone, email, url, appointmentUrl, hours, typeId, null,
			driveThru, appointmentRequired, acceptsThirdParty, referralRequired, testCriteriaId, null, otherTestCriteria, testsPerDay,
			governmentIdRequired, minimumAge, doctorReferralCriteria, firstResponderFriendly, telescreeningAvailable, acceptsInsurance,
			insuranceProvidersAccepted, freeOrLowCost, canDonatePlasma, resultNotificationEnabled, notes, active, null, null, null);
	}

	public FacilityValue(final Long id,
		final String name,
		final String address,
		final String city,
		final String state,
		final BigDecimal latitude,
		final BigDecimal longitude,
		final String phone,
		final String appointmentPhone,
		final String email,
		final String url,
		final String appointmentUrl,
		final String hours,
		final String typeId,
		final FacilityType type,
		final boolean driveThru,
		final Boolean appointmentRequired,
		final Boolean acceptsThirdParty,
		final boolean referralRequired,
		final String testCriteriaId,
		final TestCriteria testCriteria,
		final String otherTestCriteria,
		final Integer testsPerDay,
		final boolean governmentIdRequired,
		final Integer minimumAge,
		final String doctorReferralCriteria,
		final boolean firstResponderFriendly,
		final boolean telescreeningAvailable,
		final boolean acceptsInsurance,
		final String insuranceProvidersAccepted,
		final boolean freeOrLowCost,
		final boolean canDonatePlasma,
		final boolean resultNotificationEnabled,
		final String notes,
		final boolean active,
		final Date activatedAt,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.name = name;
		this.address = address;
		this.city = city;
		this.state = state;
		this.latitude = latitude;
		this.longitude = longitude;
		this.phone = phone;
		this.appointmentPhone = appointmentPhone;
		this.email = email;
		this.url = url;
		this.appointmentUrl = appointmentUrl;
		this.hours = hours;
		this.typeId = typeId;
		this.type = type;
		this.driveThru = driveThru;
		this.appointmentRequired = appointmentRequired;
		this.acceptsThirdParty = acceptsThirdParty;
		this.referralRequired = referralRequired;
		this.testCriteriaId = testCriteriaId;
		this.testCriteria = testCriteria;
		this.otherTestCriteria = otherTestCriteria;
		this.testsPerDay = testsPerDay;
		this.governmentIdRequired = governmentIdRequired;
		this.minimumAge = minimumAge;
		this.doctorReferralCriteria = doctorReferralCriteria;
		this.firstResponderFriendly = firstResponderFriendly;
		this.telescreeningAvailable = telescreeningAvailable;
		this.acceptsInsurance = acceptsInsurance;
		this.insuranceProvidersAccepted = insuranceProvidersAccepted;
		this.freeOrLowCost = freeOrLowCost;
		this.canDonatePlasma = canDonatePlasma;
		this.resultNotificationEnabled = resultNotificationEnabled;
		this.notes = notes;
		this.active = active;
		this.activatedAt = activatedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		name = StringUtils.trimToNull(name);
		address = StringUtils.trimToNull(address);
		city = StringUtils.trimToNull(city);
		state = StringUtils.trimToNull(state);
		phone = StringUtils.trimToNull(phone);
		appointmentPhone = StringUtils.trimToNull(appointmentPhone);
		email = StringUtils.trimToNull(email);
		url = StringUtils.trimToNull(url);
		appointmentUrl = StringUtils.trimToNull(appointmentUrl);
		hours = StringUtils.trimToNull(hours);
		typeId = StringUtils.trimToNull(typeId);
		testCriteriaId = StringUtils.trimToNull(testCriteriaId);
		otherTestCriteria = StringUtils.trimToNull(otherTestCriteria);
		doctorReferralCriteria = StringUtils.trimToNull(doctorReferralCriteria);
		insuranceProvidersAccepted = StringUtils.trimToNull(insuranceProvidersAccepted);
		notes = StringUtils.trimToNull(notes);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof FacilityValue)) return false;

		var v = (FacilityValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(name, v.name) &&
			Objects.equals(address, v.address) &&
			Objects.equals(city, v.city) &&
			Objects.equals(state, v.state) &&
			(0 == Objects.compare(latitude, v.latitude, (a, b) -> a.compareTo(b))) &&
			(0 == Objects.compare(longitude, v.longitude, (a, b) -> a.compareTo(b))) &&
			Objects.equals(phone, v.phone) &&
			Objects.equals(appointmentPhone, v.appointmentPhone) &&
			Objects.equals(email, v.email) &&
			Objects.equals(url, v.url) &&
			Objects.equals(appointmentUrl, v.appointmentUrl) &&
			Objects.equals(hours, v.hours) &&
			Objects.equals(typeId, v.typeId) &&
			(driveThru == v.driveThru) &&
			Objects.equals(appointmentRequired, v.appointmentRequired) &&
			Objects.equals(acceptsThirdParty, v.acceptsThirdParty) &&
			(referralRequired == v.referralRequired) &&
			Objects.equals(testCriteriaId, v.testCriteriaId) &&
			Objects.equals(otherTestCriteria, v.otherTestCriteria) &&
			Objects.equals(testsPerDay, v.testsPerDay) &&
			(governmentIdRequired == v.governmentIdRequired) &&
			Objects.equals(minimumAge, v.minimumAge) &&
			Objects.equals(doctorReferralCriteria, v.doctorReferralCriteria) &&
			(firstResponderFriendly == v.firstResponderFriendly) &&
			(telescreeningAvailable == v.telescreeningAvailable) &&
			(acceptsInsurance == v.acceptsInsurance) &&
			Objects.equals(insuranceProvidersAccepted, v.insuranceProvidersAccepted) &&
			(freeOrLowCost == v.freeOrLowCost) &&
			(canDonatePlasma == v.canDonatePlasma) &&
			(resultNotificationEnabled == v.resultNotificationEnabled) &&
			Objects.equals(notes, v.notes) &&
			(active == v.active) &&
			((activatedAt == v.activatedAt) || DateUtils.truncatedEquals(activatedAt, v.activatedAt, Calendar.SECOND)) &&
			((createdAt == v.createdAt) || DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND)) &&
			((updatedAt == v.updatedAt) || DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND));
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(id);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
