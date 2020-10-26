package app.allclear.platform.entity;

import java.util.Objects;

import javax.persistence.*;

import app.allclear.common.ObjectUtils;

/** Generic entity that represents a character primary key and numeric field.
 * 
 * @author smalleyd
 * @version 1.1.9
 * @since 4/29/2020
 *
 */

@Entity
public class CountById
{
	@Column(name="id") @Id public Long id;
	@Column(name="total") public long total;

	public CountById() {}

	public CountById(final Long id, final long total)
	{
		this.id = id;
		this.total = total;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof CountById)) return false;

		var v = (CountById) o;
		return Objects.equals(id, v.id) && (total == v.total);
	}

	@Override public int hashCode() { return Objects.hashCode(id); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
