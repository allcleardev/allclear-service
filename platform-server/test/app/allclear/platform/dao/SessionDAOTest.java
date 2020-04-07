package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.allclear.common.errors.*;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.model.StartRequest;
import app.allclear.platform.value.*;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.SMSRequest;
import app.allclear.twilio.model.SMSResponse;

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
	public static final Pattern PATTERN_TOKEN = Pattern.compile("[A-Z0-9]{6}");
	public static final String MESSAGE = "%s\nUse this code for AllClear verification or click https://app-test.allclear.app/auth?phone=%s&token=%s";

	private static final FakeRedisClient redis = new FakeRedisClient();
	private static final TwilioClient twilio = mock(TwilioClient.class);
	private static final SessionDAO dao = new SessionDAO(redis, twilio, ConfigTest.loadTest());

	private static SessionValue ADMIN;
	private static SessionValue PERSON;
	private static SessionValue PERSON_1;
	private static SessionValue START;
	private static SessionValue START_1;
	private static SessionValue SUPER;
	private static SMSResponse LAST_RESPONSE;

	@BeforeAll
	public static void up()
	{
		when(twilio.send(any(SMSRequest.class))).thenAnswer(a -> LAST_RESPONSE = new SMSResponse(((SMSRequest) a.getArgument(0))));
	}

	@Test
	public void add_admin()
	{
		Assertions.assertNotNull(ADMIN = dao.add(new AdminValue("tim", false), false));
		Assertions.assertNotNull(ADMIN, "Exists");
		Assertions.assertNotNull(ADMIN.admin, "Check admin");
		Assertions.assertEquals("tim", ADMIN.admin.id, "Check admin.id");
		Assertions.assertTrue(ADMIN.admin(), "Check admin()");
		Assertions.assertFalse(ADMIN.supers(), "Check supers()");
		Assertions.assertNull(ADMIN.registration, "Check registration");
		Assertions.assertNull(ADMIN.person, "Check person");
		Assertions.assertEquals(30 * 60, ADMIN.seconds(), "Check seconds");
		Assertions.assertEquals(30L * 60L, redis.ttl(SessionDAO.key(ADMIN.id)), "Check expiration");
	}

	@Test
	public void add_person()
	{
		Assertions.assertNotNull(PERSON = dao.add(new PeopleValue("kim", "888-555-0000", true), false));
		Assertions.assertNotNull(PERSON, "Exists");
		Assertions.assertNull(PERSON.admin, "Check admin");
		Assertions.assertNull(PERSON.registration, "Check registration");
		Assertions.assertNotNull(PERSON.person, "Check person");
		Assertions.assertEquals("888-555-0000", PERSON.person.phone, "Check person.phone");
		Assertions.assertEquals(30 * 60, PERSON.seconds(), "Check seconds");
		Assertions.assertEquals(30L * 60L, redis.ttl(SessionDAO.key(PERSON.id)), "Check expiration");
	}

	@Test
	public void add_person_rememberMe()
	{
		Assertions.assertNotNull(PERSON_1 = dao.add(new PeopleValue("rick", "888-555-0001", true), true));
		Assertions.assertNotNull(PERSON_1, "Exists");
		Assertions.assertNull(PERSON_1.admin, "Check admin");
		Assertions.assertNull(PERSON_1.registration, "Check registration");
		Assertions.assertNotNull(PERSON_1.person, "Check person");
		Assertions.assertEquals("888-555-0001", PERSON_1.person.phone, "Check person.phone");
		Assertions.assertEquals(30 * 24 * 60 * 60, PERSON_1.seconds(), "Check seconds");
		Assertions.assertEquals(30L * 24L * 60L * 60L, redis.ttl(SessionDAO.key(PERSON_1.id)), "Check expiration");
	}

	@Test
	public void add_start()
	{
		Assertions.assertNotNull(START = dao.add(new StartRequest("888-555-0002", false, false)));
		Assertions.assertNotNull(START, "Exists");
		Assertions.assertNull(START.admin, "Check admin");
		Assertions.assertNotNull(START.registration, "Check registration");
		Assertions.assertEquals("+18885550002", START.registration.phone, "Check registration.phone");
		Assertions.assertFalse(START.registration.beenTested, "Check registration.beenTested");
		Assertions.assertFalse(START.registration.haveSymptoms, "Check registration.haveSymptoms");
		Assertions.assertNull(START.person, "Check person");
		Assertions.assertEquals(30 * 60, START.seconds(), "Check seconds");
		Assertions.assertEquals(30L * 60L, redis.ttl(SessionDAO.key(START.id)), "Check expiration");
	}

	@Test
	public void add_start1()
	{
		Assertions.assertNotNull(START_1 = dao.add(new StartRequest("888-555-0005", true, true)));
		Assertions.assertNotNull(START_1, "Exists");
		Assertions.assertNull(START_1.admin, "Check admin");
		Assertions.assertNotNull(START_1.registration, "Check registration");
		Assertions.assertEquals("+18885550005", START_1.registration.phone, "Check registration.phone");
		Assertions.assertTrue(START_1.registration.beenTested, "Check registration.beenTested");
		Assertions.assertTrue(START_1.registration.haveSymptoms, "Check registration.haveSymptoms");
		Assertions.assertNull(START_1.person, "Check person");
		Assertions.assertEquals(30 * 60, START_1.seconds(), "Check seconds");
		Assertions.assertEquals(30L * 60L, redis.ttl(SessionDAO.key(START_1.id)), "Check expiration");
	}

	@Test
	public void add_super()
	{
		Assertions.assertNotNull(SUPER = dao.add(new AdminValue("naomi", true), true));
		Assertions.assertNotNull(SUPER, "Exists");
		Assertions.assertNotNull(SUPER.admin, "Check admin");
		Assertions.assertEquals("naomi", SUPER.admin.id, "Check admin.id");
		Assertions.assertTrue(SUPER.admin(), "Check admin()");
		Assertions.assertTrue(SUPER.supers(), "Check supers()");
		Assertions.assertNull(SUPER.registration, "Check registration");
		Assertions.assertNull(SUPER.person, "Check person");
		Assertions.assertEquals(30 * 24 * 60 * 60, SUPER.seconds(), "Check seconds");
		Assertions.assertEquals(30L * 24L * 60L * 60L, redis.ttl(SessionDAO.key(SUPER.id)), "Check expiration");
	}

	@Test
	public void auth_failure()
	{
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.auth("888-555-0010", "ABC")))
			.hasMessage("Confirmation failed.");
	}

	@Test
	public void auth_success()
	{
		Assertions.assertNull(LAST_RESPONSE, "Check lastResponse: before");

		var token = dao.auth("888-555-0011");
		assertThat(token).as("Check token").isNotNull().hasSize(6).matches(PATTERN_TOKEN);
		Assertions.assertTrue(redis.containsKey(SessionDAO.authKey("888-555-0011", token)), "Check redis: before");
		Assertions.assertNotNull(LAST_RESPONSE, "Check lastResponse: after");
		Assertions.assertEquals(String.format(MESSAGE, token, "888-555-0011", token), LAST_RESPONSE.body, "Check lastResponse.body");

		dao.auth("888-555-0011", token);
		Assertions.assertFalse(redis.containsKey(SessionDAO.authKey("888-555-0011", token)), "Check redis: after");
	}

	public static Stream<Arguments> checkAdmin()
	{
		return Stream.of(
			arguments(ADMIN, true),
			arguments(PERSON, false),
			arguments(PERSON_1, false),
			arguments(START, false),
			arguments(START_1, false),
			arguments(SUPER, true));
	}

	@ParameterizedTest
	@MethodSource
	public void checkAdmin(final SessionValue value, final boolean success)
	{
		dao.current(value);
		if (success)
			Assertions.assertNotNull(dao.checkAdmin());
		else
			Assertions.assertThrows(NotAuthorizedException.class, () -> dao.checkAdmin());
	}

	public static Stream<Arguments> checkPerson()
	{
		return Stream.of(
			arguments(ADMIN, false),
			arguments(PERSON, true),
			arguments(PERSON_1, true),
			arguments(START, false),
			arguments(START_1, false),
			arguments(SUPER, false));
	}

	@ParameterizedTest
	@MethodSource
	public void checkPerson(final SessionValue value, final boolean success)
	{
		dao.current(value);
		if (success)
			Assertions.assertNotNull(dao.checkPerson());
		else
			Assertions.assertThrows(NotAuthorizedException.class, () -> dao.checkPerson());
	}

	public static Stream<Arguments> checkSuper()
	{
		return Stream.of(
			arguments(ADMIN, false),
			arguments(PERSON, false),
			arguments(PERSON_1, false),
			arguments(START, false),
			arguments(START_1, false),
			arguments(SUPER, true));
	}

	@ParameterizedTest
	@MethodSource
	public void checkSuper(final SessionValue value, final boolean success)
	{
		dao.current(value);
		if (success)
			Assertions.assertNotNull(dao.checkAdmin());
		else
			Assertions.assertThrows(NotAuthorizedException.class, () -> dao.checkSuper());
	}

	@Test
	public void clear()
	{
		Assertions.assertNotNull(dao.current());

		dao.clear();
		Assertions.assertNull(dao.current());
	}

	@Test
	public void current()
	{
		Assertions.assertNull(dao.current());
	}

	@Test
	public void current_invalidId()
	{
		assertThat(Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.current("INVALID")))
			.hasMessage("The ID 'INVALID' is invalid.");
	}

	@Test
	public void current_start1()
	{
		Assertions.assertNull(dao.current());
		Assertions.assertEquals(START_1.id, dao.current(START_1.id).id);
	}

	@Test
	public void current_start1_check()
	{
		Assertions.assertEquals(START_1.id, dao.current().id);
	}

	@Test
	public void current_start1_clear()
	{
		dao.clear();
	}

	@Test
	public void get_invalid()
	{
		assertThat(Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get("invalid")))
			.hasMessage("The ID 'invalid' is invalid.");
	}

	@Test
	public void get_admin()
	{
		var v = dao.get(ADMIN.id);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNotNull(v.admin, "Check admin");
		Assertions.assertEquals("tim", v.admin.id, "Check admin.id");
		Assertions.assertTrue(v.admin(), "Check admin()");
		Assertions.assertFalse(v.supers(), "Check supers()");
		Assertions.assertNull(v.registration, "Check registration");
		Assertions.assertNull(v.person, "Check person");
		Assertions.assertFalse(v.rememberMe, "Check rememberMe");
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
		Assertions.assertEquals("+18885550002", v.registration.phone, "Check registration.phone");
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
		Assertions.assertEquals("+18885550005", v.registration.phone, "Check registration.phone");
		Assertions.assertTrue(v.registration.beenTested, "Check registration.beenTested");
		Assertions.assertTrue(v.registration.haveSymptoms, "Check registration.haveSymptoms");
		Assertions.assertNull(v.person, "Check person");
		Assertions.assertEquals(30 * 60, v.seconds(), "Check seconds");
		assertThat(v.expiresAt).as("Check expiresAt").isAfter(START_1.expiresAt);
		assertThat(v.lastAccessedAt).as("Check lastAccessedAt").isAfter(START_1.lastAccessedAt);
		assertThat(v.createdAt).as("Check createdAt").isInSameSecondAs(START_1.createdAt).isBefore(v.lastAccessedAt);
	}

	@Test
	public void get_super()
	{
		var v = dao.get(SUPER.id);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNotNull(v.admin, "Check admin");
		Assertions.assertEquals("naomi", v.admin.id, "Check admin.id");
		Assertions.assertTrue(v.admin(), "Check admin()");
		Assertions.assertTrue(v.supers(), "Check supers()");
		Assertions.assertNull(v.registration, "Check registration");
		Assertions.assertNull(v.person, "Check person");
		Assertions.assertTrue(v.rememberMe, "Check rememberMe");
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
		dao.current(START.id);

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
		Assertions.assertEquals(30L * 24L * 60L * 60L, redis.ttl(SessionDAO.key(v.id)), "Check expiration");
	}

	@Test
	public void remove_admin()
	{
		dao.remove(ADMIN.id);

		Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get(ADMIN.id));
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
		Assertions.assertNotNull(dao.get(START_1.id));

		dao.remove(START_1.id);

		Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get(START_1.id));
	}

	@Test
	public void remove_super()
	{
		dao.remove(SUPER.id);

		Assertions.assertThrows(NotAuthenticatedException.class, () -> dao.get(SUPER.id));
	}
}
