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
import org.junit.jupiter.params.provider.*;

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
import app.allclear.platform.dao.PatientDAO;
import app.allclear.platform.dao.PeopleDAO;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.dao.TestAuditor;
import app.allclear.platform.filter.PatientFilter;
import app.allclear.platform.value.AdminValue;
import app.allclear.platform.value.FacilityValue;
import app.allclear.platform.value.PatientValue;
import app.allclear.platform.value.PeopleValue;
import app.allclear.platform.value.SessionValue;

/**********************************************************************************
*
*	Functional test for the RESTful resource that handles access to the Patient entity.
*
*	@author smalleyd
*	@version 1.1.111
*	@since July 18, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class PatientResourceTest
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
	// private static final Date ENROLLED_AT = utc(2020, 6, 1);
	private static final Date ENROLLED_AT_1 = utc(2020, 7, 1);
	private static final Date REJECTED_AT = utc(2020, 6, 15);
	private static final Date REJECTED_AT_1 = utc(2020, 7, 15);
	private static final SessionValue ADMIN = new SessionValue(false, new AdminValue("admin"));
	/* private static final SessionValue EDITOR = new SessionValue(false, new AdminValue("editor", false, true));
	private static final SessionValue PATIENT_ = new SessionValue(false, PATIENT);
	private static final SessionValue PATIENT_1_ = new SessionValue(false, PATIENT);
	private static final SessionValue ASSOCIATE_ = new SessionValue(false, ASSOCIATE);
	private static final SessionValue ASSOCIATE_1_ = new SessionValue(false, ASSOCIATE); */

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new PatientResource(dao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/patients";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<PeopleValue>> TYPE_LIST_VALUE = new GenericType<List<PeopleValue>>() {};
	private static final GenericType<QueryResults<PatientValue, PatientFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<PatientValue, PatientFilter>>() {};

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
		peopleDao.add(ASSOCIATE_1.withAssociations(FACILITY, FACILITY_1), true);

		var now = new Date();
		var response = request()
			.post(Entity.entity(VALUE = new PatientValue(FACILITY_1.id, PATIENT.id, true, null, REJECTED_AT), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(PatientValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withId(1L).withFacilityName(FACILITY_1.name).withPersonName(PATIENT.name).withCreatedAt(now).withUpdatedAt(now), value);
	}

	@ParameterizedTest
	@CsvSource({"1,0,false",
	            "1,1,false",
	            "2,1,false",
	            "2,0,false"})	// The record is NOT enrolled so nothing will be returned.
	public void find(final Long facilityId, final String name, final boolean found)
	{
		var response = request(target().queryParam("facilityId", facilityId).queryParam("name", name)).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var values = response.readEntity(TYPE_LIST_VALUE);
		if (found)
			assertThat(values).isNotNull().containsExactly(PATIENT_1.withHealthWorkerStatusId(null).withHealthWorkerStatus(null));	// Remove fields that are NOT visible to the user.
		else
			assertThat(values).isNotNull().isEmpty();
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(PatientValue.class);
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
		count(new PatientFilter().withFacilityId(FACILITY_1.id), 1L);
		count(new PatientFilter().withPersonId(PATIENT.id), 1L);
		count(new PatientFilter().withAlertable(true), 1L);
		count(new PatientFilter().withHasEnrolledAt(false), 1L);
		count(new PatientFilter().withHasRejectedAt(true), 1L);
		count(new PatientFilter().withRejectedAtFrom(hourAgo(REJECTED_AT)).withRejectedAtTo(hourAhead(REJECTED_AT)), 1L);
		count(new PatientFilter().withFacilityId(FACILITY.id), 0L);
		count(new PatientFilter().withPersonId(PATIENT_1.id), 0L);
		count(new PatientFilter().withAlertable(false), 0L);
		count(new PatientFilter().withHasEnrolledAt(true), 0L);
		count(new PatientFilter().withEnrolledAtFrom(hourAgo(ENROLLED_AT_1)).withEnrolledAtTo(hourAhead(ENROLLED_AT_1)), 0L);
		count(new PatientFilter().withHasRejectedAt(false), 0L);
		count(new PatientFilter().withRejectedAtFrom(hourAgo(REJECTED_AT_1)).withRejectedAtTo(hourAhead(REJECTED_AT_1)), 0L);

		var response = request().put(Entity.entity(VALUE.withFacilityId(FACILITY.id).withPersonId(PATIENT_1.id).withAlertable(false).withEnrolledAt(ENROLLED_AT_1).withRejectedAt(null), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(PatientValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withFacilityName(FACILITY.name).withPersonName(PATIENT_1.name).withUpdatedAt(new Date()), value);
	}

	@Test
	public void modify_count()
	{
		count(new PatientFilter().withFacilityId(FACILITY_1.id), 0L);
		count(new PatientFilter().withPersonId(PATIENT.id), 0L);
		count(new PatientFilter().withAlertable(true), 0L);
		count(new PatientFilter().withHasEnrolledAt(false), 0L);
		count(new PatientFilter().withHasRejectedAt(true), 0L);
		count(new PatientFilter().withRejectedAtFrom(hourAgo(REJECTED_AT)).withRejectedAtTo(hourAhead(REJECTED_AT)), 0L);
		count(new PatientFilter().withFacilityId(FACILITY.id), 1L);
		count(new PatientFilter().withPersonId(PATIENT_1.id), 1L);
		count(new PatientFilter().withAlertable(false), 1L);
		count(new PatientFilter().withHasEnrolledAt(true), 1L);
		count(new PatientFilter().withEnrolledAtFrom(hourAgo(ENROLLED_AT_1)).withEnrolledAtTo(hourAhead(ENROLLED_AT_1)), 1L);
		count(new PatientFilter().withEnrolledAtFrom(hourAhead(ENROLLED_AT_1)).withEnrolledAtTo(hourAgo(ENROLLED_AT_1)), 0L);
		count(new PatientFilter().withHasRejectedAt(false), 1L);
		count(new PatientFilter().withRejectedAtFrom(hourAgo(REJECTED_AT)).withRejectedAtTo(hourAhead(REJECTED_AT)), 0L);
	}

	@ParameterizedTest
	@CsvSource({"1,0,false",
	            "1,1,true",
	            "2,1,false",
	            "2,0,false"})
	public void modify_find(final Long facilityId, final String name, final boolean found) { find(facilityId, name, found); }

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(PatientValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(FACILITY.id, value.facilityId, "Check facilityId");
		Assertions.assertEquals(PATIENT_1.id, value.personId, "Check personId");
		Assertions.assertFalse(value.alertable, "Check alertable");
		Assertions.assertEquals(ENROLLED_AT_1, value.enrolledAt, "Check enrolledAt");
		Assertions.assertNull(value.rejectedAt, "Check rejectedAt");
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new PatientFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new PatientFilter(1, 20).withFacilityId(VALUE.facilityId), 1L),
			arguments(new PatientFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(new PatientFilter(1, 20).withAlertable(VALUE.alertable), 1L),
			arguments(new PatientFilter(1, 20).withHasEnrolledAt(true), 1L),
			arguments(new PatientFilter(1, 20).withEnrolledAtFrom(hourAgo(ENROLLED_AT_1)), 1L),
			arguments(new PatientFilter(1, 20).withEnrolledAtTo(hourAhead(ENROLLED_AT_1)), 1L),
			arguments(new PatientFilter(1, 20).withEnrolledAtFrom(hourAgo(ENROLLED_AT_1)).withEnrolledAtTo(hourAhead(ENROLLED_AT_1)), 1L),
			arguments(new PatientFilter(1, 20).withHasRejectedAt(false), 1L),
			/* arguments(new PatientFilter(1, 20).withRejectedAtFrom(hourAgo), 1L),
			arguments(new PatientFilter(1, 20).withRejectedAtTo(hourAhead), 1L),
			arguments(new PatientFilter(1, 20).withRejectedAtFrom(hourAgo).withRejectedAtTo(hourAhead), 1L), */
			arguments(new PatientFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new PatientFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new PatientFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new PatientFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new PatientFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new PatientFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new PatientFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(new PatientFilter(1, 20).withFacilityId(VALUE.facilityId + 1000L), 0L),
			arguments(new PatientFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(new PatientFilter(1, 20).withAlertable(!VALUE.alertable), 0L),
			arguments(new PatientFilter(1, 20).withHasEnrolledAt(false), 0L),
			arguments(new PatientFilter(1, 20).withEnrolledAtFrom(hourAhead(ENROLLED_AT_1)), 0L),
			arguments(new PatientFilter(1, 20).withEnrolledAtTo(hourAgo(ENROLLED_AT_1)), 0L),
			arguments(new PatientFilter(1, 20).withEnrolledAtFrom(hourAhead(ENROLLED_AT_1)).withEnrolledAtTo(hourAgo(ENROLLED_AT_1)), 0L),
			arguments(new PatientFilter(1, 20).withHasRejectedAt(true), 0L),
			arguments(new PatientFilter(1, 20).withRejectedAtFrom(hourAgo(REJECTED_AT)), 0L),
			arguments(new PatientFilter(1, 20).withRejectedAtTo(hourAhead(REJECTED_AT)), 0L),
			arguments(new PatientFilter(1, 20).withRejectedAtFrom(hourAgo(REJECTED_AT)).withRejectedAtTo(hourAhead(REJECTED_AT)), 0L),
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
		count(new PatientFilter().withId(VALUE.id), 0L);
		count(new PatientFilter().withFacilityId(FACILITY.id), 0L);
		count(new PatientFilter().withPersonId(PATIENT_1.id), 0L);
		count(new PatientFilter().withAlertable(false), 0L);
		count(new PatientFilter().withHasEnrolledAt(true), 0L);
		count(new PatientFilter().withHasRejectedAt(false), 0L);
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
	private void count(final PatientFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
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
		if (null == expected.enrolledAt)
			Assertions.assertNull(value.enrolledAt, assertId + "Check enrolledAt");
		else
			assertThat(value.enrolledAt).as(assertId + "Check enrolledAt").isCloseTo(expected.enrolledAt, 500L);
		if (null == expected.rejectedAt)
			Assertions.assertNull(value.rejectedAt, assertId + "Check rejectedAt");
		else
			assertThat(value.rejectedAt).as(assertId + "Check rejectedAt").isCloseTo(expected.rejectedAt, 500L);
		assertThat(value.createdAt).as(assertId + "Check createdAt").isCloseTo(expected.createdAt, 500L);
		assertThat(value.updatedAt).as(assertId + "Check updatedAt").isCloseTo(expected.updatedAt, 500L);
	}
}
