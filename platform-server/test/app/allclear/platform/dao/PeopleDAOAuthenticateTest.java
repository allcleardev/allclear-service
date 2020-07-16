package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.junit.hibernate.*;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.model.PeopleAuthRequest;
import app.allclear.platform.rest.PeopleResource;
import app.allclear.platform.value.*;

/** Functional test class that verifies the PeopleDAO field access.
 * 
 * @author smalleyd
 * @version 1.1.40
 * @since 5/7/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleDAOAuthenticateTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static PeopleDAO dao;
	private static FacilityDAO facilityDao;
	private static SessionDAO sessionDao;
	private static FakeRedisClient redis = new FakeRedisClient();
	private static SessionValue ADMIN;
	private static final FacilityValue FACILITY = new FacilityValue(0);
	private static final FacilityValue FACILITY_1 = new FacilityValue(1);
	private static final PeopleValue FIRST = new PeopleValue("first", "888-555-1000", true).withEmail("first@test.com");
	private static final PeopleValue SECOND = new PeopleValue("second", "888-555-1001", false).withEmail("second@test.com");

	private static final String TARGET = "/peoples";
	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new PeopleResource(dao, null, sessionDao, null))
		.build();

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();

		dao = new PeopleDAO(factory);
		facilityDao = new FacilityDAO(factory, new TestAuditor());
		sessionDao = new SessionDAO(redis, ConfigTest.loadTest());
	}

	@BeforeEach
	public void beforeEach()
	{
		sessionDao.clear();
	}

	@Test
	public void add()
	{
		dao.add(FIRST);
		dao.add(SECOND);

		facilityDao.add(FACILITY.withPeople(List.of(SECOND.created())), true);
		facilityDao.add(FACILITY_1.withPeople(List.of(FIRST.created(), SECOND.created())), true);

		ADMIN = sessionDao.add(new SessionValue(false, new AdminValue("admin")));
	}

	public static Stream<Arguments> authenticate()
	{
		return Stream.of(
			arguments(FIRST.id, "Password_1", null, null, "AUTH-101", "Invalid credentials"),
			arguments(FIRST.id, "Password_1", "Password_2", null, "AUTH-101", "Invalid credentials"),
			arguments(SECOND.id, "Password_1", null, null, "AUTH-102", "Invalid credentials"));
	}

	@ParameterizedTest
	@CsvSource({"first,Password_1,,,AUTH-101,Invalid credentials",
	            "888-555-1000,Password_1,,,AUTH-101,Invalid credentials",
	            "first@test.com,Password_1,,,AUTH-101,Invalid credentials",
	            "first,Password_1,Password_2,,AUTH-101,Invalid credentials",
	            "888-555-1000,Password_1,Password_2,,AUTH-101,Invalid credentials",
	            "first@test.com,Password_1,Password_2,,AUTH-101,Invalid credentials",
	            "second,Password_1,,,AUTH-102,Invalid credentials",
	            "888-555-1001,Password_1,,,AUTH-102,Invalid credentials",
	            "second@test.com,Password_1,,,AUTH-102,Invalid credentials",
	            "third,Password_1,,,AUTH-102,Invalid credentials",
	            "888-555-1002,Password_1,,,AUTH-102,Invalid credentials",
	            "third@test.com,Password_1,,,AUTH-102,Invalid credentials"})
	@MethodSource
	public void authenticate(final String name, final String password, final String newPassword, final String confirmPassword, final String code, final String message)
	{
		var ex = Assertions.assertThrows(ValidationException.class, () -> dao.authenticate(new PeopleAuthRequest(name, password, false, newPassword, confirmPassword)));

		Assertions.assertEquals(code, ex.errors.get(0).code);
		assertThat(ex).as("Check message").hasMessage(message);
	}

	@ParameterizedTest
	@ValueSource(strings={"first", "second"})
	public void check(final String name)
	{
		var o = dao.find(name);
		Assertions.assertNull(o.getPassword(), "Check password");
		Assertions.assertNull(o.getAuthAt(), "Check authAt");	// Not authenticated yet.
	}

	@Test
	public void credential()
	{
		Assertions.assertNull(dao.update(FIRST.withPassword("Password_1"), true).password);	// Add password to first user.
		Assertions.assertNotNull(dao.update(SECOND.withPassword("Password_1"), false).password);	// Password NOT added to second user so not removed from the response.
	}

	@ParameterizedTest
	@ValueSource(strings={"first", "second"})
	public void credential_check(final String name)
	{
		var o = dao.find(name);
		if ("second".equals(name))
			Assertions.assertNull(o.getPassword(), "Check password");
		else
			Assertions.assertNotNull(o.getPassword(), "Check password");
		Assertions.assertNull(o.getAuthAt(), "Check authAt");	// Not authenticated yet.
	}

	public static Stream<Arguments> credential_error()
	{
		return Stream.of(
			arguments(FIRST.id, "Password_1", null, null, "AUTH-115", "Please change your password."),
			arguments(FIRST.id, "Password_1", "Password_2", null, "VC-101", "Password Confirmation is not set."),
			arguments(FIRST.id, "Password_1", StringUtils.repeat('a', PeopleValue.MIN_LEN_PASSWORD - 1), "Password_3", "VC-103", "New Password cannot be shorter than 8 characters."),
			arguments(FIRST.id, "Password_1", StringUtils.repeat('a', PeopleValue.MAX_LEN_PASSWORD + 1), "Password_3", "VC-102", "New Password cannot be longer than 40 characters."),
			arguments(FIRST.id, "Password_1", "Password_2", "Password_3", "AUTH-104", "The 'Password Confirmation' does not match the 'New Password'."),
			arguments(SECOND.id, "Password_1", null, null, "AUTH-102", "Invalid credentials"));
	}

	@ParameterizedTest
	@CsvSource({"first,Password_1,,,AUTH-115,Please change your password.",
	            "888-555-1000,Password_1,,,AUTH-115,Please change your password.",
	            "first@test.com,Password_1,,,AUTH-115,Please change your password.",
	            "first,Password_1,Password_2,,VC-101,Password Confirmation is not set.",
	            "888-555-1000,Password_1,Password_2,,VC-101,Password Confirmation is not set.",
	            "first@test.com,Password_1,Password_2,,VC-101,Password Confirmation is not set.",
	            "first,Password_1,Password_2,Password_3,AUTH-104,The 'Password Confirmation' does not match the 'New Password'.",
	            "888-555-1000,Password_1,Password_2,Password_3,AUTH-104,The 'Password Confirmation' does not match the 'New Password'.",
	            "first@test.com,Password_1,Password_2,Password_3,AUTH-104,The 'Password Confirmation' does not match the 'New Password'.",
	            "second,Password_1,,,AUTH-102,Invalid credentials",
	            "888-555-1001,Password_1,,,AUTH-102,Invalid credentials",
	            "second@test.com,Password_1,,,AUTH-102,Invalid credentials",
	            "third,Password_1,,,AUTH-102,Invalid credentials",
	            "888-555-1002,Password_1,,,AUTH-102,Invalid credentials",
	            "third@test.com,Password_1,,,AUTH-102,Invalid credentials"})
	@MethodSource
	public void credential_error(final String name, final String password, final String newPassword, final String confirmPassword, final String code, final String message)
	{
		authenticate(name, password, newPassword, confirmPassword, code, message);
	}

	@ParameterizedTest
	@ValueSource(strings={"first", "second"})
	public void credential_error_check(final String name)
	{
		credential_check(name);
	}

	@ParameterizedTest
	@CsvSource({"first@test.com,Password_1,Password_2,Password_2"})
	public void credential_success(final String name, final String password, final String newPassword, final String confirmPassword)
	{
		var o = dao.authenticate(new PeopleAuthRequest(name, password, false, newPassword, confirmPassword));
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(FIRST.id, o.id, "Check ID");
		assertThat(o.associations).as("Check associations").containsExactly(FACILITY_1.created());
	}

	@ParameterizedTest
	@ValueSource(strings={"first", "second"})
	public void credential_success_check(final String name)
	{
		var o = dao.find(name);
		if ("second".equals(name))
		{
			Assertions.assertNull(o.getPassword(), "Check password");
			Assertions.assertNull(o.getAuthAt(), "Check authAt");	// Not authenticated yet.
		}
		else
		{
			Assertions.assertNotNull(FIRST.password = o.getPassword(), "Check password");
			assertThat(FIRST.authAt = o.getAuthAt()).as("Check authAt").isCloseTo(new Date(), 500L).isEqualTo(o.getUpdatedAt());
		}
	}

	@ParameterizedTest
	@CsvSource({"first,Password_1,,,422,AUTH-101,password,Invalid credentials",
	            "first,Password_2,,,200,,,",
	            "888-555-1000,Password_2,Password_3,Password_3,200,,,"})
	public void request_auth(final String name, final String password, final String newPassword, final String confirmPassword, final int status, final String code, final String fieldName, final String message)
	{
		var response = request("auth").method(HttpMethod.PATCH, Entity.json(new PeopleAuthRequest(name, password, false, newPassword, confirmPassword)));
		Assertions.assertEquals(status, response.getStatus(), "Status");

		if (422 == status)
		{
			var ex = response.readEntity(ErrorInfo.class);
			Assertions.assertNotNull(ex, "Exists");
			Assertions.assertEquals(message, ex.message, "Check message");
			assertThat(ex.fields).as("Check fields").isNotEmpty().hasSize(1).contains(new FieldError(code, fieldName, message));
		}
		else
		{
			Assertions.assertNull(code, "Check code");
			Assertions.assertNull(fieldName, "Check fieldName");
			Assertions.assertNull(message, "Check message");

			var o = response.readEntity(SessionValue.class).person;
			assertThat(o.associations).as("Check associations").containsExactly(FACILITY_1.created());
		}
	}

	@Test
	public void request_auth_check()
	{
		var o = dao.find(FIRST.name);
		assertThat(o.getPassword()).as("Check password").isNotNull().isNotEqualTo(FIRST.password);
		assertThat(o.getAuthAt()).as("Check authAt").isCloseTo(new Date(), 500L).isEqualTo(o.getUpdatedAt()).isAfter(FIRST.authAt);

		FIRST.password = o.getPassword();
		FIRST.authAt = o.getAuthAt();
	}

	@ParameterizedTest
	@CsvSource({"888-555-1000,Password_3,Password_4,Password_4,401"})
	public void request_password_error(final String name, final String password, final String newPassword, final String confirmPassword, final int status)
	{
		sessionDao.current(ADMIN);

		var response = request("password").method(HttpMethod.PATCH, Entity.json(new PeopleAuthRequest(name, password, false, newPassword, confirmPassword)));
		Assertions.assertEquals(status, response.getStatus(), "Status");
	}

	@Test
	public void request_password_error_check()
	{
		var o = dao.find(FIRST.name);
		assertThat(o.getPassword()).as("Check password").isEqualTo(FIRST.password);
		assertThat(o.getAuthAt()).as("Check authAt").isEqualTo(FIRST.authAt);
	}

	@ParameterizedTest
	@CsvSource({"888-555-1000,Password_3,Password_4,Password_4,401"})
	public void request_password_error_unauth(final String name, final String password, final String newPassword, final String confirmPassword, final int status)
	{
		var response = request("password").method(HttpMethod.PATCH, Entity.json(new PeopleAuthRequest(name, password, false, newPassword, confirmPassword)));
		Assertions.assertEquals(status, response.getStatus(), "Status");
	}

	@Test
	public void request_password_error_unauth_check()
	{
		request_password_error_check();
	}

	@ParameterizedTest
	@CsvSource({"888-555-1000,Password_3,Password_4,Password_4,200"})
	public void request_password_success(final String name, final String password, final String newPassword, final String confirmPassword, final int status)
	{
		sessionDao.current(FIRST);

		var response = request("password").method(HttpMethod.PATCH, Entity.json(new PeopleAuthRequest(name, password, false, newPassword, confirmPassword)));
		Assertions.assertEquals(status, response.getStatus(), "Status");
	}

	@Test
	public void request_password_success_check()
	{
		request_auth_check();
	}

	@Test
	public void second()
	{
		dao.update(SECOND.withPassword("Password_2").withActive(true), true);
	}

	@ParameterizedTest
	@CsvSource({"888-555-1001,Password_1,,,422,AUTH-101,password,Invalid credentials",
	            "second,Password_2,,,422,AUTH-115,newPassword,Please change your password.",
	            "888-555-1001,Password_2,Password_3,Password_3,200,,,"})
	public void second_auth(final String name, final String password, final String newPassword, final String confirmPassword, final int status, final String code, final String fieldName, final String message)
	{
		var response = request("auth").method(HttpMethod.PATCH, Entity.json(new PeopleAuthRequest(name, password, false, newPassword, confirmPassword)));
		Assertions.assertEquals(status, response.getStatus(), "Status");

		if (422 == status)
		{
			var ex = response.readEntity(ErrorInfo.class);
			Assertions.assertNotNull(ex, "Exists");
			Assertions.assertEquals(message, ex.message, "Check message");
			assertThat(ex.fields).as("Check fields").isNotEmpty().hasSize(1).contains(new FieldError(code, fieldName, message));
		}
		else
		{
			Assertions.assertNull(code, "Check code");
			Assertions.assertNull(fieldName, "Check fieldName");
			Assertions.assertNull(message, "Check message");

			var o = response.readEntity(SessionValue.class).person;
			assertThat(o.associations).as("Check associations").containsExactly(FACILITY.created(), FACILITY_1.created());
		}
	}

	private WebTarget target() { return RULE.client().target(TARGET); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
