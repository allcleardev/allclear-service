package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/**********************************************************************************
*
*	Value object class that represents the friend table.
*
*	@author smalleyd
*	@version 1.1.9
*	@since April 27, 2020
*
**********************************************************************************/

public class FriendValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "friend";
	public static final int MAX_LEN_PERSON_ID = 10;
	public static final int MAX_LEN_INVITEE_ID = 10;

	// Members
	public String personId = null;
	public String personName = null;
	public String inviteeId = null;
	public String inviteeName = null;
	public Date acceptedAt = null;
	public Date rejectedAt = null;
	public Date createdAt = null;

	// Accessors
	@JsonProperty(access=JsonProperty.Access.READ_ONLY)
	public String getId() { return new StringBuilder(personId).append("/").append(inviteeId).toString(); }	// Identifier for REST calls.

	// Mutators
	public FriendValue withPersonId(final String newValue) { personId = newValue; return this; }
	public FriendValue withPersonName(final String newValue) { personName = newValue; return this; }
	public FriendValue withInviteeId(final String newValue) { inviteeId = newValue; return this; }
	public FriendValue withInviteeName(final String newValue) { inviteeName = newValue; return this; }
	public FriendValue withAcceptedAt(final Date newValue) { acceptedAt = newValue; return this; }
	public FriendValue withRejectedAt(final Date newValue) { rejectedAt = newValue; return this; }
	public FriendValue withCreatedAt(final Date newValue) { createdAt = newValue; return this; }

	public FriendValue() {}

	public FriendValue(final PeopleValue person,
		final String inviteeId)
	{
		this(person.id, inviteeId, null, null);
		this.personName = person.name;
	}

	public FriendValue(final String personId,
		final String inviteeId)
	{
		this(personId, inviteeId, null, null);
	}

	public FriendValue(final String personId,
		final String inviteeId,
		final Date acceptedAt,
		final Date rejectedAt)
	{
		this(personId, null, inviteeId, null, acceptedAt, rejectedAt, null);
	}

	public FriendValue(final String personId,
		final String personName,
		final String inviteeId,
		final String inviteeName,
		final Date acceptedAt,
		final Date rejectedAt,
		final Date createdAt)
	{
		this.personId = personId;
		this.personName = personName;
		this.inviteeId = inviteeId;
		this.inviteeName = inviteeName;
		this.acceptedAt = acceptedAt;
		this.rejectedAt = rejectedAt;
		this.createdAt = createdAt;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		personId = StringUtils.trimToNull(personId);
		inviteeId = StringUtils.trimToNull(inviteeId);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof FriendValue)) return false;

		var v = (FriendValue) o;
		return Objects.equals(personId, v.personId) &&
			Objects.equals(inviteeId, v.inviteeId) &&
			Objects.equals(inviteeName, v.inviteeName) &&
			((acceptedAt == v.acceptedAt) || DateUtils.truncatedEquals(acceptedAt, v.acceptedAt, Calendar.SECOND)) &&
			((rejectedAt == v.rejectedAt) || DateUtils.truncatedEquals(rejectedAt, v.rejectedAt, Calendar.SECOND)) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(personId, inviteeId);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
