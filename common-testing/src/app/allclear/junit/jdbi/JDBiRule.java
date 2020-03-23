package app.allclear.junit.jdbi;

import org.junit.rules.ExternalResource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.Handles;
import org.jdbi.v3.guava.GuavaPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import io.dropwizard.testing.junit5.DropwizardExtension;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/** JUnit testing rule that starts up a JJdbi connection and runs the Liquibase migrations.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class JDBiRule extends ExternalResource implements DropwizardExtension
{
	static { org.h2.engine.Mode.getRegular().alterTableModifyColumn = true; }	// See: https://github.com/h2database/h2database/issues/2404 - DLS on 1/21/2020.

	public static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE";	// Ignore case to be compliant with mySQL case insensitivity. // ;MODE=MYSQL"; // removed because Liquibase fails on the first dropAll in the before method.
	public static final String DB_NAME = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE";	// Ignore case to be compliant with mySQL case insensitivity. // ;MODE=MYSQL"; // removed because Liquibase fails on the first dropAll in the before method.
	public static final String MIGRATIONS = "migrations.xml";
	public static final String MIGRATION_CONTEXTS = "";

	private final String url;	// JDBC URL.
	private final String migrations;	// Liquibase Migrations file name.
	private String migrationContexts = MIGRATION_CONTEXTS;
	public JDBiRule migrationContexts(String newValue) { migrationContexts = newValue; return this; }

	private Jdbi dbi;
	public Jdbi dbi() { return dbi; }

	public JDBiRule()
	{
		this.url = DB_URL;
		this.migrations = MIGRATIONS;
	}

	public JDBiRule(final String dbName)
	{
		this(dbName, MIGRATIONS);
	}

	public JDBiRule(final String dbName, final String migrations)
	{
		this.url = String.format(DB_NAME, dbName);
		this.migrations = migrations;
	}

	/** Utility method: performs a Liquibase migration with Jdbi interface. */
	public static void migrate(final Jdbi dbi, final String migrations) throws Exception
	{
		migrate(dbi, migrations, MIGRATION_CONTEXTS);
	}

	/** Utility method: performs a Liquibase migration with Jdbi interface. */
	public static void migrate(final Jdbi dbi) throws Exception
	{
		migrate(dbi, MIGRATIONS);
	}

	/** Utility method: performs a Liquibase migration with Jdbi interface. */
	public static void migrate(final Jdbi dbi, final String migrations, final String migrationContexts) throws Exception
	{
		try (var h = dbi.open())
		{
			h.getConfig(Handles.class).setForceEndTransactions(false);	// https://github.com/jdbi/jdbi/issues/1000
			var migrator = new Liquibase(migrations, new ClassLoaderResourceAccessor(), new JdbcConnection(h.getConnection()));
			migrator.dropAll();
			migrator.update(migrationContexts);
		}
	}

	/** Utility method: drops all the tables in a Liquibase migration. Used for clean-up. */
	public static void dropAll(final Jdbi dbi) throws Exception
	{
		dropAll(dbi, MIGRATIONS);
	}

	/** Utility method: drops all the tables in a Liquibase migration. Used for clean-up. */
	public static void dropAll(final Jdbi dbi, final String migrations) throws Exception
	{
		try (var h = dbi.open())
		{
			h.getConfig(Handles.class).setForceEndTransactions(false);	// https://github.com/jdbi/jdbi/issues/1000
			new Liquibase(migrations, new ClassLoaderResourceAccessor(), new JdbcConnection(h.getConnection())).dropAll();
		}
	}

	@Override
	public void before() throws Exception
	{
		migrate(dbi = Jdbi.create(url).installPlugin(new SqlObjectPlugin()).installPlugin(new GuavaPlugin()), migrations, migrationContexts);
	}

	@Override
	public void after()
	{
		try { dropAll(dbi, migrations); }
		catch (RuntimeException ex) { throw ex; }
		catch (Exception ex) { throw new RuntimeException(ex); }
	}
}
