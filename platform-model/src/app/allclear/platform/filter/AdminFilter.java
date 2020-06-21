package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for Admin query.
*
*	@author smalleyd
*	@version 1.0.14
*	@since April 1, 2020
*
*******************************************************************************************************************/

public class AdminFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public String id = null;
	public String email = null;
	public String firstName = null;
	public String lastName = null;
	public String phone = null;
	public Boolean supers = null;
	public Boolean editor = null;
	public Boolean alertable = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;

	// Mutators
	public AdminFilter withId(final String newValue) { id = newValue; return this; }
	public AdminFilter withEmail(final String newValue) { email = newValue; return this; }
	public AdminFilter withFirstName(final String newValue) { firstName = newValue; return this; }
	public AdminFilter withLastName(final String newValue) { lastName = newValue; return this; }
	public AdminFilter withPhone(final String newValue) { phone = newValue; return this; }
	public AdminFilter withSupers(final Boolean newValue) { supers = newValue; return this; }
	public AdminFilter withEditor(final Boolean newValue) { editor = newValue; return this; }
	public AdminFilter withAlertable(final Boolean newValue) { alertable = newValue; return this; }
	public AdminFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public AdminFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }
	public AdminFilter withUpdatedAtFrom(final Date newValue) { updatedAtFrom = newValue; return this; }
	public AdminFilter withUpdatedAtTo(final Date newValue) { updatedAtTo = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public AdminFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public AdminFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public AdminFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public AdminFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param email represents the "email" field.
		@param firstName represents the "first_name" field.
		@param lastName represents the "last_name" field.
		@param phone represents the "phone" field.
		@param supers represents the "supers" field.
		@param editor represents the "editor" field.
		@param alertable represents the "alertable" field.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
		@param updatedAtFrom represents the "updated_at" field - lower boundary.
		@param updatedAtTo represents the "updated_at" field - upper boundary.
	*/
	public AdminFilter(final String id,
		final String email,
		final String firstName,
		final String lastName,
		final String phone,
		final Boolean supers,
		final Boolean editor,
		final Boolean alertable,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.id = id;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.supers = supers;
		this.editor = editor;
		this.alertable = alertable;
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
	public AdminFilter clean()
	{
		id = StringUtils.trimToNull(id);
		email = StringUtils.trimToNull(email);
		firstName = StringUtils.trimToNull(firstName);
		lastName = StringUtils.trimToNull(lastName);
		phone = StringUtils.trimToNull(phone);

		return this;
	}

	/**************************************************************************
	*
	*	Object methods
	*
	**************************************************************************/

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
