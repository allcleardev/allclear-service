package app.allclear.junit.hibernate;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.rules.ExternalResource;
import org.hibernate.*;
import org.hibernate.context.internal.ManagedSessionContext;
import org.slf4j.*;

import io.dropwizard.testing.junit5.DropwizardExtension;

/** Used in conjuction with the HibernateRule to manage the transaction on each individual test.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class HibernateTransactionRule extends ExternalResource implements DropwizardExtension
{
	private static final Logger log = LoggerFactory.getLogger(HibernateTransactionRule.class);

	/** Session factory used by all tests. */
	public SessionFactory getSessionFactory() { return sessionFactory; }
	private SessionFactory sessionFactory = null;
	private final HibernateRule classRule;

	/** Transaction for the current test. */
	public Session getSession() { return session; }
	private Session session = null;
	public void flush()
	{
		session.flush();
	}

	public Transaction getTrans() { return trans; }
	private Transaction trans = null;
	public void rollback()
	{
		if (null != trans)
		{
			if (trans.getStatus().canRollback())
				trans.rollback();
			else
				log.warn("Transaction could NOT be committed. The state '{}' must be active.", trans.getStatus());

			trans = null;
		}
	}

	/** Populator.
	 * 
	 * @param classes
	 */
	public HibernateTransactionRule(final HibernateRule classRule)
	{
		this.classRule = classRule;
	}

	@Override
	public void before() throws Exception
	{
        ManagedSessionContext.bind(session = classRule.getSessionFactory().openSession());
		trans = session.beginTransaction();
	}

	@Override
	public void after()
	{
		if (null != trans)
		{
			if (trans.getStatus().canRollback())
				trans.commit();
			else
				log.warn("Transaction could NOT be committed. The state '{}' must be active.", trans.getStatus());
		}
		session.close();
        ManagedSessionContext.unbind(classRule.getSessionFactory());
	}

	/** Discards the existing transaction and reissues. Needed for tests that manage their own transactions individually. */
	public Transaction reissue()
	{
		return (trans = session.beginTransaction());
	}

	/** Commit the existing transaction and start a new transaction. */
	public void restart() throws Exception
	{
		after();
		before();
	}

	/** Utility method - provides a managed session from an existing HibernateRule (SessionFactory).
	 *  Useful for classes that do not want the HibernateTransactionRule wrapping every method.
	 * 
	 * @param rule HibernateRule with SessionFactory
	 * @param fx function to execute
	 */
	public static void doWork(final HibernateRule rule, final Consumer<Session> fx)
	{
		var session = rule.getSessionFactory().openSession();
		try
		{
			ManagedSessionContext.bind(session);
			fx.accept(session);
		}

		finally
		{
			session.close();
			ManagedSessionContext.unbind(rule.getSessionFactory());
		}
	}

	/** Utility method - provides a managed session from an existing HibernateRule (SessionFactory).
	 *  Useful for classes that do not want the HibernateTransactionRule wrapping every method.
	 * 
	 * @param rule HibernateRule with SessionFactory
	 * @param fx function to execute
	 * @return the result of the supplied function.
	 */
	public static <T> T withWork(final HibernateRule rule, final Function<Session, T> fx)
	{
		var session = rule.getSessionFactory().openSession();
		try
		{
			ManagedSessionContext.bind(session);
			return fx.apply(session);
		}

		finally
		{
			session.close();
			ManagedSessionContext.unbind(rule.getSessionFactory());
		}
	}

	/** Utility method - provides a managed session from an existing HibernateRule (SessionFactory).
	 *  Useful for classes that do not want the HibernateTransactionRule wrapping every method.
	 * 
	 * @param rule HibernateRule with SessionFactory
	 * @param fx function to execute
	 */
	public static void doTrans(final HibernateRule rule, final Consumer<Session> fx)
	{
		var session = rule.getSessionFactory().openSession();
		Transaction trans = null;
		try
		{
			ManagedSessionContext.bind(session);
			trans = session.beginTransaction();
			fx.accept(session);
			if (trans.getStatus().canRollback())
				trans.commit();
			else
				log.warn("Transaction could NOT be committed. The state '{}' must be active.", trans.getStatus());
		}

		catch (final Throwable ex)
		{
			if (null != trans)
			{
				if (trans.getStatus().canRollback())
					trans.rollback();
				else
					log.warn("Transaction could NOT be committed. The state '{}' must be active.", trans.getStatus());
			}

			throw ex;
		}
		finally
		{
			session.close();
			ManagedSessionContext.unbind(rule.getSessionFactory());
		}
	}
}
