package app.allclear.platform.dao;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.ValidationException;
import app.allclear.platform.App;
import app.allclear.platform.entity.Friend;
import app.allclear.platform.filter.FriendFilter;
import app.allclear.platform.value.FriendValue;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Friend entity.
*
*	@author smalleyd
*	@version 1.1.9
*	@since April 27, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class FriendDAOTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static FriendDAO dao = null;
	private static PeopleDAO peopleDao = null;
	private static FriendValue VALUE = null;
	private static PeopleValue PERSON = null;
	private static PeopleValue PERSON_1 = null;

	private static Date ACCEPTED_AT;
	private static Date REJECTED_AT;
	private static Date ACCEPTED_AT_1;
	private static Date REJECTED_AT_1;

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new FriendDAO(factory);
		peopleDao = new PeopleDAO(factory);

		ACCEPTED_AT = timestamp("2020-04-27T20:27:15-0000");
		REJECTED_AT = timestamp("2020-04-27T20:37:15-0000");
		ACCEPTED_AT_1 = timestamp("2020-04-27T20:47:15-0000");
		REJECTED_AT_1 = timestamp("2020-04-27T20:57:15-0000");
	}

	@Test
	public void add()
	{
		PERSON = peopleDao.add(new PeopleValue("first", "888-555-1000", true));
		PERSON_1 = peopleDao.add(new PeopleValue("second", "888-555-1001", true));

		// TODO: populate the VALUE with data.
		var value = dao.add(VALUE = new FriendValue());
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Creates a valid Friend value for the validation tests.
	 *	@return never NULL.
	*/
	private FriendValue createValid()
	{
		// TODO: populate the valid value with data.
		return new FriendValue();
	}

	@Test
	public void add_missingPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId(null)));
	}

	@Test
	public void add_longPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId(StringUtils.repeat("A", FriendValue.MAX_LEN_PERSON_ID + 1))));
	}

	@Test
	public void add_invalidPersonId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPersonId("INVALID")));
	}

	@Test
	public void add_missingInviteeId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withInviteeId(null)));
	}

	@Test
	public void add_longInviteeId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withInviteeId(StringUtils.repeat("A", FriendValue.MAX_LEN_INVITEE_ID + 1))));
	}

	@Test
	public void add_invalidInviteeId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withInviteeId("INVALID")));
	}

	@Test
	public void add_missingCreatedAt()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withCreatedAt(null)));
	}

	@Test
	public void find()
	{
		var record = dao.findWithException(VALUE.personId, VALUE.inviteeId);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	@Test
	public void findWithException_invalid_personId()
	{
		assertThrows(ValidationException.class, () -> dao.findWithException("INVALID", VALUE.inviteeId));
	}

	@Test
	public void findWithException_invalid_inviteeId()
	{
		assertThrows(ValidationException.class, () -> dao.findWithException(VALUE.personId, "INVALID"));
	}

	@Test
	public void get()
	{
		var value = dao.getByIdWithException(VALUE.personId, VALUE.inviteeId);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void getWithException_invalid_personId()
	{
		assertThrows(ValidationException.class, () -> dao.getByIdWithException("INVALID", VALUE.inviteeId));
	}

	@Test
	public void getWithException_invalid_inviteeId()
	{
		assertThrows(ValidationException.class, () -> dao.getByIdWithException(VALUE.personId, "INVALID"));
	}

	@Test
	public void modify()
	{
		// TODO: fill in search details // count(new FriendFilter(), 1L);
		// TODO: fill in search details // count(new FriendFilter(), 0L);

		// TODO: provide a change to the VALUE.
		var value = dao.update(VALUE);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void modify_count()
	{
		// TODO: fill in search details // count(new FriendFilter(), 0L);
		// TODO: fill in search details // count(new FriendFilter(), 1L);
	}

	@Test
	public void modify_find()
	{
		var record = dao.findWithException(VALUE.personId, VALUE.inviteeId);
		Assertions.assertNotNull(record, "Exists");
		// TODO: check the changed property.
		check(VALUE, record);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new FriendFilter(1, 20).withPersonId(VALUE.personId), 1L),
			arguments(new FriendFilter(1, 20).withInviteeId(VALUE.inviteeId), 1L),
			arguments(new FriendFilter(1, 20).withUserId(VALUE.inviteeId), 1L),
			arguments(new FriendFilter(1, 20).withHasAcceptedAt(true), 1L),
			arguments(new FriendFilter(1, 20).withAcceptedAtFrom(hourAgo), 1L),
			arguments(new FriendFilter(1, 20).withAcceptedAtTo(hourAhead), 1L),
			arguments(new FriendFilter(1, 20).withAcceptedAtFrom(hourAgo).withAcceptedAtTo(hourAhead), 1L),
			arguments(new FriendFilter(1, 20).withHasRejectedAt(true), 1L),
			arguments(new FriendFilter(1, 20).withRejectedAtFrom(hourAgo), 1L),
			arguments(new FriendFilter(1, 20).withRejectedAtTo(hourAhead), 1L),
			arguments(new FriendFilter(1, 20).withRejectedAtFrom(hourAgo).withRejectedAtTo(hourAhead), 1L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new FriendFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new FriendFilter(1, 20).withPersonId("invalid"), 0L),
			arguments(new FriendFilter(1, 20).withInviteeId("invalid"), 0L),
			arguments(new FriendFilter(1, 20).withInviteeId("invalid"), 0L),
			arguments(new FriendFilter(1, 20).withHasAcceptedAt(false), 0L),
			arguments(new FriendFilter(1, 20).withAcceptedAtFrom(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withAcceptedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withAcceptedAtFrom(hourAhead).withAcceptedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withHasRejectedAt(false), 0L),
			arguments(new FriendFilter(1, 20).withRejectedAtFrom(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withRejectedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withRejectedAtFrom(hourAhead).withRejectedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new FriendFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final FriendFilter filter, final long expectedTotal)
	{
		var results = dao.search(filter);
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
			arguments(new FriendFilter("personId", null), "personId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FriendFilter("personId", "ASC"), "personId", "ASC"),
			arguments(new FriendFilter("personId", "asc"), "personId", "ASC"),
			arguments(new FriendFilter("personId", "invalid"), "personId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FriendFilter("personId", "DESC"), "personId", "DESC"),
			arguments(new FriendFilter("personId", "desc"), "personId", "DESC"),

			arguments(new FriendFilter("personName", null), "personName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FriendFilter("personName", "ASC"), "personName", "ASC"),
			arguments(new FriendFilter("personName", "asc"), "personName", "ASC"),
			arguments(new FriendFilter("personName", "invalid"), "personName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FriendFilter("personName", "DESC"), "personName", "DESC"),
			arguments(new FriendFilter("personName", "desc"), "personName", "DESC"),

			arguments(new FriendFilter("inviteeId", null), "inviteeId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FriendFilter("inviteeId", "ASC"), "inviteeId", "ASC"),
			arguments(new FriendFilter("inviteeId", "asc"), "inviteeId", "ASC"),
			arguments(new FriendFilter("inviteeId", "invalid"), "inviteeId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FriendFilter("inviteeId", "DESC"), "inviteeId", "DESC"),
			arguments(new FriendFilter("inviteeId", "desc"), "inviteeId", "DESC"),

			arguments(new FriendFilter("inviteeName", null), "inviteeName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FriendFilter("inviteeName", "ASC"), "inviteeName", "ASC"),
			arguments(new FriendFilter("inviteeName", "asc"), "inviteeName", "ASC"),
			arguments(new FriendFilter("inviteeName", "invalid"), "inviteeName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FriendFilter("inviteeName", "DESC"), "inviteeName", "DESC"),
			arguments(new FriendFilter("inviteeName", "desc"), "inviteeName", "DESC"),

			arguments(new FriendFilter("acceptedAt", null), "acceptedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FriendFilter("acceptedAt", "ASC"), "acceptedAt", "ASC"),
			arguments(new FriendFilter("acceptedAt", "asc"), "acceptedAt", "ASC"),
			arguments(new FriendFilter("acceptedAt", "invalid"), "acceptedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FriendFilter("acceptedAt", "DESC"), "acceptedAt", "DESC"),
			arguments(new FriendFilter("acceptedAt", "desc"), "acceptedAt", "DESC"),

			arguments(new FriendFilter("rejectedAt", null), "rejectedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FriendFilter("rejectedAt", "ASC"), "rejectedAt", "ASC"),
			arguments(new FriendFilter("rejectedAt", "asc"), "rejectedAt", "ASC"),
			arguments(new FriendFilter("rejectedAt", "invalid"), "rejectedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FriendFilter("rejectedAt", "DESC"), "rejectedAt", "DESC"),
			arguments(new FriendFilter("rejectedAt", "desc"), "rejectedAt", "DESC"),

			arguments(new FriendFilter("createdAt", null), "createdAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FriendFilter("createdAt", "ASC"), "createdAt", "ASC"),
			arguments(new FriendFilter("createdAt", "asc"), "createdAt", "ASC"),
			arguments(new FriendFilter("createdAt", "invalid"), "createdAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FriendFilter("createdAt", "DESC"), "createdAt", "DESC"),
			arguments(new FriendFilter("createdAt", "desc"), "createdAt", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	private void search_sort(final FriendFilter filter, final String expectedSortOn, final String expectedSortDir)
	{
		var results = dao.search(filter);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedSortOn, results.sortOn, "Check sortOn");
		Assertions.assertEquals(expectedSortDir, results.sortDir, "Check sortDir");
	}

	/** Test removal after the search. */
	@Test
	public void testRemove()
	{
		Assertions.assertFalse(dao.remove("INVALID", VALUE.inviteeId), "Invalid PersonId");
		Assertions.assertFalse(dao.remove(VALUE.personId, "INVALID"), "Invalid InviteeId");
		Assertions.assertTrue(dao.remove(VALUE.personId, VALUE.inviteeId), "Removed");
		Assertions.assertFalse(dao.remove(VALUE.personId, VALUE.inviteeId), "Already removed");
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_find()
	{
		assertThrows(ValidationException.class, () -> dao.findWithException(VALUE.personId, VALUE.inviteeId));
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_search()
	{
		count(new FriendFilter().withPersonId(VALUE.personId).withInviteeId(VALUE.inviteeId), 0L);
		// TODO: provide secondary test count.
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final FriendFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied entity record. */
	private void check(final FriendValue expected, final Friend record)
	{
		var assertId = "ID (" + expected.getId() + "): ";
		Assertions.assertEquals(expected.personId, record.getPersonId(), assertId + "Check personId");
		Assertions.assertEquals(expected.inviteeId, record.getInviteeId(), assertId + "Check inviteeId");
		Assertions.assertEquals(expected.acceptedAt, record.getAcceptedAt(), assertId + "Check acceptedAt");
		Assertions.assertEquals(expected.rejectedAt, record.getRejectedAt(), assertId + "Check rejectedAt");
		Assertions.assertEquals(expected.createdAt, record.getCreatedAt(), assertId + "Check createdAt");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final FriendValue expected, final FriendValue value)
	{
		var assertId = "ID (" + expected.getId() + "): ";
		Assertions.assertEquals(expected.personId, value.personId, assertId + "Check personId");
		Assertions.assertEquals(expected.personName, value.personName, assertId + "Check personName");
		Assertions.assertEquals(expected.inviteeId, value.inviteeId, assertId + "Check inviteeId");
		Assertions.assertEquals(expected.inviteeName, value.inviteeName, assertId + "Check inviteeName");
		Assertions.assertEquals(expected.acceptedAt, value.acceptedAt, assertId + "Check acceptedAt");
		Assertions.assertEquals(expected.rejectedAt, value.rejectedAt, assertId + "Check rejectedAt");
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
	}
}
