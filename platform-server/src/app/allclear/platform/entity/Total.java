package app.allclear.platform.entity;

import javax.persistence.*;

/** Basic entity class.
 * 
 * @author smalleyd
 * @version 1.1.54
 * @since 5/12/2020
 *
 */

@Entity
public class Total
{
	public static final Total ZERO = new Total(0L);

	@Column(name="total") @Id public Long total;

	public Total() {}
	public Total(final long value) { total = value; }
}
