package app.allclear.common.entity;

import javax.persistence.*;

/** Country entity for testing.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

@Entity
@Table(name="country")
public class Country
{
	@Id
	public String id;
	public String name;
	public String code;
	@Column (name="num_code") public String numCode;
	public boolean active;

	public Country() {}

	public Country(String id, String name, String code, String numCode)
	{
		this(id, name, code, numCode, true);
	}

	public Country(String id, String name, String code, String numCode, boolean active)
	{
		this.id = id;
		this.name = name;
		this.code = code;
		this.numCode = numCode;
		this.active = active;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", code: ").append(code)
			.append(", numCode: ").append(numCode)
			.append(", active: ").append(active)
			.append(" }").toString();
	}
}
