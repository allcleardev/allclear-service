package app.allclear.common.hibernate;

import static app.allclear.common.dao.OrderByBuilder.Sort;

import java.util.*;

import org.hibernate.query.Query;
import org.hibernate.Session;

import app.allclear.common.dao.QueryBuilder;
import app.allclear.common.dao.QueryResults;

/** Builder class that helps construct Hibernate JPL queries including joins and where clause.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 * @param <T>
 */
public class HibernateQueryBuilder<T> extends QueryBuilder<T>
{
	public final Session session;

	/** Populator.
	 * 
	 * @param session Hibernate Session.
	 * @param select SELECT clause.
	 */
	public HibernateQueryBuilder(final Session session, final String select, final Class<T> entity)
	{
		this(session, select, entity, null, null);
	}

	/** Populator.
	 * 
	 * @param session Hibernate Session.
	 * @param select SELECT clause.
	 * @param groupBy GROUP BY clause - optional.
	 */
	public HibernateQueryBuilder(final Session session, final String select, final Class<T> entity, final String groupBy)
	{
		this(session, select, entity, groupBy, null);
	}

	/** Populator.
	 * 
	 * @param session Hibernate Session.
	 * @param select SELECT clause.
	 * @param orderBy Sort object for the ORDER BY clause - optional.
	 */
	public HibernateQueryBuilder(final Session session, final String select, final Class<T> entity, final Sort orderBy)
	{
		this(session, select, entity, null, orderBy);
	}

	/** Populator.
	 * 
	 * @param session Hibernate Session.
	 * @param select SELECT clause.
	 * @param groupBy GROUP BY clause - optional.
	 * @param orderBy Sort object for the ORDER BY clause - optional.
	 */
	public HibernateQueryBuilder(final Session session, final String select, final Class<T> entity, final String groupBy, final Sort orderBy)
	{
		super(select, entity, groupBy, orderBy);
		this.session = session;
	}

	/** Creates the Hibernate Query object from the JPL.
	 * 
	 * @return never NULL.
	 */
	public Query<T> create()
	{
		return session.createQuery(build(), entity);
	}

	/** Creates the Hibernate Query object from the JPL.
	 * 
	 * @param select substitute alternate SELECT clause. Used for COUNTs.
	 * @return never NULL.
	 */
	public Query<T> create(final String select)
	{
		return create(build(select), entity);
	}

	/** Creates the Hibernate Query object from the JPL with an alternate entity.
	 *  Used for aggregates.
	 * 
	 * @param select substitute alternate SELECT clause. Used for COUNTs.
	 * @param clazz alternate entity type.
	 *
	 * @return never NULL.
	 */
	public <E> Query<E> create(final String select, final Class<E> clazz)
	{
		return session.createQuery(build(select), clazz);
	}

	/** Binds the parameters to the query.
	 * 
	 * @param query
	 * @return supplied Query.
	 */
	public Query<T> bind(final Query<T> query)
	{
		return bind(query, entity);
	}

	/** Binds the parameters to the query with an alternate entity.
	 *  Used for aggregates.
	 * 
	 * @param query
	 * @param clazz alternate entity type.
	 * @return supplied Query.
	 */
	public <E> Query<E> bind(final Query<E> query, final Class<E> clazz)
	{
		for (Map.Entry<String, Object> param : parameters.entrySet())
			query.setParameter(param.getKey(), param.getValue());

		return query;
	}

	/** Creates the Hibernate Query and binds the parameters.
	 * 
	 * @return never NULL
	 */
	public Query<T> createAndBind()
	{
		return bind(create());
	}

	@Override
	public List<T> run()
	{
		return createAndBind().list();
	}

	@Override
	public List<T> run(final int firstResult, final int maxResults)
	{
		return bind(create()).setFirstResult(firstResult).setMaxResults(maxResults).list();
	}

	@Override
	public List<T> run(final QueryResults<?, ?> results)
	{
		return run(results.firstResult(), results.pageSize);
	}

	@Override
	public List<T> run(final String select)
	{
		return bind(create(select)).list();
	}

	@Override
	public long aggregate(final String select)
	{
		return bind(create(select, Long.class), Long.class).uniqueResult();
	}
}
