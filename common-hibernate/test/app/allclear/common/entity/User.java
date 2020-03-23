package app.allclear.common.entity;

import javax.persistence.*;

/** User entity for testing.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Entity
@Table(name="user")
@NamedQuery(name="findUserByEmail", query="SELECT OBJECT(o) FROM User o WHERE o.email = :email")
public class User
{
	public @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
	public String email;
	public @Column(name="first_name") String firstName;
	public @Column(name="last_name") String lastName;
	public @Column(name="company_id") Long companyId;
	public @Column(name="state_id") String stateId;
	public @Column(name="country_id") String countryId;
	public boolean active;

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="country_id", updatable=false, insertable=false, nullable=false)
	public Country country;

	public User() {}

	public User(String email, String firstName, String lastName, String stateId, String countryId)
	{
		this(email, firstName, lastName, null, stateId, countryId, true);
	}

	public User(String email, String firstName, String lastName, Long companyId, String stateId, String countryId, boolean active)
	{
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.companyId = companyId;
		this.stateId = stateId;
		this.countryId = countryId;
		this.active = active;
	}
}
