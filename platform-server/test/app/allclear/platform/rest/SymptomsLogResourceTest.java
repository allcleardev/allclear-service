package app.allclear.platform.rest;

import static java.util.stream.Collectors.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;
import static app.allclear.platform.type.Symptom.*;

import java.util.*;
import java.util.stream.Stream;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.junit.hibernate.*;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ValidationExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.SymptomsLogFilter;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the SymptomsLog entity.
*
*	@author smalleyd
*	@version 1.0.80
*	@since April 8, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class SymptomsLogResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static final FakeRedisClient redis = new FakeRedisClient();
	private static SymptomsLogDAO dao = null;
	private static PeopleDAO peopleDao = null;
	private static final SessionDAO sessionDao = new SessionDAO(redis, ConfigTest.loadTest());
	private static SessionValue ADMIN = null;
	private static PeopleValue PERSON = null;
	private static SessionValue PERSON_ = null;
	private static PeopleValue PERSON_1 = null;
	private static SessionValue PERSON_1_ = null;

	private static int ENDED_PERSON = 0;
	private static int ENDED_PERSON_1 = 0;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new ValidationExceptionMapper())
		.addResource(new SymptomsLogResource(dao, sessionDao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/symptomsLogs";

	/** Generic types for reading values from responses. */
	private static final GenericType<QueryResults<SymptomsLogValue, SymptomsLogFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<SymptomsLogValue, SymptomsLogFilter>>() {};

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new SymptomsLogDAO(factory);
		peopleDao = new PeopleDAO(factory);
	}

	public static Stream<Arguments> search()
	{
		var size = LIST.size();
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();
		var total = LIST.size() * 2;

		return Stream.of(
			arguments(ADMIN, new SymptomsLogFilter(1, 100), total),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withPersonId(PERSON_1.id), LIST.size()),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withSymptomId(DIARRHEA.id), 2),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withStartedAtFrom(hourAgo), total),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withStartedAtTo(hourAhead), total),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withStartedAtFrom(hourAhead), 0),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withStartedAtTo(hourAgo), 0),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withHasEndedAt(true), ENDED_PERSON + ENDED_PERSON_1),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withHasEndedAt(false), total - ENDED_PERSON - ENDED_PERSON_1),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withEndedAtFrom(hourAgo), ENDED_PERSON + ENDED_PERSON_1),
			arguments(ADMIN, new SymptomsLogFilter(1, 100).withEndedAtTo(hourAhead), ENDED_PERSON + ENDED_PERSON_1),
			arguments(PERSON_, new SymptomsLogFilter(1, 100), size),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withPersonId(PERSON_1.id), size),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withSymptomId(RUNNY_NOSE.id), 1),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withStartedAtFrom(hourAgo), size),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withStartedAtTo(hourAhead), size),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withStartedAtFrom(hourAhead), 0),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withStartedAtTo(hourAgo), 0),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withHasEndedAt(true), ENDED_PERSON),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withHasEndedAt(false), size - ENDED_PERSON),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withEndedAtFrom(hourAgo), ENDED_PERSON),
			arguments(PERSON_, new SymptomsLogFilter(1, 100).withEndedAtTo(hourAhead), ENDED_PERSON),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100), size),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withPersonId(null), size),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withSymptomId(NONE.id), 1),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withStartedAtFrom(hourAgo()), size),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withStartedAtTo(hourAhead), size),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withStartedAtFrom(hourAhead), 0),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withStartedAtTo(hourAgo), 0),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withHasEndedAt(true), ENDED_PERSON_1),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withHasEndedAt(false), size - ENDED_PERSON_1),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withEndedAtFrom(hourAgo), ENDED_PERSON_1),
			arguments(PERSON_1_, new SymptomsLogFilter(1, 100).withEndedAtTo(hourAhead), ENDED_PERSON_1)
			);
	}

	@Test
	public void add()
	{
		ADMIN = sessionDao.add(new AdminValue("admin", true), false);
		PERSON_ = sessionDao.add(PERSON = peopleDao.add(new PeopleValue("once", "888-555-1000", true).withSymptoms(LIST.stream().map(v -> v.created()).collect(toList()))), false);
		PERSON_1_ = sessionDao.add(PERSON_1 = peopleDao.add(new PeopleValue("twice", "888-555-1001", true).withSymptoms(LIST.stream().map(v -> v.created()).collect(toList()))), false);
	}

	@ParameterizedTest
	@MethodSource("search")
	public void add_search(final SessionValue user, final SymptomsLogFilter filter, final int total)
	{
		sessionDao.current(user);

		var response = request("search").post(Entity.json(filter));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals((long) total, results.total, "Check total");
	}

	@Test
	public void modify()
	{
		var exclusions = Set.of(FATIGUE.id, NONE.id, RUNNY_NOSE.id);

		peopleDao.update(PERSON.withSymptoms(List.of()));
		peopleDao.update(PERSON_1.withSymptoms(LIST.stream().filter(v -> !exclusions.contains(v.id)).map(v -> v.created()).collect(toList())));

		ENDED_PERSON = LIST.size();
		ENDED_PERSON_1 = exclusions.size();
	}

	@ParameterizedTest
	@MethodSource
	public void search(final SessionValue user, final SymptomsLogFilter filter, final int total)
	{
		sessionDao.current(user);

		var response = request("search").post(Entity.json(filter));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals((long) total, results.total, "Check total");
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
