package app.allclear.platform.entity;

import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.common.value.CreateValue;
import app.allclear.common.value.CreatedValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the facility_people table.
*
*	@author smalleyd
*	@version 1.1.101
*	@since July 5, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="facility_people")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="facility_people")
@NamedQueries({@NamedQuery(name="deleteFacilityPeople", query="DELETE FROM FacilityPeople o WHERE o.facilityId = :facilityId")})
@NamedNativeQueries({@NamedNativeQuery(name="findFacilityPeopleByFacility", query="SELECT p.id, p.name, o.created_at, o.facility_id AS parent_id FROM facility_people o INNER JOIN people p ON o.person_id = p.id WHERE o.facility_id = :facilityId ORDER BY p.name", resultClass=Created.class),
	@NamedNativeQuery(name="findFacilityPeopleByFacilities", query="SELECT p.id, p.name, o.created_at, o.facility_id AS parent_id FROM facility_people o INNER JOIN people p ON o.person_id = p.id WHERE o.facility_id IN (:facilityIds) ORDER BY o.facility_id, p.name", resultClass=Created.class)})
public class FacilityPeople implements FacilityChild
{
	private static final long serialVersionUID = 1L;

	@Column(name="facility_id", columnDefinition="BIGINT", nullable=false)
	@Id
	public Long getFacilityId() { return facilityId; }
	public Long facilityId;
	public void setFacilityId(final Long newValue) { facilityId = newValue; }

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Transient public String getChildId() { return getPersonId(); }
	@Transient public String getChildName() { return getPerson().getName(); }
	@Transient public void setChildId(final String newValue) { setPersonId(newValue); }

	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="facility_id", nullable=false, updatable=false, insertable=false)
	public Facility getFacility() { return facility; }
	public Facility facility;
	public void setFacility(final Facility newValue) { facility = newValue; }
	public void putFacility(final Facility newValue) { facilityId = (facility = newValue).getId(); }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="person_id", nullable=false, updatable=false, insertable=false)
	public People getPerson() { return person; }
	public People person;
	public void setPerson(final People newValue) { person = newValue; }
	public void putPerson(final People newValue) { personId = (person = newValue).getId(); }

	public FacilityPeople() {}

	public FacilityPeople(final Facility facility,
		final People person,
		final CreatedValue value)
	{
		this.facilityId = (this.facility = facility).getId();
		this.personId = (this.person = person).getId();
		this.createdAt = ((null != value.createdAt) ? value.createdAt : (value.createdAt = facility.getUpdatedAt()));
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof FacilityPeople)) return false;

		var v = (FacilityPeople) o;
		return Objects.equals(facilityId, v.facilityId) &&
			Objects.equals(personId, v.personId) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(facilityId, personId);
	}

	@Transient
	public CreatedValue toValue()
	{
		return new CreatedValue(getPersonId(), getPerson().getName(), getCreatedAt());
	}

	@Transient
	public CreateValue toCreate()
	{
		return new CreateValue(getFacilityId(), getFacility().getName(), getCreatedAt());
	}
}
