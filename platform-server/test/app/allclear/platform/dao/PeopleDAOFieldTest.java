package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.platform.type.Condition.*;
import static app.allclear.platform.type.Exposure.*;
import static app.allclear.platform.type.HealthWorkerStatus.*;
import static app.allclear.platform.type.Symptom.*;
import static app.allclear.platform.type.Visibility.*;

import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.junit.hibernate.*;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.filter.RegistrationFilter;
import app.allclear.platform.rest.PeopleResource;
import app.allclear.platform.type.*;
import app.allclear.platform.value.*;

/** Functional test class that verifies the PeopleDAO field access.
 * 
 * @author smalleyd
 * @version 1.1.40
 * @since 5/7/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleDAOFieldTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static PeopleDAO dao;
	private static FriendDAO friendDao;
	private static SessionDAO sessionDao;
	private static FakeRedisClient redis = new FakeRedisClient();
	private static SessionValue ADMIN;
	private static SessionValue FIRST_;
	private static SessionValue SECOND_;
	private static final PeopleValue FIRST = new PeopleValue("first", "888-555-1000", true)
		.withHealthWorkerStatusId(HEALTH_WORKER.id)
		.withConditions(DIABETIC)
		.withExposures(CLOSE_CONTACT)
		.withSymptoms(SHORTNESS_OF_BREATH);
	private static final PeopleValue SECOND = new PeopleValue("second", "888-555-1001", true)
		.withHealthWorkerStatusId(NEITHER.id)
		.withConditions(WEAKENED_IMMUNE_SYSTEM)
		.withExposures(NO_EXPOSURE)
		.withSymptoms(DRY_COUGH);

	private static final String TARGET = "/peoples";
	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new PeopleResource(dao, null, sessionDao, null))
		.build();
	private static final GenericType<QueryResults<PeopleValue, PeopleFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<PeopleValue, PeopleFilter>>() {};

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();

		dao = new PeopleDAO(factory);
		friendDao = new FriendDAO(factory);
		sessionDao = new SessionDAO(redis, ConfigTest.loadTest());
	}

	@Test
	public void add()
	{
		dao.add(FIRST);
		dao.add(SECOND);

		dao.update(new PeopleFieldValue(FIRST.id, ME.id, FRIENDS.id, ME.id, FRIENDS.id));
		dao.update(new PeopleFieldValue(SECOND.id, FRIENDS.id, ME.id, FRIENDS.id, ME.id));

		friendDao.start(FIRST, SECOND.id);

		ADMIN = sessionDao.add(new SessionValue(false, new AdminValue("admin")));
		FIRST_ = sessionDao.add(new SessionValue(false, FIRST));
		SECOND_ = sessionDao.add(new SessionValue(false, SECOND));
	}

	@Test
	public void add_00()
	{
		friendDao.accept(SECOND, FIRST.id);
	}

	public static Stream<Arguments> get()
	{
		return Stream.of(
			arguments((Supplier<PeopleValue>) () -> dao.getByIdWithException(FIRST.id), HEALTH_WORKER, DIABETIC, CLOSE_CONTACT, SHORTNESS_OF_BREATH),
			arguments((Supplier<PeopleValue>) () -> dao.getByIdWithException(SECOND.id), NEITHER, WEAKENED_IMMUNE_SYSTEM, NO_EXPOSURE, DRY_COUGH),
			arguments((Supplier<PeopleValue>) () -> dao.getFriend(FIRST.id, SECOND.id), NEITHER, null, NO_EXPOSURE, null),
			arguments((Supplier<PeopleValue>) () -> dao.getFriend(SECOND.id, FIRST.id), null, DIABETIC, null, SHORTNESS_OF_BREATH));
	}

	@ParameterizedTest
	@MethodSource
	public void get(final Supplier<PeopleValue> fx,
			final HealthWorkerStatus healthWorkerStatus,
			final Condition condition,
			final Exposure exposure,
			final Symptom symptom)
	{
		get(fx.get(), healthWorkerStatus, condition, exposure, symptom);
	}

	public static Stream<Arguments> get_via_resource()
	{
		return Stream.of(
			arguments(FIRST_, FIRST.id, HEALTH_WORKER, DIABETIC, CLOSE_CONTACT, SHORTNESS_OF_BREATH),
			arguments(SECOND_, SECOND.id, NEITHER, WEAKENED_IMMUNE_SYSTEM, NO_EXPOSURE, DRY_COUGH),
			arguments(FIRST_, SECOND.id, NEITHER, null, NO_EXPOSURE, null),
			arguments(SECOND_, FIRST.id, null, DIABETIC, null, SHORTNESS_OF_BREATH));
	}

	@ParameterizedTest
	@MethodSource
	public void get_via_resource(final SessionValue session,
			final String personId,
			final HealthWorkerStatus healthWorkerStatus,
			final Condition condition,
			final Exposure exposure,
			final Symptom symptom)
	{
		sessionDao.current(session);

		get(request(personId).get(PeopleValue.class), healthWorkerStatus, condition, exposure, symptom);
	}

	private void get(final PeopleValue o,
		final HealthWorkerStatus healthWorkerStatus,
		final Condition condition,
		final Exposure exposure,
		final Symptom symptom)
	{
		Assertions.assertEquals(healthWorkerStatus, o.healthWorkerStatus, "Check healthWorkerStatus");
		if (null != condition)
			assertThat(o.conditions).as("Check conditions").containsExactly(condition.created());
		else
			Assertions.assertNull(o.conditions, "Check conditions");
		if (null != exposure)
			assertThat(o.exposures).as("Check exposures").containsExactly(exposure.created());
		else
			Assertions.assertNull(o.exposures, "Check exposures");
		if (null != symptom)
			assertThat(o.symptoms).as("Check symptoms").containsExactly(symptom.created());
		else
			Assertions.assertNull(o.symptoms, "Check symptoms");
	}

	public static Stream<Arguments> search()
	{
		return Stream.of(
			arguments(new PeopleFilter().withFriendshipId(SECOND.id), HEALTH_WORKER, DIABETIC, CLOSE_CONTACT, SHORTNESS_OF_BREATH),
			arguments(new PeopleFilter().withFriendshipId(FIRST.id), NEITHER, WEAKENED_IMMUNE_SYSTEM, NO_EXPOSURE, DRY_COUGH),
			arguments(new PeopleFilter().withFriendshipId(FIRST.id).who(FRIENDS), NEITHER, null, NO_EXPOSURE, null),
			arguments(new PeopleFilter().withFriendshipId(SECOND.id).who(FRIENDS), null, DIABETIC, null, SHORTNESS_OF_BREATH));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final PeopleFilter filter,
			final HealthWorkerStatus healthWorkerStatus,
			final Condition condition,
			final Exposure exposure,
			final Symptom symptom)
	{
		search(dao.search(filter), healthWorkerStatus, condition, exposure, symptom);
	}

	public static Stream<Arguments> search_via_resource()
	{
		return Stream.of(
			arguments(ADMIN, new PeopleFilter().withFriendshipId(SECOND.id), HEALTH_WORKER, DIABETIC, CLOSE_CONTACT, SHORTNESS_OF_BREATH),
			arguments(ADMIN, new PeopleFilter().withFriendshipId(FIRST.id), NEITHER, WEAKENED_IMMUNE_SYSTEM, NO_EXPOSURE, DRY_COUGH),
			arguments(FIRST_, new PeopleFilter(), NEITHER, null, NO_EXPOSURE, null),
			arguments(SECOND_, new PeopleFilter(), null, DIABETIC, null, SHORTNESS_OF_BREATH));
	}

	@ParameterizedTest
	@MethodSource
	public void search_via_resource(final SessionValue session,
			final PeopleFilter filter,
			final HealthWorkerStatus healthWorkerStatus,
			final Condition condition,
			final Exposure exposure,
			final Symptom symptom)
	{
		sessionDao.current(session);

		search(request("search").post(Entity.json(filter), TYPE_QUERY_RESULTS), healthWorkerStatus, condition, exposure, symptom);
	}

	private void search(final QueryResults<PeopleValue, PeopleFilter> results,
		final HealthWorkerStatus healthWorkerStatus,
		final Condition condition,
		final Exposure exposure,
		final Symptom symptom)
	{
		Assertions.assertEquals(1L, results.total, "Check total");

		var o = results.records.get(0);
		Assertions.assertEquals(healthWorkerStatus, o.healthWorkerStatus, "Check healthWorkerStatus");
		Assertions.assertNull(o.conditions, "Check conditions");
		Assertions.assertNull(o.exposures, "Check exposures");
		Assertions.assertNull(o.symptoms, "Check symptoms");

		/*	NOT a deep retrieval.
		if (null != condition)
			assertThat(o.conditions).as("Check conditions").containsExactly(condition.created());
		else
			Assertions.assertNull(o.conditions, "Check conditions");
		if (null != exposure)
			assertThat(o.exposures).as("Check exposures").containsExactly(exposure.created());
		else
			Assertions.assertNull(o.exposures, "Check exposures");
		if (null != symptom)
			assertThat(o.symptoms).as("Check symptoms").containsExactly(symptom.created());
		else
			Assertions.assertNull(o.symptoms, "Check symptoms");
		*/
	}

	private WebTarget target() { return RULE.client().target(TARGET); }
	private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
