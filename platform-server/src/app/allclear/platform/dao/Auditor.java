package app.allclear.platform.dao;

import app.allclear.platform.value.Auditable;
import app.allclear.platform.value.AuditLogValue;

/**********************************************************************************
*
*	Data access object that handles access to the AuditLog entity.
*
*	@author smalleyd
*	@version 1.1.46
*	@since May 10, 2020
*
**********************************************************************************/

public interface Auditor
{
	public AuditLogValue add(final Auditable value);
	public AuditLogValue update(final Auditable value);
	public AuditLogValue remove(final Auditable value);

	public AuditLogValue lock(final Auditable value);
	public AuditLogValue release(final Auditable value);
	public AuditLogValue review(final Auditable value);
}
