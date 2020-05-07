package app.allclear.platform.entity;

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.Exposure;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the exposures table.
*
*	@author smalleyd
*	@version 1.0.4
*	@since March 30, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="exposures")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="exposures")
@NamedQueries({@NamedQuery(name="deleteExposuresByPerson", query="DELETE FROM Exposures o WHERE o.personId = :personId")})
public class Exposures implements PeopleChild
{
	private final static long serialVersionUID = 1L;

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="exposure_id", columnDefinition="CHAR(2)", nullable=false)
	@Id
	public String getExposureId() { return exposureId; }
	public String exposureId;
	public void setExposureId(final String newValue) { exposureId = newValue; }

	@Transient @Override public String getChildId() { return getExposureId(); }
	@Transient @Override public void setChildId(final String newValue) { setExposureId(newValue); }
	@Transient @Override public String getChildName() { return Exposure.VALUES.get(getExposureId()).name; }

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

	public Exposures() {}

	public Exposures(final People person, final CreatedValue value)
	{
		this.personId = (this.person = person).getId();
		this.exposureId = value.id;
		this.createdAt = (null != value.createdAt) ? value.createdAt : (value.createdAt = person.getUpdatedAt());

		if (null == value.name) value.name = Exposure.VALUES.get(value.id).name;
	}

	Exposures(final String exposureId) { this.exposureId = exposureId; }	// For tests

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Exposures)) return false;

		var v = (Exposures) o;
		return Objects.equals(personId, v.personId) && Objects.equals(exposureId, v.exposureId);
	}

	@Override
	public int hashCode() { return Objects.hash(personId, exposureId); }

	@Transient
	public CreatedValue toValue()
	{
		return new CreatedValue(
			getExposureId(),
			Exposure.VALUES.get(getExposureId()).name,
			getCreatedAt());
	}
}
