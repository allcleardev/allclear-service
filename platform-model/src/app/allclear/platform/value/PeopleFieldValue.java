package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.platform.type.Visibility;

/**********************************************************************************
*
*	Value object class that represents the people_field table.
*
*	@author smalleyd
*	@version 1.1.36
*	@since May 4, 2020
*
**********************************************************************************/

public class PeopleFieldValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "people_field";
	public static final int MAX_LEN_ID = 10;
	public static final int MAX_LEN_VISIBILITY_HEALTH_WORKER_STATUS_ID = 1;
	public static final int MAX_LEN_VISIBILITY_CONDITIONS = 1;
	public static final int MAX_LEN_VISIBILITY_EXPOSURES = 1;
	public static final int MAX_LEN_VISIBILITY_SYMPTOMS = 1;

	// Members
	public String id = null;
	public String visibilityHealthWorkerStatusId = null;
	public String visibilityConditions = null;
	public String visibilityExposures = null;
	public String visibilitySymptoms = null;
	public String name = null;
	public Visibility visibilityHealthWorkerStatus = null;
	public Visibility visibilityCondition = null;
	public Visibility visibilityExposure = null;
	public Visibility visibilitySymptom = null;
	public Date updatedAt = null;

	// Mutators
	public PeopleFieldValue withId(final String newValue) { id = newValue; return this; }
	public PeopleFieldValue withVisibilityHealthWorkerStatusId(final String newValue) { visibilityHealthWorkerStatusId = newValue; return this; }
	public PeopleFieldValue withVisibilityConditions(final String newValue) { visibilityConditions = newValue; return this; }
	public PeopleFieldValue withVisibilityExposures(final String newValue) { visibilityExposures = newValue; return this; }
	public PeopleFieldValue withVisibilitySymptoms(final String newValue) { visibilitySymptoms = newValue; return this; }
	public PeopleFieldValue withName(final String newValue) { name = newValue; return this; }
	public PeopleFieldValue withVisibilityHealthWorkerStatus(final Visibility newValue) { visibilityHealthWorkerStatus = newValue; return this; }
	public PeopleFieldValue withVisibilityCondition(final Visibility newValue) { visibilityCondition = newValue; return this; }
	public PeopleFieldValue withVisibilityExposure(final Visibility newValue) { visibilityExposure = newValue; return this; }
	public PeopleFieldValue withVisibilitySymptom(final Visibility newValue) { visibilitySymptom = newValue; return this; }
	public PeopleFieldValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }

	public PeopleFieldValue() {}

	public PeopleFieldValue(final String id,
		final String visibilityHealthWorkerStatusId,
		final String visibilityConditions,
		final String visibilityExposures,
		final String visibilitySymptoms)
	{
		this(id, visibilityHealthWorkerStatusId, visibilityConditions, visibilityExposures, visibilitySymptoms, null);
	}

	public PeopleFieldValue(final String id,
		final String visibilityHealthWorkerStatusId,
		final String visibilityConditions,
		final String visibilityExposures,
		final String visibilitySymptoms,
		final Date updatedAt)
	{
		this(id, visibilityHealthWorkerStatusId, visibilityConditions, visibilityExposures, visibilitySymptoms,
			null, null, null, null, null, updatedAt);
	}

	public PeopleFieldValue(final String id,
		final String visibilityHealthWorkerStatusId,
		final String visibilityConditions,
		final String visibilityExposures,
		final String visibilitySymptoms,
		final String name,
		final Visibility visibilityHealthWorkerStatus,
		final Visibility visibilityCondition,
		final Visibility visibilityExposure,
		final Visibility visibilitySymptom,
		final Date updatedAt)
	{
		this.id = id;
		this.visibilityHealthWorkerStatusId = visibilityHealthWorkerStatusId;
		this.visibilityConditions = visibilityConditions;
		this.visibilityExposures = visibilityExposures;
		this.visibilitySymptoms = visibilitySymptoms;
		this.name = name;
		this.visibilityHealthWorkerStatus = visibilityHealthWorkerStatus;
		this.visibilityCondition = visibilityCondition;
		this.visibilityExposure = visibilityExposure;
		this.visibilitySymptom = visibilitySymptom;
		this.updatedAt = updatedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		id = StringUtils.trimToNull(id);
		visibilityHealthWorkerStatusId = StringUtils.trimToNull(visibilityHealthWorkerStatusId);
		visibilityConditions = StringUtils.trimToNull(visibilityConditions);
		visibilityExposures = StringUtils.trimToNull(visibilityExposures);
		visibilitySymptoms = StringUtils.trimToNull(visibilitySymptoms);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof PeopleFieldValue)) return false;

		var v = (PeopleFieldValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(visibilityHealthWorkerStatusId, v.visibilityHealthWorkerStatusId) &&
			Objects.equals(visibilityConditions, v.visibilityConditions) &&
			Objects.equals(visibilityExposures, v.visibilityExposures) &&
			Objects.equals(visibilitySymptoms, v.visibilitySymptoms) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
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
			.append(", visibilityHealthWorkerStatusId: ").append(visibilityHealthWorkerStatusId)
			.append(", visibilityConditions: ").append(visibilityConditions)
			.append(", visibilityExposures: ").append(visibilityExposures)
			.append(", visibilitySymptoms: ").append(visibilitySymptoms)
			.append(", name: ").append(name)
			.append(", visibilityHealthWorkerStatus: ").append(visibilityHealthWorkerStatus)
			.append(", visibilityCondition: ").append(visibilityCondition)
			.append(", visibilityExposure: ").append(visibilityExposure)
			.append(", visibilitySymptom: ").append(visibilitySymptom)
			.append(", updatedAt: ").append(updatedAt)
			.append(" }").toString();
	}
}
