package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
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
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.platform.App;
import app.allclear.platform.entity.People;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.type.PeopleStature;
import app.allclear.platform.type.PeopleStatus;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the People entity.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 23, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleDAOTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static PeopleDAO dao = null;
	private static final Date DOB = utc(1950, 7, 1);
	private static final Date DOB_1 = utc(1990, 7, 15);
	private static PeopleValue VALUE = null;

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new PeopleDAO(factory);
	}

	@Test
	public void add()
	{
		var value = dao.add(VALUE = new PeopleValue("ronny", "888-555-1000", "ronny@gmail.com", "Ronald", "Howard",
			DOB, PeopleStatus.INFECTED.id, PeopleStature.INFLUENCER.id, true));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Creates a valid People value for the validation tests.
	 *	@return never NULL.
	*/
	public static PeopleValue createValid()
	{
		return new PeopleValue("bryce", "888-555-1001", "dallas@gmail.com", "Dallas", "Drawoh",
			DOB_1, PeopleStatus.RECOVERED.id, PeopleStature.CELEBRITY.id, false);
	}

	@Test
	public void add_dupeName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withName("ronny")));
	}

	@Test
	public void add_missingName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withName(null)));
	}

	@Test
	public void add_longName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withName(StringUtils.repeat("A", PeopleValue.MAX_LEN_NAME + 1))));
	}

	@Test
	public void add_dupePhone()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPhone("888-555-1000")));
	}

	@Test
	public void add_missingPhone()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPhone(null)));
	}

	@Test
	public void add_longPhone()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPhone(StringUtils.repeat("A", PeopleValue.MAX_LEN_PHONE + 1))));
	}

	@Test
	public void add_longEmail()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withEmail(StringUtils.repeat("A", PeopleValue.MAX_LEN_EMAIL + 1))));
	}

	@Test
	public void add_longFirstName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withFirstName(StringUtils.repeat("A", PeopleValue.MAX_LEN_FIRST_NAME + 1))));
	}

	@Test
	public void add_longLastName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLastName(StringUtils.repeat("A", PeopleValue.MAX_LEN_LAST_NAME + 1))));
	}

	@Test
	public void add_invalidStatusId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withStatusId("?")));
	}

	@Test
	public void add_longStatusId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withStatusId(StringUtils.repeat("A", PeopleValue.MAX_LEN_STATUS_ID + 1))));
	}

	@Test
	public void add_invalidStatureId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withStatureId("?")));
	}

	@Test
	public void add_longStatureId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withStatureId(StringUtils.repeat("A", PeopleValue.MAX_LEN_STATURE_ID + 1))));
	}

	public static Stream<Arguments> check()
	{
		return Stream.of(
			arguments("888-555-1000", "ronny@gmail.com"),
			arguments("888-555-1000", null),
			arguments(null, "ronny@gmail.com"));
	}

	@Test
	public void authenticatedByPhone_invalid()
	{
		assertThat(Assertions.assertThrows(ObjectNotFoundException.class, () -> dao.authenticatedByPhone("ronny")))
			.hasMessage("Could not find the account with phone number 'ronny'.");	// Not the phone number.
	}

	@ParameterizedTest
	@MethodSource
	public void check(final String phone, final String email)
	{
		dao.check(phone, email);
	}

	public static Stream<Arguments> check_failure()
	{
		return Stream.of(
			arguments("888-555-1001", null, "The phone number '888-555-1001' is not associated with an existing user."),
			arguments(null, "dallas@gmail.com", "The email address 'dallas@gmail.com' is not associated with an existing user."),
			arguments("888-555-1001", "dallas@gmail.com", "The phone number '888-555-1001' is not associated with an existing user."),
			arguments("ronny@gmail.com", null, "The phone number 'ronny@gmail.com' is not associated with an existing user."),
			arguments(null, "888-555-1000", "The email address '888-555-1000' is not associated with an existing user."),
			arguments(null, null, "Please provide a phone number."));
	}

	@ParameterizedTest
	@MethodSource
	public void check_failure(final String phone, final String email, final String message)
	{
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.check(phone, email)))
			.hasMessage(message);
	}

	public static Stream<Arguments> existsByEmail()
	{
		return Stream.of(
			arguments("", false),
			arguments(null, false),
			arguments("ronny@gmail.com", true),
			arguments("888-555-1000", false),
			arguments("dallas@gmail.com", false),
			arguments("ronny@gmail.co", false),
			arguments("onny@gmail.com", false));
	}

	@ParameterizedTest
	@MethodSource
	public void existsByEmail(final String value, final boolean expected)
	{
		Assertions.assertEquals(expected, dao.existsByEmail(value));
	}

	public static Stream<Arguments> existsByPhone()
	{
		return Stream.of(
			arguments("", false),
			arguments(null, false),
			arguments("888-555-1000", true),
			arguments("ronny@gmail.com", false),
			arguments("888-555-1001", false),
			arguments("888-555-100", false),
			arguments("88-555-1000", false));
	}

	@ParameterizedTest
	@MethodSource
	public void existsByPhone(final String value, final boolean expected)
	{
		Assertions.assertEquals(expected, dao.existsByPhone(value));
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
	public void findByEmail()
	{
		var record = dao.findByEmail(VALUE.email);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	@Test
	public void findByEmail_invalid()
	{
		Assertions.assertNull(dao.findByEmail("invalid"));
	}

	@Test
	public void findByName()
	{
		var record = dao.find(VALUE.name);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	@Test
	public void findByName_invalid()
	{
		Assertions.assertNull(dao.find("invalid"));
	}

	@Test
	public void findByPhone()
	{
		var record = dao.findByPhone(VALUE.phone);
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	@Test
	public void findByPhone_invalid()
	{
		Assertions.assertNull(dao.findByPhone("invalid"));
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
	public void getActiveByIdOrName()
	{
		assertThat(dao.getActiveByIdOrName("invalid")).as("Invalid").isEmpty();
		assertThat(dao.getActiveByIdOrName(VALUE.id.substring(0, 3))).as("By partial ID").containsExactly(VALUE);
		assertThat(dao.getActiveByIdOrName(VALUE.name.substring(0, 3))).as("By partial name").containsExactly(VALUE);
	}

	@Test
	public void modify()
	{
		count(new PeopleFilter().withId(VALUE.id), 1L);
		count(new PeopleFilter().withName(VALUE.name), 1L);
		count(new PeopleFilter().withPhone(VALUE.phone), 1L);
		count(new PeopleFilter().withEmail(VALUE.email), 1L);
		count(new PeopleFilter().withFirstName(VALUE.firstName), 1L);
		count(new PeopleFilter().withLastName(VALUE.lastName), 1L);
		count(new PeopleFilter().withDob(VALUE.dob), 1L);
		count(new PeopleFilter().withStatusId(VALUE.statusId), 1L);
		count(new PeopleFilter().withStatureId(VALUE.statureId), 1L);
		count(new PeopleFilter().withActive(VALUE.active), 1L);

		var v = createValid();
		count(new PeopleFilter().withName(v.name), 0L);
		count(new PeopleFilter().withPhone(v.phone), 0L);
		count(new PeopleFilter().withEmail(v.email), 0L);
		count(new PeopleFilter().withFirstName(v.firstName), 0L);
		count(new PeopleFilter().withLastName(v.lastName), 0L);
		count(new PeopleFilter().withDob(v.dob), 0L);
		count(new PeopleFilter().withStatusId(v.statusId), 0L);
		count(new PeopleFilter().withStatureId(v.statureId), 0L);
		count(new PeopleFilter().withActive(v.active), 0L);

		var value = dao.update(v.withId(VALUE.id));
		Assertions.assertNotNull(value, "Exists");
		check(v, value);
	}

	@Test
	public void modify_count()
	{
		count(new PeopleFilter().withId(VALUE.id), 1L);
		count(new PeopleFilter().withName(VALUE.name), 0L);
		count(new PeopleFilter().withPhone(VALUE.phone), 0L);
		count(new PeopleFilter().withEmail(VALUE.email), 0L);
		count(new PeopleFilter().withFirstName(VALUE.firstName), 0L);
		count(new PeopleFilter().withLastName(VALUE.lastName), 0L);
		count(new PeopleFilter().withDob(VALUE.dob), 0L);
		count(new PeopleFilter().withStatusId(VALUE.statusId), 0L);
		count(new PeopleFilter().withStatureId(VALUE.statureId), 0L);
		count(new PeopleFilter().withActive(VALUE.active), 0L);

		var v = createValid();
		count(new PeopleFilter().withName(v.name), 1L);
		count(new PeopleFilter().withPhone(v.phone), 1L);
		count(new PeopleFilter().withEmail(v.email), 1L);
		count(new PeopleFilter().withFirstName(v.firstName), 1L);
		count(new PeopleFilter().withLastName(v.lastName), 1L);
		count(new PeopleFilter().withDob(v.dob), 1L);
		count(new PeopleFilter().withStatusId(v.statusId), 1L);
		count(new PeopleFilter().withStatureId(v.statureId), 1L);
		count(new PeopleFilter().withActive(v.active), 1L);

		VALUE = dao.getById(VALUE.id);
	}

	@Test
	public void modify_find()
	{
		var v = createValid();
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals(VALUE.id, record.getId(), "Check id");
		Assertions.assertEquals(v.name, record.getName(), "Check name");
		Assertions.assertEquals(v.phone, record.getPhone(), "Check phone");
		Assertions.assertEquals(v.email, record.getEmail(), "Check email");
		Assertions.assertEquals(v.firstName, record.getFirstName(), "Check firstName");
		Assertions.assertEquals(v.lastName, record.getLastName(), "Check lastName");
		Assertions.assertEquals(v.dob, record.getDob(), "Check dob");
		Assertions.assertEquals(v.statusId, record.getStatusId(), "Check statusId");
		Assertions.assertEquals(v.statureId, record.getStatureId(), "Check statureId");
		Assertions.assertEquals(v.active, record.isActive(), "Check active");
		assertThat(record.getUpdatedAt()).as("Check updatedAt").isAfter(record.getCreatedAt());
		check(VALUE, record);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new PeopleFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new PeopleFilter(1, 20).withName(VALUE.name), 1L),
			arguments(new PeopleFilter(1, 20).withPhone(VALUE.phone), 1L),
			arguments(new PeopleFilter(1, 20).withEmail(VALUE.email), 1L),
			arguments(new PeopleFilter(1, 20).withHasEmail(true), 1L),
			arguments(new PeopleFilter(1, 20).withFirstName(VALUE.firstName), 1L),
			arguments(new PeopleFilter(1, 20).withHasFirstName(true), 1L),
			arguments(new PeopleFilter(1, 20).withLastName(VALUE.lastName), 1L),
			arguments(new PeopleFilter(1, 20).withHasLastName(true), 1L),
			arguments(new PeopleFilter(1, 20).withDob(VALUE.dob), 1L),
			arguments(new PeopleFilter(1, 20).withHasDob(true), 1L),
			arguments(new PeopleFilter(1, 20).withDobFrom(days(VALUE.dob, -1)), 1L),
			arguments(new PeopleFilter(1, 20).withDobTo(days(VALUE.dob, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withDobFrom(days(VALUE.dob, -1)).withDobTo(days(VALUE.dob, 1)), 1L),
			arguments(new PeopleFilter(1, 20).withStatusId(VALUE.statusId), 1L),
			arguments(new PeopleFilter(1, 20).withHasStatusId(true), 1L),
			arguments(new PeopleFilter(1, 20).withStatureId(VALUE.statureId), 1L),
			arguments(new PeopleFilter(1, 20).withHasStatureId(true), 1L),
			arguments(new PeopleFilter(1, 20).withActive(VALUE.active), 1L),
			arguments(new PeopleFilter(1, 20).withHasAuthAt(false), 1L),
			/* arguments(new PeopleFilter(1, 20).withAuthAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withAuthAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hourAgo).withAuthAtTo(hourAhead), 1L), */
			arguments(new PeopleFilter(1, 20).withHasPhoneVerifiedAt(false), 1L),
			/* arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hourAgo).withPhoneVerifiedAtTo(hourAhead), 1L), */
			arguments(new PeopleFilter(1, 20).withHasEmailVerifiedAt(false), 1L),
			/* arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hourAgo).withEmailVerifiedAtTo(hourAhead), 1L), */
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),

			// Negative tests
			arguments(new PeopleFilter(1, 20).withId("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withName("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withPhone("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withEmail("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withHasEmail(false), 0L),
			arguments(new PeopleFilter(1, 20).withFirstName("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withHasFirstName(false), 0L),
			arguments(new PeopleFilter(1, 20).withLastName("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withHasLastName(false), 0L),
			arguments(new PeopleFilter(1, 20).withDob(DOB), 0L),
			arguments(new PeopleFilter(1, 20).withHasDob(false), 0L),
			arguments(new PeopleFilter(1, 20).withDobFrom(days(VALUE.dob, 1)), 0L),
			arguments(new PeopleFilter(1, 20).withDobTo(days(VALUE.dob, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withDobFrom(days(VALUE.dob, 1)).withDobTo(days(VALUE.dob, -1)), 0L),
			arguments(new PeopleFilter(1, 20).withStatusId("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withHasStatusId(false), 0L),
			arguments(new PeopleFilter(1, 20).withStatureId("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withHasStatureId(false), 0L),
			arguments(new PeopleFilter(1, 20).withActive(!VALUE.active), 0L),
			arguments(new PeopleFilter(1, 20).withHasAuthAt(true), 0L),
			/* arguments(new PeopleFilter(1, 20).withAuthAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withAuthAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withAuthAtFrom(hourAhead).withAuthAtTo(hourAgo), 0L), */
			arguments(new PeopleFilter(1, 20).withHasPhoneVerifiedAt(true), 0L),
			/* arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withPhoneVerifiedAtFrom(hourAhead).withPhoneVerifiedAtTo(hourAgo), 0L), */
			arguments(new PeopleFilter(1, 20).withHasEmailVerifiedAt(true), 0L),
			/* arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withEmailVerifiedAtFrom(hourAhead).withEmailVerifiedAtTo(hourAgo), 0L), */
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final PeopleFilter filter, final long expectedTotal)
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
			arguments(new PeopleFilter("id", null), "id", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("id", "ASC"), "id", "ASC"),
			arguments(new PeopleFilter("id", "asc"), "id", "ASC"),
			arguments(new PeopleFilter("id", "invalid"), "id", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("id", "DESC"), "id", "DESC"),
			arguments(new PeopleFilter("id", "desc"), "id", "DESC"),

			arguments(new PeopleFilter("name", null), "name", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("name", "ASC"), "name", "ASC"),
			arguments(new PeopleFilter("name", "asc"), "name", "ASC"),
			arguments(new PeopleFilter("name", "invalid"), "name", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("name", "DESC"), "name", "DESC"),
			arguments(new PeopleFilter("name", "desc"), "name", "DESC"),

			arguments(new PeopleFilter("phone", null), "phone", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("phone", "ASC"), "phone", "ASC"),
			arguments(new PeopleFilter("phone", "asc"), "phone", "ASC"),
			arguments(new PeopleFilter("phone", "invalid"), "phone", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("phone", "DESC"), "phone", "DESC"),
			arguments(new PeopleFilter("phone", "desc"), "phone", "DESC"),

			arguments(new PeopleFilter("email", null), "email", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("email", "ASC"), "email", "ASC"),
			arguments(new PeopleFilter("email", "asc"), "email", "ASC"),
			arguments(new PeopleFilter("email", "invalid"), "email", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("email", "DESC"), "email", "DESC"),
			arguments(new PeopleFilter("email", "desc"), "email", "DESC"),

			arguments(new PeopleFilter("firstName", null), "firstName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("firstName", "ASC"), "firstName", "ASC"),
			arguments(new PeopleFilter("firstName", "asc"), "firstName", "ASC"),
			arguments(new PeopleFilter("firstName", "invalid"), "firstName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("firstName", "DESC"), "firstName", "DESC"),
			arguments(new PeopleFilter("firstName", "desc"), "firstName", "DESC"),

			arguments(new PeopleFilter("lastName", null), "lastName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("lastName", "ASC"), "lastName", "ASC"),
			arguments(new PeopleFilter("lastName", "asc"), "lastName", "ASC"),
			arguments(new PeopleFilter("lastName", "invalid"), "lastName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("lastName", "DESC"), "lastName", "DESC"),
			arguments(new PeopleFilter("lastName", "desc"), "lastName", "DESC"),

			arguments(new PeopleFilter("dob", null), "dob", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("dob", "ASC"), "dob", "ASC"),
			arguments(new PeopleFilter("dob", "asc"), "dob", "ASC"),
			arguments(new PeopleFilter("dob", "invalid"), "dob", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("dob", "DESC"), "dob", "DESC"),
			arguments(new PeopleFilter("dob", "desc"), "dob", "DESC"),

			arguments(new PeopleFilter("statusId", null), "statusId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("statusId", "ASC"), "statusId", "ASC"),
			arguments(new PeopleFilter("statusId", "asc"), "statusId", "ASC"),
			arguments(new PeopleFilter("statusId", "invalid"), "statusId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("statusId", "DESC"), "statusId", "DESC"),
			arguments(new PeopleFilter("statusId", "desc"), "statusId", "DESC"),

			arguments(new PeopleFilter("statureId", null), "statureId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("statureId", "ASC"), "statureId", "ASC"),
			arguments(new PeopleFilter("statureId", "asc"), "statureId", "ASC"),
			arguments(new PeopleFilter("statureId", "invalid"), "statureId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("statureId", "DESC"), "statureId", "DESC"),
			arguments(new PeopleFilter("statureId", "desc"), "statureId", "DESC"),

			arguments(new PeopleFilter("active", null), "active", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("active", "ASC"), "active", "ASC"),
			arguments(new PeopleFilter("active", "asc"), "active", "ASC"),
			arguments(new PeopleFilter("active", "invalid"), "active", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("active", "DESC"), "active", "DESC"),
			arguments(new PeopleFilter("active", "desc"), "active", "DESC"),

			arguments(new PeopleFilter("authAt", null), "authAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("authAt", "ASC"), "authAt", "ASC"),
			arguments(new PeopleFilter("authAt", "asc"), "authAt", "ASC"),
			arguments(new PeopleFilter("authAt", "invalid"), "authAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("authAt", "DESC"), "authAt", "DESC"),
			arguments(new PeopleFilter("authAt", "desc"), "authAt", "DESC"),

			arguments(new PeopleFilter("phoneVerifiedAt", null), "phoneVerifiedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("phoneVerifiedAt", "ASC"), "phoneVerifiedAt", "ASC"),
			arguments(new PeopleFilter("phoneVerifiedAt", "asc"), "phoneVerifiedAt", "ASC"),
			arguments(new PeopleFilter("phoneVerifiedAt", "invalid"), "phoneVerifiedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("phoneVerifiedAt", "DESC"), "phoneVerifiedAt", "DESC"),
			arguments(new PeopleFilter("phoneVerifiedAt", "desc"), "phoneVerifiedAt", "DESC"),

			arguments(new PeopleFilter("emailVerifiedAt", null), "emailVerifiedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("emailVerifiedAt", "ASC"), "emailVerifiedAt", "ASC"),
			arguments(new PeopleFilter("emailVerifiedAt", "asc"), "emailVerifiedAt", "ASC"),
			arguments(new PeopleFilter("emailVerifiedAt", "invalid"), "emailVerifiedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("emailVerifiedAt", "DESC"), "emailVerifiedAt", "DESC"),
			arguments(new PeopleFilter("emailVerifiedAt", "desc"), "emailVerifiedAt", "DESC"),

			arguments(new PeopleFilter("createdAt", null), "createdAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("createdAt", "ASC"), "createdAt", "ASC"),
			arguments(new PeopleFilter("createdAt", "asc"), "createdAt", "ASC"),
			arguments(new PeopleFilter("createdAt", "invalid"), "createdAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("createdAt", "DESC"), "createdAt", "DESC"),
			arguments(new PeopleFilter("createdAt", "desc"), "createdAt", "DESC"),

			arguments(new PeopleFilter("updatedAt", null), "updatedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("updatedAt", "ASC"), "updatedAt", "ASC"),
			arguments(new PeopleFilter("updatedAt", "asc"), "updatedAt", "ASC"),
			arguments(new PeopleFilter("updatedAt", "invalid"), "updatedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("updatedAt", "DESC"), "updatedAt", "DESC"),
			arguments(new PeopleFilter("updatedAt", "desc"), "updatedAt", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	private void search_sort(final PeopleFilter filter, final String expectedSortOn, final String expectedSortDir)
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
		count(new PeopleFilter().withId(VALUE.id), 0L);

		var v = createValid();
		count(new PeopleFilter().withName(v.name), 0L);
		count(new PeopleFilter().withPhone(v.phone), 0L);
		count(new PeopleFilter().withEmail(v.email), 0L);
		count(new PeopleFilter().withFirstName(v.firstName), 0L);
		count(new PeopleFilter().withLastName(v.lastName), 0L);
		count(new PeopleFilter().withDob(v.dob), 0L);
		count(new PeopleFilter().withStatusId(v.statusId), 0L);
		count(new PeopleFilter().withStatureId(v.statureId), 0L);
		count(new PeopleFilter().withActive(v.active), 0L);
	}

	@Test
	public void z_00_add_min()
	{
		Assertions.assertNotNull(VALUE = dao.add(new PeopleValue("minimal", "888-minimal", false)));
	}

	@Test
	public void z_00_add_min_check()
	{
		var value = dao.getById(VALUE.id);
		Assertions.assertEquals(VALUE.id, value.id, "Check id");
		Assertions.assertEquals("minimal", value.name, "Check name");
		Assertions.assertEquals("888-minimal", value.phone, "Check phone");
		Assertions.assertNull(value.email, "Check email");
		Assertions.assertNull(value.firstName, "Check firstName");
		Assertions.assertNull(value.lastName, "Check lastName");
		Assertions.assertNull(value.dob, "Check dob");
		Assertions.assertNull(value.statusId, "Check statusId");
		Assertions.assertNull(value.statureId, "Check statureId");
		Assertions.assertFalse(value.active, "Check active");
		Assertions.assertNull(value.authAt, "Check authAt");
		Assertions.assertNull(value.phoneVerifiedAt, "Check phoneVerifiedAt");
		Assertions.assertNull(value.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(VALUE.createdAt, value.createdAt, "Check createdAt");
		Assertions.assertEquals(value.createdAt, value.updatedAt, "Check updatedAt");
	}

	@Test
	public void z_01_authenticatedByPhone()
	{
		var value = dao.authenticatedByPhone("888-minimal");
		Assertions.assertEquals("minimal", value.name, "Check name");
		Assertions.assertEquals("888-minimal", value.phone, "Check phone");
		Assertions.assertNull(value.email, "Check email");
		Assertions.assertNull(value.firstName, "Check firstName");
		Assertions.assertNull(value.lastName, "Check lastName");
		Assertions.assertNull(value.dob, "Check dob");
		Assertions.assertNull(value.statusId, "Check statusId");
		Assertions.assertNull(value.statureId, "Check statureId");
		Assertions.assertTrue(value.active, "Check active");
		assertThat(value.authAt).as("Check authAt").isCloseTo(new Date(), 200L);
		assertThat(value.phoneVerifiedAt).as("Check phoneVerifiedAt").isAfter(value.createdAt).isEqualTo(value.authAt);
		Assertions.assertNull(value.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(VALUE.createdAt, value.createdAt, "Check createdAt");
		assertThat(value.updatedAt).as("Check updatedAt").isAfter(value.createdAt).isEqualTo(value.authAt);
	}

	@Test
	public void z_01_authenticatedByPhone_check()
	{
		var value = dao.getById(VALUE.id);
		Assertions.assertEquals("minimal", value.name, "Check name");
		Assertions.assertEquals("888-minimal", value.phone, "Check phone");
		Assertions.assertNull(value.email, "Check email");
		Assertions.assertNull(value.firstName, "Check firstName");
		Assertions.assertNull(value.lastName, "Check lastName");
		Assertions.assertNull(value.dob, "Check dob");
		Assertions.assertNull(value.statusId, "Check statusId");
		Assertions.assertNull(value.statureId, "Check statureId");
		Assertions.assertTrue(value.active, "Check active");
		assertThat(value.authAt).as("Check authAt").isCloseTo(new Date(), 500L);
		assertThat(value.phoneVerifiedAt).as("Check phoneVerifiedAt").isAfter(value.createdAt).isEqualTo(value.authAt);
		Assertions.assertNull(value.emailVerifiedAt, "Check emailVerifiedAt");
		Assertions.assertEquals(VALUE.createdAt, value.createdAt, "Check createdAt");
		assertThat(value.updatedAt).as("Check updatedAt").isAfter(value.createdAt).isEqualTo(value.authAt);
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final PeopleFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied entity record. */
	private void check(final PeopleValue expected, final People record)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, record.getId(), assertId + "Check id");
		Assertions.assertEquals(expected.name, record.getName(), assertId + "Check name");
		Assertions.assertEquals(expected.phone, record.getPhone(), assertId + "Check phone");
		Assertions.assertEquals(expected.email, record.getEmail(), assertId + "Check email");
		Assertions.assertEquals(expected.firstName, record.getFirstName(), assertId + "Check firstName");
		Assertions.assertEquals(expected.lastName, record.getLastName(), assertId + "Check lastName");
		Assertions.assertEquals(expected.dob, record.getDob(), assertId + "Check dob");
		Assertions.assertEquals(expected.statusId, record.getStatusId(), assertId + "Check statusId");
		Assertions.assertEquals(expected.statureId, record.getStatureId(), assertId + "Check statureId");
		Assertions.assertEquals(expected.active, record.isActive(), assertId + "Check active");
		Assertions.assertEquals(expected.authAt, record.getAuthAt(), assertId + "Check authAt");
		Assertions.assertEquals(expected.phoneVerifiedAt, record.getPhoneVerifiedAt(), assertId + "Check phoneVerifiedAt");
		Assertions.assertEquals(expected.emailVerifiedAt, record.getEmailVerifiedAt(), assertId + "Check emailVerifiedAt");
		Assertions.assertEquals(expected.createdAt, record.getCreatedAt(), assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, record.getUpdatedAt(), assertId + "Check updatedAt");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final PeopleValue expected, final PeopleValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.name, value.name, assertId + "Check name");
		Assertions.assertEquals(expected.phone, value.phone, assertId + "Check phone");
		Assertions.assertEquals(expected.email, value.email, assertId + "Check email");
		Assertions.assertEquals(expected.firstName, value.firstName, assertId + "Check firstName");
		Assertions.assertEquals(expected.lastName, value.lastName, assertId + "Check lastName");
		Assertions.assertEquals(expected.dob, value.dob, assertId + "Check dob");
		Assertions.assertEquals(expected.statusId, value.statusId, assertId + "Check statusId");
		Assertions.assertEquals(expected.status, value.status, assertId + "Check status");
		Assertions.assertEquals(expected.statureId, value.statureId, assertId + "Check statureId");
		Assertions.assertEquals(expected.stature, value.stature, assertId + "Check stature");
		Assertions.assertEquals(expected.active, value.active, assertId + "Check active");
		Assertions.assertEquals(expected.authAt, value.authAt, assertId + "Check authAt");
		Assertions.assertEquals(expected.phoneVerifiedAt, value.phoneVerifiedAt, assertId + "Check phoneVerifiedAt");
		Assertions.assertEquals(expected.emailVerifiedAt, value.emailVerifiedAt, assertId + "Check emailVerifiedAt");
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, value.updatedAt, assertId + "Check updatedAt");
	}
}
