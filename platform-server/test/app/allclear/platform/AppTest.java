package app.allclear.platform;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.stream.Stream;
import javax.ws.rs.client.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.HealthResponse;
import app.allclear.platform.type.*;

/** Integration test that starts up application.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/25/2020
 *
 */

// @Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class AppTest
{
	public static final DropwizardAppExtension<Config> APP = new DropwizardAppExtension<>(App.class, "conf/test.json");

	private static WebTarget home;
	private static WebTarget info;
	private static WebTarget people;
	private static WebTarget types;
	
	@BeforeAll
	public static void up() throws Exception
	{
		home = ClientBuilder.newClient().target("http://127.0.0.1:" + APP.getLocalPort());
		info = home.path("info");
		people = home.path("people");
		types = home.path("types");
	}

	public static Stream<Arguments> assets()
	{
		return Stream.of(
			arguments("manager", HTTP_STATUS_OK),
			arguments("managers", HTTP_STATUS_NOT_FOUND),
			arguments("manager/index.html", HTTP_STATUS_OK),
			arguments("managers/index.html", HTTP_STATUS_NOT_FOUND),
			arguments("swagger-ui", HTTP_STATUS_OK),
			arguments("swagger_ui", HTTP_STATUS_NOT_FOUND),
			arguments("swagger-ui/index.html", HTTP_STATUS_OK),
			arguments("swagger_ui/index.html", HTTP_STATUS_NOT_FOUND),
			arguments("swagger.json", HTTP_STATUS_OK));
	}

	@ParameterizedTest
	@MethodSource
	public void assets(final String path, final int status)
	{
		Assertions.assertEquals(status, request(home.path(path)).get().getStatus(), "Check " + path);
	}

	@Test
	public void getConditions()
	{
		assertThat(request(types.path("conditions")).get(Condition[].class)).hasSize(Condition.LIST.size());
	}

	@Test
	public void getExposures()
	{
		assertThat(request(types.path("exposures")).get(Exposure[].class)).hasSize(Exposure.LIST.size());
	}

	@Test
	public void getFacilityTypes()
	{
		assertThat(request(types.path("facilityTypes")).get(FacilityType[].class)).hasSize(FacilityType.LIST.size());
	}

	@Test
	public void getPeopleStatuses()
	{
		assertThat(request(types.path("peopleStatuses")).get(PeopleStatus[].class)).hasSize(PeopleStatus.LIST.size());
	}

	@Test
	public void getPeopleStatures()
	{
		assertThat(request(types.path("peopleStatures")).get(PeopleStature[].class)).hasSize(PeopleStature.LIST.size());
	}

	@Test
	public void getPerson()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, request(people.path("INVALID")).get().getStatus());
	}

	@Test
	public void getSexes()
	{
		assertThat(request(types.path("sexes")).get(Sex[].class)).hasSize(Sex.LIST.size());
	}

	@Test
	public void getSymptoms()
	{
		assertThat(request(types.path("symptoms")).get(Symptom[].class)).hasSize(Symptom.LIST.size());
	}

	@Test
	public void getTestCriteria()
	{
		assertThat(request(types.path("testCriteria")).get(TestCriteria[].class)).hasSize(TestCriteria.LIST.size());
	}

	@Test
	public void healthy()
	{
		var response = request(info.path("health")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(HealthResponse.class);
		Assertions.assertEquals("success", value.status);
		assertThat(value.timestamp).as("Check timestamp").isCloseTo(new Date(), 1000L);
	}

	@Test
	public void home()
	{
		Assertions.assertEquals(HTTP_STATUS_AUTHENTICATE, request(home.path("")).get().getStatus(), "Check empty path");
		Assertions.assertEquals(HTTP_STATUS_AUTHENTICATE, request(home.path("/")).get().getStatus(), "Check / path");
	}

	@Test
	public void ping()
	{
		Assertions.assertEquals(HTTP_STATUS_OK, request(info.path("ping")).get().getStatus());
	}

	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
