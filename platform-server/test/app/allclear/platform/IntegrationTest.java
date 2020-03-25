package app.allclear.platform;

import static org.fest.assertions.api.Assertions.assertThat;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import javax.ws.rs.client.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.HealthResponse;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

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
	}

	@Test
	public void healthy()
	{
		var response = request(info.path("health")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(HealthResponse.class);
		Assertions.assertEquals("success", value.status);
		assertThat(value.timestamp).as("Check timestamp").isCloseTo(new Date(), 200L);
	}

	@Test
	public void ping()
	{
		Assertions.assertEquals(HTTP_STATUS_OK, request(info.path("ping")).get().getStatus());
	}

	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
