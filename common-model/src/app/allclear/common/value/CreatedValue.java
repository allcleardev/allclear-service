package app.allclear.common.value;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents a simple id, name, and creation timestamp.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class CreatedValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public String id;
	public String name;
	public Date createdAt;

	public CreatedValue withId(final String newValue) { id = newValue; return this; }
	public CreatedValue withName(final String newValue) { name = newValue; return this; }
	public CreatedValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }

	public CreatedValue(final String id)
	{
		this(id, null, null);
	}

	public CreatedValue(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name,
		@JsonProperty("createdAt") final Date createdAt)
	{
		this.id = id;
		this.name = name;
		this.createdAt = createdAt;
	}

	public CreatedValue clean()
	{
		id = StringUtils.trimToNull(id);

		return this;
	}

	@Override
	public int hashCode() { return (null != id) ? id.hashCode() : ((null != name) ? name.hashCode() : 0); }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof CreatedValue)) return false;

		var v = (CreatedValue) o;
		return (Objects.equals(id, v.id) && Objects.equals(name, v.name));	// NOT needed and complicates tests in other systems. DLS on 4/12/2018. && Objects.equals(createdAt, v.createdAt));
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", createdAt: ").append(createdAt)
			.append(" }").toString();
	}
}
