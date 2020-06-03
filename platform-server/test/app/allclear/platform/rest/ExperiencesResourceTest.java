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

import app.allclear.junit.hibernate.*;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.NotFoundExceptionMapper;
import app.allclear.common.errors.ValidationExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.ExperiencesFilter;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the RESTful resource that handles access to the Experiences entity.
*
*	@author smalleyd
*	@version 1.1.80
*	@since June 2, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class ExperiencesResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static ExperiencesDAO dao = null;
	private static FacilityDAO facilityDao = null;
	private static PeopleDAO peopleDao = null;
	private static SessionDAO sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());
	private static ExperiencesValue VALUE = null;
	private static FacilityValue FACILITY = null;
	private static FacilityValue FACILITY_1 = null;
	private static PeopleValue PERSON = null;
	private static PeopleValue PERSON_1 = null;
	private static final SessionValue ADMIN = new SessionValue(false, new AdminValue("admin"));
	private static SessionValue SESSION = null;
	private static SessionValue SESSION_1 = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new ExperiencesResource(dao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/experiences";

	/** Generic types for reading values from responses. */
	private static final GenericType<QueryResults<ExperiencesValue, ExperiencesFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<ExperiencesValue, ExperiencesFilter>>() {};

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new ExperiencesDAO(factory, sessionDao);
		facilityDao = new FacilityDAO(factory, new TestAuditor());
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
		FACILITY = facilityDao.add(new FacilityValue(0), true);
		FACILITY_1 = facilityDao.add(new FacilityValue(1), true);
		SESSION = new SessionValue(false, PERSON = peopleDao.add(new PeopleValue("zero", "888-555-1000", true)));
		SESSION_1 = new SessionValue(false, PERSON_1 = peopleDao.add(new PeopleValue("one", "888-555-1001", true)));

		sessionDao.current(SESSION_1);
		var now = new Date();
		var response = request()
			.post(Entity.entity(VALUE = new ExperiencesValue(FACILITY.id, false), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(ExperiencesValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withId(1L).withPersonId(PERSON_1.id).withPersonName(PERSON_1.name).withFacilityName(FACILITY.name).withCreatedAt(now), value);
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(ExperiencesValue.class);
		Assertions.assertNotNull(value, "Exists");
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
		count(new ExperiencesFilter().withPersonId(PERSON_1.id), 1L);
		count(new ExperiencesFilter().withFacilityId(FACILITY.id), 1L);
		count(new ExperiencesFilter().withPositive(false), 1L);
		count(new ExperiencesFilter().withPersonId(PERSON.id), 0L);
		count(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L);
		count(new ExperiencesFilter().withPositive(true), 0L);

		var response = request().put(Entity.entity(VALUE.withPersonId(PERSON.id).withFacilityId(FACILITY_1.id).withPositive(true), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(ExperiencesValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withPersonName(PERSON.name).withFacilityName(FACILITY_1.name), value);
	}

	@Test
	public void modify_count()
	{
		count(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L);
		count(new ExperiencesFilter().withFacilityId(FACILITY.id), 0L);
		count(new ExperiencesFilter().withPositive(false), 0L);
		count(new ExperiencesFilter().withPersonId(PERSON.id), 1L);
		count(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 1L);
		count(new ExperiencesFilter().withPositive(true), 1L);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(ExperiencesValue.class);
System.out.println("VALUE: " + value);
System.out.println("VALUE_1: " + dao.getById(VALUE.id));
System.out.println("PERSON: " + peopleDao.getById(PERSON.id));
System.out.println("PERSON_1: " + peopleDao.getById(PERSON_1.id));
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(PERSON.id, value.personId, "Check personId");
		Assertions.assertEquals(PERSON.name, value.personName, "Check personName");
		Assertions.assertEquals(FACILITY.id, value.facilityId, "Check facilityId");
		Assertions.assertEquals(FACILITY.name, value.facilityName, "Check facilityName");
		Assertions.assertTrue(value.positive, "Check positive");
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new ExperiencesFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new ExperiencesFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(new ExperiencesFilter(1, 20).withPositive(VALUE.positive), 1L),
			arguments(new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new ExperiencesFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(new ExperiencesFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final ExperiencesFilter filter, final long expectedTotal)
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
		count(new ExperiencesFilter().withId(VALUE.id), 0L);
		count(new ExperiencesFilter().withPersonId(PERSON.id), 0L);
		count(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L);
		count(new ExperiencesFilter().withPositive(true), 0L);
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
	private void count(final ExperiencesFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final ExperiencesValue expected, final ExperiencesValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.personId, value.personId, assertId + "Check personId");
		Assertions.assertEquals(expected.personName, value.personName, assertId + "Check personName");
		Assertions.assertEquals(expected.facilityId, value.facilityId, assertId + "Check facilityId");
		Assertions.assertEquals(expected.facilityName, value.facilityName, assertId + "Check facilityName");
		Assertions.assertEquals(expected.positive, value.positive, assertId + "Check positive");
		if (null == expected.createdAt)
			Assertions.assertNull(value.createdAt, assertId + "Check createdAt");
		else
			assertThat(value.createdAt).as(assertId + "Check createdAt").isCloseTo(expected.createdAt, 500L);
	}
}
