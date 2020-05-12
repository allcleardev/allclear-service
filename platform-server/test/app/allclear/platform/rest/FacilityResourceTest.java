package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static app.allclear.platform.type.TestCriteria.*;
import static app.allclear.platform.type.TestType.*;
import static app.allclear.testing.TestingUtils.*;

import java.math.BigDecimal;
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
import app.allclear.google.client.MapClient;
import app.allclear.google.model.GeocodeResponse;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.filter.GeoFilter;
import app.allclear.platform.type.Symptom;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Facility entity.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class FacilityResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static FacilityDAO dao = null;
	private static PeopleDAO peopleDao = null;
	private static SessionDAO sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());
	private static TestAuditor auditor = new TestAuditor();
	private static MapClient map = mock(MapClient.class);
	private static FacilityResource resource = null;
	private static FacilityValue VALUE = null;
	private static FacilityValue VALUE_1 = null;
	private static SessionValue ADMIN = null;
	private static CustomerValue CUSTOMER = null;
	private static SessionValue EDITOR = null;
	private static SessionValue PERSON = null;
	private static SessionValue PERSON_UNRESTRICTED = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(resource = new FacilityResource(dao, sessionDao, map)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/facilities";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<FacilityValue>> TYPE_LIST_VALUE = new GenericType<List<FacilityValue>>() {};
	private static final GenericType<QueryResults<FacilityValue, FacilityFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<FacilityValue, FacilityFilter>>() {};

	private static BigDecimal bg(final String value) { return new BigDecimal(value); }

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new FacilityDAO(factory, auditor);
		peopleDao = new PeopleDAO(factory);

		when(map.geocode(contains("Street"))).thenReturn(loadObject("/google/map/geocode.json", GeocodeResponse.class));
		when(map.geocode(contains("Avenue"))).thenReturn(loadObject("/google/map/geocode-requestDenied.json", GeocodeResponse.class));
		when(map.geocode(contains("Lane"))).thenReturn(loadObject("/google/map/geocode-zeroResults.json", GeocodeResponse.class));
	}

	private static int auditorAdds = 0;
	private static int auditorUpdates = 0;
	private static int auditorRemoves = 0;

	@BeforeEach
	public void beforeEach()
	{
		if (null != ADMIN) sessionDao.current(ADMIN);

		Assertions.assertEquals(auditorAdds, auditor.adds, "Check auditorAdds");
		Assertions.assertEquals(auditorUpdates, auditor.updates, "Check auditorUpdates");
		Assertions.assertEquals(auditorRemoves, auditor.removes, "Check auditorRemoves");
	}

	@Test
	public void add()
	{
		CUSTOMER = new CustomerValue("customer");
		EDITOR = sessionDao.add(new AdminValue("editor", false, true), false);
		sessionDao.current(ADMIN = sessionDao.add(new AdminValue("admin"), false));

		var now = new Date();
		var response = request()
			.post(Entity.entity(VALUE = new FacilityValue("Julius", "56 First Street", "Louisville", "KY", bg("-35"), bg("52.607"),
				true, false, true, false, true, false, true, false).withTestTypes(ANTIBODY, DONT_KNOW), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FacilityValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertFalse(value.active, "Check active");
		Assertions.assertNull(value.activatedAt, "Check activatedAt");
		check(VALUE.withId(1L).withCreatedAt(now).withUpdatedAt(now), value);

		PERSON = sessionDao.add(peopleDao.add(new PeopleValue("firstPerson", "888-555-1000", true)), false);
		PERSON_UNRESTRICTED = sessionDao.add(peopleDao.add(new PeopleValue("secondPerson", "888-555-1001", true).withSymptoms(Symptom.FEVER)), false);

		peopleDao.addFacilities(PERSON.person.id, List.of(VALUE.id));

		// For later checks
		VALUE.withTestTypes(DONT_KNOW, ANTIBODY);	// Retrieved in alphabetical order of the test type ID.

		auditorAdds++;
	}

	@Test
	public void add_noAuth_as_anonymous()
	{
		count(new FacilityFilter(), 1L);

		sessionDao.clear();

		var response = request()
			.post(Entity.entity(new FacilityValue("Julius + 1", "56 First Street", "Louisville", "KY", bg("-35"), bg("52.607"),
				true, false, true, false, true, false, true, false), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, response.getStatus(), "Status");

		count(new FacilityFilter(), 1L);
	}

	@Test
	public void add_noAuth_as_persoon()
	{
		count(new FacilityFilter(), 1L);

		sessionDao.current(PERSON);

		var response = request()
			.post(Entity.entity(new FacilityValue("Julius + 1", "56 First Street", "Louisville", "KY", bg("-35"), bg("52.607"),
				true, false, true, false, true, false, true, false), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, response.getStatus(), "Status");

		count(new FacilityFilter(), 1L);
	}

	public static Stream<Arguments> check()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new FacilityFilter(1, 20).withHasActivatedAt(true), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtTo(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAgo).withActivatedAtTo(hourAhead), 0L),

			// Negative tests
			arguments(new FacilityFilter(1, 20).withHasActivatedAt(false), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAhead).withActivatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void check(final FacilityFilter filter, final long expectedTotal) { search_(filter, expectedTotal); }

	@Test
	public void check_as_anonymous()	// Perform GET while the facility is inactive.
	{
		sessionDao.clear();

		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE.id).getStatus(), "Status");
	}

	@Test
	public void check_as_editor()	// Perform GET while the facility is inactive.
	{
		sessionDao.current(EDITOR);

		var value = getX(VALUE.id);
		check(VALUE, value);

		Assertions.assertNull(value.activatedAt, "Check activatedAt");
	}

	@Test
	public void check_as_person()	// Perform GET while the facility is inactive.
	{
		sessionDao.current(PERSON);

		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE.id).getStatus(), "Status");
	}

	@Test
	public void find()
	{
		var response = request(target().queryParam("name", "ul")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		assertThat(response.readEntity(TYPE_LIST_VALUE)).as("Check values").isEmpty();

		dao.update(VALUE.withActive(true), true);
		VALUE.withUpdatedAt(new Date());

		auditorUpdates++;
	}

	@Test
	public void find_afterActive()
	{
		var response = request(target().queryParam("name", "ul")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var values = response.readEntity(TYPE_LIST_VALUE);
		assertThat(values).as("Check values").containsExactly(VALUE);
		assertThat(values.get(0).activatedAt).as("Check activatedAt").isNotNull().isCloseTo(new Date(), 1000L);
	}

	public static Stream<Arguments> find_afterActive_search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new FacilityFilter(1, 20).withHasActivatedAt(true), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAgo).withActivatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new FacilityFilter(1, 20).withHasActivatedAt(false), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAhead).withActivatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void find_afterActive_search(final FacilityFilter filter, final long expectedTotal) { search_(filter, expectedTotal); }

	@Test
	public void find_afterActive_withKm()
	{
		var response = request(target().queryParam("name", "ul")
			.queryParam("latitude", bg("-35.7"))
			.queryParam("longitude", bg("52.24"))
			.queryParam("km", 75)).get();
		Assertions.assertEquals(HTTP_STATUS_RUNTIME_EXCEPTION, response.getStatus(), "Status");	// ST_DISTANCE_SPHERE is not available on H2.
	}

	@Test
	public void find_afterActive_withLocation()
	{
		var response = request(target().queryParam("name", "ul")
			.queryParam("location", "56th Street")
			.queryParam("km", 75)).get();
		Assertions.assertEquals(HTTP_STATUS_RUNTIME_EXCEPTION, response.getStatus(), "Status");	// ST_DISTANCE_SPHERE is not available on H2.
	}

	@Test
	public void find_afterActive_withLocation_invalid()
	{
		var response = request(target().queryParam("name", "ul")
			.queryParam("location", "56th Avenue")
			.queryParam("km", 75)).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");	// Since location is invalid, no lat/lng will be provided.
	}

	@Test
	public void find_afterActive_withMiles()
	{
		var response = request(target().queryParam("name", "ul")
			.queryParam("latitude", bg("-35.7"))
			.queryParam("longitude", bg("52.24"))
			.queryParam("miles", 55)).get();
		Assertions.assertEquals(HTTP_STATUS_RUNTIME_EXCEPTION, response.getStatus(), "Status");	// ST_DISTANCE_SPHERE is not available on H2.
	}

	@Test
	public void geocode()
	{
		Assertions.assertNull(resource.geocode("56 First Avenue"), "Check '56 First Avenue'");
		Assertions.assertNull(resource.geocode("56 First Lane"), "Check '56 First Lane'");

		var r = resource.geocode("56 First Street");
		Assertions.assertNotNull(r, "Check '56 First Street'");
		Assertions.assertEquals("924", r.streetNumber().shortName, "Check result.streetNumber");
		Assertions.assertEquals("Willow Avenue", r.streetName().longName, "Check result.streetName.longName");
		Assertions.assertEquals("Willow Ave", r.streetName().shortName, "Check result.streetName.shortName");
		Assertions.assertEquals("Hoboken", r.city().shortName, "Check result.city");
		Assertions.assertEquals("Hudson County", r.county().shortName, "Check result.county");
		Assertions.assertEquals("New Jersey", r.state().longName, "Check result.state.longName");
		Assertions.assertEquals("NJ", r.state().shortName, "Check result.state.shortName");
		Assertions.assertEquals("United States", r.country().longName, "Check result.country.longName");
		Assertions.assertEquals("US", r.country().shortName, "Check result.country.shortName");
		Assertions.assertEquals("07030", r.postalCode().shortName, "Check result.postalCode");

		var l = r.geometry.location;
		Assertions.assertEquals(new BigDecimal("40.7487855"), l.lat, "Check result.geometry.location.lat");
		Assertions.assertEquals(new BigDecimal("-74.0315385"), l.lng, "Check result.geometry.location.lng");
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FacilityValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.testTypes).as("Check testTypes").containsExactly(DONT_KNOW.created(), ANTIBODY.created());
		check(VALUE, value);
	}

	/** Helper method - calls the GET endpoint. */
	private Response get(final Long id)
	{
		return request(id.toString()).get();
	}

	private FacilityValue getX(final Long id)
	{
		return request(id.toString()).get(FacilityValue.class);
	}

	@Test
	public void getWithException()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE.id + 1000L).getStatus(), "Status");
	}

	@Test
	public void get_as_anonymous()
	{
		sessionDao.clear();

		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FacilityValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.testTypes).as("Check testTypes").containsExactly(DONT_KNOW.created(), ANTIBODY.created());
		check(VALUE, value);
	}

	@Test
	public void modify()
	{
		count(new FacilityFilter().withCity("Louisville"), 1L);
		count(new FacilityFilter().withState("KY"), 1L);
		count(new FacilityFilter().include(DONT_KNOW), 1L);
		count(new FacilityFilter().include(ANTIBODY), 1L);
		count(new FacilityFilter().include(ANTIBODY, DONT_KNOW), 1L);
		count(new FacilityFilter().exclude(NASAL_SWAB), 1L);
		count(new FacilityFilter().withCity("New Orleans"), 0L);
		count(new FacilityFilter().withState("LA"), 0L);
		count(new FacilityFilter().exclude(DONT_KNOW), 0L);
		count(new FacilityFilter().exclude(ANTIBODY), 0L);
		count(new FacilityFilter().exclude(ANTIBODY, DONT_KNOW), 0L);
		count(new FacilityFilter().include(NASAL_SWAB), 0L);

		var response = request().put(Entity.entity(VALUE.withCity("New Orleans").withState("LA").withTestTypes(NASAL_SWAB), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FacilityValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withUpdatedAt(new Date()), value);

		auditorUpdates++;
	}

	@Test
	public void modify_count()
	{
		count(new FacilityFilter().withCity("Louisville"), 0L);
		count(new FacilityFilter().withState("KY"), 0L);
		count(new FacilityFilter().include(DONT_KNOW), 0L);
		count(new FacilityFilter().include(ANTIBODY), 0L);
		count(new FacilityFilter().include(ANTIBODY, DONT_KNOW), 0L);
		count(new FacilityFilter().exclude(NASAL_SWAB), 0L);
		count(new FacilityFilter().withCity("New Orleans"), 1L);
		count(new FacilityFilter().withState("LA"), 1L);
		count(new FacilityFilter().exclude(DONT_KNOW), 1L);
		count(new FacilityFilter().exclude(ANTIBODY), 1L);
		count(new FacilityFilter().exclude(ANTIBODY, DONT_KNOW), 1L);
		count(new FacilityFilter().include(NASAL_SWAB), 1L);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(FacilityValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("New Orleans", value.city, "Check city");
		Assertions.assertEquals("LA", value.state, "Check state");
		assertThat(value.testTypes).as("Check testTypes").containsExactly(NASAL_SWAB.created());
		check(VALUE, value);
	}

	@Test
	public void modify_noAuth_as_anonymous()
	{
		sessionDao.clear();

		var response = request()
			.put(Entity.entity(new FacilityValue("Julius + 1", "56 First Street", "Louisville", "KY", bg("-35"), bg("52.607"),
				true, false, true, false, true, false, true, false).withId(VALUE.id), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, response.getStatus(), "Status");
	}

	@Test
	public void modify_noAuth_as_person()
	{
		sessionDao.current(PERSON);

		var response = request()
			.put(Entity.entity(new FacilityValue("Julius + 1", "56 First Street", "Louisville", "KY", bg("-35"), bg("52.607"),
				true, false, true, false, true, false, true, false).withId(VALUE.id), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, response.getStatus(), "Status");
	}

	@Test
	public void populate_all()
	{
		var v = resource.populate(new FacilityValue().withAddress("56 First Street"));
		Assertions.assertEquals("Hoboken", v.city, "Check city");
		Assertions.assertEquals("New Jersey", v.state, "Check state");
		Assertions.assertEquals(bg("40.7487855"), v.latitude, "Check latitude");
		Assertions.assertEquals(bg("-74.0315385"), v.longitude, "Check longitude");
	}

	public static Stream<String> populate_invalid()
	{
		return Stream.of("56 First Avenue", "56 First Lane");
	}

	@ParameterizedTest
	@MethodSource
	public void populate_invalid(final String address)
	{
		var v = resource.populate(new FacilityValue().withAddress(address));
		Assertions.assertNull(v.city, "Check city");
		Assertions.assertNull(v.state, "Check state");
		Assertions.assertNull(v.latitude, "Check latitude");
		Assertions.assertNull(v.longitude, "Check longitude");
	}

	@Test
	public void populate_none()
	{
		var v = resource.populate(new FacilityValue().withAddress("56 First Street").withCity("Manchester").withState("New Hampshire").withLatitude(bg("-78.2")).withLongitude(bg("3.14")));
		Assertions.assertEquals("Manchester", v.city, "Check city");
		Assertions.assertEquals("New Hampshire", v.state, "Check state");
		Assertions.assertEquals(bg("-78.2"), v.latitude, "Check latitude");
		Assertions.assertEquals(bg("3.14"), v.longitude, "Check longitude");
	}

	@Test
	public void populate_some()
	{
		var v = resource.populate(new FacilityValue().withAddress("56 First Street").withState("New Hampshire").withLongitude(bg("3.14")));
		Assertions.assertEquals("Hoboken", v.city, "Check city");
		Assertions.assertEquals("New Hampshire", v.state, "Check state");
		Assertions.assertEquals(bg("40.7487855"), v.latitude, "Check latitude");
		Assertions.assertEquals(bg("3.14"), v.longitude, "Check longitude");
	}

	@Test
	public void remove_noAuth_as_anonymous()
	{
		sessionDao.clear();

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(VALUE.id.toString()).delete().getStatus(), "Status");
	}

	@Test
	public void remove_noAuth_as_editor()
	{
		sessionDao.current(EDITOR);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(VALUE.id.toString()).delete().getStatus(), "Status");
	}

	@Test
	public void remove_noAuth_as_person()
	{
		sessionDao.current(PERSON);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(VALUE.id.toString()).delete().getStatus(), "Status");
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();
		var latUp = VALUE.latitude.add(bg("1"));
		var latDown = VALUE.latitude.subtract(bg("1"));
		var lngUp = VALUE.longitude.add(bg("1"));
		var lngDown = VALUE.longitude.subtract(bg("1"));

		return Stream.of(
			arguments(new FacilityFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new FacilityFilter(1, 20).withName(VALUE.name), 1L),
			arguments(new FacilityFilter(1, 20).withAddress(VALUE.address), 1L),
			arguments(new FacilityFilter(1, 20).withCity(VALUE.city), 1L),
			arguments(new FacilityFilter(1, 20).withState(VALUE.state), 1L),
			arguments(new FacilityFilter(1, 20).withLatitudeFrom(latDown), 1L),
			arguments(new FacilityFilter(1, 20).withLatitudeTo(latUp), 1L),
			arguments(new FacilityFilter(1, 20).withLongitudeFrom(lngDown), 1L),
			arguments(new FacilityFilter(1, 20).withLongitudeTo(lngUp), 1L),
			arguments(new FacilityFilter(1, 20).withPhone(VALUE.phone), 1L),
			arguments(new FacilityFilter(1, 20).withAppointmentPhone(VALUE.appointmentPhone), 1L),
			arguments(new FacilityFilter(1, 20).withEmail(VALUE.email), 1L),
			arguments(new FacilityFilter(1, 20).withUrl(VALUE.url), 1L),
			arguments(new FacilityFilter(1, 20).withAppointmentUrl(VALUE.appointmentUrl), 1L),
			arguments(new FacilityFilter(1, 20).withHours(VALUE.hours), 1L),
			arguments(new FacilityFilter(1, 20).withTypeId(VALUE.typeId), 1L),
			arguments(new FacilityFilter(1, 20).withDriveThru(VALUE.driveThru), 1L),
			arguments(new FacilityFilter(1, 20).withAppointmentRequired(VALUE.appointmentRequired), 1L),
			arguments(new FacilityFilter(1, 20).withAcceptsThirdParty(VALUE.acceptsThirdParty), 1L),
			arguments(new FacilityFilter(1, 20).withReferralRequired(VALUE.referralRequired), 1L),
			arguments(new FacilityFilter(1, 20).withTestCriteriaId(VALUE.testCriteriaId), 1L),
			arguments(new FacilityFilter(1, 20).withNotTestCriteriaId(CDC_CRITERIA.id), 1L),	// NULLs are also included.
			arguments(new FacilityFilter(1, 20).withNotTestCriteriaId(OTHER.id), 1L),	// NULLs are also included.
			arguments(new FacilityFilter(1, 20).withHasTestCriteriaId(false), 1L),
			arguments(new FacilityFilter(1, 20).withOtherTestCriteria(VALUE.otherTestCriteria), 1L),
			arguments(new FacilityFilter(1, 20).withTestsPerDay(VALUE.testsPerDay), 1L),
			arguments(new FacilityFilter(1, 20).withTestsPerDayFrom(VALUE.testsPerDay), 1L),
			arguments(new FacilityFilter(1, 20).withTestsPerDayTo(VALUE.testsPerDay), 1L),
			arguments(new FacilityFilter(1, 20).withGovernmentIdRequired(VALUE.governmentIdRequired), 1L),
			arguments(new FacilityFilter(1, 20).withMinimumAge(VALUE.minimumAge), 1L),
			arguments(new FacilityFilter(1, 20).withMinimumAgeFrom(VALUE.minimumAge), 1L),
			arguments(new FacilityFilter(1, 20).withMinimumAgeTo(VALUE.minimumAge), 1L),
			arguments(new FacilityFilter(1, 20).withDoctorReferralCriteria(VALUE.doctorReferralCriteria), 1L),
			arguments(new FacilityFilter(1, 20).withFirstResponderFriendly(VALUE.firstResponderFriendly), 1L),
			arguments(new FacilityFilter(1, 20).withTelescreeningAvailable(VALUE.telescreeningAvailable), 1L),
			arguments(new FacilityFilter(1, 20).withAcceptsInsurance(VALUE.acceptsInsurance), 1L),
			arguments(new FacilityFilter(1, 20).withInsuranceProvidersAccepted(VALUE.insuranceProvidersAccepted), 1L),
			arguments(new FacilityFilter(1, 20).withFreeOrLowCost(VALUE.freeOrLowCost), 1L),
			arguments(new FacilityFilter(1, 20).withNotes(VALUE.notes), 1L),
			arguments(new FacilityFilter(1, 20).withActive(VALUE.active), 1L),
			arguments(new FacilityFilter(1, 20).withHasActivatedAt(true), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAgo).withActivatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).include(NASAL_SWAB), 1L),
			arguments(new FacilityFilter(1, 20).exclude(ANTIBODY), 1L),
			arguments(new FacilityFilter(1, 20).exclude(ANTIBODY, DONT_KNOW), 1L),
			arguments(new FacilityFilter(1, 20).exclude(DONT_KNOW), 1L),

			// Negative tests
			arguments(new FacilityFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(new FacilityFilter(1, 20).withName("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withAddress("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withCity("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withState("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withLatitudeFrom(latUp), 0L),
			arguments(new FacilityFilter(1, 20).withLatitudeTo(latDown), 0L),
			arguments(new FacilityFilter(1, 20).withLongitudeFrom(lngUp), 0L),
			arguments(new FacilityFilter(1, 20).withLongitudeTo(lngDown), 0L),
			arguments(new FacilityFilter(1, 20).withPhone("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withAppointmentPhone("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withEmail("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withUrl("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withAppointmentUrl("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHours("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withTypeId("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withDriveThru(!VALUE.driveThru), 0L),
			arguments(new FacilityFilter(1, 20).withAppointmentRequired(true), 0L),
			arguments(new FacilityFilter(1, 20).withAcceptsThirdParty(false), 0L),
			arguments(new FacilityFilter(1, 20).withReferralRequired(!VALUE.referralRequired), 0L),
			arguments(new FacilityFilter(1, 20).withTestCriteriaId("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasTestCriteriaId(true), 0L),
			arguments(new FacilityFilter(1, 20).withOtherTestCriteria("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withTestsPerDay(1000), 0L),
			arguments(new FacilityFilter(1, 20).withTestsPerDayFrom(1), 0L),
			arguments(new FacilityFilter(1, 20).withTestsPerDayTo(1), 0L),
			arguments(new FacilityFilter(1, 20).withGovernmentIdRequired(!VALUE.governmentIdRequired), 0L),
			arguments(new FacilityFilter(1, 20).withMinimumAge(1000), 0L),
			arguments(new FacilityFilter(1, 20).withMinimumAgeFrom(1), 0L),
			arguments(new FacilityFilter(1, 20).withMinimumAgeTo(1), 0L),
			arguments(new FacilityFilter(1, 20).withDoctorReferralCriteria("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withFirstResponderFriendly(!VALUE.firstResponderFriendly), 0L),
			arguments(new FacilityFilter(1, 20).withTelescreeningAvailable(!VALUE.telescreeningAvailable), 0L),
			arguments(new FacilityFilter(1, 20).withAcceptsInsurance(!VALUE.acceptsInsurance), 0L),
			arguments(new FacilityFilter(1, 20).withInsuranceProvidersAccepted("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withFreeOrLowCost(!VALUE.freeOrLowCost), 0L),
			arguments(new FacilityFilter(1, 20).withNotes("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withActive(!VALUE.active), 0L),
			arguments(new FacilityFilter(1, 20).withHasActivatedAt(false), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAhead).withActivatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).exclude(NASAL_SWAB), 0L),
			arguments(new FacilityFilter(1, 20).include(ANTIBODY), 0L),
			arguments(new FacilityFilter(1, 20).include(ANTIBODY, DONT_KNOW), 0L),
			arguments(new FacilityFilter(1, 20).include(DONT_KNOW), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final FacilityFilter filter, final long expectedTotal) { search_(filter, expectedTotal); }
	private QueryResults<FacilityValue, FacilityFilter> search_(final FacilityFilter filter, final long expectedTotal)
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
			results.records.forEach(v -> Assertions.assertFalse(v.restricted, "Check restricted: " + v.name));
			results.records.forEach(v -> {
				if (v.id.equals(1L)) assertThat(v.testTypes).as("Check testTypes").isEqualTo(VALUE.testTypes);
				else assertThat(v.testTypes).as("Check testTypes").isNull();
			});
		}

		return results;
	}

	@Test
	public void search_withAnonymous()
	{
		sessionDao.clear();

		var response = request("search").post(Entity.json(new FacilityFilter()));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assertions.assertNotNull(results, "Exists");
		assertThat(results.records).as("Check records").hasSize(1);

		var value = results.records.get(0);
		Assertions.assertFalse(value.favorite, "Check favorite");
		Assertions.assertFalse(value.restricted, "Check restricted");
	}

	@Test
	public void search_withLocation()
	{
		var response = request("search").post(Entity.json(new FacilityFilter().withFrom(new GeoFilter(null, null, "56th Street", null, 75))));
		Assertions.assertEquals(HTTP_STATUS_RUNTIME_EXCEPTION, response.getStatus(), "Status");	// ST_DISTANCE_SPHERE is not available on H2.
	}

	@Test
	public void search_withLocation_invalid()
	{
		var response = request("search").post(Entity.json(new FacilityFilter().withFrom(new GeoFilter(null, null, "56th Lane", null, 75))));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");	// Lat/Lng won't be populated since the location is invalid.
	}

	@Test
	public void set_00()
	{
		var response = request().put(Entity.json(new FacilityValue("Alex", "8th Street",
			true, false, true, false, true, false, true, true).withTestCriteriaId(CDC_CRITERIA.id)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var v = VALUE_1 = response.readEntity(FacilityValue.class);
		Assertions.assertEquals(2L, v.id, "Check ID");
		Assertions.assertEquals("Hoboken", v.city, "Check city");
		Assertions.assertEquals("New Jersey", v.state, "Check state");
		Assertions.assertEquals(bg("40.7487855"), v.latitude, "Check latitude");
		Assertions.assertEquals(bg("-74.0315385"), v.longitude, "Check longitude");
		Assertions.assertTrue(v.active, "Check active");

		auditorAdds++;
	}

	@Test
	public void set_00_check()
	{
		var v = get(2L).readEntity(FacilityValue.class);
		Assertions.assertEquals("Hoboken", v.city, "Check city");
		Assertions.assertEquals("New Jersey", v.state, "Check state");
		Assertions.assertEquals(bg("40.74878550"), v.latitude, "Check latitude");
		Assertions.assertEquals(bg("-74.03153850"), v.longitude, "Check longitude");

		count(new FacilityFilter().withCity("Hoboken"), 1L);
		count(new FacilityFilter().withState("New Jersey"), 1L);
	}

	@Test
	public void set_00_invalid()
	{
		var response = request().put(Entity.json(new FacilityValue("Alex", "8th Avenue", null, null, null, null,
			true, false, true, false, true, false, true, false)));
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");
	}

	@Test
	public void set_00_search_as_anonymous()
	{
		sessionDao.clear();

		search(new FacilityFilter(), 2L);
	}

	@Test
	public void set_00_search_as_customer()
	{
		sessionDao.current(CUSTOMER);

		search(new FacilityFilter(), 2L);
	}

	@Test
	public void set_00_search_restrictive_as_admin()
	{
		var o = request("search").post(Entity.json(new FacilityFilter().withRestrictive(true)), TYPE_QUERY_RESULTS);
		Assertions.assertEquals(2L, o.total, "Check total: restricted");
		Assertions.assertNull(o.filter.notTestCriteriaId, "Check filter.notTestCriteriaId: restricted");
		o.records.forEach(v -> Assertions.assertFalse(v.restricted, "Check restricted: " + v.name + " - restricted"));

		o = request("search").post(Entity.json(new FacilityFilter()), TYPE_QUERY_RESULTS);
		Assertions.assertEquals(2L, o.total, "Check total: unrestricted");
		o.records.forEach(v -> Assertions.assertFalse(v.restricted, "Check restricted: " + v.name + " - unrestricted"));
		o.records.forEach(v -> Assertions.assertFalse(v.favorite, "Check favorite: " + v.name + " - favorite"));
	}

	@Test
	public void set_00_search_restrictive_as_person()
	{
		sessionDao.current(PERSON);
		Assertions.assertFalse(PERSON.person.meetsCdcPriority3(), "Check meetsCdcPriority3()");

		var o = request("search").post(Entity.json(new FacilityFilter().withRestrictive(true)), TYPE_QUERY_RESULTS);
		Assertions.assertEquals(1L, o.total, "Check total: restricted");
		Assertions.assertEquals(CDC_CRITERIA.id, o.filter.notTestCriteriaId, "Check filter.notTestCriteriaId: restricted");
		o.records.forEach(v -> Assertions.assertFalse(v.restricted, "Check restricted: " + v.name + " - restricted"));

		o = request("search").post(Entity.json(new FacilityFilter()), TYPE_QUERY_RESULTS);
		Assertions.assertEquals(2L, o.total, "Check total: unrestricted");
		o.records.forEach(v -> Assertions.assertEquals("Alex".equals(v.name), v.restricted, "Check restricted: " + v.name + " - unrestricted"));
		o.records.forEach(v -> Assertions.assertEquals(!"Alex".equals(v.name), v.favorite, "Check favorite: " + v.name + " - favorite"));
	}

	@Test
	public void set_00_search_restrictive_as_person_unrestricted()
	{
		sessionDao.current(PERSON_UNRESTRICTED);
		Assertions.assertTrue(PERSON_UNRESTRICTED.person.symptomatic(), "Check symptomatic()");
		Assertions.assertTrue(PERSON_UNRESTRICTED.person.meetsCdcPriority3(), "Check meetsCdcPriority3()");

		var o = request("search").post(Entity.json(new FacilityFilter().withRestrictive(true)), TYPE_QUERY_RESULTS);
		Assertions.assertEquals(2L, o.total, "Check total");
		Assertions.assertNull(o.filter.notTestCriteriaId, "Check filter.notTestCriteriaId");
		o.records.forEach(v -> Assertions.assertFalse(v.restricted, "Check restricted: " + v.name + " - restricted"));

		o = request("search").post(Entity.json(new FacilityFilter()), TYPE_QUERY_RESULTS);
		Assertions.assertEquals(2L, o.total, "Check total: unrestricted");
		o.records.forEach(v -> Assertions.assertFalse(v.restricted, "Check restricted: " + v.name + " - unrestricted"));
		o.records.forEach(v -> Assertions.assertFalse(v.favorite, "Check favorite: " + v.name + " - favorite"));
	}

	@Test
	public void set_01()	// Deactivate the new facility
	{
		Assertions.assertEquals(HTTP_STATUS_OK, request().put(Entity.json(VALUE_1.withActive(false))).getStatus());

		auditorUpdates++;
	}

	@Test
	public void set_01_check()	// Deactivate the new facility
	{
		var response = get(VALUE_1.id);
		Assertions.assertFalse(response.readEntity(FacilityValue.class).active);
	}

	@Test
	public void set_01_search_as_anonymous()
	{
		sessionDao.clear();

		search_(new FacilityFilter(), 1L).records.forEach(v -> Assertions.assertTrue(v.active));
	}

	@Test
	public void set_01_search_as_customer()
	{
		sessionDao.current(CUSTOMER);

		search_(new FacilityFilter(), 1L).records.forEach(v -> Assertions.assertTrue(v.active));
	}

	@Test
	public void set_01_search_restrictive_as_admin()
	{
		set_00_search_restrictive_as_admin();
	}

	public static Stream<SessionValue> set_01_search_as_person() { return Stream.of(PERSON, PERSON_UNRESTRICTED); }

	@ParameterizedTest
	@MethodSource
	public void set_01_search_as_person(final SessionValue s)
	{
		sessionDao.current(s);

		var o = request("search").post(Entity.json(new FacilityFilter().withRestrictive(true)), TYPE_QUERY_RESULTS);
		Assertions.assertEquals(1L, o.total, "Check total: restricted");
		assertThat(o.records).as("Check records: restricted").containsExactly(VALUE);

		o = request("search").post(Entity.json(new FacilityFilter()), TYPE_QUERY_RESULTS);
		Assertions.assertEquals(1L, o.total, "Check total: unrestricted");
		assertThat(o.records).as("Check records: restricted").containsExactly(VALUE);
	}

	/** Test removal after the search. */
	@Test
	public void testRemove()
	{
		remove(VALUE.id + 1000L, false);
		remove(VALUE.id, true);
		remove(VALUE.id, false);

		auditorRemoves++;
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
		count(new FacilityFilter().withId(VALUE.id), 0L);
		count(new FacilityFilter().withCity("New Orleans"), 0L);
		count(new FacilityFilter().withState("LA"), 0L);
	}

	@Test
	public void z_10_add_as_editor()
	{
		sessionDao.current(EDITOR);

		VALUE = request().post(Entity.json(FacilityDAOTest.createValid().withName("byEditor").withActive(true)), FacilityValue.class);
		Assertions.assertNotNull(VALUE.id, "Check ID");
		Assertions.assertFalse(VALUE.active, "Check active");
		Assertions.assertNull(VALUE.activatedAt, "Check activatedAt");

		auditorAdds++;
	}

	@Test
	public void z_10_add_as_editor_check()
	{
		sessionDao.current(EDITOR);

		var v = getX(VALUE.id);
		Assertions.assertEquals("byEditor", v.name, "Check name");
		Assertions.assertFalse(v.active, "Check active");
		Assertions.assertNull(v.activatedAt, "Check activatedAt");
	}

	@Test
	public void z_10_add_as_person_search()
	{
		sessionDao.current(PERSON);

		search(new FacilityFilter().withName("byEditor"), 0L);	// CanNOT see inactive facilities.
	}

	@Test
	public void z_10_modify_as_editor()
	{
		sessionDao.current(EDITOR);

		var v = request().put(Entity.json(VALUE.withActive(true)), FacilityValue.class);
		Assertions.assertEquals(VALUE.id, v.id, "Check ID");
		Assertions.assertFalse(v.active, "Check active");
		Assertions.assertTrue(VALUE.active, "Check active");	// Sent in as TRUE.
		Assertions.assertNull(v.activatedAt, "Check activatedAt");

		VALUE.withActive(false);

		auditorUpdates++;
	}

	@Test
	public void z_10_modify_as_editor_check()
	{
		sessionDao.current(EDITOR);

		var v = getX(VALUE.id);
		Assertions.assertEquals("byEditor", v.name, "Check name");
		Assertions.assertFalse(v.active, "Check active");
		Assertions.assertNull(v.activatedAt, "Check activatedAt");
	}

	@Test
	public void z_10_modify_as_person_search()
	{
		sessionDao.current(PERSON);

		search(new FacilityFilter().withName("byEditor"), 0L);	// CanNOT see inactive facilities.
	}

	@Test
	public void z_11_modify_as_admin()
	{
		sessionDao.current(ADMIN);

		var v = request().put(Entity.json(VALUE.withActive(true)), FacilityValue.class);
		Assertions.assertEquals(VALUE.id, v.id, "Check ID");
		Assertions.assertTrue(v.active, "Check active");
		Assertions.assertTrue(VALUE.active, "Check active");
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isEqualTo(v.updatedAt).isAfter(v.createdAt).isCloseTo(new Date(), 500L);

		auditorUpdates++;
	}

	@Test
	public void z_11_modify_as_admin_check()
	{
		sessionDao.current(ADMIN);

		var v = getX(VALUE.id);
		Assertions.assertEquals("byEditor", v.name, "Check name");
		Assertions.assertTrue(v.active, "Check active");
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isEqualTo(v.updatedAt).isAfter(v.createdAt).isCloseTo(new Date(), 1000L);
	}

	@Test
	public void z_11_modify_as_person_search()
	{
		sessionDao.current(PERSON);

		search(new FacilityFilter().withName("byEditor"), 1L);
	}

	@Test
	public void z_12_modify_as_editor()
	{
		sessionDao.current(EDITOR);

		var v = request().put(Entity.json(VALUE.withActive(false)), FacilityValue.class);
		Assertions.assertEquals(VALUE.id, v.id, "Check ID");
		Assertions.assertTrue(v.active, "Check active");
		Assertions.assertFalse(VALUE.active, "Check active");	// Sent in as FALSE.
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isBefore(v.updatedAt).isAfter(v.createdAt);

		VALUE.withActive(true);

		auditorUpdates++;
	}

	@Test
	public void z_12_modify_as_editor_check()
	{
		sessionDao.current(EDITOR);

		var v = getX(VALUE.id);
		Assertions.assertEquals("byEditor", v.name, "Check name");
		Assertions.assertTrue(v.active, "Check active");
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isBefore(v.updatedAt).isAfter(v.createdAt);
	}

	@Test
	public void z_12_modify_as_person_search()
	{
		sessionDao.current(PERSON);

		search(new FacilityFilter().withName("byEditor"), 1L);
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
	private void count(final FacilityFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final FacilityValue expected, final FacilityValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.name, value.name, assertId + "Check name");
		Assertions.assertEquals(expected.address, value.address, assertId + "Check address");
		Assertions.assertEquals(expected.city, value.city, assertId + "Check city");
		Assertions.assertEquals(expected.state, value.state, assertId + "Check state");
		assertThat(value.latitude).as(assertId + "Check latitude").isEqualByComparingTo(expected.latitude);
		assertThat(value.longitude).as(assertId + "Check longitude").isEqualByComparingTo(expected.longitude);
		Assertions.assertEquals(expected.phone, value.phone, assertId + "Check phone");
		Assertions.assertEquals(expected.appointmentPhone, value.appointmentPhone, assertId + "Check appointmentPhone");
		Assertions.assertEquals(expected.email, value.email, assertId + "Check email");
		Assertions.assertEquals(expected.url, value.url, assertId + "Check url");
		Assertions.assertEquals(expected.appointmentUrl, value.appointmentUrl, assertId + "Check appointmentUrl");
		Assertions.assertEquals(expected.hours, value.hours, assertId + "Check hours");
		Assertions.assertEquals(expected.typeId, value.typeId, assertId + "Check typeId");
		Assertions.assertEquals(expected.driveThru, value.driveThru, assertId + "Check driveThru");
		Assertions.assertEquals(expected.appointmentRequired, value.appointmentRequired, assertId + "Check appointmentRequired");
		Assertions.assertEquals(expected.acceptsThirdParty, value.acceptsThirdParty, assertId + "Check acceptsThirdParty");
		Assertions.assertEquals(expected.referralRequired, value.referralRequired, assertId + "Check referralRequired");
		Assertions.assertEquals(expected.testCriteriaId, value.testCriteriaId, assertId + "Check testCriteriaId");
		Assertions.assertEquals(expected.otherTestCriteria, value.otherTestCriteria, assertId + "Check otherTestCriteria");
		Assertions.assertEquals(expected.testsPerDay, value.testsPerDay, assertId + "Check testsPerDay");
		Assertions.assertEquals(expected.governmentIdRequired, value.governmentIdRequired, assertId + "Check governmentIdRequired");
		Assertions.assertEquals(expected.minimumAge, value.minimumAge, assertId + "Check minimumAge");
		Assertions.assertEquals(expected.doctorReferralCriteria, value.doctorReferralCriteria, assertId + "Check doctorReferralCriteria");
		Assertions.assertEquals(expected.firstResponderFriendly, value.firstResponderFriendly, assertId + "Check firstResponderFriendly");
		Assertions.assertEquals(expected.telescreeningAvailable, value.telescreeningAvailable, assertId + "Check telescreeningAvailable");
		Assertions.assertEquals(expected.acceptsInsurance, value.acceptsInsurance, assertId + "Check acceptsInsurance");
		Assertions.assertEquals(expected.insuranceProvidersAccepted, value.insuranceProvidersAccepted, assertId + "Check insuranceProvidersAccepted");
		Assertions.assertEquals(expected.freeOrLowCost, value.freeOrLowCost, assertId + "Check freeOrLowCost");
		Assertions.assertEquals(expected.notes, value.notes, assertId + "Check notes");
		Assertions.assertEquals(expected.active, value.active, assertId + "Check active");
		if (null == expected.activatedAt)
			Assertions.assertNull(value.activatedAt, assertId + "Check activatedAt");
		else
			assertThat(value.activatedAt).as(assertId + "Check activatedAt").isCloseTo(expected.activatedAt, 500L);
		if (null == expected.createdAt)
			Assertions.assertNull(value.createdAt, assertId + "Check createdAt");
		else
			assertThat(value.createdAt).as(assertId + "Check createdAt").isCloseTo(expected.createdAt, 1000L);
		if (null == expected.updatedAt)
			Assertions.assertNull(value.updatedAt, assertId + "Check updatedAt");
		else
			assertThat(value.updatedAt).as(assertId + "Check updatedAt").isCloseTo(expected.updatedAt, 1000L);
		Assertions.assertEquals(expected.testTypes, value.testTypes, assertId + "Check testTypes");
	}
}
