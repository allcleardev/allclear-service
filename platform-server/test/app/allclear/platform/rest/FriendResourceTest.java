package app.allclear.platform.rest;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.junit.hibernate.*;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.FriendFilter;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the RESTful resource that handles access to the Friend entity.
*
*	@author smalleyd
*	@version 1.1.9
*	@since April 27, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class FriendResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static final FakeRedisClient redis = new FakeRedisClient();
	private static FriendDAO dao = null;
	private static PeopleDAO peopleDao = null;
	private static final SessionDAO sessionDao = new SessionDAO(redis, ConfigTest.loadTest());
	private static SessionValue ADMIN = new SessionValue(false, new AdminValue("super"));
	private static FriendValue VALUE = null;
	private static FriendValue VALUE_1 = null;
	private static PeopleValue PERSON = null;
	private static PeopleValue PERSON_1 = null;
	private static SessionValue SESSION = null;
	private static SessionValue SESSION_1 = null;
	private static List<PeopleValue> INVITEES = null;
	private static List<SessionValue> INVITEES_ = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new FriendResource(dao, sessionDao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/friends";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<FriendValue>> TYPE_LIST_VALUE = new GenericType<List<FriendValue>>() {};
	private static final GenericType<QueryResults<FriendValue, FriendFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<FriendValue, FriendFilter>>() {};

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new FriendDAO(factory);
		peopleDao = new PeopleDAO(factory);
	}

	@BeforeEach
	public void beforeEach()
	{
		sessionDao.current(ADMIN);
	}

	@Test
	public void add()
	{
		PERSON = peopleDao.add(new PeopleValue("first", "888-555-1000", true));
		PERSON_1 = peopleDao.add(new PeopleValue("second", "888-555-1001", true));
		SESSION = new SessionValue(false, PERSON);
		SESSION_1 = new SessionValue(false, PERSON_1);
		VALUE_1 = new FriendValue(PERSON.id, PERSON_1.id);	// NOT added. Just used for the ID field.
		INVITEES = IntStream.range(0, 5).mapToObj(i -> peopleDao.add(new PeopleValue("invitee-" + i, "888-555-200" + i, true))).collect(toList());
		INVITEES_ = INVITEES.stream().map(o -> new SessionValue(false, o)).collect(toList());

		var now = new Date();
		var response = request()
			.post(Entity.entity(VALUE = new FriendValue(PERSON_1.id, PERSON.id), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FriendValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withCreatedAt(now).withPersonName(PERSON_1.name).withInviteeName(PERSON.name), value);
	}

	public static Stream<SessionValue> get_person_sessions() { return Stream.of(SESSION, SESSION_1); }

	@ParameterizedTest
	@MethodSource("get_person_sessions")
	public void add_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request().post(Entity.json(VALUE_1)).getStatus(), "Check first");
	}

	@Test @Disabled
	public void find()
	{
		var response = request(target().queryParam("name", "")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var values = response.readEntity(TYPE_LIST_VALUE);
		Assertions.assertNotNull(values, "Exists");
	}

	@Test
	public void get()
	{
		var response = get(VALUE.getId());
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FriendValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@ParameterizedTest
	@MethodSource("get_person_sessions")
	public void get_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, get(VALUE.getId()).getStatus(), "Check first");
	}

	/** Helper method - calls the GET endpoint. */
	private Response get(final String id)
	{
		return request(id).get();
	}

	@Test
	public void getWithException()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE_1.getId()).getStatus(), "Status");
	}

	@Test
	public void modify()
	{
		count(new FriendFilter().withHasAcceptedAt(false), 1L);
		count(new FriendFilter().withHasAcceptedAt(true), 0L);

		var response = request().put(Entity.entity(VALUE.withAcceptedAt(new Date()), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FriendValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void modify_count()
	{
		count(new FriendFilter().withHasAcceptedAt(false), 0L);
		count(new FriendFilter().withHasAcceptedAt(true), 1L);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.getId()).readEntity(FriendValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.acceptedAt).as("Check accpetedAt").isAfter(value.createdAt).isCloseTo(new Date(), 1000L);
		check(VALUE, value);
	}

	@ParameterizedTest
	@MethodSource("get_person_sessions")
	public void modify_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED,
			request().put(Entity.json(new FriendValue(PERSON_1.id, PERSON.id).withRejectedAt(new Date()))).getStatus());
	}

	@Test
	public void modify_revert()
	{
		var response = request().put(Entity.entity(VALUE.withAcceptedAt(null), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
	}

	@ParameterizedTest
	@MethodSource("get_person_sessions")
	public void remove_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(VALUE.getId()).delete().getStatus());
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new FriendFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(new FriendFilter(1, 20).withInviteeId(VALUE.inviteeId), 1L),
			arguments(new FriendFilter(1, 20).withHasAcceptedAt(false), 1L),
			arguments(new FriendFilter(1, 20).withAcceptedAtFrom(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withAcceptedAtTo(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withAcceptedAtFrom(hourAgo).withAcceptedAtTo(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withHasRejectedAt(false), 1L),
			arguments(new FriendFilter(1, 20).withRejectedAtFrom(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withRejectedAtTo(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withRejectedAtFrom(hourAgo).withRejectedAtTo(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new FriendFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new FriendFilter(1, 20).withPersonId(VALUE_1.personId), 0L),
			arguments(new FriendFilter(1, 20).withInviteeId(VALUE_1.inviteeId), 0L),
			arguments(new FriendFilter(1, 20).withHasAcceptedAt(true), 0L),
			arguments(new FriendFilter(1, 20).withAcceptedAtFrom(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withAcceptedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withAcceptedAtFrom(hourAhead).withAcceptedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withHasRejectedAt(true), 0L),
			arguments(new FriendFilter(1, 20).withRejectedAtFrom(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withRejectedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withRejectedAtFrom(hourAhead).withRejectedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final FriendFilter filter, final long expectedTotal)
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

	@ParameterizedTest
	@MethodSource("get_person_sessions")
	public void search_auth_00(final SessionValue s)
	{
		sessionDao.current(s);
		search(new FriendFilter(), 1L);
	}

	@Test
	public void search_auth_01()
	{
		Assertions.assertEquals(HTTP_STATUS_OK, request().put(Entity.json(VALUE.withRejectedAt(new Date()))).getStatus());	// Once rejected - only the invitee can see the request.
	}

	@ParameterizedTest
	@MethodSource("get_person_sessions")
	public void search_auth_02(final SessionValue s)
	{
		sessionDao.current(s);
		search(new FriendFilter(), VALUE.inviteeId.equals(s.person.id) ? 1L : 0L);
	}

	/** Test removal after the search. */
	@Test
	public void testRemove()
	{
		remove(VALUE_1.getId(), false);
		remove(VALUE.getId(), true);
		remove(VALUE.getId(), false);
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
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE.getId()).getStatus(), "Status");
	}

	@Test
	public void testRemove_search()
	{
		count(new FriendFilter(), 0L);
		count(new FriendFilter().withPersonId(VALUE.personId), 0L);
		count(new FriendFilter().withInviteeId(VALUE.inviteeId), 0L);
	}

	@Test
	public void z_00()
	{
		for (var i : INVITEES_)
		{
			sessionDao.current(i);
			assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: " + i.person.name).isNullOrEmpty();
			assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: " + i.person.name).isNullOrEmpty();
		}

		sessionDao.current(SESSION);
		assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: first").isNullOrEmpty();
		assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: first").isNullOrEmpty();

		var response = request(target().path("start").queryParam("inviteeId", INVITEES.get(2).id)).method(HttpMethod.POST);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var value = VALUE = response.readEntity(FriendValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(PERSON.id, value.personId, "Check personId");
		Assertions.assertEquals(INVITEES.get(2).id, value.inviteeId, "Check inviteeId");
		Assertions.assertNull(value.acceptedAt, "Check acceptedAt");
		Assertions.assertNull(value.rejectedAt, "Check rejectedAt");
		assertThat(value.createdAt).as("Check createdAt").isCloseTo(new Date(), 500L);
	}

	@Test
	public void z_01()
	{
		var j = 0;
		var s = Set.of(2);
		for (var i : INVITEES_)
		{
			sessionDao.current(i);
			assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: " + i.person.name).isNullOrEmpty();
			if (s.contains(j++))
			{
				assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: " + i.person.name).containsExactly(VALUE);
			}
			else
			{
				assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: " + i.person.name).isNullOrEmpty();
			}
		}

		sessionDao.current(SESSION);
		assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: first").containsExactly(VALUE);
		assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: first").isNullOrEmpty();

		var response = request(target().path("start").queryParam("inviteeId", INVITEES.get(3).id)).method(HttpMethod.POST);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var value = VALUE_1 = response.readEntity(FriendValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(PERSON.id, value.personId, "Check personId");
		Assertions.assertEquals(INVITEES.get(3).id, value.inviteeId, "Check inviteeId");
		Assertions.assertNull(value.acceptedAt, "Check acceptedAt");
		Assertions.assertNull(value.rejectedAt, "Check rejectedAt");
		assertThat(value.createdAt).as("Check createdAt").isCloseTo(new Date(), 500L);
	}

	@Test
	public void z_02()
	{
		var j = 0;
		var s = Set.of(2, 3);
		for (var i : INVITEES_)
		{
			sessionDao.current(i);
			assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: " + i.person.name).isNullOrEmpty();
			if (s.contains(j++))
			{
				assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: " + i.person.name).containsExactly(3 == j ? VALUE : VALUE_1);	// j is now incremented so use 3 index.
			}
			else
			{
				assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: " + i.person.name).isNullOrEmpty();
			}
		}

		sessionDao.current(SESSION);
		assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: first").containsOnly(VALUE, VALUE_1);
		assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: first").isNullOrEmpty();

		sessionDao.current(INVITEES_.get(3));
		var response = request(target().path("accept").queryParam("personId", PERSON.id)).method(HttpMethod.PUT);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var value = response.readEntity(FriendValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(PERSON.id, value.personId, "Check personId");
		Assertions.assertEquals(INVITEES.get(3).id, value.inviteeId, "Check inviteeId");
		assertThat(value.acceptedAt).as("Check acceptedAt").isAfter(value.createdAt).isCloseTo(new Date(), 500L);
		Assertions.assertNull(value.rejectedAt, "Check rejectedAt");
	}

	@Test
	public void z_03()
	{
		var j = 0;
		var s = Set.of(2);
		for (var i : INVITEES_)
		{
			sessionDao.current(i);
			assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: " + i.person.name).isNullOrEmpty();
			if (s.contains(j++))
			{
				assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: " + i.person.name).containsExactly(VALUE);
			}
			else
			{
				assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: " + i.person.name).isNullOrEmpty();
			}
		}

		sessionDao.current(SESSION);
		assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: first").containsOnly(VALUE);
		assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: first").isNullOrEmpty();

		sessionDao.current(INVITEES_.get(2));
		var response = request(target().path("reject").queryParam("personId", PERSON.id)).method(HttpMethod.PUT);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var value = response.readEntity(FriendValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(PERSON.id, value.personId, "Check personId");
		Assertions.assertEquals(INVITEES.get(2).id, value.inviteeId, "Check inviteeId");
		Assertions.assertNull(value.acceptedAt, "Check acceptedAt");
		assertThat(value.rejectedAt).as("Check rejectedAt").isAfter(value.createdAt).isCloseTo(new Date(), 500L);
	}

	@Test
	public void z_09()
	{
		for (var i : INVITEES_)
		{
			sessionDao.current(i);
			assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: " + i.person.name).isNullOrEmpty();
			assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: " + i.person.name).isNullOrEmpty();
		}

		sessionDao.current(SESSION);
		assertThat(request("starts").get(TYPE_LIST_VALUE)).as("Check get-starts: first").isNullOrEmpty();
		assertThat(request("incoming").get(TYPE_LIST_VALUE)).as("Check get-incoming: first").isNullOrEmpty();
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, "true").target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final FriendFilter filter, long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final FriendValue expected, final FriendValue value)
	{
		var assertId = "ID (" + expected.getId() + "): ";
		Assertions.assertEquals(expected.personId, value.personId, assertId + "Check personId");
		Assertions.assertEquals(expected.personName, value.personName, assertId + "Check personName");
		Assertions.assertEquals(expected.inviteeId, value.inviteeId, assertId + "Check inviteeId");
		Assertions.assertEquals(expected.inviteeName, value.inviteeName, assertId + "Check inviteeName");
		if (null == expected.acceptedAt)
			Assertions.assertNull(value.acceptedAt, assertId + "Check acceptedAt");
		else
			assertThat(value.acceptedAt).as(assertId + "Check acceptedAt").isCloseTo(expected.acceptedAt, 500L);
		if (null == expected.rejectedAt)
			Assertions.assertNull(value.rejectedAt, assertId + "Check rejectedAt");
		else
			assertThat(value.rejectedAt).as(assertId + "Check rejectedAt").isCloseTo(expected.rejectedAt, 500L);
		if (null == expected.createdAt)
			Assertions.assertNull(value.createdAt, assertId + "Check createdAt");
		else
			assertThat(value.createdAt).as(assertId + "Check createdAt").isCloseTo(expected.createdAt, 500L);
	}
}
