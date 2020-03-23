package app.allclear.common.hibernate;

import org.hibernate.*;
import org.hibernate.context.internal.ManagedSessionContext;

/** Abstract class that wraps an operation in a Hibernate session.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public abstract class AbstractHibernateRunner<Input, Output>
{
	/** Represents the port of the local server. */
	private final DualSessionFactory factory;

	/** Populator.
	 * 
	 * @param factory
	 */
	public AbstractHibernateRunner(final DualSessionFactory factory)
	{
		this.factory = factory;
	}

	public AbstractHibernateRunner(final SessionFactory factory)	// For tests
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

	/** Runs the abstract method and returns the results.
	 * 
	 * @param request
	 * @return results of the abstract "run" method.
	 * @throws Exception
	 */
	public Output run(final Input request) throws Exception
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
			var result = run(request, session);
			if (null != trans) trans.commit();

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
	 * @return the expected Output if successful.
	 * @throws Exception
	 */
	public abstract Output run(final Input request, final Session session) throws Exception;
}
