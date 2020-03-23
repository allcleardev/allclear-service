package app.allclear.common.entity;

import javax.persistence.*;

/** Class that represents the JPA results of a country aggregate query.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Entity
public class CountryCount
{
	@Id public String name;
	@Column(name="count_total") public Integer total;
}
