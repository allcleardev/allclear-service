package app.allclear.common.hibernate;

import static java.util.Objects.requireNonNull;
import static javax.transaction.Status.*;

import javax.transaction.Synchronization;

import org.hibernate.*;
import org.hibernate.query.Query;

/** Represents a base class for Hibernate data access objects. It provides deeper type-safety than Dropwizard's AbstractDAO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class AbstractDAO<E> extends io.dropwizard.hibernate.AbstractDAO<E>
{
	public AbstractDAO(final SessionFactory factory)
	{
		super(factory);
	}

	/** Creates a typed query for entity queries.
	 *
	 * @param name the name of the query
	 * @return never NULL
	 * @see Session#createNamedQuery(String, Class<E>)
	 */
	protected Query<E> namedQuery(final String name) throws HibernateException
	{
		return currentSession().createNamedQuery(requireNonNull(name), getEntityClass());
	}

	/** Creates a typed query for entity queries of an alternate type from this DAO.
	 *
	 * @param name the name of the query
	 * @param clazz alternate query entity.
	 * @return never NULL
	 * @see Session#createNamedQuery(String, Class<T>)
	 */
	protected <T> Query<T> namedQuery(final String name, final Class<T> clazz) throws HibernateException
	{
		return currentSession().createNamedQuery(requireNonNull(name), clazz);
	}

	/** Creates a untyped query for updates and deletes.
	 *
	 * @param name the name of the query
	 * @return never NULL
	 * @see Session#createNamedQuery(String)
	 */
	protected Query<?> namedQueryX(final String name) throws HibernateException
	{
		return currentSession().createNamedQuery(requireNonNull(name));
	}
	
	/** Creates a correctly typed version of the the dynamic QueryBuilder.
	 * 
	 * @param select
	 * @return never NULL.
	 */
	protected HibernateQueryBuilder<E> createQueryBuilder(final String select)
	{
		return new HibernateQueryBuilder<E>(currentSession(), select, getEntityClass());
	}

	/** Runs the specified action after the current transaction has completed successfully. */
	protected void afterTrans(final Runnable fx)
	{
		final Transaction trans = currentSession().getTransaction();
		if (null == trans) return;

		trans.registerSynchronization(new Synchronization() {
			@Override public void beforeCompletion() {}
			@Override public void afterCompletion(final int status) {
				if (status == STATUS_COMMITTED)
					fx.run();
			}
		});
	}

	/** Runs the specified action after the current transaction has completed successfully or rolled back. */
	protected void afterTrans(final Runnable onCommit, final Runnable onRollback)
	{
		final Transaction trans = currentSession().getTransaction();
		if (null == trans) return;

		trans.registerSynchronization(new Synchronization() {
			@Override public void beforeCompletion() {}
			@Override public void afterCompletion(final int status) {
				if (status == STATUS_COMMITTED) onCommit.run();
				else if (status == STATUS_UNKNOWN) onRollback.run();	// Unfortunately, we don't receive an STATUS_ROLLEDBACK. DLS on 7/13/2018.
			}
		});
	}
}
