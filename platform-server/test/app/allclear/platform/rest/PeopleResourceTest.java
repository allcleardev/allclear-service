package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static app.allclear.testing.TestingUtils.*;

import java.util.*;
import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.azure.storage.queue.QueueClient;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.junit.hibernate.*;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.NamedValue;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.App;
import app.allclear.platform.Config;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.*;
import app.allclear.platform.model.*;
import app.allclear.platform.value.*;
import app.allclear.platform.value.SessionValue;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.*;

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

	private static final Config conf = ConfigTest.loadTest();

	private static PeopleDAO dao = null;
	private static final FakeRedisClient redis = new FakeRedisClient();
	private static final TwilioClient twilio = mock(TwilioClient.class);
	private static final QueueClient alertQueue = mock(QueueClient.class);
	private static final SessionDAO sessionDao = new SessionDAO(redis, twilio, conf);
	private static final RegistrationDAO registrationDao = new RegistrationDAO(redis, twilio, conf);
	private static PeopleValue VALUE = null;
	private static Date AUTH_AT;
	private static Date EMAIL_VERIFIED_AT;
	private static Date PHONE_VERIFIED_AT;
	private static int ALERTED_OF = 5;
	private static Date ALERTED_AT;
	private static String LAST_ALERT;
	private static SMSResponse LAST_SMS_RESPONSE;
	private static RegistrationValue REGISTRATION;
	private static SessionValue ADMIN;
	private static SessionValue SESSION;
	private static SessionValue SESSION_1;
	private static Date LAST_UPDATED_AT = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthenticationExceptionMapper())
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new PeopleResource(dao, registrationDao, sessionDao, alertQueue))
		.addResource(new RegistrationResource(registrationDao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/peoples";
	private static final String REGISTRATIONS = "/registrations";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<NamedValue>> TYPE_LIST_NAMED = new GenericType<List<NamedValue>>() {};
	private static final GenericType<List<PeopleValue>> TYPE_LIST_VALUE = new GenericType<List<PeopleValue>>() {};
	private static final GenericType<QueryResults<PeopleValue, PeopleFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<PeopleValue, PeopleFilter>>() {};
	private static final GenericType<QueryResults<RegistrationValue, RegistrationFilter>> TYPE_QUERY_RESULTS_ =
			new GenericType<QueryResults<RegistrationValue, RegistrationFilter>>() {};

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new PeopleDAO(factory);

		AUTH_AT = timestamp("2020-03-24T12:46:30-0000");
		EMAIL_VERIFIED_AT = timestamp("2020-03-24T12:47:30-0000");
		PHONE_VERIFIED_AT = timestamp("2020-03-24T12:48:30-0000");
		ALERTED_AT = timestamp("2020-04-15T12:02:30-0000");

		when(twilio.send(any(SMSRequest.class))).thenAnswer(a -> LAST_SMS_RESPONSE = new SMSResponse(a.getArgument(0, SMSRequest.class)));
		when(alertQueue.sendMessage(any(String.class))).thenAnswer(a -> { LAST_ALERT = a.getArgument(0, String.class); return null; });
	}

	@Test
	public void add()
	{
		sessionDao.current(ADMIN = sessionDao.add(new AdminValue("admin"), false));

		var response = request()
			.post(Entity.entity(VALUE = new PeopleValue("minimal", "+18885551000", true)
				.withAuthAt(AUTH_AT).withEmailVerifiedAt(EMAIL_VERIFIED_AT).withPhoneVerifiedAt(PHONE_VERIFIED_AT).withAlertedOf(ALERTED_OF).withAlertedAt(ALERTED_AT),
					UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(PeopleValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.id).as("Check ID").hasSize(6);
		Assertions.assertNull(value.status, "Check status");
		Assertions.assertNull(value.stature, "Check stature");
		Assertions.assertNull(value.sex, "Check sex");
		assertThat(value.createdAt).as("Check createdAt").isCloseTo(new Date(), 500L);
		assertThat(value.updatedAt).as("Check updatedAt").isEqualTo(value.createdAt);
		check(VALUE.withId(value.id).withCreatedAt(value.createdAt).withUpdatedAt(value.updatedAt), value);

		SESSION = sessionDao.add(VALUE, false);
	}

	public static Stream<Arguments> alert_failure()
	{
		return Stream.of(
			arguments(ADMIN, "INVALID", HTTP_STATUS_NOT_FOUND),
			arguments(SESSION, VALUE.id, HTTP_STATUS_NOT_AUTHORIZED));
	}

	@ParameterizedTest
	@MethodSource
	public void alert_failure(final SessionValue session, final String id, final int status)
	{
		sessionDao.current(session);

		Assertions.assertEquals(status, request(id + "/alert").method(HttpMethod.POST).getStatus());
	}

	@Test
	public void alert_success()
	{
		sessionDao.current(ADMIN);

		var response = request(VALUE.id + "/alert").method(HttpMethod.POST);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		Assertions.assertEquals("{\"personId\":\"" + VALUE.id + "\"}", LAST_ALERT, "Check LAST_ALERT");
	}

	@Test
	public void authenticate_as_systemAdmin()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, request("123/auth").method(HttpMethod.POST).getStatus(), "Status: invalid Person ID");	// Invalid Person ID.

		var response = request(VALUE.id + "/auth").method(HttpMethod.POST);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(SessionValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertTrue(value.person(), "Check person()");
		Assertions.assertFalse(value.admin(), "Check admin()");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, value.duration);
		Assertions.assertEquals(VALUE.id, value.person.id, "Check person.id");

		SESSION = value;
	}

	@Test
	public void authenticate_as_systemAdmin_rememberMe()
	{
		var response = request(target().path(VALUE.id + "/auth").queryParam("rememberMe", true)).method(HttpMethod.POST);
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(SessionValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertTrue(value.person(), "Check person()");
		Assertions.assertFalse(value.admin(), "Check admin()");
		Assertions.assertEquals(SessionValue.DURATION_LONG, value.duration);
		Assertions.assertEquals(VALUE.id, value.person.id, "Check person.id");
	}

	@Test
	public void check_as_person()
	{
		sessionDao.current(SESSION);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request().post(Entity.json(new PeopleValue("next", "888-next-one", true))).getStatus(), "Status: add");
		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(VALUE.id + "/auth").method(HttpMethod.POST).getStatus(), "Status: auth");

		sessionDao.current(ADMIN);
	}

	@Test
	public void find()
	{
		var response = request(target().queryParam("name", "min")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		assertThat(response.readEntity(TYPE_LIST_VALUE)).containsExactly(VALUE);
	}

	@Test
	public void find_with_name()
	{
		var t = request("find");
		var n = new NamedValue(VALUE.id, VALUE.name);
		var req = Entity.json(new PeopleFindRequest(List.of(VALUE.name), null, null));

		sessionDao.current(SESSION);
		assertThat(t.post(req, TYPE_LIST_NAMED)).containsExactly(n);

		sessionDao.current(ADMIN);
		assertThat(t.post(req, TYPE_LIST_NAMED)).containsExactly(n);
	}

	@Test
	public void find_with_phone()
	{
		var t = request("find");
		var n = new NamedValue(VALUE.id, VALUE.name);
		var req = Entity.json(new PeopleFindRequest(List.of(), List.of(VALUE.phone, "888-minimal"), null));

		sessionDao.current(SESSION);
		assertThat(t.post(req, TYPE_LIST_NAMED)).containsExactly(n);

		sessionDao.current(ADMIN);
		assertThat(t.post(req, TYPE_LIST_NAMED)).containsExactly(n);
	}

	@Test
	public void find_z_deactivated()
	{
		sessionDao.current(ADMIN);
		dao.findWithException(VALUE.id).setActive(VALUE.active = false);
	}

	@Test
	public void find_z_deactivated_check()
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
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, get(VALUE.id + "INVALID").getStatus(), "Status");
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

	@Test
	public void remove_as_person()
	{
		sessionDao.current(SESSION);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(VALUE.id).delete().getStatus());

		sessionDao.current(ADMIN);
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
			arguments(new PeopleFilter(1, 20).withHasSexId(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasLatitude(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasLongitude(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasLocationName(false), 1L),
			arguments(new PeopleFilter(1, 20).withAlertable(VALUE.alertable), 1L),
			arguments(new PeopleFilter(1, 20).withActive(VALUE.active), 1L),
			arguments(new PeopleFilter(1, 20).withHasAuthAt(true), 1L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hours(AUTH_AT, -1)), 1L),
			arguments(new PeopleFilter(1, 20).withAuthAtTo(hours(AUTH_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hours(AUTH_AT, -1)).withAuthAtTo(hours(AUTH_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withHasPhoneVerifiedAt(true), 1L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hours(PHONE_VERIFIED_AT, -1)), 1L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtTo(hours(PHONE_VERIFIED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hours(PHONE_VERIFIED_AT, -1)).withPhoneVerifiedAtTo(hours(PHONE_VERIFIED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withHasEmailVerifiedAt(true), 1L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hours(EMAIL_VERIFIED_AT, -1)), 1L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtTo(hours(EMAIL_VERIFIED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hours(EMAIL_VERIFIED_AT, -1)).withEmailVerifiedAtTo(hours(EMAIL_VERIFIED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withAlertedOf(ALERTED_OF), 1L),
			arguments(new PeopleFilter(1, 20).withHasAlertedOf(true), 1L),
			arguments(new PeopleFilter(1, 20).withAlertedOfFrom(ALERTED_OF - 1), 1L),
			arguments(new PeopleFilter(1, 20).withAlertedOfTo(ALERTED_OF + 1), 1L),
			arguments(new PeopleFilter(1, 20).withHasAlertedAt(true), 1L),
			arguments(new PeopleFilter(1, 20).withAlertedAtFrom(hours(ALERTED_AT, -1)), 1L),
			arguments(new PeopleFilter(1, 20).withAlertedAtTo(hours(ALERTED_AT, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withAlertedAtFrom(hours(ALERTED_AT, -1)).withAlertedAtTo(hours(ALERTED_AT, 1)), 1L),
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
			arguments(new PeopleFilter(1, 20).withHasSexId(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasLatitude(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasLongitude(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasLocationName(true), 0L),
			arguments(new PeopleFilter(1, 20).withAlertable(!VALUE.alertable), 0L),
			arguments(new PeopleFilter(1, 20).withActive(!VALUE.active), 0L),
			arguments(new PeopleFilter(1, 20).withHasAuthAt(false), 0L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hours(AUTH_AT, 1)), 0L),
			arguments(new PeopleFilter(1, 20).withAuthAtTo(hours(AUTH_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hours(AUTH_AT, 1)).withAuthAtTo(hours(AUTH_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withHasPhoneVerifiedAt(false), 0L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hours(PHONE_VERIFIED_AT, 1)), 0L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtTo(hours(PHONE_VERIFIED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hours(PHONE_VERIFIED_AT, 1)).withPhoneVerifiedAtTo(hours(PHONE_VERIFIED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withHasEmailVerifiedAt(false), 0L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hours(EMAIL_VERIFIED_AT, 1)), 0L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtTo(hours(EMAIL_VERIFIED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hours(EMAIL_VERIFIED_AT, 1)).withEmailVerifiedAtTo(hours(EMAIL_VERIFIED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withAlertedOf(ALERTED_OF + 1000), 0L),
			arguments(new PeopleFilter(1, 20).withHasAlertedOf(false), 0L),
			arguments(new PeopleFilter(1, 20).withAlertedOfFrom(ALERTED_OF + 1), 0L),
			arguments(new PeopleFilter(1, 20).withAlertedOfTo(ALERTED_OF - 1), 0L),
			arguments(new PeopleFilter(1, 20).withHasAlertedAt(false), 0L),
			arguments(new PeopleFilter(1, 20).withAlertedAtFrom(hours(ALERTED_AT, 1)), 0L),
			arguments(new PeopleFilter(1, 20).withAlertedAtTo(hours(ALERTED_AT, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withAlertedAtFrom(hours(ALERTED_AT, 1)).withAlertedAtTo(hours(ALERTED_AT, -1)), 0L),
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

	@Test
	public void search_as_person()
	{
		sessionDao.current(SESSION);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request("search").post(Entity.json(new PeopleFilter())).getStatus());

		sessionDao.current(ADMIN);
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
		count(new PeopleFilter().withId(VALUE.id), 0L);
		count(new PeopleFilter().withHasEmail(true), 0L);
	}

	@Test
	public void z_00()
	{
		// Non-admin tests
		sessionDao.clear();
	}

	@Test
	public void z_00_start()
	{
		Assertions.assertEquals(HTTP_STATUS_OK,
			request("start").post(Entity.json(new StartRequest("888-555-2100", false, false))).getStatus());
	}

	@Test
	public void z_00_start_count()
	{
		sessionDao.current(ADMIN);

		var response = registrations("search").post(Entity.json(new RegistrationFilter()));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS_);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(1L, results.total, "Check results.total");
		assertThat(results.records).as("Check results.records").hasSize(1);
		Assertions.assertNull(results.records.get(0).person, "Check results.records.0.person");	// V1 registration.

		REGISTRATION = results.records.get(0);
	}

	@Test
	public void z_00_start_remove()
	{
		var response = registrations(REGISTRATION.key).delete();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
	}

	@Test
	public void z_00_start_remove_search()
	{
		var response = registrations("search").post(Entity.json(new RegistrationFilter()));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS_);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertNull(results.records, "Check results.records");
		Assertions.assertEquals(0L, results.total, "Check results.total");
	}

	@Test
	public void z_05()
	{
		// Non-admin tests
		sessionDao.clear();
	}

	@Test
	public void z_05_register()
	{
		var response = request("start").post(Entity.json(new StartRequest("888-555-2000", false, true)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status: start");

		response = request("confirm").post(Entity.json(new StartResponse("+18885552000", null, code())));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status: confirm");
		var session = response.readEntity(SessionValue.class);
		Assertions.assertNotNull(session, "Exists: session");
		Assertions.assertNotNull(session.registration, "Exists: session.registration");
		Assertions.assertNull(session.person, "Exists: session.person");

		response = request("register").post(Entity.json(VALUE = PeopleDAOTest.createValid()));
		Assertions.assertEquals(HTTP_STATUS_AUTHENTICATE, response.getStatus(), "Status: register - fail");

		var now = new Date();
		sessionDao.current(session);
		response = request("register").post(Entity.json(VALUE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status: register - success");
		var registered = SESSION = response.readEntity(SessionValue.class);
		Assertions.assertNotNull(registered, "Exists: registered");
		Assertions.assertNull(registered.registration, "Exists: registered.registration");
		Assertions.assertNotNull(registered.person, "Exists: registered.person");
		Assertions.assertNotNull(registered.person.id, "Check registered.person.id");
		assertThat(registered.person.name).as("Check registered.person.name").isNotNull().isEqualTo(VALUE.name);
		Assertions.assertEquals("+18885552000", registered.person.phone, "Check registered.person.phone");
		Assertions.assertNotEquals(VALUE.phone, registered.person.phone, "Phone fixed");
		Assertions.assertTrue(registered.person.active, "Check registered.person.active");
		assertThat(registered.person.authAt).as("Check registered.person.authAt").isCloseTo(now, 200L);
		assertThat(registered.person.phoneVerifiedAt).as("Check registered.person.phoneVerifiedAt").isCloseTo(now, 200L);
		Assertions.assertNull(registered.person.emailVerifiedAt, "Check registered.person.emailVerifiedAt");
		assertThat(registered.person.createdAt).as("Check registered.person.createdAt").isCloseTo(now, 200L);
		assertThat(registered.person.updatedAt).as("Check registered.person.updatedAt").isCloseTo(now, 200L);

		VALUE.id = registered.person.id;
	}

	@Test
	public void z_06_authenticate_fail()
	{
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, request("auth").post(Entity.json(new AuthRequest("888-555-2001"))).getStatus());
	}

	@Test
	public void z_06_authenticate_success()
	{
		var response = request("auth").post(Entity.json(new AuthRequest("888-555-2000")));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		response = request("auth").put(Entity.json(new AuthResponse("+18885552000", null, token(), true)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());
		var session = response.readEntity(SessionValue.class);
		Assertions.assertNotNull(session, "Exists: session");
		Assertions.assertNull(session.registration, "Exists: session.registration");
		Assertions.assertNotNull(session.person, "Exists: session.person");
		Assertions.assertEquals("+18885552000", session.person.phone, "Check session.person.phone");
		Assertions.assertNotEquals(SESSION.id, session.id, "New session");
	}

	@Test
	public void z_07_add()
	{
		sessionDao.current(SESSION_1 = sessionDao.add(dao.add(new PeopleValue("second", "+18885552001", true).withAuthAt(utc(2020, 4, 7))), false));
	}

	@Test
	public void z_07_find()
	{
		assertThat(request(target().queryParam("name", VALUE.name)).get(TYPE_LIST_VALUE)).containsExactly(SESSION_1.person);	// Ignores the search parameter and returns only the curreht application user.
	}

	@Test
	public void z_07_get()
	{
		Assertions.assertEquals(SESSION_1.person, request(VALUE.id).get(PeopleValue.class));
	}

	@Test
	public void z_07_modify()
	{
		LAST_UPDATED_AT = new Date();
		Assertions.assertEquals(HTTP_STATUS_OK,
			request().put(Entity.json(new PeopleValue("third", "+18885552002", false).withId(VALUE.id).withAuthAt(utc(2020, 4, 8)))).getStatus());
	}

	@Test
	public void z_07_modify_check()
	{
		var session = sessionDao.find(SESSION_1.id);
		Assertions.assertNotNull(session, "session Exists");
		Assertions.assertNotNull(session.person, "Check session.person");
		Assertions.assertEquals(SESSION_1.person.id, session.person.id, "Check session.person.id");
		Assertions.assertEquals("third", session.person.name, "Check session.person.name");
		Assertions.assertEquals("+18885552002", session.person.phone, "Check session.person.phone");
		Assertions.assertTrue(session.person.active, "Check session.person.active");	// Ignored if non-admin.
		Assertions.assertEquals(utc(2020, 4, 7), session.person.authAt, "Check session.person.authAt");	// Ignored if non-admin.
		assertThat(session.person.updatedAt).as("Check session.person.updatedAt").isCloseTo(LAST_UPDATED_AT, 1000L).isAfterOrEqualsTo(session.person.createdAt);

		var value = dao.getById(SESSION_1.person.id);
		Assertions.assertNotNull(value, "value Exists");
		Assertions.assertEquals(SESSION_1.person.id, value.id, "Check value.id");
		Assertions.assertEquals("third", value.name, "Check value.name");
		Assertions.assertEquals("+18885552002", value.phone, "Check value.phone");
		Assertions.assertTrue(value.active, "Check session.person.active");	// Ignored if non-admin.
		Assertions.assertEquals(utc(2020, 4, 7), value.authAt, "Check session.person.authAt");	// Ignored if non-admin.
		assertThat(value.updatedAt).as("Check value.updatedAt").isCloseTo(LAST_UPDATED_AT, 1000L).isAfterOrEqualsTo(session.person.createdAt);
	}

	@Test
	public void z_08_modify()
	{
		sessionDao.current(ADMIN);

		LAST_UPDATED_AT = new Date();
		Assertions.assertEquals(HTTP_STATUS_OK,
			request().put(Entity.json(new PeopleValue("second", "+18885552001", false).withId(SESSION_1.person.id).withAuthAt(utc(2020, 4, 8)))).getStatus());
	}

	@Test
	public void z_08_modify_check()
	{
		var value = dao.getById(SESSION_1.person.id);
		Assertions.assertEquals("second", value.name, "Check value.name");
		Assertions.assertEquals("+18885552001", value.phone, "Check value.phone");
		Assertions.assertFalse(value.active, "Check session.person.active");
		Assertions.assertEquals(utc(2020, 4, 8), value.authAt, "Check session.person.authAt");
	}

	@Test
	public void z_09_remove()
	{
		sessionDao.current(SESSION_1);

		Assertions.assertTrue(sessionDao.exists(SESSION_1.id), "Check exists");
		Assertions.assertNotNull(dao.getById(SESSION_1.person.id), "Check getById");

		Assertions.assertEquals(HTTP_STATUS_OK, request().delete().getStatus());
	}

	@Test
	public void z_09_remove_check()
	{
		Assertions.assertNull(dao.getById(SESSION_1.person.id), "Check getById");
		Assertions.assertFalse(sessionDao.exists(SESSION_1.id), "Check exists");
	}

	@Test
	public void z_09_z_done()
	{
		sessionDao.current(ADMIN);

		var response = registrations("search").post(Entity.json(new RegistrationFilter()));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS_);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertNull(results.records, "Check results.records");
		Assertions.assertEquals(0L, results.total, "Check results.total");
	}

	@Test
	public void z_10_start()	// V2 start
	{
		// Non-admin tests
		sessionDao.clear();

		Assertions.assertEquals(HTTP_STATUS_OK,
			request("start").put(Entity.json(new PeopleValue("lenny", "888-555-2200", false))).getStatus());
	}

	@Test
	public void z_10_start_check()
	{
		sessionDao.current(ADMIN);

		var response = registrations("search").post(Entity.json(new RegistrationFilter()));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS_);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(1L, results.total, "Check results.total");
		assertThat(results.records).as("Check results.records").isNotNull().hasSize(1);
		Assertions.assertNotNull(results.records.get(0).person, "Check results.records.0.person");
		Assertions.assertEquals("+18885552200", results.records.get(0).person.phone, "Check results.records.0.person.phone");
	}

	@Test
	public void z_11_confirm_fail()
	{
		// Non-admin tests
		sessionDao.clear();

		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION,
			request("confirm").put(Entity.json(new StartResponse("+18885552200", null, "INVALID"))).getStatus());
	}

	@Test
	public void z_11_confirm_fail_check()
	{
		z_10_start_check();	// No change
	}

	@Test
	public void z_11_confirm_success()
	{
		// Non-admin tests
		sessionDao.clear();

		var response = request("confirm").put(Entity.json(new StartResponse("+18885552200", null, code())));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		SESSION_1 = response.readEntity(SessionValue.class);
		Assertions.assertNotNull(SESSION_1, "Exists");
		Assertions.assertEquals(SessionValue.DURATION_SHORT, SESSION_1.duration, "Check duration");

		var now = new Date();
		var value = SESSION_1.person;
		Assertions.assertNotNull(value, "Check person");
		assertThat(value.id).as("Check person.id").isNotNull().hasSize(6).isNotEqualTo("doesNotMatter");
		Assertions.assertEquals("lenny", value.name, "Check person.name");
		Assertions.assertEquals("+18885552200", value.phone, "Check person.phone");
		Assertions.assertTrue(value.active, "Check person.active");
		assertThat(value.authAt).as("Check person.authAt").isNotNull().isEqualTo(value.phoneVerifiedAt).isCloseTo(now, 500L);
		assertThat(value.createdAt).as("Check person.createdAt").isNotNull().isEqualTo(value.updatedAt).isCloseTo(now, 500L);
	}

	@Test
	public void z_11_confirm_success_check()
	{
		sessionDao.current(ADMIN);

		var response = registrations("search").post(Entity.json(new RegistrationFilter()));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS_);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(0L, results.total, "Check results.total");
		assertThat(results.records).as("Check results.records").isNull();
	}

	@Test
	public void z_11_confirm_success_get()
	{
		sessionDao.current(SESSION_1);

		var response = request("doesNotMatter").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var now = new Date();
		var value = response.readEntity(PeopleValue.class);
		Assertions.assertNotNull(value, "Exists");
		assertThat(value.id).as("Check ID").isNotNull().hasSize(6).isNotEqualTo("doesNotMatter");
		Assertions.assertEquals("lenny", value.name, "Check name");
		Assertions.assertEquals("+18885552200", value.phone, "Check phone");
		Assertions.assertTrue(value.active, "Check active");
		assertThat(value.authAt).as("Check authAt").isNotNull().isEqualTo(value.phoneVerifiedAt).isCloseTo(now, 1000L);
		assertThat(value.createdAt).as("Check createdAt").isNotNull().isEqualTo(value.updatedAt).isCloseTo(now, 1000L);
	}

	@Test
	public void z_11_start()	// Perform with remember-me session.
	{
		// Non-admin tests
		sessionDao.clear();

		Assertions.assertEquals(HTTP_STATUS_OK,
			request("start").put(Entity.json(new PeopleValue("jenny", "888-555-2201", false))).getStatus());

		var response = request(target().path("confirm").queryParam("rememberMe", true)).put(Entity.json(new StartResponse("+18885552201", null, code())));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var session = response.readEntity(SessionValue.class);
		Assertions.assertNotNull(session, "Exists");
		Assertions.assertEquals(SessionValue.DURATION_LONG, session.duration, "Check duration");

		var now = new Date();
		var value = session.person;
		Assertions.assertNotNull(value, "Check person");
		assertThat(value.id).as("Check person.id").isNotNull().hasSize(6).isNotEqualTo("doesNotMatter");
		Assertions.assertEquals("jenny", value.name, "Check person.name");
		Assertions.assertEquals("+18885552201", value.phone, "Check person.phone");
		Assertions.assertTrue(value.active, "Check person.active");
		assertThat(value.authAt).as("Check person.authAt").isNotNull().isEqualTo(value.phoneVerifiedAt).isCloseTo(now, 500L);
		assertThat(value.createdAt).as("Check person.createdAt").isNotNull().isEqualTo(value.updatedAt).isCloseTo(now, 500L);
	}

	public static Stream<Arguments> z_15_start_fail()
	{
		return Stream.of(
			arguments(new PeopleValue()),
			arguments(new PeopleValue("lenny", "888-555-2201", false)),	// Phone number already exists.
			arguments(new PeopleValue(null, "888-555-2200", false)),
			arguments(new PeopleValue("lenny", "aaa-aaa-aaaa", false)),
			arguments(new PeopleValue("lenny", null, false)));
	}

	@ParameterizedTest
	@MethodSource
	public void z_15_start_fail(final PeopleValue value)
	{
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, request("start").put(Entity.json(value)).getStatus());
	}

	private String code()
	{
		var body = LAST_SMS_RESPONSE.body;
		var i = body.indexOf("code=") + 5;

		return body.substring(i);
	}

	private String token()
	{
		var body = LAST_SMS_RESPONSE.body;
		var i = body.indexOf("token=") + 6;

		return body.substring(i);
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }
	private WebTarget registration() { return RULE.client().target(REGISTRATIONS); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
	private Invocation.Builder registrations(final String path) { return request(registration().path(path)); }

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
		Assertions.assertEquals(expected.sexId, value.sexId, assertId + "Check sexId");
		Assertions.assertEquals(expected.healthWorkerStatusId, value.healthWorkerStatusId, assertId + "Check healthWorkerStatusId");
		if (null == expected.latitude)
			Assertions.assertNull(value.latitude, assertId + "Check latitude");
		else
			assertThat(value.latitude).as(assertId + "Check latitude").isEqualByComparingTo(expected.latitude);
		if (null == expected.longitude)
			Assertions.assertNull(value.longitude, assertId + "Check longitude");
		else
			assertThat(value.longitude).as(assertId + "Check longitude").isEqualByComparingTo(expected.longitude);
		Assertions.assertEquals(expected.locationName, value.locationName, assertId + "Check locationName");
		Assertions.assertEquals(expected.alertable, value.alertable, assertId + "Check alertable");
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
		Assertions.assertEquals(expected.alertedOf, value.alertedOf, assertId + "Check alertedOf");
		if (null == expected.alertedAt)
			Assertions.assertNull(value.alertedAt, assertId + "Check alertedAt");
		else
			assertThat(value.alertedAt).as(assertId + "Check alertedAt").isCloseTo(expected.alertedAt, 500L);
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
