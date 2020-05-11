package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

/**********************************************************************************
*
*	Value object class that represents the audit_log table.
*
*	@author smalleyd
*	@version 1.1.46
*	@since May 10, 2020
*
**********************************************************************************/

public class AuditLogValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String TABLE = "audit_log";
	public static final int MAX_LEN_ID = 255;
	public static final int MAX_LEN_ACTOR_TYPE = 32;
	public static final int MAX_LEN_ACTION_BY = 255;
	public static final int MAX_LEN_ACTION = 32;
	public static final int MAX_LEN_PAYLOAD = 65535;

	// Members
	public String id = null;
	public long actionAt;
	public String actorType = null;
	public String actionBy = null;
	public String action = null;
	public String payload = null;
	public Date timestamp = null;

	// Mutators
	public AuditLogValue withId(final String newValue) { id = newValue; return this; }
	public AuditLogValue withActionAt(final long newValue) { actionAt = newValue; return this; }
	public AuditLogValue withActorType(final String newValue) { actorType = newValue; return this; }
	public AuditLogValue withActionBy(final String newValue) { actionBy = newValue; return this; }
	public AuditLogValue withAction(final String newValue) { action = newValue; return this; }
	public AuditLogValue withPayload(final String newValue) { payload = newValue; return this; }
	public AuditLogValue withTimestamp(final Date newValue) { timestamp = newValue; return this; }

	public AuditLogValue() {}

	public AuditLogValue(final String id,
		final long actionAt,
		final String actorType,
		final String actionBy,
		final String action,
		final String payload,
		final Date timestamp)
	{
		this.id = id;
		this.actionAt = actionAt;
		this.actorType = actorType;
		this.actionBy = actionBy;
		this.action = action;
		this.payload = payload;
		this.timestamp = timestamp;
	}

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public void clean()
	{
		id = StringUtils.trimToNull(id);
		actorType = StringUtils.trimToNull(actorType);
		actionBy = StringUtils.trimToNull(actionBy);
		action = StringUtils.trimToNull(action);
		payload = StringUtils.trimToNull(payload);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof AuditLogValue)) return false;

		var v = (AuditLogValue) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(actionAt, v.actionAt) &&
			Objects.equals(actorType, v.actorType) &&
			Objects.equals(actionBy, v.actionBy) &&
			Objects.equals(action, v.action) &&
			Objects.equals(payload, v.payload) &&
			Objects.equals(timestamp, v.timestamp);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, actionAt);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", actionAt: ").append(actionAt)
			.append(", actorType: ").append(actorType)
			.append(", actionBy: ").append(actionBy)
			.append(", action: ").append(action)
			.append(", payload: ").append(payload)
			.append(", timestamp: ").append(timestamp)
			.append(" }").toString();
	}
}
