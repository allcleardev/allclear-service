package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import com.microsoft.azure.storage.table.TableServiceEntity;

import app.allclear.platform.value.AuditLogValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the audit_log table.
*
*	@author smalleyd
*	@version 1.1.46
*	@since May 10, 2020
*
**********************************************************************************/

public class AuditLog extends TableServiceEntity implements Serializable
{
	private final static long serialVersionUID = 1L;

	public String id() { return getPartitionKey(); }
	public String actionAt() { return getRowKey(); }

	public long getActionAt() { return actionAt; }
	public long actionAt;
	public void setActionAt(final long newValue) { actionAt = newValue; }

	public String getActorType() { return actorType; }
	public String actorType;
	public void setActorType(final String newValue) { actorType = newValue; }

	public String getActionBy() { return actionBy; }
	public String actionBy;
	public void setActionBy(final String newValue) { actionBy = newValue; }

	public String getAction() { return action; }
	public String action;
	public void setAction(final String newValue) { action = newValue; }

	public String getPayload() { return payload; }
	public String payload;
	public void setPayload(final String newValue) { payload = newValue; }

	public AuditLog() {}

	public AuditLog(final AuditLogValue value)
	{
		super(value.id, value.actionAt + "");
		this.actionAt = value.actionAt;
		this.actorType = value.actorType;
		this.actionBy = value.actionBy;
		this.action = value.action;
		this.payload = value.payload;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof AuditLog)) return false;

		var v = (AuditLog) o;
		return Objects.equals(id(), v.id()) &&
			Objects.equals(actionAt(), v.actionAt()) &&
			Objects.equals(actorType, v.actorType) &&
			Objects.equals(actionBy, v.actionBy) &&
			Objects.equals(action, v.action) &&
			Objects.equals(payload, v.payload) &&
			Objects.equals(getTimestamp(), v.getTimestamp());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id(), actionAt());
	}

	public AuditLogValue toValue()
	{
		return new AuditLogValue(
			id(),
			getActionAt(),
			getActorType(),
			getActionBy(),
			getAction(),
			getPayload(),
			getTimestamp());
	}
}
