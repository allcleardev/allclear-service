package app.allclear.common.hibernate;

import java.util.List;
import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.junit.*;
import org.junit.runners.MethodSorters;

import app.allclear.common.entity.Country;
import app.allclear.junit.hibernate.HibernateRule;

/** Functional test class that verifies the AbstractHibernateRunner class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractHibernateRunnerTest
{
	@ClassRule
	public static final HibernateRule RULE = new HibernateRule(Country.class);

	@Test
	public void add() throws Exception
	{
		Country record = (new AbstractHibernateRunner<Country, Country>(RULE.getSessionFactory()) {
			@Override public boolean readOnly() { return false; }
			@Override public boolean transactional() { return true; }
			@Override
			public Country run(Country r, Session session) {
				session.persist(r);

				return r;
			}
		}).run(new Country("99", "Ninety-nine Country", "999", "990", false));

		Assert.assertNotNull("Exists", record);
		Assert.assertEquals("Check ID", "99", record.id);
		Assert.assertEquals("Check name", "Ninety-nine Country", record.name);
		Assert.assertEquals("Check code", "999", record.code);
		Assert.assertEquals("Check numCode", "990", record.numCode);
		Assert.assertFalse("Check active", record.active);
	}

	@Test(expected=PersistenceException.class)
	public void add_constraintException() throws Exception
	{
		(new AbstractHibernateRunner<Country, Country>(RULE.getSessionFactory()) {
			@Override public boolean readOnly() { return true; }
			@Override public boolean transactional() { return true; }
			@Override
			public Country run(Country r, Session session) {
				session.persist(r);

				return r;
			}
		}).run(new Country("US", "United States X", "000", "000", false));
	}

	@Test
	public void add_nonTransaction() throws Exception
	{
		// Doesn't actual add the country because it's non-transactional.
		(new AbstractHibernateRunner<Country, Country>(RULE.getSessionFactory()) {
			@Override public boolean readOnly() { return true; }
			@Override public boolean transactional() { return false; }
			@Override
			public Country run(Country r, Session session) {
				session.persist(r);

				return r;
			}
		}).run(new Country("90", "Ninety Country", "900", "900", false));
	}

	@Test
	public void getActive() throws Exception
	{
		List<Country> records = runQuery("SELECT OBJECT(o) FROM Country o WHERE o.active = TRUE");

		Assert.assertNotNull("Exists", records);
		Assert.assertEquals("Check size", 245, records.size());
	}

	@Test
	public void getInactive() throws Exception
	{
		List<Country> records = runQuery("SELECT OBJECT(o) FROM Country o WHERE o.active = FALSE");

		Assert.assertNotNull("Exists", records);
		Assert.assertEquals("Check size", 1, records.size());
	}

	@Test
	public void getAll() throws Exception
	{
		List<Country> records = runQuery("SELECT OBJECT(o) FROM Country o");

		Assert.assertNotNull("Exists", records);
		Assert.assertEquals("Check size", 246, records.size());
	}

	@Test
	public void getById() throws Exception
	{
		Country record = getById("99");

		Assert.assertNotNull("Exists", record);
		Assert.assertEquals("Check ID", "99", record.id);
		Assert.assertEquals("Check name", "Ninety-nine Country", record.name);
		Assert.assertEquals("Check code", "999", record.code);
		Assert.assertEquals("Check numCode", "990", record.numCode);
		Assert.assertFalse("Check active", record.active);
	}

	@Test
	public void modify() throws Exception
	{
		(new AbstractHibernateRunner<String, Country>(RULE.getSessionFactory()) {
			@Override public boolean readOnly() { return false; }
			@Override public boolean transactional() { return true; }
			@Override
			public Country run(String newName, Session session) {
				Country r = (Country) session.get(Country.class, "99");
				r.name = newName;

				return r;
			}
		}).run("A different country name");
	}

	@Test
	public void modify_check() throws Exception
	{
		Country record = getById("99");

		Assert.assertNotNull("Exists", record);
		Assert.assertEquals("Check ID", "99", record.id);
		Assert.assertEquals("Check name", "A different country name", record.name);
		Assert.assertEquals("Check code", "999", record.code);
		Assert.assertEquals("Check numCode", "990", record.numCode);
		Assert.assertFalse("Check active", record.active);
	}

	@Test
	public void remove() throws Exception
	{
		(new AbstractHibernateRunner<String, Country>(RULE.getSessionFactory()) {
			@Override public boolean readOnly() { return false; }
			@Override public boolean transactional() { return true; }
			@Override
			public Country run(String id, Session session) {
				Country r = (Country) session.get(Country.class, "99");
				session.delete(r);

				return r;
			}
		}).run("99");
	}

	@Test
	public void remove_check() throws Exception
	{
		Assert.assertNull("Exists", getById("99"));
	}

	@Test
	public void remove_getAll() throws Exception
	{
		List<Country> records = runQuery("SELECT OBJECT(o) FROM Country o");

		Assert.assertNotNull("Exists", records);
		Assert.assertEquals("Check size", 246 - 1, records.size());	// Removed record.
	}

	/** Helper method - gets by ID. */
	private Country getById(final String id) throws Exception
	{
		return (new AbstractHibernateRunner<String, Country>(RULE.getSessionFactory()) {
			@Override public boolean readOnly() { return true; }
			@Override public boolean transactional() { return false; }
			@Override
			public Country run(String id, Session session) {
				return (Country) session.get(Country.class, id);
			}
		}).run(id);
	}

	/** Helper method - runs a query. */
	public List<Country> runQuery(final String query) throws Exception
	{
		return (new AbstractHibernateRunner<Long, List<Country>>(RULE.getSessionFactory()) {
			@Override public boolean readOnly() { return true; }
			@Override public boolean transactional() { return false; }
			@Override @SuppressWarnings("unchecked")
			public List<Country> run(Long ignore, Session session) {
				return session.createQuery(query).list();
			}
		}).run(1L);
	}
}
