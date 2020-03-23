package app.allclear.common.hibernate;

import static app.allclear.testing.TestingUtils.*;

import javax.ws.rs.client.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import app.allclear.common.entity.Country;
import app.allclear.common.mediatype.UTF8MediaType;

/** Represents a Dropwizard & Hibernate Integration test.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class IntegrationTest
{
	public static final DropwizardAppExtension<DropwizardConfig> APP = new DropwizardAppExtension<>(DropwizardApp.class, "test-res/dataSource.json");

	private static WebTarget countryResource;

	@BeforeAll
	public static void beforeAll() throws Exception
	{
		var ds = APP.getConfiguration().dataSource.build(
			APP.getEnvironment().metrics(), "migrations");
		try (var connection = ds.getConnection())
		{
			var migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
			migrator.dropAll();
			migrator.update("");
		}

		var home = ClientBuilder.newClient().target(String.format("http://127.0.0.1:%d", APP.getLocalPort()));
		countryResource = home.path("countries");
	}

	@Test
	public void add()
	{
		var response = countryResource.request(UTF8MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new Country("00", "First Country", "aaa", "000", false)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var record = response.readEntity(Country.class);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals("First Country", record.name, "Check name");
		Assertions.assertEquals("aaa", record.code, "Check code");
		Assertions.assertEquals("000", record.numCode, "Check numCode");
		Assertions.assertFalse(record.active, "Check active");
	}

	@Test
	public void find()
	{
		var response = countryResource.path("00").request(UTF8MediaType.APPLICATION_JSON_TYPE).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var record = response.readEntity(Country.class);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals("First Country", record.name, "Check name");
		Assertions.assertEquals("aaa", record.code, "Check code");
		Assertions.assertEquals("000", record.numCode, "Check numCode");
		Assertions.assertFalse(record.active, "Check active");
	}

	@ParameterizedTest
	@CsvFileSource(resources="/seed/country.csv", numLinesToSkip=1)
	public void find(final String id, final String name, final String code, final String numCode, final boolean active)
	{
		var response = countryResource.path(id).request(UTF8MediaType.APPLICATION_JSON_TYPE).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var record = response.readEntity(Country.class);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(name, record.name, "Check name");
		Assertions.assertEquals(code, record.code, "Check code");
		Assertions.assertEquals(numCode, record.numCode, "Check numCode");
		Assertions.assertEquals(active, record.active, "Check active");
	}
}
