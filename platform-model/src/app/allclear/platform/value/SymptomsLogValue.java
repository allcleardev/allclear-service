package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.platform.type.Symptom;

/**********************************************************************************
*
*	Value object class that represents the symptoms_log table.
*
*	@author smalleyd
*	@version 1.0.80
*	@since April 8, 2020
*
**********************************************************************************/

public class SymptomsLogValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "symptoms_log";
	public static final int MAX_LEN_PERSON_ID = 10;
	public static final int MAX_LEN_SYMPTOM_ID = 2;

	// Members
	public Long id = null;
	public String personId = null;
	public String personName = null;
	public String symptomId = null;
	public Symptom symptom = null;
	public Date startedAt = null;
	public Date endedAt = null;

	// Mutators
	public SymptomsLogValue withId(final Long newValue) { id = newValue; return this; }
	public SymptomsLogValue withPersonId(final String newValue) { personId = newValue; return this; }
	public SymptomsLogValue withPersonName(final String newValue) { personName = newValue; return this; }
	public SymptomsLogValue withSymptomId(final String newValue) { symptomId = newValue; return this; }
	public SymptomsLogValue withSymptom(final Symptom newValue) { symptom = newValue; return this; }
	public SymptomsLogValue withStartedAt(final Date newValue) { startedAt = newValue; return this; }
	public SymptomsLogValue withEndedAt(final Date newValue) { endedAt = newValue; return this; }

	public SymptomsLogValue() {}

	public SymptomsLogValue(
		final String personId,
		final String symptomId,
		final Date startedAt,
		final Date endedAt)
	{
		this(null, personId, null, symptomId, null, startedAt, endedAt);
	}

	public SymptomsLogValue(final Long id,
		final String personId,
		final String personName,
		final String symptomId,
		final Symptom symptom,
		final Date startedAt,
		final Date endedAt)
	{
		this.id = id;
		this.personId = personId;
		this.personName = personName;
		this.symptomId = symptomId;
		this.symptom = symptom;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		personId = StringUtils.trimToNull(personId);
		symptomId = StringUtils.trimToNull(symptomId);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof SymptomsLogValue)) return false;

		var v = (SymptomsLogValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(personId, v.personId) &&
			Objects.equals(personName, v.personName) &&
			Objects.equals(symptomId, v.symptomId) &&
			DateUtils.truncatedEquals(startedAt, v.startedAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(endedAt, v.endedAt, Calendar.SECOND);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(id);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", personId: ").append(personId)
			.append(", personName: ").append(personName)
			.append(", symptomId: ").append(symptomId)
			.append(", symptom: ").append(symptom)
			.append(", startedAt: ").append(startedAt)
			.append(", endedAt: ").append(endedAt)
			.append(" }").toString();
	}
}
