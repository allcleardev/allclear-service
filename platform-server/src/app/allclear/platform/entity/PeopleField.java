package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.platform.type.Visibility;
import app.allclear.platform.value.PeopleFieldValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the people_field table.
*
*	@author smalleyd
*	@version 1.1.36
*	@since May 4, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="people_field")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="people_field")
public class PeopleField implements Serializable
{
	private final static long serialVersionUID = 1L;

	@Column(name="id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getId() { return id; }
	public String id;
	public void setId(final String newValue) { id = newValue; }

	@Column(name="visibility_health_worker_status_id", columnDefinition="CHAR(1)", nullable=false)
	public String getVisibilityHealthWorkerStatusId() { return visibilityHealthWorkerStatusId; }
	public String visibilityHealthWorkerStatusId;
	public void setVisibilityHealthWorkerStatusId(final String newValue) { visibilityHealthWorkerStatusId = newValue; }
	@Transient public Visibility visibilityHealthWorkerStatusId() { return Visibility.get(visibilityHealthWorkerStatusId); }

	@Column(name="visibility_conditions", columnDefinition="CHAR(1)", nullable=false)
	public String getVisibilityConditions() { return visibilityConditions; }
	public String visibilityConditions;
	public void setVisibilityConditions(final String newValue) { visibilityConditions = newValue; }
	@Transient public Visibility visibilityConditions() { return Visibility.get(visibilityConditions); }

	@Column(name="visibility_exposures", columnDefinition="CHAR(1)", nullable=false)
	public String getVisibilityExposures() { return visibilityExposures; }
	public String visibilityExposures;
	public void setVisibilityExposures(final String newValue) { visibilityExposures = newValue; }
	@Transient public Visibility visibilityExposures() { return Visibility.get(visibilityExposures); }

	@Column(name="visibility_symptoms", columnDefinition="CHAR(1)", nullable=false)
	public String getVisibilitySymptoms() { return visibilitySymptoms; }
	public String visibilitySymptoms;
	public void setVisibilitySymptoms(final String newValue) { visibilitySymptoms = newValue; }
	@Transient public Visibility visibilitySymptoms() { return Visibility.get(visibilitySymptoms); }

	@Column(name="updated_at", columnDefinition="DATETIME", nullable=false)
	public Date getUpdatedAt() { return updatedAt; }
	public Date updatedAt;
	public void setUpdatedAt(final Date newValue) { updatedAt = newValue; }

	@OneToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="id", nullable=false, updatable=false, insertable=false)
	public People getPerson() { return person; }
	public People person;
	public void setPerson(final People newValue) { person = newValue; }
	public void putPerson(final People newValue) { id = (person = newValue).getId(); }

	public PeopleField() {}

	public PeopleField(final People person, final Date updatedAt)
	{
		this.putPerson(person);
		this.visibilityHealthWorkerStatusId = Visibility.DEFAULT.id;
		this.visibilityConditions = Visibility.DEFAULT.id;
		this.visibilityExposures = Visibility.DEFAULT.id;
		this.visibilitySymptoms = Visibility.DEFAULT.id;
		this.updatedAt = updatedAt;
	}

	PeopleField(final String id,	// For unit tests
		final String visibilityHealthWorkerStatusId,
		final String visibilityConditions,
		final String visibilityExposures,
		final String visibilitySymptoms)
	{
		this.id = id;
		this.visibilityHealthWorkerStatusId = visibilityHealthWorkerStatusId;
		this.visibilityConditions = visibilityConditions;
		this.visibilityExposures = visibilityExposures;
		this.visibilitySymptoms = visibilitySymptoms;
	}

	public PeopleFieldValue update(final PeopleFieldValue value)
	{
		this.visibilityHealthWorkerStatusId = value.visibilityHealthWorkerStatusId;
		this.visibilityConditions = value.visibilityConditions;
		this.visibilityExposures = value.visibilityExposures;
		this.visibilitySymptoms = value.visibilitySymptoms;
		this.updatedAt = value.updatedAt = new Date();

		return value;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof PeopleField)) return false;

		var v = (PeopleField) o;
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

	@Transient
	public PeopleFieldValue toValue()
	{
		return new PeopleFieldValue(
			getId(),
			getVisibilityHealthWorkerStatusId(),
			getVisibilityConditions(),
			getVisibilityExposures(),
			getVisibilitySymptoms(),
			getPerson().getName(),
			Visibility.get(getVisibilityHealthWorkerStatusId()),
			Visibility.get(getVisibilityConditions()),
			Visibility.get(getVisibilityExposures()),
			Visibility.get(getVisibilitySymptoms()),
			getUpdatedAt());
	}
}
