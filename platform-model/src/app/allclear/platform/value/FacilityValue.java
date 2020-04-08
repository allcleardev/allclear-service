package app.allclear.platform.value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.platform.type.FacilityType;
import app.allclear.platform.type.TestCriteria;

/**********************************************************************************
*
*	Value object class that represents the facility table.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
**********************************************************************************/

public class FacilityValue implements Serializable
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
	public String notes = null;
	public boolean active;
	public Date createdAt = null;
	public Date updatedAt = null;
	public Long meters = null;	// A result included on distance searches. DLS on 4/3/2020
	public Boolean restricted = null;	// Is the current user restricted from accessing this site. Populated in FacilityResource.search. DLS on 4/8/2020.

	// Accessors
	public boolean restricted() { return (null != testCriteria) && testCriteria.restricted; }

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
	public FacilityValue withNotes(final String newValue) { notes = newValue; return this; }
	public FacilityValue withActive(final boolean newValue) { active = newValue; return this; }
	public FacilityValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public FacilityValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }
	public FacilityValue withMeters(final Long newValue) { meters = newValue; return this; }
	public FacilityValue withRestricted(final Boolean newValue) { restricted = newValue; return this; }

	public FacilityValue() {}

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
		final boolean active)
	{
		this(name, address, null, null, null, null, driveThru, referralRequired, governmentIdRequired,
			firstResponderFriendly, telescreeningAvailable, acceptsInsurance, freeOrLowCost, active);
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
		final boolean active)
	{
		this(name, address, city, state, latitude, longitude, null, null, null, null, null, null, null,
				driveThru, null, null, referralRequired, null, null, null,
				governmentIdRequired, null, null, firstResponderFriendly, telescreeningAvailable, acceptsInsurance,
				null, freeOrLowCost, null, active);
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
		final String notes,
		final boolean active)
	{
		this(null, name, address, city, state, latitude, longitude, phone, appointmentPhone, email, url, appointmentUrl, hours, typeId, null,
			driveThru, appointmentRequired, acceptsThirdParty, referralRequired, testCriteriaId, null, otherTestCriteria, testsPerDay,
			governmentIdRequired, minimumAge, doctorReferralCriteria, firstResponderFriendly, telescreeningAvailable, acceptsInsurance,
			insuranceProvidersAccepted, freeOrLowCost, notes, active, null, null);
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
		final String notes,
		final boolean active,
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
		this.notes = notes;
		this.active = active;
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
			Objects.equals(notes, v.notes) &&
			(active == v.active) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", address: ").append(address)
			.append(", city: ").append(city)
			.append(", state: ").append(state)
			.append(", latitude: ").append(latitude)
			.append(", longitude: ").append(longitude)
			.append(", phone: ").append(phone)
			.append(", appointmentPhone: ").append(appointmentPhone)
			.append(", email: ").append(email)
			.append(", url: ").append(url)
			.append(", appointmentUrl: ").append(appointmentUrl)
			.append(", hours: ").append(hours)
			.append(", typeId: ").append(typeId)
			.append(", type: ").append(type)
			.append(", driveThru: ").append(driveThru)
			.append(", appointmentRequired: ").append(appointmentRequired)
			.append(", acceptsThirdParty: ").append(acceptsThirdParty)
			.append(", referralRequired: ").append(referralRequired)
			.append(", testCriteriaId: ").append(testCriteriaId)
			.append(", testCriteria: ").append(testCriteria)
			.append(", otherTestCriteria: ").append(otherTestCriteria)
			.append(", testsPerDay: ").append(testsPerDay)
			.append(", governmentIdRequired: ").append(governmentIdRequired)
			.append(", minimumAge: ").append(minimumAge)
			.append(", doctorReferralCriteria: ").append(doctorReferralCriteria)
			.append(", firstResponderFriendly: ").append(firstResponderFriendly)
			.append(", telescreeningAvailable: ").append(telescreeningAvailable)
			.append(", acceptsInsurance: ").append(acceptsInsurance)
			.append(", insuranceProvidersAccepted: ").append(insuranceProvidersAccepted)
			.append(", freeOrLowCost: ").append(freeOrLowCost)
			.append(", notes: ").append(notes)
			.append(", active: ").append(active)
			.append(", createdAt: ").append(createdAt)
			.append(", updatedAt: ").append(updatedAt)
			.append(", meters: ").append(meters)
			.append(", restricted: ").append(restricted)
			.append(" }").toString();
	}
}
