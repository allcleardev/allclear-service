package app.allclear.platform.dao;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.platform.App;
import app.allclear.platform.filter.AuditLogFilter;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the AuditLog entity.
*
*	@author smalleyd
*	@version 1.1.46
*	@since May 10, 2020
*
**********************************************************************************/

@Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class AuditLogDAOTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);	// Transactions are need to trigger the afterTrans action in the AuditLogDAO.insert method. DLS on 5/10/2020.

	private static AuditLogDAO dao = null;
	private static final SessionDAO sessionDao = new FakeSessionDAO();
	private static final FacilityValue AUDITABLE = new FacilityValue(-1).withId(-1L);
	private static AuditLogValue VALUE;
	private static SessionValue ADMIN = new SessionValue(false, new AdminValue("linda", false, false));
	private static SessionValue EDITOR = new SessionValue(false, new AdminValue("maureen", false, true));
	private static SessionValue SUPER = new SessionValue(false, new AdminValue("allen", true, false));

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new AuditLogDAO(factory, sessionDao, System.getenv("AUDIT_LOG_CONNECTION_STRING"));
	}

	@AfterAll
	public static void down() throws Exception
	{
		Assertions.assertEquals(3, dao.clear(AUDITABLE.tableName(), "-1"));
	}

	@Test
	public void add()
	{
		count(new AuditLogFilter().withId("-1"), 0L);

		sessionDao.current(ADMIN);
		var value = VALUE = dao.add(AUDITABLE.withName("Being Added"));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void modify()
	{
		count(new AuditLogFilter().withId("-1"), 1L);

		sessionDao.current(EDITOR);
		var value = VALUE = dao.update(AUDITABLE.withName("Being Modified"));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void remove()
	{
		count(new AuditLogFilter().withId("-1"), 2L);

		sessionDao.current(SUPER);
		var value = VALUE = dao.remove(AUDITABLE.withName("Being Removed"));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new AuditLogFilter(1, 20).withId("-1"), 3L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionAt(VALUE.actionAt), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActorType("Admin"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActorType("Editor"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActorType("Super"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionBy("linda"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionBy("maureen"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withActionBy("allen"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("add"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("update"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withAction("remove"), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withPayload(VALUE.payload), 1L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampFrom(hourAgo), 3L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampTo(hourAhead), 3L),
			arguments(new AuditLogFilter(1, 20).withId("-1").withTimestampFrom(hourAgo).withTimestampTo(hourAhead), 3L),

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
		var results = dao.search(AUDITABLE.tableName(), filter);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedTotal, results.total, "Check total");
		if (0L == expectedTotal)
			Assertions.assertNull(results.records, "Records exist");
		else
		{
			Assertions.assertNotNull(results.records, "Records exists");
			int total = (int) expectedTotal;
			if (total > results.pageSize)
			{
				if (results.page == results.pages)
					total%= results.pageSize;
				else
					total = results.pageSize;
			}
			Assertions.assertEquals(total, results.records.size(), "Check records.size");
		}
	}

	public static Stream<Arguments> search_sort()
	{
		return Stream.of(
			arguments(new AuditLogFilter("id", null), "id", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AuditLogFilter("id", "ASC"), "id", "ASC"),
			arguments(new AuditLogFilter("id", "asc"), "id", "ASC"),
			arguments(new AuditLogFilter("id", "invalid"), "id", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AuditLogFilter("id", "DESC"), "id", "DESC"),
			arguments(new AuditLogFilter("id", "desc"), "id", "DESC"),

			arguments(new AuditLogFilter("actionAt", null), "actionAt", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AuditLogFilter("actionAt", "ASC"), "actionAt", "ASC"),
			arguments(new AuditLogFilter("actionAt", "asc"), "actionAt", "ASC"),
			arguments(new AuditLogFilter("actionAt", "invalid"), "actionAt", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AuditLogFilter("actionAt", "DESC"), "actionAt", "DESC"),
			arguments(new AuditLogFilter("actionAt", "desc"), "actionAt", "DESC"),

			arguments(new AuditLogFilter("actorType", null), "actorType", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AuditLogFilter("actorType", "ASC"), "actorType", "ASC"),
			arguments(new AuditLogFilter("actorType", "asc"), "actorType", "ASC"),
			arguments(new AuditLogFilter("actorType", "invalid"), "actorType", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AuditLogFilter("actorType", "DESC"), "actorType", "DESC"),
			arguments(new AuditLogFilter("actorType", "desc"), "actorType", "DESC"),

			arguments(new AuditLogFilter("actionBy", null), "actionBy", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AuditLogFilter("actionBy", "ASC"), "actionBy", "ASC"),
			arguments(new AuditLogFilter("actionBy", "asc"), "actionBy", "ASC"),
			arguments(new AuditLogFilter("actionBy", "invalid"), "actionBy", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AuditLogFilter("actionBy", "DESC"), "actionBy", "DESC"),
			arguments(new AuditLogFilter("actionBy", "desc"), "actionBy", "DESC"),

			arguments(new AuditLogFilter("action", null), "action", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AuditLogFilter("action", "ASC"), "action", "ASC"),
			arguments(new AuditLogFilter("action", "asc"), "action", "ASC"),
			arguments(new AuditLogFilter("action", "invalid"), "action", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AuditLogFilter("action", "DESC"), "action", "DESC"),
			arguments(new AuditLogFilter("action", "desc"), "action", "DESC"),

			arguments(new AuditLogFilter("payload", null), "payload", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AuditLogFilter("payload", "ASC"), "payload", "ASC"),
			arguments(new AuditLogFilter("payload", "asc"), "payload", "ASC"),
			arguments(new AuditLogFilter("payload", "invalid"), "payload", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AuditLogFilter("payload", "DESC"), "payload", "DESC"),
			arguments(new AuditLogFilter("payload", "desc"), "payload", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	public void search_sort(final AuditLogFilter filter, final String expectedSortOn, final String expectedSortDir)
	{
		var results = dao.search(AUDITABLE.tableName(), filter);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedSortOn, results.sortOn, "Check sortOn");
		Assertions.assertEquals(expectedSortDir, results.sortDir, "Check sortDir");
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final AuditLogFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(AUDITABLE.tableName(), filter), "COUNT " + filter + ": Check total");
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
