package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;
import static app.allclear.platform.type.TestType.*;

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

import app.allclear.junit.hibernate.*;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.NotFoundExceptionMapper;
import app.allclear.common.errors.ValidationExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.FacilityDAO;
import app.allclear.platform.dao.FacilityDAOTest;
import app.allclear.platform.dao.PeopleDAO;
import app.allclear.platform.dao.PeopleDAOTest;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.dao.TestsDAO;
import app.allclear.platform.filter.TestsFilter;
import app.allclear.platform.value.AdminValue;
import app.allclear.platform.value.FacilityValue;
import app.allclear.platform.value.PeopleValue;
import app.allclear.platform.value.SessionValue;
import app.allclear.platform.value.TestsValue;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Tests entity.
*
*	@author smalleyd
*	@version 1.0.44
*	@since April 4, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class TestsResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static TestsDAO dao = null;
	private static FacilityDAO facilityDao = null;
	private static PeopleDAO peopleDao = null;
	private static SessionDAO sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());
	private static TestsValue VALUE = null;
	private static SessionValue ADMIN = null;
	private static FacilityValue FACILITY = null;
	private static PeopleValue PERSON = null;
	private static Date TAKEN_ON = utc(2020, 4, 4);

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new TestsResource(dao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/tests";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<TestsValue>> TYPE_LIST_VALUE = new GenericType<List<TestsValue>>() {};
	private static final GenericType<QueryResults<TestsValue, TestsFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<TestsValue, TestsFilter>>() {};

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new TestsDAO(factory, sessionDao);
		facilityDao = new FacilityDAO(factory);
		peopleDao = new PeopleDAO(factory);
	}

	@BeforeEach
	public void beforeEach()
	{
		if (null != ADMIN) sessionDao.current(ADMIN);
	}

	@Test
	public void add()
	{
		sessionDao.current(ADMIN = sessionDao.add(new AdminValue("admin"), false));
		FACILITY = facilityDao.add(FacilityDAOTest.createValid(), true);
		PERSON = peopleDao.add(PeopleDAOTest.createValid());

		var now = new Date();
		var response = request()
			.post(Entity.entity(VALUE = new TestsValue(PERSON.id, RT_PCR.id, TAKEN_ON, FACILITY.id, true, "All the other details"), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withId(1L).withPersonName(PERSON.name).withType(RT_PCR).withFacilityName(FACILITY.name).withCreatedAt(now).withUpdatedAt(now), value);
	}

	@Test
	public void find()
	{
		var response = request(target().queryParam("personId", PERSON.id)).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		assertThat(response.readEntity(TYPE_LIST_VALUE)).containsExactly(VALUE);
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(value.createdAt, value.updatedAt, "Check updatedAt");
		check(VALUE, value);
	}

	/** Helper method - calls the GET endpoint. */
	private Response get(final Long id)
	{
		return request(id.toString()).get();
	}

	@Test
	public void getWithException()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE.id + 1000L).getStatus(), "Status");
	}

	@Test
	public void modify()
	{
		count(new TestsFilter().withPositive(true), 1L);
		count(new TestsFilter().withPositive(false), 0L);

		var now = new Date();
		var response = request().put(Entity.entity(VALUE.withPositive(false), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withUpdatedAt(now), value);
	}

	@Test
	public void modify_count()
	{
		count(new TestsFilter().withPositive(true), 0L);
		count(new TestsFilter().withPositive(false), 1L);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertFalse(value.positive, "Check positive");
		assertThat(value.updatedAt).as("Check updatedAt").isAfter(value.createdAt);
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();
		var hourAgoTaken = hours(VALUE.takenOn, -1);
		var hourAheadTaken = hours(VALUE.takenOn, 1);

		return Stream.of(
			arguments(new TestsFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new TestsFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(new TestsFilter(1, 20).withTypeId(VALUE.typeId), 1L),
			arguments(new TestsFilter(1, 20).withTakenOn(VALUE.takenOn), 1L),
			arguments(new TestsFilter(1, 20).withTakenOnFrom(hourAgoTaken), 1L),
			arguments(new TestsFilter(1, 20).withTakenOnTo(hourAheadTaken), 1L),
			arguments(new TestsFilter(1, 20).withTakenOnFrom(hourAgoTaken).withTakenOnTo(hourAheadTaken), 1L),
			arguments(new TestsFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(new TestsFilter(1, 20).withPositive(VALUE.positive), 1L),
			arguments(new TestsFilter(1, 20).withNotes(VALUE.notes), 1L),
			arguments(new TestsFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new TestsFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new TestsFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new TestsFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new TestsFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new TestsFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new TestsFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(new TestsFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(new TestsFilter(1, 20).withTypeId("invalid"), 0L),
			arguments(new TestsFilter(1, 20).withTakenOn(utc(2020, 2, 4)), 0L),
			arguments(new TestsFilter(1, 20).withTakenOnFrom(hourAheadTaken), 0L),
			arguments(new TestsFilter(1, 20).withTakenOnTo(hourAgoTaken), 0L),
			arguments(new TestsFilter(1, 20).withTakenOnFrom(hourAheadTaken).withTakenOnTo(hourAgoTaken), 0L),
			arguments(new TestsFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(new TestsFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(new TestsFilter(1, 20).withNotes("invalid"), 0L),
			arguments(new TestsFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new TestsFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new TestsFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new TestsFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new TestsFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new TestsFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final TestsFilter filter, final long expectedTotal)
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
		remove(VALUE.id + 1000L, false);
		remove(VALUE.id, true);
		remove(VALUE.id, false);
	}

	/** Helper method - call the DELETE endpoint. */
	private void remove(final Long id, boolean success)
	{
		var response = request(id.toString()).delete();
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

	@Test
	public void testRemove_search()
	{
		count(new TestsFilter().withId(VALUE.id), 0L);
		count(new TestsFilter().withPositive(false), 0L);
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
	private void count(final TestsFilter filter, long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final TestsValue expected, final TestsValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.personId, value.personId, assertId + "Check personId");
		Assertions.assertEquals(expected.personName, value.personName, assertId + "Check personName");
		Assertions.assertEquals(expected.typeId, value.typeId, assertId + "Check typeId");
		if (null == expected.takenOn)
			Assertions.assertNull(value.takenOn, assertId + "Check takenOn");
		else
			assertThat(value.takenOn).as(assertId + "Check takenOn").isCloseTo(expected.takenOn, 500L);
		Assertions.assertEquals(expected.facilityId, value.facilityId, assertId + "Check facilityId");
		Assertions.assertEquals(expected.facilityName, value.facilityName, assertId + "Check facilityName");
		Assertions.assertEquals(expected.positive, value.positive, assertId + "Check positive");
		Assertions.assertEquals(expected.notes, value.notes, assertId + "Check notes");
		if (null == expected.createdAt)
			Assertions.assertNull(value.createdAt, assertId + "Check createdAt");
		else
			assertThat(value.createdAt).as(assertId + "Check createdAt").isCloseTo(expected.createdAt, 500L);
		if (null == expected.updatedAt)
			Assertions.assertNull(value.updatedAt, assertId + "Check updatedAt");
		else
			assertThat(value.updatedAt).as(assertId + "Check updatedAt").isCloseTo(expected.updatedAt, 500L);
	}
}
