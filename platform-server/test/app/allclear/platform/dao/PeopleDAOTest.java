package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;

import static app.allclear.platform.type.Condition.*;
import static app.allclear.platform.type.Exposure.*;
import static app.allclear.platform.type.Symptom.*;
import static app.allclear.platform.type.Timezone.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.value.CreatedValue;
import app.allclear.platform.App;
import app.allclear.platform.entity.Named;
import app.allclear.platform.entity.People;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.model.PeopleFindRequest;
import app.allclear.platform.type.*;
import app.allclear.platform.value.PeopleValue;
import app.allclear.platform.value.PeopleFieldValue;

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
	private static PeopleFieldValue FIELD = null;

	private static BigDecimal bg(final String value) { return new BigDecimal(value); }

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
			DOB, PeopleStatus.INFECTED.id, Stature.INFLUENCER.id, Sex.MALE.id, HealthWorkerStatus.LIVE_WITH.id,
			bg("86.5"), bg("-37.1"), "Phoenix, Arizona", false, true));
		Assertions.assertNotNull(value, "Exists");
		check(VALUE, value);
	}

	/** Creates a valid People value for the validation tests.
	 *	@return never NULL.
	*/
	public static PeopleValue createValid()
	{
		return new PeopleValue("bryce", "888-555-1001", "dallas@gmail.com", "Dallas", "Drawoh",
			DOB_1, PeopleStatus.RECOVERED.id, Stature.CELEBRITY.id, Sex.FEMALE.id, HealthWorkerStatus.HEALTH_WORKER.id,
			bg("-86.5"), bg("37.1"), "Atlanta, GA", true, false);
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

	@Test
	public void add_invalidSexId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withSexId("$")));
	}

	@Test
	public void add_longSexId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withSexId(StringUtils.repeat("A", PeopleValue.MAX_LEN_SEX_ID + 1))));
	}

	@Test
	public void add_invalidHealthWorkerStatusId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withHealthWorkerStatusId("$")));
	}

	@Test
	public void add_longHealthWorkerStatusId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withHealthWorkerStatusId(StringUtils.repeat("A", PeopleValue.MAX_LEN_HEALTH_WORKER_STATUS_ID + 1))));
	}

	@Test
	public void add_missingHealthWorkerStatusId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withHealthWorkerStatusId(null)));
	}

	@Test
	public void add_tooHighLatitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLatitude(bg("91"))));
	}

	@Test
	public void add_tooLowLatitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLatitude(bg("-91"))));
	}

	@Test
	public void add_tooHighLongitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLatitude(bg("181"))));
	}

	@Test
	public void add_tooLowLongitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLatitude(bg("-181"))));
	}

	@Test
	public void add_longLocationName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLocationName(StringUtils.repeat("A", PeopleValue.MAX_LEN_LOCATION_NAME + 1))));
	}

	public static Stream<Arguments> add_invalidConditions()
	{
		return Stream.of(
			arguments(List.of(new CreatedValue("1")), "'1' is not a valid Condition."),
			arguments(Arrays.asList(new CreatedValue("1"), null), "'1' is not a valid Condition."),
			arguments(List.of(new CreatedValue("1"), new CreatedValue(null)), "'1' is not a valid Condition.\nCondition is not set."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null)), "'1' is not a valid Condition.\nCondition is not set."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null), new CreatedValue("2")), "'1' is not a valid Condition.\nCondition is not set.\n'2' is not a valid Condition."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null), new CreatedValue("2"), DIABETIC.created()), "'1' is not a valid Condition.\nCondition is not set.\n'2' is not a valid Condition."));
	}

	@ParameterizedTest
	@MethodSource
	public void add_invalidConditions(final List<CreatedValue> values, final String message)
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withConditions(values))))
			.hasMessage(message);
	}

	public static Stream<Arguments> add_invalidExposures()
	{
		return Stream.of(
			arguments(List.of(new CreatedValue("1")), "'1' is not a valid Exposure."),
			arguments(Arrays.asList(new CreatedValue("1"), null), "'1' is not a valid Exposure."),
			arguments(List.of(new CreatedValue("1"), new CreatedValue(null)), "'1' is not a valid Exposure.\nExposure is not set."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null)), "'1' is not a valid Exposure.\nExposure is not set."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null), new CreatedValue("2")), "'1' is not a valid Exposure.\nExposure is not set.\n'2' is not a valid Exposure."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null), new CreatedValue("2"), LIVE_WITH.created()), "'1' is not a valid Exposure.\nExposure is not set.\n'2' is not a valid Exposure."));
	}

	@ParameterizedTest
	@MethodSource
	public void add_invalidExposures(final List<CreatedValue> values, final String message)
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withExposures(values))))
			.hasMessage(message);
	}

	public static Stream<Arguments> add_invalidSymptoms()
	{
		return Stream.of(
			arguments(List.of(new CreatedValue("1")), "'1' is not a valid Symptom."),
			arguments(Arrays.asList(new CreatedValue("1"), null), "'1' is not a valid Symptom."),
			arguments(List.of(new CreatedValue("1"), new CreatedValue(null)), "'1' is not a valid Symptom.\nSymptom is not set."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null)), "'1' is not a valid Symptom.\nSymptom is not set."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null), new CreatedValue("2")), "'1' is not a valid Symptom.\nSymptom is not set.\n'2' is not a valid Symptom."),
			arguments(Arrays.asList(new CreatedValue("1"), null, new CreatedValue(null), new CreatedValue("2"), SHORTNESS_OF_BREATH.created()), "'1' is not a valid Symptom.\nSymptom is not set.\n'2' is not a valid Symptom."));
	}

	@ParameterizedTest
	@MethodSource
	public void add_invalidSymptoms(final List<CreatedValue> values, final String message)
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withSymptoms(values))))
			.hasMessage(message);
	}

	@Test
	public void alertByPhone_00()
	{
		var record = dao.findWithException(VALUE.id);
		assertThat(record.getUpdatedAt()).as("Check updatedAt: before").isEqualTo(record.getCreatedAt());
		Assertions.assertFalse(record.isAlertable(), "Check alertable: first");

		Assertions.assertFalse(dao.unalertByPhone(VALUE.phone), "unalertByPhone");
		Assertions.assertFalse(dao.findWithException(VALUE.id).isAlertable(), "Check alertable: second");

		Assertions.assertTrue(dao.alertByPhone(VALUE.phone), "alertByPhone");
		Assertions.assertTrue(dao.findWithException(VALUE.id).isAlertable(), "Check alertable: third");

		Assertions.assertFalse(dao.alertByPhone(VALUE.phone), "alertByPhone");
		Assertions.assertTrue(dao.findWithException(VALUE.id).isAlertable(), "Check alertable: fourth");

		Assertions.assertTrue(dao.unalertByPhone(VALUE.phone), "unalertByPhone");
		Assertions.assertFalse((record = dao.findWithException(VALUE.id)).isAlertable(), "Check alertable: fifth");

		assertThat(record.getUpdatedAt()).as("Check updatedAt: after").isAfter(record.getCreatedAt());

		VALUE.withUpdatedAt(record.getUpdatedAt());
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
		assertThat(assertThrows(ObjectNotFoundException.class, () -> dao.authenticatedByPhone("ronny")))
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
	public void findField()
	{
		var o = dao.findField(VALUE.id);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityHealthWorkerStatusId, "Check visibilityHealthWorkerStatusId");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityConditions, "Check visibilityConditions");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityExposures, "Check visibilityExposures");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilitySymptoms, "Check visibilitySymptoms");
		Assertions.assertEquals(VALUE.createdAt, o.updatedAt, "Check updatedAt");
	}

	@Test
	public void findField_invalid()
	{
		Assertions.assertNull(dao.findField("INVALID"), "Exists");
	}

	@Test
	public void findFieldWithException()
	{
		var o = dao.findFieldWithException(VALUE.id);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityHealthWorkerStatusId, "Check visibilityHealthWorkerStatusId");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityConditions, "Check visibilityConditions");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityExposures, "Check visibilityExposures");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilitySymptoms, "Check visibilitySymptoms");
		Assertions.assertEquals(VALUE.createdAt, o.updatedAt, "Check updatedAt");
	}

	@Test
	public void findFieldWithException_invalid()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.findFieldWithException("INVALID"));
	}

	public static Stream<Arguments> findOne()
	{
		var valid = createValid();

		return Stream.of(
			arguments(VALUE.id, true),
			arguments(VALUE.name, true),
			arguments(VALUE.phone, true),
			arguments(VALUE.email, true),
			arguments(VALUE.firstName, false),
			arguments(VALUE.lastName, false),
			arguments("ron", false),
			arguments("888", false),
			arguments("+1888", false),
			arguments("+1", false),
			arguments("INVALID", false),
			arguments(valid.name, false),
			arguments(valid.phone, false),
			arguments(valid.email, false));
	}

	@ParameterizedTest
	@MethodSource
	public void findOne(final String name, final boolean found)
	{
		var o = dao.findOne(name);
		if (found)
		{
			Assertions.assertNotNull(o, "Exists");
			Assertions.assertEquals(VALUE.id, o.getId(), "Check ID");
		}
		else
			Assertions.assertNull(o, "Exists");
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

	public static Stream<Arguments> getActiveByIdOrName()
	{
		var invalid = createValid();

		return Stream.of(
			arguments("invalid", false),
			arguments(VALUE.id.substring(0, 3), true),
			arguments(VALUE.name.substring(0, 3), true),
			arguments(VALUE.phone.substring(0, 3), true),
			arguments(VALUE.email.substring(0, 3), true),
			arguments(invalid.name, false),
			arguments(invalid.phone, false),
			arguments(invalid.email, false));
	}

	@ParameterizedTest
	@MethodSource
	public void getActiveByIdOrName(final String name, final boolean found)
	{
		var o = assertThat(dao.getActiveByIdOrName(name));
		if (found) o.containsExactly(VALUE);
		else o.isEmpty();
	}

	@Test
	public void getFieldWithException()
	{
		var o = FIELD = dao.getFieldWithException(VALUE.id);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityHealthWorkerStatusId, "Check visibilityHealthWorkerStatusId");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityConditions, "Check visibilityConditions");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilityExposures, "Check visibilityExposures");
		Assertions.assertEquals(Visibility.FRIENDS.id, o.visibilitySymptoms, "Check visibilitySymptoms");
		Assertions.assertEquals(VALUE.createdAt, o.updatedAt, "Check updatedAt");
	}

	@Test
	public void getFieldWithException_invalid()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.getFieldWithException("INVALID"));
	}

	@Test
	public void getIdByPhone()
	{
		Assertions.assertEquals(VALUE.id, dao.getIdByPhone("888-555-1000"));
	}

	@Test
	public void getIdByPhone_invalid()
	{
		Assertions.assertNull(dao.getIdByPhone("888-555-1001"));
	}

	public static Stream<Arguments> modif()
	{
		var v = createValid();

		return Stream.of(
			arguments(new PeopleFilter().withId(VALUE.id), 1L),
			arguments(new PeopleFilter().withName(VALUE.name), 1L),
			arguments(new PeopleFilter().withPhone(VALUE.phone), 1L),
			arguments(new PeopleFilter().withEmail(VALUE.email), 1L),
			arguments(new PeopleFilter().withFirstName(VALUE.firstName), 1L),
			arguments(new PeopleFilter().withLastName(VALUE.lastName), 1L),
			arguments(new PeopleFilter().withDob(VALUE.dob), 1L),
			arguments(new PeopleFilter().withStatusId(VALUE.statusId), 1L),
			arguments(new PeopleFilter().withStatureId(VALUE.statureId), 1L),
			arguments(new PeopleFilter().withSexId(VALUE.sexId), 1L),
			arguments(new PeopleFilter().withHealthWorkerStatusId(VALUE.healthWorkerStatusId), 1L),
			arguments(new PeopleFilter().withLatitude(VALUE.latitude), 1L),
			arguments(new PeopleFilter().withLongitude(VALUE.longitude), 1L),
			arguments(new PeopleFilter().withLocationName(VALUE.locationName), 1L),
			arguments(new PeopleFilter().withAlertable(VALUE.alertable), 1L),
			arguments(new PeopleFilter().withActive(VALUE.active), 1L),
			arguments(new PeopleFilter().withName(v.name), 0L),
			arguments(new PeopleFilter().withPhone(v.phone), 0L),
			arguments(new PeopleFilter().withEmail(v.email), 0L),
			arguments(new PeopleFilter().withFirstName(v.firstName), 0L),
			arguments(new PeopleFilter().withLastName(v.lastName), 0L),
			arguments(new PeopleFilter().withDob(v.dob), 0L),
			arguments(new PeopleFilter().withStatusId(v.statusId), 0L),
			arguments(new PeopleFilter().withStatureId(v.statureId), 0L),
			arguments(new PeopleFilter().withSexId(v.sexId), 0L),
			arguments(new PeopleFilter().withHealthWorkerStatusId(v.healthWorkerStatusId), 0L),
			arguments(new PeopleFilter().withLatitude(v.latitude), 0L),
			arguments(new PeopleFilter().withLongitude(v.longitude), 0L),
			arguments(new PeopleFilter().withLocationName(v.locationName), 0L),
			arguments(new PeopleFilter().withAlertable(v.alertable), 0L),
			arguments(new PeopleFilter().withActive(v.active), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void modif(final PeopleFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void modify()
	{
		var v = createValid();
		var value = dao.update(v.withId(VALUE.id));
		Assertions.assertNotNull(value, "Exists");
		check(v, value);
	}

	public static Stream<Arguments> modify_count()
	{
		var v = createValid();

		return Stream.of(
			arguments(new PeopleFilter().withId(VALUE.id), 1L),
			arguments(new PeopleFilter().withName(VALUE.name), 0L),
			arguments(new PeopleFilter().withPhone(VALUE.phone), 0L),
			arguments(new PeopleFilter().withEmail(VALUE.email), 0L),
			arguments(new PeopleFilter().withFirstName(VALUE.firstName), 0L),
			arguments(new PeopleFilter().withLastName(VALUE.lastName), 0L),
			arguments(new PeopleFilter().withDob(VALUE.dob), 0L),
			arguments(new PeopleFilter().withStatusId(VALUE.statusId), 0L),
			arguments(new PeopleFilter().withStatureId(VALUE.statureId), 0L),
			arguments(new PeopleFilter().withSexId(VALUE.sexId), 0L),
			arguments(new PeopleFilter().withHealthWorkerStatusId(VALUE.healthWorkerStatusId), 0L),
			arguments(new PeopleFilter().withLatitude(VALUE.latitude), 0L),
			arguments(new PeopleFilter().withLongitude(VALUE.longitude), 0L),
			arguments(new PeopleFilter().withLocationName(VALUE.locationName), 0L),
			arguments(new PeopleFilter().withAlertable(VALUE.alertable), 0L),
			arguments(new PeopleFilter().withActive(VALUE.active), 0L),
			arguments(new PeopleFilter().withName(v.name), 1L),
			arguments(new PeopleFilter().withPhone(v.phone), 1L),
			arguments(new PeopleFilter().withEmail(v.email), 1L),
			arguments(new PeopleFilter().withFirstName(v.firstName), 1L),
			arguments(new PeopleFilter().withLastName(v.lastName), 1L),
			arguments(new PeopleFilter().withDob(v.dob), 1L),
			arguments(new PeopleFilter().withStatusId(v.statusId), 1L),
			arguments(new PeopleFilter().withStatureId(v.statureId), 1L),
			arguments(new PeopleFilter().withSexId(v.sexId), 1L),
			arguments(new PeopleFilter().withHealthWorkerStatusId(v.healthWorkerStatusId), 1L),
			arguments(new PeopleFilter().withLatitude(v.latitude), 1L),
			arguments(new PeopleFilter().withLongitude(v.longitude), 1L),
			arguments(new PeopleFilter().withLocationName(v.locationName), 1L),
			arguments(new PeopleFilter().withAlertable(v.alertable), 1L),
			arguments(new PeopleFilter().withActive(v.active), 1L));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_count(final PeopleFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void modify_count_00()
	{
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
		Assertions.assertEquals(v.sexId, record.getSexId(), "Check sexId");
		Assertions.assertEquals(v.healthWorkerStatusId, record.getHealthWorkerStatusId(), "Check healthWorkerStatusId");
		assertThat(record.getLatitude()).as("Check latitude").isEqualByComparingTo(v.latitude);
		assertThat(record.getLongitude()).as("Check longitude").isEqualByComparingTo(v.longitude);
		Assertions.assertEquals("Atlanta, GA", record.getLocationName(), "Check locationName");
		Assertions.assertEquals(v.alertable, record.isAlertable(), "Check alertable");
		Assertions.assertEquals(v.active, record.isActive(), "Check active");
		assertThat(record.getUpdatedAt()).as("Check updatedAt").isAfter(record.getCreatedAt());
		check(VALUE, record);
	}

	@Test
	public void modify_field()
	{
		count(new PeopleFilter().withVisibilityHealthWorkerStatusId(Visibility.FRIENDS.id), 1L);
		count(new PeopleFilter().withVisibilityConditions(Visibility.FRIENDS.id), 1L);
		count(new PeopleFilter().withVisibilityExposures(Visibility.FRIENDS.id), 1L);
		count(new PeopleFilter().withVisibilitySymptoms(Visibility.FRIENDS.id), 1L);
		count(new PeopleFilter().withVisibilityHealthWorkerStatusId(Visibility.ME.id), 0L);
		count(new PeopleFilter().withVisibilityConditions(Visibility.ALL.id), 0L);
		count(new PeopleFilter().withVisibilityExposures(Visibility.ME.id), 0L);
		count(new PeopleFilter().withVisibilitySymptoms(Visibility.ALL.id), 0L);

		var v = dao.update(new PeopleFieldValue(VALUE.id, Visibility.ME.id, Visibility.ALL.id, Visibility.ME.id, Visibility.ALL.id));
		assertThat(v.updatedAt).as("Check updatedAt").isAfter(FIELD.updatedAt).isCloseTo(new Date(), 500L);
	}

	@Test
	public void modify_field_count()
	{
		count(new PeopleFilter().withVisibilityHealthWorkerStatusId(Visibility.FRIENDS.id), 0L);
		count(new PeopleFilter().withVisibilityConditions(Visibility.FRIENDS.id), 0L);
		count(new PeopleFilter().withVisibilityExposures(Visibility.FRIENDS.id), 0L);
		count(new PeopleFilter().withVisibilitySymptoms(Visibility.FRIENDS.id), 0L);
		count(new PeopleFilter().withVisibilityHealthWorkerStatusId(Visibility.ME.id), 1L);
		count(new PeopleFilter().withVisibilityConditions(Visibility.ALL.id), 1L);
		count(new PeopleFilter().withVisibilityExposures(Visibility.ME.id), 1L);
		count(new PeopleFilter().withVisibilitySymptoms(Visibility.ALL.id), 1L);
	}

	@Test
	public void modify_field_get()
	{
		var v = dao.getFieldWithException(VALUE.id);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertEquals(VALUE.id, v.id, "Check id");
		Assertions.assertEquals(VALUE.name, v.name, "Check name");
		Assertions.assertEquals(Visibility.ME.id, v.visibilityHealthWorkerStatusId, "Check visibilityHealthWorkerStatusId");
		Assertions.assertEquals(Visibility.ME, v.visibilityHealthWorkerStatus, "Check visibilityHealthWorkerStatus");
		Assertions.assertEquals(Visibility.ALL.id, v.visibilityConditions, "Check visibilityConditions");
		Assertions.assertEquals(Visibility.ALL, v.visibilityCondition, "Check visibilityCondition");
		Assertions.assertEquals(Visibility.ME.id, v.visibilityExposures, "Check visibilityExposures");
		Assertions.assertEquals(Visibility.ME, v.visibilityExposure, "Check visibilityExposure");
		Assertions.assertEquals(Visibility.ALL.id, v.visibilitySymptoms, "Check visibilitySymptoms");
		Assertions.assertEquals(Visibility.ALL, v.visibilitySymptom, "Check visibilitySymptom");
		assertThat(v.updatedAt).as("Check updatedAt").isAfter(VALUE.updatedAt);

		FIELD = v;
	}

	public static Stream<Arguments> modify_field_errors()
	{
		return Stream.of(
				arguments("", Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, "ID is not set."),
				arguments(null, Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, "ID is not set."),
				arguments(VALUE.id, "", Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, "Visibility Health Worker Status is not set."),
				arguments(VALUE.id, null, Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, "Visibility Health Worker Status is not set."),
				arguments(VALUE.id, "#", Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, "'#' is not a valid Visibility Health Worker Status."),
				arguments(VALUE.id, Visibility.ME.id, "", Visibility.ME.id, Visibility.ME.id, "Visibility Conditions is not set."),
				arguments(VALUE.id, Visibility.ME.id, null, Visibility.ME.id, Visibility.ME.id, "Visibility Conditions is not set."),
				arguments(VALUE.id, Visibility.ME.id, "#", Visibility.ME.id, Visibility.ME.id, "'#' is not a valid Visibility Conditions."),
				arguments(VALUE.id, Visibility.ME.id, Visibility.ME.id, "", Visibility.ME.id, "Visibility Exposures is not set."),
				arguments(VALUE.id, Visibility.ME.id, Visibility.ME.id, null, Visibility.ME.id, "Visibility Exposures is not set."),
				arguments(VALUE.id, Visibility.ME.id, Visibility.ME.id, "#", Visibility.ME.id, "'#' is not a valid Visibility Exposures."),
				arguments(VALUE.id, Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, "", "Visibility Symptoms is not set."),
				arguments(VALUE.id, Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, null, "Visibility Symptoms is not set."),
				arguments(VALUE.id, Visibility.ME.id, Visibility.ME.id, Visibility.ME.id, "#", "'#' is not a valid Visibility Symptoms."));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_field_errors(final String id,
		final String visibilityHealthWorkerStatusId,
		final String visibilityConditions,
		final String visibilityExposures,
		final String visibilitySymptoms,
		final String message)
	{
		assertThat(assertThrows(ValidationException.class,
			() -> dao.update(new PeopleFieldValue(id, visibilityHealthWorkerStatusId, visibilityConditions, visibilityExposures, visibilitySymptoms))))
				.hasMessage(message);
	}

	@Test
	public void modify_field_not_found()
	{
		assertThat(assertThrows(ObjectNotFoundException.class,
			() -> dao.update(new PeopleFieldValue("INVALID", Visibility.ME.id, Visibility.ALL.id, Visibility.ME.id, Visibility.ALL.id))))
				.hasMessage("The People Field 'INVALID' is invalid.");
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();
		var five = new BigDecimal("5");
		var upLat = VALUE.latitude.add(five);
		var upLng = VALUE.longitude.add(five);
		var downLat = VALUE.latitude.subtract(five);
		var downLng = VALUE.longitude.subtract(five);

		return Stream.of(
			arguments(new PeopleFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new PeopleFilter(1, 20).withName(VALUE.name), 1L),
			arguments(new PeopleFilter(1, 20).withNameX("ryc"), 1L),
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
			arguments(new PeopleFilter(1, 20).withSexId(VALUE.sexId), 1L),
			arguments(new PeopleFilter(1, 20).withHasSexId(true), 1L),
			arguments(new PeopleFilter(1, 20).withHealthWorkerStatusId(VALUE.healthWorkerStatusId), 1L),
			arguments(new PeopleFilter(1, 20).withLatitude(VALUE.latitude), 1L),
			arguments(new PeopleFilter(1, 20).withHasLatitude(true), 1L),
			arguments(new PeopleFilter(1, 20).withLatitudeFrom(downLat), 1L),
			arguments(new PeopleFilter(1, 20).withLatitudeTo(upLat), 1L),
			arguments(new PeopleFilter(1, 20).withLongitude(VALUE.longitude), 1L),
			arguments(new PeopleFilter(1, 20).withHasLongitude(true), 1L),
			arguments(new PeopleFilter(1, 20).withLongitudeFrom(downLng), 1L),
			arguments(new PeopleFilter(1, 20).withLongitudeTo(upLng), 1L),
			arguments(new PeopleFilter(1, 20).withLocationName(VALUE.locationName), 1L),
			arguments(new PeopleFilter(1, 20).withHasLocationName(true), 1L),
			arguments(new PeopleFilter(1, 20).withAlertable(VALUE.alertable), 1L),
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
			arguments(new PeopleFilter(1, 20).withHasAlertedOf(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasAlertedAt(false), 1L),
			/* arguments(new PeopleFilter(1, 20).withAlertedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withAlertedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withAlertedAtFrom(hourAgo).withAlertedAtTo(hourAhead), 1L), */
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),
			arguments(new PeopleFilter(1, 20).withHasTakenTest(false), 1L),
			arguments(new PeopleFilter(1, 20).withHasPositiveTest(false), 1L),
			arguments(new PeopleFilter(1, 20).withVisibilityHealthWorkerStatusId(Visibility.ME.id), 1L),
			arguments(new PeopleFilter(1, 20).withVisibilityConditions(Visibility.ALL.id), 1L),
			arguments(new PeopleFilter(1, 20).withVisibilityExposures(Visibility.ME.id), 1L),
			arguments(new PeopleFilter(1, 20).withVisibilitySymptoms(Visibility.ALL.id), 1L),

			// Negative tests
			arguments(new PeopleFilter(1, 20).withId("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withName("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withNameX("onn"), 0L),
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
			arguments(new PeopleFilter(1, 20).withSexId("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withHasSexId(false), 0L),
			arguments(new PeopleFilter(1, 20).withHealthWorkerStatusId("invalid"), 0L),
			arguments(new PeopleFilter(1, 20).withTimezoneId(EST.id), 0L),
			arguments(new PeopleFilter(1, 20).withTimezoneId(CST.id), 0L),
			arguments(new PeopleFilter(1, 20).withTimezoneId(MST.id), 0L),
			arguments(new PeopleFilter(1, 20).withTimezoneId(PST.id), 0L),
			arguments(new PeopleFilter(1, 20).withLatitude(upLat), 0L),
			arguments(new PeopleFilter(1, 20).withHasLatitude(false), 0L),
			arguments(new PeopleFilter(1, 20).withLatitudeFrom(upLat), 0L),
			arguments(new PeopleFilter(1, 20).withLatitudeTo(downLat), 0L),
			arguments(new PeopleFilter(1, 20).withLongitude(downLng), 0L),
			arguments(new PeopleFilter(1, 20).withHasLongitude(false), 0L),
			arguments(new PeopleFilter(1, 20).withLongitudeFrom(upLng), 0L),
			arguments(new PeopleFilter(1, 20).withLongitudeTo(downLng), 0L),
			arguments(new PeopleFilter(1, 20).withLocationName("Phoenix, Arizona"), 0L),
			arguments(new PeopleFilter(1, 20).withHasLocationName(false), 0L),
			arguments(new PeopleFilter(1, 20).withAlertable(!VALUE.alertable), 0L),
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
			arguments(new PeopleFilter(1, 20).withHasAlertedOf(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasAlertedAt(true), 0L),
			/* arguments(new PeopleFilter(1, 20).withAlertedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withAlertedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withAlertedAtFrom(hourAhead).withAlertedAtTo(hourAgo), 0L), */
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L),
			arguments(new PeopleFilter(1, 20).withHasTakenTest(true), 0L),
			arguments(new PeopleFilter(1, 20).withHasPositiveTest(true), 0L),
			arguments(new PeopleFilter(1, 20).withVisibilityHealthWorkerStatusId(Visibility.ALL.id), 0L),
			arguments(new PeopleFilter(1, 20).withVisibilityHealthWorkerStatusId(Visibility.FRIENDS.id), 0L),
			arguments(new PeopleFilter(1, 20).withVisibilityConditions(Visibility.FRIENDS.id), 0L),
			arguments(new PeopleFilter(1, 20).withVisibilityConditions(Visibility.ME.id), 0L),
			arguments(new PeopleFilter(1, 20).withVisibilityExposures(Visibility.ALL.id), 0L),
			arguments(new PeopleFilter(1, 20).withVisibilityExposures(Visibility.FRIENDS.id), 0L),
			arguments(new PeopleFilter(1, 20).withVisibilitySymptoms(Visibility.ME.id), 0L),
			arguments(new PeopleFilter(1, 20).withVisibilitySymptoms(Visibility.FRIENDS.id), 0L));
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
			arguments(new PeopleFilter(null, null), "id", "ASC"),
			arguments(new PeopleFilter(null, "invalid"), "id", "ASC"),
			arguments(new PeopleFilter(null, "asc"), "id", "ASC"),
			arguments(new PeopleFilter("invalid", "invalid"), "id", "ASC"),
			arguments(new PeopleFilter("invalid", null), "id", "ASC"),
			arguments(new PeopleFilter("invalid", "desc"), "id", "ASC"),

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

			arguments(new PeopleFilter("sexId", null), "sexId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("sexId", "ASC"), "sexId", "ASC"),
			arguments(new PeopleFilter("sexId", "asc"), "sexId", "ASC"),
			arguments(new PeopleFilter("sexId", "invalid"), "sexId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("sexId", "DESC"), "sexId", "DESC"),
			arguments(new PeopleFilter("sexId", "desc"), "sexId", "DESC"),

			arguments(new PeopleFilter("healthWorkerStatusId", null), "healthWorkerStatusId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("healthWorkerStatusId", "ASC"), "healthWorkerStatusId", "ASC"),
			arguments(new PeopleFilter("healthWorkerStatusId", "asc"), "healthWorkerStatusId", "ASC"),
			arguments(new PeopleFilter("healthWorkerStatusId", "invalid"), "healthWorkerStatusId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("healthWorkerStatusId", "DESC"), "healthWorkerStatusId", "DESC"),
			arguments(new PeopleFilter("healthWorkerStatusId", "desc"), "healthWorkerStatusId", "DESC"),

			arguments(new PeopleFilter("latitude", null), "latitude", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("latitude", "ASC"), "latitude", "ASC"),
			arguments(new PeopleFilter("latitude", "asc"), "latitude", "ASC"),
			arguments(new PeopleFilter("latitude", "invalid"), "latitude", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("latitude", "DESC"), "latitude", "DESC"),
			arguments(new PeopleFilter("latitude", "desc"), "latitude", "DESC"),

			arguments(new PeopleFilter("longitude", null), "longitude", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("longitude", "ASC"), "longitude", "ASC"),
			arguments(new PeopleFilter("longitude", "asc"), "longitude", "ASC"),
			arguments(new PeopleFilter("longitude", "invalid"), "longitude", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("longitude", "DESC"), "longitude", "DESC"),
			arguments(new PeopleFilter("longitude", "desc"), "longitude", "DESC"),

			arguments(new PeopleFilter("locationName", null), "locationName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("locationName", "ASC"), "locationName", "ASC"),
			arguments(new PeopleFilter("locationName", "asc"), "locationName", "ASC"),
			arguments(new PeopleFilter("locationName", "invalid"), "locationName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("locationName", "DESC"), "locationName", "DESC"),
			arguments(new PeopleFilter("locationName", "desc"), "locationName", "DESC"),

			arguments(new PeopleFilter("alertable", null), "alertable", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("alertable", "ASC"), "alertable", "ASC"),
			arguments(new PeopleFilter("alertable", "asc"), "alertable", "ASC"),
			arguments(new PeopleFilter("alertable", "invalid"), "alertable", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("alertable", "DESC"), "alertable", "DESC"),
			arguments(new PeopleFilter("alertable", "desc"), "alertable", "DESC"),

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

			arguments(new PeopleFilter("alertedOf", null), "alertedOf", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("alertedOf", "ASC"), "alertedOf", "ASC"),
			arguments(new PeopleFilter("alertedOf", "asc"), "alertedOf", "ASC"),
			arguments(new PeopleFilter("alertedOf", "invalid"), "alertedOf", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("alertedOf", "DESC"), "alertedOf", "DESC"),
			arguments(new PeopleFilter("alertedOf", "desc"), "alertedOf", "DESC"),

			arguments(new PeopleFilter("alertedAt", null), "alertedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new PeopleFilter("alertedAt", "ASC"), "alertedAt", "ASC"),
			arguments(new PeopleFilter("alertedAt", "asc"), "alertedAt", "ASC"),
			arguments(new PeopleFilter("alertedAt", "invalid"), "alertedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new PeopleFilter("alertedAt", "DESC"), "alertedAt", "DESC"),
			arguments(new PeopleFilter("alertedAt", "desc"), "alertedAt", "DESC"),

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
	public void search_sort(final PeopleFilter filter, final String expectedSortOn, final String expectedSortDir)
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
		assertThrows(ObjectNotFoundException.class, () -> dao.findFieldWithException(VALUE.id));
	}

	public static Stream<Arguments> testRemove_search()
	{
		var v = createValid();

		return Stream.of(
			arguments(new PeopleFilter().withId(VALUE.id), 0L),
			arguments(new PeopleFilter().withName(v.name), 0L),
			arguments(new PeopleFilter().withPhone(v.phone), 0L),
			arguments(new PeopleFilter().withEmail(v.email), 0L),
			arguments(new PeopleFilter().withFirstName(v.firstName), 0L),
			arguments(new PeopleFilter().withLastName(v.lastName), 0L),
			arguments(new PeopleFilter().withDob(v.dob), 0L),
			arguments(new PeopleFilter().withStatusId(v.statusId), 0L),
			arguments(new PeopleFilter().withStatureId(v.statureId), 0L),
			arguments(new PeopleFilter().withActive(v.active), 0L),
			arguments(new PeopleFilter().withVisibilityHealthWorkerStatusId(Visibility.ME.id), 0L),
			arguments(new PeopleFilter().withVisibilityConditions(Visibility.ALL.id), 0L),
			arguments(new PeopleFilter().withVisibilityExposures(Visibility.ME.id), 0L),
			arguments(new PeopleFilter().withVisibilitySymptoms(Visibility.ALL.id), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void testRemove_search(final PeopleFilter filter, final long expected)
	{
		count(filter, expected);
	}

	@Test
	public void z_00_add_min()
	{
		Assertions.assertNotNull(VALUE = dao.add(new PeopleValue("minimal", "888-minimal", false).withLatitude(bg("41.8781136")).withLongitude(bg("-87.6297982"))));
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

	public static Stream<Arguments> z_00_add_min_search()
	{
		return Stream.of(
			arguments(new PeopleFilter(1, 20).withTimezoneId(EST.id), 0L),
			arguments(new PeopleFilter(1, 20).withTimezoneId(CST.id), 1L),
			arguments(new PeopleFilter(1, 20).withTimezoneId(MST.id), 0L),
			arguments(new PeopleFilter(1, 20).withTimezoneId(PST.id), 0L),
			arguments(new PeopleFilter(1, 20).withTimezoneId("INVALID"), 1L));	// Equivalent of no search.
	}

	@ParameterizedTest
	@MethodSource
	public void z_00_add_min_search(final PeopleFilter filter, final long total)
	{
		Assertions.assertEquals(total, dao.search(filter).total);
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

	@Test
	public void z_10_addWithChildren()
	{
		VALUE = dao.add(new PeopleValue("minWithChildren", "888-555-children", true)
			.withConditions(DIABETIC, PREGNANT)
			.withExposures(CLOSE_CONTACT, NO_EXPOSURE)
			.withSymptoms(DIARRHEA, FEVER));

		var now = new Date();
		VALUE.conditions.forEach(c -> assertThat(c.createdAt).as("Check conditions.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.conditions).as("Check conditions").containsOnly(DIABETIC.created(), PREGNANT.created());

		VALUE.exposures.forEach(c -> assertThat(c.createdAt).as("Check exposures.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.exposures).as("Check exposures").containsOnly(CLOSE_CONTACT.created(), NO_EXPOSURE.created());

		VALUE.symptoms.forEach(c -> assertThat(c.createdAt).as("Check symptoms.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.symptoms).as("Check symptoms").containsOnly(DIARRHEA.created(), FEVER.created());
	}

	@Test
	public void z_10_addWithChildren_check()
	{
		var now = new Date();
		var value = dao.getById(VALUE.id);
		value.conditions.forEach(c -> assertThat(c.createdAt).as("Check conditions.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.conditions).as("Check conditions").containsOnly(DIABETIC.created(), PREGNANT.created());

		value.exposures.forEach(c -> assertThat(c.createdAt).as("Check exposures.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.exposures).as("Check exposures").containsOnly(CLOSE_CONTACT.created(), NO_EXPOSURE.created());

		value.symptoms.forEach(c -> assertThat(c.createdAt).as("Check symptoms.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.symptoms).as("Check symptoms").containsOnly(DIARRHEA.created(), FEVER.created());
	}

	@Test
	public void z_10_addWithChildren_count()
	{
		countByChildren(1L, 0L);
	}

	@Test
	public void z_10_changeLess()
	{
		dao.update(VALUE.nullConditions().nullExposures().nullSymptoms());
	}

	@Test
	public void z_10_changeLess_check()
	{
		z_10_addWithChildren_check();
	}

	@Test
	public void z_10_changeLess_count()
	{
		z_10_addWithChildren_count();
	}

	@Test
	public void z_10_modifyWithChildren()
	{
		dao.update(VALUE
			.withConditions(CARDIO_RESPIRATORY_DISEASE, KIDNEY_CIRRHOSIS, WEAKENED_IMMUNE_SYSTEM)
			.withExposures(LIVE_WITH, UNSURE)
			.withSymptoms(NAUSEA_VOMITING, SHORTNESS_OF_BREATH, SORE_THROAT));

		var now = new Date();
		VALUE.conditions.forEach(c -> assertThat(c.createdAt).as("Check conditions.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.conditions).as("Check conditions").containsOnly(CARDIO_RESPIRATORY_DISEASE.created(), KIDNEY_CIRRHOSIS.created(), WEAKENED_IMMUNE_SYSTEM.created());

		VALUE.exposures.forEach(c -> assertThat(c.createdAt).as("Check exposures.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.exposures).as("Check exposures").containsOnly(LIVE_WITH.created(), UNSURE.created());

		VALUE.symptoms.forEach(c -> assertThat(c.createdAt).as("Check symptoms.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.symptoms).as("Check symptoms").containsOnly(NAUSEA_VOMITING.created(), SHORTNESS_OF_BREATH.created(), SORE_THROAT.created());
	}

	@Test
	public void z_10_modifyWithChildren_check()
	{
		var now = new Date();
		var value = dao.getById(VALUE.id);
		value.conditions.forEach(c -> assertThat(c.createdAt).as("Check conditions.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.conditions).as("Check conditions").containsOnly(CARDIO_RESPIRATORY_DISEASE.created(), KIDNEY_CIRRHOSIS.created(), WEAKENED_IMMUNE_SYSTEM.created());

		value.exposures.forEach(c -> assertThat(c.createdAt).as("Check exposures.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.exposures).as("Check exposures").containsOnly(LIVE_WITH.created(), UNSURE.created());

		value.symptoms.forEach(c -> assertThat(c.createdAt).as("Check symptoms.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.symptoms).as("Check symptoms").containsOnly(NAUSEA_VOMITING.created(), SHORTNESS_OF_BREATH.created(), SORE_THROAT.created());
	}

	@Test
	public void z_10_modifyWithChildren_count()
	{
		countByChildren(0L, 1L);
	}

	@Test
	public void z_10_removeChildren()
	{
		dao.update(VALUE.emptyConditions().emptyExposures().emptySymptoms());
	}

	@Test
	public void z_10_removeChildren_check()
	{
		var value = dao.getById(VALUE.id);
		Assertions.assertNull(value.conditions, "Check conditions");
		Assertions.assertNull(value.exposures, "Check exposures");
		Assertions.assertNull(value.symptoms, "Check symptoms");
	}

	@Test
	public void z_10_removeWithChildren_count()
	{
		countByChildren(0L, 0L);
	}

	@Test
	public void z_11_modifyWithAllChildren()
	{
		dao.update(VALUE
			.withConditions(CARDIO_RESPIRATORY_DISEASE, DIABETIC, KIDNEY_CIRRHOSIS, PREGNANT, WEAKENED_IMMUNE_SYSTEM)
			.withExposures(CLOSE_CONTACT, LIVE_WITH, NO_EXPOSURE, UNSURE)
			.withSymptoms(DIARRHEA, DRY_COUGH, FATIGUE, FEVER, MUSCLE_ACHE, NAUSEA_VOMITING, RUNNY_NOSE, SHORTNESS_OF_BREATH, SORE_THROAT));

		var now = new Date();
		VALUE.conditions.forEach(c -> assertThat(c.createdAt).as("Check conditions.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.conditions).as("Check conditions").containsOnly(CARDIO_RESPIRATORY_DISEASE.created(), DIABETIC.created(), KIDNEY_CIRRHOSIS.created(), PREGNANT.created(), WEAKENED_IMMUNE_SYSTEM.created());

		VALUE.exposures.forEach(c -> assertThat(c.createdAt).as("Check exposures.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.exposures).as("Check exposures").containsOnly(CLOSE_CONTACT.created(), LIVE_WITH.created(), NO_EXPOSURE.created(), UNSURE.created());

		VALUE.symptoms.forEach(c -> assertThat(c.createdAt).as("Check symptoms.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.symptoms).as("Check symptoms").containsOnly(DIARRHEA.created(), DRY_COUGH.created(), FATIGUE.created(), FEVER.created(), MUSCLE_ACHE.created(), NAUSEA_VOMITING.created(), RUNNY_NOSE.created(), SHORTNESS_OF_BREATH.created(), SORE_THROAT.created());
	}

	@Test
	public void z_11_modifyWithAllChildren_check()
	{
		var now = new Date();
		var value = dao.getById(VALUE.id);
		value.conditions.forEach(c -> assertThat(c.createdAt).as("Check conditions.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.conditions).as("Check conditions").containsOnly(CARDIO_RESPIRATORY_DISEASE.created(), DIABETIC.created(), KIDNEY_CIRRHOSIS.created(), PREGNANT.created(), WEAKENED_IMMUNE_SYSTEM.created());

		value.exposures.forEach(c -> assertThat(c.createdAt).as("Check exposures.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.exposures).as("Check exposures").containsOnly(CLOSE_CONTACT.created(), LIVE_WITH.created(), NO_EXPOSURE.created(), UNSURE.created());

		value.symptoms.forEach(c -> assertThat(c.createdAt).as("Check symptoms.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.symptoms).as("Check symptoms").containsOnly(DIARRHEA.created(), DRY_COUGH.created(), FATIGUE.created(), FEVER.created(), MUSCLE_ACHE.created(), NAUSEA_VOMITING.created(), RUNNY_NOSE.created(), SHORTNESS_OF_BREATH.created(), SORE_THROAT.created());
	}

	@Test
	public void z_11_modifyWithAllChildren_count()
	{
		countByChildren(1L, 1L);
	}

	@Test
	public void z_12_modifyWithANullChild()
	{
		dao.update(VALUE
			.withConditions(Arrays.asList(null, DIABETIC.created(), null, PREGNANT.created(), null))
			.withExposures(Arrays.asList(CLOSE_CONTACT.created(), null, NO_EXPOSURE.created(), null))
			.withSymptoms(Arrays.asList(DIARRHEA.created(), null, FEVER.created(), null)));

		var now = new Date();
		VALUE.conditions.stream().filter(c -> null != c).forEach(c -> assertThat(c.createdAt).as("Check conditions.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.conditions).as("Check conditions").containsOnly(null, DIABETIC.created(), null, PREGNANT.created(), null);

		VALUE.exposures.stream().filter(c -> null != c).forEach(c -> assertThat(c.createdAt).as("Check exposures.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.exposures).as("Check exposures").containsOnly(CLOSE_CONTACT.created(), null, NO_EXPOSURE.created(), null);

		VALUE.symptoms.stream().filter(c -> null != c).forEach(c -> assertThat(c.createdAt).as("Check symptoms.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(VALUE.symptoms).as("Check symptoms").containsOnly(DIARRHEA.created(), null, FEVER.created(), null);
	}

	@Test
	public void z_12_modifyWithANullChild_check()
	{
		var now = new Date();
		var value = dao.getById(VALUE.id);
		value.conditions.forEach(c -> assertThat(c.createdAt).as("Check conditions.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.conditions).as("Check conditions").containsOnly(DIABETIC.created(), PREGNANT.created());

		value.exposures.forEach(c -> assertThat(c.createdAt).as("Check exposures.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.exposures).as("Check exposures").containsOnly(CLOSE_CONTACT.created(), NO_EXPOSURE.created());

		value.symptoms.forEach(c -> assertThat(c.createdAt).as("Check symptoms.createdAt: " + c.id).isCloseTo(now, 500L));
		assertThat(value.symptoms).as("Check symptoms").containsOnly(DIARRHEA.created(), FEVER.created());
	}

	@Test
	public void z_12_modifyWithANullChild_count()
	{
		countByChildren(1L, 0L);
	}

	private static List<Named> n(final int... indices)
	{
		return Arrays.stream(indices).mapToObj(i -> StringUtils.leftPad(i + "", 3, '0')).map(v -> new Named(v)).collect(toList());
	}

	private static List<String> nm(final int... indices)
	{
		return Arrays.stream(indices).mapToObj(i -> StringUtils.leftPad(i + "", 3, '0')).collect(toList());
	}

	@Test
	public void z_20()
	{
		IntStream.range(0, 100).mapToObj(i -> StringUtils.leftPad(i + "", 3, '0')).forEach(i -> dao.add(new PeopleValue(i, "+1" + i, !i.endsWith("7"))));
	}

	public static Stream<Arguments> z_20_find()
	{
		return Stream.of(
			arguments(List.of("004", "007", "020", "200", "060", "\t", "005", "26"), null, null, new int[] { 4, 5, 20, 60 }),
			arguments(List.of("004", "007", "020", "200", "060", "\t", "005", "26"), null, 2L, new int[] { 4 }),	// Only one is returned because the list is cut short.
			arguments(List.of("004", "007", "020", "200", "060", "\t", "005", "26"), List.of(" ", " \n "), 2L, new int[] { 4 }),
			arguments(List.of("", "  ", " \n"), List.of("+1075", "080", "12", "+1087", "+1015", "+1037", "\n\n", "+1043", "+1007", "", "002", "1000", "+1011"), 30L, new int[] { 2, 11, 15, 43, 75, 80 }),
			arguments(List.of("", "050", " \n"), List.of("+1075", "080", "12", "+1087", "+1015", "+1037", "\n\n", "+1043", "+1007", "", "002", "1000", "+1011"), 30L, new int[] {}),	// Intersection of names and phones filter do not have overlapping results.
			arguments(List.of("", "050", " \n", "037", "015", "043"), List.of("+1075", "080", "12", "+1087", "+1015", "+1037", "\n\n", "+1043", "+1007", "", "002", "1000", "+1011"), 30L, new int[] { 15, 43 })
		 );
	}

	@ParameterizedTest
	@MethodSource
	public void z_20_find(final List<String> names, final List<String> phones, final Long pageSize, final int[] indices)
	{
		assertThat(dao.find(new PeopleFindRequest(names, phones, pageSize))).isEqualTo(n(indices));
	}

	public static Stream<PeopleFindRequest> z_20_find_invalid()
	{
		return Stream.of(
			new PeopleFindRequest(null, null, null),
			new PeopleFindRequest(List.of(), null, null),
			new PeopleFindRequest(null, List.of(), null),
			new PeopleFindRequest(List.of(), List.of(), null),
			new PeopleFindRequest(List.of(" ", " \n"), List.of(" ", " \t "), null));
	}

	@ParameterizedTest
	@MethodSource
	public void z_20_find_invalid(final PeopleFindRequest request)
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.find(request)))
			.hasMessage("Please provide at least one name or one phone number.");
	}

	@ParameterizedTest
	@CsvSource({"079,079",
	            "077,",	// Inactive
	            "09,",
	            "009,009",
	            "007,",	// Inactive
	            "0790,",
	            "+079,",
	            "+1079,079",
	            "+1077,",	// Inactive
	            "+10790,"})
	public void z_20_findOne(final String name, final String expected)
	{
		var o = dao.findOne(name);
		if (null != expected)
		{
			Assertions.assertNotNull(o, "Exists");
			Assertions.assertEquals(expected, o.getName(), "Check name");
		}
		else
			Assertions.assertNull(o, "Exists");
	}

	public static Stream<Arguments> z_20_getActiveByIdOrName()
	{
		return Stream.of(
			arguments("invalid", null),
			arguments("006", new int[] { 6 }),
			arguments("007", null),	// Inactive
			arguments("105", null),
			arguments("200", null),
			arguments("00", new int[] { 0, 1, 2, 3, 4, 5, 6, 8, 9 }),
			arguments("+1043", new int[] { 43 }),
			arguments("+1047", null),	// Inactive
			arguments("+1101", null),
			arguments("+12", null),
			arguments("+104", new int[] { 40, 41, 42, 43, 44, 45, 46, 48, 49 }));
	}

	@ParameterizedTest
	@MethodSource
	public void z_20_getActiveByIdOrName(final String name, final int[] indices)
	{
		var o = assertThat(dao.getActiveByIdOrName(name).stream().map(v -> v.name).collect(toList()));
		if (null != indices) o.isEqualTo(nm(indices));
		else o.isEmpty();
	}

	private void countByChildren(final long first, final long second)
	{
		var combined = first | second;

		count(new PeopleFilter().withIncludeConditions(DIABETIC.id), first);
		count(new PeopleFilter().withIncludeConditions(PREGNANT.id), first);
		count(new PeopleFilter().withIncludeConditions(DIABETIC.id, PREGNANT.id), first);
		count(new PeopleFilter().withIncludeConditions(DIABETIC.id, KIDNEY_CIRRHOSIS.id, PREGNANT.id), combined);
		count(new PeopleFilter().withIncludeConditions(CARDIO_RESPIRATORY_DISEASE.id, PREGNANT.id), combined);
		count(new PeopleFilter().withIncludeConditions(DIABETIC.id, WEAKENED_IMMUNE_SYSTEM.id), combined);
		count(new PeopleFilter().withIncludeConditions(CARDIO_RESPIRATORY_DISEASE.id), second);
		count(new PeopleFilter().withIncludeConditions(KIDNEY_CIRRHOSIS.id), second);
		count(new PeopleFilter().withIncludeConditions(WEAKENED_IMMUNE_SYSTEM.id), second);
		count(new PeopleFilter().withIncludeConditions(CARDIO_RESPIRATORY_DISEASE.id, KIDNEY_CIRRHOSIS.id, WEAKENED_IMMUNE_SYSTEM.id), second);

		count(new PeopleFilter().withExcludeConditions(DIABETIC.id), 2L - first);
		count(new PeopleFilter().withExcludeConditions(PREGNANT.id), 2L- first);
		count(new PeopleFilter().withExcludeConditions(DIABETIC.id, PREGNANT.id), 2L - first);
		count(new PeopleFilter().withExcludeConditions(DIABETIC.id, KIDNEY_CIRRHOSIS.id, PREGNANT.id), 2L - combined);
		count(new PeopleFilter().withExcludeConditions(CARDIO_RESPIRATORY_DISEASE.id, PREGNANT.id), 2L - combined);
		count(new PeopleFilter().withExcludeConditions(DIABETIC.id, WEAKENED_IMMUNE_SYSTEM.id), 2L - combined);
		count(new PeopleFilter().withExcludeConditions(CARDIO_RESPIRATORY_DISEASE.id), 2L - second);
		count(new PeopleFilter().withExcludeConditions(KIDNEY_CIRRHOSIS.id), 2L - second);
		count(new PeopleFilter().withExcludeConditions(WEAKENED_IMMUNE_SYSTEM.id), 2L - second);
		count(new PeopleFilter().withExcludeConditions(CARDIO_RESPIRATORY_DISEASE.id, KIDNEY_CIRRHOSIS.id, WEAKENED_IMMUNE_SYSTEM.id), 2L - second);

		count(new PeopleFilter().withIncludeExposures(CLOSE_CONTACT.id), first);
		count(new PeopleFilter().withIncludeExposures(NO_EXPOSURE.id), first);
		count(new PeopleFilter().withIncludeExposures(CLOSE_CONTACT.id, NO_EXPOSURE.id), first);
		count(new PeopleFilter().withIncludeExposures(CLOSE_CONTACT.id, LIVE_WITH.id, NO_EXPOSURE.id), combined);
		count(new PeopleFilter().withIncludeExposures(UNSURE.id, NO_EXPOSURE.id), combined);
		count(new PeopleFilter().withIncludeExposures(CLOSE_CONTACT.id, UNSURE.id), combined);
		count(new PeopleFilter().withIncludeExposures(UNSURE.id), second);
		count(new PeopleFilter().withIncludeExposures(LIVE_WITH.id), second);
		count(new PeopleFilter().withIncludeExposures(UNSURE.id, LIVE_WITH.id), second);

		count(new PeopleFilter().withExcludeExposures(CLOSE_CONTACT.id), 2L - first);
		count(new PeopleFilter().withExcludeExposures(NO_EXPOSURE.id), 2L- first);
		count(new PeopleFilter().withExcludeExposures(CLOSE_CONTACT.id, NO_EXPOSURE.id), 2L - first);
		count(new PeopleFilter().withExcludeExposures(CLOSE_CONTACT.id, LIVE_WITH.id, NO_EXPOSURE.id), 2L - combined);
		count(new PeopleFilter().withExcludeExposures(UNSURE.id, NO_EXPOSURE.id), 2L - combined);
		count(new PeopleFilter().withExcludeExposures(CLOSE_CONTACT.id, UNSURE.id), 2L - combined);
		count(new PeopleFilter().withExcludeExposures(UNSURE.id), 2L - second);
		count(new PeopleFilter().withExcludeExposures(LIVE_WITH.id), 2L - second);
		count(new PeopleFilter().withExcludeExposures(UNSURE.id, LIVE_WITH.id), 2L - second);

		count(new PeopleFilter().withIncludeSymptoms(DIARRHEA.id), first);
		count(new PeopleFilter().withIncludeSymptoms(FEVER.id), first);
		count(new PeopleFilter().withIncludeSymptoms(DIARRHEA.id, FEVER.id), first);
		count(new PeopleFilter().withIncludeSymptoms(DIARRHEA.id, SHORTNESS_OF_BREATH.id, FEVER.id), combined);
		count(new PeopleFilter().withIncludeSymptoms(NAUSEA_VOMITING.id, FEVER.id), combined);
		count(new PeopleFilter().withIncludeSymptoms(DIARRHEA.id, SORE_THROAT.id), combined);
		count(new PeopleFilter().withIncludeSymptoms(NAUSEA_VOMITING.id), second);
		count(new PeopleFilter().withIncludeSymptoms(SHORTNESS_OF_BREATH.id), second);
		count(new PeopleFilter().withIncludeSymptoms(SORE_THROAT.id), second);
		count(new PeopleFilter().withIncludeSymptoms(NAUSEA_VOMITING.id, SHORTNESS_OF_BREATH.id, SORE_THROAT.id), second);

		count(new PeopleFilter().withExcludeSymptoms(DIARRHEA.id), 2L - first);
		count(new PeopleFilter().withExcludeSymptoms(FEVER.id), 2L- first);
		count(new PeopleFilter().withExcludeSymptoms(DIARRHEA.id, FEVER.id), 2L - first);
		count(new PeopleFilter().withExcludeSymptoms(DIARRHEA.id, SHORTNESS_OF_BREATH.id, FEVER.id), 2L - combined);
		count(new PeopleFilter().withExcludeSymptoms(NAUSEA_VOMITING.id, FEVER.id), 2L - combined);
		count(new PeopleFilter().withExcludeSymptoms(DIARRHEA.id, SORE_THROAT.id), 2L - combined);
		count(new PeopleFilter().withExcludeSymptoms(NAUSEA_VOMITING.id), 2L - second);
		count(new PeopleFilter().withExcludeSymptoms(SHORTNESS_OF_BREATH.id), 2L - second);
		count(new PeopleFilter().withExcludeSymptoms(SORE_THROAT.id), 2L - second);
		count(new PeopleFilter().withExcludeSymptoms(NAUSEA_VOMITING.id, SHORTNESS_OF_BREATH.id, SORE_THROAT.id), 2L - second);
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
		Assertions.assertEquals(expected.sexId, record.getSexId(), assertId + "Check sexId");
		Assertions.assertEquals(expected.healthWorkerStatusId, record.getHealthWorkerStatusId(), assertId + "Check healthWorkerStatusId");
		assertThat(record.getLatitude()).as(assertId + "Check latitude").isEqualByComparingTo(expected.latitude);
		assertThat(record.getLongitude()).as(assertId + "Check longitude").isEqualByComparingTo(expected.longitude);
		Assertions.assertEquals(expected.locationName, record.getLocationName(), assertId + "Check locationName");
		Assertions.assertEquals(expected.alertable, record.isAlertable(), assertId + "Check alertable");
		Assertions.assertEquals(expected.active, record.isActive(), assertId + "Check active");
		Assertions.assertEquals(expected.authAt, record.getAuthAt(), assertId + "Check authAt");
		Assertions.assertEquals(expected.phoneVerifiedAt, record.getPhoneVerifiedAt(), assertId + "Check phoneVerifiedAt");
		Assertions.assertEquals(expected.emailVerifiedAt, record.getEmailVerifiedAt(), assertId + "Check emailVerifiedAt");
		Assertions.assertEquals(expected.alertedOf, record.getAlertedOf(), assertId + "Check alertedOf");
		Assertions.assertEquals(expected.alertedAt, record.getAlertedAt(), assertId + "Check alertedAt");
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
		Assertions.assertEquals(expected.sexId, value.sexId, assertId + "Check sexId");
		Assertions.assertEquals(expected.sex, value.sex, assertId + "Check sex");
		Assertions.assertEquals(expected.healthWorkerStatusId, value.healthWorkerStatusId, assertId + "Check healthWorkerStatusId");
		Assertions.assertEquals(expected.healthWorkerStatus, value.healthWorkerStatus, assertId + "Check healthWorkerStatus");
		assertThat(value.latitude).as(assertId + "Check latitude").isEqualByComparingTo(expected.latitude);
		assertThat(value.longitude).as(assertId + "Check longitude").isEqualByComparingTo(expected.longitude);
		Assertions.assertEquals(expected.locationName, value.locationName, assertId + "Check locationName");
		Assertions.assertEquals(expected.alertable, value.alertable, assertId + "Check alertable");
		Assertions.assertEquals(expected.active, value.active, assertId + "Check active");
		Assertions.assertEquals(expected.authAt, value.authAt, assertId + "Check authAt");
		Assertions.assertEquals(expected.phoneVerifiedAt, value.phoneVerifiedAt, assertId + "Check phoneVerifiedAt");
		Assertions.assertEquals(expected.emailVerifiedAt, value.emailVerifiedAt, assertId + "Check emailVerifiedAt");
		Assertions.assertEquals(expected.alertedOf, value.alertedOf, assertId + "Check alertedOf");
		Assertions.assertEquals(expected.alertedAt, value.alertedAt, assertId + "Check alertedAt");
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, value.updatedAt, assertId + "Check updatedAt");
		Assertions.assertNull(value.conditions, assertId + "Check conditons");
	}
}
