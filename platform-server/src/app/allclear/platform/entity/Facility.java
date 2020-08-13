package app.allclear.platform.entity;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.FacilityType;
import app.allclear.platform.type.TestCriteria;
import app.allclear.platform.value.FacilityValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the facility table.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="facility", uniqueConstraints=@UniqueConstraint(name="unq_facility", columnNames="name"))
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="facility")
@NamedQueries({@NamedQuery(name="existFacilityById", query="SELECT o.id FROM Facility o WHERE o.id = :id"),
	@NamedQuery(name="findActiveFacilitiesByName", query="SELECT OBJECT(o) FROM Facility o WHERE o.name LIKE :name AND o.active = TRUE ORDER BY o.name"),
	@NamedQuery(name="findFacility", query="SELECT OBJECT(o) FROM Facility o WHERE o.name = :name")})
@NamedNativeQueries({@NamedNativeQuery(name="getFacilityCitiesByState", query="SELECT o.city AS name, COUNT(o.city) AS total FROM facility o WHERE o.state = :state AND o.active = TRUE GROUP BY o.city ORDER BY o.city", resultClass=CountByName.class),
	@NamedNativeQuery(name="getFacilityCounties", query="SELECT o.county_id AS name, COUNT(o.county_id) AS total FROM facility o WHERE o.active = TRUE AND o.county_id IS NOT NULL GROUP BY o.county_id ORDER BY o.county_id", resultClass=CountByName.class),
	@NamedNativeQuery(name="getFacilityNamesByIds", query="SELECT o.id, o.name FROM facility o WHERE o.id IN (:ids)", resultClass=Name.class),
	@NamedNativeQuery(name="getFacilityStates", query="SELECT o.state AS name, COUNT(o.state) AS total FROM facility o WHERE o.active = TRUE AND o.state IS NOT NULL GROUP BY o.state ORDER BY o.state", resultClass=CountByName.class)})
public class Facility implements Serializable
{
	private final static long serialVersionUID = 1L;

	@Column(name="id", columnDefinition="BIGINT", nullable=false)
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() { return id; }
	public Long id;
	public void setId(final Long newValue) { id = newValue; }

	@Column(name="name", columnDefinition="VARCHAR(128)", nullable=false)
	public String getName() { return name; }
	public String name;
	public void setName(final String newValue) { name = newValue; }

	@Column(name="address", columnDefinition="VARCHAR(128)", nullable=false)
	public String getAddress() { return address; }
	public String address;
	public void setAddress(final String newValue) { address = newValue; }

	@Column(name="city", columnDefinition="VARCHAR(128)", nullable=false)
	public String getCity() { return city; }
	public String city;
	public void setCity(final String newValue) { city = newValue; }

	@Column(name="state", columnDefinition="VARCHAR(128)", nullable=false)
	public String getState() { return state; }
	public String state;
	public void setState(final String newValue) { state = newValue; }

	@Column(name="county_id", columnDefinition="CHAR(5)", nullable=true)
	public String getCountyId() { return countyId; }
	public String countyId;
	public void setCountyId(final String newValue) { countyId = newValue; }

	@Column(name="county_name", columnDefinition="VARCHAR(128)", nullable=true)
	public String getCountyName() { return countyName; }
	public String countyName;
	public void setCountyName(final String newValue) { countyName = newValue; }

	@Column(name="latitude", columnDefinition="DECIMAL(12,8)", nullable=false)
	public BigDecimal getLatitude() { return latitude; }
	public BigDecimal latitude;
	public void setLatitude(final BigDecimal newValue) { latitude = newValue; }

	@Column(name="longitude", columnDefinition="DECIMAL(12,8)", nullable=false)
	public BigDecimal getLongitude() { return longitude; }
	public BigDecimal longitude;
	public void setLongitude(final BigDecimal newValue) { longitude = newValue; }

