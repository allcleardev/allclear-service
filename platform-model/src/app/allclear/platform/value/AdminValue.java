package app.allclear.platform.value;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.time.DateUtils;

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

	public String id = null;
	public String password = null;
	public String email = null;
	public String firstName = null;
	public String lastName = null;
	public boolean supers = false;
	public Date createdAt = null;
	public Date updatedAt = null;

	public AdminValue withId(final String newValue) { id = newValue; return this; }
	public AdminValue withPassword(final String newValue) { password = newValue; return this; }
	public AdminValue withEmail(final String newValue) { email = newValue; return this; }
	public AdminValue withFirstName(final String newValue) { firstName = newValue; return this; }
	public AdminValue withLastName(final String newValue) { lastName = newValue; return this; }
	public AdminValue withSupers(final boolean newValue) { supers = newValue; return this; }
	public AdminValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public AdminValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }

	public AdminValue() { super(); }
	public AdminValue(final String id) { this(id, false); }
	public AdminValue(final String id, final boolean supers) { this(id, null, null, null, null, supers); }

	public AdminValue(final String id,
		final String password,
		final String email,
		final String firstName,
		final String lastName,
		final boolean supers)
	{
		this(id, password, email, firstName, lastName, supers, null, null);
	}

	public AdminValue(final String id,
		final String password,
		final String email,
		final String firstName,
		final String lastName,
		final boolean supers,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.password = password;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.supers = supers;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
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
			supers == v.supers &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", email: ").append(email)
			.append(", firstName: ").append(firstName)
			.append(", lastName: ").append(lastName)
			.append(", supers: ").append(supers)
			.append(", createdAt: ").append(createdAt)
			.append(", updatedAt: ").append(updatedAt)
			.append(" }").toString();
	}
}
