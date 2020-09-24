package app.allclear.platform.entity;

import java.util.Date;
import java.util.Objects;

import javax.persistence.*;

import app.allclear.common.ObjectUtils;

/** Generic entity that represents a character primary key and numeric field.
 * 
 * @author smalleyd
 * @version 1.1.134
 * @since 9/24/2020
 *
 */

@Entity
public class CountByNameAndDate
{
	public static final CountByNameAndDate EMPTY = new CountByNameAndDate(null, 0L, null);

	@Column(name="name") @Id public String name;
	@Column(name="total") public long total;
	@Column(name="last") public Date last;

	public CountByNameAndDate() {}

	public CountByNameAndDate(final String name, final long total, final Date last)
	{
		this.last = last;
		this.name = name;
		this.total = total;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof CountByNameAndDate)) return false;

		var v = (CountByNameAndDate) o;
		return Objects.equals(name, v.name) && (total == v.total) & Objects.equals(last, v.last);
	}

	@Override public int hashCode() { return Objects.hashCode(name); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
