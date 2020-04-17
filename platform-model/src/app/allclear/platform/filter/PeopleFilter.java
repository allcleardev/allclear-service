package app.allclear.platform.filter;

import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.dao.QueryFilter;
import app.allclear.platform.type.Timezone;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for People query.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 22, 2020
*
*******************************************************************************************************************/

public class PeopleFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public String id = null;
	public String name = null;
	public String phone = null;
	public String email = null;
	public Boolean hasEmail = null;
	public String firstName = null;
	public Boolean hasFirstName = null;
	public String lastName = null;
	public Boolean hasLastName = null;
	public Date dob = null;
	public Boolean hasDob = null;
	public Date dobFrom = null;
	public Date dobTo = null;
	public String statusId = null;
	public Boolean hasStatusId = null;
	public String statureId = null;
	public Boolean hasStatureId = null;
	public String sexId = null;
	public Boolean hasSexId = null;
	public String healthWorkerStatusId = null;
	public String timezoneId = null;
	public BigDecimal latitude = null;
	public Boolean hasLatitude = null;
	public BigDecimal latitudeFrom = null;
	public BigDecimal latitudeTo = null;
	public BigDecimal longitude = null;
	public Boolean hasLongitude = null;
	public BigDecimal longitudeFrom = null;
	public BigDecimal longitudeTo = null;
	public String locationName = null;
	public Boolean hasLocationName = null;
	public Boolean alertable = null;
	public Boolean active = null;
	public Boolean hasAuthAt = null;
	public Date authAtFrom = null;
	public Date authAtTo = null;
	public Boolean hasPhoneVerifiedAt = null;
	public Date phoneVerifiedAtFrom = null;
	public Date phoneVerifiedAtTo = null;
	public Boolean hasEmailVerifiedAt = null;
	public Date emailVerifiedAtFrom = null;
	public Date emailVerifiedAtTo = null;
	public Integer alertedOf = null;
	public Boolean hasAlertedOf = null;
	public Integer alertedOfFrom = null;
	public Integer alertedOfTo = null;
	public Boolean hasAlertedAt = null;
	public Date alertedAtFrom = null;
	public Date alertedAtTo = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;
	public List<String> includeConditions = null;
	public List<String> excludeConditions = null;
	public List<String> includeExposures = null;
	public List<String> excludeExposures = null;
	public List<String> includeSymptoms = null;
	public List<String> excludeSymptoms = null;
	public Boolean hasFacilities = null;
	public List<Long> includeFacilities = null;
	public List<Long> excludeFacilities = null;

	// Mutators
	public PeopleFilter withId(final String newValue) { id = newValue; return this; }
	public PeopleFilter withName(final String newValue) { name = newValue; return this; }
	public PeopleFilter withPhone(final String newValue) { phone = newValue; return this; }
	public PeopleFilter withEmail(final String newValue) { email = newValue; return this; }
	public PeopleFilter withHasEmail(final Boolean newValue) { hasEmail = newValue; return this; }
	public PeopleFilter withFirstName(final String newValue) { firstName = newValue; return this; }
	public PeopleFilter withHasFirstName(final Boolean newValue) { hasFirstName = newValue; return this; }
	public PeopleFilter withLastName(final String newValue) { lastName = newValue; return this; }
	public PeopleFilter withHasLastName(final Boolean newValue) { hasLastName = newValue; return this; }
	public PeopleFilter withDob(final Date newValue) { dob = newValue; return this; }
	public PeopleFilter withHasDob(final Boolean newValue) { hasDob = newValue; return this; }
	public PeopleFilter withDobFrom(final Date newValue) { dobFrom = newValue; return this; }
	public PeopleFilter withDobTo(final Date newValue) { dobTo = newValue; return this; }
	public PeopleFilter withStatusId(final String newValue) { statusId = newValue; return this; }
	public PeopleFilter withHasStatusId(final Boolean newValue) { hasStatusId = newValue; return this; }
	public PeopleFilter withStatureId(final String newValue) { statureId = newValue; return this; }
	public PeopleFilter withHasStatureId(final Boolean newValue) { hasStatureId = newValue; return this; }
	public PeopleFilter withSexId(final String newValue) { sexId = newValue; return this; }
	public PeopleFilter withHasSexId(final Boolean newValue) { hasSexId = newValue; return this; }
	public PeopleFilter withHealthWorkerStatusId(final String newValue) { healthWorkerStatusId = newValue; return this; }
	public PeopleFilter withTimezoneId(final String newValue) { timezoneId = newValue; return this; }
	public PeopleFilter withLatitude(final BigDecimal newValue) { latitude = newValue; return this; }
	public PeopleFilter withHasLatitude(final Boolean newValue) { hasLatitude = newValue; return this; }
	public PeopleFilter withLatitudeFrom(final BigDecimal newValue) { latitudeFrom = newValue; return this; }
	public PeopleFilter withLatitudeTo(final BigDecimal newValue) { latitudeTo = newValue; return this; }
	public PeopleFilter withLongitude(final BigDecimal newValue) { longitude = newValue; return this; }
	public PeopleFilter withHasLongitude(final Boolean newValue) { hasLongitude = newValue; return this; }
	public PeopleFilter withLongitudeFrom(final BigDecimal newValue) { longitudeFrom = newValue; return this; }
	public PeopleFilter withLongitudeTo(final BigDecimal newValue) { longitudeTo = newValue; return this; }
	public PeopleFilter withLocationName(final String newValue) { locationName = newValue; return this; }
	public PeopleFilter withHasLocationName(final Boolean newValue) { hasLocationName = newValue; return this; }
	public PeopleFilter withAlertable(final Boolean newValue) { alertable = newValue; return this; }
	public PeopleFilter withActive(final Boolean newValue) { active = newValue; return this; }
	public PeopleFilter withHasAuthAt(final Boolean newValue) { hasAuthAt = newValue; return this; }
	public PeopleFilter withAuthAtFrom(final Date newValue) { authAtFrom = newValue; return this; }
	public PeopleFilter withAuthAtTo(final Date newValue) { authAtTo = newValue; return this; }
	public PeopleFilter withHasPhoneVerifiedAt(final Boolean newValue) { hasPhoneVerifiedAt = newValue; return this; }
	public PeopleFilter withPhoneVerifiedAtFrom(final Date newValue) { phoneVerifiedAtFrom = newValue; return this; }
	public PeopleFilter withPhoneVerifiedAtTo(final Date newValue) { phoneVerifiedAtTo = newValue; return this; }
	public PeopleFilter withHasEmailVerifiedAt(final Boolean newValue) { hasEmailVerifiedAt = newValue; return this; }
	public PeopleFilter withEmailVerifiedAtFrom(final Date newValue) { emailVerifiedAtFrom = newValue; return this; }
	public PeopleFilter withEmailVerifiedAtTo(final Date newValue) { emailVerifiedAtTo = newValue; return this; }
	public PeopleFilter withAlertedOf(final Integer newValue) { alertedOf = newValue; return this; }
	public PeopleFilter withHasAlertedOf(final Boolean newValue) { hasAlertedOf = newValue; return this; }
	public PeopleFilter withAlertedOfFrom(final Integer newValue) { alertedOfFrom = newValue; return this; }
	public PeopleFilter withAlertedOfTo(final Integer newValue) { alertedOfTo = newValue; return this; }
	public PeopleFilter withHasAlertedAt(final Boolean newValue) { hasAlertedAt = newValue; return this; }
	public PeopleFilter withAlertedAtFrom(final Date newValue) { alertedAtFrom = newValue; return this; }
	public PeopleFilter withAlertedAtTo(final Date newValue) { alertedAtTo = newValue; return this; }
	public PeopleFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public PeopleFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }
	public PeopleFilter withUpdatedAtFrom(final Date newValue) { updatedAtFrom = newValue; return this; }
	public PeopleFilter withUpdatedAtTo(final Date newValue) { updatedAtTo = newValue; return this; }
	public PeopleFilter withIncludeConditions(final List<String> newValues) { includeConditions = newValues; return this; }
	public PeopleFilter withIncludeConditions(final String... newValues) { return withIncludeConditions(Arrays.asList(newValues)); }
	public PeopleFilter withExcludeConditions(final List<String> newValues) { excludeConditions = newValues; return this; }
	public PeopleFilter withExcludeConditions(final String... newValues) { return withExcludeConditions(Arrays.asList(newValues)); }
	public PeopleFilter withIncludeExposures(final List<String> newValues) { includeExposures = newValues; return this; }
	public PeopleFilter withIncludeExposures(final String... newValues) { return withIncludeExposures(Arrays.asList(newValues)); }
	public PeopleFilter withExcludeExposures(final List<String> newValues) { excludeExposures = newValues; return this; }
	public PeopleFilter withExcludeExposures(final String... newValues) { return withExcludeExposures(Arrays.asList(newValues)); }
	public PeopleFilter withIncludeSymptoms(final List<String> newValues) { includeSymptoms = newValues; return this; }
	public PeopleFilter withIncludeSymptoms(final String... newValues) { return withIncludeSymptoms(Arrays.asList(newValues)); }
	public PeopleFilter withExcludeSymptoms(final List<String> newValues) { excludeSymptoms = newValues; return this; }
	public PeopleFilter withExcludeSymptoms(final String... newValues) { return withExcludeSymptoms(Arrays.asList(newValues)); }
	public PeopleFilter withHasFacilities(final Boolean newValue) { hasFacilities = newValue; return this; }
	public PeopleFilter withIncludeFacilities(final List<Long> newValues) { includeFacilities = newValues; return this; }
	public PeopleFilter withIncludeFacilities(final Long... newValues) { return withIncludeFacilities(Arrays.asList(newValues)); }
	public PeopleFilter withExcludeFacilities(final List<Long> newValues) { excludeFacilities = newValues; return this; }
	public PeopleFilter withExcludeFacilities(final Long... newValues) { return withExcludeFacilities(Arrays.asList(newValues)); }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public PeopleFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public PeopleFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public PeopleFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public PeopleFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param name represents the "name" field.
		@param phone represents the "phone" field.
		@param email represents the "email" field.
		@param firstName represents the "first_name" field.
		@param lastName represents the "last_name" field.
		@param dob represents the "dob" field.
		@param dobFrom represents the "dob" field - lower boundary.
		@param dobTo represents the "dob" field - upper boundary.
		@param statusId represents the "status_id" field.
		@param statureId represents the "stature_id" field.
		@param sexId represents the "sex_id" field.
		@param healthWorkerStatusId represents the "health_worker_status_id" field.
		@param latitude represents the "latitude" field.
		@param latitudeFrom represents the "latitude" field - lower boundary.
		@param latitudeTo represents the "latitude" field - upper boundary.
		@param longitude represents the "longitude" field.
		@param longitudeFrom represents the "longitude" field - lower boundary.
		@param longitudeTo represents the "longitude" field - upper boundary.
		@param locationName represents the "location_name" field.
		@param alertable represents the "alertable" field.
		@param active represents the "active" field.
		@param authAtFrom represents the "auth_at" field - lower boundary.
		@param authAtTo represents the "auth_at" field - upper boundary.
		@param phoneVerifiedAtFrom represents the "phone_verified_at" field - lower boundary.
		@param phoneVerifiedAtTo represents the "phone_verified_at" field - upper boundary.
		@param emailVerifiedAtFrom represents the "email_verified_at" field - lower boundary.
		@param emailVerifiedAtTo represents the "email_verified_at" field - upper boundary.
		@param alertedOf represents the "alerted_of" field.
		@param alertedOfFrom represents the "alerted_of" field - lower boundary.
		@param alertedOfTo represents the "alerted_of" field - upper boundary.
		@param alertedAtFrom represents the "alerted_at" field - lower boundary.
		@param alertedAtTo represents the "alerted_at" field - upper boundary.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
		@param updatedAtFrom represents the "updated_at" field - lower boundary.
		@param updatedAtTo represents the "updated_at" field - upper boundary.
	*/
	public PeopleFilter(final String id,
		final String name,
		final String phone,
		final String email,
		final String firstName,
		final String lastName,
		final Date dob,
		final Date dobFrom,
		final Date dobTo,
		final String statusId,
		final String statureId,
		final String sexId,
		final String healthWorkerStatusId,
		final BigDecimal latitude,
		final BigDecimal latitudeFrom,
		final BigDecimal latitudeTo,
		final BigDecimal longitude,
		final BigDecimal longitudeFrom,
		final BigDecimal longitudeTo,
		final String locationName,
		final Boolean alertable,
		final Boolean active,
		final Date authAtFrom,
		final Date authAtTo,
		final Date phoneVerifiedAtFrom,
		final Date phoneVerifiedAtTo,
		final Date emailVerifiedAtFrom,
		final Date emailVerifiedAtTo,
		final Integer alertedOf,
		final Integer alertedOfFrom,
		final Integer alertedOfTo,
		final Date alertedAtFrom,
		final Date alertedAtTo,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.dobFrom = dobFrom;
		this.dobTo = dobTo;
		this.statusId = statusId;
		this.statureId = statureId;
		this.sexId = sexId;
		this.healthWorkerStatusId = healthWorkerStatusId;
		this.latitude = latitude;
		this.latitudeFrom = latitudeFrom;
		this.latitudeTo = latitudeTo;
		this.longitude = longitude;
		this.longitudeFrom = longitudeFrom;
		this.longitudeTo = longitudeTo;
		this.locationName = locationName;
		this.alertable = alertable;
		this.active = active;
		this.authAtFrom = authAtFrom;
		this.authAtTo = authAtTo;
		this.phoneVerifiedAtFrom = phoneVerifiedAtFrom;
		this.phoneVerifiedAtTo = phoneVerifiedAtTo;
		this.emailVerifiedAtFrom = emailVerifiedAtFrom;
		this.emailVerifiedAtTo = emailVerifiedAtTo;
		this.alertedOf = alertedOf;
		this.alertedOfFrom = alertedOfFrom;
		this.alertedOfTo = alertedOfTo;
		this.alertedAtFrom = alertedAtFrom;
		this.alertedAtTo = alertedAtTo;
		this.createdAtFrom = createdAtFrom;
		this.createdAtTo = createdAtTo;
		this.updatedAtFrom = updatedAtFrom;
		this.updatedAtTo = updatedAtTo;
	}

	/**************************************************************************
	*
	*	Helper methods
	*
	**************************************************************************/

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public PeopleFilter clean()
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
		timezoneId = StringUtils.trimToNull(timezoneId);
		locationName = StringUtils.trimToNull(locationName);

		if (null != timezoneId)	// Apply timezone value to their longitudinal constraints.
		{
			var zone = Timezone.get(timezoneId);
			if (null != zone) withLongitudeFrom(zone.longitudeFrom).withLongitudeTo(zone.longitudeTo);
		}

		return this;
	}

	/**************************************************************************
	*
	*	Object methods
	*
	**************************************************************************/

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", phone: ").append(phone)
			.append(", email: ").append(email)
			.append(", hasEmail: ").append(hasEmail)
			.append(", firstName: ").append(firstName)
			.append(", hasFirstName: ").append(hasFirstName)
			.append(", lastName: ").append(lastName)
			.append(", hasLastName: ").append(hasLastName)
			.append(", dob: ").append(dob)
			.append(", hasDob: ").append(hasDob)
			.append(", dobFrom: ").append(dobFrom)
			.append(", dobTo: ").append(dobTo)
			.append(", statusId: ").append(statusId)
			.append(", hasStatusId: ").append(hasStatusId)
			.append(", statureId: ").append(statureId)
			.append(", hasStatureId: ").append(hasStatureId)
			.append(", sexId: ").append(sexId)
			.append(", hasSexId: ").append(hasSexId)
			.append(", healthWorkerStatusId: ").append(healthWorkerStatusId)
			.append(", timezoneId: ").append(timezoneId)
			.append(", latitude: ").append(latitude)
			.append(", hasLatitude: ").append(hasLatitude)
			.append(", latitudeFrom: ").append(latitudeFrom)
			.append(", latitudeTo: ").append(latitudeTo)
			.append(", longitude: ").append(longitude)
			.append(", hasLongitude: ").append(hasLongitude)
			.append(", longitudeFrom: ").append(longitudeFrom)
			.append(", longitudeTo: ").append(longitudeTo)
			.append(", locationName: ").append(locationName)
			.append(", hasLocationName: ").append(hasLocationName)
			.append(", alertable: ").append(alertable)
			.append(", active: ").append(active)
			.append(", hasAuthAt: ").append(hasAuthAt)
			.append(", authAtFrom: ").append(authAtFrom)
			.append(", authAtTo: ").append(authAtTo)
			.append(", hasPhoneVerifiedAt: ").append(hasPhoneVerifiedAt)
			.append(", phoneVerifiedAtFrom: ").append(phoneVerifiedAtFrom)
			.append(", phoneVerifiedAtTo: ").append(phoneVerifiedAtTo)
			.append(", hasEmailVerifiedAt: ").append(hasEmailVerifiedAt)
			.append(", emailVerifiedAtFrom: ").append(emailVerifiedAtFrom)
			.append(", emailVerifiedAtTo: ").append(emailVerifiedAtTo)
			.append(", alertedOf: ").append(alertedOf)
			.append(", hasAlertedOf: ").append(hasAlertedOf)
			.append(", alertedOfFrom: ").append(alertedOfFrom)
			.append(", alertedOfTo: ").append(alertedOfTo)
			.append(", hasAlertedAt: ").append(hasAlertedAt)
			.append(", alertedAtFrom: ").append(alertedAtFrom)
			.append(", alertedAtTo: ").append(alertedAtTo)
			.append(", createdAtFrom: ").append(createdAtFrom)
			.append(", createdAtTo: ").append(createdAtTo)
			.append(", updatedAtFrom: ").append(updatedAtFrom)
			.append(", updatedAtTo: ").append(updatedAtTo)
			.append(", includeConditions: ").append(includeConditions)
			.append(", excludeConditions: ").append(excludeConditions)
			.append(", includeExposures: ").append(includeExposures)
			.append(", excludeExposures: ").append(excludeExposures)
			.append(", includeSymptoms: ").append(includeSymptoms)
			.append(", excludeSymptoms: ").append(excludeSymptoms)
			.append(", hasFacilities: ").append(hasFacilities)
			.append(", includeFacilities: ").append(includeFacilities)
			.append(", excludeFacilities: ").append(excludeFacilities)
			.append(" }").toString();
	}
}
