package app.allclear.platform;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.*;

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

	@Test
	public void local() throws Exception
	{
		var o = mapper.readValue(new File("conf/local.json"), Config.class);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals("local", o.env, "Check env");
		Assertions.assertEquals("com.mysql.jdbc.Driver", o.trans.getDriverClass(), "Check trans.driverClass");
		Assertions.assertEquals("allclear", o.trans.getUser(), "Check trans.user");
		Assertions.assertEquals("allclear", o.trans.getPassword(), "Check trans.password");
		Assertions.assertEquals("jdbc:mysql://localhost:3306/allclear?useEncoding=true&characterEncoding=UTF-8&prepStmtCacheSize=100&prepStmtCacheSqlLimit=1024&serverTimezone=UTC",  o.trans.getUrl(), "Check trans.url");
		Assertions.assertEquals(Duration.seconds(1L), o.trans.getMaxWaitForConnection(), "Check trans.maxWaitForConnection");
		Assertions.assertEquals(Optional.of("SELECT 1"), o.trans.getValidationQuery(), "Check trans.validationQuery");
		Assertions.assertEquals(1, o.trans.getMinSize(), "Check trans.minSize");
		Assertions.assertEquals(10, o.trans.getMaxSize(), "Check trans.maxSize");
		Assertions.assertTrue(o.trans.getCheckConnectionWhileIdle(), "Check trans.checkConnectionWhileIdle");
		Assertions.assertTrue(o.trans.getCheckConnectionOnBorrow(), "Check trans.checkConnectionOnBorrow");
		Assertions.assertEquals(TransactionIsolation.READ_COMMITTED, o.trans.getDefaultTransactionIsolation(), "Check trans.defaultTransactionIsolation");
	}
}