	@Column(name="phone", columnDefinition="VARCHAR(32)", nullable=true)
	public String getPhone() { return phone; }
	public String phone;
	public void setPhone(final String newValue) { phone = newValue; }

	@Column(name="appointment_phone", columnDefinition="VARCHAR(32)", nullable=true)
	public String getAppointmentPhone() { return appointmentPhone; }
	public String appointmentPhone;
	public void setAppointmentPhone(final String newValue) { appointmentPhone = newValue; }

	@Column(name="email", columnDefinition="VARCHAR(128)", nullable=true)
	public String getEmail() { return email; }
	public String email;
	public void setEmail(final String newValue) { email = newValue; }

	@Column(name="url", columnDefinition="VARCHAR(128)", nullable=true)
	public String getUrl() { return url; }
	public String url;
	public void setUrl(final String newValue) { url = newValue; }

	@Column(name="appointment_url", columnDefinition="VARCHAR(128)", nullable=true)
	public String getAppointmentUrl() { return appointmentUrl; }
	public String appointmentUrl;
	public void setAppointmentUrl(final String newValue) { appointmentUrl = newValue; }

	@Column(name="hours", columnDefinition="TEXT", nullable=true)
	public String getHours() { return hours; }
	public String hours;
	public void setHours(final String newValue) { hours = newValue; }

	@Column(name="type_id", columnDefinition="CHAR(2)", nullable=true)
	public String getTypeId() { return typeId; }
	public String typeId;
	public void setTypeId(final String newValue) { typeId = newValue; }

	@Column(name="drive_thru", columnDefinition="BIT", nullable=false)
	public boolean isDriveThru() { return driveThru; }
	public boolean driveThru;
	public void setDriveThru(final boolean newValue) { driveThru = newValue; }

	@Column(name="appointment_required", columnDefinition="BIT", nullable=true)
	public Boolean isAppointmentRequired() { return appointmentRequired; }
	public Boolean appointmentRequired;
	public void setAppointmentRequired(final Boolean newValue) { appointmentRequired = newValue; }

	@Column(name="accepts_third_party", columnDefinition="BIT", nullable=true)
	public Boolean isAcceptsThirdParty() { return acceptsThirdParty; }
	public Boolean acceptsThirdParty;
	public void setAcceptsThirdParty(final Boolean newValue) { acceptsThirdParty = newValue; }

	@Column(name="referral_required", columnDefinition="BIT", nullable=false)
	public boolean isReferralRequired() { return referralRequired; }
	public boolean referralRequired;
	public void setReferralRequired(final boolean newValue) { referralRequired = newValue; }

	@Column(name="test_criteria_id", columnDefinition="CHAR(2)", nullable=true)
	public String getTestCriteriaId() { return testCriteriaId; }
	public String testCriteriaId;
	public void setTestCriteriaId(final String newValue) { testCriteriaId = newValue; }

	@Column(name="other_test_criteria", columnDefinition="TEXT", nullable=true)
	public String getOtherTestCriteria() { return otherTestCriteria; }
	public String otherTestCriteria;
	public void setOtherTestCriteria(final String newValue) { otherTestCriteria = newValue; }

	@Column(name="tests_per_day", columnDefinition="INT", nullable=true)
	public Integer getTestsPerDay() { return testsPerDay; }
	public Integer testsPerDay;
	public void setTestsPerDay(final Integer newValue) { testsPerDay = newValue; }

	@Column(name="government_id_required", columnDefinition="BIT", nullable=false)
	public boolean isGovernmentIdRequired() { return governmentIdRequired; }
	public boolean governmentIdRequired;
	public void setGovernmentIdRequired(final boolean newValue) { governmentIdRequired = newValue; }

	@Column(name="minimum_age", columnDefinition="INT", nullable=true)
	public Integer getMinimumAge() { return minimumAge; }
	public Integer minimumAge;
	public void setMinimumAge(final Integer newValue) { minimumAge = newValue; }

