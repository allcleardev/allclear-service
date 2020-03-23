package app.allclear.common.value;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/** Value object that represents just the name and identifier portion of an entity. Used to
 *  return data for type-ahead searches.
 *
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class NameValue implements Serializable
{
	/** Constant - serial version UID. */
	public static final long serialVersionUID = 1L;

	/** Represents the entity identifier. */
	public Long getId() { return id; }
	private Long id = null;
	public void setId(Long newValue) { id = newValue; }
	public NameValue withId(Long newValue) { id = newValue; return this; }
	public Integer id() { return (null != id) ? id.intValue() : null; }

	/** Represents the entity's name/caption. */
	public String getName() { return name; }
	private String name = null;
	public void setName(String newValue) { name = newValue; }
	public NameValue withName(String newValue) { name = newValue; return this; }

	/** Indicates that it is empty.
	 *  Do NOT name isEmpty. Does not need to be serialized to JSON.
	 * 
	 * @return TRUE if either the ID or name field is specified.
	 */
	public boolean empty() { return ((null == id) && (null == name)); }

	/** Default/empty. */
	public NameValue() {}

	/** Populator - needed for client apps post arrays.
	 * 
	 * @param id
	 */
	public NameValue(Integer id) { this.id = id.longValue(); }

	/** Populator - needed for client apps post arrays.
	 * 
	 * @param id
	 */
	public NameValue(Long id) { this.id = id; }

	/** Populator - needed for client apps post arrays.
	 * 
	 * @param id
	 */
	public NameValue(String id) { this.id = Long.valueOf(id); }

	/** Populator.
	 * 
	 * @param id
	 * @param name
	 */
	public NameValue(Long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	/** Helper method - trims the name value. */
	public NameValue clean()
	{
		name = StringUtils.trimToNull(name);
		return this;
	}

	@Override
	public int hashCode() { return (null != id) ? id.hashCode() : ((null != name) ? name.hashCode() : 0); }

	@Override
	public boolean equals(Object value)
	{
		if (!(value instanceof NameValue)) return false;

		var v = (NameValue) value;
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
