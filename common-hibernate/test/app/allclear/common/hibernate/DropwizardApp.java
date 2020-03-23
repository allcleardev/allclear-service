package app.allclear.common.hibernate;

import java.util.List;

import app.allclear.common.entity.*;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/** Represents a Dropwizard test application.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class DropwizardApp extends Application<DropwizardConfig>
{
	public static final List<Class<?>> ENTITIES = List.of(Country.class, User.class);

	private static final HibernateBundle<DropwizardConfig> dataSource = new HibernateBundle<>(ENTITIES)
	{
		@Override public DataSourceFactory getDataSourceFactory(final DropwizardConfig conf) { return conf.dataSource; }
		@Override public DataSourceFactory getReadSourceFactory(final DropwizardConfig conf) { return conf.dataSource; }
	};

	public static void main(final String... args) throws Exception
	{
		new DropwizardApp().run(args);
	}

	@Override
	public String getName() { return "dropwizard-hibernate-test"; }

	@Override
	public void initialize(final Bootstrap<DropwizardConfig> bootstrap)
	{
		bootstrap.addBundle(dataSource);
		bootstrap.addBundle(new MigrationsBundle<DropwizardConfig>() {
			@Override public DataSourceFactory getDataSourceFactory(final DropwizardConfig conf) { return conf.dataSource; }
		});
	}

	@Override
	public void run(final DropwizardConfig conf, final Environment env)
	{
		var dao = new CountryDAO(dataSource.getSessionFactory());
		env.jersey().register(new CountryResource(dao));
	}
}
