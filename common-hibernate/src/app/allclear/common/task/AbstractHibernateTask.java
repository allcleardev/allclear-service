package app.allclear.common.task;

import org.hibernate.*;
import org.hibernate.context.internal.ManagedSessionContext;

import app.allclear.common.hibernate.DualSessionFactory;

/** Background task that provides a Hibernate Session to process the request.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public abstract class AbstractHibernateTask<T> implements TaskCallback<T>
{
	/** Represents the port of the local server. */
	private final DualSessionFactory factory;

	/** Populator.
	 * 
	 * @param factory
	 */
	public AbstractHibernateTask(final DualSessionFactory factory)
	{
		this.factory = factory;
	}

	public AbstractHibernateTask(final SessionFactory factory)	// For tests
	{
		this(new DualSessionFactory(factory));
	}

	/** Should the session be read only. Defaults to FALSE. */
	protected boolean readOnly() { return false; }

	/** Should the session start a new transaction. Defaults to TRUE. */
	protected boolean transactional() { return true; }

	/** Specifies the CacheMode. Defaults to NORMAL. */
	protected CacheMode cacheMode() { return CacheMode.NORMAL; }

	/** Specifies the FlushMode. Defaults to AUTO. */
	protected FlushMode flushMode() { return FlushMode.AUTO; }

	@Override
	public boolean process(T request) throws Exception
	{
		Transaction trans = null;
		var factory = this.factory.prepare(readOnly());
		var session = factory.openSession();
		try
		{
			// Configure the Hibernate Session.
			session.setDefaultReadOnly(readOnly());
			session.setCacheMode(cacheMode());
			session.setHibernateFlushMode(flushMode());
			ManagedSessionContext.bind(session);

			// If transactional, start the transaction.
			trans = (transactional()) ? session.beginTransaction() : null;

			// Process the request.
			boolean result = process(request, session);
			if (null != trans)
				trans.commit();

			return result;
		}
		catch (final Throwable ex)
		{
			if (null != trans)
				trans.rollback();

			throw ex;
		}
		finally
		{
			session.close();
			ManagedSessionContext.unbind(factory);
		}
	}

	/** Process the request with a Hibernate session.
	 * 
	 * @param request
	 * @param session
	 * @return TRUE if successfully processed. FALSE indicates to delay processing the request.
	 * @throws Exception
	 */
	public abstract boolean process(final T request, final Session session) throws Exception;
}
