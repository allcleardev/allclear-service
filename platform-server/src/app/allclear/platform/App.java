package app.allclear.platform;

import java.util.List;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.*;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.*;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import app.allclear.common.AutoCloseableManager;
import app.allclear.common.azure.QueueManager;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.HibernateBundle;
import app.allclear.common.jackson.ObjectMapperProvider;
import app.allclear.common.jersey.CrossDomainHeadersFilter;
import app.allclear.common.redis.RedisClient;
import app.allclear.common.resources.*;
import app.allclear.common.task.TaskOperator;
import app.allclear.google.client.MapClient;
import app.allclear.platform.dao.*;
import app.allclear.platform.entity.*;
import app.allclear.platform.model.*;
import app.allclear.platform.rest.*;
import app.allclear.platform.task.*;
import app.allclear.platform.value.FacilitateValue;
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
	public static final String QUEUE_ALERT = "alert";
	public static final String QUEUE_ALERT_INIT = "alert-init";
	public static final String QUEUE_FACILITATE = "facilitate";
	public static final String SESSION = "session-cache";

	public static final Class<?>[] ENTITIES = new Class<?>[] { Conditions.class, CountByBoolean.class, CountByName.class, Created.class, Experiences.class, ExperiencesTag.class, Exposures.class, Facility.class, FacilityX.class, FacilityPeople.class, FacilityTestType.class, Friend.class, Friendship.class, Name.class, Named.class, People.class, PeopleFacility.class, PeopleField.class, Symptoms.class, SymptomsLog.class, Tests.class, Total.class };

	private final HibernateBundle<Config> transHibernateBundle = new HibernateBundle<>(People.class, ENTITIES) {
		@Override public DataSourceFactory getDataSourceFactory(final Config conf) { return conf.trans; }
		@Override public DataSourceFactory getReadSourceFactory(final Config conf) { return conf.read; }
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
			new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection)).update("");
		}
		ds.stop();	// Kill this data source pool after migration is completed. A new one will be created when the session factory is created. DLS on 4/3/2020.
		log.info("Migrations: completed");

		var lifecycle = env.lifecycle();
		var map = new MapClient(conf.geocode.pool());
		var session = new RedisClient(conf.session.pool());
		var twilio = new TwilioClient(conf.twilio);

		lifecycle.manage(new AutoCloseableManager(map));
		lifecycle.manage(new AutoCloseableManager(session));
		lifecycle.manage(new AutoCloseableManager(twilio));

		var factory = transHibernateBundle.getSessionFactory();
		var adminDao = new AdminDAO(conf.admins);
		var sessionDao = new SessionDAO(session, twilio, conf);
		var auditor = new AuditLogDAO(factory, sessionDao, conf.auditLog);
		var customerDao = new CustomerDAO(conf.env, conf.admins, session);
		var facilityDao = new FacilityDAO(factory, auditor);
		var peopleDao = new PeopleDAO(factory);
		var registrationDao = new RegistrationDAO(session, twilio, conf);

		var task = new QueueManager(conf.queue, conf.task(), 2,
			new TaskOperator<>(QUEUE_ALERT, new AlertTask(factory, peopleDao, facilityDao, sessionDao), AlertRequest.class, 10, 5, 60, 3600),
			new TaskOperator<>(QUEUE_FACILITATE, new FacilitateTask(adminDao, twilio, conf), FacilitateValue.class, 3, 30, 60, 3600));

		lifecycle.manage(task.addOperator(new TaskOperator<>(QUEUE_ALERT_INIT, new AlertInitTask(factory, peopleDao, task.queue(QUEUE_ALERT)), AlertInitRequest.class, 10, 120, 60, 3600)));

		var hc = env.healthChecks();
		hc.register(SESSION, session);

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
        jersey.register(new InfoResource(conf, hc, List.of(HibernateBundle.PRIMARY), conf.getVersion()));
        jersey.register(new HeapDumpResource());
        jersey.register(new HibernateResource(factory));
        jersey.register(new LogResource());
        jersey.register(new AuthFilter(sessionDao));
        jersey.register(new AdminResource(adminDao, sessionDao));
        jersey.register(new AuditLogResource(auditor));
        jersey.register(new CustomerResource(customerDao));
        jersey.register(new ExperiencesResource(new ExperiencesDAO(factory, sessionDao)));
        jersey.register(new FacilitateResource(new FacilitateDAO(conf.auditLog, facilityDao, sessionDao, task.queue(QUEUE_FACILITATE))));
        jersey.register(new FacilityResource(facilityDao, sessionDao, map));
        jersey.register(new FriendResource(new FriendDAO(factory), sessionDao));
        jersey.register(new MapResource(map));
		jersey.register(new PeopleResource(peopleDao, registrationDao, sessionDao, task.queue(QUEUE_ALERT)));
		jersey.register(new app.allclear.common.azure.QueueResource(task));
		jersey.register(new RegistrationResource(registrationDao));
		jersey.register(new SessionResource(sessionDao));
		jersey.register(new SymptomsLogResource(new SymptomsLogDAO(factory), sessionDao));
		jersey.register(new TestsResource(new TestsDAO(factory, sessionDao)));
		jersey.register(new TwilioResource(conf.twilio.authToken, peopleDao));
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
