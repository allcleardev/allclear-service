package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.platform.type.TestType;
import app.allclear.platform.value.TestsValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the tests table.
*
*	@author smalleyd
*	@version 1.0.44
*	@since April 4, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="tests")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="tests")
@NamedQueries(@NamedQuery(name="findTestsByPerson", query="SELECT OBJECT(o) FROM Tests o WHERE o.personId = :personId ORDER BY o.takenOn DESC"))
public class Tests implements Serializable
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

	@Column(name="type_id", columnDefinition="CHAR(2)", nullable=false)
	public String getTypeId() { return typeId; }
	public String typeId;
	public void setTypeId(final String newValue) { typeId = newValue; }

	@Column(name="taken_on", columnDefinition="DATE", nullable=false)
	public Date getTakenOn() { return takenOn; }
	public Date takenOn;
	public void setTakenOn(final Date newValue) { takenOn = newValue; }

	@Column(name="facility_id", columnDefinition="BIGINT", nullable=false)
	public Long getFacilityId() { return facilityId; }
	public Long facilityId;
	public void setFacilityId(final Long newValue) { facilityId = newValue; }

	@Column(name="positive", columnDefinition="BIT", nullable=false)
	public boolean isPositive() { return positive; }
	public boolean positive;
	public void setPositive(final boolean newValue) { positive = newValue; }

	@Column(name="notes", columnDefinition="TEXT", nullable=true)
	public String getNotes() { return notes; }
	public String notes;
	public void setNotes(final String newValue) { notes = newValue; }

	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@Column(name="updated_at", columnDefinition="DATETIME", nullable=false)
	public Date getUpdatedAt() { return updatedAt; }
	public Date updatedAt;
	public void setUpdatedAt(final Date newValue) { updatedAt = newValue; }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="person_id", nullable=false, updatable=false, insertable=false)
	public People getPerson() { return person; }
	public People person;
	public void setPerson(final People newValue) { person = newValue; }
	public void putPerson(final People newValue) { personId = (person = newValue).getId(); }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="facility_id", nullable=false, updatable=false, insertable=false)
	public Facility getFacility() { return facility; }
	public Facility facility;
	public void setFacility(final Facility newValue) { facility = newValue; }
	public void putFacility(final Facility newValue) { facilityId = (facility = newValue).getId(); }

	public Tests() {}

	public Tests(
		final People person,
		final String typeId,
		final Date takenOn,
		final Facility facility,
		final boolean positive,
		final String notes,
		final Date createdAt)
	{
		this.personId = (this.person = person).getId();
		this.typeId = typeId;
		this.takenOn = takenOn;
		this.facilityId = (this.facility = facility).getId();
		this.positive = positive;
		this.notes = notes;
		this.createdAt = this.updatedAt = createdAt;
	}

	public Tests(final TestsValue value, final People person, final Facility facility)
	{
		this(person, value.typeId, value.takenOn, facility, value.positive, value.notes, value.createdAt = value.updatedAt = new Date());
	}

	public Tests update(final TestsValue value, final People person, final Facility facility)
	{
		putPerson(person);
		setTypeId(value.typeId);
		setTakenOn(value.takenOn);
		putFacility(facility);
		setPositive(value.positive);
		setNotes(value.notes);
		value.createdAt = getCreatedAt();
		setUpdatedAt(value.updatedAt = new Date());

		return this;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Tests)) return false;

		var v = (Tests) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(personId, v.personId) &&
			Objects.equals(typeId, v.typeId) &&
			DateUtils.truncatedEquals(takenOn, v.takenOn, Calendar.SECOND) &&
			Objects.equals(facilityId, v.facilityId) &&
			(positive == v.positive) &&
			Objects.equals(notes, v.notes) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override public int hashCode() { return Objects.hashCode(id); }

	@Transient
	public TestsValue toValue()
	{
		return new TestsValue(
			getId(),
			getPersonId(),
			getPerson().getName(),
			getTypeId(),
			TestType.get(getTypeId()),
			getTakenOn(),
			getFacilityId(),
			getFacility().getName(),
			isPositive(),
			getNotes(),
			getCreatedAt(),
			getUpdatedAt());
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", personId: ").append(personId)
			.append(", typeId: ").append(typeId)
			.append(", takenOn: ").append(takenOn)
			.append(", facilityId: ").append(facilityId)
			.append(", positive: ").append(positive)
			.append(", notes: ").append(notes)
			.append(", createdAt: ").append(createdAt)
			.append(", updatedAt: ").append(updatedAt)
			.append(" }").toString();
	}
}
