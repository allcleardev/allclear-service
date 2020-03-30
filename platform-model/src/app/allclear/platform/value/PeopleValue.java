package app.allclear.platform.value;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.*;

/**********************************************************************************
*
*	Value object class that represents the people table.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 22, 2020
*
**********************************************************************************/

public class PeopleValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "people";
	public static final int MAX_LEN_ID = 10;
	public static final int MAX_LEN_NAME = 64;
	public static final int MAX_LEN_PHONE = 32;
	public static final int MAX_LEN_EMAIL = 128;
	public static final int MAX_LEN_FIRST_NAME = 32;
	public static final int MAX_LEN_LAST_NAME = 32;
	public static final int MAX_LEN_STATUS_ID = 1;
	public static final int MAX_LEN_STATURE_ID = 1;
	public static final int MAX_LEN_CONDITION_ID = 2;

	// Members
	public String id = null;
	public String name = null;
	public String phone = null;
	public String email = null;
	public String firstName = null;
	public String lastName = null;
	public Date dob = null;
	public String statusId = null;
	public PeopleStatus status = null;
	public String statureId = null;
	public PeopleStature stature = null;
	public boolean active;
	public Date authAt = null;
	public Date phoneVerifiedAt = null;
	public Date emailVerifiedAt = null;
	public Date createdAt = null;
	public Date updatedAt = null;
	public List<CreatedValue> conditions = null;

	// Mutators
	public PeopleValue withId(final String newValue) { id = newValue; return this; }
	public PeopleValue withName(final String newValue) { name = newValue; return this; }
	public PeopleValue withPhone(final String newValue) { phone = newValue; return this; }
	public PeopleValue withEmail(final String newValue) { email = newValue; return this; }
	public PeopleValue withFirstName(final String newValue) { firstName = newValue; return this; }
	public PeopleValue withLastName(final String newValue) { lastName = newValue; return this; }
	public PeopleValue withDob(final Date newValue) { dob = newValue; return this; }
	public PeopleValue withStatusId(final String newValue) { statusId = newValue; return this; }
	public PeopleValue withStatus(final PeopleStatus newValue) { status = newValue; return this; }
	public PeopleValue withStatureId(final String newValue) { statureId = newValue; return this; }
	public PeopleValue withStature(final PeopleStature newValue) { stature = newValue; return this; }
	public PeopleValue withActive(final boolean newValue) { active = newValue; return this; }
	public PeopleValue withAuthAt(final Date newValue) { authAt = newValue; return this; }
	public PeopleValue withPhoneVerifiedAt(final Date newValue) { phoneVerifiedAt = newValue; return this; }
	public PeopleValue withEmailVerifiedAt(final Date newValue) { emailVerifiedAt = newValue; return this; }
	public PeopleValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public PeopleValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }
	public PeopleValue withConditions(final List<CreatedValue> newValues) { conditions = newValues; return this; }
	public PeopleValue emptyConditions() { return withConditions(List.of()); }
	public PeopleValue nullConditions() { conditions = null; return this; }
	public PeopleValue withConditions(final Condition... newValues)
	{
		return withConditions(Arrays.asList(newValues).stream().map(v -> v.created()).collect(toList()));
	}

	public PeopleValue initDates()
	{
		this.createdAt = this.updatedAt = new Date();

		return this;
	}

	public PeopleValue() {}

	public PeopleValue(
		final String name,
		final String phone,
		final boolean active)
	{
		this(name, phone, null, null, null, null, null, null, active);
	}

	public PeopleValue(
		final String name,
		final String phone,
		final String email,
		final String firstName,
		final String lastName,
		final Date dob,
		final String statusId,
		final String statureId,
		final boolean active)
	{
		this(null, name, phone, email, firstName, lastName, dob, statusId, null, statureId, null, active, null, null, null, null, null);
	}

	public PeopleValue(final String id,
		final String name,
		final String phone,
		final String email,
		final String firstName,
		final String lastName,
		final Date dob,
		final String statusId,
		final PeopleStatus status,
		final String statureId,
		final PeopleStature stature,
		final boolean active,
		final Date authAt,
		final Date phoneVerifiedAt,
		final Date emailVerifiedAt,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.statusId = statusId;
		this.status = status;
		this.statureId = statureId;
		this.stature = stature;
		this.active = active;
		this.authAt = authAt;
		this.phoneVerifiedAt = phoneVerifiedAt;
		this.emailVerifiedAt = emailVerifiedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		id = StringUtils.trimToNull(id);
		name = StringUtils.trimToNull(name);
		phone = StringUtils.trimToNull(phone);
		email = StringUtils.trimToNull(email);
		firstName = StringUtils.trimToNull(firstName);
		lastName = StringUtils.trimToNull(lastName);
		statusId = StringUtils.trimToNull(statusId);
		statureId = StringUtils.trimToNull(statureId);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof PeopleValue)) return false;

		var v = (PeopleValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(name, v.name) &&
			Objects.equals(phone, v.phone) &&
			Objects.equals(email, v.email) &&
			Objects.equals(firstName, v.firstName) &&
			Objects.equals(lastName, v.lastName) &&
			((dob == v.dob) || DateUtils.truncatedEquals(dob, v.dob, Calendar.DATE)) &&
			Objects.equals(statusId, v.statusId) &&
			Objects.equals(statureId, v.statureId) &&
			(active == v.active) &&
			((authAt == v.authAt) || DateUtils.truncatedEquals(authAt, v.authAt, Calendar.SECOND)) &&
			((phoneVerifiedAt == v.phoneVerifiedAt) || DateUtils.truncatedEquals(phoneVerifiedAt, v.phoneVerifiedAt, Calendar.SECOND)) &&
			((emailVerifiedAt == v.emailVerifiedAt) || DateUtils.truncatedEquals(emailVerifiedAt, v.emailVerifiedAt, Calendar.SECOND)) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", phone: ").append(phone)
			.append(", email: ").append(email)
			.append(", firstName: ").append(firstName)
			.append(", lastName: ").append(lastName)
			.append(", dob: ").append(dob)
			.append(", statusId: ").append(statusId)
			.append(", status: ").append(status)
			.append(", statureId: ").append(statureId)
			.append(", stature: ").append(stature)
			.append(", active: ").append(active)
			.append(", authAt: ").append(authAt)
			.append(", phoneVerifiedAt: ").append(phoneVerifiedAt)
			.append(", emailVerifiedAt: ").append(emailVerifiedAt)
			.append(", createdAt: ").append(createdAt)
			.append(", updatedAt: ").append(updatedAt)
			.append(", conditions: ").append(conditions)
			.append(" }").toString();
	}
}