	@Column(name="doctor_referral_criteria", columnDefinition="TEXT", nullable=true)
	public String getDoctorReferralCriteria() { return doctorReferralCriteria; }
	public String doctorReferralCriteria;
	public void setDoctorReferralCriteria(final String newValue) { doctorReferralCriteria = newValue; }

	@Column(name="first_responder_friendly", columnDefinition="BIT", nullable=false)
	public boolean isFirstResponderFriendly() { return firstResponderFriendly; }
	public boolean firstResponderFriendly;
	public void setFirstResponderFriendly(final boolean newValue) { firstResponderFriendly = newValue; }

	@Column(name="telescreening_available", columnDefinition="BIT", nullable=false)
	public boolean isTelescreeningAvailable() { return telescreeningAvailable; }
	public boolean telescreeningAvailable;
	public void setTelescreeningAvailable(final boolean newValue) { telescreeningAvailable = newValue; }

	@Column(name="accepts_insurance", columnDefinition="BIT", nullable=false)
	public boolean isAcceptsInsurance() { return acceptsInsurance; }
	public boolean acceptsInsurance;
	public void setAcceptsInsurance(final boolean newValue) { acceptsInsurance = newValue; }

	@Column(name="insurance_providers_accepted", columnDefinition="TEXT", nullable=true)
	public String getInsuranceProvidersAccepted() { return insuranceProvidersAccepted; }
	public String insuranceProvidersAccepted;
	public void setInsuranceProvidersAccepted(final String newValue) { insuranceProvidersAccepted = newValue; }

	@Column(name="free_or_low_cost", columnDefinition="BIT", nullable=false)
	public boolean isFreeOrLowCost() { return freeOrLowCost; }
	public boolean freeOrLowCost;
	public void setFreeOrLowCost(final boolean newValue) { freeOrLowCost = newValue; }

	@Column(name="can_donate_plasma", columnDefinition="BIT", nullable=false)
	public boolean isCanDonatePlasma() { return canDonatePlasma; }
	public boolean canDonatePlasma;
	public void setCanDonatePlasma(final boolean newValue) { canDonatePlasma = newValue; }

	@Column(name="result_notification_enabled", columnDefinition="BIT", nullable=false)
	public boolean isResultNotificationEnabled() { return resultNotificationEnabled; }
	public boolean resultNotificationEnabled;
	public void setResultNotificationEnabled(final boolean newValue) { resultNotificationEnabled = newValue; }

	@Column(name="notes", columnDefinition="TEXT", nullable=true)
	public String getNotes() { return notes; }
	public String notes;
	public void setNotes(final String newValue) { notes = newValue; }

	@Column(name="active", columnDefinition="BIT", nullable=false)
	public boolean isActive() { return active; }
	public boolean active;
	public void setActive(final boolean newValue) { active = newValue; }

	@Column(name="activated_at", columnDefinition="DATETIME", nullable=true)
	public Date getActivatedAt() { return activatedAt; }
	public Date activatedAt;
	public void setActivatedAt(final Date newValue) { activatedAt = newValue; }

	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@Column(name="updated_at", columnDefinition="DATETIME", nullable=false)
	public Date getUpdatedAt() { return updatedAt; }
	public Date updatedAt;
	public void setUpdatedAt(final Date newValue) { updatedAt = newValue; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="facility")
	public List<FacilityPeople> getPeople() { return people; }
	public List<FacilityPeople> people;
	public void setPeople(final List<FacilityPeople> newValues) { people = newValues; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="facility")
	@OrderBy("testTypeId")
	public List<FacilityTestType> getTestTypes() { return testTypes; }
	public List<FacilityTestType> testTypes;
	public void setTestTypes(final List<FacilityTestType> newValues) { testTypes = newValues; }

	public Facility() {}

