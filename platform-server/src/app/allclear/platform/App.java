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
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import app.allclear.common.AutoCloseableManager;
import app.allclear.common.errors.*;
import app.allclear.common.jackson.ObjectMapperProvider;
import app.allclear.common.jersey.CrossDomainHeadersFilter;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.redis.RedisClient;
import app.allclear.common.resources.*;
import app.allclear.platform.dao.*;
import app.allclear.platform.entity.*;
import app.allclear.platform.rest.*;
import app.allclear.twilio.client.TwilioClient;

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

	public static final Class<?>[] ENTITIES = new Class<?>[] { Conditions.class, Exposures.class, Facility.class, People.class, Symptoms.class };

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
		bootstrap.addBundle(new AssetsBundle("/assets/web", "/manager", "index.html", "admin-app"));	// MUST use /manager as the context because anything that begins with 'admin' apparently collides with config "server.adminContextPath: admin". DLS on 4/1/2020.
		bootstrap.addBundle(new AssetsBundle("/assets/swagger_ui", "/swagger-ui/", "index.html", "swagger-ui"));
		bootstrap.addBundle(new MigrationsBundle<Config>() {
			@Override
			public DataSourceFactory getDataSourceFactory(final Config conf) { return conf.trans; }
		});
	}

	@Override
	public void run(final Config conf, final Environment env) throws Exception
	{
		log.info("Initialized: {} - {}", conf.env, conf.getVersion());

		var ds = conf.trans.build(env.metrics(), "migrations-init");
		try (var connection = ds.getConnection())
		{
			var migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
			migrator.dropAll();
			migrator.update("");
		}
		log.info("Migrations: completed");

		var lifecycle = env.lifecycle();
		var session = conf.session.test ? new FakeRedisClient() : new RedisClient(conf.session);
		var twilio = new TwilioClient(conf.twilio);

		lifecycle.manage(new AutoCloseableManager(session));
		lifecycle.manage(new AutoCloseableManager(twilio));

		var factory = transHibernateBundle.getSessionFactory();
		var adminDao = new AdminDAO(conf.admins);
		var peopleDao = new PeopleDAO(factory);
		var sessionDao = new SessionDAO(session, twilio, conf);
		var registrationDao = new RegistrationDAO(session, twilio, conf);

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
        jersey.register(new AuthFilter(sessionDao));
        jersey.register(new AdminResource(adminDao, sessionDao));
        jersey.register(new FacilityResource(new FacilityDAO(factory)));
		jersey.register(new PeopleResource(peopleDao, registrationDao, sessionDao));
		jersey.register(new TypeResource());

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
		config.setResourcePackage("app.allclear.common.resources,app.allclear.platform.rest");
		config.setScan(true);

		env.jersey().register(new RedirectResource("swagger-ui/index.html"));
	}
}
