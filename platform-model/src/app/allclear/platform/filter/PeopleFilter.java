package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for People query.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 22, 2020
*
*******************************************************************************************************************/

public class PeopleFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public String id = null;
	public String name = null;
	public String phone = null;
	public String email = null;
	public String firstName = null;
	public String lastName = null;
	public Date dob = null;
	public Date dobFrom = null;
	public Date dobTo = null;
	public String statusId = null;
	public String statureId = null;
	public Boolean active = null;
	public Date authAtFrom = null;
	public Date authAtTo = null;
	public Date phoneVerifiedAtFrom = null;
	public Date phoneVerifiedAtTo = null;
	public Date emailVerifiedAtFrom = null;
	public Date emailVerifiedAtTo = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;

	// Mutators
	public PeopleFilter withId(final String newValue) { id = newValue; return this; }
	public PeopleFilter withName(final String newValue) { name = newValue; return this; }
	public PeopleFilter withPhone(final String newValue) { phone = newValue; return this; }
	public PeopleFilter withEmail(final String newValue) { email = newValue; return this; }
	public PeopleFilter withFirstName(final String newValue) { firstName = newValue; return this; }
	public PeopleFilter withLastName(final String newValue) { lastName = newValue; return this; }
	public PeopleFilter withDob(final Date newValue) { dob = newValue; return this; }
	public PeopleFilter withDobFrom(final Date newValue) { dobFrom = newValue; return this; }
	public PeopleFilter withDobTo(final Date newValue) { dobTo = newValue; return this; }
	public PeopleFilter withStatusId(final String newValue) { statusId = newValue; return this; }
	public PeopleFilter withStatureId(final String newValue) { statureId = newValue; return this; }
	public PeopleFilter withActive(final Boolean newValue) { active = newValue; return this; }
	public PeopleFilter withAuthAtFrom(final Date newValue) { authAtFrom = newValue; return this; }
	public PeopleFilter withAuthAtTo(final Date newValue) { authAtTo = newValue; return this; }
	public PeopleFilter withPhoneVerifiedAtFrom(final Date newValue) { phoneVerifiedAtFrom = newValue; return this; }
	public PeopleFilter withPhoneVerifiedAtTo(final Date newValue) { phoneVerifiedAtTo = newValue; return this; }
	public PeopleFilter withEmailVerifiedAtFrom(final Date newValue) { emailVerifiedAtFrom = newValue; return this; }
	public PeopleFilter withEmailVerifiedAtTo(final Date newValue) { emailVerifiedAtTo = newValue; return this; }
	public PeopleFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public PeopleFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }
	public PeopleFilter withUpdatedAtFrom(final Date newValue) { updatedAtFrom = newValue; return this; }
	public PeopleFilter withUpdatedAtTo(final Date newValue) { updatedAtTo = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public PeopleFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public PeopleFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public PeopleFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public PeopleFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param name represents the "name" field.
		@param phone represents the "phone" field.
		@param email represents the "email" field.
		@param firstName represents the "first_name" field.
		@param lastName represents the "last_name" field.
		@param dob represents the "dob" field.
		@param dobFrom represents the "dob" field - lower boundary.
		@param dobTo represents the "dob" field - upper boundary.
		@param statusId represents the "status_id" field.
		@param statureId represents the "stature_id" field.
		@param active represents the "active" field.
		@param authAtFrom represents the "auth_at" field - lower boundary.
		@param authAtTo represents the "auth_at" field - upper boundary.
		@param phoneVerifiedAtFrom represents the "phone_verified_at" field - lower boundary.
		@param phoneVerifiedAtTo represents the "phone_verified_at" field - upper boundary.
		@param emailVerifiedAtFrom represents the "email_verified_at" field - lower boundary.
		@param emailVerifiedAtTo represents the "email_verified_at" field - upper boundary.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
		@param updatedAtFrom represents the "updated_at" field - lower boundary.
		@param updatedAtTo represents the "updated_at" field - upper boundary.
	*/
	public PeopleFilter(final String id,
		final String name,
		final String phone,
		final String email,
		final String firstName,
		final String lastName,
		final Date dob,
		final Date dobFrom,
		final Date dobTo,
		final String statusId,
		final String statureId,
		final Boolean active,
		final Date authAtFrom,
		final Date authAtTo,
		final Date phoneVerifiedAtFrom,
		final Date phoneVerifiedAtTo,
		final Date emailVerifiedAtFrom,
		final Date emailVerifiedAtTo,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dob = dob;
		this.dobFrom = dobFrom;
		this.dobTo = dobTo;
		this.statusId = statusId;
		this.statureId = statureId;
		this.active = active;
		this.authAtFrom = authAtFrom;
		this.authAtTo = authAtTo;
		this.phoneVerifiedAtFrom = phoneVerifiedAtFrom;
		this.phoneVerifiedAtTo = phoneVerifiedAtTo;
		this.emailVerifiedAtFrom = emailVerifiedAtFrom;
		this.emailVerifiedAtTo = emailVerifiedAtTo;
		this.createdAtFrom = createdAtFrom;
		this.createdAtTo = createdAtTo;
		this.updatedAtFrom = updatedAtFrom;
		this.updatedAtTo = updatedAtTo;
	}

	/**************************************************************************
	*
	*	Helper methods
	*
	**************************************************************************/

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public PeopleFilter clean()
	{
		id = StringUtils.trimToNull(id);
		name = StringUtils.trimToNull(name);
		phone = StringUtils.trimToNull(phone);
		email = StringUtils.trimToNull(email);
		firstName = StringUtils.trimToNull(firstName);
		lastName = StringUtils.trimToNull(lastName);
		statusId = StringUtils.trimToNull(statusId);
		statureId = StringUtils.trimToNull(statureId);

		return this;
	}

	/**************************************************************************
	*
	*	Object methods
	*
	**************************************************************************/

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
			.append(", dobFrom: ").append(dobFrom)
			.append(", dobTo: ").append(dobTo)
			.append(", statusId: ").append(statusId)
			.append(", statureId: ").append(statureId)
			.append(", active: ").append(active)
			.append(", authAtFrom: ").append(authAtFrom)
			.append(", authAtTo: ").append(authAtTo)
			.append(", phoneVerifiedAtFrom: ").append(phoneVerifiedAtFrom)
			.append(", phoneVerifiedAtTo: ").append(phoneVerifiedAtTo)
			.append(", emailVerifiedAtFrom: ").append(emailVerifiedAtFrom)
			.append(", emailVerifiedAtTo: ").append(emailVerifiedAtTo)
			.append(", createdAtFrom: ").append(createdAtFrom)
			.append(", createdAtTo: ").append(createdAtTo)
			.append(", updatedAtFrom: ").append(updatedAtFrom)
			.append(", updatedAtTo: ").append(updatedAtTo)
			.append(" }").toString();
	}
}
