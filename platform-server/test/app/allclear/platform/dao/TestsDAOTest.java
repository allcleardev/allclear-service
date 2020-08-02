package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;
import static app.allclear.platform.type.TestType.*;

import java.util.*;
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
import app.allclear.platform.entity.Tests;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.filter.TestsFilter;
import app.allclear.platform.value.*;

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
public class TestsDAOTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static TestsDAO dao = null;
	private static FacilityDAO facilityDao = null;
	private static PeopleDAO peopleDao = null;
	private static SessionDAO sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());
	private static TestsValue VALUE = null;
	private static TestsValue VALUE_1 = null;
	private static SessionValue ADMIN = null;
	private static SessionValue EDITOR = null;
	private static FacilityValue FACILITY = null;
	private static FacilityValue FACILITY_1 = null;
	private static PeopleValue PERSON = null;
	private static PeopleValue PERSON_1 = null;
	private static PeopleValue ASSOCIATE = null;
	private static PeopleValue ASSOCIATE_1 = null;
	private static Date RECEIVED_AT = utc(2020, 6, 14);
	private static Date RECEIVED_AT_1 = utc(2020, 7, 14);
	private static Date TAKEN_ON = utc(2020, 4, 4);
	private static Date TAKEN_ON_1 = utc(2020, 3, 4);

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new TestsDAO(factory, sessionDao);
		facilityDao = new FacilityDAO(factory, new TestAuditor());
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
		EDITOR = sessionDao.add(new AdminValue("editor", false, true), false);
		sessionDao.current(ADMIN = sessionDao.add(new AdminValue("admin"), false));
		FACILITY = facilityDao.add(FacilityDAOTest.createValid(), true);
		FACILITY_1 = facilityDao.add(FacilityDAOTest.createValid().withName("second"), true);
		PERSON = peopleDao.add(PeopleDAOTest.createValid());
		PERSON_1 = peopleDao.add(PeopleDAOTest.createValid().withName("second").withPhone("+18885552000").withEmail(null));
		ASSOCIATE = peopleDao.add(new PeopleValue(10).withAssociations(FACILITY));
		ASSOCIATE_1 = peopleDao.add(new PeopleValue(11).withAssociations(FACILITY_1));

		var value = dao.add(VALUE = new TestsValue(PERSON.id, NASAL_SWAB.id, TAKEN_ON, FACILITY.id, false, "All the other details").withRemoteId("remote-1"));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Creates a valid Tests value for the validation tests.
	 *	@return never NULL.
	*/
	private TestsValue createValid()
	{
		return new TestsValue(PERSON_1.id, ANTIBODY.id, TAKEN_ON_1, FACILITY_1.id, true, "Some more information").withRemoteId("local_2").withReceivedAt(RECEIVED_AT_1);
	}

	@Test
	public void add_as_editor()
	{
		sessionDao.current(EDITOR);
		assertThrows(NotAuthorizedException.class, () -> dao.add(createValid()));
	}

	@Test
	public void add_missingPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId(null)));
	}

	@Test
	public void add_longPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId(StringUtils.repeat("A", TestsValue.MAX_LEN_PERSON_ID + 1))));
	}

	@Test
	public void add_invalidPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId("INVALID")));
	}

	@Test
	public void add_invalidTypeId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withTypeId("??")));
	}

	@Test
	public void add_missingTypeId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withTypeId(null)));
	}

	@Test
	public void add_missingTakenOn()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withTakenOn(null)));
	}

	@Test
	public void add_missingFacilityId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(null)));
	}

	@Test
	public void add_invalidFacilityId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(VALUE.id + 1000L)));
	}

	@Test
	public void add_dupeRemoteId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(FACILITY.id).withRemoteId("remote-1")));
	}

	@Test
	public void add_longRemoteId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withRemoteId(StringUtils.repeat("A", TestsValue.MAX_LEN_REMOTE_ID + 1))));
	}

	@Test
	public void add_longNotes()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withNotes(StringUtils.repeat("A", TestsValue.MAX_LEN_NOTES + 1))));
	}

	public static Stream<SessionValue> check_fail()
	{
		return Stream.of(EDITOR, new SessionValue(false, PERSON_1), new SessionValue(false, ASSOCIATE_1));
	}

	@ParameterizedTest
	@MethodSource
	public void check_fail(final SessionValue session)
	{
		sessionDao.current(session);

		assertThrows(NotAuthorizedException.class, () -> dao.check(dao.findWithException(VALUE.id)));
	}

	public static Stream<SessionValue> check_success()
	{
		return Stream.of(ADMIN, new SessionValue(false, PERSON), new SessionValue(false, ASSOCIATE));
	}

	@ParameterizedTest
	@MethodSource
	public void check_success(final SessionValue session)
	{
		sessionDao.current(session);

		Assertions.assertNotNull(dao.check(dao.findWithException(VALUE.id)));
	}

	public static Stream<Arguments> count()
	{
		return Stream.of(
			arguments(new PeopleFilter("createdAt", "ASC"), List.of(PERSON, PERSON_1, ASSOCIATE, ASSOCIATE_1)),
			arguments(new PeopleFilter("createdAt", "ASC").withHasTakenTest(true), List.of(PERSON)),
			arguments(new PeopleFilter("createdAt", "ASC").withHasTakenTest(false), List.of(PERSON_1, ASSOCIATE, ASSOCIATE_1)),
			arguments(new PeopleFilter("createdAt", "ASC").withHasPositiveTest(true), null),
			arguments(new PeopleFilter("createdAt", "ASC").withHasPositiveTest(false), List.of(PERSON, PERSON_1, ASSOCIATE, ASSOCIATE_1)));
	}

	@ParameterizedTest
	@MethodSource
	public void count(final PeopleFilter filter, final List<PeopleValue> records)
	{
		var results = peopleDao.search(filter);
		Assertions.assertEquals(null != records ? (long) records.size() : 0L, results.total);
		assertThat(results.records).isEqualTo(records);
	}

	@Test
	public void find()
	{
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals("remote-1", record.getRemoteId(), "Check remoteId");
		check(VALUE, record);
	}

	@Test
	public void find_as_editor()
	{
		sessionDao.current(EDITOR);
		assertThrows(NotAuthorizedException.class, () -> dao.findWithException(VALUE.id));
	}

	@Test
	public void find_as_person()
	{
		sessionDao.current(PERSON);
		check(VALUE, dao.findWithException(VALUE.id));
	}

	@Test
	public void find_as_person1()
	{
		sessionDao.current(PERSON_1);
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
		Assertions.assertEquals("remote-1", value.remoteId, "Check remoteId");
		check(VALUE, value);
	}

	@Test
	public void getWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException(VALUE.id + 1000L));
	}

	@Test
	public void getByPerson_by_admin()
	{
		assertThat(dao.getByPerson(PERSON.id)).containsExactly(VALUE);
		assertThat(dao.getByPerson(PERSON_1.id)).isEmpty();
	}

	@Test
	public void getByPerson_by_editor()
	{
		sessionDao.current(EDITOR);
		assertThrows(NotAuthorizedException.class, () -> dao.getByPerson(PERSON.id));
		assertThrows(NotAuthorizedException.class, () -> dao.getByPerson(PERSON_1.id));
	}

	@Test
	public void getByPerson_by_person()
	{
		sessionDao.current(PERSON);
		assertThat(dao.getByPerson(PERSON.id)).containsExactly(VALUE);
		assertThat(dao.getByPerson(PERSON_1.id)).containsExactly(VALUE);
	}

	@Test
	public void getByPerson_by_person1()
	{
		sessionDao.current(PERSON_1);
		assertThat(dao.getByPerson(PERSON.id)).isEmpty();
		assertThat(dao.getByPerson(PERSON_1.id)).isEmpty();
	}

	@Test
	public void modify()
	{
		var v = createValid();
		count(new TestsFilter().withPersonId(VALUE.personId), 1L);
		count(new TestsFilter().withTypeId(VALUE.typeId), 1L);
		count(new TestsFilter().withTakenOn(VALUE.takenOn), 1L);
		count(new TestsFilter().withFacilityId(VALUE.facilityId), 1L);
		count(new TestsFilter().withRemoteId("remote-1"), 1L);
		count(new TestsFilter().withPositive(VALUE.positive), 1L);
		count(new TestsFilter().withNotes(VALUE.notes), 1L);
		count(new TestsFilter().withHasReceivedAt(false), 1L);
		count(new TestsFilter().withPersonId(v.personId), 0L);
		count(new TestsFilter().withTypeId(v.typeId), 0L);
		count(new TestsFilter().withTakenOn(v.takenOn), 0L);
		count(new TestsFilter().withFacilityId(v.facilityId), 0L);
		count(new TestsFilter().withRemoteId("local_2"), 0L);
		count(new TestsFilter().withPositive(v.positive), 0L);
		count(new TestsFilter().withNotes(v.notes), 0L);
		count(new TestsFilter().withHasReceivedAt(true), 0L);

		var value = dao.update(VALUE_1 = v.withId(VALUE.id));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE_1, value);
	}

	@Test
	public void modify_as_editor()
	{
		sessionDao.current(EDITOR);
		assertThrows(NotAuthorizedException.class, () -> dao.update(VALUE));	// Try to revert, but will fail.
	}

	@Test
	public void modify_count()
	{
		var v = createValid();
		count(new TestsFilter().withPersonId(VALUE.personId), 0L);
		count(new TestsFilter().withTypeId(VALUE.typeId), 0L);
		count(new TestsFilter().withTakenOn(VALUE.takenOn), 0L);
		count(new TestsFilter().withFacilityId(VALUE.facilityId), 0L);
		count(new TestsFilter().withRemoteId("remote-1"), 0L);
		count(new TestsFilter().withPositive(VALUE.positive), 0L);
		count(new TestsFilter().withNotes(VALUE.notes), 0L);
		count(new TestsFilter().withHasReceivedAt(false), 0L);
		count(new TestsFilter().withReceivedAtFrom(hours(RECEIVED_AT, -1)).withReceivedAtTo(hours(RECEIVED_AT, 1)), 0L);
		count(new TestsFilter().withPersonId(v.personId), 1L);
		count(new TestsFilter().withTypeId(v.typeId), 1L);
		count(new TestsFilter().withTakenOn(v.takenOn), 1L);
		count(new TestsFilter().withFacilityId(v.facilityId), 1L);
		count(new TestsFilter().withRemoteId("local_2"), 1L);
		count(new TestsFilter().withPositive(v.positive), 1L);
		count(new TestsFilter().withNotes(v.notes), 1L);
		count(new TestsFilter().withHasReceivedAt(true), 1L);
		count(new TestsFilter().withReceivedAtFrom(hours(RECEIVED_AT_1, -1)).withReceivedAtTo(hours(RECEIVED_AT_1, 1)), 1L);

		VALUE = VALUE_1;
	}

	@Test
	public void modify_find()
	{
		var v = createValid();
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(v.personId, record.getPersonId(), "Check personId");
		Assertions.assertEquals(v.typeId, record.getTypeId(), "Check typeId");
		Assertions.assertEquals(v.takenOn, record.getTakenOn(), "Check takenOn");
		Assertions.assertEquals(v.facilityId, record.getFacilityId(), "Check facilityId");
		Assertions.assertEquals("local_2", record.getRemoteId(), "Check remoteId");
		Assertions.assertEquals(v.positive, record.isPositive(), "Check positive");
		Assertions.assertEquals(v.notes, record.getNotes(), "Check notes");
		Assertions.assertEquals(RECEIVED_AT_1, record.getReceivedAt(), "Check receivedAt");
		check(VALUE, record);
	}

	public static Stream<Arguments> modify_search()
	{
		return Stream.of(
			arguments(new PeopleFilter("createdAt", "ASC"), List.of(PERSON, PERSON_1, ASSOCIATE, ASSOCIATE_1)),
			arguments(new PeopleFilter("createdAt", "ASC").withHasTakenTest(true), List.of(PERSON_1)),
			arguments(new PeopleFilter("createdAt", "ASC").withHasTakenTest(false), List.of(PERSON, ASSOCIATE, ASSOCIATE_1)),
			arguments(new PeopleFilter("createdAt", "ASC").withHasPositiveTest(true), List.of(PERSON_1)),
			arguments(new PeopleFilter("createdAt", "ASC").withHasPositiveTest(false), List.of(PERSON, ASSOCIATE, ASSOCIATE_1)));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_search(final PeopleFilter filter, final List<PeopleValue> records)
	{
		var results = peopleDao.search(filter);
		Assertions.assertEquals(null != records ? (long) records.size() : 0L, results.total);
		assertThat(results.records).isEqualTo(records);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();
		var hourAgoTaken = hours(VALUE.takenOn, -1);
		var hourAheadTaken = hours(VALUE.takenOn, 1);
		var hourAgoReceived = hours(VALUE.receivedAt, -1);
		var hourAheadReceived = hours(VALUE.receivedAt, 1);

		return Stream.of(
			arguments(new TestsFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new TestsFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(new TestsFilter(1, 20).withTypeId(VALUE.typeId), 1L),
			arguments(new TestsFilter(1, 20).withTakenOn(VALUE.takenOn), 1L),
			arguments(new TestsFilter(1, 20).withTakenOnFrom(hourAgoTaken), 1L),
			arguments(new TestsFilter(1, 20).withTakenOnTo(hourAheadTaken), 1L),
			arguments(new TestsFilter(1, 20).withTakenOnFrom(hourAgoTaken).withTakenOnTo(hourAheadTaken), 1L),
			arguments(new TestsFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(new TestsFilter(1, 20).withRemoteId("local_2"), 1L),
			arguments(new TestsFilter(1, 20).withPositive(VALUE.positive), 1L),
			arguments(new TestsFilter(1, 20).withNotes(VALUE.notes), 1L),
			arguments(new TestsFilter(1, 20).withHasNotes(true), 1L),
			arguments(new TestsFilter(1, 20).withHasReceivedAt(true), 1L),
			arguments(new TestsFilter(1, 20).withReceivedAtFrom(hourAgoReceived), 1L),
			arguments(new TestsFilter(1, 20).withReceivedAtTo(hourAheadReceived), 1L),
			arguments(new TestsFilter(1, 20).withReceivedAtFrom(hourAgoReceived).withReceivedAtTo(hourAheadReceived), 1L),
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
			arguments(new TestsFilter(1, 20).withRemoteId("remote-1"), 0L),
			arguments(new TestsFilter(1, 20).withPositive(!VALUE.positive), 0L),
			arguments(new TestsFilter(1, 20).withNotes("invalid"), 0L),
			arguments(new TestsFilter(1, 20).withHasNotes(false), 0L),
			arguments(new TestsFilter(1, 20).withHasReceivedAt(false), 0L),
			arguments(new TestsFilter(1, 20).withReceivedAtFrom(hourAheadReceived), 0L),
			arguments(new TestsFilter(1, 20).withReceivedAtTo(hourAgoReceived), 0L),
			arguments(new TestsFilter(1, 20).withReceivedAtFrom(hourAheadReceived).withReceivedAtTo(hourAgoReceived), 0L),
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

	@Test
	public void search_as_editor()
	{
		sessionDao.current(EDITOR);
		assertThrows(NotAuthorizedException.class, () -> dao.search(new TestsFilter()));
	}

	@Test
	public void search_as_person()
	{
		sessionDao.current(PERSON);
		search(new TestsFilter(), 0L);
	}

	@Test
	public void search_as_person1()
	{
		sessionDao.current(PERSON_1);
		search(new TestsFilter(), 1L);
	}

	public static Stream<Arguments> search_sort()
	{
		return Stream.of(
			arguments(new TestsFilter(null, null), "id", "DESC"), // Default sort
			arguments(new TestsFilter(null, "invalid"), "id", "DESC"),
			arguments(new TestsFilter(null, "asc"), "id", "DESC"),
			arguments(new TestsFilter("invalid", "invalid"), "id", "DESC"),
			arguments(new TestsFilter("invalid", null), "id", "DESC"),
			arguments(new TestsFilter("invalid", "desc"), "id", "DESC"),

			arguments(new TestsFilter("id", null), "id", "DESC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("id", "ASC"), "id", "ASC"),
			arguments(new TestsFilter("id", "asc"), "id", "ASC"),
			arguments(new TestsFilter("id", "invalid"), "id", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("id", "DESC"), "id", "DESC"),
			arguments(new TestsFilter("id", "desc"), "id", "DESC"),

			arguments(new TestsFilter("personId", null), "personId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("personId", "ASC"), "personId", "ASC"),
			arguments(new TestsFilter("personId", "asc"), "personId", "ASC"),
			arguments(new TestsFilter("personId", "invalid"), "personId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("personId", "DESC"), "personId", "DESC"),
			arguments(new TestsFilter("personId", "desc"), "personId", "DESC"),

			arguments(new TestsFilter("personName", null), "personName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("personName", "ASC"), "personName", "ASC"),
			arguments(new TestsFilter("personName", "asc"), "personName", "ASC"),
			arguments(new TestsFilter("personName", "invalid"), "personName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("personName", "DESC"), "personName", "DESC"),
			arguments(new TestsFilter("personName", "desc"), "personName", "DESC"),

			arguments(new TestsFilter("typeId", null), "typeId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("typeId", "ASC"), "typeId", "ASC"),
			arguments(new TestsFilter("typeId", "asc"), "typeId", "ASC"),
			arguments(new TestsFilter("typeId", "invalid"), "typeId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("typeId", "DESC"), "typeId", "DESC"),
			arguments(new TestsFilter("typeId", "desc"), "typeId", "DESC"),

			arguments(new TestsFilter("takenOn", null), "takenOn", "DESC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("takenOn", "ASC"), "takenOn", "ASC"),
			arguments(new TestsFilter("takenOn", "asc"), "takenOn", "ASC"),
			arguments(new TestsFilter("takenOn", "invalid"), "takenOn", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("takenOn", "DESC"), "takenOn", "DESC"),
			arguments(new TestsFilter("takenOn", "desc"), "takenOn", "DESC"),

			arguments(new TestsFilter("facilityId", null), "facilityId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("facilityId", "ASC"), "facilityId", "ASC"),
			arguments(new TestsFilter("facilityId", "asc"), "facilityId", "ASC"),
			arguments(new TestsFilter("facilityId", "invalid"), "facilityId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("facilityId", "DESC"), "facilityId", "DESC"),
			arguments(new TestsFilter("facilityId", "desc"), "facilityId", "DESC"),

			arguments(new TestsFilter("facilityName", null), "facilityName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("facilityName", "ASC"), "facilityName", "ASC"),
			arguments(new TestsFilter("facilityName", "asc"), "facilityName", "ASC"),
			arguments(new TestsFilter("facilityName", "invalid"), "facilityName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("facilityName", "DESC"), "facilityName", "DESC"),
			arguments(new TestsFilter("facilityName", "desc"), "facilityName", "DESC"),

			arguments(new TestsFilter("remoteId", null), "remoteId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("remoteId", "ASC"), "remoteId", "ASC"),
			arguments(new TestsFilter("remoteId", "asc"), "remoteId", "ASC"),
			arguments(new TestsFilter("remoteId", "invalid"), "remoteId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("remoteId", "DESC"), "remoteId", "DESC"),
			arguments(new TestsFilter("remoteId", "desc"), "remoteId", "DESC"),

			arguments(new TestsFilter("positive", null), "positive", "DESC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("positive", "ASC"), "positive", "ASC"),
			arguments(new TestsFilter("positive", "asc"), "positive", "ASC"),
			arguments(new TestsFilter("positive", "invalid"), "positive", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("positive", "DESC"), "positive", "DESC"),
			arguments(new TestsFilter("positive", "desc"), "positive", "DESC"),

			arguments(new TestsFilter("notes", null), "notes", "ASC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("notes", "ASC"), "notes", "ASC"),
			arguments(new TestsFilter("notes", "asc"), "notes", "ASC"),
			arguments(new TestsFilter("notes", "invalid"), "notes", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("notes", "DESC"), "notes", "DESC"),
			arguments(new TestsFilter("notes", "desc"), "notes", "DESC"),

			arguments(new TestsFilter("receivedAt", null), "receivedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("receivedAt", "ASC"), "receivedAt", "ASC"),
			arguments(new TestsFilter("receivedAt", "asc"), "receivedAt", "ASC"),
			arguments(new TestsFilter("receivedAt", "invalid"), "receivedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("receivedAt", "DESC"), "receivedAt", "DESC"),
			arguments(new TestsFilter("receivedAt", "desc"), "receivedAt", "DESC"),

			arguments(new TestsFilter("createdAt", null), "createdAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("createdAt", "ASC"), "createdAt", "ASC"),
			arguments(new TestsFilter("createdAt", "asc"), "createdAt", "ASC"),
			arguments(new TestsFilter("createdAt", "invalid"), "createdAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("createdAt", "DESC"), "createdAt", "DESC"),
			arguments(new TestsFilter("createdAt", "desc"), "createdAt", "DESC"),

			arguments(new TestsFilter("updatedAt", null), "updatedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new TestsFilter("updatedAt", "ASC"), "updatedAt", "ASC"),
			arguments(new TestsFilter("updatedAt", "asc"), "updatedAt", "ASC"),
			arguments(new TestsFilter("updatedAt", "invalid"), "updatedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new TestsFilter("updatedAt", "DESC"), "updatedAt", "DESC"),
			arguments(new TestsFilter("updatedAt", "desc"), "updatedAt", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	public void search_sort(final TestsFilter filter, final String expectedSortOn, final String expectedSortDir)
	{
		var results = dao.search(filter);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedSortOn, results.sortOn, "Check sortOn");
		Assertions.assertEquals(expectedSortDir, results.sortDir, "Check sortDir");
	}

	@Test
	public void set_as_person()
	{
		sessionDao.current(PERSON);
		assertThrows(NotAuthorizedException.class, () -> dao.update(VALUE));
	}

	@Test
	public void set_as_person1()
	{
		Assertions.assertTrue(dao.getById(VALUE.id).positive);

		sessionDao.current(PERSON_1);
		dao.update(VALUE.withPositive(false));
	}

	@Test
	public void set_as_person1_check()
	{
		Assertions.assertFalse(dao.getById(VALUE.id).positive);
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
		var v = createValid();
		count(new TestsFilter().withId(VALUE.id), 0L);
		count(new TestsFilter().withPersonId(v.personId), 0L);
		count(new TestsFilter().withTypeId(v.typeId), 0L);
		count(new TestsFilter().withTakenOn(v.takenOn), 0L);
		count(new TestsFilter().withFacilityId(v.facilityId), 0L);
		count(new TestsFilter().withRemoteId("local_2"), 0L);
		count(new TestsFilter().withPositive(v.positive), 0L);
		count(new TestsFilter().withNotes(v.notes), 0L);
	}

	@Test
	public void z_00_add()
	{
		sessionDao.current(PERSON);

		Assertions.assertNotNull(VALUE = dao.add(createValid()));
	}

	@Test
	public void z_00_get()
	{
		var v = dao.getById(VALUE.id);
		Assertions.assertEquals(PERSON.id, v.personId, "Check personId");
		Assertions.assertEquals(RECEIVED_AT_1, v.receivedAt, "Check receivedAt");	// People can set receivedAt on add.
	}

	@Test
	public void z_00_get_as_person1()
	{
		sessionDao.current(PERSON_1);

		assertThrows(NotAuthorizedException.class, () -> dao.getById(VALUE.id));
	}

	@Test
	public void z_00_modify_00()
	{
		sessionDao.current(PERSON);

		var v = dao.update(VALUE.withReceivedAt(RECEIVED_AT));
		Assertions.assertEquals(RECEIVED_AT_1, v.receivedAt, "Check receivedAt");	// People cannot set receivedAt on update.
	}

	@Test
	public void z_00_modify_00_get()
	{
		var v = dao.getById(VALUE.id);
		Assertions.assertEquals(RECEIVED_AT_1, v.receivedAt, "Check receivedAt");	// People cannot set receivedAt on update.
	}

	@Test
	public void z_00_modify_01()
	{
		sessionDao.current(EDITOR);

		assertThrows(NotAuthorizedException.class, () -> dao.update(VALUE.withReceivedAt(RECEIVED_AT)));
	}

	@Test
	public void z_00_modify_01_get()
	{
		var v = dao.getById(VALUE.id);
		Assertions.assertEquals(RECEIVED_AT_1, v.receivedAt, "Check receivedAt");	// Editors cannot set receivedAt on update.
	}

	@Test
	public void z_00_modify_02()
	{
		var v = dao.update(VALUE.withReceivedAt(RECEIVED_AT));
		Assertions.assertEquals(RECEIVED_AT, v.receivedAt, "Check receivedAt");	// Admins can set receivedAt on update.
	}

	@Test
	public void z_00_modify_02_get()
	{
		var v = dao.getById(VALUE.id);
		Assertions.assertEquals(RECEIVED_AT, v.receivedAt, "Check receivedAt");	// Admins can set receivedAt on update.
	}

	@Test
	public void z_00_modify_03()
	{
		var v = dao.update(VALUE.withReceivedAt(null));
		Assertions.assertNull(v.receivedAt, "Check receivedAt");	// Admins can set receivedAt on update.
	}

	@Test
	public void z_00_modify_03_get()
	{
		var v = dao.getById(VALUE.id);
		Assertions.assertNull(v.receivedAt, "Check receivedAt");	// Admins can set receivedAt on update.
	}

	@Test
	public void z_00_remove_noAuth()
	{
		sessionDao.current(PERSON_1);

		assertThrows(NotAuthorizedException.class, () -> dao.remove(VALUE.id));
	}

	@Test
	public void z_00_remove_noAuth_check()
	{
		count(new TestsFilter(), 1L);
		Assertions.assertNotNull(dao.getById(VALUE.id));
	}

	@Test
	public void z_00_remove_withAuth()
	{
		sessionDao.current(PERSON);

		Assertions.assertTrue(dao.remove(VALUE.id));
	}

	@Test
	public void z_00_remove_withAuth_check()
	{
		count(new TestsFilter(), 0L);
		Assertions.assertNull(dao.getById(VALUE.id));
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final TestsFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied entity record. */
	private void check(final TestsValue expected, final Tests record)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, record.getId(), assertId + "Check id");
		Assertions.assertEquals(expected.personId, record.getPersonId(), assertId + "Check personId");
		Assertions.assertEquals(expected.typeId, record.getTypeId(), assertId + "Check typeId");
		Assertions.assertEquals(expected.takenOn, record.getTakenOn(), assertId + "Check takenOn");
		Assertions.assertEquals(expected.facilityId, record.getFacilityId(), assertId + "Check facilityId");
		Assertions.assertEquals(expected.remoteId, record.getRemoteId(), assertId + "Check remoteId");
		Assertions.assertEquals(expected.positive, record.isPositive(), assertId + "Check positive");
		Assertions.assertEquals(expected.notes, record.getNotes(), assertId + "Check notes");
		Assertions.assertEquals(expected.receivedAt, record.getReceivedAt(), assertId + "Check receivedAt");
		Assertions.assertEquals(expected.createdAt, record.getCreatedAt(), assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, record.getUpdatedAt(), assertId + "Check updatedAt");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final TestsValue expected, final TestsValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.personId, value.personId, assertId + "Check personId");
		Assertions.assertEquals(expected.personName, value.personName, assertId + "Check personName");
		Assertions.assertEquals(expected.typeId, value.typeId, assertId + "Check typeId");
		Assertions.assertEquals(expected.type, value.type, assertId + "Check type");
		Assertions.assertEquals(expected.takenOn, value.takenOn, assertId + "Check takenOn");
		Assertions.assertEquals(expected.facilityId, value.facilityId, assertId + "Check facilityId");
		Assertions.assertEquals(expected.facilityName, value.facilityName, assertId + "Check facilityName");
		Assertions.assertEquals(expected.remoteId, value.remoteId, assertId + "Check remoteId");
		Assertions.assertEquals(expected.positive, value.positive, assertId + "Check positive");
		Assertions.assertEquals(expected.notes, value.notes, assertId + "Check notes");
		Assertions.assertEquals(expected.receivedAt, value.receivedAt, assertId + "Check receivedAt");
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, value.updatedAt, assertId + "Check updatedAt");
	}
}
