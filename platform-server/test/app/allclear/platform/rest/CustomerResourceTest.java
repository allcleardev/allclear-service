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
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.CustomerDAO;
import app.allclear.platform.filter.CustomerFilter;
import app.allclear.platform.value.CustomerValue;

/**********************************************************************************
*
*	Functional test for the RESTful resource that handles access to the Customer entity.
*
*	@author smalleyd
*	@version 1.1.0
*	@since April 26, 2020
*
**********************************************************************************/

@Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class CustomerResourceTest
{
	private static CustomerDAO dao = null;
	private static CustomerValue VALUE = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new CustomerResource(dao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/customers";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<CustomerValue>> TYPE_LIST_VALUE = new GenericType<List<CustomerValue>>() {};
	private static final GenericType<QueryResults<CustomerValue, CustomerFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<CustomerValue, CustomerFilter>>() {};

	@BeforeAll
	public static void up() throws Exception
	{
		dao = new CustomerDAO("test", ConfigTest.loadTest().admins);
	}

	@Test
	public void add()
	{
		var now = new Date();
		var response = request()
			.post(Entity.entity(VALUE = new CustomerValue("Anne Archer"), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(CustomerValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.id).as("Check ID").isNotNull().hasSize(36).matches(Validator.PATTERN_UUID);
		check(VALUE.withId(value.id).withCreatedAt(now).withUpdatedAt(now), value);
	}

	@Test
	public void authenticate()
	{
		count(new CustomerFilter().withHasLastAccessedAt(true), 0L);
		count(new CustomerFilter().withHasLastAccessedAt(false), 1L);
		Assertions.assertNull(dao.getByIdWithException(VALUE.id).lastAccessedAt);

		var value = dao.access(VALUE.id, CustomerValue.MAX_LIMIT + 1);	// Does NOT trigger throttle exception because no limit is specified on the customer.
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.lastAccessedAt).as("lastAccessedAt Exists").isNotNull().isCloseTo(new Date(), 500L);
		count(new CustomerFilter().withHasLastAccessedAt(true), 1L);
		count(new CustomerFilter().withHasLastAccessedAt(false), 0L);
		Assertions.assertEquals(value.lastAccessedAt, dao.getByIdWithException(VALUE.id).lastAccessedAt);

		VALUE.lastAccessedAt = value.lastAccessedAt;
	}

	@Test
	public void find()
	{
		var response = request(target().queryParam("name", "Ann")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		assertThat(response.readEntity(TYPE_LIST_VALUE)).as("Check results").isNotNull().containsExactly(VALUE);
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(CustomerValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(0, value.limit, "Check limit");
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
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get("INVALID").getStatus(), "Status");
	}

	@Test
	public void modify()
	{
		count(new CustomerFilter().withHasLimit(false), 1L);
		count(new CustomerFilter().withHasLimit(true), 0L);

		var response = request().put(Entity.entity(VALUE.withLimit(20), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(CustomerValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withUpdatedAt(new Date()), value);
	}

	@Test
	public void modify_count()
	{
		count(new CustomerFilter().withLimit(1), 0L);
		count(new CustomerFilter().withHasLimit(false), 0L);
		count(new CustomerFilter().withHasLimit(true), 1L);
		count(new CustomerFilter().withLimit(20), 1L);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(CustomerValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.limit).as("Check limit").isNotNull().isEqualTo(20);
		assertThat(value.updatedAt).as("Check updatedAt").isAfter(value.createdAt);
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new CustomerFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new CustomerFilter(1, 20).withName(VALUE.name), 1L),
			arguments(new CustomerFilter(1, 20).withLimit(VALUE.limit), 1L),
			arguments(new CustomerFilter(1, 20).withHasLimit(true), 1L),
			arguments(new CustomerFilter(1, 20).withLimitFrom(VALUE.limit), 1L),
			arguments(new CustomerFilter(1, 20).withLimitTo(VALUE.limit), 1L),
			arguments(new CustomerFilter(1, 20).withActive(VALUE.active), 1L),
			arguments(new CustomerFilter(1, 20).withHasLastAccessedAt(true), 1L),
			arguments(new CustomerFilter(1, 20).withLastAccessedAtFrom(hourAgo), 1L),
			arguments(new CustomerFilter(1, 20).withLastAccessedAtTo(hourAhead), 1L),
			arguments(new CustomerFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new CustomerFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new CustomerFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new CustomerFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new CustomerFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new CustomerFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new CustomerFilter(1, 20).withId("invalid"), 0L),
			arguments(new CustomerFilter(1, 20).withName("invalid"), 0L),
			arguments(new CustomerFilter(1, 20).withLimit(VALUE.limit + 1000), 0L),
			arguments(new CustomerFilter(1, 20).withHasLimit(false), 0L),
			arguments(new CustomerFilter(1, 20).withLimitFrom(VALUE.limit + 1), 0L),
			arguments(new CustomerFilter(1, 20).withLimitTo(VALUE.limit - 1), 0L),
			arguments(new CustomerFilter(1, 20).withActive(!VALUE.active), 0L),
			arguments(new CustomerFilter(1, 20).withHasLastAccessedAt(false), 0L),
			arguments(new CustomerFilter(1, 20).withLastAccessedAtFrom(hourAhead), 0L),
			arguments(new CustomerFilter(1, 20).withLastAccessedAtTo(hourAgo), 0L),
			arguments(new CustomerFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new CustomerFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new CustomerFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new CustomerFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new CustomerFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new CustomerFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final CustomerFilter filter, final long expectedTotal)
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

	@Test
	public void testRemove_search()
	{
		count(new CustomerFilter().withId(VALUE.id), 0L);
		count(new CustomerFilter().withHasLimit(true), 0L);
		count(new CustomerFilter().withLimit(20), 0L);
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
	private void count(final CustomerFilter filter, long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final CustomerValue expected, final CustomerValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.name, value.name, assertId + "Check name");
		Assertions.assertEquals(expected.limit, value.limit, assertId + "Check limited");
		Assertions.assertEquals(expected.active, value.active, assertId + "Check active");
		assertThat(value.createdAt).as(assertId + "Check createdAt").isCloseTo(expected.createdAt, 500L);
		assertThat(value.updatedAt).as(assertId + "Check updatedAt").isCloseTo(expected.updatedAt, 500L);
	}
}
