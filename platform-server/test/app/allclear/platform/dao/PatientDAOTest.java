package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.NotAuthorizedException;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.entity.Patient;
import app.allclear.platform.filter.PatientFilter;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Patient entity.
*
*	@author smalleyd
*	@version 1.1.111
*	@since July 18, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class PatientDAOTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static PatientDAO dao = null;
	private static FacilityDAO facilityDao = null;
	private static PeopleDAO peopleDao = null;
	private static SessionDAO sessionDao = null;
	private static PatientValue VALUE = null;
	private static FacilityValue FACILITY = null;
	private static FacilityValue FACILITY_1 = null;
	private static final PeopleValue PATIENT = new PeopleValue(0);
	private static final PeopleValue PATIENT_1 = new PeopleValue(1);
	private static final PeopleValue ASSOCIATE = new PeopleValue(10);
	private static final PeopleValue ASSOCIATE_1 = new PeopleValue(11);
	private static final Date ENROLLED_AT = utc(2020, 6, 1);
	private static final Date ENROLLED_AT_1 = utc(2020, 7, 1);
	private static final Date REJECTED_AT = utc(2020, 6, 15);
	private static final Date REJECTED_AT_1 = utc(2020, 7, 15);
	private static final SessionValue ADMIN = new SessionValue(false, new AdminValue("admin"));
	private static final SessionValue EDITOR = new SessionValue(false, new AdminValue("editor", false, true));
	private static final SessionValue PATIENT_ = new SessionValue(false, PATIENT);
	private static final SessionValue PATIENT_1_ = new SessionValue(false, PATIENT_1);
	private static final SessionValue ASSOCIATE_ = new SessionValue(false, ASSOCIATE);
	private static final SessionValue ASSOCIATE_1_ = new SessionValue(false, ASSOCIATE_1);

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());
		dao = new PatientDAO(factory, sessionDao);
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
		peopleDao.add(PATIENT);
		peopleDao.add(PATIENT_1);
		peopleDao.add(ASSOCIATE.withAssociations(FACILITY), true);
		peopleDao.add(ASSOCIATE_1.withAssociations(FACILITY_1), true);

		var value = dao.add(VALUE = new PatientValue(FACILITY.id, PATIENT_1.id, false, ENROLLED_AT, null));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Creates a valid Patient value for the validation tests.
	 *	@return never NULL.
	*/
	private PatientValue createValid()
	{
		return new PatientValue(FACILITY_1.id, PATIENT.id, true, null, REJECTED_AT);
	}

	@Test
	public void add_dupe()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(FACILITY.id).withPersonId(PATIENT_1.id)));
	}

	@Test
	public void add_missingFacilityId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(null)));
	}

	@Test
	public void add_invalidFacilityId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFacilityId(FACILITY.id + 1000L)));
	}

	@Test
	public void add_missingPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId(null)));
	}

	@Test
	public void add_longPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId(StringUtils.repeat("A", PatientValue.MAX_LEN_PERSON_ID + 1))));
	}

	@Test
	public void add_invalidPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId("INVALID")));
	}

	@Test
	public void add_withEnrolledAtAndRejectedAt()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withEnrolledAt(new Date()).withRejectedAt(new Date())));
	}

	public static Stream<Arguments> check()
	{
		return Stream.of(
			arguments(ADMIN, false, false, true),
			arguments(ADMIN, true, false, true),
			arguments(ADMIN, false, true, true),
			arguments(ADMIN, true, true, true),
			arguments(EDITOR, false, false, false),
			arguments(EDITOR, true, false, false),
			arguments(EDITOR, false, true, false),
			arguments(EDITOR, true, true, false),
			arguments(PATIENT_, false, false, false),
			arguments(PATIENT_, true, false, false),
			arguments(PATIENT_, false, true, false),
			arguments(PATIENT_, true, true, false),
			arguments(PATIENT_1_, false, false, true),
			arguments(PATIENT_1_, true, false, true),
			arguments(PATIENT_1_, false, true, true),
			arguments(PATIENT_1_, true, true, true),
			arguments(ASSOCIATE_, false, false, true),
			arguments(ASSOCIATE_, true, false, false),
			arguments(ASSOCIATE_, false, true, true),
			arguments(ASSOCIATE_, true, true, false),
			arguments(ASSOCIATE_1_, false, false, false),
			arguments(ASSOCIATE_1_, true, false, false),
			arguments(ASSOCIATE_1_, false, true, false),
			arguments(ASSOCIATE_1_, true, true, false));
	}

	@ParameterizedTest
	@MethodSource
	public void check(final SessionValue session, final boolean update, final boolean removal, final boolean success)
	{
		sessionDao.current(session);

		var record = dao.find(VALUE.id);
		if (success)
			check(VALUE, dao.check(record, update, removal));
		else
			assertThrows(NotAuthorizedException.class, () -> dao.check(record, update, removal));
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
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.id + 1000L));
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
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException(VALUE.id + 1000L));

		System.out.println("ASSOCIATE: " + ASSOCIATE);
		System.out.println("ASSOCIATED: " + facilityDao.getById(FACILITY.id, true));
	}

	public static Stream<Arguments> getEnrolledByFacilityAndName()
	{
		PATIENT_1.withHealthWorkerStatusId(null).withHealthWorkerStatus(null);	// These fields are NOT returned from the getEnrolledByFacilityAndName call.

		return Stream.of(
			arguments(ADMIN, FACILITY.id, "0", false, false),
			arguments(ADMIN, FACILITY.id, "1", true, false),
			arguments(ADMIN, FACILITY_1.id, "0", false, false),
			arguments(ADMIN, FACILITY_1.id, "1", false, false),
			arguments(EDITOR, FACILITY.id, "0", false, true),
			arguments(EDITOR, FACILITY.id, "1", false, true),
			arguments(EDITOR, FACILITY_1.id, "0", false, true),
			arguments(EDITOR, FACILITY_1.id, "1", false, true),
			arguments(PATIENT_, FACILITY.id, "0", false, true),
			arguments(PATIENT_, FACILITY.id, "1", false, true),
			arguments(PATIENT_, FACILITY_1.id, "0", false, true),
			arguments(PATIENT_, FACILITY_1.id, "1", false, true),
			arguments(PATIENT_1_, FACILITY.id, "0", false, true),
			arguments(PATIENT_1_, FACILITY.id, "1", false, true),
			arguments(PATIENT_1_, FACILITY_1.id, "0", false, true),
			arguments(PATIENT_1_, FACILITY_1.id, "1", false, true),
			arguments(ASSOCIATE_, FACILITY.id, "0", false, false),
			arguments(ASSOCIATE_, FACILITY.id, "1", true, false),
			arguments(ASSOCIATE_, FACILITY_1.id, "0", false, false),
			arguments(ASSOCIATE_, FACILITY_1.id, "1", true, false),	// Ignores the supplied Facility ID.
			arguments(ASSOCIATE_1_, FACILITY.id, "0", false, false),
			arguments(ASSOCIATE_1_, FACILITY.id, "1", false, false),
			arguments(ASSOCIATE_1_, FACILITY_1.id, "0", false, false),
			arguments(ASSOCIATE_1_, FACILITY_1.id, "1", false, false));
	}

	@ParameterizedTest
	@MethodSource
	public void getEnrolledByFacilityAndName(final SessionValue session, final Long facilityId, final String name, final boolean success, final boolean error)
	{
		sessionDao.current(session);

		if (error)
			assertThrows(NotAuthorizedException.class, () -> dao.getEnrolledByFacilityAndName(facilityId, name));
		else
		{
			var values = dao.getEnrolledByFacilityAndName(facilityId, name);
			if (success)
				assertThat(values).containsExactly(PATIENT_1);
			else
				assertThat(values).isEmpty();
		}
	}

	@Test
	public void modify()
	{
		count(new PatientFilter().withFacilityId(FACILITY.id), 1L);
		count(new PatientFilter().withPersonId(PATIENT_1.id), 1L);
		count(new PatientFilter().withAlertable(false), 1L);
		count(new PatientFilter().withHasEnrolledAt(true), 1L);
		count(new PatientFilter().withEnrolledAtFrom(hourAgo(ENROLLED_AT)).withEnrolledAtTo(hourAhead(ENROLLED_AT)), 1L);
		count(new PatientFilter().withHasRejectedAt(false), 1L);
		count(new PatientFilter().withFacilityId(FACILITY_1.id), 0L);
		count(new PatientFilter().withPersonId(PATIENT.id), 0L);
		count(new PatientFilter().withAlertable(true), 0L);
		count(new PatientFilter().withHasEnrolledAt(false), 0L);
		count(new PatientFilter().withEnrolledAtFrom(hourAhead(ENROLLED_AT)).withEnrolledAtTo(hourAgo(ENROLLED_AT)), 0L);
		count(new PatientFilter().withHasRejectedAt(true), 0L);
		count(new PatientFilter().withRejectedAtFrom(hourAgo(REJECTED_AT_1)).withRejectedAtTo(hourAhead(REJECTED_AT_1)), 0L);

		var value = dao.update(VALUE.withFacilityId(FACILITY_1.id).withPersonId(PATIENT.id).withAlertable(true).withEnrolledAt(null).withRejectedAt(REJECTED_AT_1));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	public static Stream<Arguments> modify_check()
	{
		return Stream.of(
			arguments(ADMIN, false, false, true),
			arguments(ADMIN, true, false, true),
			arguments(ADMIN, false, true, true),
			arguments(ADMIN, true, true, true),
			arguments(EDITOR, false, false, false),
			arguments(EDITOR, true, false, false),
			arguments(EDITOR, false, true, false),
			arguments(EDITOR, true, true, false),
			arguments(PATIENT_, false, false, true),
			arguments(PATIENT_, true, false, true),
			arguments(PATIENT_, false, true, true),
			arguments(PATIENT_, true, true, true),
			arguments(PATIENT_1_, false, false, false),
			arguments(PATIENT_1_, true, false, false),
			arguments(PATIENT_1_, false, true, false),
			arguments(PATIENT_1_, true, true, false),
			arguments(ASSOCIATE_, false, false, false),
			arguments(ASSOCIATE_, true, false, false),
			arguments(ASSOCIATE_, false, true, false),
			arguments(ASSOCIATE_, true, true, false),
			arguments(ASSOCIATE_1_, false, false, true),
			arguments(ASSOCIATE_1_, true, false, false),
			arguments(ASSOCIATE_1_, false, true, true),
			arguments(ASSOCIATE_1_, true, true, false));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_check(final SessionValue session, final boolean update, final boolean removal, final boolean success)
	{
		sessionDao.current(session);

		var record = dao.find(VALUE.id);
		if (success)
			check(VALUE, dao.check(record, update, removal));
		else
			assertThrows(NotAuthorizedException.class, () -> dao.check(record, update, removal));
	}

	@Test
	public void modify_count()
	{
		count(new PatientFilter().withFacilityId(FACILITY.id), 0L);
		count(new PatientFilter().withPersonId(PATIENT_1.id), 0L);
		count(new PatientFilter().withAlertable(false), 0L);
		count(new PatientFilter().withHasEnrolledAt(true), 0L);
		count(new PatientFilter().withEnrolledAtFrom(hourAgo(ENROLLED_AT)).withEnrolledAtTo(hourAhead(ENROLLED_AT)), 0L);
		count(new PatientFilter().withHasRejectedAt(false), 0L);
		count(new PatientFilter().withRejectedAtFrom(hourAhead(REJECTED_AT_1)).withEnrolledAtTo(hourAgo(REJECTED_AT_1)), 0L);
		count(new PatientFilter().withFacilityId(FACILITY_1.id), 1L);
		count(new PatientFilter().withPersonId(PATIENT.id), 1L);
		count(new PatientFilter().withAlertable(true), 1L);
		count(new PatientFilter().withHasEnrolledAt(false), 1L);
		count(new PatientFilter().withEnrolledAtFrom(hourAhead(ENROLLED_AT)).withEnrolledAtTo(hourAgo(ENROLLED_AT)), 0L);
		count(new PatientFilter().withHasRejectedAt(true), 1L);
		count(new PatientFilter().withRejectedAtFrom(hourAgo(REJECTED_AT_1)).withRejectedAtTo(hourAhead(REJECTED_AT_1)), 1L);
	}

	@Test
	public void modify_find()
	{
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(FACILITY_1.id, record.getFacilityId(), "Check facilityId");
		Assertions.assertEquals(PATIENT.id, record.getPersonId(), "Check personId");
		Assertions.assertTrue(record.isAlertable(), "Check alertable");
		Assertions.assertNull(record.getEnrolledAt(), "Check enrolledAt");
		Assertions.assertEquals(REJECTED_AT_1, record.getRejectedAt(), "Check rejectedAt");
		check(VALUE, record);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();
		var rejectedAtAgo = hourAgo(REJECTED_AT_1);
		var rejectedAtAhead = hourAhead(REJECTED_AT_1);

		return Stream.of(
			arguments(new PatientFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new PatientFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(new PatientFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(new PatientFilter(1, 20).withAlertable(VALUE.alertable), 1L),
			arguments(new PatientFilter(1, 20).withHasEnrolledAt(false), 1L),
			/* arguments(new PatientFilter(1, 20).withEnrolledAtFrom(hourAgo), 1L),
			arguments(new PatientFilter(1, 20).withEnrolledAtTo(hourAhead), 1L),
			arguments(new PatientFilter(1, 20).withEnrolledAtFrom(hourAgo).withEnrolledAtTo(hourAhead), 1L), */
			arguments(new PatientFilter(1, 20).withHasRejectedAt(true), 1L),
			arguments(new PatientFilter(1, 20).withRejectedAtFrom(rejectedAtAgo), 1L),
			arguments(new PatientFilter(1, 20).withRejectedAtTo(rejectedAtAhead), 1L),
			arguments(new PatientFilter(1, 20).withRejectedAtFrom(rejectedAtAgo).withRejectedAtTo(rejectedAtAhead), 1L),
			arguments(new PatientFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new PatientFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new PatientFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new PatientFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new PatientFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new PatientFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new PatientFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(new PatientFilter(1, 20).withFacilityId(FACILITY.id), 0L),
			arguments(new PatientFilter(1, 20).withPersonId(PATIENT_1.id), 0L),
			arguments(new PatientFilter(1, 20).withAlertable(!VALUE.alertable), 0L),
			arguments(new PatientFilter(1, 20).withHasEnrolledAt(true), 0L),
			arguments(new PatientFilter(1, 20).withEnrolledAtFrom(hourAgo), 0L),
			arguments(new PatientFilter(1, 20).withEnrolledAtTo(hourAhead), 0L),
			arguments(new PatientFilter(1, 20).withEnrolledAtFrom(hourAgo).withEnrolledAtTo(hourAhead), 0L),
			arguments(new PatientFilter(1, 20).withHasRejectedAt(false), 0L),
			arguments(new PatientFilter(1, 20).withRejectedAtFrom(rejectedAtAhead), 0L),
			arguments(new PatientFilter(1, 20).withRejectedAtTo(rejectedAtAgo), 0L),
			arguments(new PatientFilter(1, 20).withRejectedAtFrom(rejectedAtAhead).withRejectedAtTo(rejectedAtAgo), 0L),
			arguments(new PatientFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new PatientFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new PatientFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new PatientFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new PatientFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new PatientFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final PatientFilter filter, final long expectedTotal)
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

	public static Stream<Arguments> search_as()
	{
		return Stream.of(
			arguments(ADMIN, true, 1L),
			arguments(EDITOR, false, 0L),
			arguments(PATIENT_, true, 1L),
			arguments(PATIENT_1_, true, 0L),
			arguments(ASSOCIATE_, true, 0L),
			arguments(ASSOCIATE_1_, true, 1L));
	}

	@ParameterizedTest
	@MethodSource
	public void search_as(final SessionValue session, final boolean success, final long expectedTotal)
	{
		sessionDao.current(session);

		if (success)
			search(new PatientFilter(), expectedTotal);
		else
			assertThrows(NotAuthorizedException.class, () -> dao.search(new PatientFilter()));
	}

	public static Stream<Arguments> search_sort()
	{
		return Stream.of(
			arguments(new PatientFilter("id", null), "id", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("id", "ASC"), "id", "ASC"),
			arguments(new PatientFilter("id", "asc"), "id", "ASC"),
			arguments(new PatientFilter("id", "invalid"), "id", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("id", "DESC"), "id", "DESC"),
			arguments(new PatientFilter("id", "desc"), "id", "DESC"),

			arguments(new PatientFilter("facilityId", null), "facilityId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("facilityId", "ASC"), "facilityId", "ASC"),
			arguments(new PatientFilter("facilityId", "asc"), "facilityId", "ASC"),
			arguments(new PatientFilter("facilityId", "invalid"), "facilityId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("facilityId", "DESC"), "facilityId", "DESC"),
			arguments(new PatientFilter("facilityId", "desc"), "facilityId", "DESC"),

			arguments(new PatientFilter("facilityName", null), "facilityName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("facilityName", "ASC"), "facilityName", "ASC"),
			arguments(new PatientFilter("facilityName", "asc"), "facilityName", "ASC"),
			arguments(new PatientFilter("facilityName", "invalid"), "facilityName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("facilityName", "DESC"), "facilityName", "DESC"),
			arguments(new PatientFilter("facilityName", "desc"), "facilityName", "DESC"),

			arguments(new PatientFilter("personId", null), "personId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("personId", "ASC"), "personId", "ASC"),
			arguments(new PatientFilter("personId", "asc"), "personId", "ASC"),
			arguments(new PatientFilter("personId", "invalid"), "personId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("personId", "DESC"), "personId", "DESC"),
			arguments(new PatientFilter("personId", "desc"), "personId", "DESC"),

			arguments(new PatientFilter("personName", null), "personName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("personName", "ASC"), "personName", "ASC"),
			arguments(new PatientFilter("personName", "asc"), "personName", "ASC"),
			arguments(new PatientFilter("personName", "invalid"), "personName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("personName", "DESC"), "personName", "DESC"),
			arguments(new PatientFilter("personName", "desc"), "personName", "DESC"),

			arguments(new PatientFilter("alertable", null), "alertable", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("alertable", "ASC"), "alertable", "ASC"),
			arguments(new PatientFilter("alertable", "asc"), "alertable", "ASC"),
			arguments(new PatientFilter("alertable", "invalid"), "alertable", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("alertable", "DESC"), "alertable", "DESC"),
			arguments(new PatientFilter("alertable", "desc"), "alertable", "DESC"),

			arguments(new PatientFilter("enrolledAt", null), "enrolledAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("enrolledAt", "ASC"), "enrolledAt", "ASC"),
			arguments(new PatientFilter("enrolledAt", "asc"), "enrolledAt", "ASC"),
			arguments(new PatientFilter("enrolledAt", "invalid"), "enrolledAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("enrolledAt", "DESC"), "enrolledAt", "DESC"),
			arguments(new PatientFilter("enrolledAt", "desc"), "enrolledAt", "DESC"),

			arguments(new PatientFilter("rejectedAt", null), "rejectedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("rejectedAt", "ASC"), "rejectedAt", "ASC"),
			arguments(new PatientFilter("rejectedAt", "asc"), "rejectedAt", "ASC"),
			arguments(new PatientFilter("rejectedAt", "invalid"), "rejectedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("rejectedAt", "DESC"), "rejectedAt", "DESC"),
			arguments(new PatientFilter("rejectedAt", "desc"), "rejectedAt", "DESC"),

			arguments(new PatientFilter("createdAt", null), "createdAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("createdAt", "ASC"), "createdAt", "ASC"),
			arguments(new PatientFilter("createdAt", "asc"), "createdAt", "ASC"),
			arguments(new PatientFilter("createdAt", "invalid"), "createdAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("createdAt", "DESC"), "createdAt", "DESC"),
			arguments(new PatientFilter("createdAt", "desc"), "createdAt", "DESC"),

			arguments(new PatientFilter("updatedAt", null), "updatedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PatientFilter("updatedAt", "ASC"), "updatedAt", "ASC"),
			arguments(new PatientFilter("updatedAt", "asc"), "updatedAt", "ASC"),
			arguments(new PatientFilter("updatedAt", "invalid"), "updatedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PatientFilter("updatedAt", "DESC"), "updatedAt", "DESC"),
			arguments(new PatientFilter("updatedAt", "desc"), "updatedAt", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	public void search_sort(final PatientFilter filter, final String expectedSortOn, final String expectedSortDir)
	{
		var results = dao.search(filter);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedSortOn, results.sortOn, "Check sortOn");
		Assertions.assertEquals(expectedSortDir, results.sortDir, "Check sortDir");
	}

	public static Stream<SessionValue> testRemove_fail()
	{
		return Stream.of(EDITOR, PATIENT_1_, ASSOCIATE_);
	}

	@ParameterizedTest
	@MethodSource
	public void testRemove_fail(final SessionValue session)
	{
		sessionDao.current(session);

		assertThrows(NotAuthorizedException.class, () -> dao.remove(VALUE.id));
	}

	@Test
	public void testRemove_fail_count() { modify_count(); }

	/** Test removal after the search. */
	@Test
	public void testRemove_success()
	{
		Assertions.assertFalse(dao.remove(VALUE.id + 1000L), "Invalid");
		Assertions.assertTrue(dao.remove(VALUE.id), "Removed");
		Assertions.assertFalse(dao.remove(VALUE.id), "Already removed");
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_success_find()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.id));
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_success_search()
	{
		count(new PatientFilter().withId(VALUE.id), 0L);
		count(new PatientFilter().withFacilityId(FACILITY_1.id), 0L);
		count(new PatientFilter().withPersonId(PATIENT.id), 0L);
		count(new PatientFilter().withAlertable(true), 0L);
		count(new PatientFilter().withHasEnrolledAt(false), 0L);
		count(new PatientFilter().withHasRejectedAt(true), 0L);
	}

	@Test
	public void z_00_add()
	{
		sessionDao.current(PATIENT_);

		VALUE = dao.add(new PatientValue(FACILITY_1.id, PATIENT_1.id, false, ENROLLED_AT_1, null));	// Will ignore the supplied personId.
	}

	@Test
	public void z_00_get()
	{
		var value = dao.getById(VALUE.id);
		Assertions.assertEquals(FACILITY_1.id, value.facilityId, "Check facilityId");
		Assertions.assertEquals(PATIENT.id, value.personId, "Check personId");
	}

	@Test
	public void z_00_remove_fail()
	{
		sessionDao.current(PATIENT_1_);

		assertThrows(NotAuthorizedException.class, () -> dao.remove(VALUE.id));
	}

	@Test
	public void z_00_remove_fail_check()
	{
		z_00_get();
	}

	@Test
	public void z_00_remove_success()
	{
		sessionDao.current(PATIENT_);

		Assertions.assertTrue(dao.remove(VALUE.id));
	}

	@Test
	public void z_00_remove_success_check()
	{
		Assertions.assertNull(dao.getById(VALUE.id));
	}

	public static Stream<Arguments> z_10_enroll_fail()
	{
		return Stream.of(
			arguments(ADMIN, FACILITY.id, PATIENT.id, NotAuthorizedException.class, "Must be a Person session."),
			arguments(EDITOR, FACILITY.id, PATIENT.id, NotAuthorizedException.class, "Must be a Person session."),
			arguments(PATIENT_, FACILITY.id, PATIENT.id, NotAuthorizedException.class, " is not an associate of the facility ID '1'."),
			arguments(ASSOCIATE_1_, FACILITY.id, PATIENT.id, NotAuthorizedException.class, " is not an associate of the facility ID '1'."),
			arguments(ASSOCIATE_, null, PATIENT.id, ValidationException.class, "Facility is not set."),
			arguments(ASSOCIATE_, 1000L, PATIENT.id, NotAuthorizedException.class, " is not an associate of the facility ID '1000'."),
			arguments(ASSOCIATE_, FACILITY.id, null, ValidationException.class, "Person is not set."),
			arguments(ASSOCIATE_, FACILITY.id, StringUtils.repeat('1', PatientValue.MAX_LEN_PERSON_ID + 1), ValidationException.class, "Person '11111111111' is longer than the expected size of 10."),
			arguments(ASSOCIATE_, FACILITY.id, "invalid", ValidationException.class, "The Person ID, invalid, is invalid."));
	}

	@ParameterizedTest
	@MethodSource
	public void z_10_enroll_fail(final SessionValue session, final Long facilityId, final String personId, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(assertThrows(ex, () -> dao.enroll(facilityId, personId)))
			.hasMessageContaining(message);
	}

	@Test
	public void z_10_enroll_success()
	{
		sessionDao.current(ASSOCIATE_);

		var record = dao.enroll(FACILITY.id, PATIENT_1.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(3L, record.getId(), "Check id");
	}

	@Test
	public void z_10_enroll_success_check()
	{
		var record = dao.findWithException(3L);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(FACILITY.id, record.getFacilityId(), "Check facilityId");
		Assertions.assertEquals(PATIENT_1.id, record.getPersonId(), "Check personId");
		Assertions.assertNull(record.getEnrolledAt(), "Check enrolledAt");
		Assertions.assertNull(record.getRejectedAt(), "Check rejectedAt");
		Assertions.assertFalse(record.isAlertable(), "Check alertable");
		Assertions.assertEquals(record.getUpdatedAt(), record.getCreatedAt(), "Check createdAt");
	}

	public static Stream<PeopleValue> z_11_accept_fail() { return Stream.of(ASSOCIATE, ASSOCIATE_1, PATIENT); }

	@ParameterizedTest
	@MethodSource
	public void z_11_accept_fail(final PeopleValue person)
	{
		Assertions.assertFalse(dao.accept(person.id));
	}

	@Test
	public void z_11_accept_fail_check()
	{
		z_10_enroll_success_check();	// Unchanged.
	}

	@ParameterizedTest
	@MethodSource("z_11_accept_fail")
	public void z_11_reject_fail(final PeopleValue person)
	{
		Assertions.assertFalse(dao.reject(person.id));
	}

	@Test
	public void z_11_reject_fail_check()
	{
		z_10_enroll_success_check();	// Unchanged.
	}

	@Test
	public void z_12_accept()
	{
		Assertions.assertTrue(dao.accept(PATIENT_1.id));
	}

	@Test
	public void z_12_accept_check()
	{
		var record = dao.findWithException(3L);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(FACILITY.id, record.getFacilityId(), "Check facilityId");
		Assertions.assertEquals(PATIENT_1.id, record.getPersonId(), "Check personId");
		assertThat(record.getEnrolledAt()).as("Check enrolledAt").isCloseTo(new Date(), 1000L);
		Assertions.assertNull(record.getRejectedAt(), "Check rejectedAt");
		Assertions.assertTrue(record.isAlertable(), "Check alertable");
		assertThat(record.getUpdatedAt()).as("Check createdAt").isAfter(record.getCreatedAt()).isEqualTo(record.getEnrolledAt());
	}

	@Test
	public void z_12_accept_iterate()
	{
		Assertions.assertFalse(dao.accept(PATIENT_1.id));	// Already accepted.
	}

	@Test
	public void z_12_accept_iterate_check()
	{
		z_12_accept_check();
	}

	@Test
	public void z_12_reject()
	{
		Assertions.assertTrue(dao.reject(PATIENT_1.id));
	}

	@Test
	public void z_12_reject_check()
	{
		var record = dao.findWithException(3L);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(FACILITY.id, record.getFacilityId(), "Check facilityId");
		Assertions.assertEquals(PATIENT_1.id, record.getPersonId(), "Check personId");
		Assertions.assertNull(record.getEnrolledAt(), "Check enrolledAt");
		assertThat(record.getRejectedAt()).as("Check rejectedAt").isCloseTo(new Date(), 1000L);
		Assertions.assertFalse(record.isAlertable(), "Check alertable");
		assertThat(record.getUpdatedAt()).as("Check createdAt").isAfter(record.getCreatedAt()).isEqualTo(record.getRejectedAt());
	}

	@Test
	public void z_12_reject_iterate()
	{
		Assertions.assertFalse(dao.reject(PATIENT_1.id));	// Already rejected.
	}

	@Test
	public void z_12_reject_iterate_check()
	{
		z_12_reject_check();
	}

	@Test
	public void z_19_revert()
	{
		var record = dao.findWithException(3L);
		record.setEnrolledAt(null);
		record.setRejectedAt(null);
		record.setAlertable(false);
		record.setUpdatedAt(record.getCreatedAt());
	}

	@Test
	public void z_19_revert_check()
	{
		z_10_enroll_success_check();
	}

	@Test
	public void z_20_enroll_success()
	{
		sessionDao.current(ASSOCIATE_1_);

		var record = dao.enroll(FACILITY_1.id, PATIENT_1.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(4L, record.getId(), "Check id");
	}

	@Test
	public void z_20_enroll_success_check()
	{
		z_10_enroll_success_check();

		var record = dao.findWithException(4L);
		Assertions.assertNull(record.getEnrolledAt(), "Check enrolledAt");
		Assertions.assertNull(record.getRejectedAt(), "Check rejectedAt");
		Assertions.assertFalse(record.isAlertable(), "Check alertable");
	}

	@Test
	public void z_21_accept()
	{
		Assertions.assertTrue(dao.accept(PATIENT_1.id));
	}

	@ParameterizedTest
	@CsvSource({"3,false",
	            "4,true"})
	public void z_21_accept_check(final long id, final boolean expected)
	{
		var record = dao.findWithException(id);
		if (expected)
			assertThat(record.getEnrolledAt()).as("Check enrolledAt").isCloseTo(new Date(), 500L);
		else
			Assertions.assertNull(record.getEnrolledAt(), "Check enrolledAt");

		Assertions.assertNull(record.getRejectedAt(), "Check rejectedAt");
		Assertions.assertEquals(expected, record.isAlertable(), "Check alertable");
	}

	@Test
	public void z_21_accept_more()
	{
		Assertions.assertTrue(dao.accept(PATIENT_1.id));
	}

	@ParameterizedTest
	@CsvSource({"3,true",
	            "4,true"})
	public void z_21_accept_more_check(final long id, final boolean expected)
	{
		z_21_accept_check(id, expected);
	}

	@Test
	public void z_21_accept_more_more()
	{
		Assertions.assertFalse(dao.accept(PATIENT_1.id));
	}

	@ParameterizedTest
	@CsvSource({"3,true",
	            "4,true"})
	public void z_21_accept_more_more_check(final long id, final boolean expected)
	{
		z_21_accept_check(id, expected);
	}

	@Test
	public void z_21_reject()
	{
		Assertions.assertTrue(dao.reject(PATIENT_1.id));
	}

	@ParameterizedTest
	@CsvSource({"3,false",
	            "4,true"})
	public void z_21_reject_check(final long id, final boolean expected)
	{
		var record = dao.findWithException(id);
		if (expected)
		{
			Assertions.assertNull(record.getEnrolledAt(), "Check enrolledAt");
			assertThat(record.getRejectedAt()).as("Check rejectedAt").isCloseTo(new Date(), 500L);
		}
		else
		{
			assertThat(record.getEnrolledAt()).as("Check enrolledAt").isCloseTo(new Date(), 500L);
			Assertions.assertNull(record.getRejectedAt(), "Check rejectedAt");
		}

		Assertions.assertEquals(!expected, record.isAlertable(), "Check alertable");
	}

	@Test
	public void z_21_reject_more()
	{
		Assertions.assertTrue(dao.reject(PATIENT_1.id));
	}

	@ParameterizedTest
	@CsvSource({"3,true",
	            "4,true"})
	public void z_21_reject_more_check(final long id, final boolean expected)
	{
		z_21_reject_check(id, expected);
	}

	@Test
	public void z_21_reject_more_more()
	{
		Assertions.assertFalse(dao.reject(PATIENT_1.id));
	}

	@ParameterizedTest
	@CsvSource({"3,true",
	            "4,true"})
	public void z_21_reject_more_more_check(final long id, final boolean expected)
	{
		z_21_reject_check(id, expected);
	}

	@Test
	public void z_22_accept()
	{
		Assertions.assertTrue(dao.accept(PATIENT_1.id));
	}

	@Test
	public void z_22_accept_check()
	{
		var record = dao.findWithException(4L);
		assertThat(record.getEnrolledAt()).as("Check enrolledAt").isCloseTo(new Date(), 500L);
		Assertions.assertNull(record.getRejectedAt(), "Check rejectedAt");
		Assertions.assertTrue(record.isAlertable(), "Check alertable");
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final PatientFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied entity record. */
	private void check(final PatientValue expected, final Patient record)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, record.getId(), assertId + "Check id");
		Assertions.assertEquals(expected.facilityId, record.getFacilityId(), assertId + "Check facilityId");
		Assertions.assertEquals(expected.personId, record.getPersonId(), assertId + "Check personId");
		Assertions.assertEquals(expected.alertable, record.isAlertable(), assertId + "Check alertable");
		Assertions.assertEquals(expected.enrolledAt, record.getEnrolledAt(), assertId + "Check enrolledAt");
		Assertions.assertEquals(expected.rejectedAt, record.getRejectedAt(), assertId + "Check rejectedAt");
		Assertions.assertEquals(expected.createdAt, record.getCreatedAt(), assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, record.getUpdatedAt(), assertId + "Check updatedAt");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final PatientValue expected, final PatientValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.facilityId, value.facilityId, assertId + "Check facilityId");
		Assertions.assertEquals(expected.facilityName, value.facilityName, assertId + "Check facilityName");
		Assertions.assertEquals(expected.personId, value.personId, assertId + "Check personId");
		Assertions.assertEquals(expected.personName, value.personName, assertId + "Check personName");
		Assertions.assertEquals(expected.alertable, value.alertable, assertId + "Check alertable");
		Assertions.assertEquals(expected.enrolledAt, value.enrolledAt, assertId + "Check enrolledAt");
		Assertions.assertEquals(expected.rejectedAt, value.rejectedAt, assertId + "Check rejectedAt");
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, value.updatedAt, assertId + "Check updatedAt");
	}
}
