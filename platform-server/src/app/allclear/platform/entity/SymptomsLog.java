package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.platform.type.Symptom;
import app.allclear.platform.value.SymptomsLogValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the symptoms_log table.
*
*	@author smalleyd
*	@version 1.0.80
*	@since April 8, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="symptoms_log")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="symptoms_log")
public class SymptomsLog implements Serializable
{
	private final static long serialVersionUID = 1L;

	@Column(name="id", columnDefinition="BIGINT", nullable=false)
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() { return id; }
	public Long id;
	public void setId(final Long newValue) { id = newValue; }

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="symptom_id", columnDefinition="CHAR(2)", nullable=false)
	public String getSymptomId() { return symptomId; }
	public String symptomId;
	public void setSymptomId(final String newValue) { symptomId = newValue; }

	@Column(name="started_at", columnDefinition="DATETIME", nullable=false)
	public Date getStartedAt() { return startedAt; }
	public Date startedAt;
	public void setStartedAt(final Date newValue) { startedAt = newValue; }

	@Column(name="ended_at", columnDefinition="DATETIME", nullable=true)
	public Date getEndedAt() { return endedAt; }
	public Date endedAt;
	public void setEndedAt(final Date newValue) { endedAt = newValue; }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="person_id", nullable=false, updatable=false, insertable=false)
	public People getPerson() { return person; }
	public People person;
	public void setPerson(final People newValue) { person = newValue; }
	public void putPerson(final People newValue) { personId = (person = newValue).getId(); }

	public SymptomsLog() {}

	public SymptomsLog(
		final People person,
		final String symptomId,
		final Date startedAt,
		final Date endedAt)
	{
		this.personId = (this.person = person).getId();
		this.symptomId = symptomId;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
	}

	public SymptomsLog(final SymptomsLogValue value, final People person)
	{
		this(person, value.symptomId, value.startedAt, value.endedAt);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof SymptomsLog)) return false;

		var v = (SymptomsLog) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(personId, v.personId) &&
			Objects.equals(symptomId, v.symptomId) &&
			DateUtils.truncatedEquals(startedAt, v.startedAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(endedAt, v.endedAt, Calendar.SECOND);
	}

	@Transient
	public SymptomsLogValue toValue()
	{
		return new SymptomsLogValue(
			getId(),
			getPersonId(),
			getPerson().getName(),
			getSymptomId(),
			Symptom.get(getSymptomId()),
			getStartedAt(),
			getEndedAt());
	}
}
