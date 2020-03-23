package app.allclear.common.value;

import java.io.Serializable;
import java.util.Objects;

/** Value object that represents just the name and identifier portion of an entity. Used to
 *  return data for type-ahead searches.
 *
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class NamedValue implements Serializable
{
	/** Constant - serial version UID. */
	public static final long serialVersionUID = 1L;

	/** Represents the entity identifier. */
	public String getId() { return id; }
	private String id = null;
	public void setId(String newValue) { id = newValue; }
	public NamedValue withId(String newValue) { id = newValue; return this; }

	/** Represents the entity's name/caption. */
	public String getName() { return name; }
	private String name = null;
	public void setName(String newValue) { name = newValue; }
	public NamedValue withName(String newValue) { name = newValue; return this; }

	/** Indicates that it is empty.
	 *  Do NOT name isEmpty. Does not need to be serialized to JSON.
	 * 
	 * @return TRUE if either the ID or name field is specified.
	 */
	public boolean empty() { return ((null == id) && (null == name)); }

	/** Default/empty. */
	public NamedValue() {}

	public NamedValue(final String id)
	{
		this(id, null);
	}

	/** Populator.
	 * 
	 * @param id
	 * @param name
	 */
	public NamedValue(final String id, final String name)
	{
		this.id = id;
		this.name = name;
	}

	@Override
	public int hashCode() { return (null != id) ? id.hashCode() : ((null != name) ? name.hashCode() : 0); }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof NamedValue)) return false;

		var v = (NamedValue) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: '").append(name)
			.append("' }").toString();
	}
}
