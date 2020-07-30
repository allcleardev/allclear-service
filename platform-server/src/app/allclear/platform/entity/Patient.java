package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.platform.value.PatientValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the patient table.
*
*	@author smalleyd
*	@version 1.1.111
*	@since July 18, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="patient")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="patient")
@NamedQueries({@NamedQuery(name="findPatient", query="SELECT OBJECT(o) FROM Patient o WHERE o.facilityId = :facilityId AND o.personId = :personId"),
	@NamedQuery(name="findEnrolledPatientsByAssociateAndName", query="SELECT OBJECT(p) FROM Patient o INNER JOIN o.person p INNER JOIN o.facility f INNER JOIN f.people a WHERE a.personId = :associateId AND o.enrolledAt IS NOT NULL AND ((p.name LIKE :name) OR (p.phone LIKE :name) OR (p.email LIKE :name) OR (p.firstName LIKE :name) OR (p.lastName LIKE :name)) ORDER BY p.name"),
	@NamedQuery(name="findEnrolledPatientsByFacilityAndName", query="SELECT OBJECT(p) FROM Patient o INNER JOIN o.person p WHERE o.facilityId = :facilityId AND o.enrolledAt IS NOT NULL AND ((p.name LIKE :name) OR (p.phone LIKE :name) OR (p.email LIKE :name) OR (p.firstName LIKE :name) OR (p.lastName LIKE :name)) ORDER BY p.name")})
public class Patient implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Column(name="id", columnDefinition="BIGINT", nullable=false)
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() { return id; }
	public Long id;
	public void setId(final Long newValue) { id = newValue; }

	@Column(name="facility_id", columnDefinition="BIGINT", nullable=false)
	public Long getFacilityId() { return facilityId; }
	public Long facilityId;
	public void setFacilityId(final Long newValue) { facilityId = newValue; }

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="alertable", columnDefinition="BIT", nullable=false)
	public boolean isAlertable() { return alertable; }
	public boolean alertable;
	public void setAlertable(final boolean newValue) { alertable = newValue; }

	@Column(name="enrolled_at", columnDefinition="DATETIME", nullable=true)
	public Date getEnrolledAt() { return enrolledAt; }
	public Date enrolledAt;
	public void setEnrolledAt(final Date newValue) { enrolledAt = newValue; }

	@Column(name="rejected_at", columnDefinition="DATETIME", nullable=true)
	public Date getRejectedAt() { return rejectedAt; }
	public Date rejectedAt;
	public void setRejectedAt(final Date newValue) { rejectedAt = newValue; }

	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@Column(name="updated_at", columnDefinition="DATETIME", nullable=false)
	public Date getUpdatedAt() { return updatedAt; }
	public Date updatedAt;
	public void setUpdatedAt(final Date newValue) { updatedAt = newValue; }

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

	public Patient() {}

	public Patient(final Facility facility, final People person)
	{
		putFacility(facility);
		putPerson(person);
		this.alertable = false;
		this.enrolledAt = null;
		this.rejectedAt = null;
		this.createdAt = this.updatedAt = new Date();
	}

	public Patient(final PatientValue value, final Object[] cmrs)
	{
		this.facilityId = value.facilityId = (this.facility = (Facility) cmrs[1]).getId();
		this.personId = value.personId = (this.person = (People) cmrs[2]).getId();
		this.alertable = value.alertable;
		this.enrolledAt = value.enrolledAt;
		this.rejectedAt = value.rejectedAt;
		this.createdAt = this.updatedAt = value.createdAt = value.updatedAt = new Date();
	}

	public Patient update(final PatientValue value, final Object[] cmrs)
	{
		this.facilityId = value.facilityId = (this.facility = (Facility) cmrs[1]).getId();
		this.personId = value.personId = (this.person = (People) cmrs[2]).getId();
		this.alertable = value.alertable;
		this.enrolledAt = value.enrolledAt;
		this.rejectedAt = value.rejectedAt;
		value.createdAt = getCreatedAt();
		this.updatedAt = value.updatedAt = new Date();

		return this;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Patient)) return false;

		var v = (Patient) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(facilityId, v.facilityId) &&
			Objects.equals(personId, v.personId) &&
			(alertable == v.alertable) &&
			((enrolledAt == v.enrolledAt) || DateUtils.truncatedEquals(enrolledAt, v.enrolledAt, Calendar.SECOND)) &&
			((rejectedAt == v.rejectedAt) || DateUtils.truncatedEquals(rejectedAt, v.rejectedAt, Calendar.SECOND)) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(id);
	}

	@Transient
	public PatientValue toValue()
	{
		return new PatientValue(
			getId(),
			getFacilityId(),
			getPersonId(),
			isAlertable(),
			getEnrolledAt(),
			getRejectedAt(),
			getCreatedAt(),
			getUpdatedAt()).denormalize(getFacility().getName(), getPerson().getName());
	}
}
