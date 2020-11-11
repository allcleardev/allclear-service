package app.allclear.platform.dao;

import app.allclear.platform.value.AuditLogValue;
import app.allclear.platform.value.Auditable;

/** Test implementation of the Auditor for non-production use.
 * 
 * @author smalleyd
 * @version 1.1.46
 * @since 5/11/2020
 *
 */

public class TestAuditor implements Auditor
{
	public int adds = 0;
	public int updates = 0;
	public int removes = 0;
	public int locks = 0;
	public int releases = 0;
	public int reviews = 0;

	@Override public AuditLogValue add(Auditable value) { adds++; return null; }
	@Override public AuditLogValue update(Auditable value) { updates++; return null; }
	@Override public AuditLogValue remove(Auditable value) { removes++; return null; }
	@Override public AuditLogValue lock(Auditable value) { locks++; return null; }
	@Override public AuditLogValue release(Auditable value) { releases++; return null; }
	@Override public AuditLogValue review(Auditable value) { reviews++; return null; }
}
