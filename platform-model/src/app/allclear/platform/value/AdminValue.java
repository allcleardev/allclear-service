package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.twilio.model.TwilioUtils;

/** Value object that represents an internal administrator.
 * 
 * @author smalleyd
 * @version 1.0.9
 * @since 3/31/2020
 *
 */

public class AdminValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "admin";
	public static final int MAX_LEN_ID = 64;
	public static final int MAX_LEN_PASSWORD = 40;	// Leave long enough for a UUID.
	public static final int MIN_LEN_PASSWORD = 8;
	public static final int MAX_LEN_EMAIL = 128;
	public static final int MAX_LEN_FIRST_NAME = 32;
	public static final int MAX_LEN_LAST_NAME = 32;
	public static final int MAX_LEN_PHONE = 32;

	// Members
	public String id = null;
	public String password = null;
	public String email = null;
	public String firstName = null;
	public String lastName = null;
	public String phone = null;
	public boolean supers = false;
	public boolean editor = false;
	public boolean alertable = false;
	public Date createdAt = null;
	public Date updatedAt = null;

	// Accessors
	public boolean canAdmin() { return !editor || supers; }	// Admin accounts marked as Editors lose standard 'admin' access UNLESS also marked as Supers. DLS on 4/30/2020.
	public String type() { return (supers ? "Super" : (editor ? "Editor" : "Admin")); }

	// Mutators
	public AdminValue withId(final String newValue) { id = newValue; return this; }
	public AdminValue withPassword(final String newValue) { password = newValue; return this; }
	public AdminValue withEmail(final String newValue) { email = newValue; return this; }
	public AdminValue withFirstName(final String newValue) { firstName = newValue; return this; }
	public AdminValue withLastName(final String newValue) { lastName = newValue; return this; }
	public AdminValue withPhone(final String newValue) { phone = newValue; return this; }
	public AdminValue withSupers(final boolean newValue) { supers = newValue; return this; }
	public AdminValue withEditor(final boolean newValue) { editor = newValue; return this; }
	public AdminValue withAlertable(final boolean newValue) { alertable = newValue; return this; }
	public AdminValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public AdminValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }

	public AdminValue() { super(); }
	public AdminValue(final String id) { this(id, false); }
	public AdminValue(final String id, final boolean supers) { this(id, supers, false); }
	public AdminValue(final String id, final boolean supers, final boolean editor) { this(id, null, null, null, null, null, supers, editor, false); }

	public AdminValue(final String id,
		final String password,
		final String email,
		final String firstName,
		final String lastName,
		final String phone,
		final boolean supers,
		final boolean editor,
		final boolean alertable)
	{
		this(id, password, email, firstName, lastName, phone, supers, editor, alertable, null, null);
	}

	public AdminValue(final String id,
		final String password,
		final String email,
		final String firstName,
		final String lastName,
		final String phone,
		final boolean supers,
		final boolean editor,
		final boolean alertable,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.password = password;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.supers = supers;
		this.editor = editor;
		this.alertable = alertable;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		id = StringUtils.trimToNull(id);
		password = StringUtils.trimToNull(password);
		email = StringUtils.trimToNull(email);
		firstName = StringUtils.trimToNull(firstName);
		lastName = StringUtils.trimToNull(lastName);
		phone = StringUtils.trimToNull(TwilioUtils.normalize(phone));
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof AdminValue)) return false;

		var v = (AdminValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(email, v.email) &&
			Objects.equals(firstName, v.firstName) &&
			Objects.equals(lastName, v.lastName) &&
			Objects.equals(phone, v.phone) &&
			supers == v.supers &&
			editor == v.editor &&
			alertable == v.alertable &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public int hashCode() { return Objects.hashCode(id); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
