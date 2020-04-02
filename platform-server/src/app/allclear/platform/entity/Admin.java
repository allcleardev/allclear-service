package app.allclear.platform.entity;

import static app.allclear.common.crypto.Hasher.saltAndHash;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.time.DateUtils;

import com.microsoft.azure.storage.table.TableServiceEntity;

import app.allclear.platform.value.AdminValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the admin table.
*
*	@author smalleyd
*	@version 1.0.14
*	@since April 1, 2020
*
**********************************************************************************/

public class Admin extends TableServiceEntity implements Serializable
{
	private final static long serialVersionUID = 1L;

	public static final String PARTITION = "ADMIN";

	public String id() { return getRowKey(); }

	public String getPassword() { return password; }
	public String password;
	public void setPassword(final String newValue) { password = newValue; }

	public String getEmail() { return email; }
	public String email;
	public void setEmail(final String newValue) { email = newValue; }

	public String getFirstName() { return firstName; }
	public String firstName;
	public void setFirstName(final String newValue) { firstName = newValue; }

	public String getLastName() { return lastName; }
	public String lastName;
	public void setLastName(final String newValue) { lastName = newValue; }

	public boolean getSupers() { return supers; }
	public boolean supers;
	public void setSupers(final boolean newValue) { supers = newValue; }

	public long getIdentifier() { return identifier; }
	public long identifier;
	public void setIdentifier(final long newValue) { identifier = newValue; }

	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	public Date getUpdatedAt() { return updatedAt; }
	public Date updatedAt;
	public void setUpdatedAt(final Date newValue) { updatedAt = newValue; }

	public Admin() {}

	public Admin(final AdminValue value)
	{
		super(PARTITION, value.id);

		var now = new Date();
		this.identifier = now.getTime();
		this.password = saltAndHash(identifier, value.password);
		this.email = value.email;
		this.firstName = value.firstName;
		this.lastName = value.lastName;
		this.supers = value.supers;
		this.createdAt = this.updatedAt = value.createdAt = value.updatedAt = now;

		value.password = null;	// Do NOT reflect back the user password.
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Admin)) return false;

		var v = (Admin) o;
		return Objects.equals(id(), v.id()) &&
			Objects.equals(password, v.password) &&
			Objects.equals(email, v.email) &&
			Objects.equals(firstName, v.firstName) &&
			Objects.equals(lastName, v.lastName) &&
			(supers == v.supers) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	public Admin update(final AdminValue value)
	{
		if (null != value.password)
		{
			setPassword(saltAndHash(identifier, value.password));
			value.password = null;	// Do NOT reflect back the user password.
		}

		setEmail(value.email);
		setFirstName(value.firstName);
		setLastName(value.lastName);
		setSupers(value.supers);
		value.createdAt = getCreatedAt();
		setUpdatedAt(value.updatedAt = new Date());

		return this;
	}

	/** Checks the supplied value against the existing password.
	 * 
	 * @param value
	 * @return TRUE if the value matches the existing password.
	 */
	public boolean check(final String value)
	{
		return saltAndHash(identifier, value).equals(password);
	}

	public AdminValue toValue()
	{
		return new AdminValue(
			id(),
			null,	// getPassword(), Don't output hashed password.
			getEmail(),
			getFirstName(),
			getLastName(),
			getSupers(),
			getCreatedAt(),
			getUpdatedAt());
	}
}
