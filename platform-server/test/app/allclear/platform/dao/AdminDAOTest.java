package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.common.crypto.Hasher.saltAndHash;
import static app.allclear.testing.TestingUtils.*;

import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.entity.Admin;
import app.allclear.platform.filter.AdminFilter;
import app.allclear.platform.model.AuthenticationRequest;
import app.allclear.platform.model.ChangePasswordRequest;
import app.allclear.platform.value.AdminValue;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Admin entity.
*
*	@author smalleyd
*	@version 1.0.14
*	@since April 1, 2020
*
**********************************************************************************/

@Disabled
@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class AdminDAOTest
{
	private static AdminDAO dao;
	private static AdminValue VALUE = null;
	private static String CURRENT_PASSWORD;
	private static String NEW_PASSWORD;
	private static String OLD_PASSWORD = "old";

	@BeforeAll
	public static void up() throws Exception
	{
		dao = new AdminDAO(ConfigTest.loadTest().admins, "test");
	}

	@Test
	public void add()
	{
		var value = dao.add(VALUE = new AdminValue("~tester-b", CURRENT_PASSWORD = UUID.randomUUID().toString(), "dsmall@allclear.app", "Dave", "Small", true, false, true));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Creates a valid Admin value for the validation tests.
	 *	@return never NULL.
	*/
	private AdminValue createValid()
	{
		return new AdminValue("kathy", "Password_2", "kathy@gmail.com", "Kathy", "Reiner", false, false, false);
	}

	@Test
	public void add_missingId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withId(null)));
	}

	@Test
	public void add_longId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withId(StringUtils.repeat("A", AdminValue.MAX_LEN_ID + 1))));
	}

	@Test
	public void add_missingPassword()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPassword(null)));
	}

	@Test
	public void add_longPassword()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPassword(StringUtils.repeat("A", AdminValue.MAX_LEN_PASSWORD + 1))));
	}

	@Test
	public void add_shortPassword()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPassword(StringUtils.repeat("A", AdminValue.MIN_LEN_PASSWORD - 1))));
	}

	@Test
	public void add_missingEmail()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withEmail(null)));
	}

	@Test
	public void add_longEmail()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withEmail(StringUtils.repeat("A", AdminValue.MAX_LEN_EMAIL + 1))));
	}

	@Test
	public void add_missingFirstName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFirstName(null)));
	}

	@Test
	public void add_longFirstName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFirstName(StringUtils.repeat("A", AdminValue.MAX_LEN_FIRST_NAME + 1))));
	}

	@Test
	public void add_missingLastName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLastName(null)));
	}

	@Test
	public void add_longLastName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLastName(StringUtils.repeat("A", AdminValue.MAX_LEN_LAST_NAME + 1))));
	}

	@Test
	public void authenticate()
	{
		var value = dao.authenticate(new AuthenticationRequest("~tester-b", CURRENT_PASSWORD, false));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	public static Stream<Arguments> authenticate_fail()
	{
		return Stream.of(
				arguments("~tester-b", "Password_1"),
				arguments("~tester-b", "Password_2"),
				arguments("~tester-a", "Password_1"),
				arguments("kathy", "Password_2")
			);
	}

	@ParameterizedTest
	@MethodSource
	public void authenticate_fail(final String userName, final String password)
	{
		assertThrows(ValidationException.class, () -> dao.authenticate(new AuthenticationRequest(userName, password, false)));
	}

	@Test
	public void changePassword()
	{
		NEW_PASSWORD = UUID.randomUUID().toString();
		Assertions.assertNotNull(dao.changePassword(VALUE, new ChangePasswordRequest(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)));
	}

	@Test
	public void changePassword_check()
	{
		var value = dao.authenticate(new AuthenticationRequest("~tester-b", NEW_PASSWORD, false));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void changePassword_check_fail()
	{
		assertThrows(ValidationException.class, () -> dao.authenticate(new AuthenticationRequest("~tester-b", CURRENT_PASSWORD, false)));
	}

	public static Stream<Arguments> changePassword_failure()
	{
		var shortPwd = StringUtils.repeat('A', AdminValue.MIN_LEN_PASSWORD - 1);
		var longPwd = StringUtils.repeat('A', AdminValue.MAX_LEN_PASSWORD + 1);

		return Stream.of(
				arguments("", "Password_2", "Password_2", "Current Password is not set."),
				arguments(null, "Password_2", "Password_2", "Current Password is not set."),
				arguments(NEW_PASSWORD, "", "Password_2", "New Password is not set."),
				arguments(NEW_PASSWORD, null, "Password_2", "New Password is not set."),
				arguments(NEW_PASSWORD, shortPwd, shortPwd, "New Password cannot be shorter than 8 characters."),
				arguments(NEW_PASSWORD, longPwd, longPwd, "New Password cannot be longer than 40 characters."),
				arguments(NEW_PASSWORD, "Password_2", "", "The New Password does not match the Confirmation Password."),
				arguments(NEW_PASSWORD, "Password_2", null, "The New Password does not match the Confirmation Password."),
				arguments(NEW_PASSWORD, "Password_2", "Password_3", "The New Password does not match the Confirmation Password."),
				arguments(CURRENT_PASSWORD, "Password_2", "Password_2", "The Current Password is invalid.")
			);
	}

	@ParameterizedTest
	@MethodSource
	public void changePassword_failure(final String currentPassword, final String newPassword, final String confirmPassword, final String message)
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.changePassword(VALUE, new ChangePasswordRequest(currentPassword, newPassword, confirmPassword))))
			.hasMessage(message);
	}

	@Test
	public void changePassword_invalidUser()
	{
		assertThrows(ObjectNotFoundException.class,
			() -> dao.changePassword(new AdminValue("invalid"), new ChangePasswordRequest(NEW_PASSWORD, "Password_2", "Password_2")));
	}

	@Test
	public void changePassword_z_final()
	{
		OLD_PASSWORD = CURRENT_PASSWORD;
		CURRENT_PASSWORD = NEW_PASSWORD;
	}

	@Test
	public void find()
	{
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	@Test
	public void findWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.id + "INVALID"));
	}

	@Test
	public void get()
	{
		var value = dao.getByIdWithException(VALUE.id);
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test
	public void getWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException(VALUE.id + "INVALID"));
	}

	@Test
	public void modify()
	{
		count(new AdminFilter().withFirstName("Dave"), 1L);
		count(new AdminFilter().withAlertable(true), 1L);
		count(new AdminFilter().withFirstName("David"), 0L);
		count(new AdminFilter().withAlertable(false), 0L);

		var value = dao.update(VALUE.withFirstName("David").withAlertable(false));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	@Test @Disabled
	public void modify_count()
	{
		count(new AdminFilter().withFirstName("Dave"), 0L);
		count(new AdminFilter().withAlertable(true), 0L);
		count(new AdminFilter().withFirstName("David"), 1L);
		count(new AdminFilter().withAlertable(false), 1L);
	}

	@Test
	public void modify_find()
	{
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals("David", record.getFirstName(), "Check firstName");
		Assertions.assertFalse(record.getAlertable(), "Check alertable");
		assertThat(record.getUpdatedAt()).as("Check updatedAt").isAfter(record.getCreatedAt());
		check(VALUE, record);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new AdminFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new AdminFilter(1, 20).withEmail(VALUE.email), 1L),
			arguments(new AdminFilter(1, 20).withFirstName(VALUE.firstName), 1L),
			arguments(new AdminFilter(1, 20).withLastName(VALUE.lastName), 1L),
			arguments(new AdminFilter(1, 20).withSupers(VALUE.supers), 1L),
			arguments(new AdminFilter(1, 20).withEditor(VALUE.editor), 1L),
			arguments(new AdminFilter(1, 20).withAlertable(VALUE.alertable), 1L),
			arguments(new AdminFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new AdminFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new AdminFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new AdminFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new AdminFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new AdminFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new AdminFilter(1, 20).withId("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withEmail("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withFirstName("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withLastName("invalid"), 0L),
			arguments(new AdminFilter(1, 20).withSupers(!VALUE.supers), 0L),
			arguments(new AdminFilter(1, 20).withEditor(!VALUE.editor), 0L),
			arguments(new AdminFilter(1, 20).withAlertable(!VALUE.alertable), 0L),
			arguments(new AdminFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new AdminFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new AdminFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new AdminFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new AdminFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new AdminFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final AdminFilter filter, final long expectedTotal)
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
			arguments(new AdminFilter("id", null), "id", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("id", "ASC"), "id", "ASC"),
			arguments(new AdminFilter("id", "asc"), "id", "ASC"),
			arguments(new AdminFilter("id", "invalid"), "id", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("id", "DESC"), "id", "DESC"),
			arguments(new AdminFilter("id", "desc"), "id", "DESC"),

			arguments(new AdminFilter("password", null), "password", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("password", "ASC"), "password", "ASC"),
			arguments(new AdminFilter("password", "asc"), "password", "ASC"),
			arguments(new AdminFilter("password", "invalid"), "password", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("password", "DESC"), "password", "DESC"),
			arguments(new AdminFilter("password", "desc"), "password", "DESC"),

			arguments(new AdminFilter("email", null), "email", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("email", "ASC"), "email", "ASC"),
			arguments(new AdminFilter("email", "asc"), "email", "ASC"),
			arguments(new AdminFilter("email", "invalid"), "email", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("email", "DESC"), "email", "DESC"),
			arguments(new AdminFilter("email", "desc"), "email", "DESC"),

			arguments(new AdminFilter("firstName", null), "firstName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("firstName", "ASC"), "firstName", "ASC"),
			arguments(new AdminFilter("firstName", "asc"), "firstName", "ASC"),
			arguments(new AdminFilter("firstName", "invalid"), "firstName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("firstName", "DESC"), "firstName", "DESC"),
			arguments(new AdminFilter("firstName", "desc"), "firstName", "DESC"),

			arguments(new AdminFilter("lastName", null), "lastName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("lastName", "ASC"), "lastName", "ASC"),
			arguments(new AdminFilter("lastName", "asc"), "lastName", "ASC"),
			arguments(new AdminFilter("lastName", "invalid"), "lastName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("lastName", "DESC"), "lastName", "DESC"),
			arguments(new AdminFilter("lastName", "desc"), "lastName", "DESC"),

			arguments(new AdminFilter("supers", null), "supers", "DESC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("supers", "ASC"), "supers", "ASC"),
			arguments(new AdminFilter("supers", "asc"), "supers", "ASC"),
			arguments(new AdminFilter("supers", "invalid"), "supers", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("supers", "DESC"), "supers", "DESC"),
			arguments(new AdminFilter("supers", "desc"), "supers", "DESC"),

			arguments(new AdminFilter("editor", null), "editor", "DESC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("editor", "ASC"), "editor", "ASC"),
			arguments(new AdminFilter("editor", "asc"), "editor", "ASC"),
			arguments(new AdminFilter("editor", "invalid"), "editor", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("editor", "DESC"), "editor", "DESC"),
			arguments(new AdminFilter("editor", "desc"), "editor", "DESC"),

			arguments(new AdminFilter("alertable", null), "alertable", "DESC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("alertable", "ASC"), "alertable", "ASC"),
			arguments(new AdminFilter("alertable", "asc"), "alertable", "ASC"),
			arguments(new AdminFilter("alertable", "invalid"), "alertable", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("alertable", "DESC"), "alertable", "DESC"),
			arguments(new AdminFilter("alertable", "desc"), "alertable", "DESC"),

			arguments(new AdminFilter("createdAt", null), "createdAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("createdAt", "ASC"), "createdAt", "ASC"),
			arguments(new AdminFilter("createdAt", "asc"), "createdAt", "ASC"),
			arguments(new AdminFilter("createdAt", "invalid"), "createdAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("createdAt", "DESC"), "createdAt", "DESC"),
			arguments(new AdminFilter("createdAt", "desc"), "createdAt", "DESC"),

			arguments(new AdminFilter("updatedAt", null), "updatedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new AdminFilter("updatedAt", "ASC"), "updatedAt", "ASC"),
			arguments(new AdminFilter("updatedAt", "asc"), "updatedAt", "ASC"),
			arguments(new AdminFilter("updatedAt", "invalid"), "updatedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new AdminFilter("updatedAt", "DESC"), "updatedAt", "DESC"),
			arguments(new AdminFilter("updatedAt", "desc"), "updatedAt", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	public void search_sort(final AdminFilter filter, final String expectedSortOn, final String expectedSortDir)
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
		Assertions.assertFalse(dao.remove(VALUE.id + "INVALID"), "Invalid");
		Assertions.assertTrue(dao.remove(VALUE.id), "Removed");
		Assertions.assertFalse(dao.remove(VALUE.id), "Already removed");
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_find()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.id));
	}

	/** Test removal after the search. */
	@Test
	public void testRemove_search()
	{
		count(new AdminFilter().withId(VALUE.id), 0L);
	}

	@Test
	public void z_add_editor()
	{
		var value = dao.add(createValid().withEditor(true));
		Assertions.assertTrue(value.editor, "Check editor");
		Assertions.assertFalse(value.canAdmin(), "Check canAdmin()");
	}

	@Test
	public void z_get_editor()
	{
		var value = VALUE = dao.getById("kathy");
		Assertions.assertTrue(value.editor, "Check editor");
		Assertions.assertFalse(value.canAdmin(), "Check canAdmin()");
	}

	@Test
	public void z_modify_editor()
	{
		var value = dao.update(VALUE.withEditor(false));
		Assertions.assertFalse(value.editor, "Check editor");
		Assertions.assertTrue(value.canAdmin(), "Check canAdmin()");
	}

	@Test
	public void z_modify_editor_get()
	{
		var value = VALUE = dao.getById("kathy");
		Assertions.assertFalse(value.editor, "Check editor");
		Assertions.assertTrue(value.canAdmin(), "Check canAdmin()");
	}

	@Test
	public void z_remove_editor()
	{
		Assertions.assertTrue(dao.remove("kathy"));
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final AdminFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied entity record. */
	private void check(final AdminValue expected, final Admin record)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, record.id(), assertId + "Check id");
		Assertions.assertEquals(saltAndHash(record.getIdentifier(), CURRENT_PASSWORD), record.getPassword(), assertId + "Check password");
		Assertions.assertNotEquals(saltAndHash(record.getIdentifier(), OLD_PASSWORD), record.getPassword(), assertId + "Check password");
		Assertions.assertEquals(expected.email, record.getEmail(), assertId + "Check email");
		Assertions.assertEquals(expected.firstName, record.getFirstName(), assertId + "Check firstName");
		Assertions.assertEquals(expected.lastName, record.getLastName(), assertId + "Check lastName");
		Assertions.assertEquals(expected.supers, record.getSupers(), assertId + "Check supers");
		Assertions.assertEquals(expected.editor, record.getEditor(), assertId + "Check editor");
		Assertions.assertEquals(expected.alertable, record.getAlertable(), assertId + "Check alertable");
		Assertions.assertEquals(expected.createdAt, record.getCreatedAt(), assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, record.getUpdatedAt(), assertId + "Check updatedAt");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final AdminValue expected, final AdminValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertNull(value.password, assertId + "Check password");	// Should always be NULL.
		Assertions.assertEquals(expected.email, value.email, assertId + "Check email");
		Assertions.assertEquals(expected.firstName, value.firstName, assertId + "Check firstName");
		Assertions.assertEquals(expected.lastName, value.lastName, assertId + "Check lastName");
		Assertions.assertEquals(expected.supers, value.supers, assertId + "Check supers");
		Assertions.assertEquals(expected.editor, value.editor, assertId + "Check editor");
		Assertions.assertEquals(!expected.editor || expected.supers, value.canAdmin(), assertId + "Check canAdmin()");
		Assertions.assertEquals(expected.alertable, value.alertable, assertId + "Check alertable");
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, value.updatedAt, assertId + "Check updatedAt");
	}
}
