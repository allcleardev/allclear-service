package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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
import app.allclear.common.errors.ValidationExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.App;
import app.allclear.platform.dao.FacilityDAO;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.value.FacilityValue;

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
	private static FacilityValue VALUE = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new ValidationExceptionMapper())
		.addResource(new FacilityResource(dao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/facilities";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<FacilityValue>> TYPE_LIST_VALUE = new GenericType<List<FacilityValue>>() {};
	private static final GenericType<QueryResults<FacilityValue, FacilityFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<FacilityValue, FacilityFilter>>() {};

	private static BigDecimal bg(final String value) { return new BigDecimal(value); }

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new FacilityDAO(factory);
	}

	@Test
	public void add()
	{
		var now = new Date();
		var response = request()
			.post(Entity.entity(VALUE = new FacilityValue("Julius", "56 First Street", "Louisville", "KY", bg("-35"), bg("52.607"),
				true, false, true, false, true, false, true, false), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FacilityValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withId(1L).withCreatedAt(now).withUpdatedAt(now), value);
	}

	@Test
	public void find()
	{
		var response = request(target().queryParam("name", "ul")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		assertThat(response.readEntity(TYPE_LIST_VALUE)).as("Check values").isEmpty();

		dao.update(VALUE.withActive(true));
		VALUE.withUpdatedAt(new Date());
	}

	@Test
	public void find_afterActive()
	{
		var response = request(target().queryParam("name", "ul")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		assertThat(response.readEntity(TYPE_LIST_VALUE)).as("Check values").containsExactly(VALUE);
	}

	@Test
	public void get()
	{
		var response = get(VALUE.id);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FacilityValue.class);
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
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, get(VALUE.id + 1000L).getStatus(), "Status");
	}

	@Test
	public void modify()
	{
		count(new FacilityFilter().withCity("Louisville"), 1L);
		count(new FacilityFilter().withState("KY"), 1L);
		count(new FacilityFilter().withCity("New Orleans"), 0L);
		count(new FacilityFilter().withState("LA"), 0L);

		var response = request().put(Entity.entity(VALUE.withCity("New Orleans").withState("LA"), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(FacilityValue.class);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE.withUpdatedAt(new Date()), value);
	}

	@Test
	public void modify_count()
	{
		count(new FacilityFilter().withCity("Louisville"), 0L);
		count(new FacilityFilter().withState("KY"), 0L);
		count(new FacilityFilter().withCity("New Orleans"), 1L);
		count(new FacilityFilter().withState("LA"), 1L);
	}

	@Test
	public void modify_get()
	{
		var value = get(VALUE.id).readEntity(FacilityValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("New Orleans", value.city, "Check city");
		Assertions.assertEquals("LA", value.state, "Check state");
		check(VALUE, value);
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
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

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
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final FacilityFilter filter, final long expectedTotal)
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
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, get(VALUE.id).getStatus(), "Status");
	}

	@Test
	public void testRemove_search()
	{
		count(new FacilityFilter().withId(VALUE.id), 0L);
		count(new FacilityFilter().withCity("New Orleans"), 0L);
		count(new FacilityFilter().withState("LA"), 0L);
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
	private void count(final FacilityFilter filter, long expectedTotal)
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
		if (null == expected.createdAt)
			Assertions.assertNull(value.createdAt, assertId + "Check createdAt");
		else
			assertThat(value.createdAt).as(assertId + "Check createdAt").isCloseTo(expected.createdAt, 1000L);
		if (null == expected.updatedAt)
			Assertions.assertNull(value.updatedAt, assertId + "Check updatedAt");
		else
			assertThat(value.updatedAt).as(assertId + "Check updatedAt").isCloseTo(expected.updatedAt, 1000L);
	}
}
