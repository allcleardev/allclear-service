package app.allclear.platform;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.TextStringBuilder;
import org.fest.assertions.data.MapEntry;
import org.junit.jupiter.api.*;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.db.DataSourceFactory.TransactionIsolation;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Unit test class that verifies the Config POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ConfigTest
{
	private static final ObjectMapper mapper = Jackson.newObjectMapper();

	private Config load(final String name) throws Exception
	{
		return mapper.readValue(new EnvironmentVariableSubstitutor().replace(
				new TextStringBuilder(IOUtils.toString(new FileInputStream("conf/" + name + ".json"), Charset.defaultCharset()))), Config.class);
	}

	@Test
	public void local() throws Exception
	{
		var o = load("local");
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("local", o.env, "Check env");
		Assertions.assertFalse(o.disableSwagger, "Check disableSwagger");
		Assertions.assertEquals("com.mysql.jdbc.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertEquals("allclear", o.trans.getUser(), "Check trans.user");
		Assertions.assertEquals("allclear", o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:mysql://localhost:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(1, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(10, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertTrue(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertTrue(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));
	}

	@Test
	public void dev() throws Exception
	{
		var o = load("dev");
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("dev", o.env, "Check env");
		Assertions.assertFalse(o.disableSwagger, "Check disableSwagger");
		Assertions.assertEquals("com.mysql.jdbc.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertEquals("allclear", o.trans.getUser(), "Check trans.user");
		Assertions.assertEquals("allclearpwd", o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:mysql://allcleardb-dev.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(5, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(20, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertTrue(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertTrue(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));
	}
}
