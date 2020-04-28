package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for Friend query.
*
*	@author smalleyd
*	@version 1.1.9
*	@since April 27, 2020
*
*******************************************************************************************************************/

public class FriendFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public String personId = null;
	public String inviteeId = null;
	public String userId = null;
	public Boolean hasAcceptedAt = null;
	public Date acceptedAtFrom = null;
	public Date acceptedAtTo = null;
	public Boolean hasRejectedAt = null;
	public Date rejectedAtFrom = null;
	public Date rejectedAtTo = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;

	// Mutators
	public FriendFilter withPersonId(final String newValue) { personId = newValue; return this; }
	public FriendFilter withInviteeId(final String newValue) { inviteeId = newValue; return this; }
	public FriendFilter withUserId(final String newValue) { userId = newValue; return this; }
	public FriendFilter withHasAcceptedAt(final Boolean newValue) { hasAcceptedAt = newValue; return this; }
	public FriendFilter withAcceptedAtFrom(final Date newValue) { acceptedAtFrom = newValue; return this; }
	public FriendFilter withAcceptedAtTo(final Date newValue) { acceptedAtTo = newValue; return this; }
	public FriendFilter withHasRejectedAt(final Boolean newValue) { hasRejectedAt = newValue; return this; }
	public FriendFilter withRejectedAtFrom(final Date newValue) { rejectedAtFrom = newValue; return this; }
	public FriendFilter withRejectedAtTo(final Date newValue) { rejectedAtTo = newValue; return this; }
	public FriendFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public FriendFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public FriendFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public FriendFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public FriendFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public FriendFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param personId represents the "person_id" field.
		@param inviteeId represents the "invitee_id" field.
		@param acceptedAtFrom represents the "accepted_at" field - lower boundary.
		@param acceptedAtTo represents the "accepted_at" field - upper boundary.
		@param rejectedAtFrom represents the "rejected_at" field - lower boundary.
		@param rejectedAtTo represents the "rejected_at" field - upper boundary.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
	*/
	public FriendFilter(final String personId,
		final String inviteeId,
		final Date acceptedAtFrom,
		final Date acceptedAtTo,
		final Date rejectedAtFrom,
		final Date rejectedAtTo,
		final Date createdAtFrom,
		final Date createdAtTo)
	{
		this.personId = personId;
		this.inviteeId = inviteeId;
		this.acceptedAtFrom = acceptedAtFrom;
		this.acceptedAtTo = acceptedAtTo;
		this.rejectedAtFrom = rejectedAtFrom;
		this.rejectedAtTo = rejectedAtTo;
		this.createdAtFrom = createdAtFrom;
		this.createdAtTo = createdAtTo;
	}

	/**************************************************************************
	*
	*	Helper methods
	*
	**************************************************************************/

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public FriendFilter clean()
	{
		personId = StringUtils.trimToNull(personId);
		inviteeId = StringUtils.trimToNull(inviteeId);

		return this;
	}

	/**************************************************************************
	*
	*	Object methods
	*
	**************************************************************************/

	@Override
	public String toString()
	{
		return new StringBuilder("{ personId: ").append(personId)
			.append(", inviteeId: ").append(inviteeId)
			.append(", userId: ").append(userId)
			.append(", hasAcceptedAt: ").append(hasAcceptedAt)
			.append(", acceptedAtFrom: ").append(acceptedAtFrom)
			.append(", acceptedAtTo: ").append(acceptedAtTo)
			.append(", hasRejectedAt: ").append(hasRejectedAt)
			.append(", rejectedAtFrom: ").append(rejectedAtFrom)
			.append(", rejectedAtTo: ").append(rejectedAtTo)
			.append(", createdAtFrom: ").append(createdAtFrom)
			.append(", createdAtTo: ").append(createdAtTo)
			.append(" }").toString();
	}
}
