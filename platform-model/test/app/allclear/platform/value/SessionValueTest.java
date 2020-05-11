package app.allclear.platform.value;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.*;

import app.allclear.common.ThreadUtils;
import app.allclear.platform.model.StartRequest;

/** Unit test class that verifies the SessionValue POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/25/2020
 *
 */

public class SessionValueTest
{
	@Test
	public void create_admin()
	{
		var o = new SessionValue(false, new AdminValue("andy", false));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, o.duration, "Check duration");
		Assertions.assertEquals(30 * 60, o.seconds(), "Check seconds");
		Assertions.assertNotNull(o.admin, "Check admin");
		Assertions.assertEquals("andy", o.admin.id, "Check admin.id");
		Assertions.assertFalse(o.admin.supers, "Check admin.supers");
		Assertions.assertNull(o.person, "Check person");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);
		Assertions.assertTrue(o.admin(), "Check admin()");
		Assertions.assertFalse(o.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertTrue(o.canAdmin(), "Check canAdmin()");
		Assertions.assertFalse(o.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertFalse(o.registration(), "Check registration()");
		Assertions.assertEquals("Admin", o.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);
	}

	@Test
	public void create_anonymous()
	{
		var o = SessionValue.anonymous();
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, o.duration, "Check duration");
		Assertions.assertEquals(30 * 60, o.seconds(), "Check seconds");
		Assertions.assertNull(o.admin, "Check admin");
		Assertions.assertNull(o.person, "Check person");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);
		Assertions.assertFalse(o.admin(), "Check admin()");
		Assertions.assertFalse(o.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertFalse(o.canAdmin(), "Check canAdmin()");
		Assertions.assertFalse(o.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertFalse(o.registration(), "Check registration()");
		Assertions.assertEquals("Anonymous", o.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);
	}

	@Test
	public void create_customer()
	{
		var o = new SessionValue(new CustomerValue("Techmo"));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, o.duration, "Check duration");
		Assertions.assertEquals(30 * 60, o.seconds(), "Check seconds");
		Assertions.assertNull(o.admin, "Check admin");
		Assertions.assertNull(o.person, "Check person");
		Assertions.assertNotNull(o.customer, "Check customer");
		Assertions.assertEquals("Techmo", o.customer.name, "Check customer.name");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);
		Assertions.assertFalse(o.admin(), "Check admin()");
		Assertions.assertFalse(o.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertFalse(o.canAdmin(), "Check canAdmin()");
		Assertions.assertFalse(o.person(), "Check person()");
		Assertions.assertTrue(o.customer(), "Check customer()");
		Assertions.assertFalse(o.registration(), "Check registration()");
		Assertions.assertEquals("Customer", o.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);
	}

	@Test
	public void create_editor()
	{
		var o = new SessionValue(false, new AdminValue("andy", false, true));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, o.duration, "Check duration");
		Assertions.assertEquals(30 * 60, o.seconds(), "Check seconds");
		Assertions.assertNotNull(o.admin, "Check admin");
		Assertions.assertEquals("andy", o.admin.id, "Check admin.id");
		Assertions.assertFalse(o.admin.supers, "Check admin.supers");
		Assertions.assertNull(o.person, "Check person");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);
		Assertions.assertTrue(o.admin(), "Check admin()");
		Assertions.assertFalse(o.supers(), "Check supers()");
		Assertions.assertTrue(o.editor(), "Check editor()");
		Assertions.assertFalse(o.canAdmin(), "Check canAdmin()");
		Assertions.assertFalse(o.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertFalse(o.registration(), "Check registration()");
		Assertions.assertEquals("Editor", o.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);

		Assertions.assertTrue(o.admin.withSupers(true).canAdmin(), "Check canAdmin()");
	}

	@Test
	public void create_people()
	{
		var o = new SessionValue(false, new PeopleValue("stevie", "888-555-0001", true));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, o.duration, "Check duration");
		Assertions.assertEquals(30 * 60, o.seconds(), "Check seconds");
		Assertions.assertNull(o.admin, "Check admin");
		Assertions.assertNotNull(o.person, "Check person");
		Assertions.assertEquals("stevie", o.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0001", o.person.phone, "Check person.phone");
		Assertions.assertTrue(o.person.active, "Check person.active");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);
		Assertions.assertFalse(o.admin(), "Check admin()");
		Assertions.assertFalse(o.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertFalse(o.canAdmin(), "Check canAdmin()");
		Assertions.assertTrue(o.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertFalse(o.registration(), "Check registration()");
		Assertions.assertEquals("Person", o.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);
	}

	@Test
	public void create_people_rememberMe()
	{
		var o = new SessionValue(true, new PeopleValue("moira", "888-555-0002", false));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertTrue(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_LONG, o.duration, "Check duration");
		Assertions.assertEquals(30 * 24 * 60 * 60, o.seconds(), "Check seconds");
		Assertions.assertNull(o.admin, "Check admin");
		Assertions.assertNotNull(o.person, "Check person");
		Assertions.assertEquals("moira", o.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0002", o.person.phone, "Check person.phone");
		Assertions.assertFalse(o.person.active, "Check person.active");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_LONG), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);
		Assertions.assertFalse(o.admin(), "Check admin()");
		Assertions.assertFalse(o.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertFalse(o.canAdmin(), "Check canAdmin()");
		Assertions.assertTrue(o.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertFalse(o.registration(), "Check registration()");
		Assertions.assertEquals("Person", o.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);
	}

	@Test
	public void create_registration()
	{
		var o = new SessionValue(new StartRequest("888-555-0003", true, true));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, o.duration, "Check duration");
		Assertions.assertEquals(30 * 60, o.seconds(), "Check seconds");
		Assertions.assertNull(o.admin, "Check admin");
		Assertions.assertNull(o.person, "Check person");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNotNull(o.registration, "Check registration");
		Assertions.assertEquals("+18885550003", o.registration.phone, "Check registration.phone");
		Assertions.assertTrue(o.registration.beenTested, "Check registration.beenTested");
		Assertions.assertTrue(o.registration.haveSymptoms, "Check registration.haveSymptoms");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);
		Assertions.assertFalse(o.admin(), "Check admin()");
		Assertions.assertFalse(o.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertFalse(o.canAdmin(), "Check canAdmin()");
		Assertions.assertFalse(o.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertTrue(o.registration(), "Check registration()");
		Assertions.assertEquals("Registration", o.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);

		ThreadUtils.sleep(2000L);

		var s = o.promote(true, new PeopleValue("johnny", "888-555-0004", true));
		assertThat(s.id).as("Check ID").hasSize(36);
		Assertions.assertTrue(s.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_LONG, s.duration, "Check duration");
		Assertions.assertEquals(30 * 24 * 60 * 60, s.seconds(), "Check seconds");
		Assertions.assertNull(o.admin, "Check admin");
		Assertions.assertNotNull(s.person, "Check person");
		Assertions.assertEquals("johnny", s.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0004", s.person.phone, "Check person.phone");
		Assertions.assertTrue(s.person.active, "Check person.active");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNull(s.registration, "Check registration");
		assertThat(s.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_LONG), 100L).isAfter(o.expiresAt);
		assertThat(s.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isAfter(o.createdAt).isAfter(o.lastAccessedAt);
		Assertions.assertFalse(s.admin(), "Check admin()");
		Assertions.assertFalse(s.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertFalse(o.canAdmin(), "Check canAdmin()");
		Assertions.assertTrue(s.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertFalse(s.registration(), "Check registration()");
		Assertions.assertEquals("Person", s.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var ss = o.promote(false, new PeopleValue("barbara", "888-555-0005", true));
		assertThat(ss.id).as("Check ID").hasSize(36);
		Assertions.assertFalse(ss.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, ss.duration, "Check duration");
		Assertions.assertEquals(30 * 60, ss.seconds(), "Check seconds");
		Assertions.assertNull(o.admin, "Check admin");
		Assertions.assertNotNull(ss.person, "Check person");
		Assertions.assertEquals("barbara", ss.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0005", ss.person.phone, "Check person.phone");
		Assertions.assertTrue(ss.person.active, "Check person.active");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNull(ss.registration, "Check registration");
		assertThat(ss.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_SHORT), 100L).isAfter(o.expiresAt);
		assertThat(ss.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isAfter(o.createdAt).isAfter(s.lastAccessedAt);
		Assertions.assertFalse(ss.admin(), "Check admin()");
		Assertions.assertFalse(ss.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertFalse(o.canAdmin(), "Check canAdmin()");
		Assertions.assertTrue(ss.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertFalse(ss.registration(), "Check registration()");
		Assertions.assertEquals("Person", ss.type(), "Check type()");
	}

	@Test
	public void create_supers_rememberMe()
	{
		var o = new SessionValue(true, new AdminValue("mandy", true));
		assertThat(o.id).as("Check ID").hasSize(36);
		Assertions.assertTrue(o.rememberMe, "Check rememberMe");
		Assertions.assertEquals(SessionValue.DURATION_LONG, o.duration, "Check duration");
		Assertions.assertEquals(30 * 24 * 60 * 60, o.seconds(), "Check seconds");
		Assertions.assertNotNull(o.admin, "Check admin");
		Assertions.assertEquals("mandy", o.admin.id, "Check admin.id");
		Assertions.assertTrue(o.admin.supers, "Check admin.supers");
		Assertions.assertNull(o.person, "Check person");
		Assertions.assertNull(o.customer, "Check customer");
		Assertions.assertNull(o.registration, "Check registration");
		assertThat(o.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_LONG), 100L);
		assertThat(o.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isEqualTo(o.createdAt);
		Assertions.assertTrue(o.admin(), "Check admin()");
		Assertions.assertTrue(o.supers(), "Check supers()");
		Assertions.assertFalse(o.editor(), "Check editor()");
		Assertions.assertTrue(o.canAdmin(), "Check canAdmin()");
		Assertions.assertFalse(o.person(), "Check person()");
		Assertions.assertFalse(o.customer(), "Check customer()");
		Assertions.assertFalse(o.registration(), "Check registration()");
		Assertions.assertEquals("Super", o.type(), "Check type()");

		ThreadUtils.sleep(2000L);

		var expiresAt = new Date(o.expiresAt.getTime());
		var lastAccessedAt = new Date(o.lastAccessedAt.getTime());
		assertThat(o.accessed().expiresAt).as("Check accessed: expiresAt").isAfter(expiresAt);
		assertThat(o.accessed().lastAccessedAt).as("Check accessed: lastAccessedAt").isAfter(lastAccessedAt);
	}
}
