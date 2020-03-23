package app.allclear.common.task;

import java.util.function.Consumer;

import org.hibernate.*;
import org.junit.*;
import org.junit.runners.MethodSorters;

import app.allclear.junit.hibernate.HibernateRule;
import app.allclear.junit.hibernate.HibernateTransactionRule;
import app.allclear.common.entity.*;

/** Functional test class that verifies the AbstractHibernateTask.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractHibernateTaskTest
{
	@ClassRule
	public static final HibernateRule RULE = new HibernateRule(Country.class, User.class);

	@Test
	public void add_country() throws Exception
	{
		Country record = new Country("00", "Zero Land", "000", "000");
		Assert.assertTrue("Success", (new AbstractHibernateTask<Country>(RULE.getSessionFactory()) {
			@Override
			public boolean process(Country request, Session session) {
				session.persist(request);
				return true;
			}
		}).process(record));
	}

	@Test
	public void add_country_check()
	{
		doWork(session -> {
			Assert.assertNotNull("Exists", session.get(Country.class, "00"));
		});
	}

	@Test
	public void add_user() throws Exception
	{
		Assert.assertTrue("Success", (new AbstractHibernateTask<User>(RULE.getSessionFactory()) {
			@Override public boolean readOnly() { return true; }
			@Override public boolean transactional() { return false; }

			@Override
			public boolean process(User request, Session session) {
				session.persist(request);
				return true;
			}
		}).process(new User("tom@brady.com", "Thomas", "Brady", "MA", "US")));
	}

	@Test
	public void add_user_check()
	{
		// Since the operation was non-transactional, the User record was not persisted.
		doWork(session -> {
			Assert.assertNull("Exists", session.getNamedQuery("findUserByEmail").setParameter("email", "tom@brady.com").uniqueResult());
		});
	}

	@Test
	public void add_user_with_transaction() throws Exception
	{
		Assert.assertTrue("Success", (new AbstractHibernateTask<User>(RULE.getSessionFactory()) {
			@Override
			public boolean process(User request, Session session) {
				session.persist(request);
				return true;
			}
		}).process(new User("tom@brady.com", "Thomas", "Brady", "MA", "US")));
	}

	@Test
	public void add_user_with_transaction_check()
	{
		doWork(session -> {
			Assert.assertNotNull("Exists", session.getNamedQuery("findUserByEmail").setParameter("email", "tom@brady.com").uniqueResult());
		});
	}

	/** Helper method - perform Session related activities. */
	private void doWork(Consumer<Session> fx)
	{
		HibernateTransactionRule.doWork(RULE, fx);
	}
}
