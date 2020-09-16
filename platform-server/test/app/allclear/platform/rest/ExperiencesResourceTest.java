package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.platform.type.Experience.GOOD_HYGIENE;
import static app.allclear.platform.type.Experience.OVERLY_CROWDED;
import static app.allclear.platform.type.Experience.SOCIAL_DISTANCING_ENFORCED;
import static app.allclear.testing.TestingUtils.*;
import static java.util.stream.Collectors.toSet;

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
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.ExperiencesFilter;
import app.allclear.platform.model.ExperiencesCalcResponse;
import app.allclear.platform.type.Experience;
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
		check(VALUE.withId(1L).withPersonId(PERSON_1.id).withPersonName(PERSON_1.name).withFacilityName(FACILITY.name).withCreatedAt(now).withUpdatedAt(now), value);
	}

	@Test
	public void calc()
	{
		var response = request(target().path("calc").queryParam("facilityId", FACILITY.id)).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(ExperiencesCalcResponse.class);
		Assertions.assertNotNull(value, "Exists");
		check(value, 0L, 1L);
	}

	@Test
	public void calc_none()
	{
		var response = request(target().path("calc").queryParam("facilityId", FACILITY_1.id)).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(ExperiencesCalcResponse.class);
		Assertions.assertNotNull(value, "Exists");
		check(value, 0L, 0L);
	}

	@Test
	public void calc_null()
	{
		var response = request(target().path("calc")).get();
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");

		var ex = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(ex, "Exists");
		Assertions.assertEquals("Must provide a Facility identifier.", ex.message, "Check message");
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(ExperiencesValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertNull(value.tags, "Check tags");
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

	public static Stream<Arguments> modif()
	{
		return Stream.of(
			arguments(new ExperiencesFilter().withPersonId(PERSON_1.id), 1L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY.id), 1L),
			arguments(new ExperiencesFilter().withPositive(false), 1L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE), 1L),
			arguments(new ExperiencesFilter().withExcludeTags(OVERLY_CROWDED), 1L),
			arguments(new ExperiencesFilter().withExcludeTags(SOCIAL_DISTANCING_ENFORCED), 1L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L),
			arguments(new ExperiencesFilter().withPersonId(PERSON.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L),
			arguments(new ExperiencesFilter().withPositive(true), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(OVERLY_CROWDED), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void modif(final ExperiencesFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void modify()
	{
		var response = request().put(Entity.entity(VALUE.withPersonId(PERSON.id).withFacilityId(FACILITY_1.id).withPositive(true).withTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED),
			UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(ExperiencesValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withPersonName(PERSON.name).withFacilityName(FACILITY_1.name).withUpdatedAt(new Date()), value);
	}

	public static Stream<Arguments> modify_count()
	{
		return Stream.of(
			arguments(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY.id), 0L),
			arguments(new ExperiencesFilter().withPositive(false), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(OVERLY_CROWDED), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(new ExperiencesFilter().withPersonId(PERSON.id), 1L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 1L),
			arguments(new ExperiencesFilter().withPositive(true), 1L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE), 1L),
			arguments(new ExperiencesFilter().withIncludeTags(OVERLY_CROWDED), 1L),
			arguments(new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED), 1L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_count(final ExperiencesFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(ExperiencesValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(PERSON.id, value.personId, "Check personId");
		Assertions.assertEquals(PERSON.name, value.personName, "Check personName");
		Assertions.assertEquals(FACILITY_1.id, value.facilityId, "Check facilityId");
		Assertions.assertEquals(FACILITY_1.name, value.facilityName, "Check facilityName");
		Assertions.assertTrue(value.positive, "Check positive");
		assertThat(value.tags).as("Check tags").containsExactly(GOOD_HYGIENE.named(), OVERLY_CROWDED.named(), SOCIAL_DISTANCING_ENFORCED.named());
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(ADMIN, new ExperiencesFilter(1, 20).withId(VALUE.id), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withPositive(VALUE.positive), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withId(VALUE.id), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPersonId(VALUE.personId), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPositive(VALUE.positive), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withId(VALUE.id), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPositive(VALUE.positive), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L),

			// Negative tests
			arguments(ADMIN, new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPersonId("invalid"), 1L),	// Overridden based on the current user.
			arguments(SESSION, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final SessionValue s, final ExperiencesFilter filter, final long expectedTotal)
	{
		sessionDao.current(s);

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

	public static Stream<Arguments> testRemove_search()
	{
		return Stream.of(
			arguments(new ExperiencesFilter().withId(VALUE.id), 0L),
			arguments(new ExperiencesFilter().withPersonId(PERSON.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L),
			arguments(new ExperiencesFilter().withPositive(true), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void testRemove_search(final ExperiencesFilter filter, final long expected)
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
		if (null == expected.updatedAt)
			Assertions.assertNull(value.updatedAt, assertId + "Check updatedAt");
		else
			assertThat(value.updatedAt).as(assertId + "Check updatedAt").isCloseTo(expected.updatedAt, 500L);
		Assertions.assertEquals(expected.tags, value.tags, assertId + "Check tags");
	}

	private void check(final ExperiencesCalcResponse response, final long positives, final long negatives, final Experience... tags)
	{
		Assertions.assertEquals(positives, response.positives, "Check positives");
		Assertions.assertEquals(negatives, response.negatives, "Check negatives");
		Assertions.assertEquals(positives + negatives, response.total, "Check total");

		var tagIds = Arrays.stream(tags).map(v -> v.id).collect(toSet());
		Experience.LIST.forEach(v -> {
			Assertions.assertEquals(tagIds.contains(v.id) ? 1L : 0L, response.tags.get(v.id).count, "Check tags." + v.name);
		});
	}
}
