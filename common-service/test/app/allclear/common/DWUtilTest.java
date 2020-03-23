package app.allclear.common;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.junit.*;

import io.dropwizard.jackson.Jackson;

/** Unit test class that verifies the DWUtil class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */
public class DWUtilTest
{
	public static final String DB_USER_NAME = "db.userName";
	public static final String DB_PASSWORD = "db.password";

	@Before
	public void init()
	{
		System.clearProperty(DB_USER_NAME);
		System.clearProperty(DB_PASSWORD);
		System.clearProperty("db.initialSize");
		System.clearProperty("db.minSize");
		System.clearProperty("db.checkConnectionOnBorrow");
		System.clearProperty(DWUtil.APP_CREDENTIALS_FILE);
	}

	@Test(expected=IllegalArgumentException.class)
	public void loadAppCredentials_invalidPath()
	{
		System.setProperty(DWUtil.APP_CREDENTIALS_FILE, "/invalid/fileName.cred");
		DWUtil.loadAppCredentials();
	}

	@Test(expected=IllegalArgumentException.class)
	public void loadAppCredentials_invalidFile()
	{
		System.setProperty(DWUtil.APP_CREDENTIALS_FILE, "./test-res/data/awsManager");
		DWUtil.loadAppCredentials();
	}

	@Test
	public void loadAppCredentials() throws Exception
	{
		Assert.assertNull("Check db.userName: before", System.getProperty(DB_USER_NAME));
		Assert.assertNull("Check db.password: before", System.getProperty(DB_PASSWORD));

		System.setProperty(DWUtil.APP_CREDENTIALS_FILE, "./test-res/app.cred");
		StringSubstitutor replacer = DWUtil.loadAppCredentials();
		SimpleConfiguration conf = loadConfig(replacer);

		Assert.assertEquals("Check db.userName: after", "superuser", System.getProperty(DB_USER_NAME));
		Assert.assertEquals("Check db.password: after", "easy_to_guess", System.getProperty(DB_PASSWORD));

		Assert.assertEquals("Check driverClass", "org.h2.Driver", conf.dataSource.getDriverClass());
		Assert.assertEquals("Check url", "${db.url}", conf.dataSource.getUrl());	// Missing: jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE
		Assert.assertEquals("Check user", "superuser", conf.dataSource.getUser());
		Assert.assertEquals("Check password", "easy_to_guess", conf.dataSource.getPassword());
		Assert.assertEquals("Check initialSize", 100, conf.dataSource.getInitialSize());
		Assert.assertEquals("Check minSize", 75, conf.dataSource.getMinSize());
		Assert.assertEquals("Check maxSize", 500, conf.dataSource.getMaxSize());
		Assert.assertTrue("Check checkConnectionWhileIdle", conf.dataSource.getCheckConnectionWhileIdle());
		Assert.assertTrue("Check checkConnectionOnBorrow", conf.dataSource.getCheckConnectionOnBorrow());
	}

	@Test(expected=IllegalArgumentException.class)
	public void loadAppCredentials_invalid()
	{
		System.clearProperty(DWUtil.APP_CREDENTIALS_FILE);
		System.setProperty(DWUtil.APP_CREDENTIALS_FILE, "./test-res/sheldon_cooper.jpg");
		DWUtil.loadAppCredentials();
	}

	@Test
	public void loadWithoutAppCredentials() throws Exception
	{
		Assert.assertNull("Check db.userName: before", System.getProperty(DB_USER_NAME));
		Assert.assertNull("Check db.password: before", System.getProperty(DB_PASSWORD));

		StringSubstitutor replacer = DWUtil.loadAppCredentials();
		SimpleConfiguration conf = loadConfig(replacer);

		Assert.assertNull("Check db.userName: after", System.getProperty(DB_USER_NAME));
		Assert.assertNull("Check db.password: after", System.getProperty(DB_PASSWORD));

		Assert.assertEquals("Check driverClass", "org.h2.Driver", conf.dataSource.getDriverClass());
		Assert.assertEquals("Check url", "${db.url}", conf.dataSource.getUrl());	// Missing: jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE
		Assert.assertEquals("Check user", "sa", conf.dataSource.getUser());
		Assert.assertEquals("Check password", "not_guessable_at_all", conf.dataSource.getPassword());
		Assert.assertEquals("Check initialSize", 100, conf.dataSource.getInitialSize());
		Assert.assertEquals("Check minSize", 50, conf.dataSource.getMinSize());
		Assert.assertEquals("Check maxSize", 500, conf.dataSource.getMaxSize());
		Assert.assertTrue("Check checkConnectionWhileIdle", conf.dataSource.getCheckConnectionWhileIdle());
		Assert.assertFalse("Check checkConnectionOnBorrow", conf.dataSource.getCheckConnectionOnBorrow());
	}

	/** Loads the configuration file. */
	private SimpleConfiguration loadConfig(StringSubstitutor replacer) throws Exception
	{
		return Jackson.newObjectMapper().readValue(replacer.replace(IOUtils.toString(
			new FileInputStream(new File("test-res/sample.json")), Charset.defaultCharset())), SimpleConfiguration.class);
	}
}
