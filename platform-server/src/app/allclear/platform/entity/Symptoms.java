package app.allclear.platform.entity;

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.Symptom;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the symptoms table.
*
*	@author smalleyd
*	@version 1.0.4
*	@since March 30, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="symptoms")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="symptoms")
@NamedQueries({@NamedQuery(name="deleteSymptomsByPerson", query="DELETE FROM Symptoms o WHERE o.personId = :personId")})
public class Symptoms implements PeopleChild
{
	private final static long serialVersionUID = 1L;

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="symptom_id", columnDefinition="CHAR(2)", nullable=false)
	@Id
	public String getSymptomId() { return symptomId; }
	public String symptomId;
	public void setSymptomId(final String newValue) { symptomId = newValue; }

	@Transient @Override public String getChildId() { return getSymptomId(); }
	@Transient @Override public void setChildId(final String newValue) { setSymptomId(newValue); }
	@Transient @Override public String getChildName() { return Symptom.get(getSymptomId()).name; }
	
	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="person_id", nullable=false, updatable=false, insertable=false)
	public People getPerson() { return person; }
	public People person;
	public void setPerson(final People newValue) { person = newValue; }
	public void putPerson(final People newValue) { personId = (person = newValue).getId(); }

	public Symptoms() {}

	public Symptoms(final People person, final CreatedValue value)
	{
		this.personId = (this.person = person).getId();
		this.symptomId = value.id;
		this.createdAt = (null != value.createdAt) ? value.createdAt : (value.createdAt = person.getUpdatedAt());

		if (null == value.name) value.name = Symptom.VALUES.get(value.id).name; 
	}

	Symptoms(final String symptomId) { this.symptomId = symptomId; }	// For tests

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Symptoms)) return false;

		var v = (Symptoms) o;
		return Objects.equals(personId, v.personId) && Objects.equals(symptomId, v.symptomId);	// MUST exclude createdAt since this doubles as the PK class.
	}

	@Override
	public int hashCode() { return Objects.hash(personId, symptomId); }

	@Transient
	public CreatedValue toValue()
	{
		return new CreatedValue(
			getSymptomId(),
			Symptom.VALUES.get(getSymptomId()).name,
			getCreatedAt());
	}
}
