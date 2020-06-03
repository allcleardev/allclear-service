package app.allclear.platform.value;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.value.NamedValue;
import app.allclear.platform.type.Experience;

/**********************************************************************************
*
*	Value object class that represents the experiences table.
*
*	@author smalleyd
*	@version 1.1.80
*	@since June 2, 2020
*
**********************************************************************************/

public class ExperiencesValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "experiences";
	public static final int MAX_LEN_PERSON_ID = 10;

	// Members
	public Long id = null;
	public String personId = null;
	public String personName = null;
	public Long facilityId = null;
	public String facilityName = null;
	public boolean positive;
	public Date createdAt = null;
	public Date updatedAt = null;
	public List<NamedValue> tags;

	// Mutators
	public ExperiencesValue withId(final Long newValue) { id = newValue; return this; }
	public ExperiencesValue withPersonId(final String newValue) { personId = newValue; return this; }
	public ExperiencesValue withPersonName(final String newValue) { personName = newValue; return this; }
	public ExperiencesValue withFacilityId(final Long newValue) { facilityId = newValue; return this; }
	public ExperiencesValue withFacilityName(final String newValue) { facilityName = newValue; return this; }
	public ExperiencesValue withPositive(final boolean newValue) { positive = newValue; return this; }
	public ExperiencesValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public ExperiencesValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }
	public ExperiencesValue withTags(final List<NamedValue> newValues) { tags = newValues; return this; }
	public ExperiencesValue withTags(final Experience... newValues) { return withTags(Arrays.stream(newValues).map(v -> v.named()).collect(toList())); }
	public ExperiencesValue emptyTags() { return withTags(List.of()); }
	public ExperiencesValue nullTags() { tags = null; return this; }
	public ExperiencesValue denormalize(final String personName, final String facilityName)
	{
		this.personName = personName;
		this.facilityName = facilityName;

		return this;
	}

	public ExperiencesValue() {}

	public ExperiencesValue(
		final Long facilityId,
		final boolean positive)
	{
		this(null, facilityId, positive);
	}

	public ExperiencesValue(
		final String personId,
		final Long facilityId,
		final boolean positive)
	{
		this(null, personId, facilityId, positive, null, null);
	}

	public ExperiencesValue(final Long id,
		final String personId,
		final Long facilityId,
		final boolean positive,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.personId = personId;
		this.facilityId = facilityId;
		this.positive = positive;
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
		if (!(o instanceof ExperiencesValue)) return false;

		var v = (ExperiencesValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(personId, v.personId) &&
			Objects.equals(personName, v.personName) &&
			Objects.equals(facilityId, v.facilityId) &&
			Objects.equals(facilityName, v.facilityName) &&
			(positive == v.positive) &&
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
