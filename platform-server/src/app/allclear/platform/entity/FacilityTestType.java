package app.allclear.platform.entity;

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.TestType;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the facility_test_type table.
*
*	@author smalleyd
*	@version 1.1.44
*	@since May 8, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="facility_test_type")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="facility_test_type")
@NamedQueries({@NamedQuery(name="deleteFacilityTestTypes", query="DELETE FROM FacilityTestType o WHERE o.facilityId = :facilityId"),
	@NamedQuery(name="findFacilityTestTypes", query="SELECT OBJECT(o) FROM FacilityTestType o WHERE o.facilityId IN (:facilityIds) ORDER BY o.facilityId, o.testTypeId")})
public class FacilityTestType implements FacilityChild
{
	private final static long serialVersionUID = 1L;

	@Column(name="facility_id", columnDefinition="BIGINT", nullable=false)
	@Id
	public Long getFacilityId() { return facilityId; }
	public Long facilityId;
	public void setFacilityId(final Long newValue) { facilityId = newValue; }

	@Column(name="test_type_id", columnDefinition="CHAR(2)", nullable=false)
	@Id
	public String getTestTypeId() { return testTypeId; }
	public String testTypeId;
	public void setTestTypeId(final String newValue) { testTypeId = newValue; }

	@Transient public String getChildId() { return getTestTypeId(); }
	@Transient public void setChildId(final String newValue) { testTypeId = newValue; }
	@Transient public String getChildName() { return TestType.get(getTestTypeId()).name; }

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

	public FacilityTestType() {}

	public FacilityTestType(final Facility facility,
		final CreatedValue value)
	{
		this.putFacility(facility);
		this.testTypeId = value.id;
		this.createdAt = ((null != value.createdAt) ? value.createdAt : (value.createdAt = facility.getUpdatedAt()));

		if (null == value.name) value.name = TestType.VALUES.get(value.id).name;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof FacilityTestType)) return false;

		var v = (FacilityTestType) o;
		return Objects.equals(facilityId, v.facilityId) && Objects.equals(testTypeId, v.testTypeId);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(facilityId, testTypeId);
	}

	@Transient
	public CreatedValue toValue()
	{
		return new CreatedValue(getTestTypeId(), getChildName(), getCreatedAt());
	}
}
