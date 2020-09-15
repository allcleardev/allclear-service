package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.*;
import java.util.stream.Stream;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.NotFoundExceptionMapper;
import app.allclear.common.errors.ValidationExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.Config;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.AdminDAO;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.filter.AdminFilter;
import app.allclear.platform.model.*;
import app.allclear.platform.value.AdminValue;
import app.allclear.platform.value.SessionValue;

/**********************************************************************************
*
*	Functional test for the RESTful resource that handles access to the Admin entity.
*
*	@author smalleyd
*	@version 1.1.7
*	@since April 27, 2020
*
**********************************************************************************/

@Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class AdminResourceTest
{
	private static final Config conf = ConfigTest.loadTest();
	private static final FakeRedisClient redis = new FakeRedisClient();
	private static AdminDAO dao = null;
	private static final SessionDAO sessionDao = new SessionDAO(redis, conf);
	private static AdminValue VALUE = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new AdminResource(dao, sessionDao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/admins";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<AdminValue>> TYPE_LIST_VALUE = new GenericType<List<AdminValue>>() {};
	private static final GenericType<QueryResults<AdminValue, AdminFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<AdminValue, AdminFilter>>() {};

	@BeforeAll
	public static void up() throws Exception
	{
		dao = new AdminDAO(conf.admins, "test");
	}

	@Test
	public void add()
	{
		var now = new Date();
		var response = request()
			.post(Entity.entity(VALUE = new AdminValue("~abby~", "Password_1", "abby@me.me", "Abby", "Dorn", "888-555-1000", false, false, false), UTF8MediaType.APPLICATION_JSON_TYPE));	// MUST use ~abby~ since there is only one database instance across all environments. DLS on 4/27/2020.
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(AdminValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withPhone("+18885551000").withCreatedAt(now).withUpdatedAt(now), value.withPassword("Password_1"));

		VALUE.withPassword(null);	// For later checks
	}

	@Test
	public void authenticate()
	{
		Assertions.assertEquals(HTTP_STATUS_OK, request("auth").post(Entity.json(new AuthenticationRequest("~abby~", "Password_1", false))).getStatus());
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, request("auth").post(Entity.json(new AuthenticationRequest("~abby~", "Password_2", false))).getStatus());
	}

	@Test
	public void changePassword()
	{
		sessionDao.current(new SessionValue(false, VALUE));

		Assertions.assertEquals(HTTP_STATUS_OK, request("self").put(Entity.json(new ChangePasswordRequest("Password_1", "Password_2", "Password_2"))).getStatus());
	}

	@Test
	public void changePassword_check()
	{
		sessionDao.clear();

		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, request("auth").post(Entity.json(new AuthenticationRequest("~abby~", "Password_1", false))).getStatus());
		Assertions.assertEquals(HTTP_STATUS_OK, request("auth").post(Entity.json(new AuthenticationRequest("~abby~", "Password_2", false))).getStatus());
	}

	@Test @Disabled	// NOT implemented
	public void find()
	{
		var response = request(target().queryParam("name", "~ab")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var values = response.readEntity(TYPE_LIST_VALUE);
		assertThat(values).as("Check results").isNotNull().containsExactly(VALUE);
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(AdminValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Helper method - calls the GET endpoint. */
	private Response get(final String id)
	{
		return request(id).get();
	}

	@Test
	public void getWithException()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE.id + "INVALID").getStatus(), "Status");
	}

	public static Stream<Arguments> modif()
	{
		return Stream.of(
			arguments(new AdminFilter().withSupers(false), 1L),
			arguments(new AdminFilter().withAlertable(false), 1L),
			arguments(new AdminFilter().withSupers(true), 0L),
			arguments(new AdminFilter().withAlertable(true), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void modif(final AdminFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void modify()
	{
		var response = request().put(Entity.entity(VALUE.withSupers(true).withAlertable(true), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var now = new Date();
		var value = response.readEntity(AdminValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.updatedAt).as("Check updatedAt").isAfter(value.createdAt).isAfter(VALUE.updatedAt).isCloseTo(now, 500L);
		check(VALUE.withUpdatedAt(now), value);
	}

	public static Stream<Arguments> modify_count()
	{
		return Stream.of(
			arguments(new AdminFilter().withSupers(false), 0L),
			arguments(new AdminFilter().withAlertable(false), 0L),
			arguments(new AdminFilter().withSupers(true), 1L),
			arguments(new AdminFilter().withAlertable(true), 1L));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_count(final AdminFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(AdminValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertTrue(value.supers, "Check supers");
		Assertions.assertTrue(value.alertable, "Check alertable");
		assertThat(value.updatedAt).as("Check updatedAt").isAfter(value.createdAt);
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new AdminFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new AdminFilter(1, 20).withEmail(VALUE.email), 1L),
			arguments(new AdminFilter(1, 20).withFirstName(VALUE.firstName), 1L),
			arguments(new AdminFilter(1, 20).withLastName(VALUE.lastName), 1L),
			arguments(new AdminFilter(1, 20).withPhone(VALUE.phone), 1L),
			arguments(new AdminFilter(1, 20).withSupers(VALUE.supers), 1L),
			arguments(new AdminFilter(1, 20).withEditor(VALUE.editor), 1L),
			arguments(new AdminFilter(1, 20).withAlertable(VALUE.alertable), 1L),
			arguments(new AdminFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new AdminFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new AdminFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new AdminFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new AdminFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new AdminFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new AdminFilter(1, 20).withId("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withEmail("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withFirstName("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withLastName("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withPhone("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withSupers(!VALUE.supers), 0L),
			arguments(new AdminFilter(1, 20).withEditor(!VALUE.editor), 0L),
			arguments(new AdminFilter(1, 20).withAlertable(!VALUE.alertable), 0L),
			arguments(new AdminFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new AdminFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new AdminFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new AdminFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new AdminFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new AdminFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final AdminFilter filter, final long expectedTotal)
	{
		var response = request("search")
			.post(Entity.entity(filter, UTF8MediaType.APPLICATION_JSON_TYPE));
		var assertId = "SEARCH " + filter + ": ";
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), assertId + "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assertions.assertNotNull(results, assertId + "Exists");
		Assertions.assertEquals(expectedTotal, results.total, assertId + "Check total");
		if (0L == expectedTotal)
			Assertions.assertNull(results.records, assertId + "Records exist");
		else
		{
			Assertions.assertNotNull(results.records, assertId + "Records exist");
			int total = (int) expectedTotal;
			if (total > results.pageSize)
			{
				if (results.page == results.pages)
					total%= results.pageSize;
				else
					total = results.pageSize;
			}
			Assertions.assertEquals(total, results.records.size(), assertId + "Check records.size");
		}
	}

	/** Test removal after the search. */
	@Test
	public void testRemove()
	{
		remove(VALUE.id + "INVALID", false);
		remove(VALUE.id, true);
		remove(VALUE.id, false);
	}

	/** Helper method - call the DELETE endpoint. */
	private void remove(final String id, boolean success)
	{
		var response = request(id).delete();
		var assertId = "DELETE (" + id + ", " + success + "): ";
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), assertId + "Status");

		var results = response.readEntity(OperationResponse.class);
		Assertions.assertNotNull(results, assertId + "Exists");
		Assertions.assertEquals(success, results.operation, assertId + "Check value");
	}

	@Test
	public void testRemove_get()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE.id).getStatus(), "Status");
	}

	public static Stream<Arguments> testRemove_search()
	{
		return Stream.of(
			arguments(new AdminFilter().withId(VALUE.id), 0L),
			arguments(new AdminFilter().withSupers(true), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void testRemove_search(final AdminFilter filter, final long expected)
	{
		count(filter, expected);
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final AdminFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final AdminValue expected, final AdminValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.password, value.password, assertId + "Check password");
		Assertions.assertEquals(expected.email, value.email, assertId + "Check email");
		Assertions.assertEquals(expected.firstName, value.firstName, assertId + "Check firstName");
		Assertions.assertEquals(expected.lastName, value.lastName, assertId + "Check lastName");
		Assertions.assertEquals(expected.phone, value.phone, assertId + "Check phone");
		Assertions.assertEquals(expected.supers, value.supers, assertId + "Check supers");
		Assertions.assertEquals(expected.editor, value.editor, assertId + "Check editor");
		Assertions.assertEquals(!expected.editor || expected.supers, value.canAdmin(), assertId + "Check canAdmin()");
		Assertions.assertEquals(expected.alertable, value.alertable, assertId + "Check alertable");
		assertThat(value.createdAt).as(assertId + "Check createdAt").isCloseTo(expected.createdAt, 500L);
		assertThat(value.updatedAt).as(assertId + "Check updatedAt").isCloseTo(expected.updatedAt, 500L);
	}
}
