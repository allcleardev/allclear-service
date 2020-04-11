package app.allclear.platform.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

/** JDBi data access object that maps to the PEOPLE table.
 * 
 * @author smalleyd
 * @version 1.0.104
 * @since 4/11/2020
 *
 */

public interface PeopleDAO
{
	@SqlQuery("SELECT o.id FROM people o WHERE o.id > :lastId AND o.alertable = TRUE AND o.active = TRUE ORDER BY o.id LIMIT :pageSize")
	public List<Long> getActiveAlertableIds(@Bind("lastId") final String lastId, @Bind("pageSize") final int pageSize);
}
