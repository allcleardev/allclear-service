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
import app.allclear.common.errors.ValidationExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.App;
import app.allclear.platform.dao.PeopleDAO;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the People entity.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 23, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static PeopleDAO dao = null;
	private static PeopleValue VALUE = null;
	private static Date AUTH_AT;
	private static Date EMAIL_VERIFIED_AT;
	private static Date PHONE_VERIFIED_AT;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new ValidationExceptionMapper())
		.addResource(new PeopleResource(dao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/peoples";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<PeopleValue>> TYPE_LIST_VALUE = new GenericType<List<PeopleValue>>() {};
	private static final GenericType<QueryResults<PeopleValue, PeopleFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<PeopleValue, PeopleFilter>>() {};

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new PeopleDAO(factory);

		AUTH_AT = timestamp("2020-03-24T12:46:30-0000");
		EMAIL_VERIFIED_AT = timestamp("2020-03-24T12:47:30-0000");
		PHONE_VERIFIED_AT = timestamp("2020-03-24T12:48:30-0000");
	}

	@Test
	public void add()
	{
		var response = request()
			.post(Entity.entity(VALUE = new PeopleValue("minimal", "888-minimal", true)
				.withAuthAt(AUTH_AT).withEmailVerifiedAt(EMAIL_VERIFIED_AT).withPhoneVerifiedAt(PHONE_VERIFIED_AT), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(PeopleValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.id).as("Check ID").hasSize(6);
		Assertions.assertNull(value.status, "Check status");
		Assertions.assertNull(value.stature, "Check stature");
		assertThat(value.createdAt).as("Check createdAt").isCloseTo(new Date(), 500L);
		assertThat(value.updatedAt).as("Check updatedAt").isEqualTo(value.createdAt);
		check(VALUE.withId(value.id).withCreatedAt(value.createdAt).withUpdatedAt(value.updatedAt), value);
	}

	@Test
	public void find()
	{
		var response = request(target().queryParam("name", "min")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		assertThat(response.readEntity(TYPE_LIST_VALUE)).containsExactly(VALUE);

		dao.findWithException(VALUE.id).setActive(VALUE.active = false);
	}

	@Test
	public void find_after_deactivated()
	{
		var response = request(target().queryParam("name", "min")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		assertThat(response.readEntity(TYPE_LIST_VALUE)).isEmpty();
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(PeopleValue.class);
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
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, get(VALUE.id + "INVALID").getStatus(), "Status");
	}

	@Test
	public void modify()
	{
		count(new PeopleFilter().withHasEmail(false), 1L);
		count(new PeopleFilter().withHasEmail(true), 0L);

		var response = request().put(Entity.entity(VALUE.withEmail("min@allclear.app"), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(PeopleValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withUpdatedAt(new Date()), value);
	}

	@Test
	public void modify_count()
	{
		count(new PeopleFilter().withHasEmail(false), 0L);
		count(new PeopleFilter().withHasEmail(true), 1L);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(PeopleValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(value.email, "min@allclear.app", "Check email");
		assertThat(value.updatedAt).as("Check updatedAt").isAfter(value.createdAt);
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new PeopleFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new PeopleFilter(1, 20).withName(VALUE.name), 1L),
			arguments(new PeopleFilter(1, 20).withPhone(VALUE.phone), 1L),
			arguments(new PeopleFilter(1, 20).withHasEmail(true), 1L),
			arguments(new PeopleFilter(1, 20).withHasFirstName(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasLastName(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasDob(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasStatusId(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasStatureId(false), 1L),
			arguments(new PeopleFilter(1, 20).withActive(VALUE.active), 1L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hours(AUTH_AT, -1)), 1L),
			arguments(new PeopleFilter(1, 20).withAuthAtTo(hours(AUTH_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hours(AUTH_AT, -1)).withAuthAtTo(hours(AUTH_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hours(PHONE_VERIFIED_AT, -1)), 1L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtTo(hours(PHONE_VERIFIED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hours(PHONE_VERIFIED_AT, -1)).withPhoneVerifiedAtTo(hours(PHONE_VERIFIED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hours(EMAIL_VERIFIED_AT, -1)), 1L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtTo(hours(EMAIL_VERIFIED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hours(EMAIL_VERIFIED_AT, -1)).withEmailVerifiedAtTo(hours(EMAIL_VERIFIED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new PeopleFilter(1, 20).withId("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withName("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withPhone("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withHasEmail(false), 0L),
			arguments(new PeopleFilter(1, 20).withHasFirstName(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasLastName(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasDob(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasStatusId(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasStatureId(true), 0L),
			arguments(new PeopleFilter(1, 20).withActive(!VALUE.active), 0L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hours(AUTH_AT, 1)), 0L),
			arguments(new PeopleFilter(1, 20).withAuthAtTo(hours(AUTH_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hours(AUTH_AT, 1)).withAuthAtTo(hours(AUTH_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hours(PHONE_VERIFIED_AT, 1)), 0L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtTo(hours(PHONE_VERIFIED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hours(PHONE_VERIFIED_AT, 1)).withPhoneVerifiedAtTo(hours(PHONE_VERIFIED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hours(EMAIL_VERIFIED_AT, 1)), 0L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtTo(hours(EMAIL_VERIFIED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hours(EMAIL_VERIFIED_AT, 1)).withEmailVerifiedAtTo(hours(EMAIL_VERIFIED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final PeopleFilter filter, final long expectedTotal)
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
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, get(VALUE.id).getStatus(), "Status");
	}

	@Test
	public void testRemove_search()
	{
		count(new PeopleFilter().withId(VALUE.id), 0L);
		count(new PeopleFilter().withHasEmail(true), 0L);
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
	private void count(final PeopleFilter filter, long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final PeopleValue expected, final PeopleValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.name, value.name, assertId + "Check name");
		Assertions.assertEquals(expected.phone, value.phone, assertId + "Check phone");
		Assertions.assertEquals(expected.email, value.email, assertId + "Check email");
		Assertions.assertEquals(expected.firstName, value.firstName, assertId + "Check firstName");
		Assertions.assertEquals(expected.lastName, value.lastName, assertId + "Check lastName");
		if (null == expected.dob)
			Assertions.assertNull(value.dob, assertId + "Check dob");
		else
			assertThat(value.dob).as(assertId + "Check dob").isCloseTo(expected.dob, 500L);
		Assertions.assertEquals(expected.statusId, value.statusId, assertId + "Check statusId");
		Assertions.assertEquals(expected.statureId, value.statureId, assertId + "Check statureId");
		Assertions.assertEquals(expected.active, value.active, assertId + "Check active");
		if (null == expected.authAt)
			Assertions.assertNull(value.authAt, assertId + "Check authAt");
		else
			assertThat(value.authAt).as(assertId + "Check authAt").isCloseTo(expected.authAt, 500L);
		if (null == expected.phoneVerifiedAt)
			Assertions.assertNull(value.phoneVerifiedAt, assertId + "Check phoneVerifiedAt");
		else
			assertThat(value.phoneVerifiedAt).as(assertId + "Check phoneVerifiedAt").isCloseTo(expected.phoneVerifiedAt, 500L);
		if (null == expected.emailVerifiedAt)
			Assertions.assertNull(value.emailVerifiedAt, assertId + "Check emailVerifiedAt");
		else
			assertThat(value.emailVerifiedAt).as(assertId + "Check emailVerifiedAt").isCloseTo(expected.emailVerifiedAt, 500L);
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
