package app.allclear.common.hibernate;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;

import io.dropwizard.hibernate.UnitOfWork;

/** Wrapper class for the operations to perform on a primary SessionFactory and a reader SessionFactory for
 *  a single UnitOfWork.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class UnitOfWorkAspect
{
	private final DualSessionFactory factory;

	public UnitOfWorkAspect(final DualSessionFactory factory)
	{
		this.factory = factory;
	}

	// Context variables
	@Nullable
	private UnitOfWork unitOfWork;

	@Nullable
	private Session session;

	@Nullable
	private SessionFactory sessionFactory;

	// was the session created by this aspect?
	private boolean sessionCreated;
	// do we manage the transaction or did we join an existing one?
	private boolean transactionStarted;

	public void beforeStart(@Nullable final UnitOfWork unitOfWork)
	{
		if (unitOfWork == null) return;
		this.unitOfWork = unitOfWork;

		sessionFactory = factory.prepare(unitOfWork.readOnly());

		Session existingSession = null;
		if (ManagedSessionContext.hasBind(sessionFactory))
			existingSession = sessionFactory.getCurrentSession();

		if (existingSession != null)
		{
			sessionCreated = false;
			session = existingSession;
			validateSession();
		}
		else
		{
			sessionCreated = true;
			session = sessionFactory.openSession();
			try
			{
				configureSession();
				ManagedSessionContext.bind(session);
				beginTransaction(unitOfWork, session);
			}
			catch (final Throwable ex)
			{
				session.close();
				session = null;
				ManagedSessionContext.unbind(sessionFactory);
				throw ex;
			}
		}
	}

	public void afterEnd()
	{
		if (unitOfWork == null || session == null) return;

		try { commitTransaction(unitOfWork, session); }
		catch (final Exception ex)
		{
			rollbackTransaction(unitOfWork, session);
			throw ex;
		}
		// We should not close the session to let the lazy loading work during serializing a response to the client.
		// If the response successfully serialized, then the session will be closed by the `onFinish` method
	}

	public void onError()
	{
		if (unitOfWork == null || session == null) return;

		try { rollbackTransaction(unitOfWork, session); }
		finally { onFinish(); }
	}

	public void onFinish()
	{
		try
		{
			if (sessionCreated && session != null) session.close();
		}
		finally
		{
			session = null;
			if (sessionCreated) ManagedSessionContext.unbind(sessionFactory);
		}
	}

	protected void configureSession()
	{
		if ((null == unitOfWork) || (null == session))
			throw new NullPointerException("unitOfWork or session is null. This is a bug!");

		session.setDefaultReadOnly(unitOfWork.readOnly());
		session.setCacheMode(unitOfWork.cacheMode());
		session.setHibernateFlushMode(unitOfWork.flushMode());
	}

	protected void validateSession()
	{
		if ((null == unitOfWork) || (null == session))
			throw new NullPointerException("unitOfWork or session is null. This is a bug!");

		if (unitOfWork.readOnly() != session.isDefaultReadOnly())
			throw new IllegalStateException(String.format(
				"Existing session readOnly state (%b) does not match requested state (%b)",
				session.isDefaultReadOnly(),
				unitOfWork.readOnly()));

		if (unitOfWork.cacheMode() != session.getCacheMode())
			throw new IllegalStateException(String.format(
				"Existing session cache mode (%s) does not match requested mode (%s)",
				session.getCacheMode(),
				unitOfWork.cacheMode()));

		if (unitOfWork.flushMode() != session.getHibernateFlushMode())
			throw new IllegalStateException(String.format(
				"Existing session flush mode (%s) does not match requested mode (%s)",
				session.getHibernateFlushMode(),
				unitOfWork.flushMode()));

		var txn = session.getTransaction();
		if (unitOfWork.transactional() != ((null != txn) && txn.isActive()))
			throw new IllegalStateException(String.format(
				"Existing session transaction state (%s) does not match requested (%b)",
				txn == null ? "NULL" : Boolean.valueOf(txn.isActive()),
				unitOfWork.transactional()));
	}

	private void beginTransaction(final UnitOfWork unitOfWork, final Session session)
	{
		if (!unitOfWork.transactional()) return;

		var txn = session.getTransaction();
		if ((null != txn) && txn.isActive())
			transactionStarted = false;
		else
		{
			session.beginTransaction();
			transactionStarted = true;
		}
	}

	private void rollbackTransaction(final UnitOfWork unitOfWork, final Session session)
	{
		if (!unitOfWork.transactional()) return;

		var txn = session.getTransaction();
		if (transactionStarted && (null != txn) && txn.getStatus().canRollback()) txn.rollback();
	}

	private void commitTransaction(final UnitOfWork unitOfWork, final Session session)
	{
		if (!unitOfWork.transactional()) return;

		var txn = session.getTransaction();
		if (transactionStarted && (null != txn) && txn.getStatus().canRollback()) txn.commit();
	}

	protected Session getSession() { return requireNonNull(session); }
	protected SessionFactory getSessionFactory() { return requireNonNull(sessionFactory); }
}
