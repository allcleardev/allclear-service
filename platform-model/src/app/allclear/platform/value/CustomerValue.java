package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

/**********************************************************************************
*
*	Value object class that represents the customer table.
*
*	@author smalleyd
*	@version 1.1.0
*	@since April 26, 2020
*
**********************************************************************************/

public class CustomerValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "customer";
	public static final int MAX_LEN_ID = 40;
	public static final int MAX_LEN_NAME = 128;
	public static final int MIN_LIMIT = 0;
	public static final int MAX_LIMIT = 1000;

	// Members
	public String id = null;
	public String name = null;
	public int limit = 0;	// Zero indicates no throttling limit.
	public boolean active;
	public Date createdAt = null;
	public Date updatedAt = null;

	// Mutators
	public CustomerValue withId(final String newValue) { id = newValue; return this; }
	public CustomerValue withName(final String newValue) { name = newValue; return this; }
	public CustomerValue withLimit(final int newValue) { limit = newValue; return this; }
	public CustomerValue withActive(final boolean newValue) { active = newValue; return this; }
	public CustomerValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }
	public CustomerValue withUpdatedAt(final Date newValue) { updatedAt = newValue; return this; }

	public CustomerValue() {}

	public CustomerValue(final String name) { this(name, 0); }

	public CustomerValue(
		final String name,
		final int limit)
	{
		this(name, limit, true);
	}

	public CustomerValue(
		final String name,
		final int limit,
		final boolean active)
	{
		this(null, name, limit, active, null, null);
	}

	public CustomerValue(final String id,
		final String name,
		final int limit,
		final boolean active,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.name = name;
		this.limit = limit;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		id = StringUtils.trimToNull(id);
		name = StringUtils.trimToNull(name);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof CustomerValue)) return false;

		var v = (CustomerValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(name, v.name) &&
			(limit == v.limit) &&
			(active == v.active) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", limit: ").append(limit)
			.append(", active: ").append(active)
			.append(", createdAt: ").append(createdAt)
			.append(", updatedAt: ").append(updatedAt)
			.append(" }").toString();
	}
}
