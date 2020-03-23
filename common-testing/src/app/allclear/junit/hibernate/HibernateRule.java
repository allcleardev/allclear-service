package app.allclear.junit.hibernate;

import java.sql.*;

import org.junit.rules.ExternalResource;
import org.hibernate.*;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.guava.GuavaPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import io.dropwizard.testing.junit5.DropwizardExtension;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/** JUnit external resource that manages the starting and stopping of an embedded H2 database
 *  and its Hibernate SessionFactory.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class HibernateRule extends ExternalResource implements DropwizardExtension
{
	static { org.h2.engine.Mode.getRegular().alterTableModifyColumn = true; }	// See: https://github.com/h2database/h2database/issues/2404 - DLS on 1/21/2020.

	public static final String PROPERTY_READER = "jdbiReader";

	/** Session factory used by all tests. */
	public SessionFactory getSessionFactory() { return sessionFactory; }
	private SessionFactory sessionFactory = null;

	/** Entities to test. */
	private final Class<?>[] classes;

	/** DB constants. */
	public static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE";	// Ignore case to be compliant with mySQL case insensitivity. // ;MODE=MYSQL"; // removed because Liquibase fails on the first dropAll in the before method.
	public static final String DB_NAME = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE";	// Ignore case to be compliant with mySQL case insensitivity. // ;MODE=MYSQL"; // removed because Liquibase fails on the first dropAll in the before method.
	// public static final String DB_URL = "jdbc:h2:~/local/test/data;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE";	// Uncomment to create the DB physically on harddrive for later inspection. DLS on 4/29/2015.

	/** Liquibase contexts under which to set up the database. */
	private String migrationContexts = "";

	public String getDbUrl() { return dbUrl; }
	private String dbUrl = DB_URL;
	public HibernateRule withDbUrl(String newValue) { dbUrl = newValue; return this; }
	public HibernateRule withDbName(String newValue) { return withDbUrl(String.format(DB_NAME, newValue)); }	// Create an in-memory URL with an alternate database name.

	/** Represents the name of the migration resource file. */
	public String getMigrations() { return migrations; }
	private String migrations = "migrations.xml";
	public HibernateRule withMigrations(String newValue) { migrations = newValue; return this; }

	/** Represents the read-only JDBi interface for queries. */
	private Jdbi reader = null;
	public Jdbi reader() { return reader; }

	/** Populator.
	 * 
	 * @param classes
	 */
	public HibernateRule(final Class<?>... classes)
	{
		this.classes = classes;
	}

	/** Populator.
	 * 
	 * @param migrationContexts
	 * @param classes
	 */
	public HibernateRule(final String migrationContexts, final Class<?>... classes)
	{
		this.migrationContexts = migrationContexts;
		this.classes = classes;
	}

	@Override
	public void before() throws Exception
	{
		// Create the database first since it is validated.
		Class.forName("org.h2.Driver");
		try (var connection = DriverManager.getConnection(dbUrl))
		{
			var migrator = new Liquibase(migrations, new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
			migrator.dropAll();
			migrator.update(migrationContexts);
		}

		var conf = new Configuration();
		for (var clazz : classes)
			conf.addAnnotatedClass(clazz);

		conf.setProperty(AvailableSettings.DIALECT, "org.hibernate.dialect.H2Dialect");	// MySQL5InnoDBDialect");
		conf.setProperty(AvailableSettings.DRIVER, "org.h2.Driver");
		conf.setProperty(AvailableSettings.URL, dbUrl);
		conf.setProperty(AvailableSettings.SHOW_SQL, "false");
		conf.setProperty(AvailableSettings.HBM2DDL_AUTO, "none");  // "validate");	// Could use "none".
		conf.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
		conf.setProperty(AvailableSettings.CACHE_REGION_FACTORY, "org.hibernate.testing.cache.CachingRegionFactory");
		conf.setProperty(AvailableSettings.USE_QUERY_CACHE, "true");
		conf.setProperty(AvailableSettings.USE_SECOND_LEVEL_CACHE, "true");
		conf.setProperty(AvailableSettings.DEFAULT_CACHE_CONCURRENCY_STRATEGY, AccessType.READ_WRITE.getExternalName());

		sessionFactory = conf.buildSessionFactory();
		sessionFactory.getProperties().put(PROPERTY_READER, reader = Jdbi.create(dbUrl).installPlugin(new SqlObjectPlugin()).installPlugin(new GuavaPlugin()));	// Set read-only query interface.
	}

	@Override
	public void after()
	{
		sessionFactory.close();
		reader = null;

		// Drop all the data.
		try
		{
			Class.forName("org.h2.Driver");
			try (var connection = DriverManager.getConnection(dbUrl))
			{
				(new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection))).dropAll();
			}
		}
		catch (final Exception ex) { ex.printStackTrace(); }
	}
}
