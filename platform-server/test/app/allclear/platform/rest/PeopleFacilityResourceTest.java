package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static app.allclear.testing.TestingUtils.*;
import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.stream.IntStream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import com.azure.storage.queue.QueueClient;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.CountResults;
import app.allclear.platform.App;
import app.allclear.platform.Config;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.value.*;
import app.allclear.twilio.client.TwilioClient;

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
public class PeopleFacilityResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static final Config conf = ConfigTest.loadTest();

	private static PeopleDAO dao = null;
	private static FacilityDAO facilityDao = null;
	private static final FakeRedisClient redis = new FakeRedisClient();
	private static final TwilioClient twilio = mock(TwilioClient.class);
	private static final SessionDAO sessionDao = new SessionDAO(redis, twilio, conf);
	private static final RegistrationDAO registrationDao = new RegistrationDAO(redis, twilio, conf);
	private static PeopleValue VALUE = null;
	private static List<FacilityValue> FACILITIES = null;
	private static SessionValue ADMIN;
	private static SessionValue SESSION;

	private static List<FacilityValue> facilities(final int... indices)
	{
		return Arrays.stream(indices).mapToObj(i -> FACILITIES.get(i)).collect(toList());
	}

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthenticationExceptionMapper())
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new PeopleResource(dao, registrationDao, sessionDao, mock(QueueClient.class)))
		.addResource(new RegistrationResource(registrationDao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/peoples";

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new PeopleDAO(factory);
		facilityDao = new FacilityDAO(factory);
	}

	@Test
	public void add()
	{
		ADMIN = sessionDao.add(new AdminValue("admin"), false);

		var value = dao.add(VALUE = new PeopleValue("min", "888-555-1000", true));
		Assertions.assertNotNull(value, "Exists");
		SESSION = sessionDao.add(VALUE, false);

		FACILITIES = IntStream.range(0, 25).mapToObj(i -> facilityDao.add(new FacilityValue(i))).collect(toList());
		assertThat(FACILITIES).as("Check facilities").hasSize(25);
	}

	@Test
	public void addFacilities_fail_as_admin()
	{
		sessionDao.current(ADMIN);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request("facilities").post(Entity.json(List.of(1L, 4L, 7L))).getStatus());
	}

	@Test
	public void addFacilities_fail_as_person()
	{
		sessionDao.current(SESSION);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(VALUE.id + "/facilities").post(Entity.json(List.of(10L, 13L, 16L))).getStatus());
	}

	@Test
	public void addFacilities_success()
	{
		Assertions.assertNull(dao.getById(VALUE.id).facilities);
	}

	@Test
	public void addFacilities_success_as_admin()
	{
		sessionDao.current(ADMIN);

		var response = request(VALUE.id + "/facilities").post(Entity.json(List.of(1L, 4L, 7L)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(CountResults.class);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(3, results.count, "Check results");
	}

	@Test
	public void addFacilities_success_as_admin_check()
	{
		Assertions.assertEquals(facilities(0, 3, 6), dao.getById(VALUE.id).facilities);
	}

	@Test
	public void addFacilities_success_as_person()
	{
		sessionDao.current(SESSION);

		var response = request("facilities").post(Entity.json(List.of(10L, 13L, 16L)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(CountResults.class);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(3, results.count, "Check results");
	}

	@Test
	public void addFacilities_success_as_person_check()
	{
		Assertions.assertEquals(facilities(0, 3, 6, 9, 12, 15), dao.getById(VALUE.id).facilities);
	}

	@Test
	public void removeFacilities_fail_as_admin()
	{
		sessionDao.current(ADMIN);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request("facilities").method(HttpMethod.DELETE, Entity.json(List.of(1L, 4L, 7L))).getStatus());
	}

	@Test
	public void removeFacilities_fail_as_person()
	{
		sessionDao.current(SESSION);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(VALUE.id + "/facilities").method(HttpMethod.DELETE, Entity.json(List.of(10L, 13L, 16L))).getStatus());
	}

	@Test
	public void removeFacilities_success()
	{
		Assertions.assertEquals(facilities(0, 3, 6, 9, 12, 15), dao.getById(VALUE.id).facilities);
	}

	@Test
	public void removeFacilities_success_as_admin()
	{
		sessionDao.current(ADMIN);

		var response = request(VALUE.id + "/facilities").method(HttpMethod.DELETE, Entity.json(List.of(1L, 4L, 7L)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(CountResults.class);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(3, results.count, "Check results");
	}

	@Test
	public void removeFacilities_success_as_admin_check()
	{
		Assertions.assertEquals(facilities(9, 12, 15), dao.getById(VALUE.id).facilities);
	}

	@Test
	public void removeFacilities_success_as_person()
	{
		sessionDao.current(SESSION);

		var response = request("facilities").method(HttpMethod.DELETE, Entity.json(List.of(10L, 13L, 16L)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(CountResults.class);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(3, results.count, "Check results");
	}

	@Test
	public void removeFacilities_success_as_person_check()
	{
		Assertions.assertNull(dao.getById(VALUE.id).facilities);
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true).target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
