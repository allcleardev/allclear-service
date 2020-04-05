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
import app.allclear.platform.type.*;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the people table.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 22, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="people",
	uniqueConstraints={@UniqueConstraint(columnNames="name"), @UniqueConstraint(columnNames="phone"), @UniqueConstraint(columnNames="email")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="people")
@NamedQueries({@NamedQuery(name="existsPeople", query="SELECT o.id FROM People o WHERE o.id = :id"),
	@NamedQuery(name="findPeople", query="SELECT OBJECT(o) FROM People o WHERE o.name = :name"),
	@NamedQuery(name="findPeopleByEmail", query="SELECT OBJECT(o) FROM People o WHERE o.email = :email"),
	@NamedQuery(name="findPeopleByPhone", query="SELECT OBJECT(o) FROM People o WHERE o.phone = :phone"),
	@NamedQuery(name="findActivePeopleByIdOrName", query="SELECT OBJECT(o) FROM People o WHERE ((o.id LIKE :name) OR (o.name LIKE :name)) AND o.active = TRUE ORDER BY o.name"),
	@NamedQuery(name="getPeopleIdByEmail", query="SELECT o.id FROM People o WHERE o.email = :email"),
	@NamedQuery(name="getPeopleIdByPhone", query="SELECT o.id FROM People o WHERE o.phone = :phone")})
public class People implements Serializable
{
	private final static long serialVersionUID = 1L;

	@Column(name="id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getId() { return id; }
	public String id;
	public void setId(final String newValue) { id = newValue; }

	@Column(name="name", columnDefinition="VARCHAR(64)", nullable=false)
	public String getName() { return name; }
	public String name;
	public void setName(final String newValue) { name = newValue; }

	@Column(name="phone", columnDefinition="VARCHAR(32)", nullable=false)
	public String getPhone() { return phone; }
	public String phone;
	public void setPhone(final String newValue) { phone = newValue; }

	@Column(name="email", columnDefinition="VARCHAR(128)", nullable=true)
	public String getEmail() { return email; }
	public String email;
	public void setEmail(final String newValue) { email = newValue; }

	@Column(name="first_name", columnDefinition="VARCHAR(32)", nullable=true)
	public String getFirstName() { return firstName; }
	public String firstName;
	public void setFirstName(final String newValue) { firstName = newValue; }

	@Column(name="last_name", columnDefinition="VARCHAR(32)", nullable=true)
	public String getLastName() { return lastName; }
	public String lastName;
	public void setLastName(final String newValue) { lastName = newValue; }

	@Column(name="dob", columnDefinition="DATE", nullable=true)
	public Date getDob() { return dob; }
	public Date dob;
	public void setDob(final Date newValue) { dob = newValue; }

	@Column(name="status_id", columnDefinition="CHAR(1)", nullable=true)
	public String getStatusId() { return statusId; }
	public String statusId;
	public void setStatusId(final String newValue) { statusId = newValue; }

	@Column(name="stature_id", columnDefinition="CHAR(1)", nullable=true)
	public String getStatureId() { return statureId; }
	public String statureId;
	public void setStatureId(final String newValue) { statureId = newValue; }

	@Column(name="sex_id", columnDefinition="CHAR(1)", nullable=true)
	public String getSexId() { return sexId; }
	public String sexId;
	public void setSexId(final String newValue) { sexId = newValue; }

	@Column(name="health_worker_status_id", columnDefinition="CHAR(1)", nullable=true)
	public String getHealthWorkerStatusId() { return healthWorkerStatusId; }
	public String healthWorkerStatusId;
	public void setHealthWorkerStatusId(final String newValue) { healthWorkerStatusId = newValue; }

	@Column(name="latitude", columnDefinition="DECIMAL(12, 5)", nullable=true)
	public BigDecimal getLatitude() { return latitude; }
	public BigDecimal latitude;
	public void setLatitude(final BigDecimal newValue) { latitude = newValue; }

	@Column(name="longitude", columnDefinition="DECIMAL(12, 5)", nullable=true)
	public BigDecimal getLongitude() { return longitude; }
	public BigDecimal longitude;
	public void setLongitude(final BigDecimal newValue) { longitude = newValue; }

	@Column(name="alertable", columnDefinition="BIT", nullable=false)
	public boolean isAlertable() { return alertable; }
	public boolean alertable;
	public void setAlertable(final boolean newValue) { alertable = newValue; }

	@Column(name="active", columnDefinition="BIT", nullable=false)
	public boolean isActive() { return active; }
	public boolean active;
	public void setActive(final boolean newValue) { active = newValue; }

	@Column(name="auth_at", columnDefinition="DATETIME", nullable=true)
	public Date getAuthAt() { return authAt; }
	public Date authAt;
	public void setAuthAt(final Date newValue) { authAt = newValue; }

	@Column(name="phone_verified_at", columnDefinition="DATETIME", nullable=true)
	public Date getPhoneVerifiedAt() { return phoneVerifiedAt; }
	public Date phoneVerifiedAt;
	public void setPhoneVerifiedAt(final Date newValue) { phoneVerifiedAt = newValue; }

	@Column(name="email_verified_at", columnDefinition="DATETIME", nullable=true)
	public Date getEmailVerifiedAt() { return emailVerifiedAt; }
	public Date emailVerifiedAt;
	public void setEmailVerifiedAt(final Date newValue) { emailVerifiedAt = newValue; }

	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@Column(name="updated_at", columnDefinition="DATETIME", nullable=false)
	public Date getUpdatedAt() { return updatedAt; }
	public Date updatedAt;
	public void setUpdatedAt(final Date newValue) { updatedAt = newValue; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="person")
	public List<Conditions> getConditions() { return conditions; }
	public List<Conditions> conditions;
	public void setConditions(final List<Conditions> newValues) { conditions = newValues; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="person")
	public List<Exposures> getExposures() { return exposures; }
	public List<Exposures> exposures;
	public void setExposures(final List<Exposures> newValues) { exposures = newValues; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="person")
	public List<Symptoms> getSymptoms() { return symptoms; }
	public List<Symptoms> symptoms;
	public void setSymptoms(final List<Symptoms> newValues) { symptoms = newValues; }

	public People() {}

	public People(final String id,
		final String name,
		final String phone,
		final String email,
		final String firstName,
		final String lastName,
		final Date dob,
		final String statusId,
		final String statureId,
		final String sexId,
		final String healthWorkerStatusId,
		final BigDecimal latitude,
		final BigDecimal longitude,
		final boolean alertable,
		final boolean active,
		final Date authAt,
		final Date phoneVerifiedAt,
		final Date emailVerifiedAt,
		final Date createdAt)
	{
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.statusId = statusId;
		this.statureId = statureId;
		this.sexId = sexId;
		this.healthWorkerStatusId = healthWorkerStatusId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.alertable = alertable;
		this.active = active;
		this.authAt = authAt;
		this.phoneVerifiedAt = phoneVerifiedAt;
		this.emailVerifiedAt = emailVerifiedAt;
		this.createdAt = this.updatedAt = createdAt;
	}

	public People(final PeopleValue value)
	{
		this(value.id, value.name, value.phone, value.email,
			value.firstName, value.lastName, value.dob,
			value.statusId, value.statureId, value.sexId,
			value.healthWorkerStatusId,
			value.latitude, value.longitude, value.alertable,
			value.active, value.authAt, value.phoneVerifiedAt,
			value.emailVerifiedAt, value.initDates());
	}

	public People update(final PeopleValue value)
	{
		setName(value.name);
		setPhone(value.phone);
		setEmail(value.email);
		setFirstName(value.firstName);
		setLastName(value.lastName);
		setDob(value.dob);
		setStatusId(value.statusId);
		setStatureId(value.statureId);
		setSexId(value.sexId);
		setHealthWorkerStatusId(value.healthWorkerStatusId);
		setLatitude(value.latitude);
		setLongitude(value.longitude);
		setAlertable(value.alertable);
		setActive(value.active);
		setAuthAt(value.authAt);
		setPhoneVerifiedAt(value.phoneVerifiedAt);
		setEmailVerifiedAt(value.emailVerifiedAt);
		value.createdAt = getCreatedAt();
		setUpdatedAt(value.updatedAt = new Date());

		return this;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof People)) return false;

