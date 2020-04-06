package app.allclear.platform.value;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.*;

/**********************************************************************************
*
*	Value object class that represents the people table.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 22, 2020
*
**********************************************************************************/

public class PeopleValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "people";
	public static final int MAX_LEN_ID = 10;
	public static final int MAX_LEN_NAME = 64;
	public static final int MAX_LEN_PHONE = 32;
	public static final int MAX_LEN_EMAIL = 128;
	public static final int MAX_LEN_FIRST_NAME = 32;
	public static final int MAX_LEN_LAST_NAME = 32;
	public static final int MAX_LEN_STATUS_ID = 1;
	public static final int MAX_LEN_STATURE_ID = 1;
	public static final int MAX_LEN_SEX_ID = 1;
	public static final int MAX_LEN_HEALTH_WORKER_STATUS_ID = 1;
	public static final int MAX_LEN_CONDITION_ID = 2;

	// Members
	public String id = null;
	public String name = null;
	public String phone = null;
	public String email = null;
	public String firstName = null;
	public String lastName = null;
	public Date dob = null;
	public String statusId = null;
	public PeopleStatus status = null;
	public String statureId = null;
	public PeopleStature stature = null;
	public String sexId = null;
	public Sex sex = null;
	public String healthWorkerStatusId = null;
	public HealthWorkerStatus healthWorkerStatus = null;
	public BigDecimal latitude = null;
	public BigDecimal longitude = null;
	public boolean alertable;
	public boolean active;
	public Date authAt = null;
	public Date phoneVerifiedAt = null;
	public Date emailVerifiedAt = null;
	public Date createdAt = null;
	public Date updatedAt = null;
	public List<CreatedValue> conditions = null;
	public List<CreatedValue> exposures = null;
	public List<CreatedValue> symptoms = null;

	// Mutators
	public PeopleValue withId(final String newValue) { id = newValue; return this; }
	public PeopleValue withName(final String newValue) { name = newValue; return this; }
	public PeopleValue withPhone(final String newValue) { phone = newValue; return this; }
	public PeopleValue withEmail(final String newValue) { email = newValue; return this; }
	public PeopleValue withFirstName(final String newValue) { firstName = newValue; return this; }
	public PeopleValue withLastName(final String newValue) { lastName = newValue; return this; }
	public PeopleValue withDob(final Date newValue) { dob = newValue; return this; }
	public PeopleValue withStatusId(final String newValue) { statusId = newValue; return this; }
	public PeopleValue withStatus(final PeopleStatus newValue) { status = newValue; return this; }
	public PeopleValue withStatureId(final String newValue) { statureId = newValue; return this; }
	public PeopleValue withStature(final PeopleStature newValue) { stature = newValue; return this; }
	public PeopleValue withSexId(final String newValue) { sexId = newValue; return this; }
	public PeopleValue withSex(final Sex newValue) { sex = newValue; return this; }
	public PeopleValue withHealthWorkerStatusId(final String newValue) { healthWorkerStatusId = newValue; return this; }
	public PeopleValue withHealthWorkerStatus(final HealthWorkerStatus newValue) { healthWorkerStatus = newValue; return this; }
	public PeopleValue withLatitude(final BigDecimal newValue) { latitude = newValue; return this; }
	public PeopleValue withLongitude(final BigDecimal newValue) { longitude = newValue; return this; }
	public PeopleValue withAlertable(final boolean newValue) { alertable = newValue; return this; }
	public PeopleValue withActive(final boolean newValue) { active = newValue; return this; }
	public PeopleValue withAuthAt(final Date newValue) { authAt = newValue; return this; }
	public PeopleValue withPhoneVerifiedAt(final Date newValue) { phoneVerifiedAt = newValue; return this; }
	public PeopleValue withEmailVerifiedAt(final Date newValue) { emailVerifiedAt = newValue; return this; }
	public PeopleValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public PeopleValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }
	public PeopleValue withConditions(final List<CreatedValue> newValues) { conditions = newValues; return this; }
	public PeopleValue emptyConditions() { return withConditions(List.of()); }
	public PeopleValue nullConditions() { conditions = null; return this; }
	public PeopleValue withConditions(final Condition... newValues)
	{
		return withConditions(Arrays.asList(newValues).stream().map(v -> v.created()).collect(toList()));
	}
	public PeopleValue withExposures(final List<CreatedValue> newValues) { exposures = newValues; return this; }
	public PeopleValue emptyExposures() { return withExposures(List.of()); }
	public PeopleValue nullExposures() { exposures = null; return this; }
	public PeopleValue withExposures(final Exposure... newValues)
	{
		return withExposures(Arrays.asList(newValues).stream().map(v -> v.created()).collect(toList()));
	}
	public PeopleValue withSymptoms(final List<CreatedValue> newValues) { symptoms = newValues; return this; }
	public PeopleValue emptySymptoms() { return withSymptoms(List.of()); }
	public PeopleValue nullSymptoms() { symptoms = null; return this; }
	public PeopleValue withSymptoms(final Symptom... newValues)
	{
		return withSymptoms(Arrays.asList(newValues).stream().map(v -> v.created()).collect(toList()));
	}

	public PeopleValue registered() { return withActive(true).withAuthAt(new Date()); }
	public PeopleValue registeredByPhone() { return registered().withPhoneVerifiedAt(authAt).withEmailVerifiedAt(null); }
	public Date initDates()
	{
		return this.createdAt = this.updatedAt = new Date();
	}

	public PeopleValue() {}

	public PeopleValue(
		final String name,
		final String phone,
		final boolean active)
	{
		this(name, phone, null, null, null, null, null, null, null, null, null, null, false, active);
	}

	public PeopleValue(
		final String name,
		final String phone,
		final String email,
		final String firstName,
		final String lastName,
		final Date dob,
		final String statusId,
		final String statureId,
		final String sexId,
		final String healthWorkerStatusId,
		final BigDecimal latitude,
		final BigDecimal longitude,
		final boolean alertable,
		final boolean active)
	{
		this(null, name, phone, email, firstName, lastName, dob, statusId, null, statureId, null, sexId, null, healthWorkerStatusId, null, latitude, longitude, alertable, active, null, null, null, null, null);
	}

	public PeopleValue(final String id,
		final String name,
		final String phone,
		final String email,
		final String firstName,
		final String lastName,
		final Date dob,
		final String statusId,
		final PeopleStatus status,
		final String statureId,
		final PeopleStature stature,
		final String sexId,
		final Sex sex,
		final String healthWorkerStatusId,
		final HealthWorkerStatus healthWorkerStatus,
		final BigDecimal latitude,
		final BigDecimal longitude,
		final boolean alertable,
		final boolean active,
		final Date authAt,
		final Date phoneVerifiedAt,
		final Date emailVerifiedAt,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.statusId = statusId;
		this.status = status;
		this.statureId = statureId;
		this.stature = stature;
		this.sexId = sexId;
		this.sex = sex;
		this.healthWorkerStatusId = healthWorkerStatusId;
		this.healthWorkerStatus = healthWorkerStatus;
		this.latitude = latitude;
		this.longitude = longitude;
		this.alertable = alertable;
		this.active = active;
		this.authAt = authAt;
		this.phoneVerifiedAt = phoneVerifiedAt;
		this.emailVerifiedAt = emailVerifiedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		id = StringUtils.trimToNull(id);
		name = StringUtils.trimToNull(name);
		phone = StringUtils.trimToNull(phone);
		email = StringUtils.trimToNull(email);
		firstName = StringUtils.trimToNull(firstName);
		lastName = StringUtils.trimToNull(lastName);
		statusId = StringUtils.trimToNull(statusId);
		statureId = StringUtils.trimToNull(statureId);
		sexId = StringUtils.trimToNull(sexId);
		healthWorkerStatusId = StringUtils.trimToNull(healthWorkerStatusId);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof PeopleValue)) return false;

		var v = (PeopleValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(name, v.name) &&
			Objects.equals(phone, v.phone) &&
			Objects.equals(email, v.email) &&
			Objects.equals(firstName, v.firstName) &&
			Objects.equals(lastName, v.lastName) &&
			((dob == v.dob) || DateUtils.truncatedEquals(dob, v.dob, Calendar.DATE)) &&
			Objects.equals(statusId, v.statusId) &&
			Objects.equals(statureId, v.statureId) &&
			Objects.equals(sexId, v.sexId) &&
			Objects.equals(healthWorkerStatusId, v.healthWorkerStatusId) &&
			((latitude == v.latitude) || (0 == Objects.compare(latitude, v.latitude, (a, b) -> a.compareTo(b)))) &&
			((longitude == v.longitude) || (0 == Objects.compare(longitude, v.longitude, (a, b) -> a.compareTo(b)))) &&
			(alertable == v.alertable) &&
			(active == v.active) &&
			((authAt == v.authAt) || DateUtils.truncatedEquals(authAt, v.authAt, Calendar.SECOND)) &&
			((phoneVerifiedAt == v.phoneVerifiedAt) || DateUtils.truncatedEquals(phoneVerifiedAt, v.phoneVerifiedAt, Calendar.SECOND)) &&
			((emailVerifiedAt == v.emailVerifiedAt) || DateUtils.truncatedEquals(emailVerifiedAt, v.emailVerifiedAt, Calendar.SECOND)) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", phone: ").append(phone)
			.append(", email: ").append(email)
			.append(", firstName: ").append(firstName)
			.append(", lastName: ").append(lastName)
			.append(", dob: ").append(dob)
			.append(", statusId: ").append(statusId)
			.append(", status: ").append(status)
			.append(", statureId: ").append(statureId)
			.append(", stature: ").append(stature)
			.append(", sexId: ").append(sexId)
			.append(", sex: ").append(sex)
			.append(", healthWorkerStatusId: ").append(healthWorkerStatusId)
			.append(", healthWorkerStatus: ").append(healthWorkerStatus)
			.append(", latitude: ").append(latitude)
			.append(", longitude: ").append(longitude)
			.append(", alertable: ").append(alertable)
			.append(", active: ").append(active)
			.append(", authAt: ").append(authAt)
			.append(", phoneVerifiedAt: ").append(phoneVerifiedAt)
			.append(", emailVerifiedAt: ").append(emailVerifiedAt)
			.append(", createdAt: ").append(createdAt)
			.append(", updatedAt: ").append(updatedAt)
			.append(", conditions: ").append(conditions)
			.append(", exposures: ").append(exposures)
			.append(", symptoms: ").append(symptoms)
			.append(" }").toString();
	}

	/** Returns true iff the user is symptomatic **/
	public boolean isSymptomatic()
	{
		return this.symptoms.contains(Symptom.FEVER);
	}

	/** Returns true iff the user qualifies for CDC Priority 3 **/
	public boolean isCdcPriority3()
	{
		boolean isHealthWorker = this.healthWorkerStatus.equals(HealthWorkerStatus.HEALTH_WORKER);
		return isHealthWorker || this.isSymptomatic()
	}

	/** Returns true iff the user matches the testing criteria for a facility **/
	public boolean matchesCriteria(FacilityValue facility)
	{
		return facility.testCriteria.equals(TestCriteria.CDC_CRITERIA) && this.isCdcPriority3();
	}
}
