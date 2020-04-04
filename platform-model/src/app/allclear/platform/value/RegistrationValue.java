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
	public final boolean beenTested;
	public final boolean haveSymptoms;
	public final Long ttl;

	public RegistrationValue(final String key, final StartRequest request, final Long ttl)
	{
		this(key, request.phone, request.beenTested, request.haveSymptoms, ttl);
	}

	public RegistrationValue(@JsonProperty("key") final String key,
		@JsonProperty("phone") final String phone,
		@JsonProperty("beenTested") final Boolean beenTested,
		@JsonProperty("haveSymptoms") final Boolean haveSymptoms,
		@JsonProperty("ttl") final Long ttl)
	{
		this.ttl = ttl;
		this.key = StringUtils.trimToNull(key);
		this.phone = StringUtils.trimToNull(phone);
		this.beenTested = Boolean.TRUE.equals(beenTested);
		this.haveSymptoms = Boolean.TRUE.equals(haveSymptoms);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof RegistrationValue)) return false;

		var v = (RegistrationValue) o;

		return Objects.equals(key, v.key) &&
			Objects.equals(phone, v.phone) &&
			(beenTested == v.beenTested) &&
			(haveSymptoms == v.haveSymptoms) &&
			Objects.equals(ttl, v.ttl);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ key: ").append(key)
			.append(", phone: ").append(phone)
			.append(", beenTested: ").append(beenTested)
			.append(", haveSymptoms: ").append(haveSymptoms)
			.append(", ttl: ").append(ttl)
			.append(" }").toString();
	}
}
