package app.allclear.platform.value;

import java.util.Date;

/** Represents values that can be audited.
 * 
 * @author smalleyd
 * @version 1.1.46
 * @since 5/10/2020
 *
 */

public interface Auditable
{
	public String id();
	public String tableName();
	public Date updatedAt();
}
