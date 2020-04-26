package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.time.DateUtils;

import com.microsoft.azure.storage.table.TableServiceEntity;

import app.allclear.platform.value.CustomerValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the admin table.
*
*	@author smalleyd
*	@version 1.1.0
*	@since April 26, 2020
*
**********************************************************************************/

public class Customer extends TableServiceEntity implements Serializable
{
	private final static long serialVersionUID = 1L;

	public String env() { return getPartitionKey(); }
	public String id() { return getRowKey(); }

	public String getName() { return name; }
	public String name;
	public void setName(final String newValue) { name = newValue; }

	public int getLimit() { return limit; }	// Number of calls permitted per second.
	public int limit;
	public void setLimit(final int newValue) { limit = newValue; }

	public boolean getActive() { return active; }
	public boolean active;
	public void setActive(final boolean newValue) { active = newValue; }

	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	public Date getUpdatedAt() { return updatedAt; }
	public Date updatedAt;
	public void setUpdatedAt(final Date newValue) { updatedAt = newValue; }

	public Customer() {}

	public Customer(final String env, final CustomerValue value)
	{
		super(env, value.id);
		this.name = value.name;
		this.limit = value.limit;
		this.active = value.active;
		this.createdAt = value.createdAt = this.updatedAt = value.updatedAt = new Date();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Customer)) return false;

		var v = (Customer) o;
		return Objects.equals(env(), v.env()) &&
			Objects.equals(id(), v.id()) &&
			Objects.equals(name, v.name) &&
			(limit == v.limit) && 
			(active == v.active) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	public Customer update(final CustomerValue value)
	{
		setName(value.name);
		setLimit(value.limit);
		setActive(value.active);
		value.createdAt = getCreatedAt();
		setUpdatedAt(value.updatedAt = new Date());

		return this;
	}

	public CustomerValue toValue()
	{
		return new CustomerValue(
			id(),
			getName(),
			getLimit(),
			getActive(),
			getCreatedAt(),
			getUpdatedAt());
	}
}
