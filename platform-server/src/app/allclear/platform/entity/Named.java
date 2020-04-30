package app.allclear.platform.entity;

import java.util.Objects;

import javax.persistence.*;

/** Generic entity that represents a character primary key and name field.
 * 
 * @author smalleyd
 * @version 1.1.9
 * @since 4/29/2020
 *
 */

@Entity
public class Named
{
	@Column(name="id") @Id public String id;
	@Column(name="name") public String name;

	public Named() {}

	public Named(final String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Named)) return false;

		var v = (Named) o;
		return Objects.equals(name, v.name);
	}

	@Override public int hashCode() { return Objects.hashCode(id); }

	@Override
	public String toString() { return name; }
}