		var v = (People) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(name, v.name) &&
			Objects.equals(phone, v.phone) &&
			Objects.equals(email, v.email) &&
			Objects.equals(firstName, v.firstName) &&
			Objects.equals(lastName, v.lastName) &&
			DateUtils.truncatedEquals(dob, v.dob, Calendar.SECOND) &&
			Objects.equals(statusId, v.statusId) &&
			Objects.equals(statureId, v.statureId) &&
			Objects.equals(sexId, v.sexId) &&
			Objects.equals(healthWorkerStatusId, v.healthWorkerStatusId) &&
			Objects.equals(latitude, v.latitude) &&
			Objects.equals(longitude, v.longitude) &&
			(alertable == v.alertable) &&
			(active == v.active) &&
			DateUtils.truncatedEquals(authAt, v.authAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(phoneVerifiedAt, v.phoneVerifiedAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(emailVerifiedAt, v.emailVerifiedAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Transient
	public People auth()
	{
		active = true;
		updatedAt = authAt = new Date();
		if (null == phoneVerifiedAt) phoneVerifiedAt = updatedAt;

		return this;
	}

	@Transient
	public PeopleValue toValue()
	{
		return new PeopleValue(
			getId(),
			getName(),
			getPhone(),
			getEmail(),
			getFirstName(),
			getLastName(),
			getDob(),
			getStatusId(),
			(null != getStatusId()) ? PeopleStatus.get(getStatusId()) : null,
			getStatureId(),
			(null != getStatureId()) ? PeopleStature.get(getStatureId()) : null,
			getSexId(),
			(null != getSexId()) ? Sex.get(getSexId()) : null,
			getHealthWorkerStatusId(),
			(null != getHealthWorkerStatusId()) ? HealthWorkerStatus.get(getHealthWorkerStatusId()) : null,
			getLatitude(),
			getLongitude(),
			isAlertable(),
			isActive(),
			getAuthAt(),
			getPhoneVerifiedAt(),
			getEmailVerifiedAt(),
			getCreatedAt(),
			getUpdatedAt());
	}

	@Transient
	public PeopleValue toValueX()
	{
		return toValue().withConditions(toCreatedValues(getConditions()))
			.withExposures(toCreatedValues(getExposures()))
			.withSymptoms(toCreatedValues(getSymptoms()));
	}

	@Transient
	public List<CreatedValue> toCreatedValues(final List<? extends PeopleChild> values)
	{
		return (CollectionUtils.isEmpty(values)) ? null : values.stream().map(o -> o.toValue()).collect(toList());
	}
}
