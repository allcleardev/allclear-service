package app.allclear.platform.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;
import app.allclear.twilio.model.TwilioUtils;

/** Value object that represents initiation of a Test result.
 * 
 * @author smalleyd
 * @version 1.1.111
 * @since 7/30/2020
 *
 */

public class TestInitRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final Long facilityId;
	public final String personId;
	public final String phone;
	public final String firstName;
	public final String lastName;
	public final String typeId;
	public final String identifier;

	public boolean valid() { return ((null != personId) || (null != phone)); }

	public TestInitRequest(final Long facilityId, final String personId, final String typeId, final String identifier)
	{
		this(facilityId, personId, null, null, null, typeId, identifier);
	}

	public TestInitRequest(final Long facilityId, final String phone, final String firstName, final String lastName, final String typeId, final String identifier)
	{
		this(facilityId, null, phone, firstName, lastName, typeId, identifier);
	}

	public TestInitRequest(@JsonProperty("facilityId") final Long facilityId,
		@JsonProperty("personId") final String personId,
		@JsonProperty("phone") final String phone,
		@JsonProperty("firstName") final String firstName,
		@JsonProperty("lastName") final String lastName,
		@JsonProperty("typeId") final String typeId,
		@JsonProperty("identifier") final String identifier)
	{
		this.facilityId = facilityId;
		this.personId = StringUtils.trimToNull(personId);
		this.phone = TwilioUtils.normalize(phone);
		this.firstName = StringUtils.trimToNull(firstName);
		this.lastName = StringUtils.trimToNull(lastName);
		this.typeId = StringUtils.trimToNull(typeId);
		this.identifier = StringUtils.trimToNull(identifier);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
