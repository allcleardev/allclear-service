package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.platform.type.Experience.*;
import static app.allclear.testing.TestingUtils.*;

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.*;
import app.allclear.platform.App;
import app.allclear.platform.entity.Experiences;
import app.allclear.platform.filter.ExperiencesFilter;
import app.allclear.platform.model.ExperiencesCalcResponse;
import app.allclear.platform.type.Experience;
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
	private static final SessionDAO sessionDao = new FakeSessionDAO();
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

		for (int i = 2; i < 10; i++) facilityDao.add(new FacilityValue(i), true);
	}

	public static Stream<Arguments> add_counts()
	{
		return Stream.of(
			arguments(PERSON.id, FACILITY.id, 0L, 0L),
			arguments(PERSON.id, FACILITY_1.id, 0L, 0L),
			arguments(PERSON_1.id, FACILITY.id, 0L, 0L),
			arguments(PERSON_1.id, FACILITY_1.id, 0L, 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void add_counts(final String personId, final Long facilityId, final long first, final long second)
	{
		assertThat(dao.countTodayExperiencesByPerson(personId, facilityId)).containsSequence(first, second);
	}

	public static Stream<Arguments> add_counts_limit()
	{
		return Stream.of(
			arguments(SESSION, FACILITY),
			arguments(SESSION_1, FACILITY),
			arguments(SESSION, FACILITY_1),
			arguments(SESSION_1, FACILITY_1));
	}

	@ParameterizedTest
	@MethodSource
	public void add_counts_limit(final SessionValue s, final FacilityValue facility)
	{
		sessionDao.current(s);

		var o = dao.limit(facility.id);
		Assertions.assertEquals(0L, o.total, "Check total");
		Assertions.assertEquals(0L, o.byFacility, "Check byFacility");
	}

	/** Creates a valid Experiences value for the validation tests.
	 *	@return never NULL.
	*/
	private ExperiencesValue createValid()
	{
		return new ExperiencesValue(FACILITY.id, false);
	}

	@Test
	public void add_d()
	{
		sessionDao.current(SESSION);
		var value = dao.add(VALUE = new ExperiencesValue(FACILITY_1.id, true).withTags(GOOD_HYGIENE, SOCIAL_DISTANCING_ENFORCED, OVERLY_CROWDED));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), value);	// Replace tags with re-ordered as will be retrieved.
	}

	public static Stream<Arguments> add_d_counts()
	{
		return Stream.of(
			arguments(PERSON.id, FACILITY.id, 1L, 0L),
			arguments(PERSON.id, FACILITY_1.id, 1L, 1L),
			arguments(PERSON_1.id, FACILITY.id, 0L, 0L),
			arguments(PERSON_1.id, FACILITY_1.id, 0L, 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void add_d_counts(final String personId, final Long facilityId, final long first, final long second)
	{
		assertThat(dao.countTodayExperiencesByPerson(personId, facilityId)).containsSequence(first, second);
	}

	@Test
	public void add_dupe()
	{
		sessionDao.current(SESSION);
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(FACILITY_1.id))))
			.hasMessage("You have already provided an Experience for " + FACILITY_1.name + " today.");
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

	public static Stream<SessionValue> add_noAuth() { return Stream.of(null, ADMIN, CUSTOMER, EDITOR); }

	@ParameterizedTest
	@MethodSource
	public void add_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		assertThrows(NotAuthorizedException.class, () -> dao.add(createValid()));
	}

	public static Stream<SessionValue> find() { return Stream.of(ADMIN, SESSION); }

	@Test
	public void calc()
	{
		check(dao.calcByFacility(FACILITY_1.id), 1L, 0L, GOOD_HYGIENE, SOCIAL_DISTANCING_ENFORCED, OVERLY_CROWDED);
	}

	@Test
	public void calc_none()
	{
		check(dao.calcByFacility(FACILITY.id), 0L, 0L);
	}

	@Test
	public void calc_null()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.calcByFacility(null)))
			.hasMessage("Must provide a Facility identifier.");
	}

	@ParameterizedTest
	@MethodSource
	public void find(final SessionValue s)
	{
		sessionDao.current(s);

		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	public static Stream<SessionValue> find_noAuth() { return Stream.of(null, CUSTOMER, EDITOR, SESSION_1); }

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
		assertThat(value.tags).as("Check tags").containsExactly(GOOD_HYGIENE.named(), OVERLY_CROWDED.named(), SOCIAL_DISTANCING_ENFORCED.named());
		check(VALUE, value);
	}

	@Test
	public void getWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException(VALUE.id + 1000L));
	}

	public static Stream<Arguments> limit()
	{
		return Stream.of(
			arguments(SESSION, FACILITY, 1L),
			arguments(SESSION_1, FACILITY, 0L),
			// arguments(SESSION, FACILITY_1, 1L),	// throws exception due to limit reached.
			arguments(SESSION_1, FACILITY_1, 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void limit(final SessionValue s, final FacilityValue facility, final long expected)
	{
		sessionDao.current(s);

		var o = dao.limit(facility.id);
		Assertions.assertEquals(expected, o.total, "Check total");
		Assertions.assertEquals(0L, o.byFacility, "Check byFacility");
	}

	public static Stream<Arguments> limit_fail()
	{
		return Stream.of(
			arguments(null, FACILITY_1.id, NotAuthorizedException.class, "Must be a Person session."),
			arguments(ADMIN, FACILITY_1.id, NotAuthorizedException.class, "Must be a Person session."),
			arguments(CUSTOMER, FACILITY_1.id, NotAuthorizedException.class, "Must be a Person session."),
			arguments(EDITOR, FACILITY_1.id, NotAuthorizedException.class, "Must be a Person session."),
			arguments(SESSION, null, ValidationException.class, "Facility is not set."),
			arguments(SESSION, FACILITY_1.id, ValidationException.class, "You have already provided an Experience for Test Center 1 today."),
			arguments(SESSION, FACILITY_1.id + 1000L, ValidationException.class, "The Facility ID, 1002, is invalid."));
	}

	@ParameterizedTest
	@MethodSource
	public void limit_fail(final SessionValue s, final Long facilityId, final Class<? extends Throwable> ex, final String message)
	{
		sessionDao.current(s);

		assertThat(assertThrows(ex, () -> dao.limit(facilityId))).hasMessage(message);
	}

	public static Stream<Arguments> modif()
	{
		return Stream.of(
			arguments(new ExperiencesFilter().withPersonId(PERSON.id), 1L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 1L),
			arguments(new ExperiencesFilter().withPositive(true), 1L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE), 1L),
			arguments(new ExperiencesFilter().withIncludeTags(OVERLY_CROWDED), 1L),
			arguments(new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED), 1L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L),
			arguments(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY.id), 0L),
			arguments(new ExperiencesFilter().withPositive(false), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(OVERLY_CROWDED), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L));
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
		var value = dao.update(VALUE.withPersonId(PERSON_1.id).withFacilityId(FACILITY.id).withPositive(false).emptyTags());
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);

		Assertions.assertEquals(PERSON_1.name, VALUE.personName, "Check personName");
		Assertions.assertEquals(FACILITY.name, VALUE.facilityName, "Check facilityName");
	}

	@Test
	public void modify_calc()
	{
		check(dao.calcByFacility(FACILITY.id), 0L, 1L);
	}

	@Test
	public void modify_calc_none()
	{
		check(dao.calcByFacility(FACILITY_1.id), 0L, 0L);
	}

	public static Stream<Arguments> modify_count()
	{
		return Stream.of(
			arguments(new ExperiencesFilter().withPersonId(PERSON.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L),
			arguments(new ExperiencesFilter().withPositive(true), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(OVERLY_CROWDED), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(new ExperiencesFilter().withPersonId(PERSON_1.id), 1L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY.id), 1L),
			arguments(new ExperiencesFilter().withPositive(false), 1L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE), 1L),
			arguments(new ExperiencesFilter().withExcludeTags(OVERLY_CROWDED), 1L),
			arguments(new ExperiencesFilter().withExcludeTags(SOCIAL_DISTANCING_ENFORCED), 1L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_count(final ExperiencesFilter filter, final long expected)
	{
		count(filter, expected);
	}

	public static Stream<Arguments> modify_d_counts()
	{
		return Stream.of(
			arguments(PERSON.id, FACILITY.id, 0L, 0L),
			arguments(PERSON.id, FACILITY_1.id, 0L, 0L),
			arguments(PERSON_1.id, FACILITY.id, 1L, 1L),
			arguments(PERSON_1.id, FACILITY_1.id, 1L, 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_d_counts(final String personId, final Long facilityId, final long first, final long second)
	{
		assertThat(dao.countTodayExperiencesByPerson(personId, facilityId)).containsSequence(first, second);
	}

	public static Stream<SessionValue> modify_fail() { return Stream.of(null, CUSTOMER, EDITOR, SESSION, SESSION_1); }

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
		assertThat(record.getTags()).as("Check tags").isEmpty();
		check(VALUE, record);
	}

	public static Stream<SessionValue> modify_find_noAuth() { return Stream.of(null, CUSTOMER, EDITOR, SESSION); }

	@ParameterizedTest
	@MethodSource
	public void modify_find_noAuth(final SessionValue s)
	{
		sessionDao.current(s);
		assertThrows(NotAuthorizedException.class, () -> dao.findWithException(VALUE.id));
	}

	public static Stream<Arguments> modify_limit()
	{
		return Stream.of(
			arguments(SESSION, FACILITY, 0L),
			// arguments(SESSION_1, FACILITY, 0L),	// throws exception due to limit reached.
			arguments(SESSION, FACILITY_1, 0L),
			arguments(SESSION_1, FACILITY_1, 1L));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_limit(final SessionValue s, final FacilityValue facility, final long expected)
	{
		sessionDao.current(s);

		var o = dao.limit(facility.id);
		Assertions.assertEquals(expected, o.total, "Check total");
		Assertions.assertEquals(0L, o.byFacility, "Check byFacility");
	}

	@Test
	@MethodSource
	public void modify_limit_fail()
	{
		sessionDao.current(SESSION_1);

		assertThat(assertThrows(ValidationException.class, () -> dao.limit(FACILITY.id))).hasMessage("You have already provided an Experience for Test Center 0 today.");
	}

	public static Stream<SessionValue> remove_fail() { return Stream.of(null, CUSTOMER, EDITOR, SESSION, SESSION_1); }

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
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),
			arguments(ADMIN, new ExperiencesFilter(1, 20).withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withId(VALUE.id), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPersonId(VALUE.personId), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPositive(VALUE.positive), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withId(VALUE.id), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPositive(VALUE.positive), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L),

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
			arguments(ADMIN, new ExperiencesFilter(1, 20).withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L),
			arguments(SESSION, new ExperiencesFilter(1, 20).withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPersonId("invalid"), 1L),	// Overridden based on the current user.
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L),
			arguments(SESSION_1, new ExperiencesFilter(1, 20).withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L));
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

	public static Stream<SessionValue> search_fail() { return Stream.of(null, CUSTOMER, EDITOR); }

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
			arguments(new ExperiencesFilter("createdAt", "desc"), "createdAt", "DESC"),

			arguments(new ExperiencesFilter("updatedAt", null), "updatedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new ExperiencesFilter("updatedAt", "ASC"), "updatedAt", "ASC"),
			arguments(new ExperiencesFilter("updatedAt", "asc"), "updatedAt", "ASC"),
			arguments(new ExperiencesFilter("updatedAt", "invalid"), "updatedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new ExperiencesFilter("updatedAt", "DESC"), "updatedAt", "DESC"),
			arguments(new ExperiencesFilter("updatedAt", "desc"), "updatedAt", "DESC"));
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

	@Test
	public void testRemove_check()
	{
		check(dao.calcByFacility(FACILITY.id), 0L, 0L);
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_find()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.id));
	}

	public static Stream<Arguments> testRemove_search()
	{
		return Stream.of(
			arguments(new ExperiencesFilter().withId(VALUE.id), 0L),
			arguments(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY.id), 0L),
			arguments(new ExperiencesFilter().withPositive(false), 0L),
			arguments(new ExperiencesFilter().withExcludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void testRemove_search(final ExperiencesFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void z_00_add()
	{
		sessionDao.current(SESSION);
		VALUE = dao.add(createValid().withTags(POOR_HYGIENE, CONFUSING_APPOINTMENT_PROCESS));
	}

	@Test
	public void z_00_calc()
	{
		sessionDao.clear();	// No authentication required.
		check(dao.calcByFacility(FACILITY.id), 0L, 1L, POOR_HYGIENE, CONFUSING_APPOINTMENT_PROCESS);
	}

	public static Stream<Arguments> z_00_d_counts()
	{
		return Stream.of(
			arguments(PERSON.id, FACILITY.id, 1L, 1L),
			arguments(PERSON.id, FACILITY_1.id, 1L, 0L),
			arguments(PERSON_1.id, FACILITY.id, 0L, 0L),
			arguments(PERSON_1.id, FACILITY_1.id, 0L, 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void z_00_d_counts(final String personId, final Long facilityId, final long first, final long second)
	{
		assertThat(dao.countTodayExperiencesByPerson(personId, facilityId)).containsSequence(first, second);
	}

	@Test
	public void z_00_get()
	{
		assertThat(dao.getById(VALUE.id).tags).as("Check tags").containsExactly(CONFUSING_APPOINTMENT_PROCESS.named(), POOR_HYGIENE.named());
	}

	@Test
	public void z_00_search()
	{
		search(ADMIN, new ExperiencesFilter().withIncludeTags(CONFUSING_APPOINTMENT_PROCESS), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(POOR_HYGIENE), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(POOR_HYGIENE, FRIENDLY_STAFF), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(CONFUSING_APPOINTMENT_PROCESS, POOR_HYGIENE), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(OVERLY_CROWDED), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED, FRIENDLY_STAFF), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L);
		assertThat(dao.search(new ExperiencesFilter()).records.get(0).tags).as("Check tags").containsExactly(CONFUSING_APPOINTMENT_PROCESS.named(), POOR_HYGIENE.named());
	}

	@Test
	public void z_01_add()
	{
		dao.update(VALUE.nullTags());	// No change
	}

	@Test
	public void z_01_calc() { z_00_calc(); }

	@ParameterizedTest
	@MethodSource("z_00_d_counts")
	public void z_01_d_counts(final String personId, final Long facilityId, final long first, final long second)
	{
		assertThat(dao.countTodayExperiencesByPerson(personId, facilityId)).containsSequence(first, second);
	}

	@Test
	public void z_01_get() { z_00_get(); }

	@Test
	public void z_01_search() { z_00_search(); }

	@Test
	public void z_02_add()
	{
		dao.update(VALUE.withTags(SOCIAL_DISTANCING_ENFORCED, OVERLY_CROWDED, GOOD_HYGIENE));
	}

	@Test
	public void z_02_calc()
	{
		sessionDao.clear();	// No authentication required.
		check(dao.calcByFacility(FACILITY.id), 0L, 1L, SOCIAL_DISTANCING_ENFORCED, OVERLY_CROWDED, GOOD_HYGIENE);
	}

	@Test
	public void z_02_get()
	{
		assertThat(dao.getById(VALUE.id).tags).as("Check tags").containsExactly(GOOD_HYGIENE.named(), OVERLY_CROWDED.named(), SOCIAL_DISTANCING_ENFORCED.named());
	}

	@Test
	public void z_02_search()
	{
		search(ADMIN, new ExperiencesFilter().withIncludeTags(CONFUSING_APPOINTMENT_PROCESS), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(POOR_HYGIENE), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(POOR_HYGIENE, FRIENDLY_STAFF), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(CONFUSING_APPOINTMENT_PROCESS, POOR_HYGIENE), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(OVERLY_CROWDED), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED, FRIENDLY_STAFF), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L);
		assertThat(dao.search(new ExperiencesFilter()).records.get(0).tags).as("Check tags")
			.containsExactly(GOOD_HYGIENE.named(), OVERLY_CROWDED.named(), SOCIAL_DISTANCING_ENFORCED.named());
	}

	@Test
	public void z_03_add()
	{
		dao.update(VALUE.withPositive(true).withTags(SOCIAL_DISTANCING_ENFORCED, CONFUSING_APPOINTMENT_PROCESS, GOOD_HYGIENE));
	}

	@Test
	public void z_03_calc()
	{
		sessionDao.clear();	// No authentication required.
		check(dao.calcByFacility(FACILITY.id), 1L, 0L, SOCIAL_DISTANCING_ENFORCED, CONFUSING_APPOINTMENT_PROCESS, GOOD_HYGIENE);
	}

	@Test
	public void z_03_get()
	{
		assertThat(dao.getById(VALUE.id).tags).as("Check tags").containsExactly(CONFUSING_APPOINTMENT_PROCESS.named(), GOOD_HYGIENE.named(), SOCIAL_DISTANCING_ENFORCED.named());
	}

	@Test
	public void z_03_search()
	{
		search(ADMIN, new ExperiencesFilter().withIncludeTags(CONFUSING_APPOINTMENT_PROCESS), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(POOR_HYGIENE), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(POOR_HYGIENE, FRIENDLY_STAFF), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(CONFUSING_APPOINTMENT_PROCESS, POOR_HYGIENE), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(OVERLY_CROWDED), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED, FRIENDLY_STAFF), 1L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 1L);
		assertThat(dao.search(new ExperiencesFilter()).records.get(0).tags).as("Check tags")
			.containsExactly(CONFUSING_APPOINTMENT_PROCESS.named(), GOOD_HYGIENE.named(), SOCIAL_DISTANCING_ENFORCED.named());
	}

	@Test
	public void z_04_add()
	{
		dao.update(VALUE.emptyTags());
	}

	@Test
	public void z_04_calc()
	{
		sessionDao.clear();	// No authentication required.
		check(dao.calcByFacility(FACILITY.id), 1L, 0L);
	}

	@Test
	public void z_04_get()
	{
		assertThat(dao.getById(VALUE.id).tags).as("Check tags").isNull();
	}

	@Test
	public void z_04_search()
	{
		search(ADMIN, new ExperiencesFilter().withIncludeTags(CONFUSING_APPOINTMENT_PROCESS), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(POOR_HYGIENE), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(POOR_HYGIENE, FRIENDLY_STAFF), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(CONFUSING_APPOINTMENT_PROCESS, POOR_HYGIENE), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(OVERLY_CROWDED), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(SOCIAL_DISTANCING_ENFORCED, FRIENDLY_STAFF), 0L);
		search(ADMIN, new ExperiencesFilter().withIncludeTags(GOOD_HYGIENE, OVERLY_CROWDED, SOCIAL_DISTANCING_ENFORCED), 0L);
		assertThat(dao.search(new ExperiencesFilter()).records.get(0).tags).as("Check tags").isNull();
	}

	@Test
	public void z_09_clear()
	{
		Assertions.assertEquals(1, transRule.getSession().createQuery("DELETE FROM Experiences o").executeUpdate());
	}

	@Test
	public void z_10_add()
	{
		sessionDao.current(SESSION);

		VALUE = dao.add(createValid());
	}

	@Test
	public void z_10_add_dupe()
	{
		sessionDao.current(SESSION);

		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid())))
			.hasMessage("You have already provided an Experience for Test Center 0 today.");
	}

	public static Stream<Arguments> z_10_add_dupe_check()
	{
		var hourAgo = hourAgo();

		return Stream.of(
			arguments(new ExperiencesFilter(), 1L),
			arguments(new ExperiencesFilter().withPersonId(PERSON.id), 1L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY.id), 1L),
			arguments(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L),
			arguments(new ExperiencesFilter().withCreatedAtFrom(hourAgo), 1L),
			arguments(new ExperiencesFilter().withCreatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void z_10_add_dupe_check(final ExperiencesFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void z_10_change_created()
	{
		var o = dao.findWithException(VALUE.id);
		var date = days(o.getCreatedAt(), -1);
		o.setCreatedAt(date);
		o.setUpdatedAt(date);
	}

	public static Stream<Arguments> z_10_change_created_check()
	{
		var hourAgo = hourAgo();

		return Stream.of(
			arguments(new ExperiencesFilter(), 1L),
			arguments(new ExperiencesFilter().withPersonId(PERSON.id), 1L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY.id), 1L),
			arguments(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L),
			arguments(new ExperiencesFilter().withCreatedAtFrom(hourAgo), 0L),
			arguments(new ExperiencesFilter().withCreatedAtTo(hourAgo), 1L));
	}

	@ParameterizedTest
	@MethodSource
	public void z_10_change_created_check(final ExperiencesFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void z_10_dupe_next_day()
	{
		sessionDao.current(SESSION);

		dao.add(createValid());
	}

	public static Stream<Arguments> z_10_dupe_next_day_check()
	{
		var hourAgo = hourAgo();

		return Stream.of(
			arguments(new ExperiencesFilter(), 2L),
			arguments(new ExperiencesFilter().withPersonId(PERSON.id), 2L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY.id), 2L),
			arguments(new ExperiencesFilter().withPersonId(PERSON_1.id), 0L),
			arguments(new ExperiencesFilter().withFacilityId(FACILITY_1.id), 0L),
			arguments(new ExperiencesFilter().withCreatedAtFrom(hourAgo), 1L),
			arguments(new ExperiencesFilter().withCreatedAtTo(hourAgo), 1L));
	}

	@ParameterizedTest
	@MethodSource
	public void z_10_dupe_next_day_check(final ExperiencesFilter filter, final long expected)
	{
		count(filter, expected);
	}

	public static Stream<SessionValue> z_11_add() { return Stream.of(SESSION, SESSION_1); }

	@ParameterizedTest
	@MethodSource
	public void z_11_add(final SessionValue s)
	{
		sessionDao.current(s);

		for (long i = 2L; i <= 5L; i++)
			dao.add(new ExperiencesValue(FACILITY.id + i, false));
	}

	@Test
	public void z_11_add_fail()
	{
		sessionDao.current(SESSION);

		assertThat(assertThrows(ValidationException.class, () -> dao.add(new ExperiencesValue(FACILITY.id + 6L, false)))).hasMessage("You have already provided five Experiences today.");
	}

	@ParameterizedTest(name="facilityId={0}, total={1}, byFacility={2}")
	@CsvSource({"1,4,0", "2,4,0", "7,4,0", "8,4,0", "9,4,0", "10,4,0"})
	public void z_11_add_limit(final Long facilityId, final long total, final long byFacility)
	{
		sessionDao.current(SESSION_1);

		var o = dao.limit(facilityId);
		Assertions.assertEquals(total, o.total, "Check total");
		Assertions.assertEquals(byFacility, o.byFacility, "Check byFacility");
	}

	@ParameterizedTest(name="firstSession={0}, facilityId={1}")
	@CsvSource({"true,1", "true,2", "false,3", "true,3", "false,4", "true,4", "false,5", "true,5", "false,6", "true,6", "true,7", "true,8", "true,9", "true,10"})
	public void z_11_add_limit_fail(final boolean first, final Long facilityId)
	{
		sessionDao.current(first ? SESSION : SESSION_1);

		assertThrows(ValidationException.class, () -> dao.limit(facilityId));
	}

	@Test
	public void z_11_add_success()
	{
		sessionDao.current(SESSION_1);

		dao.add(new ExperiencesValue(FACILITY.id + 6L, false));
	}

	@ParameterizedTest(name="facilityId={0}")
	@CsvSource({"1", "2", "7", "8", "9", "10"})
	public void z_11_add_success_limit(final Long facilityId)
	{
		sessionDao.current(SESSION_1);

		assertThrows(ValidationException.class, () -> dao.limit(facilityId));
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
		Assertions.assertEquals(expected.updatedAt, record.getUpdatedAt(), assertId + "Check updatedAt");
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
		Assertions.assertEquals(expected.updatedAt, value.updatedAt, assertId + "Check updatedAt");
		Assertions.assertEquals(expected.tags, value.tags, assertId + "Check tags");
	}

	private void check(final ExperiencesCalcResponse response, final long positives, final long negatives, final Experience... tags)
	{
		Assertions.assertEquals(positives, response.positives, "Check positives");
		Assertions.assertEquals(negatives, response.negatives, "Check negatives");
		Assertions.assertEquals(positives + negatives, response.total, "Check total");

		var tagIds = Arrays.stream(tags).map(v -> v.id).collect(toSet());
		Experience.LIST.forEach(v -> {
			var t = response.tags.get(v.id);
			Assertions.assertEquals(tagIds.contains(v.id) ? 1L : 0L, t.count, "Check tags." + v.name + ":count");
			Assertions.assertEquals(tagIds.contains(v.id) ? VALUE.updatedAt : null, t.last, "Check tags." + v.name + ":last");
		});
	}
}