	public Facility(final FacilityValue value)
	{
		this.name = value.name;
		this.address = value.address;
		this.city = value.city;
		this.state = value.state;
		this.countyId = value.countyId;
		this.countyName = value.countyName;
		this.latitude = value.latitude;
		this.longitude = value.longitude;
		this.phone = value.phone;
		this.appointmentPhone = value.appointmentPhone;
		this.email = value.email;
		this.url = value.url;
		this.appointmentUrl = value.appointmentUrl;
		this.hours = value.hours;
		this.typeId = value.typeId;
		this.driveThru = value.driveThru;
		this.appointmentRequired = value.appointmentRequired;
		this.acceptsThirdParty = value.acceptsThirdParty;
		this.referralRequired = value.referralRequired;
		this.testCriteriaId = value.testCriteriaId;
		this.otherTestCriteria = value.otherTestCriteria;
		this.testsPerDay = value.testsPerDay;
		this.governmentIdRequired = value.governmentIdRequired;
		this.minimumAge = value.minimumAge;
		this.doctorReferralCriteria = value.doctorReferralCriteria;
		this.firstResponderFriendly = value.firstResponderFriendly;
		this.telescreeningAvailable = value.telescreeningAvailable;
		this.acceptsInsurance = value.acceptsInsurance;
		this.insuranceProvidersAccepted = value.insuranceProvidersAccepted;
		this.freeOrLowCost = value.freeOrLowCost;
		this.canDonatePlasma = value.canDonatePlasma;
		this.resultNotificationEnabled = value.resultNotificationEnabled;
		this.notes = value.notes;
		this.active = value.active;
		this.createdAt = this.updatedAt = value.createdAt = value.updatedAt = new Date();
		this.activatedAt = value.activatedAt = (value.active ? value.createdAt : null);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Facility)) return false;

		var v = (Facility) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(name, v.name) &&
			Objects.equals(address, v.address) &&
			Objects.equals(city, v.city) &&
			Objects.equals(state, v.state) &&
			Objects.equals(countyId, v.countyId) &&
			Objects.equals(countyName, v.countyName) &&
			(0 == Objects.compare(latitude, v.latitude, (a, b) -> a.compareTo(b))) &&
			(0 == Objects.compare(longitude, v.longitude, (a, b) -> a.compareTo(b))) &&
			Objects.equals(phone, v.phone) &&
			Objects.equals(appointmentPhone, v.appointmentPhone) &&
			Objects.equals(email, v.email) &&
			Objects.equals(url, v.url) &&
			Objects.equals(appointmentUrl, v.appointmentUrl) &&
			Objects.equals(hours, v.hours) &&
			Objects.equals(typeId, v.typeId) &&
			(driveThru == v.driveThru) &&
			Objects.equals(appointmentRequired, v.appointmentRequired) &&
			Objects.equals(acceptsThirdParty, v.acceptsThirdParty) &&
			(referralRequired == v.referralRequired) &&
			Objects.equals(testCriteriaId, v.testCriteriaId) &&
			Objects.equals(otherTestCriteria, v.otherTestCriteria) &&
			Objects.equals(testsPerDay, v.testsPerDay) &&
			(governmentIdRequired == v.governmentIdRequired) &&
			Objects.equals(minimumAge, v.minimumAge) &&
			Objects.equals(doctorReferralCriteria, v.doctorReferralCriteria) &&
			(firstResponderFriendly == v.firstResponderFriendly) &&
			(telescreeningAvailable == v.telescreeningAvailable) &&
			(acceptsInsurance == v.acceptsInsurance) &&
			Objects.equals(insuranceProvidersAccepted, v.insuranceProvidersAccepted) &&
			(freeOrLowCost == v.freeOrLowCost) &&
			(canDonatePlasma == v.canDonatePlasma) &&
			(resultNotificationEnabled = v.resultNotificationEnabled) && 
			Objects.equals(notes, v.notes) &&
			(active == v.active) &&
			((activatedAt == v.activatedAt) || DateUtils.truncatedEquals(activatedAt, v.activatedAt, Calendar.SECOND)) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override public int hashCode() { return Objects.hashCode(id); }

	public Facility update(final FacilityValue value, final boolean admin)
	{
		setName(value.name);
		setAddress(value.address);
		setCity(value.city);
		setState(value.state);
		setCountyId(value.countyId);
		setCountyName(value.countyName);
		setLatitude(value.latitude);
		setLongitude(value.longitude);
		setPhone(value.phone);
		setAppointmentPhone(value.appointmentPhone);
		setEmail(value.email);
		setUrl(value.url);
		setAppointmentUrl(value.appointmentUrl);
		setHours(value.hours);
		setTypeId(value.typeId);
		setDriveThru(value.driveThru);
		setAppointmentRequired(value.appointmentRequired);
		setAcceptsThirdParty(value.acceptsThirdParty);
		setReferralRequired(value.referralRequired);
		setTestCriteriaId(value.testCriteriaId);
		setOtherTestCriteria(value.otherTestCriteria);
		setTestsPerDay(value.testsPerDay);
		setGovernmentIdRequired(value.governmentIdRequired);
		setMinimumAge(value.minimumAge);
		setDoctorReferralCriteria(value.doctorReferralCriteria);
		setFirstResponderFriendly(value.firstResponderFriendly);
		setTelescreeningAvailable(value.telescreeningAvailable);
		setAcceptsInsurance(value.acceptsInsurance);
		setInsuranceProvidersAccepted(value.insuranceProvidersAccepted);
		setFreeOrLowCost(value.freeOrLowCost);
		setCanDonatePlasma(value.canDonatePlasma);
		setNotes(value.notes);
		value.createdAt = getCreatedAt();
		setUpdatedAt(value.updatedAt = new Date());

		if (admin)
		{
			setResultNotificationEnabled(value.resultNotificationEnabled);
			setActive(value.active);
			if (value.active && (null == activatedAt)) setActivatedAt(value.activatedAt = value.updatedAt);
			else value.activatedAt = getActivatedAt();
		}
		else
		{
			value.resultNotificationEnabled = isResultNotificationEnabled();
			value.active = isActive();
			value.activatedAt = getActivatedAt();
		}

		return this;
	}

	@Transient
	public FacilityValue toValue()
	{
		return new FacilityValue(
			getId(),
			getName(),
			getAddress(),
			getCity(),
			getState(),
			getCountyId(),
			getCountyName(),
			getLatitude(),
			getLongitude(),
			getPhone(),
			getAppointmentPhone(),
			getEmail(),
			getUrl(),
			getAppointmentUrl(),
			getHours(),
			getTypeId(),
			(null != getTypeId()) ? FacilityType.get(getTypeId()) : null,
			isDriveThru(),
			isAppointmentRequired(),
			isAcceptsThirdParty(),
			isReferralRequired(),
			getTestCriteriaId(),
			(null != getTestCriteriaId()) ? TestCriteria.get(getTestCriteriaId()) : null,
			getOtherTestCriteria(),
			getTestsPerDay(),
			isGovernmentIdRequired(),
			getMinimumAge(),
			getDoctorReferralCriteria(),
			isFirstResponderFriendly(),
			isTelescreeningAvailable(),
			isAcceptsInsurance(),
			getInsuranceProvidersAccepted(),
			isFreeOrLowCost(),
			isCanDonatePlasma(),
			isResultNotificationEnabled(),
			getNotes(),
			isActive(),
			getActivatedAt(),
			getCreatedAt(),
			getUpdatedAt());
	}

	@Transient
	public FacilityValue toValueX() { return toValue().withTestTypes(toCreatedValues(getTestTypes())); }

	@Transient
	public List<CreatedValue> toCreatedValues(final List<? extends FacilityChild> values)
	{
		return (CollectionUtils.isEmpty(values)) ? null : values.stream().map(o -> o.toValue()).collect(toList());
	}
}
