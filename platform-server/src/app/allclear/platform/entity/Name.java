package app.allclear.platform.entity;

import java.util.Objects;

import javax.persistence.*;

/** Generic entity that represents a long primary key and name field.
 * 
 * @author smalleyd
 * @version 1.1.80
 * @since 6/2/2020
 *
 */

@Entity
public class Name
{
	@Column(name="id") @Id public Long id;
	@Column(name="name") public String name;

	public Name() {}

	public Name(final String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Name)) return false;

		var v = (Name) o;
		return Objects.equals(name, v.name);
	}

	@Override public int hashCode() { return Objects.hashCode(id); }

	@Override
	public String toString() { return name; }
}
