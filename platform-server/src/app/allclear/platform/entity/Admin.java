package app.allclear.platform.entity;

import static app.allclear.common.crypto.Hasher.saltAndHash;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.time.DateUtils;

import com.microsoft.azure.storage.table.TableServiceEntity;

import app.allclear.platform.value.AdminValue;

/**********************************************************************************
*
*	Azure Cosmos entity that represents the admin table.
*
*	@author smalleyd
*	@version 1.0.14
*	@since April 1, 2020
*
**********************************************************************************/

public class Admin extends TableServiceEntity implements Serializable
{
	private final static long serialVersionUID = 1L;

	public String id() { return getRowKey(); }

	public String getPassword() { return password; }
	public String password;
	public void setPassword(final String newValue) { password = newValue; }
	public void putPassword(final String newValue)
	{
		this.password = saltAndHash(identifier, newValue);
	}

	public String getEmail() { return email; }
	public String email;
	public void setEmail(final String newValue) { email = newValue; }

	public String getFirstName() { return firstName; }
	public String firstName;
	public void setFirstName(final String newValue) { firstName = newValue; }

	public String getLastName() { return lastName; }
	public String lastName;
	public void setLastName(final String newValue) { lastName = newValue; }

	public String getPhone() { return phone; }
	public String phone;
	public void setPhone(final String newValue) { phone = newValue; }

	public boolean getSupers() { return supers; }
	public boolean supers;
	public void setSupers(final boolean newValue) { supers = newValue; }

	public boolean getEditor() { return editor; }
	public boolean editor;
	public void setEditor(final boolean newValue) { editor = newValue; }

	public boolean getAlertable() { return alertable; }
	public boolean alertable;
	public void setAlertable(final boolean newValue) { alertable = newValue; }

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

	public Admin(final String partition, final AdminValue value)
	{
		super(partition, value.id);

		var now = new Date();
		this.identifier = now.getTime();	// SET 'identifier' first as it's used to salt the password hash. DLS on 5/1/2020.
		putPassword(value.password);
		this.email = value.email;
		this.firstName = value.firstName;
		this.lastName = value.lastName;
		this.phone = value.phone;
		this.supers = value.supers;
		this.editor = value.editor;
		this.alertable = value.alertable;
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
			Objects.equals(phone, v.phone) &&
			(supers == v.supers) &&
			(editor == v.editor) &&
			(alertable = v.alertable) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override public int hashCode() { return Objects.hashCode(getRowKey()); }

	public Admin update(final AdminValue value)
	{
		if (null != value.password)
		{
			putPassword(value.password);
			value.password = null;	// Do NOT reflect back the user password.
		}

		setEmail(value.email);
		setFirstName(value.firstName);
		setLastName(value.lastName);
		setPhone(value.phone);
		setSupers(value.supers);
		setEditor(value.editor);
		setAlertable(value.alertable);
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
			getPhone(),
			getSupers(),
			getEditor(),
			getAlertable(),
			getCreatedAt(),
			getUpdatedAt());
	}
}
