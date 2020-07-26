package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.common.ObjectUtils;

/**********************************************************************************
*
*	Value object class that represents the patient table.
*
*	@author smalleyd
*	@version 1.1.111
*	@since July 18, 2020
*
**********************************************************************************/

public class PatientValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "patient";
	public static final int MAX_LEN_PERSON_ID = 10;

	// Members
	public Long id = null;
	public Long facilityId = null;
	public String facilityName = null;
	public String personId = null;
	public String personName = null;
	public boolean alertable = false;
	public Date enrolledAt = null;
	public Date rejectedAt = null;
	public Date createdAt = null;
	public Date updatedAt = null;

	// Mutators
	public PatientValue withId(final Long newValue) { id = newValue; return this; }
	public PatientValue withFacilityId(final Long newValue) { facilityId = newValue; return this; }
	public PatientValue withFacilityName(final String newValue) { facilityName = newValue; return this; }
	public PatientValue withPersonId(final String newValue) { personId = newValue; return this; }
	public PatientValue withPersonName(final String newValue) { personName = newValue; return this; }
	public PatientValue withAlertable(final boolean newValue) { alertable = newValue; return this; }
	public PatientValue withEnrolledAt(final Date newValue) { enrolledAt = newValue; return this; }
	public PatientValue withRejectedAt(final Date newValue) { rejectedAt = newValue; return this; }
	public PatientValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public PatientValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }
	public PatientValue denormalize(final String facilityName, final String personName)
	{
		this.facilityName = facilityName;
		this.personName = personName;

		return this;
	}

	public PatientValue() {}

	public PatientValue(
		final Long facilityId,
		final String personId,
		final boolean alertable,
		final Date enrolledAt,
		final Date rejectedAt)
	{
		this(null, facilityId, personId, alertable, enrolledAt, rejectedAt, null, null);
	}

	public PatientValue(final Long id,
		final Long facilityId,
		final String personId,
		final boolean alertable,
		final Date enrolledAt,
		final Date rejectedAt,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.facilityId = facilityId;
		this.personId = personId;
		this.alertable = alertable;
		this.enrolledAt = enrolledAt;
		this.rejectedAt = rejectedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		personId = StringUtils.trimToNull(personId);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof PatientValue)) return false;

		var v = (PatientValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(facilityId, v.facilityId) &&
			Objects.equals(facilityName, v.facilityName) &&
			Objects.equals(personId, v.personId) &&
			Objects.equals(personName, v.personName) &&
			(alertable == v.alertable) &&
			((enrolledAt == v.enrolledAt) || DateUtils.truncatedEquals(enrolledAt, v.enrolledAt, Calendar.SECOND)) &&
			((rejectedAt == v.rejectedAt) || DateUtils.truncatedEquals(rejectedAt, v.rejectedAt, Calendar.SECOND)) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(id);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
