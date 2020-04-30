package app.allclear.platform.value;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.platform.model.StartRequest;

/** Value object that represents a registration request.
 * 
 * @author smalleyd
 * @version 1.0.39
 * @since 4/3/2020
 *
 */

public class RegistrationValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String key;
	public final String phone;
	public final PeopleValue person;
	public final Long ttl;

	public RegistrationValue(final String key, final StartRequest request, final Long ttl)
	{
		this(key, request.phone, null, ttl);
	}

	public RegistrationValue(final String key, final PeopleValue person, final Long ttl)
	{
		this(key, person.phone, person, ttl);
	}

	public RegistrationValue(@JsonProperty("key") final String key,
		@JsonProperty("phone") final String phone,
		@JsonProperty("person") final PeopleValue person,
		@JsonProperty("ttl") final Long ttl)
	{
		this.ttl = ttl;
		this.person = person;
		this.key = StringUtils.trimToNull(key);
		this.phone = StringUtils.trimToNull(phone);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof RegistrationValue)) return false;

		var v = (RegistrationValue) o;

		return Objects.equals(key, v.key) &&
			Objects.equals(phone, v.phone) &&
			Objects.equals(person, v.person) &&
			Objects.equals(ttl, v.ttl);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(key);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ key: ").append(key)
			.append(", phone: ").append(phone)
			.append(", person: ").append(person)
			.append(", ttl: ").append(ttl)
			.append(" }").toString();
	}
}
