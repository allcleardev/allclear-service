package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.platform.App;
import app.allclear.platform.type.HealthWorkerStatus;

/** Functional test class that verifies PeopleDAO.getOrAdd method.
 * 
 * @author smalleyd
 * @version 1.1.111
 * @since 7/31/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleDAOGetOrAddTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static PeopleDAO dao;
	private static String ID;

	@BeforeAll
	public static void beforeAll()
	{
		dao = new PeopleDAO(DAO_RULE.getSessionFactory());
	}

	@Test
	public void add()
	{
		Assertions.assertNull(dao.findByPhone("888-555-1000"));

		var record = dao.getOrAdd("888-555-1000", "Greg", "Landers");
		Assertions.assertNotNull(record);

		assertThat(ID = record.getId()).as("Check ID").isNotNull().hasSize(6);
	}

	@Test
	public void add_check()
	{
		var record = dao.findByPhone("888-555-1000");
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(ID, record.getId(), "Check ID");
		Assertions.assertEquals("888-555-1000", record.getPhone(), "Check phone");
		Assertions.assertEquals("Greg", record.getFirstName(), "Check firstName");
		Assertions.assertEquals("Landers", record.getLastName(), "Check lastName");
		Assertions.assertEquals(HealthWorkerStatus.NEITHER.id, record.getHealthWorkerStatusId(), "Check healthWorkerStatusId");
		Assertions.assertFalse(record.isAlertable(), "Check alertable");
		Assertions.assertFalse(record.isActive(), "Check active");
	}

	@Test
	public void change_activate()
	{
		Assertions.assertTrue(dao.activateByPhone("888-555-1000"));
	}

	@Test
	public void change_activate_check()
	{
		var record = dao.findByPhone("888-555-1000");
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(ID, record.getId(), "Check ID");
		Assertions.assertEquals("888-555-1000", record.getPhone(), "Check phone");
		Assertions.assertEquals("Greg", record.getFirstName(), "Check firstName");
		Assertions.assertEquals("Landers", record.getLastName(), "Check lastName");
		Assertions.assertEquals(HealthWorkerStatus.NEITHER.id, record.getHealthWorkerStatusId(), "Check healthWorkerStatusId");
		Assertions.assertFalse(record.isAlertable(), "Check alertable");
		Assertions.assertTrue(record.isActive(), "Check active");
	}

	@Test
	public void change_modify()
	{
		var record = dao.findByPhone("888-555-1000");
		record.setFirstName("Benjamin");
		record.setLastName("Von Trapp");
		record.setHealthWorkerStatusId(HealthWorkerStatus.LIVE_WITH.id);
		record.setAlertable(true);
	}

	@Test
	public void change_modify_check()
	{
		var record = dao.getOrAdd("888-555-1000", "Betty", "White");
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(ID, record.getId(), "Check ID");
		Assertions.assertEquals("888-555-1000", record.getPhone(), "Check phone");
		Assertions.assertEquals("Benjamin", record.getFirstName(), "Check firstName");
		Assertions.assertEquals("Von Trapp", record.getLastName(), "Check lastName");
		Assertions.assertEquals(HealthWorkerStatus.LIVE_WITH.id, record.getHealthWorkerStatusId(), "Check healthWorkerStatusId");
		Assertions.assertTrue(record.isAlertable(), "Check alertable");
		Assertions.assertTrue(record.isActive(), "Check active");
	}

	@Test
	public void change_reactivate()
	{
		Assertions.assertFalse(dao.activateByPhone("888-555-1000"));
	}

	@Test
	public void change_reactivate_check()
	{
		change_modify_check();
	}
}
