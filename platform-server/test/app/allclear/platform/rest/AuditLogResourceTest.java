package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

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
import app.allclear.platform.dao.AuditLogDAO;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.filter.AuditLogFilter;
import app.allclear.platform.value.AdminValue;
import app.allclear.platform.value.AuditLogValue;
import app.allclear.platform.value.FacilityValue;
import app.allclear.platform.value.SessionValue;

/**********************************************************************************
*
*	Functional test for the RESTful resource that handles access to the AuditLog entity.
*
*	@author smalleyd
*	@version 1.1.46
*	@since May 10, 2020
*
**********************************************************************************/

@Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class AuditLogResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static AuditLogDAO dao = null;
	private static final FakeRedisClient redis = new FakeRedisClient();
	private static final SessionDAO sessionDao = new SessionDAO(redis, ConfigTest.loadTest());
	private static final FacilityValue AUDITABLE = new FacilityValue(-1).withId(-1L);
	private static AuditLogValue VALUE;
	private static SessionValue ADMIN = new SessionValue(false, new AdminValue("linda", false, false));
	private static SessionValue EDITOR = new SessionValue(false, new AdminValue("maureen", false, true));
	private static SessionValue SUPER = new SessionValue(false, new AdminValue("allen", true, false));

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new ValidationExceptionMapper())
		.addResource(new AuditLogResource(dao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/auditLogs/facilities";

	/** Generic types for reading values from responses. */
	private static final GenericType<List<AuditLogValue>> TYPE_LIST_VALUE = new GenericType<List<AuditLogValue>>() {};
	private static final GenericType<QueryResults<AuditLogValue, AuditLogFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<AuditLogValue, AuditLogFilter>>() {};

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new AuditLogDAO(factory, sessionDao, System.getenv("AUDIT_LOG_CONNECTION_STRING"));
	}

	@AfterAll
	public static void down() throws Exception
	{
		Assertions.assertEquals(7, dao.clear(AUDITABLE.tableName(), "-1"));
	}

	@Test
	public void add()
	{
		count("-1", 0);

		sessionDao.current(ADMIN);
		var value = VALUE = dao.add(AUDITABLE.withName("Being Added"));
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("add", value.action, "Check action");
		check(VALUE, value);
	}

	@Test
	public void extend()
	{
		count("-1", 1);

		sessionDao.current(SUPER);
		var value = VALUE = dao.extend(AUDITABLE.withName("Being Extended"));
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("extend", value.action, "Check action");
		check(VALUE, value);
	}

	@Test
	public void lock()
	{
		count("-1", 2);

		sessionDao.current(ADMIN);
		var value = VALUE = dao.lock(AUDITABLE.withName("Being Locked"));
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("lock", value.action, "Check action");
		check(VALUE, value);
	}

	@Test
	public void modify()
	{
		count("-1", 3);

		sessionDao.current(EDITOR);
		var value = VALUE = dao.update(AUDITABLE.withName("Being Modified"));
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("update", value.action, "Check action");
		check(VALUE, value);
	}

	@Test
	public void release()
	{
		count("-1", 4);

		sessionDao.current(EDITOR);
		var value = VALUE = dao.release(AUDITABLE.withName("Being Released"));
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("release", value.action, "Check action");
		check(VALUE, value);
	}

	@Test
	public void remove()
	{
		count("-1", 5);

		sessionDao.current(SUPER);
		var value = VALUE = dao.remove(AUDITABLE.withName("Being Removed"));
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("remove", value.action, "Check action");
		check(VALUE, value);
	}

	@Test
	public void review()
	{
		count("-1", 6);

		sessionDao.current(SUPER);
		var value = VALUE = dao.review(AUDITABLE.withName("Being Reviewed"));
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals("review", value.action, "Check action");
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new AuditLogFilter(1, 20).withId("-1"), 7L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionAt(VALUE.actionAt), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActorType("Admin"), 2L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActorType("Editor"), 2L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActorType("Super"), 3L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionBy("linda"), 2L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionBy("maureen"), 2L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionBy("allen"), 3L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("add"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("extend"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("lock"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("update"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("release"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("remove"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("review"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withPayload(VALUE.payload), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampFrom(hourAgo), 7L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampTo(hourAhead), 7L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampFrom(hourAgo).withTimestampTo(hourAhead), 7L),

			// Negative tests
			arguments(new AuditLogFilter(1, 20).withId("-2"), 0L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionAt(VALUE.actionAt + 1000L), 0L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActorType("invalid"), 0L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionBy("invalid"), 0L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("invalid"), 0L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withPayload("invalid"), 0L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampFrom(hourAhead), 0L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampTo(hourAgo), 0L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampFrom(hourAhead).withTimestampTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final AuditLogFilter filter, final long expectedTotal)
	{
		var response = request("search")
			.post(Entity.entity(filter, UTF8MediaType.APPLICATION_JSON_TYPE));
		var assertId = "SEARCH " + filter + ": ";
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), assertId + "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assertions.assertNotNull(results, assertId + "Exists");
		Assertions.assertEquals(expectedTotal, results.total, assertId + "Check total");
		if (0L == expectedTotal)
			Assertions.assertNull(results.records, assertId + "Records exist");
		else
		{
			Assertions.assertNotNull(results.records, assertId + "Records exist");
			int total = (int) expectedTotal;
			if (total > results.pageSize)
			{
				if (results.page == results.pages)
					total%= results.pageSize;
				else
					total = results.pageSize;
			}
			Assertions.assertEquals(total, results.records.size(), assertId + "Check records.size");
		}
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final String id, final int expected)
	{
		var response = request(id).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(TYPE_LIST_VALUE)).hasSize(expected);
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final AuditLogValue expected, final AuditLogValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.actionAt, value.actionAt, assertId + "Check actionAt");
		Assertions.assertEquals(expected.actorType, value.actorType, assertId + "Check actorType");
		Assertions.assertEquals(expected.actionBy, value.actionBy, assertId + "Check actionBy");
		Assertions.assertEquals(expected.action, value.action, assertId + "Check action");
		Assertions.assertEquals(expected.payload, value.payload, assertId + "Check payload");
	}
}
