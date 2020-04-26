package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ThrottledException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.errors.Validator;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.entity.Customer;
import app.allclear.platform.filter.CustomerFilter;
import app.allclear.platform.value.CustomerValue;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Customer entity.
*
*	@author smalleyd
*	@version 1.1.0
*	@since April 26, 2020
*
**********************************************************************************/

@Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
public class CustomerDAOTest
{
	private static CustomerDAO dao = null;
	private static CustomerValue VALUE = null;
	private static CustomerValue VALID = null;

	@BeforeAll
	public static void up() throws Exception
	{
		dao = new CustomerDAO("test", ConfigTest.loadTest().admins);
	}

	@Test
	public void add()
	{
		var value = dao.add(VALUE = new CustomerValue("Roy Rogers", 10, true));
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.id).as("Check ID").isNotNull().hasSize(36).matches(Validator.PATTERN_UUID);
		check(VALUE, value);
	}

	/** Creates a valid Customer value for the validation tests.
	 *	@return never NULL.
	*/
	private CustomerValue createValid()
	{
		return new CustomerValue("Molly Potter", 5, false);
	}

	@Test
	public void add_missingId()
	{
		assertThrows(ValidationException.class, () -> dao.update(createValid().withId(null)));	// NULL on ADD is expected so only test UPDATE.
	}

	@Test @Disabled
	public void add_longId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withId(StringUtils.repeat("A", CustomerValue.MAX_LEN_ID + 1))));
	}

	@Test
	public void add_missingName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withName(null)));
	}

	@Test
	public void add_longName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withName(StringUtils.repeat("A", CustomerValue.MAX_LEN_NAME + 1))));
	}

	@Test
	public void add_tooLowLimit()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLimit(CustomerValue.MIN_LIMIT - 1)));
	}

	@Test
	public void add_tooHighLimit()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLimit(CustomerValue.MAX_LIMIT + 1)));
	}

	@Test
	public void authenticate()
	{
		count(new CustomerFilter().withHasLastAccessedAt(true), 0L);
		count(new CustomerFilter().withHasLastAccessedAt(false), 1L);
		Assertions.assertEquals(0L, dao.findWithException(VALUE.id).getLastAccessedAt());

		assertThrows(ThrottledException.class, () -> dao.access(VALUE.id, VALUE.limit));
		count(new CustomerFilter().withHasLastAccessedAt(true), 0L);
		count(new CustomerFilter().withHasLastAccessedAt(false), 1L);
		Assertions.assertEquals(0L, dao.findWithException(VALUE.id).getLastAccessedAt());

		assertThrows(ObjectNotFoundException.class, () -> dao.access("INVALID", 0));
		count(new CustomerFilter().withHasLastAccessedAt(true), 0L);
		count(new CustomerFilter().withHasLastAccessedAt(false), 1L);
		Assertions.assertEquals(0L, dao.findWithException(VALUE.id).getLastAccessedAt());

		var value = dao.access(VALUE.id, VALUE.limit - 1);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.lastAccessedAt).as("lastAccessedAt Exists").isNotNull().isCloseTo(new Date(), 500L);
		count(new CustomerFilter().withHasLastAccessedAt(true), 1L);
		count(new CustomerFilter().withHasLastAccessedAt(false), 0L);
		Assertions.assertEquals(value.lastAccessedAt.getTime(), dao.findWithException(VALUE.id).getLastAccessedAt());

		VALUE.lastAccessedAt = value.lastAccessedAt;
	}

	@Test
	public void find()
	{
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	@Test
	public void findWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException("INVALID"));
	}

	@Test
	public void get()
	{
		var value = dao.getByIdWithException(VALUE.id);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void getWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException("INVALID"));
	}

	@Test
	public void getByName()
	{
		assertThat(dao.getByName("Roy")).as("Success").containsExactly(VALUE);
		assertThat(dao.getByName("Molly")).as("Failure").isEmpty();
	}

	@Test
	public void modify()
	{
		var valid = createValid();
		count(new CustomerFilter().withName(VALUE.name), 1L);
		count(new CustomerFilter().withLimit(VALUE.limit), 1L);
		count(new CustomerFilter().withActive(VALUE.active), 1L);
		count(new CustomerFilter().withName(valid.name), 0L);
		count(new CustomerFilter().withLimit(valid.limit), 0L);
		count(new CustomerFilter().withActive(valid.active), 0L);

		var value = dao.update(VALID = valid.withId(VALUE.id));
		Assertions.assertNotNull(value, "Exists");
		check(valid, value);
	}

	@Test
	public void modify_count()
	{
		var valid = createValid();

		count(new CustomerFilter().withName(VALUE.name), 0L);
		count(new CustomerFilter().withLimit(VALUE.limit), 0L);
		count(new CustomerFilter().withActive(VALUE.active), 0L);
		count(new CustomerFilter().withName(valid.name), 1L);
		count(new CustomerFilter().withLimit(valid.limit), 1L);
		count(new CustomerFilter().withActive(valid.active), 1L);
	}

	@Test
	public void modify_find()
	{
		var valid = createValid();
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(valid.name, record.getName(), "Check name");
		Assertions.assertEquals(valid.limit, record.getLimit(), "Check limit");
		Assertions.assertEquals(valid.active, record.getActive(), "Check active");
		check(VALID, record);
	}

	@Test
	public void modify_getByName()
	{
		assertThat(dao.getByName("Roy")).as("Failure").isEmpty();
		assertThat(dao.getByName("Molly")).as("Success").containsExactly(VALID);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new CustomerFilter(1, 20).withId(VALID.id), 1L),
			arguments(new CustomerFilter(1, 20).withName(VALID.name), 1L),
			arguments(new CustomerFilter(1, 20).withLimit(VALID.limit), 1L),
			arguments(new CustomerFilter(1, 20).withHasLimit(true), 1L),
			arguments(new CustomerFilter(1, 20).withLimitFrom(VALID.limit), 1L),
			arguments(new CustomerFilter(1, 20).withLimitTo(VALID.limit), 1L),
			arguments(new CustomerFilter(1, 20).withActive(VALID.active), 1L),
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
			arguments(new CustomerFilter(1, 20).withLimit(VALID.limit + 1000), 0L),
			arguments(new CustomerFilter(1, 20).withHasLimit(false), 0L),
			arguments(new CustomerFilter(1, 20).withLimitFrom(VALID.limit + 1), 0L),
			arguments(new CustomerFilter(1, 20).withLimitTo(VALID.limit - 1), 0L),
			arguments(new CustomerFilter(1, 20).withActive(!VALID.active), 0L),
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
		var results = dao.search(filter);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedTotal, results.total, "Check total");
		if (0L == expectedTotal)
			Assertions.assertNull(results.records, "Records exist");
		else
		{
			Assertions.assertNotNull(results.records, "Records exists");
			int total = (int) expectedTotal;
			if (total > results.pageSize)
			{
				if (results.page == results.pages)
					total%= results.pageSize;
				else
					total = results.pageSize;
			}
			Assertions.assertEquals(total, results.records.size(), "Check records.size");
		}
	}

	public static Stream<Arguments> search_sort()
	{
		return Stream.of(
			arguments(new CustomerFilter("id", null), "id", "ASC"), // Missing sort direction is converted to the default.
			arguments(new CustomerFilter("id", "ASC"), "id", "ASC"),
			arguments(new CustomerFilter("id", "asc"), "id", "ASC"),
			arguments(new CustomerFilter("id", "invalid"), "id", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new CustomerFilter("id", "DESC"), "id", "DESC"),
			arguments(new CustomerFilter("id", "desc"), "id", "DESC"),

			arguments(new CustomerFilter("name", null), "name", "ASC"), // Missing sort direction is converted to the default.
			arguments(new CustomerFilter("name", "ASC"), "name", "ASC"),
			arguments(new CustomerFilter("name", "asc"), "name", "ASC"),
			arguments(new CustomerFilter("name", "invalid"), "name", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new CustomerFilter("name", "DESC"), "name", "DESC"),
			arguments(new CustomerFilter("name", "desc"), "name", "DESC"),

			arguments(new CustomerFilter("limit", null), "limit", "DESC"), // Missing sort direction is converted to the default.
			arguments(new CustomerFilter("limit", "ASC"), "limit", "ASC"),
			arguments(new CustomerFilter("limit", "asc"), "limit", "ASC"),
			arguments(new CustomerFilter("limit", "invalid"), "limit", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new CustomerFilter("limit", "DESC"), "limit", "DESC"),
			arguments(new CustomerFilter("limit", "desc"), "limit", "DESC"),

			arguments(new CustomerFilter("active", null), "active", "DESC"), // Missing sort direction is converted to the default.
			arguments(new CustomerFilter("active", "ASC"), "active", "ASC"),
			arguments(new CustomerFilter("active", "asc"), "active", "ASC"),
			arguments(new CustomerFilter("active", "invalid"), "active", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new CustomerFilter("active", "DESC"), "active", "DESC"),
			arguments(new CustomerFilter("active", "desc"), "active", "DESC"),

			arguments(new CustomerFilter("lastAccessedAt", null), "lastAccessedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new CustomerFilter("lastAccessedAt", "ASC"), "lastAccessedAt", "ASC"),
			arguments(new CustomerFilter("lastAccessedAt", "asc"), "lastAccessedAt", "ASC"),
			arguments(new CustomerFilter("lastAccessedAt", "invalid"), "lastAccessedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new CustomerFilter("lastAccessedAt", "DESC"), "lastAccessedAt", "DESC"),
			arguments(new CustomerFilter("lastAccessedAt", "desc"), "lastAccessedAt", "DESC"),

			arguments(new CustomerFilter("createdAt", null), "createdAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new CustomerFilter("createdAt", "ASC"), "createdAt", "ASC"),
			arguments(new CustomerFilter("createdAt", "asc"), "createdAt", "ASC"),
			arguments(new CustomerFilter("createdAt", "invalid"), "createdAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new CustomerFilter("createdAt", "DESC"), "createdAt", "DESC"),
			arguments(new CustomerFilter("createdAt", "desc"), "createdAt", "DESC"),

			arguments(new CustomerFilter("updatedAt", null), "updatedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new CustomerFilter("updatedAt", "ASC"), "updatedAt", "ASC"),
			arguments(new CustomerFilter("updatedAt", "asc"), "updatedAt", "ASC"),
			arguments(new CustomerFilter("updatedAt", "invalid"), "updatedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new CustomerFilter("updatedAt", "DESC"), "updatedAt", "DESC"),
			arguments(new CustomerFilter("updatedAt", "desc"), "updatedAt", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	private void search_sort(final CustomerFilter filter, final String expectedSortOn, final String expectedSortDir)
	{
		var results = dao.search(filter);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedSortOn, results.sortOn, "Check sortOn");
		Assertions.assertEquals(expectedSortDir, results.sortDir, "Check sortDir");
	}

	/** Test removal after the search. */
	@Test
	public void testRemove()
	{
		Assertions.assertFalse(dao.remove(VALUE.id + "INVALID"), "Invalid");
		Assertions.assertTrue(dao.remove(VALUE.id), "Removed");
		Assertions.assertFalse(dao.remove(VALUE.id), "Already removed");
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_find()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.id));
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_search()
	{
		var valid = createValid();

		count(new CustomerFilter().withId(VALUE.id), 0L);
		count(new CustomerFilter().withName(valid.name), 0L);
		count(new CustomerFilter().withLimit(valid.limit), 0L);
		count(new CustomerFilter().withActive(valid.active), 0L);
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final CustomerFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied entity record. */
	private void check(final CustomerValue expected, final Customer record)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, record.id(), assertId + "Check id");
		Assertions.assertEquals(expected.name, record.getName(), assertId + "Check name");
		Assertions.assertEquals(expected.limit, record.getLimit(), assertId + "Check limit");
		Assertions.assertEquals(expected.active, record.getActive(), assertId + "Check active");
		if (null == expected.lastAccessedAt)
			Assertions.assertEquals(0L, record.getLastAccessedAt(), "Check lastAccessedAt");
		else
			Assertions.assertEquals(expected.lastAccessedAt.getTime(), record.getLastAccessedAt(), "Check lastAccessedAt");
		Assertions.assertEquals(expected.createdAt, record.getCreatedAt(), assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, record.getUpdatedAt(), assertId + "Check updatedAt");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final CustomerValue expected, final CustomerValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.name, value.name, assertId + "Check name");
		Assertions.assertEquals(expected.limit, value.limit, assertId + "Check limit");
		Assertions.assertEquals(expected.active, value.active, assertId + "Check active");
		Assertions.assertEquals(expected.lastAccessedAt, value.lastAccessedAt, assertId + "Check lastAccessedAt");
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, value.updatedAt, assertId + "Check updatedAt");
	}
}
