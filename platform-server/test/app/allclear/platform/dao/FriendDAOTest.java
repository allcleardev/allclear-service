package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.platform.App;
import app.allclear.platform.entity.Friend;
import app.allclear.platform.entity.Friendship;
import app.allclear.platform.filter.FriendFilter;
import app.allclear.platform.filter.PeopleFilter;
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
	private static FriendValue VALUE_1 = null;
	private static PeopleValue PERSON = null;
	private static PeopleValue PERSON_1 = null;
	private static PeopleValue INACTIVE = null;

	private static Date ACCEPTED_AT;
	private static Date REJECTED_AT;
	private static Date ACCEPTED_AT_1;
	private static Date REJECTED_AT_1;

	private static FriendFilter acceptedAt() { return acceptedAt(ACCEPTED_AT); }
	private static FriendFilter acceptedAt1() { return acceptedAt(ACCEPTED_AT_1); }
	private static FriendFilter acceptedAt(final Date v) { return new FriendFilter().withAcceptedAtFrom(hours(v, -1)).withAcceptedAtTo(hours(v, 1)); }

	private static FriendFilter rejectedAt() { return rejectedAt(REJECTED_AT); }
	private static FriendFilter rejectedAt1() { return rejectedAt(REJECTED_AT_1); }
	private static FriendFilter rejectedAt(final Date v) { return new FriendFilter().withRejectedAtFrom(hours(v, -1)).withRejectedAtTo(hours(v, 1)); }

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new FriendDAO(factory);
		peopleDao = new PeopleDAO(factory);

		ACCEPTED_AT = timestamp("2020-04-25T20:27:15-0000");
		REJECTED_AT = timestamp("2020-04-26T20:27:15-0000");
		ACCEPTED_AT_1 = timestamp("2020-04-27T20:27:15-0000");
		REJECTED_AT_1 = timestamp("2020-04-28T20:27:15-0000");
	}

	@Test
	public void add()
	{
		PERSON = peopleDao.add(new PeopleValue("first", "888-555-1000", true));
		PERSON_1 = peopleDao.add(new PeopleValue("second", "888-555-1001", true));
		INACTIVE = peopleDao.add(new PeopleValue("inactive", "888-555-2000", false));

		var value = dao.add(VALUE = new FriendValue(PERSON.id, PERSON_1.id, ACCEPTED_AT, REJECTED_AT));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Creates a valid Friend value for the validation tests.
	 *	@return never NULL.
	*/
	private FriendValue createValid()
	{
		return new FriendValue(PERSON_1.id, PERSON.id, ACCEPTED_AT_1, REJECTED_AT_1);
	}

	@Test
	public void add_dupe()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(new FriendValue(PERSON.id, PERSON_1.id))))
			.hasMessage("The friendship request already exists.");
	}

	@Test
	public void add_self()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(new FriendValue(PERSON.id, PERSON.id))))
			.hasMessage("You cannot send a friend request to yourself.");
	}

	@Test
	public void add_inactivePerson()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(new FriendValue(INACTIVE.id, PERSON.id))))
			.hasMessage("The Person ID, " + INACTIVE.id + ", is invalid.");
	}

	@Test
	public void add_inactiveInvitee()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(new FriendValue(PERSON.id, INACTIVE.id))))
			.hasMessage("The Invitee ID, " + INACTIVE.id + ", is invalid.");
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
	public void find()
	{
		var record = dao.findWithException(VALUE.personId, VALUE.inviteeId);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	@Test
	public void findWithException_invalid_personId()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException("INVALID", VALUE.inviteeId));
	}

	@Test
	public void findWithException_invalid_inviteeId()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.personId, "INVALID"));
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
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException("INVALID", VALUE.inviteeId));
	}

	@Test
	public void getWithException_invalid_inviteeId()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException(VALUE.personId, "INVALID"));
	}

	@Test
	public void modify()
	{
		count(acceptedAt(), 1L);
		count(rejectedAt(), 1L);
		count(acceptedAt1(), 0L);
		count(rejectedAt1(), 0L);

		var value = dao.update(VALUE.withAcceptedAt(ACCEPTED_AT_1).withRejectedAt(REJECTED_AT_1));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void modify_count()
	{
		count(acceptedAt(), 0L);
		count(rejectedAt(), 0L);
		count(acceptedAt1(), 1L);
		count(rejectedAt1(), 1L);
	}

	@Test
	public void modify_find()
	{
		var record = dao.findWithException(VALUE.personId, VALUE.inviteeId);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(ACCEPTED_AT_1, record.getAcceptedAt(), "Check acceptedAt");
		Assertions.assertEquals(REJECTED_AT_1, record.getRejectedAt(), "Check rejectedAt");
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
			arguments(acceptedAt1(), 1L),
			arguments(new FriendFilter(1, 20).withHasRejectedAt(true), 1L),
			arguments(rejectedAt1(), 1L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new FriendFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new FriendFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new FriendFilter(1, 20).withPersonId(VALUE.inviteeId), 0L),
			arguments(new FriendFilter(1, 20).withInviteeId(VALUE.personId), 0L),
			arguments(new FriendFilter(1, 20).withUserId(VALUE.personId), 0L),	// The creator of the invitation canNOT see rejected requests.
			arguments(new FriendFilter(1, 20).withHasAcceptedAt(false), 0L),
			arguments(acceptedAt(), 0L),
			arguments(new FriendFilter(1, 20).withHasRejectedAt(false), 0L),
			arguments(rejectedAt(), 0L),
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
	public void search_sort(final FriendFilter filter, final String expectedSortOn, final String expectedSortDir)
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
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.personId, VALUE.inviteeId));
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_search()
	{
		count(new FriendFilter().withPersonId(VALUE.personId).withInviteeId(VALUE.inviteeId), 0L);
		count(acceptedAt1(), 0L);
		count(rejectedAt1(), 0L);
	}

	@Test
	public void y_00_add_multiple()
	{
		Assertions.assertNotNull(VALUE = dao.add(new FriendValue(PERSON.id, PERSON_1.id)), "Check first");
		Assertions.assertNotNull(VALUE_1 = dao.add(new FriendValue(PERSON_1.id, PERSON.id)), "Check second");
	}

	@Test
	public void y_00_add_multiple_check()
	{
		var filter = new FriendFilter();
		count(filter, 2L);
		search(filter, 2L);
	}

	@Test
	public void y_00_clear()
	{
		Assertions.assertTrue(dao.remove(VALUE), "Check first");
		Assertions.assertTrue(dao.remove(VALUE_1), "Check second");
	}

	@Test
	public void y_00_clear_check()
	{
		var filter = new FriendFilter();
		count(filter, 0L);
		search(filter, 0L);
	}

	@Test
	public void z_00()
	{
		var o = dao.start(PERSON_1, PERSON.id);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(PERSON_1.id, o.personId, "Check personId");
		Assertions.assertEquals(PERSON.id, o.inviteeId, "Check inviteeId");
		Assertions.assertNull(o.acceptedAt, "Check acceptedAt");
		Assertions.assertNull(o.rejectedAt, "Check rejectedAt");
	}

	@Test
	public void z_00_check()
	{
		var o = dao.getById(PERSON_1.id, PERSON.id);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(PERSON_1.id, o.personId, "Check personId");
		Assertions.assertEquals(PERSON.id, o.inviteeId, "Check inviteeId");
		Assertions.assertNull(o.acceptedAt, "Check acceptedAt");
		Assertions.assertNull(o.rejectedAt, "Check rejectedAt");
		assertThat(dao.ships(PERSON_1.id, PERSON.id)).as("Check ships").isEmpty();
		assertThat(peopleIds(PERSON_1.id, null, null)).as("Check friends").containsExactly(PERSON.id);
		assertThat(peopleIds(PERSON.id, null, null)).as("Check friends").isNullOrEmpty();
		assertThat(peopleIds(null, PERSON_1.id, null)).as("Check invitees").isNullOrEmpty();
		assertThat(peopleIds(null, PERSON.id, null)).as("Check invitees").containsExactly(PERSON_1.id);
		assertThat(peopleIds(null, null, PERSON_1.id)).as("Check friendships").isNullOrEmpty();
		assertThat(peopleIds(null, null, PERSON.id)).as("Check friendships").isNullOrEmpty();
	}

	@Test
	public void z_00_check_again() { z_00(); }

	@Test
	public void z_00_check_again_check() { z_00_check(); }

	@Test
	public void z_00_empty_inviteeId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(PERSON_1, "  ")))
			.hasMessage("Invitee is not set.");
	}

	@Test
	public void z_00_empty_personId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(new PeopleValue().withId(" \t \t \n "), PERSON.id)))
			.hasMessage("Person is not set.");
	}

	@Test
	public void z_00_inactive_inviteeId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(PERSON_1, INACTIVE.id)))
			.hasMessage("The Invitee ID, " + INACTIVE.id + ", is invalid.");
	}

	@Test
	public void z_00_inactive_personId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(INACTIVE, PERSON.id)))
			.hasMessage("The Person ID, " + INACTIVE.id + ", is invalid.");
	}

	@Test
	public void z_00_invalid_acceptance()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.accept(PERSON_1, PERSON.id)))
			.hasMessage("The friendship request does not exist.");
	}

	@Test
	public void z_00_invalid_inviteeId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(PERSON_1, "INVALID")))
			.hasMessage("The Invitee ID, INVALID, is invalid.");
	}

	@Test
	public void z_00_invalid_personId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(new PeopleValue().withId("INVALID"), PERSON.id)))
			.hasMessage("The Person ID, INVALID, is invalid.");
	}

	@Test
	public void z_00_invalid_rejection()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.reject(PERSON_1, PERSON.id)))
			.hasMessage("The friendship request does not exist.");
	}

	@Test
	public void z_00_long_inviteeId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(PERSON_1, StringUtils.repeat('A', PeopleValue.MAX_LEN_ID + 1))))
			.hasMessage("Invitee 'AAAAAAAAAAA' is longer than the expected size of 10.");
	}

	@Test
	public void z_00_long_personId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(new PeopleValue().withId(StringUtils.repeat('A', PeopleValue.MAX_LEN_ID + 1)), PERSON.id)))
			.hasMessage("Person 'AAAAAAAAAAA' is longer than the expected size of 10.");
	}

	@Test
	public void z_00_missing_inviteeId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(PERSON_1, null)))
			.hasMessage("Invitee is not set.");
	}

	@Test
	public void z_00_missing_personId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(new PeopleValue().withId(null), PERSON.id)))
			.hasMessage("Person is not set.");
	}

	@Test
	public void z_01_accept()
	{
		assertThat(dao.ships(PERSON_1.id, PERSON.id)).as("Check ships").isEmpty();

		var o = dao.accept(PERSON, PERSON_1.id);	// Invitee calls the accept method so is first.
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(PERSON_1.id, o.personId, "Check personId");
		Assertions.assertEquals(PERSON.id, o.inviteeId, "Check inviteeId");
		assertThat(o.acceptedAt).as("Check acceptedAt").isNotNull().isAfter(o.createdAt).isCloseTo(new Date(), 500L);
		Assertions.assertNull(o.rejectedAt, "Check rejectedAt");
	}

	private static Friendship ship(final PeopleValue person, final PeopleValue friend) { return new Friendship(person.id, friend.id, null); }

	@Test
	public void z_01_accept_check()
	{
		var o = dao.getById(PERSON_1.id, PERSON.id);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(PERSON_1.id, o.personId, "Check personId");
		Assertions.assertEquals(PERSON.id, o.inviteeId, "Check inviteeId");
		assertThat(o.acceptedAt).as("Check acceptedAt").isNotNull().isAfter(o.createdAt).isCloseTo(new Date(), 500L);
		Assertions.assertNull(o.rejectedAt, "Check rejectedAt");
		assertThat(dao.ships(PERSON_1.id, PERSON.id)).as("Check ships").containsOnly(ship(PERSON, PERSON_1), ship(PERSON_1, PERSON));
		assertThat(peopleIds(PERSON_1.id, null, null)).as("Check friends").containsExactly(PERSON.id);
		assertThat(peopleIds(PERSON.id, null, null)).as("Check friends").isNullOrEmpty();
		assertThat(peopleIds(null, PERSON_1.id, null)).as("Check invitees").isNullOrEmpty();
		assertThat(peopleIds(null, PERSON.id, null)).as("Check invitees").containsExactly(PERSON_1.id);
		assertThat(peopleIds(null, null, PERSON_1.id)).as("Check friendships").containsExactly(PERSON.id);
		assertThat(peopleIds(null, null, PERSON.id)).as("Check friendships").containsExactly(PERSON_1.id);
	}

	@Test
	public void z_01_accept_done()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.accept(PERSON, PERSON_1.id)))
			.hasMessage("The friendship request has already been accepted.");
	}

	@Test
	public void z_01_accept_done_check() { z_01_accept_check(); }

	@Test
	public void z_01_accept_now_start_again()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(PERSON_1, PERSON.id)))
			.hasMessage("The friendship request has already been accepted.");
	}

	@Test
	public void z_01_accept_now_start_again_check() { z_01_accept_done_check(); }

	@Test
	public void z_01_accept_reject()
	{
		var o = dao.reject(PERSON, PERSON_1.id);	// Invitee calls the reject method so is first.
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(PERSON_1.id, o.personId, "Check personId");
		Assertions.assertEquals(PERSON.id, o.inviteeId, "Check inviteeId");
		Assertions.assertNull(o.acceptedAt, "Check acceptedAt");
		assertThat(o.rejectedAt).as("Check rejectedAt").isNotNull().isAfter(o.createdAt).isCloseTo(new Date(), 500L);
	}

	@Test
	public void z_01_accept_reject_check()
	{
		var o = dao.getById(PERSON_1.id, PERSON.id);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(PERSON_1.id, o.personId, "Check personId");
		Assertions.assertEquals(PERSON.id, o.inviteeId, "Check inviteeId");
		Assertions.assertNull(o.acceptedAt, "Check acceptedAt");
		assertThat(o.rejectedAt).as("Check rejectedAt").isNotNull().isAfter(o.createdAt).isCloseTo(new Date(), 500L);
		assertThat(dao.ships(PERSON_1.id, PERSON.id)).as("Check ships").isEmpty();
		assertThat(peopleIds(PERSON_1.id, null, null)).as("Check friends").containsExactly(PERSON.id);
		assertThat(peopleIds(PERSON.id, null, null)).as("Check friends").isNullOrEmpty();
		assertThat(peopleIds(null, PERSON_1.id, null)).as("Check invitees").isNullOrEmpty();
		assertThat(peopleIds(null, PERSON.id, null)).as("Check invitees").containsExactly(PERSON_1.id);
		assertThat(peopleIds(null, null, PERSON_1.id)).as("Check friendships").isNullOrEmpty();
		assertThat(peopleIds(null, null, PERSON.id)).as("Check friendships").isNullOrEmpty();
	}

	@Test
	public void z_01_accept_reject_done()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.reject(PERSON, PERSON_1.id)))
			.hasMessage("The friendship request has already been rejected.");
	}

	@Test
	public void z_01_accept_reject_done_check() { z_01_accept_reject_check(); }

	@Test
	public void z_01_accept_reject_now_start_again()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.start(PERSON_1, PERSON.id)))
			.hasMessage("The friendship request cannot be reissued.");
	}

	@Test
	public void z_01_accept_reject_now_start_again_check() { z_01_accept_reject_done_check(); }

	@Test
	public void z_01_accept_reject_reaccept()
	{
		var o = dao.accept(PERSON, PERSON_1.id);	// Invitee calls the accept method so is first.
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(PERSON_1.id, o.personId, "Check personId");
		Assertions.assertEquals(PERSON.id, o.inviteeId, "Check inviteeId");
		assertThat(o.acceptedAt).as("Check acceptedAt").isNotNull().isAfter(o.createdAt).isCloseTo(new Date(), 500L);
		Assertions.assertNull(o.rejectedAt, "Check rejectedAt");
	}

	@Test
	public void z_01_accept_reject_reaccept_check()
	{
		var o = dao.getById(PERSON_1.id, PERSON.id);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(PERSON_1.id, o.personId, "Check personId");
		Assertions.assertEquals(PERSON.id, o.inviteeId, "Check inviteeId");
		assertThat(o.acceptedAt).as("Check acceptedAt").isNotNull().isAfter(o.createdAt).isCloseTo(new Date(), 500L);
		Assertions.assertNull(o.rejectedAt, "Check rejectedAt");
		assertThat(dao.ships(PERSON_1.id, PERSON.id)).as("Check ships").containsOnly(ship(PERSON, PERSON_1), ship(PERSON_1, PERSON));
		assertThat(peopleIds(PERSON_1.id, null, null)).as("Check friends").containsExactly(PERSON.id);
		assertThat(peopleIds(PERSON.id, null, null)).as("Check friends").isNullOrEmpty();
		assertThat(peopleIds(null, PERSON_1.id, null)).as("Check invitees").isNullOrEmpty();
		assertThat(peopleIds(null, PERSON.id, null)).as("Check invitees").containsExactly(PERSON_1.id);
		assertThat(peopleIds(null, null, PERSON_1.id)).as("Check friendships").containsExactly(PERSON.id);
		assertThat(peopleIds(null, null, PERSON.id)).as("Check friendships").containsExactly(PERSON_1.id);
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

	private List<String> peopleIds(final String friendId, final String inviteeId, final String friendshipId)
	{
		var results = peopleDao.search(new PeopleFilter().withFriendId(friendId).withInviteeId(inviteeId).withFriendshipId(friendshipId));
		return results.noRecords() ? null : results.records.stream().map(v -> v.id).collect(toList());
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
