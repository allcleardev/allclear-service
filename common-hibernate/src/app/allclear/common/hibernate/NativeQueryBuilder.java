package app.allclear.common.hibernate;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

/** Builder class that helps construct Hibernate native SQL queries including joins and where clause.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 * @param <T>
 */
public class NativeQueryBuilder<T> extends HibernateQueryBuilder<T>
{
	public final String alias;

	/** Populator.
	 * 
	 * @param session Hibernate Session.
	 * @param select SELECT clause.
	 * @param entity Hibernate entity within which to return each record.
	 * @param alias FROM clause alias for the desired table to populate the entity.
	 */
	public NativeQueryBuilder(final Session session, final String select, final Class<T> entity, final String alias)
	{
		this(session, select, entity, alias, null);
	}

	/** Populator.
	 * 
	 * @param session Hibernate Session.
	 * @param select SELECT clause.
	 * @param entity Hibernate entity within which to return each record.
	 * @param alias FROM clause alias for the desired table to populate the entity.
	 * @param groupBy GROUP BY clause - optional.
	 */
	public NativeQueryBuilder(final Session session, final String select, final Class<T> entity, final String alias, final String groupBy)
	{
		super(session, select, entity, groupBy);
		this.alias = alias;
	}

	@Override
	public NativeQuery<T> create()
	{
		return session.createNativeQuery(build(), entity);
	}

	@Override
	public <E> NativeQuery<E> create(final String select, final Class<E> clazz)
	{
		return session.createNativeQuery(build(select), clazz);
	}

	@Override
	@SuppressWarnings("unchecked")
	public long aggregate(final String select)
	{
		// CanNOT call createNativeQuery(sql, Long.class). The resultClass must be a registered Entity type. DLS on 9/12/2018.
		return ((Number) bind(session.createNativeQuery(build(select)), Object.class).uniqueResult()).longValue();
	}
}
