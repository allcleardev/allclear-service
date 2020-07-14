package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.platform.type.TestType;

/**********************************************************************************
*
*	Value object class that represents the tests table.
*
*	@author smalleyd
*	@version 1.0.44
*	@since April 4, 2020
*
**********************************************************************************/

public class TestsValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "tests";
	public static final int MAX_LEN_PERSON_ID = 10;
	public static final int MAX_LEN_TYPE_ID = 2;
	public static final int MAX_LEN_REMOTE_ID = 64;
	public static final int MAX_LEN_NOTES = 65535;

	// Members
	public Long id = null;
	public String personId = null;
	public String personName = null;
	public String typeId = null;
	public TestType type = null;
	public Date takenOn = null;
	public Long facilityId = null;
	public String facilityName = null;
	public String remoteId = null;
	public boolean positive;
	public String notes = null;
	public Date receivedAt = null;
	public Date createdAt = null;
	public Date updatedAt = null;

	// Mutators
	public TestsValue withId(final Long newValue) { id = newValue; return this; }
	public TestsValue withPersonId(final String newValue) { personId = newValue; return this; }
	public TestsValue withPersonName(final String newValue) { personName = newValue; return this; }
	public TestsValue withTypeId(final String newValue) { typeId = newValue; return this; }
	public TestsValue withType(final TestType newValue) { type = newValue; return this; }
	public TestsValue withTakenOn(final Date newValue) { takenOn = newValue; return this; }
	public TestsValue withFacilityId(final Long newValue) { facilityId = newValue; return this; }
	public TestsValue withFacilityName(final String newValue) { facilityName = newValue; return this; }
	public TestsValue withRemoteId(final String newValue) { remoteId = newValue; return this; }
	public TestsValue withPositive(final boolean newValue) { positive = newValue; return this; }
	public TestsValue withNotes(final String newValue) { notes = newValue; return this; }
	public TestsValue withReceivedAt(final Date newValue) { receivedAt = newValue; return this; }
	public TestsValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public TestsValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }

	public TestsValue() {}

	public TestsValue(final String personId, final String typeId, final Date takenOn, final Long facilityId, boolean positive, final String notes)
	{
		this(null, personId, null, typeId, null, takenOn, facilityId, null, null, positive, notes, null, null, null);
	}

	public TestsValue(final Long id,
		final String personId,
		final String personName,
		final String typeId,
		final TestType type,
		final Date takenOn,
		final Long facilityId,
		final String facilityName,
		final String remoteId,
		final boolean positive,
		final String notes,
		final Date receivedAt,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.personId = personId;
		this.personName = personName;
		this.typeId = typeId;
		this.type = type;
		this.takenOn = takenOn;
		this.facilityId = facilityId;
		this.facilityName = facilityName;
		this.remoteId = remoteId;
		this.positive = positive;
		this.notes = notes;
		this.receivedAt = receivedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		personId = StringUtils.trimToNull(personId);
		typeId = StringUtils.trimToNull(typeId);
		notes = StringUtils.trimToNull(notes);
		remoteId = StringUtils.trimToNull(remoteId);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof TestsValue)) return false;

		var v = (TestsValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(personId, v.personId) &&
			Objects.equals(personName, v.personName) &&
			Objects.equals(typeId, v.typeId) &&
			DateUtils.truncatedEquals(takenOn, v.takenOn, Calendar.SECOND) &&
			Objects.equals(facilityId, v.facilityId) &&
			Objects.equals(facilityName, v.facilityName) &&
			Objects.equals(remoteId, v.remoteId) &&
			(positive == v.positive) &&
			Objects.equals(notes, v.notes) &&
			((receivedAt == v.receivedAt) || DateUtils.truncatedEquals(receivedAt, v.receivedAt, Calendar.SECOND)) &&
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
