package app.allclear.platform.entity;

import java.util.Date;
import java.util.Objects;

import javax.persistence.*;

import app.allclear.common.value.CreatedValue;

/** Generic entity that represents a character primary key, name field, and creation date field.
 * 
 * @author smalleyd
 * @version 1.1.101
 * @since 7/6/2020
 *
 */

@Entity
public class Created
{
	@Column(name="id") @Id public String id;
	@Column(name="name") public String name;
	@Column(name="created_at") public Date createdAt;
	@Column(name="parent_id") public Long parentId;	// Optional

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Created)) return false;

		var v = (Created) o;
		return Objects.equals(name, v.name);
	}

	@Override public int hashCode() { return Objects.hashCode(id); }

	@Override
	public String toString() { return name; }

	@Transient
	public CreatedValue toValue() { return new CreatedValue(id, name, createdAt); }
}
