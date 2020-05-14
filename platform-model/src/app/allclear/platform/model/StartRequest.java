package app.allclear.platform.model;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;
import app.allclear.twilio.model.TwilioUtils;

/** Value object that represents the request to start the registration process.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class StartRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String phone;
	public final boolean beenTested;
	public final boolean haveSymptoms;

	public StartRequest(@JsonProperty("phone") final String phone,
		@JsonProperty("beenTested") final Boolean beenTested,
		@JsonProperty("haveSymptoms") final Boolean haveSymptoms)
	{
		this.phone = TwilioUtils.normalize(StringUtils.trimToNull(phone));
		this.beenTested = Boolean.TRUE.equals(beenTested);
		this.haveSymptoms = Boolean.TRUE.equals(haveSymptoms);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof StartRequest)) return false;

		var v = (StartRequest) o;
		return Objects.equals(phone, v.phone) &&
			(beenTested == v.beenTested) &&
			(haveSymptoms == v.haveSymptoms);
	}

	@Override
	public int hashCode() { return Objects.hashCode(phone); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
