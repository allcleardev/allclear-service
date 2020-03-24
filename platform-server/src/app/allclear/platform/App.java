package app.allclear.platform;

import java.util.List;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.*;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.*;

import app.allclear.common.errors.*;
import app.allclear.common.jackson.ObjectMapperProvider;
import app.allclear.common.jersey.CrossDomainHeadersFilter;
import app.allclear.common.resources.*;
import app.allclear.platform.dao.*;
import app.allclear.platform.entity.*;
import app.allclear.platform.rest.*;

/** Represents the Dropwizard application entry point.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class App extends Application<Config>
{
	private static final Logger log = LoggerFactory.getLogger(App.class);

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
		bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));

		bootstrap.addBundle(transHibernateBundle);
		bootstrap.addBundle(new AssetsBundle("/assets/swagger_ui", "/swagger-ui/", null, "swagger-ui"));
		bootstrap.addBundle(new MigrationsBundle<Config>() {
			@Override
			public DataSourceFactory getDataSourceFactory(final Config conf) { return conf.trans; }
		});
	}

	@Override
	public void run(final Config conf, final Environment env)
	{
		log.info("Initialized: {} - {}", conf.env, conf.getVersion());

		var factory = transHibernateBundle.getSessionFactory();
		var peopleDao = new PeopleDAO(factory);

		var jersey = env.jersey();
        jersey.register(MultiPartFeature.class);
        jersey.register(new ObjectMapperProvider());
        jersey.register(new CrossDomainHeadersFilter());
        jersey.register(new ValidationExceptionMapper());
        jersey.register(new NotFoundExceptionMapper());
        jersey.register(new AuthenticationExceptionMapper());
        jersey.register(new AuthorizationExceptionMapper());
        jersey.register(new PageNotFoundExceptionMapper());
        jersey.register(new LockAcquisitionExceptionMapper());
        jersey.register(new LockTimeoutExceptionMapper());
        jersey.register(new ThrowableExceptionMapper());
        jersey.register(new InfoResource(conf, env.healthChecks(), List.of(HibernateBundle.DEFAULT_NAME), conf.getVersion()));
        jersey.register(new HeapDumpResource());
        jersey.register(new HibernateResource(factory));
		jersey.register(new PeopleResource(peopleDao));

		setupSwagger(conf, env);
	}

	/** Helper method - sets up the Swagger endpoint document & UI. */
	private void setupSwagger(final Config conf, final Environment env)
	{
		if (conf.disableSwagger) return;		// Disable Swagger in certain environments.

		env.jersey().register(new ApiListingResource());
		env.jersey().register(new SwaggerSerializers());

		var config = new BeanConfig();
		config.setTitle(conf.env.toUpperCase() + " " + getName());
		config.setVersion(conf.getVersion());
		config.setResourcePackage("com.jibe.dwservice.resources,com.jibe.translation.rest");
		config.setScan(true);

		env.jersey().register(new RedirectResource("swagger-ui/index.html"));
	}
}
