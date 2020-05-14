package app.allclear.platform.entity;

import java.util.Objects;

import javax.persistence.*;

import app.allclear.common.ObjectUtils;

/** Generic entity that represents a character primary key and name field.
 * 
 * @author smalleyd
 * @version 1.1.9
 * @since 4/29/2020
 *
 */

@Entity
public class CountByName
{
	@Column(name="name") @Id public String name;
	@Column(name="total") public long total;

	public CountByName() {}

	public CountByName(final String name, final long total)
	{
		this.name = name;
		this.total = total;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof CountByName)) return false;

		var v = (CountByName) o;
		return Objects.equals(name, v.name) && (total == v.total);
	}

	@Override public int hashCode() { return Objects.hashCode(name); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
