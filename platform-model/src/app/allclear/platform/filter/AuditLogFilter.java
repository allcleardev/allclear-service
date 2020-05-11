package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for AuditLog query.
*
*	@author smalleyd
*	@version 1.1.46
*	@since May 10, 2020
*
*******************************************************************************************************************/

public class AuditLogFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public String id = null;
	public Long actionAt = null;
	public String actorType = null;
	public String actionBy = null;
	public String action = null;
	public String payload = null;
	public Date timestampFrom = null;
	public Date timestampTo = null;

	// Mutators
	public AuditLogFilter withId(final String newValue) { id = newValue; return this; }
	public AuditLogFilter withActionAt(final Long newValue) { actionAt = newValue; return this; }
	public AuditLogFilter withActorType(final String newValue) { actorType = newValue; return this; }
	public AuditLogFilter withActionBy(final String newValue) { actionBy = newValue; return this; }
	public AuditLogFilter withAction(final String newValue) { action = newValue; return this; }
	public AuditLogFilter withPayload(final String newValue) { payload = newValue; return this; }
	public AuditLogFilter withTimestampFrom(final Date newValue) { timestampFrom = newValue; return this; }
	public AuditLogFilter withTimestampTo(final Date newValue) { timestampTo = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public AuditLogFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public AuditLogFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public AuditLogFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public AuditLogFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param actionAt represents the "action_at" field.
		@param actorType represents the "actor_type" field.
		@param actionBy represents the "action_by" field.
		@param action represents the "action" field.
		@param payload represents the "payload" field.
	*/
	public AuditLogFilter(final String id,
		final Long actionAt,
		final String actorType,
		final String actionBy,
		final String action,
		final String payload)
	{
		this.id = id;
		this.actionAt = actionAt;
		this.actorType = actorType;
		this.actionBy = actionBy;
		this.action = action;
		this.payload = payload;
	}

	/**************************************************************************
	*
	*	Helper methods
	*
	**************************************************************************/

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public AuditLogFilter clean()
	{
		id = StringUtils.trimToNull(id);
		actorType = StringUtils.trimToNull(actorType);
		actionBy = StringUtils.trimToNull(actionBy);
		action = StringUtils.trimToNull(action);
		payload = StringUtils.trimToNull(payload);

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
		return new StringBuilder("{ id: ").append(id)
			.append(", actionAt: ").append(actionAt)
			.append(", actorType: ").append(actorType)
			.append(", actionBy: ").append(actionBy)
			.append(", action: ").append(action)
			.append(", payload: ").append(payload)
			.append(", timestampFrom: ").append(timestampFrom)
			.append(", timestampTo: ").append(timestampTo)
			.append(" }").toString();
	}
}
