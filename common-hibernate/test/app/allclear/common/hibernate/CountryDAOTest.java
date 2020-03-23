package app.allclear.common.hibernate;

import org.junit.*;
import org.junit.runners.MethodSorters;

import app.allclear.junit.hibernate.*;
import app.allclear.common.entity.Country;

/** Functional test class that verifies the AbstractDAO base component. Uses
 *  the CountryDAO as a concrete implementation of the AbstractDAO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountryDAOTest
{
	@ClassRule
	public static final HibernateRule DAO_RULE = new HibernateRule(Country.class);

	@Rule
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static CountryDAO dao;
	private static int commits = 0;
	private static int commits_ = 0;
	private static int rollbacks = 0;
	private static int rollbacks_ = 0;

	@BeforeClass
	public static void up()
	{
		dao = new CountryDAO(DAO_RULE.getSessionFactory());
	}

	@AfterClass
	public static void down()
	{
		// doWork does NOT create a transaction. AbstractDAO.afterTrans should just ignore the callback if there is not transaction.
		HibernateTransactionRule.doWork(DAO_RULE, s -> {
			Assert.assertNotNull(dao.find("00", ()-> commits_++));
		});

		Assert.assertEquals(commits, commits_);	// No change.

		// doTransaction DOES create a transaction. AbstractDAO.afterTrans should execute the callback.
		HibernateTransactionRule.doTrans(DAO_RULE, s -> {
			Assert.assertNotNull(dao.find("00", ()-> commits_++));
		});

		Assert.assertEquals(++commits, commits_);
	}

	@Before
	public void beforeEach()
	{
		Assert.assertEquals("Check commits", commits, commits_);
		Assert.assertEquals("Check rollbacks", rollbacks, rollbacks_);
	}

	@Test
	public void add()
	{
		Assert.assertEquals("Before Add", 0, commits);
		Assert.assertNotNull(dao.add("00", "Zero", "000", "000", () -> commits_++));
		Assert.assertEquals("After Add", 0, commits);	// Transaction NOT committed yet.

		commits++;
	}

	@Test
	public void add_00_rollback()
	{
		dao.add("01", null, "001", "001", () -> commits_++, () -> rollbacks_++);
		transRule.rollback();

		rollbacks++;
	}

	@Test
	public void add_00_success()
	{
		dao.add("01", "First country", "001", "001", () -> commits_++, () -> rollbacks_++);

		commits++;
	}

	@Test
	public void check()
	{
		Assert.assertEquals(2, commits_);
		Assert.assertEquals(1, rollbacks_);
	}

	@Test
	public void get()
	{
		final Country record = dao.find("00", () -> commits_++);
		Assert.assertNotNull("Exists", record);
		Assert.assertEquals("Check ID", "00", record.id);
		Assert.assertEquals("Check name", "Zero", record.name);
		Assert.assertEquals("Check code", "000", record.code);
		Assert.assertEquals("Check numCode", "000", record.numCode);
		Assert.assertTrue("Check active", record.active);

		commits++;
	}

	@Test
	public void rollback()
	{
		Assert.assertNotNull(dao.add("02", "Two", "002", "00b", () -> commits_++));

		transRule.rollback();
	}

	@Test
	public void rollback_check()
	{
		Assert.assertEquals(3, commits_);
	}

	@Test
	public void rollback_get() throws Exception
	{
		dao.find("00", () -> commits_++);
		transRule.rollback();	// Without a transaction available for the session, it should not run the function.
	}

	@Test
	public void rollback_get_check() throws Exception
	{	
		Assert.assertEquals(3, commits_);
	}

	@Test
	public void rollback_get_more()
	{
		Assert.assertNull(dao.find("02", ()-> commits_++));

		commits++;
	}

	@Test
	public void rollback_get_more_check() throws Exception
	{
		Assert.assertEquals(4, commits_);
	}
}
