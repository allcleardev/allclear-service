package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static app.allclear.testing.TestingUtils.*;

import java.util.*;
import java.util.stream.Stream;
import javax.ws.rs.client.*;

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

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new FacilityResource(dao, sessionDao, map)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/facilities";

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new FacilityDAO(factory);
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
		Assertions.assertEquals(HTTP_STATUS_OK, request().post(Entity.json(new FacilityValue(i, city, state, 45, 45, active))).getStatus());
	}

	@Test
	public void check()
	{
		sessionDao.clear();
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
	public void getByName_invalid()
	{
		var response = request(target().path("name").queryParam("name", "Test Center 15231")).get();
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, response.getStatus(), "Status");
	}

	public static Stream<Arguments> getDistinctCities()
	{
		return Stream.of(
			arguments("Alabama", List.of("bessemer", "Birmingham", "butler", "Centreville", "Columbiana", "dora", "Fultondale", "Hoover", "Jasper", "midfield", "mountain", "Mountain Brook", "Pell City", "south", "Springville", "Trussville", "Tuscaloosa", "vestavia", "Vestavia Hills", "winfield", "woodlawn", "Woodstock")),
			arguments("Connecticut", List.of("new haven", "orange", "Southbury", "stamford")),
			arguments("Massachusetts", List.of("Cambridge", "Jamaica Plain")),
			arguments("Washington", List.of("burien", "Lake Forest Park", "New York", "Seattle", "shoreline", "Washington")),
			arguments("Washington DC", List.of()));	// Washington DC facilities are deactivated for test.
	}

	@ParameterizedTest
	@MethodSource
	public void getDistinctCities(final String state, final List<String> expected)
	{
		assertThat(request(target().path("cities").queryParam("state", state)).get(String[].class)).containsAll(expected);
	}

	@Test
	public void getDistinctStates()
	{
		assertThat(request("states").get(String[].class)).containsExactly("Alabama", "Connecticut", "Massachusetts", "Washington");
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
