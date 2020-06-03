package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.*;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.entity.Experiences;
import app.allclear.platform.filter.ExperiencesFilter;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Experiences entity.
*
*	@author smalleyd
*	@version 1.1.80
*	@since June 2, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class ExperiencesDAOTest
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
	private static final SessionValue CUSTOMER = new SessionValue(new CustomerValue("cust1"));
	private static final SessionValue EDITOR = new SessionValue(false, new AdminValue("admin", false, true));
	private static SessionValue SESSION = null;
	private static SessionValue SESSION_1 = null;

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

		sessionDao.current(SESSION);
		var value = dao.add(VALUE = new ExperiencesValue(FACILITY_1.id, true));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Creates a valid Experiences value for the validation tests.
	 *	@return never NULL.
	*/
	private ExperiencesValue createValid()
	{
		return new ExperiencesValue(FACILITY.id, false);
	}

	@Test
	public void add_dupe()
	{
		sessionDao.current(SESSION);
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(FACILITY_1.id))))
			.hasMessage("You have already provided an Experience for " + FACILITY_1.name + ".");
	}

	@Test
	public void add_missingPersonId()
	{
		sessionDao.current(new PeopleValue("min", "888-555-2000", true).withId(null));
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid())))
			.hasMessage("Person is not set.");
	}

	@Test
	public void add_longPersonId()
	{
		sessionDao.current(new PeopleValue("min", "888-555-2000", true).withId(StringUtils.repeat('A', PeopleValue.MAX_LEN_ID + 1)));
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid())))
			.hasMessage("Person 'AAAAAAAAAAA' is longer than the expected size of 10.");
	}

	@Test
	public void add_invalidPersonId()
	{
		sessionDao.current(new PeopleValue("min", "888-555-2000", true).withId("INVALID"));
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid())))
			.hasMessage("The Person ID, INVALID, is invalid.");
	}

	@Test
	public void add_missingFacilityId()
	{
		sessionDao.current(SESSION_1);
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(null))))
			.hasMessage("Facility is not set.");
	}

	@Test
	public void add_invalidFacilityId()
	{
		sessionDao.current(SESSION_1);
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(FACILITY.id + 1000L))))
			.hasMessage("The Facility ID, 1001, is invalid.");
	}

	public static Stream<SessionValue> add_noAuth() { return Stream.of(ADMIN, CUSTOMER, EDITOR); }

	@ParameterizedTest
	@MethodSource
	public void add_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		assertThrows(NotAuthorizedException.class, () -> dao.add(createValid()));
	}

	public static Stream<SessionValue> find() { return Stream.of(ADMIN, SESSION); }

	@ParameterizedTest
	@MethodSource
	public void find(final SessionValue s)
	{
		sessionDao.current(s);

		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	public static Stream<SessionValue> find_noAuth() { return Stream.of(CUSTOMER, EDITOR, SESSION_1); }

	@ParameterizedTest
	@MethodSource
	public void find_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		assertThrows(NotAuthorizedException.class, () -> dao.findWithException(VALUE.id));
	}

	@Test
	public void findWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.id + 1000L));
	}

	@Test
	public void get()
	{
		var value = dao.getByIdWithException(VALUE.id);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(PERSON.id, value.personId, "Check personId");
		Assertions.assertEquals(FACILITY_1.id, value.facilityId, "Check facilityId");
		Assertions.assertTrue(value.positive, "Check positive");
		check(VALUE, value);
	}

	@Test
	public void getWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException(VALUE.id + 1000L));
	}

	@Test
	public void modify()
	{
		count(new ExperiencesFilter().withPersonId(PERSON.id), 1L);
		count(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 1L);
		count(new ExperiencesFilter().withPositive(true), 1L);
		count(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L);
		count(new ExperiencesFilter().withFacilityId(FACILITY.id), 0L);
		count(new ExperiencesFilter().withPositive(false), 0L);

		var value = dao.update(VALUE.withPersonId(PERSON_1.id).withFacilityId(FACILITY.id).withPositive(false));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);

		Assertions.assertEquals(PERSON_1.name, VALUE.personName, "Check personName");
		Assertions.assertEquals(FACILITY.name, VALUE.facilityName, "Check facilityName");
	}

	@Test
	public void modify_count()
	{
		count(new ExperiencesFilter().withPersonId(PERSON.id), 0L);
		count(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L);
		count(new ExperiencesFilter().withPositive(true), 0L);
		count(new ExperiencesFilter().withPersonId(PERSON_1.id), 1L);
		count(new ExperiencesFilter().withFacilityId(FACILITY.id), 1L);
		count(new ExperiencesFilter().withPositive(false), 1L);
	}

	public static Stream<SessionValue> modify_fail() { return Stream.of(CUSTOMER, EDITOR, SESSION, SESSION_1); }

	@ParameterizedTest
	@MethodSource
	public void modify_fail(final SessionValue s)
	{
		sessionDao.current(s);
		assertThrows(NotAuthorizedException.class, () -> dao.remove(VALUE.id));
	}

	public static Stream<SessionValue> modify_find() { return Stream.of(ADMIN, SESSION_1); }

	@ParameterizedTest
	@MethodSource
	public void modify_find(final SessionValue s)
	{
		sessionDao.current(s);

		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(PERSON_1.id, record.getPersonId(), "Check personId");
		Assertions.assertEquals(PERSON_1.name, record.getPerson().getName(), "Check personName");
		Assertions.assertEquals(FACILITY.id, record.getFacilityId(), "Check facilityId");
		Assertions.assertEquals(FACILITY.name, record.getFacility().getName(), "Check facilityName");
		Assertions.assertFalse(record.isPositive(), "Check positive");
		check(VALUE, record);
	}

	public static Stream<SessionValue> modify_find_noAuth() { return Stream.of(CUSTOMER, EDITOR, SESSION); }

	@ParameterizedTest
	@MethodSource
	public void modify_find_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		assertThrows(NotAuthorizedException.class, () -> dao.findWithException(VALUE.id));
	}

	public static Stream<SessionValue> remove_fail() { return Stream.of(CUSTOMER, EDITOR, SESSION, SESSION_1); }

	@ParameterizedTest
	@MethodSource
	public void remove_fail(final SessionValue s)
	{
		sessionDao.current(s);
		assertThrows(NotAuthorizedException.class, () -> dao.remove(VALUE.id));
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
			arguments(SESSION, new ExperiencesFilter(1, 20).withId(VALUE.id), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPersonId(VALUE.personId), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPositive(VALUE.positive), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withId(VALUE.id), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPositive(VALUE.positive), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(ADMIN, new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPersonId("invalid"), 1L),	// Overridden based on the current user.
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final SessionValue s, final ExperiencesFilter filter, final long expectedTotal)
	{
		sessionDao.current(s);

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

	public static Stream<SessionValue> search_fail() { return Stream.of(CUSTOMER, EDITOR); }

	@ParameterizedTest
	@MethodSource
	public void search_fail(final SessionValue s)
	{
		sessionDao.current(s);
		assertThrows(NotAuthorizedException.class, () -> dao.search(new ExperiencesFilter()));
	}

	public static Stream<Arguments> search_sort()
	{
		return Stream.of(
			arguments(new ExperiencesFilter(null, null), "id", "DESC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter(null, "ASC"), "id", "DESC"),
			arguments(new ExperiencesFilter(null, "invalid"), "id", "DESC"),
			arguments(new ExperiencesFilter("invalid", "invalid"), "id", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("invalid", null), "id", "DESC"),
			arguments(new ExperiencesFilter("invalid", "desc"), "id", "DESC"),

			arguments(new ExperiencesFilter("id", null), "id", "DESC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter("id", "ASC"), "id", "ASC"),
			arguments(new ExperiencesFilter("id", "asc"), "id", "ASC"),
			arguments(new ExperiencesFilter("id", "invalid"), "id", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("id", "DESC"), "id", "DESC"),
			arguments(new ExperiencesFilter("id", "desc"), "id", "DESC"),

			arguments(new ExperiencesFilter("personId", null), "personId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter("personId", "ASC"), "personId", "ASC"),
			arguments(new ExperiencesFilter("personId", "asc"), "personId", "ASC"),
			arguments(new ExperiencesFilter("personId", "invalid"), "personId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("personId", "DESC"), "personId", "DESC"),
			arguments(new ExperiencesFilter("personId", "desc"), "personId", "DESC"),

			arguments(new ExperiencesFilter("personName", null), "personName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter("personName", "ASC"), "personName", "ASC"),
			arguments(new ExperiencesFilter("personName", "asc"), "personName", "ASC"),
			arguments(new ExperiencesFilter("personName", "invalid"), "personName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("personName", "DESC"), "personName", "DESC"),
			arguments(new ExperiencesFilter("personName", "desc"), "personName", "DESC"),

			arguments(new ExperiencesFilter("facilityId", null), "facilityId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter("facilityId", "ASC"), "facilityId", "ASC"),
			arguments(new ExperiencesFilter("facilityId", "asc"), "facilityId", "ASC"),
			arguments(new ExperiencesFilter("facilityId", "invalid"), "facilityId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("facilityId", "DESC"), "facilityId", "DESC"),
			arguments(new ExperiencesFilter("facilityId", "desc"), "facilityId", "DESC"),

			arguments(new ExperiencesFilter("facilityName", null), "facilityName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter("facilityName", "ASC"), "facilityName", "ASC"),
			arguments(new ExperiencesFilter("facilityName", "asc"), "facilityName", "ASC"),
			arguments(new ExperiencesFilter("facilityName", "invalid"), "facilityName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("facilityName", "DESC"), "facilityName", "DESC"),
			arguments(new ExperiencesFilter("facilityName", "desc"), "facilityName", "DESC"),

			arguments(new ExperiencesFilter("positive", null), "positive", "DESC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter("positive", "ASC"), "positive", "ASC"),
			arguments(new ExperiencesFilter("positive", "asc"), "positive", "ASC"),
			arguments(new ExperiencesFilter("positive", "invalid"), "positive", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("positive", "DESC"), "positive", "DESC"),
			arguments(new ExperiencesFilter("positive", "desc"), "positive", "DESC"),

			arguments(new ExperiencesFilter("createdAt", null), "createdAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter("createdAt", "ASC"), "createdAt", "ASC"),
			arguments(new ExperiencesFilter("createdAt", "asc"), "createdAt", "ASC"),
			arguments(new ExperiencesFilter("createdAt", "invalid"), "createdAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("createdAt", "DESC"), "createdAt", "DESC"),
			arguments(new ExperiencesFilter("createdAt", "desc"), "createdAt", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	public void search_sort(final ExperiencesFilter filter, final String expectedSortOn, final String expectedSortDir)
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
		Assertions.assertFalse(dao.remove(VALUE.id + 1000L), "Invalid");
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
		count(new ExperiencesFilter().withId(VALUE.id), 0L);
		count(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L);
		count(new ExperiencesFilter().withFacilityId(FACILITY.id), 0L);
		count(new ExperiencesFilter().withPositive(false), 0L);
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final ExperiencesFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied entity record. */
	private void check(final ExperiencesValue expected, final Experiences record)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, record.getId(), assertId + "Check id");
		Assertions.assertEquals(expected.personId, record.getPersonId(), assertId + "Check personId");
		Assertions.assertEquals(expected.facilityId, record.getFacilityId(), assertId + "Check facilityId");
		Assertions.assertEquals(expected.positive, record.isPositive(), assertId + "Check positive");
		Assertions.assertEquals(expected.createdAt, record.getCreatedAt(), assertId + "Check createdAt");
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
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
	}
}
