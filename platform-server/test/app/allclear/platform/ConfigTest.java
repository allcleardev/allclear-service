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

import app.allclear.common.redis.RedisConfig;
import app.allclear.twilio.client.TwilioConfig;

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
		Assertions.assertEquals("%s\nUse this code for AllClear verification or click https://app-dev.allclear.app/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Click https://app-dev.allclear.app/auth?phone=%s&token=%s to login in.", o.authenticationSMSMessage, "Check authenticationSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.session, "Check session");
		Assertions.assertEquals("allclear-dev.redis.cache.windows.net", o.session.host, "Check session.host");
		Assertions.assertEquals(6380, o.session.port, "Check session.port");
		Assertions.assertEquals(200L, o.session.timeout, "Check session.timeout");
		Assertions.assertEquals(10, o.session.poolSize, "Check session.poolSize");
		Assertions.assertEquals("password", o.session.password, "Check session.password");
		Assertions.assertTrue(o.session.ssl, "Check session.ssl");
		Assertions.assertTrue(o.session.testWhileIdle, "Check session.testWhileIdle");
		Assertions.assertFalse(o.session.test, "Check session.test");
		Assertions.assertNotNull(o.twilio, "Check twilio");
		Assertions.assertEquals(TwilioConfig.BASE_URL, o.twilio.baseUrl, "Check twilio.baseUrl");
		Assertions.assertNotNull(o.twilio.accountId, "Check twilio.accountId");	// Could be the real account ID if the environment variable is set.
		Assertions.assertNotNull(o.twilio.authToken, "Check twilio.authToken");	// Could be the real authorization token if the environment variable is set.
		Assertions.assertEquals("com.mysql.jdbc.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertEquals("allclear", o.trans.getUser(), "Check trans.user");
		Assertions.assertEquals("allclearpwd", o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:mysql://allclear-dev.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.trans.getUrl(), "Check trans.url");
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
		Assertions.assertEquals("%s\nUse this code for AllClear verification or click http://localhost:8080/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Click http://localhost:8080/auth?phone=%s&token=%s to login in.", o.authenticationSMSMessage, "Check authenticationSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.session, "Check session");
		Assertions.assertEquals("localhost", o.session.host, "Check session.host");
		Assertions.assertEquals(RedisConfig.PORT_DEFAULT, o.session.port, "Check session.port");
		Assertions.assertEquals(200L, o.session.timeout, "Check session.timeout");
		Assertions.assertEquals(10, o.session.poolSize, "Check session.poolSize");
		Assertions.assertNull(o.session.password, "Check session.password");
		Assertions.assertFalse(o.session.ssl, "Check session.ssl");
		Assertions.assertTrue(o.session.testWhileIdle, "Check session.testWhileIdle");
		Assertions.assertFalse(o.session.test, "Check session.test");
		Assertions.assertNotNull(o.twilio, "Check twilio");
		Assertions.assertEquals(TwilioConfig.BASE_URL, o.twilio.baseUrl, "Check twilio.baseUrl");
		Assertions.assertEquals("123", o.twilio.accountId, "Check twilio.accountId");
		Assertions.assertEquals("token", o.twilio.authToken, "Check twilio.authToken");
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
	public void prod() throws Exception
	{
		var o = load("prod");
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("prod", o.env, "Check env");
		Assertions.assertFalse(o.disableSwagger, "Check disableSwagger");
		Assertions.assertEquals("https://app.allclear.app", o.baseUrl, "Check baseUrl");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertEquals("+16466321488", o.authenticationPhone, "Check authenticationPhone");
		Assertions.assertEquals("%s\nUse this code for AllClear verification or click https://app.allclear.app/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Click https://app.allclear.app/auth?phone=%s&token=%s to login in.", o.authenticationSMSMessage, "Check authenticationSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.session, "Check session");
		Assertions.assertEquals("allclear-prod.redis.cache.windows.net", o.session.host, "Check session.host");
		Assertions.assertEquals(6380, o.session.port, "Check session.port");
		Assertions.assertEquals(200L, o.session.timeout, "Check session.timeout");
		Assertions.assertEquals(10, o.session.poolSize, "Check session.poolSize");
		Assertions.assertEquals("password", o.session.password, "Check session.password");
		Assertions.assertTrue(o.session.ssl, "Check session.ssl");
		Assertions.assertTrue(o.session.testWhileIdle, "Check session.testWhileIdle");
		Assertions.assertFalse(o.session.test, "Check session.test");
		Assertions.assertNotNull(o.twilio, "Check twilio");
		Assertions.assertEquals(TwilioConfig.BASE_URL, o.twilio.baseUrl, "Check twilio.baseUrl");
		Assertions.assertNotNull(o.twilio.accountId, "Check twilio.accountId");	// Could be the real account ID if the environment variable is set.
		Assertions.assertNotNull(o.twilio.authToken, "Check twilio.authToken");	// Could be the real authorization token if the environment variable is set.
		Assertions.assertEquals("com.mysql.jdbc.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertEquals("allclear", o.trans.getUser(), "Check trans.user");
		Assertions.assertEquals("allclearpwd", o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:mysql://allclear-prod.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(10, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(40, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertTrue(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertTrue(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));
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
		Assertions.assertEquals("%s\nUse this code for AllClear verification or click https://app-test.allclear.app/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Click https://app-test.allclear.app/auth?phone=%s&token=%s to login in.", o.authenticationSMSMessage, "Check authenticationSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.session, "Check session");
		Assertions.assertNull(o.session.host, "Check session.host");
		Assertions.assertEquals(RedisConfig.PORT_DEFAULT, o.session.port, "Check session.port");
		Assertions.assertNull(o.session.timeout, "Check session.timeout");
		Assertions.assertNull(o.session.poolSize, "Check session.poolSize");
		Assertions.assertNull(o.session.password, "Check session.password");
		Assertions.assertFalse(o.session.ssl, "Check session.ssl");
		Assertions.assertTrue(o.session.testWhileIdle, "Check session.testWhileIdle");
		Assertions.assertTrue(o.session.test, "Check session.test");
		Assertions.assertNotNull(o.twilio, "Check twilio");
		Assertions.assertEquals(TwilioConfig.BASE_URL, o.twilio.baseUrl, "Check twilio.baseUrl");
		Assertions.assertEquals("123", o.twilio.accountId, "Check twilio.accountId");
		Assertions.assertEquals("token", o.twilio.authToken, "Check twilio.authToken");
		Assertions.assertEquals("org.h2.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertNull(o.trans.getUser(), "Check trans.user");
		Assertions.assertNull(o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(1, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(10, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertFalse(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertFalse(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.H2Dialect"));
	}
}
