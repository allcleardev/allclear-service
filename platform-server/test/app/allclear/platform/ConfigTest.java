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

import app.allclear.redis.JedisConfig;
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
		Assertions.assertEquals("https://api-dev.allclear.app/manager/index.html", o.adminUrl, "Check adminUrl");
		Assertions.assertNull(o.alertSid, "Check alertSid");
		Assertions.assertEquals("+16466321488", o.alertPhone, "Check alertPhone");
		Assertions.assertNull(o.registrationSid, "Check registrationSid");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertNull(o.authSid, "Check authSid");
		Assertions.assertEquals("+16466321488", o.authPhone, "Check authPhone");
		Assertions.assertEquals("New COVID-19 test locations are available in your area. Click here to view them on AllClear: https://app-dev.allclear.app/alert?lastAlertedAt=%s&phone=%s&token=%s", o.alertSMSMessage, "Check alertSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to register is %s or click this magic link https://app-dev.allclear.app/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to login is %s or click this magic link https://app-dev.allclear.app/auth?phone=%s&token=%s", o.authSMSMessage, "Check authSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		assertThat(o.auditLog).as("Check auditLog").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-audit-dev;AccountKey=").endsWith(";TableEndpoint=https://allclear-audit-dev.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.geocode, "Check geocode");
		Assertions.assertEquals("allclear-dev-mapcache.redis.cache.windows.net", o.geocode.host, "Check geocode.host");
		Assertions.assertEquals(6380, o.geocode.port, "Check geocode.port");
		Assertions.assertEquals(200L, o.geocode.timeout, "Check geocode.timeout");
		Assertions.assertEquals(10, o.geocode.poolSize, "Check geocode.poolSize");
		Assertions.assertEquals("password", o.geocode.password, "Check geocode.password");
		Assertions.assertTrue(o.geocode.ssl, "Check geocode.ssl");
		Assertions.assertTrue(o.geocode.testWhileIdle, "Check geocode.testWhileIdle");
		Assertions.assertFalse(o.geocode.test, "Check geocode.test");
		Assertions.assertEquals(60, o.task, "Check task");
		Assertions.assertEquals(60000L, o.task(), "Check task()");
		assertThat(o.queue).as("Check queue").startsWith("DefaultEndpointsProtocol=https;AccountName=allcleardevqueues;AccountKey=").endsWith(";EndpointSuffix=core.windows.net");
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
		Assertions.assertEquals(5, o.read.getMinSize(), "Check read.minSize");
		Assertions.assertEquals(20, o.read.getMaxSize(), "Check read.maxSize");
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
		Assertions.assertEquals("https://api-local.allclear.app/manager/index.html", o.adminUrl, "Check adminUrl");
		Assertions.assertNull(o.alertSid, "Check alertSid");
		Assertions.assertEquals("+16466321488", o.alertPhone, "Check alertPhone");
		Assertions.assertNull(o.registrationSid, "Check registrationSid");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertNull(o.authSid, "Check authSid");
		Assertions.assertEquals("+16466321488", o.authPhone, "Check authPhone");
		Assertions.assertEquals("New COVID-19 test locations are available in your area. Click here to view them on AllClear: http://localhost:8080/alert?lastAlertedAt=%s&phone=%s&token=%s", o.alertSMSMessage, "Check alertSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to register is %s or click this magic link http://localhost:8080/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to login is %s or click this magic link http://localhost:8080/auth?phone=%s&token=%s", o.authSMSMessage, "Check authSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		assertThat(o.auditLog).as("Check auditLog").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-audit-dev;AccountKey=").endsWith(";TableEndpoint=https://allclear-audit-dev.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.geocode, "Check geocode");
		Assertions.assertEquals("localhost", o.geocode.host, "Check geocode.host");
		Assertions.assertEquals(JedisConfig.PORT_DEFAULT, o.geocode.port, "Check geocode.port");
		Assertions.assertEquals(200L, o.geocode.timeout, "Check geocode.timeout");
		Assertions.assertEquals(10, o.geocode.poolSize, "Check geocode.poolSize");
		Assertions.assertNull(o.geocode.password, "Check geocode.password");
		Assertions.assertFalse(o.geocode.ssl, "Check geocode.ssl");
		Assertions.assertTrue(o.geocode.testWhileIdle, "Check geocode.testWhileIdle");
		Assertions.assertFalse(o.geocode.test, "Check geocode.test");
		Assertions.assertEquals(0, o.task, "Check task");
		Assertions.assertEquals(0L, o.task(), "Check task()");
		assertThat(o.queue).as("Check queue").startsWith("DefaultEndpointsProtocol=https;AccountName=allcleardevqueues;AccountKey=").endsWith(";EndpointSuffix=core.windows.net");
		Assertions.assertNotNull(o.session, "Check session");
		Assertions.assertEquals("localhost", o.session.host, "Check session.host");
		Assertions.assertEquals(JedisConfig.PORT_DEFAULT, o.session.port, "Check session.port");
		Assertions.assertEquals(200L, o.session.timeout, "Check session.timeout");
		Assertions.assertEquals(10, o.session.poolSize, "Check session.poolSize");
		Assertions.assertNull(o.session.password, "Check session.password");
		Assertions.assertFalse(o.session.ssl, "Check session.ssl");
		Assertions.assertTrue(o.session.testWhileIdle, "Check session.testWhileIdle");
		Assertions.assertFalse(o.session.test, "Check session.test");
		Assertions.assertNotNull(o.twilio, "Check twilio");
		Assertions.assertEquals(TwilioConfig.BASE_URL, o.twilio.baseUrl, "Check twilio.baseUrl");
		Assertions.assertNotNull(o.twilio.accountId, "Check twilio.accountId");	// Could be the real account ID if the environment variable is set.
		Assertions.assertNotNull(o.twilio.authToken, "Check twilio.authToken");	// Could be the real authorization token if the environment variable is set.

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
		Assertions.assertEquals("https://api.allclear.app/manager/index.html", o.adminUrl, "Check adminUrl");
		Assertions.assertEquals("MG84db3eef8585f4f32478113f58b6138e", o.alertSid, "Check alertSid");
		Assertions.assertNull(o.alertPhone, "Check alertPhone");
		Assertions.assertEquals("MG84db3eef8585f4f32478113f58b6138e", o.registrationSid, "Check registrationSid");
		Assertions.assertNull(o.registrationPhone, "Check registrationPhone");
		Assertions.assertEquals("MG84db3eef8585f4f32478113f58b6138e", o.authSid, "Check authSid");
		Assertions.assertNull(o.authPhone, "Check authPhone");
		Assertions.assertEquals("New COVID-19 test locations are available in your area. Click here to view them on AllClear: https://app.allclear.app/alert?lastAlertedAt=%s&phone=%s&token=%s", o.alertSMSMessage, "Check alertSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to register is %s or click this magic link https://app.allclear.app/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to login is %s or click this magic link https://app.allclear.app/auth?phone=%s&token=%s", o.authSMSMessage, "Check authSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		assertThat(o.auditLog).as("Check auditLog").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-audit-prod;AccountKey=").endsWith(";TableEndpoint=https://allclear-audit-prod.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.geocode, "Check geocode");
		Assertions.assertEquals("allclear-prod-mapcache.redis.cache.windows.net", o.geocode.host, "Check geocode.host");
		Assertions.assertEquals(6380, o.geocode.port, "Check geocode.port");
		Assertions.assertEquals(200L, o.geocode.timeout, "Check geocode.timeout");
		Assertions.assertEquals(10, o.geocode.poolSize, "Check geocode.poolSize");
		Assertions.assertEquals("password", o.geocode.password, "Check geocode.password");
		Assertions.assertTrue(o.geocode.ssl, "Check geocode.ssl");
		Assertions.assertTrue(o.geocode.testWhileIdle, "Check geocode.testWhileIdle");
		Assertions.assertFalse(o.geocode.test, "Check geocode.test");
		Assertions.assertEquals(60, o.task, "Check task");
		Assertions.assertEquals(60000L, o.task(), "Check task()");
		assertThat(o.queue).as("Check queue").startsWith("DefaultEndpointsProtocol=https;AccountName=allclearprodqueues;AccountKey=").endsWith(";EndpointSuffix=core.windows.net");
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
	public void staging() throws Exception
	{
		var o = load("staging");
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("staging", o.env, "Check env");
		Assertions.assertFalse(o.disableSwagger, "Check disableSwagger");
		Assertions.assertEquals("https://app-staging.allclear.app", o.baseUrl, "Check baseUrl");
		Assertions.assertEquals("https://api-staging.allclear.app/manager/index.html", o.adminUrl, "Check adminUrl");
		Assertions.assertNull(o.alertSid, "Check alertSid");
		Assertions.assertEquals("+16466321488", o.alertPhone, "Check alertPhone");
		Assertions.assertNull(o.registrationSid, "Check registrationSid");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertNull(o.authSid, "Check authSid");
		Assertions.assertEquals("+16466321488", o.authPhone, "Check authPhone");
		Assertions.assertEquals("New COVID-19 test locations are available in your area. Click here to view them on AllClear: https://app-staging.allclear.app/alert?lastAlertedAt=%s&phone=%s&token=%s", o.alertSMSMessage, "Check alertSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to register is %s or click this magic link https://app-staging.allclear.app/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to login is %s or click this magic link https://app-staging.allclear.app/auth?phone=%s&token=%s", o.authSMSMessage, "Check authSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		assertThat(o.auditLog).as("Check auditLog").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-audit-staging;AccountKey=").endsWith(";TableEndpoint=https://allclear-audit-staging.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.geocode, "Check geocode");
		Assertions.assertEquals("allclear-staging2-mapcache.redis.cache.windows.net", o.geocode.host, "Check geocode.host");
		Assertions.assertEquals(6380, o.geocode.port, "Check geocode.port");
		Assertions.assertEquals(200L, o.geocode.timeout, "Check geocode.timeout");
		Assertions.assertEquals(10, o.geocode.poolSize, "Check geocode.poolSize");
		Assertions.assertEquals("password", o.geocode.password, "Check geocode.password");
		Assertions.assertTrue(o.geocode.ssl, "Check geocode.ssl");
		Assertions.assertTrue(o.geocode.testWhileIdle, "Check geocode.testWhileIdle");
		Assertions.assertFalse(o.geocode.test, "Check geocode.test");
		Assertions.assertEquals(60, o.task, "Check task");
		Assertions.assertEquals(60000L, o.task(), "Check task()");
		assertThat(o.queue).as("Check queue").startsWith("DefaultEndpointsProtocol=https;AccountName=allclearstagingqueues;AccountKey=").endsWith(";EndpointSuffix=core.windows.net");
		Assertions.assertNotNull(o.session, "Check session");
		Assertions.assertEquals("allclear-staging2.redis.cache.windows.net", o.session.host, "Check session.host");
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
		Assertions.assertEquals("jdbc:mysql://allclear-staging.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.trans.getValidationQueryTimeout(), "Check trans.validationQueryTimeout");
		Assertions.assertEquals(5, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(20, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertNull(o.trans.getReadOnlyByDefault(), "Check trans.readOnlyByDefault");
		Assertions.assertTrue(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertTrue(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
		assertThat(o.trans.getProperties()).as("Check trans.properties").contains(MapEntry.entry("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect"));

		Assertions.assertEquals("com.mysql.jdbc.Driver", o.read.getDriverClass(), "Check read.driverClass");
		Assertions.assertEquals("allclear", o.read.getUser(), "Check read.user");
		Assertions.assertEquals("allclearpwd", o.read.getPassword(), "Check read.password");
		Assertions.assertEquals("jdbc:mysql://allclear-staging.mysql.database.azure.com:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC&useSSL=true&requireSSL=true",  o.read.getUrl(), "Check read.url");
		Assertions.assertEquals(Duration.seconds(1L), o.read.getMaxWaitForConnection(), "Check read.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.read.getValidationQuery(), "Check read.validationQuery");
		Assertions.assertEquals(Optional.of(Duration.seconds(10L)), o.read.getValidationQueryTimeout(), "Check read.validationQueryTimeout");
		Assertions.assertEquals(5, o.read.getMinSize(), "Check read.minSize");
		Assertions.assertEquals(20, o.read.getMaxSize(), "Check read.maxSize");
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
		Assertions.assertEquals("https://api-test.allclear.app/manager/index.html", o.adminUrl, "Check adminUrl");
		Assertions.assertNull(o.alertSid, "Check alertSid");
		Assertions.assertEquals("+16466321488", o.alertPhone, "Check alertPhone");
		Assertions.assertNull(o.registrationSid, "Check registrationSid");
		Assertions.assertEquals("+16466321488", o.registrationPhone, "Check registrationPhone");
		Assertions.assertNull(o.authSid, "Check authSid");
		Assertions.assertEquals("+16466321488", o.authPhone, "Check authPhone");
		Assertions.assertEquals("New COVID-19 test locations are available in your area. Click here to view them on AllClear: https://app-test.allclear.app/alert?lastAlertedAt=%s&phone=%s&token=%s", o.alertSMSMessage, "Check alertSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to register is %s or click this magic link https://app-test.allclear.app/register?phone=%s&code=%s", o.registrationSMSMessage, "Check registrationSMSMessage");
		Assertions.assertEquals("Your AllClear passcode to login is %s or click this magic link https://app-test.allclear.app/auth?phone=%s&token=%s", o.authSMSMessage, "Check authSMSMessage");
		assertThat(o.admins).as("Check admins").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-admins;AccountKey=").endsWith(";TableEndpoint=https://allclear-admins.table.cosmos.azure.com:443/;");
		assertThat(o.auditLog).as("Check auditLog").startsWith("DefaultEndpointsProtocol=https;AccountName=allclear-audit-dev;AccountKey=").endsWith(";TableEndpoint=https://allclear-audit-dev.table.cosmos.azure.com:443/;");
		Assertions.assertNotNull(o.geocode, "Check geocode");
		Assertions.assertNull(o.geocode.host, "Check geocode.host");
		Assertions.assertEquals(JedisConfig.PORT_DEFAULT, o.geocode.port, "Check geocode.port");
		Assertions.assertNull(o.geocode.timeout, "Check geocode.timeout");
		Assertions.assertNull(o.geocode.poolSize, "Check geocode.poolSize");
		Assertions.assertNull(o.geocode.password, "Check geocode.password");
		Assertions.assertFalse(o.geocode.ssl, "Check geocode.ssl");
		Assertions.assertTrue(o.geocode.testWhileIdle, "Check geocode.testWhileIdle");
		Assertions.assertTrue(o.geocode.test, "Check geocode.test");
		Assertions.assertEquals(0, o.task, "Check task");
		Assertions.assertEquals(0L, o.task(), "Check task()");
		assertThat(o.queue).as("Check queue").startsWith("DefaultEndpointsProtocol=https;AccountName=allcleardevqueues;AccountKey=").endsWith(";EndpointSuffix=core.windows.net");
		Assertions.assertNotNull(o.session, "Check session");
		Assertions.assertNull(o.session.host, "Check session.host");
		Assertions.assertEquals(JedisConfig.PORT_DEFAULT, o.session.port, "Check session.port");
		Assertions.assertNull(o.session.timeout, "Check session.timeout");
		Assertions.assertNull(o.session.poolSize, "Check session.poolSize");
		Assertions.assertNull(o.session.password, "Check session.password");
		Assertions.assertFalse(o.session.ssl, "Check session.ssl");
		Assertions.assertTrue(o.session.testWhileIdle, "Check session.testWhileIdle");
		Assertions.assertTrue(o.session.test, "Check session.test");
		Assertions.assertNotNull(o.twilio, "Check twilio");
		Assertions.assertEquals(TwilioConfig.BASE_URL, o.twilio.baseUrl, "Check twilio.baseUrl");
		Assertions.assertNotNull(o.twilio.accountId, "Check twilio.accountId");	// Could be the real account ID if the environment variable is set.
		Assertions.assertNotNull(o.twilio.authToken, "Check twilio.authToken");	// Could be the real authorization token if the environment variable is set.

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
