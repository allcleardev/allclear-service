package app.allclear.platform;

import app.allclear.platform.entity.People;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/** Represents the Dropwizard application entry point.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class App extends Application<Config>
{
	public static final String APP_NAME = "AllClear Platform";

	public static final Class<?>[] ENTITIES = new Class<?>[] { People.class };

	private final HibernateBundle<Config> transHibernateBundle = new HibernateBundle<>(People.class, ENTITIES) {
		@Override public DataSourceFactory getDataSourceFactory(final Config conf) { return conf.trans; }
	};

	public static void main(final String... args) throws Exception
	{
		new App().run(args);
	}

	@Override
	public String getName() { return APP_NAME; }

	@Override
	public void initialize(final Bootstrap<Config> bootstrap)
	{
		bootstrap.addBundle(transHibernateBundle);
		bootstrap.addBundle(new MigrationsBundle<Config>() {
			@Override
			public DataSourceFactory getDataSourceFactory(final Config conf) { return conf.trans; }
		});
	}

	@Override
	public void run(final Config conf, final Environment env)
	{
		
	}
}
