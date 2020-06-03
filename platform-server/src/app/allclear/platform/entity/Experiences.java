package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.platform.value.ExperiencesValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the experiences table.
*
*	@author smalleyd
*	@version 1.1.80
*	@since June 2, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="experiences",
	uniqueConstraints={@UniqueConstraint(columnNames={"person_id", "facility_id"})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="experiences")
@NamedQueries({@NamedQuery(name="findExperience", query="SELECT OBJECT(o) FROM Experiences o WHERE o.personId = :personId AND o.facilityId = :facilityId")})
public class Experiences implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Column(name="id", columnDefinition="BIGINT", nullable=false)
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() { return id; }
	public Long id;
	public void setId(final Long newValue) { id = newValue; }

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="facility_id", columnDefinition="BIGINT", nullable=false)
	public Long getFacilityId() { return facilityId; }
	public Long facilityId;
	public void setFacilityId(final Long newValue) { facilityId = newValue; }

	@Column(name="positive", columnDefinition="BIT", nullable=false)
	public boolean isPositive() { return positive; }
	public boolean positive;
	public void setPositive(final boolean newValue) { positive = newValue; }

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

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="facility_id", nullable=false, updatable=false, insertable=false)
	public Facility getFacility() { return facility; }
	public Facility facility;
	public void setFacility(final Facility newValue) { facility = newValue; }
	public void putFacility(final Facility newValue) { facilityId = (facility = newValue).getId(); }

	void put(final Object[] cmrs)
	{
		putPerson((People) cmrs[1]);
		putFacility((Facility) cmrs[2]);
	}

	public Experiences() {}

	public Experiences(final ExperiencesValue value, final Object[] cmrs)
	{
		this.put(cmrs);
		this.positive = value.positive;
		this.createdAt = value.createdAt = new Date();
	}

	public Experiences update(final ExperiencesValue value, final Object[] cmrs)
	{
		this.put(cmrs);
		this.positive = value.positive;
		value.createdAt = getCreatedAt();

		return this;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Experiences)) return false;

		var v = (Experiences) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(personId, v.personId) &&
			Objects.equals(facilityId, v.facilityId) &&
			(positive == v.positive) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(id);
	}

	@Transient
	public ExperiencesValue toValue()
	{
		return new ExperiencesValue(
			getId(),
			getPersonId(),
			getFacilityId(),
			isPositive(),
			getCreatedAt());
	}

	@Transient
	public ExperiencesValue toValueX()
	{
		return toValue().denormalize(getPerson().getName(), getFacility().getName());
	}
}
