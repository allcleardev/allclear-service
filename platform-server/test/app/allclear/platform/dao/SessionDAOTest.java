package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.*;

import app.allclear.common.errors.NotAuthenticatedException;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.model.StartRequest;
import app.allclear.platform.value.*;

/** Functional test class that verifies the SessionDAO component.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/25/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class SessionDAOTest
{
	private static final FakeRedisClient redis = new FakeRedisClient();
	private static final SessionDAO dao = new SessionDAO(redis);

	private static SessionValue START;
	private static SessionValue START_1;
	private static SessionValue PERSON;
	private static SessionValue PERSON_1;

	@Test
	public void add_person()
	{
		Assertions.assertNotNull(PERSON = dao.add(new PeopleValue("kim", "888-555-0000", true), false));
		Assertions.assertNotNull(PERSON, "Exists");
		Assertions.assertNull(PERSON.registration, "Check registration");
		Assertions.assertNotNull(PERSON.person, "Check person");
		Assertions.assertEquals(30 * 60, PERSON.seconds(), "Check seconds");
	}

	@Test
	public void add_person_rememberMe()
	{
		Assertions.assertNotNull(PERSON_1 = dao.add(new PeopleValue("rick", "888-555-0001", true), true));
		Assertions.assertNotNull(PERSON_1, "Exists");
		Assertions.assertNull(PERSON_1.registration, "Check registration");
		Assertions.assertNotNull(PERSON_1.person, "Check person");
		Assertions.assertEquals(30 * 24 * 60 * 60, PERSON_1.seconds(), "Check seconds");
	}

	@Test
	public void add_start()
	{
		Assertions.assertNotNull(START = dao.add(new StartRequest("888-555-0002", false, false)));
		Assertions.assertNotNull(START, "Exists");
		Assertions.assertNotNull(START.registration, "Check registration");
		Assertions.assertNull(START.person, "Check person");
		Assertions.assertEquals(30 * 60, START.seconds(), "Check seconds");
	}

	@Test
	public void add_start1()
	{
		Assertions.assertNotNull(START_1 = dao.add(new StartRequest("888-555-0005", true, true)));
		Assertions.assertNotNull(START, "Exists");
		Assertions.assertNotNull(START.registration, "Check registration");
		Assertions.assertNull(START.person, "Check person");
		Assertions.assertEquals(30 * 60, START.seconds(), "Check seconds");
	}

	@Test
	public void get_invalid()
	{
		assertThat(Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get("invalid")))
			.hasMessage("The ID 'invalid' is invalid.");
	}

	@Test
	public void get_person()
	{
		var v = dao.get(PERSON.id);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNull(v.registration, "Check registration");
		Assertions.assertNotNull(v.person, "Check person");
		Assertions.assertEquals("kim", v.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0000", v.person.phone, "Check person.phone");
		Assertions.assertEquals(30 * 60, v.seconds(), "Check seconds");
		assertThat(v.expiresAt).as("Check expiresAt").isAfter(PERSON.expiresAt);
		assertThat(v.lastAccessedAt).as("Check lastAccessedAt").isAfter(PERSON.lastAccessedAt);
		assertThat(v.createdAt).as("Check createdAt").isInSameSecondAs(PERSON.createdAt).isBefore(v.lastAccessedAt);
	}

	@Test
	public void get_person_rememberMe()
	{
		var v = dao.get(PERSON_1.id);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNull(v.registration, "Check registration");
		Assertions.assertNotNull(v.person, "Check person");
		Assertions.assertEquals("rick", v.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0001", v.person.phone, "Check person.phone");
		Assertions.assertEquals(30 * 24 * 60 * 60, v.seconds(), "Check seconds");
		assertThat(v.expiresAt).as("Check expiresAt").isAfter(PERSON_1.expiresAt);
		assertThat(v.lastAccessedAt).as("Check lastAccessedAt").isAfter(PERSON_1.lastAccessedAt);
		assertThat(v.createdAt).as("Check createdAt").isInSameSecondAs(PERSON_1.createdAt).isBefore(v.lastAccessedAt);
	}

	@Test
	public void get_start()
	{
		var v = dao.get(START.id);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNotNull(v.registration, "Check registration");
		Assertions.assertEquals("888-555-0002", v.registration.phone, "Check registration.phone");
		Assertions.assertFalse(v.registration.beenTested, "Check registration.beenTested");
		Assertions.assertFalse(v.registration.haveSymptoms, "Check registration.haveSymptoms");
		Assertions.assertNull(v.person, "Check person");
		Assertions.assertEquals(30 * 60, v.seconds(), "Check seconds");
		assertThat(v.expiresAt).as("Check expiresAt").isAfter(START.expiresAt);
		assertThat(v.lastAccessedAt).as("Check lastAccessedAt").isAfter(START.lastAccessedAt);
		assertThat(v.createdAt).as("Check createdAt").isInSameSecondAs(START.createdAt).isBefore(v.lastAccessedAt);
	}

	@Test
	public void get_start1()
	{
		var v = dao.get(START_1.id);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNotNull(v.registration, "Check registration");
		Assertions.assertEquals("888-555-0005", v.registration.phone, "Check registration.phone");
		Assertions.assertTrue(v.registration.beenTested, "Check registration.beenTested");
		Assertions.assertTrue(v.registration.haveSymptoms, "Check registration.haveSymptoms");
		Assertions.assertNull(v.person, "Check person");
		Assertions.assertEquals(30 * 60, v.seconds(), "Check seconds");
		assertThat(v.expiresAt).as("Check expiresAt").isAfter(START_1.expiresAt);
		assertThat(v.lastAccessedAt).as("Check lastAccessedAt").isAfter(START_1.lastAccessedAt);
		assertThat(v.createdAt).as("Check createdAt").isInSameSecondAs(START_1.createdAt).isBefore(v.lastAccessedAt);
	}

	@Test
	public void promote()
	{
		assertThat(Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.promote(new PeopleValue("morty", "888-555-0003", true), true)))
			.hasMessage("No current session is available.");
	}

	@Test
	public void promote_start()
	{
		dao.current(START);

		var v = dao.promote(new PeopleValue("morty", "888-555-0003", true), true);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNull(v.registration, "Check registration");
		Assertions.assertNotNull(v.person, "Check person");
		Assertions.assertEquals("morty", v.person.name, "Check person.name");
		Assertions.assertEquals("888-555-0003", v.person.phone, "Check person.phone");
		Assertions.assertEquals(30 * 24 * 60 * 60, v.seconds(), "Check seconds");
		assertThat(v.expiresAt).as("Check expiresAt").isCloseTo(new Date(System.currentTimeMillis() + SessionValue.DURATION_LONG), 100L).isAfter(START.expiresAt);
		assertThat(v.lastAccessedAt).as("Check lastAccessedAt").isCloseTo(new Date(), 100L).isAfter(START.lastAccessedAt);
		assertThat(v.createdAt).as("Check createdAt").isInSameSecondAs(START.createdAt).isBefore(v.lastAccessedAt);
	}

	@Test
	public void remove_person()
	{
		dao.remove(PERSON.id);

		Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get(PERSON.id));
	}

	@Test
	public void remove_person_1()
	{
		dao.remove(PERSON_1.id);

		Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get(PERSON_1.id));
	}

	@Test
	public void remove_start()
	{
		dao.remove(START.id);

		Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get(START.id));
	}

	@Test
	public void remove_start1()
	{
		dao.remove(START_1.id);

		Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get(START_1.id));
	}
}
