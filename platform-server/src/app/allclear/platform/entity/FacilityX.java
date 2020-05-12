package app.allclear.platform.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

import app.allclear.platform.type.FacilityType;
import app.allclear.platform.type.TestCriteria;
import app.allclear.platform.value.FacilityValue;

/** Extends the Facility entity to include the calulated DISTANCE field.
 * 
 * @author smalleyd
 * @version 1.0.33
 * @since 4/3/2020
 *
 */

@Entity
@NamedNativeQueries(@NamedNativeQuery(name="findActiveFacilitiesByNameAndDistance",
	query="SELECT o.*, ST_DISTANCE_SPHERE(POINT(o.longitude, o.latitude), POINT(:longitude, :latitude)) AS meters FROM facility o WHERE o.name LIKE :name AND o.active = TRUE AND ST_DISTANCE_SPHERE(POINT(o.longitude, o.latitude), POINT(:longitude, :latitude)) <= :meters ORDER BY meters",
	resultClass=FacilityX.class))
public class FacilityX implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Column(name="id", columnDefinition="BIGINT", nullable=false) @Id public Long id;
	@Column(name="name", columnDefinition="VARCHAR(128)", nullable=false) public String name;
	@Column(name="address", columnDefinition="VARCHAR(128)", nullable=false) public String address;
	@Column(name="city", columnDefinition="VARCHAR(128)", nullable=false) public String city;
	@Column(name="state", columnDefinition="VARCHAR(128)", nullable=false) public String state;
	@Column(name="latitude", columnDefinition="DECIMAL(12,8)", nullable=false) public BigDecimal latitude;
	@Column(name="longitude", columnDefinition="DECIMAL(12,8)", nullable=false) public BigDecimal longitude;
	@Column(name="phone", columnDefinition="VARCHAR(32)", nullable=true) public String phone;
	@Column(name="appointment_phone", columnDefinition="VARCHAR(32)", nullable=true) public String appointmentPhone;
	@Column(name="email", columnDefinition="VARCHAR(128)", nullable=true) public String email;
	@Column(name="url", columnDefinition="VARCHAR(128)", nullable=true) public String url;
	@Column(name="appointment_url", columnDefinition="VARCHAR(128)", nullable=true) public String appointmentUrl;
	@Column(name="hours", columnDefinition="TEXT", nullable=true) public String hours;
	@Column(name="type_id", columnDefinition="CHAR(2)", nullable=true) public String typeId;
	@Column(name="drive_thru", columnDefinition="BIT", nullable=false) public boolean driveThru;
	@Column(name="appointment_required", columnDefinition="BIT", nullable=true) public Boolean appointmentRequired;
	@Column(name="accepts_third_party", columnDefinition="BIT", nullable=true) public Boolean acceptsThirdParty;
	@Column(name="referral_required", columnDefinition="BIT", nullable=false) public boolean referralRequired;
	@Column(name="test_criteria_id", columnDefinition="CHAR(2)", nullable=true) public String testCriteriaId;
	@Column(name="other_test_criteria", columnDefinition="TEXT", nullable=true) public String otherTestCriteria;
	@Column(name="tests_per_day", columnDefinition="INT", nullable=true) public Integer testsPerDay;
	@Column(name="government_id_required", columnDefinition="BIT", nullable=false) public boolean governmentIdRequired;
	@Column(name="minimum_age", columnDefinition="INT", nullable=true) public Integer minimumAge;
	@Column(name="doctor_referral_criteria", columnDefinition="TEXT", nullable=true) public String doctorReferralCriteria;
	@Column(name="first_responder_friendly", columnDefinition="BIT", nullable=false) public boolean firstResponderFriendly;
	@Column(name="telescreening_available", columnDefinition="BIT", nullable=false) public boolean telescreeningAvailable;
	@Column(name="accepts_insurance", columnDefinition="BIT", nullable=false) public boolean acceptsInsurance;
	@Column(name="insurance_providers_accepted", columnDefinition="TEXT", nullable=true) public String insuranceProvidersAccepted;
	@Column(name="free_or_low_cost", columnDefinition="BIT", nullable=false) public boolean freeOrLowCost;
	@Column(name="notes", columnDefinition="TEXT", nullable=true) public String notes;
	@Column(name="active", columnDefinition="BIT", nullable=false) public boolean active;
	@Column(name="activated_at", columnDefinition="DATETIME", nullable=true) public Date activatedAt;
	@Column(name="created_at", columnDefinition="DATETIME", nullable=false) public Date createdAt;
	@Column(name="updated_at", columnDefinition="DATETIME", nullable=false) public Date updatedAt;
	@Column(name="meters", columnDefinition="BIGINT", nullable=false) public long meters;

	@Transient
	public FacilityValue toValue()
	{
		return new FacilityValue(
			id,
			name,
			address,
			city,
			state,
			latitude,
			longitude,
			phone,
			appointmentPhone,
			email,
			url,
			appointmentUrl,
			hours,
			typeId,
			(null != typeId) ? FacilityType.get(typeId) : null,
			driveThru,
			appointmentRequired,
			acceptsThirdParty,
			referralRequired,
			testCriteriaId,
			(null != testCriteriaId) ? TestCriteria.get(testCriteriaId) : null,
			otherTestCriteria,
			testsPerDay,
			governmentIdRequired,
			minimumAge,
			doctorReferralCriteria,
			firstResponderFriendly,
			telescreeningAvailable,
			acceptsInsurance,
			insuranceProvidersAccepted,
			freeOrLowCost,
			notes,
			active,
			activatedAt,
			createdAt,
			updatedAt).withMeters(meters);
	}
}
