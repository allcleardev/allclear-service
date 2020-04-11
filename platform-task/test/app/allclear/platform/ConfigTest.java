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

	public static Config loadTest()
	{
		try { return load("test"); }
		catch (final RuntimeException ex) { throw ex; }
		catch (final Exception ex) { throw new RuntimeException(ex); }
	}

	public static Config load(final String name) throws Exception
	{
		return mapper.readValue(new EnvironmentVariableSubstitutor().replace(
				new TextStringBuilder(IOUtils.toString(new FileInputStream("conf/" + name + ".json"), Charset.defaultCharset()))), Config.class);
	}

	@Test
	public void dev() throws Exception
	{
		var o = load("dev");
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("dev", o.env, "Check env");
		Assertions.assertFalse(o.disableSwagger, "Check disableSwagger");
		Assertions.assertEquals("https://app-dev.allclear.app", o.baseUrl, "Check baseUrl");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertEquals("+16466321488", o.authenticationPhone, "Check authenticationPhone");

		Assertions.assertEquals("com.mysql.jdbc.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertEquals("allclear", o.trans.getUser(), "Check trans.user");
		Assertions.assertEquals("allclearpwd", o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:mysql://allclear-dev.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(2, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(8, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertNull(o.trans.getReadOnlyByDefault(), "Check trans.readOnlyByDefault");
		Assertions.assertTrue(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertTrue(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check trans.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));

		Assertions.assertEquals("com.mysql.jdbc.Driver", o.read.getDriverClass(), "Check read.driverClass");
		Assertions.assertEquals("allclear", o.read.getUser(), "Check read.user");
		Assertions.assertEquals("allclearpwd", o.read.getPassword(), "Check read.password");
		Assertions.assertEquals("jdbc:mysql://allclear-dev.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.read.getUrl(), "Check read.url");
		Assertions.assertEquals(Duration.seconds(1L), o.read.getMaxWaitForConnection(), "Check read.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.read.getValidationQuery(), "Check read.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.read.getValidationQueryTimeout(), "Check read.validationQueryTimeout");
		Assertions.assertEquals(2, o.read.getMinSize(), "Check read.minSize");
		Assertions.assertEquals(8, o.read.getMaxSize(), "Check read.maxSize");
		Assertions.assertNull(o.read.getReadOnlyByDefault(), "Check read.readOnlyByDefault");
		Assertions.assertTrue(o.read.getCheckConnectionWhileIdle(), "Check read.checkConnectionWhileIdle");
		Assertions.assertTrue(o.read.getCheckConnectionOnBorrow(), "Check read.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.read.getDefaultTransactionIsolation(), "Check read.defaultTransactionIsolation");
		assertThat(o.read.getProperties()).as("Check read.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));
	}

	@Test
	public void local() throws Exception
	{
		var o = load("local");
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("local", o.env, "Check env");
		Assertions.assertFalse(o.disableSwagger, "Check disableSwagger");
		Assertions.assertEquals("http://localhost:8080", o.baseUrl, "Check baseUrl");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertEquals("+16466321488", o.authenticationPhone, "Check authenticationPhone");

		Assertions.assertEquals("com.mysql.jdbc.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertEquals("allclear", o.trans.getUser(), "Check trans.user");
		Assertions.assertEquals("allclear", o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:mysql://localhost:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(1, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(10, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertNull(o.trans.getReadOnlyByDefault(), "Check trans.readOnlyByDefault");
		Assertions.assertTrue(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertTrue(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check trans.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));

		Assertions.assertEquals("com.mysql.jdbc.Driver", o.read.getDriverClass(), "Check read.driverClass");
		Assertions.assertEquals("allclear", o.read.getUser(), "Check read.user");
		Assertions.assertEquals("allclear", o.read.getPassword(), "Check read.password");
		Assertions.assertEquals("jdbc:mysql://localhost:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC",  o.read.getUrl(), "Check read.url");
		Assertions.assertEquals(Duration.seconds(1L), o.read.getMaxWaitForConnection(), "Check read.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.read.getValidationQuery(), "Check read.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.read.getValidationQueryTimeout(), "Check read.validationQueryTimeout");
		Assertions.assertEquals(1, o.read.getMinSize(), "Check read.minSize");
		Assertions.assertEquals(10, o.read.getMaxSize(), "Check read.maxSize");
		Assertions.assertNull(o.read.getReadOnlyByDefault(), "Check read.readOnlyByDefault");
		Assertions.assertTrue(o.read.getCheckConnectionWhileIdle(), "Check read.checkConnectionWhileIdle");
		Assertions.assertTrue(o.read.getCheckConnectionOnBorrow(), "Check read.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.read.getDefaultTransactionIsolation(), "Check read.defaultTransactionIsolation");
		assertThat(o.read.getProperties()).as("Check read.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));
	}

	@Test
	public void prod() throws Exception
	{
		var o = load("prod");
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("prod", o.env, "Check env");
		Assertions.assertFalse(o.disableSwagger, "Check disableSwagger");
		Assertions.assertEquals("https://app.allclear.app", o.baseUrl, "Check baseUrl");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertEquals("+16466321488", o.authenticationPhone, "Check authenticationPhone");

		Assertions.assertEquals("com.mysql.jdbc.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertEquals("allclear", o.trans.getUser(), "Check trans.user");
		Assertions.assertEquals("allclearpwd", o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:mysql://allclear-prod.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(2, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(10, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertNull(o.trans.getReadOnlyByDefault(), "Check trans.readOnlyByDefault");
		Assertions.assertTrue(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertTrue(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check trans.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));

		Assertions.assertEquals("com.mysql.jdbc.Driver", o.read.getDriverClass(), "Check read.driverClass");
		Assertions.assertEquals("allclear", o.read.getUser(), "Check read.user");
		Assertions.assertEquals("allclearpwd", o.read.getPassword(), "Check read.password");
		Assertions.assertEquals("jdbc:mysql://ro-allclear-prod.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.read.getUrl(), "Check read.url");
		Assertions.assertEquals(Duration.seconds(1L), o.read.getMaxWaitForConnection(), "Check read.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.read.getValidationQuery(), "Check read.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.read.getValidationQueryTimeout(), "Check read.validationQueryTimeout");
		Assertions.assertEquals(10, o.read.getMinSize(), "Check read.minSize");
		Assertions.assertEquals(40, o.read.getMaxSize(), "Check read.maxSize");
		Assertions.assertTrue(o.read.getReadOnlyByDefault(), "Check read.readOnlyByDefault");
		Assertions.assertTrue(o.read.getCheckConnectionWhileIdle(), "Check read.checkConnectionWhileIdle");
		Assertions.assertTrue(o.read.getCheckConnectionOnBorrow(), "Check read.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_UNCOMMITTED, o.read.getDefaultTransactionIsolation(), "Check read.defaultTransactionIsolation");
		assertThat(o.read.getProperties()).as("Check read.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));
	}

	@Test
	public void test() throws Exception
	{
		var o = load("test");
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("test", o.env, "Check env");
		Assertions.assertFalse(o.disableSwagger, "Check disableSwagger");
		Assertions.assertEquals("https://app-test.allclear.app", o.baseUrl, "Check baseUrl");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertEquals("+16466321488", o.authenticationPhone, "Check authenticationPhone");

		Assertions.assertEquals("org.h2.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertNull(o.trans.getUser(), "Check trans.user");
		Assertions.assertNull(o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(1, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(10, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertNull(o.trans.getReadOnlyByDefault(), "Check trans.readOnlyByDefault");
		Assertions.assertFalse(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertFalse(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check trans.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.H2Dialect"));

		Assertions.assertEquals("org.h2.Driver", o.read.getDriverClass(), "Check read.driverClass");
		Assertions.assertNull(o.read.getUser(), "Check read.user");
		Assertions.assertNull(o.read.getPassword(), "Check read.password");
		Assertions.assertEquals("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",  o.read.getUrl(), "Check read.url");
		Assertions.assertEquals(Duration.seconds(1L), o.read.getMaxWaitForConnection(), "Check read.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.read.getValidationQuery(), "Check read.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.read.getValidationQueryTimeout(), "Check read.validationQueryTimeout");
		Assertions.assertEquals(1, o.read.getMinSize(), "Check read.minSize");
		Assertions.assertEquals(10, o.read.getMaxSize(), "Check read.maxSize");
		Assertions.assertNull(o.read.getReadOnlyByDefault(), "Check read.readOnlyByDefault");
		Assertions.assertFalse(o.read.getCheckConnectionWhileIdle(), "Check read.checkConnectionWhileIdle");
		Assertions.assertFalse(o.read.getCheckConnectionOnBorrow(), "Check read.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.read.getDefaultTransactionIsolation(), "Check read.defaultTransactionIsolation");
		assertThat(o.read.getProperties()).as("Check read.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.H2Dialect"));
	}
}
