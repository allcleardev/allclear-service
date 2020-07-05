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
	@NamedQuery(name="findPeopleViaFriendship", query="SELECT OBJECT(o) FROM Friendship f INNER JOIN f.friend o WHERE f.personId = :personId AND f.friendId = :friendId AND o.active = TRUE"),
	@NamedQuery(name="getActiveAlertablePeopleIdsByLongitude", query="SELECT o.id FROM People o WHERE o.id > :lastId AND o.latitude IS NOT NULL AND ((o.longitude >= :longitudeFrom) AND (o.longitude < :longitudeTo)) AND o.alertable = TRUE AND o.active = TRUE ORDER BY o.id"),
	@NamedQuery(name="getPeopleIdByEmail", query="SELECT o.id FROM People o WHERE o.email = :email"),
	@NamedQuery(name="getPeopleIdByPhone", query="SELECT o.id FROM People o WHERE o.phone = :phone")})
@NamedNativeQueries({@NamedNativeQuery(name="findActivePeopleByIdOrName", query="SELECT * FROM people o WHERE o.id LIKE :name AND o.active = TRUE UNION DISTINCT SELECT * FROM people oo WHERE oo.name LIKE :name AND oo.active = TRUE UNION DISTINCT SELECT * FROM people p WHERE p.phone LIKE :name AND p.active = TRUE UNION DISTINCT SELECT * FROM people e WHERE e.email LIKE :name AND e.active = TRUE ORDER BY name", resultClass=People.class),	// Leverages all indices with 4 SELECTs as opposed to a single SELECT with an OR conjunction. DLS on 4/28/2020.
	@NamedNativeQuery(name="getPeopleNamesByIds", query="SELECT o.id, o.name FROM people o WHERE o.id IN (:ids)", resultClass=Named.class)})
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

	@Column(name="latitude", columnDefinition="DECIMAL(12,8)", nullable=true)
	public BigDecimal getLatitude() { return latitude; }
	public BigDecimal latitude;
	public void setLatitude(final BigDecimal newValue) { latitude = newValue; }

	@Column(name="longitude", columnDefinition="DECIMAL(12,8)", nullable=true)
	public BigDecimal getLongitude() { return longitude; }
	public BigDecimal longitude;
	public void setLongitude(final BigDecimal newValue) { longitude = newValue; }

	@Column(name="location_name", columnDefinition="VARCHAR(255)", nullable=true)
	public String getLocationName() { return locationName; }
	public String locationName;
	public void setLocationName(final String newValue) { locationName = newValue; }

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

	@Column(name="alerted_of", columnDefinition="INT", nullable=true)
	public Integer getAlertedOf() { return alertedOf; }
	public Integer alertedOf;
	public void setAlertedOf(final Integer newValue) { alertedOf = newValue; }

	@Column(name="alerted_at", columnDefinition="DATETIME", nullable=true)
	public Date getAlertedAt() { return alertedAt; }
	public Date alertedAt;
	public void setAlertedAt(final Date newValue) { alertedAt = newValue; }
	@Transient public Date alertedAt() { return (null != alertedAt) ? alertedAt : authAt; }	// ALLCLEAR-248: assume the last authentication included a facility search. We just need a date in the near past to limit the facility search. DLS on 4/15/2020.

	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@Column(name="updated_at", columnDefinition="DATETIME", nullable=false)
	public Date getUpdatedAt() { return updatedAt; }
	public Date updatedAt;
	public void setUpdatedAt(final Date newValue) { updatedAt = newValue; }

	@OneToOne(cascade={CascadeType.REMOVE}, fetch=FetchType.EAGER, mappedBy="person")
	public PeopleField getField() { return field; }
	public PeopleField field;
	public void setField(final PeopleField newValue) { field = newValue; }

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

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="person")
	public List<Tests> getTests() { return tests; }
	public List<Tests> tests;
	public void setTests(final List<Tests> newValues) { tests = newValues; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="person")	// Friend requests
	public List<Friend> getFriends() { return friends; }
	public List<Friend> friends;
	public void setFriends(final List<Friend> newValues) { friends = newValues; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="invitee")	// Needed for cascading deletes.
	public List<Friend> getInvitees() { return invitees; }
	public List<Friend> invitees;
	public void setInvitees(final List<Friend> newValues) { invitees = newValues; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="person")	// Persistent friendships after acceptance of the requests.
	public List<Friendship> getFriendships() { return friendships; }
	public List<Friendship> friendships;
	public void setFriendships(final List<Friendship> newValues) { friendships = newValues; }

	@OneToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY, mappedBy="friend")	// Othersize of the friendship. Needed for cascading deletes.
	public List<Friendship> getOthers() { return others; }
	public List<Friendship> others;
	public void setOthers(final List<Friendship> newValues) { others = newValues; }

	@ManyToMany(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY)
	@JoinTable(name="people_facility", joinColumns=@JoinColumn(name="person_id"), inverseJoinColumns=@JoinColumn(name="facility_id"))
	public List<Facility> getFacilities() { return facilities; }
	public List<Facility> facilities;
	public void setFacilities(final List<Facility> newValues) { facilities = newValues; }

	public People() {}

	/** For test */
	public People(final String id, final BigDecimal latitude, final BigDecimal longitude, final boolean alertable, final boolean active)
	{
		this.id = this.name = this.phone = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.healthWorkerStatusId = HealthWorkerStatus.NEITHER.id;
		this.alertable = alertable;
		this.active = active;
		this.createdAt = this.updatedAt = new Date();
	}

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
		final String locationName,
		final boolean alertable,
		final boolean active,
		final Date authAt,
		final Date phoneVerifiedAt,
		final Date emailVerifiedAt,
		final Integer alertedOf,
		final Date alertedAt,
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
		this.locationName = locationName;
		this.alertable = alertable;
		this.active = active;
		this.authAt = authAt;
		this.phoneVerifiedAt = phoneVerifiedAt;
		this.emailVerifiedAt = emailVerifiedAt;
		this.alertedOf = alertedOf;
		this.alertedAt = alertedAt;
		this.createdAt = this.updatedAt = createdAt;
	}

	public People(final PeopleValue value)
	{
		this(value.id, value.name, value.phone, value.email,
			value.firstName, value.lastName, value.dob,
			value.statusId, value.statureId, value.sexId,
			value.healthWorkerStatusId,
			value.latitude, value.longitude, value.locationName,
			value.alertable, value.active, value.authAt, value.phoneVerifiedAt,
			value.emailVerifiedAt, value.alertedOf, value.alertedAt, value.initDates());
	}

	public People update(final PeopleValue value, final boolean admin)
	{
		setName(value.name);
		setPhone(value.phone);
		setEmail(value.email);
		setFirstName(value.firstName);
		setLastName(value.lastName);
		setDob(value.dob);
		setStatusId(value.statusId);
		setSexId(value.sexId);
		setHealthWorkerStatusId(value.healthWorkerStatusId);
		setLatitude(value.latitude);
		setLongitude(value.longitude);
		setLocationName(value.locationName);
		setAlertable(value.alertable);
		if (admin)
		{
			setActive(value.active);
			setAuthAt(value.authAt);
			setStatureId(value.statureId);	// Users canNOT set their own stature. That's an admin function. DLS on 4/10/2020.
			setPhoneVerifiedAt(value.phoneVerifiedAt);
			setEmailVerifiedAt(value.emailVerifiedAt);
			setAlertedOf(value.alertedOf);
			setAlertedAt(value.alertedAt);
		}
		else
		{
			value.active = isActive();
			value.authAt = getAuthAt();
			value.phoneVerifiedAt = getPhoneVerifiedAt();
			value.emailVerifiedAt = getEmailVerifiedAt();
			value.alertedOf = getAlertedOf();
			value.alertedAt = getAlertedAt();

			if (null != (value.statureId = getStatureId())) value.stature = Stature.get(value.statureId);
		}
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
			Objects.equals(locationName, v.locationName) &&
			(alertable == v.alertable) &&
			(active == v.active) &&
			DateUtils.truncatedEquals(authAt, v.authAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(phoneVerifiedAt, v.phoneVerifiedAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(emailVerifiedAt, v.emailVerifiedAt, Calendar.SECOND) &&
			Objects.equals(alertedOf, v.alertedOf) &&
			DateUtils.truncatedEquals(alertedAt, v.alertedAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override public int hashCode() { return Objects.hashCode(id); }

	@Transient
	public People auth()
	{
		active = true;
		updatedAt = authAt = new Date();
		if (null == phoneVerifiedAt) phoneVerifiedAt = updatedAt;

		return this;
	}

	@Transient
	public PeopleValue toValue(final Visibility who)
	{
		return toValue(who, getField());
	}

	@Transient
	private PeopleValue toValue(final Visibility who, final PeopleField fields)
	{
		var available = fields.visibilityHealthWorkerStatusId().available(who);

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
			(null != getStatureId()) ? Stature.get(getStatureId()) : null,
			getSexId(),
			(null != getSexId()) ? Sex.get(getSexId()) : null,
			available ? getHealthWorkerStatusId() : null,
			available? HealthWorkerStatus.get(getHealthWorkerStatusId()) : null,
			getLatitude(),
			getLongitude(),
			getLocationName(),
			isAlertable(),
			isActive(),
			getAuthAt(),
			getPhoneVerifiedAt(),
			getEmailVerifiedAt(),
			getAlertedOf(),
			getAlertedAt(),
			getCreatedAt(),
			getUpdatedAt());
	}

	@Transient
	public PeopleValue toValueX(final Visibility who)
	{
		var fields = getField();
		var facilities_ = CollectionUtils.isEmpty(getFacilities()) ? null : getFacilities().stream().map(o -> o.toValue().withFavorite(true)).collect(toList());

		var v = toValue(who, fields);
		if (fields.visibilityConditions().available(who)) v.withConditions(toCreatedValues(getConditions()));
		if (fields.visibilityExposures().available(who)) v.withExposures(toCreatedValues(getExposures()));
		if (fields.visibilitySymptoms().available(who)) v.withSymptoms(toCreatedValues(getSymptoms()));

		return v.withFacilities(facilities_);
	}

	@Transient
	public List<CreatedValue> toCreatedValues(final List<? extends PeopleChild> values)
	{
		return (CollectionUtils.isEmpty(values)) ? null : values.stream().map(o -> o.toValue()).collect(toList());
	}
}
