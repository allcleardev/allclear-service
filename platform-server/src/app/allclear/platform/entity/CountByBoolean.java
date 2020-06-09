package app.allclear.platform.entity;

import java.util.Objects;

import javax.persistence.*;

import app.allclear.common.ObjectUtils;

/** Generic entity that represents a boolean key and numeric field.
 * 
 * @author smalleyd
 * @version 1.1.85
 * @since 6/9/2020
 *
 */

@Entity
public class CountByBoolean
{
	@Column(name="id") @Id public boolean id;
	@Column(name="total") public long total;

	public CountByBoolean() {}

	public CountByBoolean(final boolean id, final long total)
	{
		this.id = id;
		this.total = total;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof CountByBoolean)) return false;

		var v = (CountByBoolean) o;
		return Objects.equals(id, v.id) && (total == v.total);
	}

	@Override public int hashCode() { return id ? 1 : 0; }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
