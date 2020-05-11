package app.allclear.platform.rest;

import static java.util.stream.Collectors.toList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static app.allclear.testing.TestingUtils.*;

import java.util.*;
import java.util.stream.Stream;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.google.client.MapClient;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.entity.CountByName;
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
public class FacilityResourceFacetTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static FacilityDAO dao = null;
	private static SessionDAO sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());
	private static MapClient map = mock(MapClient.class);
	private static final SessionValue ADMIN = new SessionValue(false, new AdminValue("admin"));
	private static final Map<String, List<CountByName>> CITIES = new HashMap<>();

	private static int activeCount = 0;
	private static int inactiveCount = 0;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new FacilityResource(dao, sessionDao, map)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/facilities";

	private static final GenericType<List<CountByName>> TYPE_LIST_COUNT = new GenericType<List<CountByName>>() {};

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new FacilityDAO(factory, new TestAuditor());
	}

	@Test
	public void add()
	{
		sessionDao.current(ADMIN);
	}

	@ParameterizedTest
	@CsvFileSource(resources="/feeds/facility_facets.csv", numLinesToSkip=1)
	public void add_all(final int i, final String city, final String state, final boolean active)
	{
		if (active)
		{
			activeCount++;
			var o = CITIES.computeIfAbsent(state, k -> new LinkedList<CountByName>());
			o.stream().filter(v -> v.name.equalsIgnoreCase(city)).findFirst().ifPresentOrElse(v -> v.total++, () -> o.add(new CountByName(city, 1L)));
		}
		else
			inactiveCount++;

		Assertions.assertEquals(HTTP_STATUS_OK, request().post(Entity.json(new FacilityValue(i, city, state, 45, 45, active))).getStatus());
	}

	@Test
	public void check()
	{
		sessionDao.clear();

		Assertions.assertEquals(4, CITIES.size(), "Check states size");
		Assertions.assertEquals(95, activeCount, "Check activeCount");
		Assertions.assertEquals(5, inactiveCount, "Check inactiveCount");
		Assertions.assertEquals(95L,
			CITIES.entrySet().stream().flatMap(v -> v.getValue().stream()).mapToLong(v -> v.total).sum(), "Cities total");
	}

	public static Stream<Arguments> check_sizes()
	{
		return Stream.of(
			arguments("Alabama", 22),
			arguments("Connecticut", 4),
			arguments("Massachusetts", 2),
			arguments("Washington", 6));
	}

	@ParameterizedTest
	@MethodSource
	public void check_sizes(final String state, final int size)
	{
		Assertions.assertEquals(size, CITIES.get(state).size());
	}

	@Test
	public void getByName()
	{
		var response = request(target().path("name").queryParam("name", "Test Center 15232")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var o = response.readEntity(FacilityValue.class);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("Bessemer", o.city, "Check city");
		Assertions.assertEquals("Alabama", o.state, "Check state");
	}

	@Test
	public void getByName_blank()
	{
		var response = request(target().path("name").queryParam("name", "    ")).get();
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");

		var info = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(info, "Exists");
		Assertions.assertEquals("Please provide a 'name' parameter.", info.message, "Check message");
	}

	@Test
	public void getByName_empty()
	{
		var response = request(target().path("name").queryParam("name", "")).get();
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");

		var info = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(info, "Exists");
		Assertions.assertEquals("Please provide a 'name' parameter.", info.message, "Check message");
	}

	@Test
	public void getByName_invalid()
	{
		var response = request(target().path("name").queryParam("name", "Test Center 15231")).get();
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, response.getStatus(), "Status");
	}

	@Test
	public void getByName_missing()
	{
		var response = request("name").get();
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");

		var info = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(info, "Exists");
		Assertions.assertEquals("Please provide a 'name' parameter.", info.message, "Check message");
	}

	public static Stream<Arguments> getDistinctCities()
	{
		return Stream.of(
			arguments("Alabama", CITIES.get("Alabama").stream().sorted((a, b) -> a.name.compareToIgnoreCase(b.name)).collect(toList())),
			arguments("Connecticut", CITIES.get("Connecticut").stream().sorted((a, b) -> a.name.compareToIgnoreCase(b.name)).collect(toList())),
			arguments("Massachusetts", CITIES.get("Massachusetts").stream().sorted((a, b) -> a.name.compareToIgnoreCase(b.name)).collect(toList())),
			arguments("Washington", CITIES.get("Washington").stream().sorted((a, b) -> a.name.compareToIgnoreCase(b.name)).collect(toList())),
			arguments("Washington DC", List.of()));	// Washington DC facilities are deactivated for test.
	}

	@ParameterizedTest
	@MethodSource
	public void getDistinctCities(final String state, final List<CountByName> expected)
	{
		assertThat(request(target().path("cities").queryParam("state", state)).get(TYPE_LIST_COUNT)).isEqualTo(expected);
	}

	@Test
	public void getDistinctCities_blank()
	{
		var response = request(target().path("cities").queryParam("state", "    ")).get();
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");

		var info = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(info, "Exists");
		Assertions.assertEquals("Please provide a 'state' parameter.", info.message, "Check message");
	}

	@Test
	public void getDistinctCities_empty()
	{
		var response = request(target().path("cities").queryParam("state", "")).get();
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");

		var info = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(info, "Exists");
		Assertions.assertEquals("Please provide a 'state' parameter.", info.message, "Check message");
	}

	@Test
	public void getDistinctCities_missing()
	{
		var response = request("cities").get();
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");

		var info = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(info, "Exists");
		Assertions.assertEquals("Please provide a 'state' parameter.", info.message, "Check message");
	}

	@Test
	public void getDistinctStates()
	{
		assertThat(request("states").get(CountByName[].class))
			.containsExactly(new CountByName("Alabama", 50L),
				new CountByName("Connecticut", 8L),
				new CountByName("Massachusetts", 2L),
				new CountByName("Washington", 35L));
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
