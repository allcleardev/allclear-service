package app.allclear.platform;

import static org.fest.assertions.api.Assertions.assertThat;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import javax.ws.rs.client.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

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

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class IntegrationTest
{
	public static final DropwizardAppExtension<Config> APP = new DropwizardAppExtension<>(App.class, "conf/test.json");

	private static Config conf;
	private static WebTarget home;
	private static WebTarget info;
	private static WebTarget people;
	private static WebTarget types;
	
	@BeforeAll
	public static void up() throws Exception
	{
		conf = APP.getConfiguration();

		try (var conn = conf.trans.build(APP.getEnvironment().metrics(), "migration").getConnection())
		{
			var migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(conn));
			migrator.dropAll();
			migrator.update("");
		}

		home = ClientBuilder.newClient().target("http://127.0.0.1:" + APP.getLocalPort());
		info = home.path("info");
		people = home.path("people");
		types = home.path("types");
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
	public void healthy()
	{
		var response = request(info.path("health")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(HealthResponse.class);
		Assertions.assertEquals("success", value.status);
		assertThat(value.timestamp).as("Check timestamp").isCloseTo(new Date(), 1000L);
	}

	@Test
	public void ping()
	{
		Assertions.assertEquals(HTTP_STATUS_OK, request(info.path("ping")).get().getStatus());
	}

	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
