package app.allclear.common.hibernate;

import java.util.*;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.SessionFactoryHealthCheck;
import io.dropwizard.hibernate.SessionFactoryManager;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;

public abstract class HibernateBundle<T> implements ConfiguredBundle<T>, DatabaseConfiguration<T>
{
	public static final String PRIMARY = "hibernate-primary";
	public static final String READER = "hibernate-reader";

	private boolean lazyLoadingEnabled = true;

	private final List<Class<?>> entities;
	private DualSessionFactory factory;

	protected HibernateBundle(final Class<?> entity, final Class<?>... entities)
	{
		var entityClasses = new ArrayList<Class<?>>();
		entityClasses.add(entity);
		entityClasses.addAll(Arrays.asList(entities));

		this.entities = Collections.unmodifiableList(entityClasses);
	}

	protected HibernateBundle(final List<Class<?>> entities)
	{
		this.entities = entities;
	}

	@Override
	public final void initialize(Bootstrap<?> bootstrap)
	{
		bootstrap.getObjectMapper().registerModule(createHibernate5Module());
	}

	/**
	 * Override to configure the {@link Hibernate5Module}.
	 */
	protected Hibernate5Module createHibernate5Module()
	{
		var module = new Hibernate5Module();
		if (lazyLoadingEnabled)
			module.enable(Feature.FORCE_LAZY_LOADING);

		return module;
	}

	abstract public PooledDataSourceFactory getReadSourceFactory(T configuration);

	@Override
	public final void run(final T configuration, final Environment env) throws Exception
	{
		factory = new DualSessionFactory(createFactory(PRIMARY, env, getDataSourceFactory(configuration)),
			createFactory(READER, env, getReadSourceFactory(configuration)));

		env.jersey().register(new UnitOfWorkApplicationListener(factory));
	}

	private SessionFactory createFactory(final String name, final Environment env, final PooledDataSourceFactory dbConfig)
	{
		var o = build(this, env, dbConfig, entities, name);
		env.healthChecks().register(name,
			new SessionFactoryHealthCheck(
				env.getHealthCheckExecutorService(),
				dbConfig.getValidationQueryTimeout().orElse(Duration.seconds(5)),
				o,
				dbConfig.getValidationQuery()));

		return o;
	}

	private SessionFactory build(HibernateBundle<?> bundle,
		Environment environment,
		PooledDataSourceFactory dbConfig,
		List<Class<?>> entities,
		String name)
	{
		var dataSource = dbConfig.build(environment.metrics(), name);
		return build(bundle, environment, dbConfig, dataSource, entities);
	}
	
	private SessionFactory build(HibernateBundle<?> bundle,
		Environment environment,
		PooledDataSourceFactory dbConfig,
		ManagedDataSource dataSource,
		List<Class<?>> entities)
	{
		var provider = buildConnectionProvider(dataSource, dbConfig.getProperties());
		var result = buildSessionFactory(bundle, dbConfig, provider, dbConfig.getProperties(), entities);
		var managedFactory = new SessionFactoryManager(result, dataSource);
		environment.lifecycle().manage(managedFactory);
		return result;
	}
	
	private ConnectionProvider buildConnectionProvider(DataSource dataSource, Map<String, String> properties)
	{
		var connectionProvider = new DatasourceConnectionProviderImpl();
		connectionProvider.setDataSource(dataSource);
		connectionProvider.configure(properties);
		return connectionProvider;
	}

	private SessionFactory buildSessionFactory(HibernateBundle<?> bundle,
		PooledDataSourceFactory dbConfig,
		ConnectionProvider connectionProvider,
		Map<String, String> properties,
		List<Class<?>> entities)
	{
		var bootstrapServiceRegistry = new BootstrapServiceRegistryBuilder().build();
		var configuration = new Configuration(bootstrapServiceRegistry);
		configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
		configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS, Boolean.toString(dbConfig.isAutoCommentsEnabled()));
		configuration.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
		configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
		configuration.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
		configuration.setProperty(AvailableSettings.ORDER_UPDATES, "true");
		configuration.setProperty(AvailableSettings.ORDER_INSERTS, "true");
		configuration.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
		configuration.setProperty("jadira.usertype.autoRegisterUserTypes", "true");
		for (var property : properties.entrySet())
			configuration.setProperty(property.getKey(), property.getValue());

		addAnnotatedClasses(configuration, entities);
		bundle.configure(configuration);

		var registry = new StandardServiceRegistryBuilder(bootstrapServiceRegistry)
			.addService(ConnectionProvider.class, connectionProvider)
			.applySettings(configuration.getProperties())
			.build();

		configure(configuration, registry);

		return configuration.buildSessionFactory(registry);
	}
	
	protected void configure(Configuration configuration, ServiceRegistry registry) {}
	
	private void addAnnotatedClasses(Configuration configuration, Iterable<Class<?>> entities)
	{
		var entityClasses = new TreeSet<>();
		for (var klass : entities)
		{
			configuration.addAnnotatedClass(klass);
			entityClasses.add(klass.getCanonicalName());
		}
	}

	public boolean isLazyLoadingEnabled() { return lazyLoadingEnabled; }

	public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) { this.lazyLoadingEnabled = lazyLoadingEnabled; }

	public DualSessionFactory getSessionFactory() { return factory; }

	protected void configure(org.hibernate.cfg.Configuration configuration) { }
}
