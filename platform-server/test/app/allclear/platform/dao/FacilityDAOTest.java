package app.allclear.platform.dao;

import static java.util.stream.Collectors.toList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static app.allclear.testing.TestingUtils.*;
import static app.allclear.platform.type.FacilityType.*;
import static app.allclear.platform.type.TestCriteria.*;
import static app.allclear.platform.type.TestType.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.PersistenceException;

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
import app.allclear.common.value.CreatedValue;
import app.allclear.platform.App;
import app.allclear.platform.entity.CountByName;
import app.allclear.platform.entity.Facility;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.filter.GeoFilter;
import app.allclear.platform.value.FacilityValue;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Facility entity.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class FacilityDAOTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static FacilityDAO dao = null;
	private static PeopleDAO peopleDao = null;
	private static final TestAuditor auditor = new TestAuditor();
	private static FacilityValue VALUE = null;
	private static FacilityValue VALUE_1 = null;
	private static PeopleValue PERSON = null;
	private static PeopleValue PERSON_1 = null;

	private static BigDecimal bg(final String value) { return new BigDecimal(value); }

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new FacilityDAO(factory, auditor);
		peopleDao = new PeopleDAO(factory);
	}

	private static int auditorAdds = 0;
	private static int auditorUpdates = 0;
	private static int auditorRemoves = 0;

	@BeforeEach
	public void beforeEach()
	{
		Assertions.assertEquals(auditorAdds, auditor.adds, "Check auditorAdds");
		Assertions.assertEquals(auditorUpdates, auditor.updates, "Check auditorUpdates");
		Assertions.assertEquals(auditorRemoves, auditor.removes, "Check auditorRemoves");
	}

	@Test
	public void add()
	{
		var value = dao.add(VALUE = new FacilityValue("Adam", "101 McClain Ave", "Miami", "FL", bg("45"), bg("-35"),
			"888-555-1000", "888-555-1001", "adam@test.com", "http://www.adam.com", "http://www.adam.com/appoinment", "8AM to 10PM",
			HOSPITAL.id, true, false, true, false, OTHER.id, "My other criteria", 2500, true, 16, "Doctor requires: something",
			false, true, false, "These providers are accepted: One", true, false, false, "Quick notations", true), true);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertTrue(value.active, "Check active");
		assertThat(value.activatedAt).as("Check activatedAt").isNotNull().isCloseTo(new Date(), 500L);
		check(VALUE, value);

		PERSON = peopleDao.add(new PeopleValue("first", "8885551000", true));
		PERSON_1 = peopleDao.add(new PeopleValue("second", "8885551001", true));
		peopleDao.addFacilities(PERSON.id, List.of(value.id));

		auditorAdds++;
	}

	/** Creates a valid Facility value for the validation tests.
	 *	@return never NULL.
	*/
	public static FacilityValue createValid()
	{
		return new FacilityValue("Eve", "909 Stuart St", "Atlanta", "GA", bg("-45"), bg("35"),
			"888-555-2000", "888-555-2001", "eve@test.net", "http://www.eve.net", "http://www.eve.net/calendar", "10AM to 8PM",
			URGENT_CARE.id, false, true, false, true, OTHER.id, "My other criteria", 2500, false, 16, "Doctor requires: something",
			true, false, true, "These providers are accepted: Two", false, true, true, "Slow notations", false);
	}

	@Test
	public void add_missingName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withName(null), true));
	}

	@Test
	public void add_longName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withName(StringUtils.repeat("A", FacilityValue.MAX_LEN_NAME + 1)), true));
	}

	@Test
	public void add_missingAddress()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withAddress(null), true));
	}

	@Test
	public void add_longAddress()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withAddress(StringUtils.repeat("A", FacilityValue.MAX_LEN_ADDRESS + 1)), true));
	}

	@Test
	public void add_missingCity()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withCity(null), true));
	}

	@Test
	public void add_longCity()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withCity(StringUtils.repeat("A", FacilityValue.MAX_LEN_CITY + 1)), true));
	}

	@Test
	public void add_missingState()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withState(null), true));
	}

	@Test
	public void add_longState()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withState(StringUtils.repeat("A", FacilityValue.MAX_LEN_STATE + 1)), true));
	}

	@Test
	public void add_longCountyId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withCountyId(StringUtils.repeat("A", FacilityValue.MAX_LEN_COUNTY_ID + 1)), true));
	}

	@Test
	public void add_longCountyName()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withCountyName(StringUtils.repeat("A", FacilityValue.MAX_LEN_COUNTY_NAME + 1)), true));
	}

	@Test
	public void add_highLatitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLatitude(bg("91")), true));
	}

	@Test
	public void add_lowLatitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLatitude(bg("-91")), true));
	}

	@Test
	public void add_highLongitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLongitude(bg("181")), true));
	}

	@Test
	public void add_lowLongitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLongitude(bg("-181")), true));
	}

	@Test
	public void add_missingLongitude()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withLongitude(null), true));
	}

	@Test
	public void add_longPhone()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withPhone(StringUtils.repeat("A", FacilityValue.MAX_LEN_PHONE + 1)), true));
	}

	@Test
	public void add_longAppointmentPhone()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withAppointmentPhone(StringUtils.repeat("A", FacilityValue.MAX_LEN_APPOINTMENT_PHONE + 1)), true));
	}

	@Test
	public void add_longEmail()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withEmail(StringUtils.repeat("A", FacilityValue.MAX_LEN_EMAIL + 1)), true));
	}

	@Test
	public void add_longUrl()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withUrl(StringUtils.repeat("A", FacilityValue.MAX_LEN_URL + 1)), true));
	}

	@Test
	public void add_longAppointmentUrl()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withAppointmentUrl(StringUtils.repeat("A", FacilityValue.MAX_LEN_APPOINTMENT_URL + 1)), true));
	}

	@Test
	public void add_longHours()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withHours(StringUtils.repeat("A", FacilityValue.MAX_LEN_HOURS + 1)), true));
	}

	@Test
	public void add_invalidTypeId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withTypeId(StringUtils.repeat("1", FacilityValue.MAX_LEN_TYPE_ID)), true));
	}

	@Test
	public void add_longTypeId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withTypeId(StringUtils.repeat("A", FacilityValue.MAX_LEN_TYPE_ID + 1)), true));
	}

	@Test
	public void add_invalidTestCriteriaId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withTestCriteriaId(StringUtils.repeat("1", FacilityValue.MAX_LEN_TEST_CRITERIA_ID)), true));
	}

	@Test
	public void add_longTestCriteriaId()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withTestCriteriaId(StringUtils.repeat("A", FacilityValue.MAX_LEN_TEST_CRITERIA_ID + 1)), true));
	}

	@Test
	public void add_longOtherTestCriteria()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withOtherTestCriteria(StringUtils.repeat("A", FacilityValue.MAX_LEN_OTHER_TEST_CRITERIA + 1)), true));
	}

	@Test
	public void add_longDoctorReferralCriteria()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withDoctorReferralCriteria(StringUtils.repeat("A", FacilityValue.MAX_LEN_DOCTOR_REFERRAL_CRITERIA + 1)), true));
	}

	@Test
	public void add_longInsuranceProvidersAccepted()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withInsuranceProvidersAccepted(StringUtils.repeat("A", FacilityValue.MAX_LEN_INSURANCE_PROVIDERS_ACCEPTED + 1)), true));
	}

	@Test
	public void add_longNotes()
	{
		assertThrows(ValidationException.class, () -> dao.add(createValid().withNotes(StringUtils.repeat("A", FacilityValue.MAX_LEN_NOTES + 1)), true));
	}

	@Test
	public void add_invalidTestType()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withTestTypes(List.of(new CreatedValue("$$"))), true)))
			.hasMessage("'$$' is not a valid Test Type.");
	}

	@Test
	public void add_missingTestType()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(createValid().withTestTypes(List.of(new CreatedValue(null))), true)))
			.hasMessage("Test Type is not set.");
	}

	@Test
	public void countActivatedAtByDistance()
	{
		assertThat(assertThrows(PersistenceException.class, () -> dao.countActivatedAtByDistance(new Date(), bg("45.5"), bg("-35.7"), 100000L, 20)))	// Function "ST_DISTANCE_SPHERE" not found
			.hasMessage("org.hibernate.exception.SQLGrammarException: could not prepare statement");
	}

	public static Stream<Arguments> exist()
	{
		return Stream.of(
			arguments(null, false),
			arguments(0L, false),
			arguments(VALUE.id, true),
			arguments(100L, false));
	}

	@ParameterizedTest
	@MethodSource
	public void exist(final Long id, final boolean expected)
	{
		Assertions.assertEquals(expected, dao.exists(id));
	}

	@Test
	public void favorite()
	{
		Assertions.assertNull(VALUE.favorite);

		VALUE.favorite(List.of(0L, 2L));
		Assertions.assertFalse(VALUE.favorite);

		VALUE.favorite(List.of(0L, 1L, 2L));
		Assertions.assertTrue(VALUE.favorite);
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
		assertThrows(ObjectNotFoundException.class, () -> dao.findWithException(VALUE.id + 1000L));
	}

	@Test
	public void findByName()
	{
		var record = dao.find("Adam");
		Assertions.assertNotNull(record, "Exists");
		check(VALUE, record);
	}

	@Test
	public void findByName_invalid()
	{
		Assertions.assertNull(dao.find("Eve"));
	}

	@Test
	public void get()
	{
		var value = dao.getByIdWithException(VALUE.id, true);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertTrue(value.active, "Check active");
		assertThat(value.activatedAt).as("Check activatedAt").isNotNull().isEqualTo(value.createdAt);
		Assertions.assertNull(value.testTypes, "Check testTypes");
		check(VALUE, value);
	}

	@Test
	public void getWithException()
	{
		assertThrows(ObjectNotFoundException.class, () -> dao.getByIdWithException(VALUE.id + 1000L, true));
	}

	@Test
	public void getActiveByName()
	{
		assertThat(dao.getActiveByName("da")).containsExactly(VALUE);
	}

	@Test
	public void getActiveByName_invalid()
	{
		assertThat(dao.getActiveByName("Eve")).isEmpty();
	}

	@Test
	public void getActiveByNameAndDistance()
	{
		assertThrows(PersistenceException.class, () -> dao.getActiveByNameAndDistance("da", bg("45.5"), bg("-35.7"), 100000L));	// Function "ST_DISTANCE_SPHERE" not found
	}

	@Test
	public void getByNameWithException()
	{
		Assertions.assertEquals(VALUE, dao.getByNameWithException(VALUE.name, true));
	}

	@Test
	public void getByNameWithException_invalid()
	{
		assertThat(assertThrows(ObjectNotFoundException.class, () -> dao.getByNameWithException("invalid", true)))
			.hasMessage("The Facility 'invalid' does not exist.");
	}

	public static Stream<Arguments> getIdsByPerson()
	{
		return Stream.of(
			arguments(PERSON, new Long[] { VALUE.id }),
			arguments(PERSON_1, new Long[0]),
			arguments(null, new Long[0]));
	}

	@ParameterizedTest
	@MethodSource
	public void getIdsByPerson(final PeopleValue person, final Long[] ids)
	{
		assertThat(dao.getIdsByPerson(person)).isEqualTo(List.of(ids));
	}

	public static Stream<Arguments> getDistinctCitiesByState()
	{
		return Stream.of(
			arguments("Florida", List.of()),
			arguments("FL", List.of(new CountByName("Miami", 1L))),
			arguments("Georgia", List.of()),
			arguments("GA", List.of()));
	}

	@ParameterizedTest
	@MethodSource
	public void getDistinctCitiesByState(final String state, final List<CountByName> expected)
	{
		Assertions.assertEquals(expected, dao.getDistinctCitiesByState(state));
	}

	@Test
	public void getDistinctStates()
	{
		assertThat(dao.getDistinctStates()).containsExactly(new CountByName("FL", 1L));
	}

	@Test
	public void modify()
	{
		var v = createValid();
		count(new FacilityFilter().withName(VALUE.name), 1L);
		count(new FacilityFilter().withHasCountyId(false), 1L);
		count(new FacilityFilter().withHasCountyName(false), 1L);
		count(new FacilityFilter().withCanDonatePlasma(false), 1L);
		count(new FacilityFilter().withResultNotificationEnabled(false), 1L);
		count(new FacilityFilter().withActive(VALUE.active), 1L);
		count(new FacilityFilter().exclude(NASAL_SWAB), 1L);
		count(new FacilityFilter().exclude(ANTIBODY), 1L);
		count(new FacilityFilter().withName(v.name), 0L);
		count(new FacilityFilter().withHasCountyId(true), 0L);
		count(new FacilityFilter().withCountyId("48167"), 0L);
		count(new FacilityFilter().withHasCountyName(true), 0L);
		count(new FacilityFilter().withCountyName("Galveston"), 0L);
		count(new FacilityFilter().withCanDonatePlasma(true), 0L);
		count(new FacilityFilter().withResultNotificationEnabled(true), 0L);
		count(new FacilityFilter().withActive(v.active), 0L);
		count(new FacilityFilter().include(NASAL_SWAB), 0L);
		count(new FacilityFilter().include(ANTIBODY), 0L);

		var value = dao.update(v.withId(VALUE.id).withCounty("48167", "Galveston").withCanDonatePlasma(true).withResultNotificationEnabled(true).withTestTypes(NASAL_SWAB), true);
		Assertions.assertNotNull(value, "Exists");
		check(v, value);

		VALUE_1 = VALUE;
		VALUE = v;

		auditorUpdates++;
	}

	@Test
	public void modify_count()
	{
		var v = createValid();
		count(new FacilityFilter().withName(VALUE_1.name), 0L);
		count(new FacilityFilter().withHasCountyId(false), 0L);
		count(new FacilityFilter().withHasCountyName(false), 0L);
		count(new FacilityFilter().withCanDonatePlasma(false), 0L);
		count(new FacilityFilter().withResultNotificationEnabled(false), 0L);
		count(new FacilityFilter().withActive(VALUE_1.active), 0L);
		count(new FacilityFilter().exclude(NASAL_SWAB), 0L);
		count(new FacilityFilter().exclude(ANTIBODY), 1L);
		count(new FacilityFilter().withName(v.name), 1L);
		count(new FacilityFilter().withHasCountyId(true), 1L);
		count(new FacilityFilter().withCountyId("48167"), 1L);
		count(new FacilityFilter().withHasCountyName(true), 1L);
		count(new FacilityFilter().withCountyName("Galveston"), 1L);
		count(new FacilityFilter().withCanDonatePlasma(true), 1L);
		count(new FacilityFilter().withResultNotificationEnabled(true), 1L);
		count(new FacilityFilter().withActive(v.active), 1L);
		count(new FacilityFilter().include(NASAL_SWAB), 1L);
		count(new FacilityFilter().include(ANTIBODY), 0L);
	}

	@Test
	public void modify_find()
	{
		var record = dao.findWithException(VALUE.id);
		Assertions.assertNotNull(record, "Exists");
		Assertions.assertEquals("Eve", record.getName(), "Check name");
		Assertions.assertEquals("48167", record.getCountyId(), "Check countyId");
		Assertions.assertEquals("Galveston", record.getCountyName(), "Check countyName");
		Assertions.assertTrue(record.isCanDonatePlasma(), "Check canDonatePlasma");
		Assertions.assertTrue(record.isResultNotificationEnabled(), "Check resultNotificationEnabled");
		Assertions.assertFalse(record.isActive(), "Check active");
		check(VALUE, record);
	}

	@Test
	public void modify_get()
	{
		assertThat(dao.getById(VALUE.id, true).testTypes).containsExactly(NASAL_SWAB.created());
	}

	@Test
	public void modify_getActiveByName()
	{
		assertThat(dao.getActiveByName(VALUE.name)).isEmpty();
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();
		var latUp = VALUE.latitude.add(bg("1"));
		var latDown = VALUE.latitude.subtract(bg("1"));
		var lngUp = VALUE.longitude.add(bg("1"));
		var lngDown = VALUE.longitude.subtract(bg("1"));

		return Stream.of(
			arguments(new FacilityFilter(1, 20).withId(VALUE.id), 1L),
			arguments(new FacilityFilter(1, 20).withIdFrom(VALUE.id), 1L),
			arguments(new FacilityFilter(1, 20).withIdTo(VALUE.id), 1L),
			arguments(new FacilityFilter(1, 20).withName(VALUE.name), 1L),
			arguments(new FacilityFilter(1, 20).withAddress(VALUE.address), 1L),
			arguments(new FacilityFilter(1, 20).withCity(VALUE.city), 1L),
			arguments(new FacilityFilter(1, 20).withState(VALUE.state), 1L),
			arguments(new FacilityFilter(1, 20).withCountyId(VALUE.countyId), 1L),
			arguments(new FacilityFilter(1, 20).withHasCountyId(true), 1L),
			arguments(new FacilityFilter(1, 20).withCountyName(VALUE.countyName), 1L),
			arguments(new FacilityFilter(1, 20).withHasCountyName(true), 1L),
			arguments(new FacilityFilter(1, 20).withLatitudeFrom(latDown), 1L),
			arguments(new FacilityFilter(1, 20).withLatitudeTo(latUp), 1L),
			arguments(new FacilityFilter(1, 20).withLongitudeFrom(lngDown), 1L),
			arguments(new FacilityFilter(1, 20).withLongitudeTo(lngUp), 1L),
			arguments(new FacilityFilter(1, 20).withPhone(VALUE.phone), 1L),
			arguments(new FacilityFilter(1, 20).withHasPhone(true), 1L),
			arguments(new FacilityFilter(1, 20).withAppointmentPhone(VALUE.appointmentPhone), 1L),
			arguments(new FacilityFilter(1, 20).withHasAppointmentPhone(true), 1L),
			arguments(new FacilityFilter(1, 20).withEmail(VALUE.email), 1L),
			arguments(new FacilityFilter(1, 20).withHasEmail(true), 1L),
			arguments(new FacilityFilter(1, 20).withUrl(VALUE.url), 1L),
			arguments(new FacilityFilter(1, 20).withHasUrl(true), 1L),
			arguments(new FacilityFilter(1, 20).withAppointmentUrl(VALUE.appointmentUrl), 1L),
			arguments(new FacilityFilter(1, 20).withHasAppointmentUrl(true), 1L),
			arguments(new FacilityFilter(1, 20).withHours(VALUE.hours), 1L),
			arguments(new FacilityFilter(1, 20).withHasHours(true), 1L),
			arguments(new FacilityFilter(1, 20).withTypeId(VALUE.typeId), 1L),
			arguments(new FacilityFilter(1, 20).withHasTypeId(true), 1L),
			arguments(new FacilityFilter(1, 20).withDriveThru(VALUE.driveThru), 1L),
			arguments(new FacilityFilter(1, 20).withAppointmentRequired(VALUE.appointmentRequired), 1L),
			arguments(new FacilityFilter(1, 20).withHasAppointmentRequired(true), 1L),
			arguments(new FacilityFilter(1, 20).withAcceptsThirdParty(VALUE.acceptsThirdParty), 1L),
			arguments(new FacilityFilter(1, 20).withHasAcceptsThirdParty(true), 1L),
			arguments(new FacilityFilter(1, 20).withReferralRequired(VALUE.referralRequired), 1L),
			arguments(new FacilityFilter(1, 20).withTestCriteriaId(OTHER.id), 1L),
			arguments(new FacilityFilter(1, 20).withNotTestCriteriaId(CDC_CRITERIA.id), 1L),
			arguments(new FacilityFilter(1, 20).withHasTestCriteriaId(true), 1L),
			arguments(new FacilityFilter(1, 20).withOtherTestCriteria(VALUE.otherTestCriteria), 1L),
			arguments(new FacilityFilter(1, 20).withHasOtherTestCriteria(true), 1L),
			arguments(new FacilityFilter(1, 20).withTestsPerDay(VALUE.testsPerDay), 1L),
			arguments(new FacilityFilter(1, 20).withHasTestsPerDay(true), 1L),
			arguments(new FacilityFilter(1, 20).withTestsPerDayFrom(VALUE.testsPerDay), 1L),
			arguments(new FacilityFilter(1, 20).withTestsPerDayTo(VALUE.testsPerDay), 1L),
			arguments(new FacilityFilter(1, 20).withGovernmentIdRequired(VALUE.governmentIdRequired), 1L),
			arguments(new FacilityFilter(1, 20).withMinimumAge(VALUE.minimumAge), 1L),
			arguments(new FacilityFilter(1, 20).withHasMinimumAge(true), 1L),
			arguments(new FacilityFilter(1, 20).withMinimumAgeFrom(VALUE.minimumAge), 1L),
			arguments(new FacilityFilter(1, 20).withMinimumAgeTo(VALUE.minimumAge), 1L),
			arguments(new FacilityFilter(1, 20).withDoctorReferralCriteria(VALUE.doctorReferralCriteria), 1L),
			arguments(new FacilityFilter(1, 20).withHasDoctorReferralCriteria(true), 1L),
			arguments(new FacilityFilter(1, 20).withFirstResponderFriendly(VALUE.firstResponderFriendly), 1L),
			arguments(new FacilityFilter(1, 20).withTelescreeningAvailable(VALUE.telescreeningAvailable), 1L),
			arguments(new FacilityFilter(1, 20).withAcceptsInsurance(VALUE.acceptsInsurance), 1L),
			arguments(new FacilityFilter(1, 20).withInsuranceProvidersAccepted(VALUE.insuranceProvidersAccepted), 1L),
			arguments(new FacilityFilter(1, 20).withHasInsuranceProvidersAccepted(true), 1L),
			arguments(new FacilityFilter(1, 20).withFreeOrLowCost(VALUE.freeOrLowCost), 1L),
			arguments(new FacilityFilter(1, 20).withCanDonatePlasma(VALUE.canDonatePlasma), 1L),
			arguments(new FacilityFilter(1, 20).withResultNotificationEnabled(VALUE.resultNotificationEnabled), 1L),
			arguments(new FacilityFilter(1, 20).withNotes(VALUE.notes), 1L),
			arguments(new FacilityFilter(1, 20).withHasNotes(true), 1L),
			arguments(new FacilityFilter(1, 20).withActive(VALUE.active), 1L),
			arguments(new FacilityFilter(1, 20).withHasActivatedAt(true), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAgo).withActivatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withCreatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAgo), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 1L),
			arguments(new FacilityFilter(1, 20).include(NASAL_SWAB), 1L),
			arguments(new FacilityFilter(1, 20).exclude(ANTIBODY), 1L),

			// Negative tests
			arguments(new FacilityFilter(1, 20).withId(VALUE.id + 1000L), 0L),
			arguments(new FacilityFilter(1, 20).withIdFrom(VALUE.id + 1L), 0L),
			arguments(new FacilityFilter(1, 20).withIdTo(VALUE.id - 1L), 0L),
			arguments(new FacilityFilter(1, 20).withName("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withAddress("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withCity("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withState("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withCountyId("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasCountyId(false), 0L),
			arguments(new FacilityFilter(1, 20).withCountyName("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasCountyName(false), 0L),
			arguments(new FacilityFilter(1, 20).withLatitudeFrom(latUp), 0L),
			arguments(new FacilityFilter(1, 20).withLatitudeTo(latDown), 0L),
			arguments(new FacilityFilter(1, 20).withLongitudeFrom(lngUp), 0L),
			arguments(new FacilityFilter(1, 20).withLongitudeTo(lngDown), 0L),
			arguments(new FacilityFilter(1, 20).withPhone("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasPhone(false), 0L),
			arguments(new FacilityFilter(1, 20).withAppointmentPhone("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasAppointmentPhone(false), 0L),
			arguments(new FacilityFilter(1, 20).withEmail("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasEmail(false), 0L),
			arguments(new FacilityFilter(1, 20).withUrl("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasUrl(false), 0L),
			arguments(new FacilityFilter(1, 20).withAppointmentUrl("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasAppointmentUrl(false), 0L),
			arguments(new FacilityFilter(1, 20).withHours("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasHours(false), 0L),
			arguments(new FacilityFilter(1, 20).withTypeId("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasTypeId(false), 0L),
			arguments(new FacilityFilter(1, 20).withDriveThru(!VALUE.driveThru), 0L),
			arguments(new FacilityFilter(1, 20).withAppointmentRequired(!VALUE.appointmentRequired), 0L),
			arguments(new FacilityFilter(1, 20).withHasAppointmentRequired(false), 0L),
			arguments(new FacilityFilter(1, 20).withAcceptsThirdParty(!VALUE.acceptsThirdParty), 0L),
			arguments(new FacilityFilter(1, 20).withHasAcceptsThirdParty(false), 0L),
			arguments(new FacilityFilter(1, 20).withReferralRequired(!VALUE.referralRequired), 0L),
			arguments(new FacilityFilter(1, 20).withTestCriteriaId(CDC_CRITERIA.id), 0L),
			arguments(new FacilityFilter(1, 20).withNotTestCriteriaId(OTHER.id), 0L),
			arguments(new FacilityFilter(1, 20).withHasTestCriteriaId(false), 0L),
			arguments(new FacilityFilter(1, 20).withOtherTestCriteria("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasOtherTestCriteria(false), 0L),
			arguments(new FacilityFilter(1, 20).withTestsPerDay(VALUE.testsPerDay + 1000), 0L),
			arguments(new FacilityFilter(1, 20).withHasTestsPerDay(false), 0L),
			arguments(new FacilityFilter(1, 20).withTestsPerDayFrom(VALUE.testsPerDay + 1), 0L),
			arguments(new FacilityFilter(1, 20).withTestsPerDayTo(VALUE.testsPerDay - 1), 0L),
			arguments(new FacilityFilter(1, 20).withGovernmentIdRequired(!VALUE.governmentIdRequired), 0L),
			arguments(new FacilityFilter(1, 20).withMinimumAge(VALUE.minimumAge + 1000), 0L),
			arguments(new FacilityFilter(1, 20).withHasMinimumAge(false), 0L),
			arguments(new FacilityFilter(1, 20).withMinimumAgeFrom(VALUE.minimumAge + 1), 0L),
			arguments(new FacilityFilter(1, 20).withMinimumAgeTo(VALUE.minimumAge - 1), 0L),
			arguments(new FacilityFilter(1, 20).withDoctorReferralCriteria("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasDoctorReferralCriteria(false), 0L),
			arguments(new FacilityFilter(1, 20).withFirstResponderFriendly(!VALUE.firstResponderFriendly), 0L),
			arguments(new FacilityFilter(1, 20).withTelescreeningAvailable(!VALUE.telescreeningAvailable), 0L),
			arguments(new FacilityFilter(1, 20).withAcceptsInsurance(!VALUE.acceptsInsurance), 0L),
			arguments(new FacilityFilter(1, 20).withInsuranceProvidersAccepted("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasInsuranceProvidersAccepted(false), 0L),
			arguments(new FacilityFilter(1, 20).withFreeOrLowCost(!VALUE.freeOrLowCost), 0L),
			arguments(new FacilityFilter(1, 20).withCanDonatePlasma(!VALUE.canDonatePlasma), 0L),
			arguments(new FacilityFilter(1, 20).withResultNotificationEnabled(!VALUE.resultNotificationEnabled), 0L),
			arguments(new FacilityFilter(1, 20).withNotes("invalid"), 0L),
			arguments(new FacilityFilter(1, 20).withHasNotes(false), 0L),
			arguments(new FacilityFilter(1, 20).withActive(!VALUE.active), 0L),
			arguments(new FacilityFilter(1, 20).withHasActivatedAt(false), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withActivatedAtFrom(hourAhead).withActivatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withCreatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAhead), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L),
			arguments(new FacilityFilter(1, 20).exclude(NASAL_SWAB), 0L),
			arguments(new FacilityFilter(1, 20).include(ANTIBODY), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final FacilityFilter filter, final long expectedTotal)
	{
		var results = dao.search(filter, true);
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
			results.records.forEach(v -> Assertions.assertNull(v.restricted, "Check restricted: " + v.name));
			results.records.forEach(v -> assertThat(v.testTypes).as("Check testTypes").containsExactly(NASAL_SWAB.created()));
		}
	}

	@Test
	public void search_from()
	{
		// Function "ST_DISTANCE_SPHERE" not found ON H2 database.
		assertThrows(PersistenceException.class, () -> dao.search(new FacilityFilter().withFrom(new GeoFilter(bg("45.7"), bg("-35.42"), null, 50, null)), true));
	}

	@Test
	public void search_from_invalid()
	{
		Assertions.assertEquals(1L, dao.search(new FacilityFilter().withFrom(new GeoFilter(null, null, "Philadelphia", 50, null)), true).total);
	}

	public static Stream<Arguments> search_sort()
	{
		return Stream.of(
			arguments(new FacilityFilter(null, null), "id", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter(null, "ASC"), "id", "DESC"),
			arguments(new FacilityFilter(null, "invalid"), "id", "DESC"),
			arguments(new FacilityFilter("invalid", "invalid"), "id", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("invalid", null), "id", "DESC"),
			arguments(new FacilityFilter("invalid", "desc"), "id", "DESC"),

			arguments(new FacilityFilter("id", null), "id", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("id", "ASC"), "id", "ASC"),
			arguments(new FacilityFilter("id", "asc"), "id", "ASC"),
			arguments(new FacilityFilter("id", "invalid"), "id", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("id", "DESC"), "id", "DESC"),
			arguments(new FacilityFilter("id", "desc"), "id", "DESC"),

			arguments(new FacilityFilter("name", null), "name", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("name", "ASC"), "name", "ASC"),
			arguments(new FacilityFilter("name", "asc"), "name", "ASC"),
			arguments(new FacilityFilter("name", "invalid"), "name", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("name", "DESC"), "name", "DESC"),
			arguments(new FacilityFilter("name", "desc"), "name", "DESC"),

			arguments(new FacilityFilter("address", null), "address", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("address", "ASC"), "address", "ASC"),
			arguments(new FacilityFilter("address", "asc"), "address", "ASC"),
			arguments(new FacilityFilter("address", "invalid"), "address", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("address", "DESC"), "address", "DESC"),
			arguments(new FacilityFilter("address", "desc"), "address", "DESC"),

			arguments(new FacilityFilter("city", null), "city", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("city", "ASC"), "city", "ASC"),
			arguments(new FacilityFilter("city", "asc"), "city", "ASC"),
			arguments(new FacilityFilter("city", "invalid"), "city", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("city", "DESC"), "city", "DESC"),
			arguments(new FacilityFilter("city", "desc"), "city", "DESC"),

			arguments(new FacilityFilter("state", null), "state", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("state", "ASC"), "state", "ASC"),
			arguments(new FacilityFilter("state", "asc"), "state", "ASC"),
			arguments(new FacilityFilter("state", "invalid"), "state", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("state", "DESC"), "state", "DESC"),
			arguments(new FacilityFilter("state", "desc"), "state", "DESC"),

			arguments(new FacilityFilter("countyId", null), "countyId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("countyId", "ASC"), "countyId", "ASC"),
			arguments(new FacilityFilter("countyId", "asc"), "countyId", "ASC"),
			arguments(new FacilityFilter("countyId", "invalid"), "countyId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("countyId", "DESC"), "countyId", "DESC"),
			arguments(new FacilityFilter("countyId", "desc"), "countyId", "DESC"),

			arguments(new FacilityFilter("countyName", null), "countyName", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("countyName", "ASC"), "countyName", "ASC"),
			arguments(new FacilityFilter("countyName", "asc"), "countyName", "ASC"),
			arguments(new FacilityFilter("countyName", "invalid"), "countyName", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("countyName", "DESC"), "countyName", "DESC"),
			arguments(new FacilityFilter("countyName", "desc"), "countyName", "DESC"),

			arguments(new FacilityFilter("latitude", null), "latitude", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("latitude", "ASC"), "latitude", "ASC"),
			arguments(new FacilityFilter("latitude", "asc"), "latitude", "ASC"),
			arguments(new FacilityFilter("latitude", "invalid"), "latitude", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("latitude", "DESC"), "latitude", "DESC"),
			arguments(new FacilityFilter("latitude", "desc"), "latitude", "DESC"),

			arguments(new FacilityFilter("longitude", null), "longitude", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("longitude", "ASC"), "longitude", "ASC"),
			arguments(new FacilityFilter("longitude", "asc"), "longitude", "ASC"),
			arguments(new FacilityFilter("longitude", "invalid"), "longitude", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("longitude", "DESC"), "longitude", "DESC"),
			arguments(new FacilityFilter("longitude", "desc"), "longitude", "DESC"),

			arguments(new FacilityFilter("phone", null), "phone", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("phone", "ASC"), "phone", "ASC"),
			arguments(new FacilityFilter("phone", "asc"), "phone", "ASC"),
			arguments(new FacilityFilter("phone", "invalid"), "phone", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("phone", "DESC"), "phone", "DESC"),
			arguments(new FacilityFilter("phone", "desc"), "phone", "DESC"),

			arguments(new FacilityFilter("appointmentPhone", null), "appointmentPhone", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("appointmentPhone", "ASC"), "appointmentPhone", "ASC"),
			arguments(new FacilityFilter("appointmentPhone", "asc"), "appointmentPhone", "ASC"),
			arguments(new FacilityFilter("appointmentPhone", "invalid"), "appointmentPhone", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("appointmentPhone", "DESC"), "appointmentPhone", "DESC"),
			arguments(new FacilityFilter("appointmentPhone", "desc"), "appointmentPhone", "DESC"),

			arguments(new FacilityFilter("email", null), "email", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("email", "ASC"), "email", "ASC"),
			arguments(new FacilityFilter("email", "asc"), "email", "ASC"),
			arguments(new FacilityFilter("email", "invalid"), "email", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("email", "DESC"), "email", "DESC"),
			arguments(new FacilityFilter("email", "desc"), "email", "DESC"),

			arguments(new FacilityFilter("url", null), "url", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("url", "ASC"), "url", "ASC"),
			arguments(new FacilityFilter("url", "asc"), "url", "ASC"),
			arguments(new FacilityFilter("url", "invalid"), "url", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("url", "DESC"), "url", "DESC"),
			arguments(new FacilityFilter("url", "desc"), "url", "DESC"),

			arguments(new FacilityFilter("appointmentUrl", null), "appointmentUrl", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("appointmentUrl", "ASC"), "appointmentUrl", "ASC"),
			arguments(new FacilityFilter("appointmentUrl", "asc"), "appointmentUrl", "ASC"),
			arguments(new FacilityFilter("appointmentUrl", "invalid"), "appointmentUrl", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("appointmentUrl", "DESC"), "appointmentUrl", "DESC"),
			arguments(new FacilityFilter("appointmentUrl", "desc"), "appointmentUrl", "DESC"),

			arguments(new FacilityFilter("hours", null), "hours", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("hours", "ASC"), "hours", "ASC"),
			arguments(new FacilityFilter("hours", "asc"), "hours", "ASC"),
			arguments(new FacilityFilter("hours", "invalid"), "hours", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("hours", "DESC"), "hours", "DESC"),
			arguments(new FacilityFilter("hours", "desc"), "hours", "DESC"),

			arguments(new FacilityFilter("typeId", null), "typeId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("typeId", "ASC"), "typeId", "ASC"),
			arguments(new FacilityFilter("typeId", "asc"), "typeId", "ASC"),
			arguments(new FacilityFilter("typeId", "invalid"), "typeId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("typeId", "DESC"), "typeId", "DESC"),
			arguments(new FacilityFilter("typeId", "desc"), "typeId", "DESC"),

			arguments(new FacilityFilter("driveThru", null), "driveThru", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("driveThru", "ASC"), "driveThru", "ASC"),
			arguments(new FacilityFilter("driveThru", "asc"), "driveThru", "ASC"),
			arguments(new FacilityFilter("driveThru", "invalid"), "driveThru", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("driveThru", "DESC"), "driveThru", "DESC"),
			arguments(new FacilityFilter("driveThru", "desc"), "driveThru", "DESC"),

			arguments(new FacilityFilter("appointmentRequired", null), "appointmentRequired", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("appointmentRequired", "ASC"), "appointmentRequired", "ASC"),
			arguments(new FacilityFilter("appointmentRequired", "asc"), "appointmentRequired", "ASC"),
			arguments(new FacilityFilter("appointmentRequired", "invalid"), "appointmentRequired", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("appointmentRequired", "DESC"), "appointmentRequired", "DESC"),
			arguments(new FacilityFilter("appointmentRequired", "desc"), "appointmentRequired", "DESC"),

			arguments(new FacilityFilter("acceptsThirdParty", null), "acceptsThirdParty", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("acceptsThirdParty", "ASC"), "acceptsThirdParty", "ASC"),
			arguments(new FacilityFilter("acceptsThirdParty", "asc"), "acceptsThirdParty", "ASC"),
			arguments(new FacilityFilter("acceptsThirdParty", "invalid"), "acceptsThirdParty", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("acceptsThirdParty", "DESC"), "acceptsThirdParty", "DESC"),
			arguments(new FacilityFilter("acceptsThirdParty", "desc"), "acceptsThirdParty", "DESC"),

			arguments(new FacilityFilter("referralRequired", null), "referralRequired", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("referralRequired", "ASC"), "referralRequired", "ASC"),
			arguments(new FacilityFilter("referralRequired", "asc"), "referralRequired", "ASC"),
			arguments(new FacilityFilter("referralRequired", "invalid"), "referralRequired", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("referralRequired", "DESC"), "referralRequired", "DESC"),
			arguments(new FacilityFilter("referralRequired", "desc"), "referralRequired", "DESC"),

			arguments(new FacilityFilter("testCriteriaId", null), "testCriteriaId", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("testCriteriaId", "ASC"), "testCriteriaId", "ASC"),
			arguments(new FacilityFilter("testCriteriaId", "asc"), "testCriteriaId", "ASC"),
			arguments(new FacilityFilter("testCriteriaId", "invalid"), "testCriteriaId", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("testCriteriaId", "DESC"), "testCriteriaId", "DESC"),
			arguments(new FacilityFilter("testCriteriaId", "desc"), "testCriteriaId", "DESC"),

			arguments(new FacilityFilter("otherTestCriteria", null), "otherTestCriteria", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("otherTestCriteria", "ASC"), "otherTestCriteria", "ASC"),
			arguments(new FacilityFilter("otherTestCriteria", "asc"), "otherTestCriteria", "ASC"),
			arguments(new FacilityFilter("otherTestCriteria", "invalid"), "otherTestCriteria", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("otherTestCriteria", "DESC"), "otherTestCriteria", "DESC"),
			arguments(new FacilityFilter("otherTestCriteria", "desc"), "otherTestCriteria", "DESC"),

			arguments(new FacilityFilter("testsPerDay", null), "testsPerDay", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("testsPerDay", "ASC"), "testsPerDay", "ASC"),
			arguments(new FacilityFilter("testsPerDay", "asc"), "testsPerDay", "ASC"),
			arguments(new FacilityFilter("testsPerDay", "invalid"), "testsPerDay", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("testsPerDay", "DESC"), "testsPerDay", "DESC"),
			arguments(new FacilityFilter("testsPerDay", "desc"), "testsPerDay", "DESC"),

			arguments(new FacilityFilter("governmentIdRequired", null), "governmentIdRequired", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("governmentIdRequired", "ASC"), "governmentIdRequired", "ASC"),
			arguments(new FacilityFilter("governmentIdRequired", "asc"), "governmentIdRequired", "ASC"),
			arguments(new FacilityFilter("governmentIdRequired", "invalid"), "governmentIdRequired", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("governmentIdRequired", "DESC"), "governmentIdRequired", "DESC"),
			arguments(new FacilityFilter("governmentIdRequired", "desc"), "governmentIdRequired", "DESC"),

			arguments(new FacilityFilter("minimumAge", null), "minimumAge", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("minimumAge", "ASC"), "minimumAge", "ASC"),
			arguments(new FacilityFilter("minimumAge", "asc"), "minimumAge", "ASC"),
			arguments(new FacilityFilter("minimumAge", "invalid"), "minimumAge", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("minimumAge", "DESC"), "minimumAge", "DESC"),
			arguments(new FacilityFilter("minimumAge", "desc"), "minimumAge", "DESC"),

			arguments(new FacilityFilter("doctorReferralCriteria", null), "doctorReferralCriteria", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("doctorReferralCriteria", "ASC"), "doctorReferralCriteria", "ASC"),
			arguments(new FacilityFilter("doctorReferralCriteria", "asc"), "doctorReferralCriteria", "ASC"),
			arguments(new FacilityFilter("doctorReferralCriteria", "invalid"), "doctorReferralCriteria", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("doctorReferralCriteria", "DESC"), "doctorReferralCriteria", "DESC"),
			arguments(new FacilityFilter("doctorReferralCriteria", "desc"), "doctorReferralCriteria", "DESC"),

			arguments(new FacilityFilter("firstResponderFriendly", null), "firstResponderFriendly", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("firstResponderFriendly", "ASC"), "firstResponderFriendly", "ASC"),
			arguments(new FacilityFilter("firstResponderFriendly", "asc"), "firstResponderFriendly", "ASC"),
			arguments(new FacilityFilter("firstResponderFriendly", "invalid"), "firstResponderFriendly", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("firstResponderFriendly", "DESC"), "firstResponderFriendly", "DESC"),
			arguments(new FacilityFilter("firstResponderFriendly", "desc"), "firstResponderFriendly", "DESC"),

			arguments(new FacilityFilter("telescreeningAvailable", null), "telescreeningAvailable", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("telescreeningAvailable", "ASC"), "telescreeningAvailable", "ASC"),
			arguments(new FacilityFilter("telescreeningAvailable", "asc"), "telescreeningAvailable", "ASC"),
			arguments(new FacilityFilter("telescreeningAvailable", "invalid"), "telescreeningAvailable", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("telescreeningAvailable", "DESC"), "telescreeningAvailable", "DESC"),
			arguments(new FacilityFilter("telescreeningAvailable", "desc"), "telescreeningAvailable", "DESC"),

			arguments(new FacilityFilter("acceptsInsurance", null), "acceptsInsurance", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("acceptsInsurance", "ASC"), "acceptsInsurance", "ASC"),
			arguments(new FacilityFilter("acceptsInsurance", "asc"), "acceptsInsurance", "ASC"),
			arguments(new FacilityFilter("acceptsInsurance", "invalid"), "acceptsInsurance", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("acceptsInsurance", "DESC"), "acceptsInsurance", "DESC"),
			arguments(new FacilityFilter("acceptsInsurance", "desc"), "acceptsInsurance", "DESC"),

			arguments(new FacilityFilter("insuranceProvidersAccepted", null), "insuranceProvidersAccepted", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("insuranceProvidersAccepted", "ASC"), "insuranceProvidersAccepted", "ASC"),
			arguments(new FacilityFilter("insuranceProvidersAccepted", "asc"), "insuranceProvidersAccepted", "ASC"),
			arguments(new FacilityFilter("insuranceProvidersAccepted", "invalid"), "insuranceProvidersAccepted", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("insuranceProvidersAccepted", "DESC"), "insuranceProvidersAccepted", "DESC"),
			arguments(new FacilityFilter("insuranceProvidersAccepted", "desc"), "insuranceProvidersAccepted", "DESC"),

			arguments(new FacilityFilter("freeOrLowCost", null), "freeOrLowCost", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("freeOrLowCost", "ASC"), "freeOrLowCost", "ASC"),
			arguments(new FacilityFilter("freeOrLowCost", "asc"), "freeOrLowCost", "ASC"),
			arguments(new FacilityFilter("freeOrLowCost", "invalid"), "freeOrLowCost", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("freeOrLowCost", "DESC"), "freeOrLowCost", "DESC"),
			arguments(new FacilityFilter("freeOrLowCost", "desc"), "freeOrLowCost", "DESC"),

			arguments(new FacilityFilter("canDonatePlasma", null), "canDonatePlasma", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("canDonatePlasma", "ASC"), "canDonatePlasma", "ASC"),
			arguments(new FacilityFilter("canDonatePlasma", "asc"), "canDonatePlasma", "ASC"),
			arguments(new FacilityFilter("canDonatePlasma", "invalid"), "canDonatePlasma", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("canDonatePlasma", "DESC"), "canDonatePlasma", "DESC"),
			arguments(new FacilityFilter("canDonatePlasma", "desc"), "canDonatePlasma", "DESC"),

			arguments(new FacilityFilter("resultNotificationEnabled", null), "resultNotificationEnabled", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("resultNotificationEnabled", "ASC"), "resultNotificationEnabled", "ASC"),
			arguments(new FacilityFilter("resultNotificationEnabled", "asc"), "resultNotificationEnabled", "ASC"),
			arguments(new FacilityFilter("resultNotificationEnabled", "invalid"), "resultNotificationEnabled", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("resultNotificationEnabled", "DESC"), "resultNotificationEnabled", "DESC"),
			arguments(new FacilityFilter("resultNotificationEnabled", "desc"), "resultNotificationEnabled", "DESC"),

			arguments(new FacilityFilter("notes", null), "notes", "ASC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("notes", "ASC"), "notes", "ASC"),
			arguments(new FacilityFilter("notes", "asc"), "notes", "ASC"),
			arguments(new FacilityFilter("notes", "invalid"), "notes", "ASC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("notes", "DESC"), "notes", "DESC"),
			arguments(new FacilityFilter("notes", "desc"), "notes", "DESC"),

			arguments(new FacilityFilter("active", null), "active", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("active", "ASC"), "active", "ASC"),
			arguments(new FacilityFilter("active", "asc"), "active", "ASC"),
			arguments(new FacilityFilter("active", "invalid"), "active", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("active", "DESC"), "active", "DESC"),
			arguments(new FacilityFilter("active", "desc"), "active", "DESC"),

			arguments(new FacilityFilter("activatedAt", null), "activatedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("activatedAt", "ASC"), "activatedAt", "ASC"),
			arguments(new FacilityFilter("activatedAt", "asc"), "activatedAt", "ASC"),
			arguments(new FacilityFilter("activatedAt", "invalid"), "activatedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("activatedAt", "DESC"), "activatedAt", "DESC"),
			arguments(new FacilityFilter("activatedAt", "desc"), "activatedAt", "DESC"),

			arguments(new FacilityFilter("createdAt", null), "createdAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("createdAt", "ASC"), "createdAt", "ASC"),
			arguments(new FacilityFilter("createdAt", "asc"), "createdAt", "ASC"),
			arguments(new FacilityFilter("createdAt", "invalid"), "createdAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("createdAt", "DESC"), "createdAt", "DESC"),
			arguments(new FacilityFilter("createdAt", "desc"), "createdAt", "DESC"),

			arguments(new FacilityFilter("updatedAt", null), "updatedAt", "DESC"), // Missing sort direction is converted to the default.
			arguments(new FacilityFilter("updatedAt", "ASC"), "updatedAt", "ASC"),
			arguments(new FacilityFilter("updatedAt", "asc"), "updatedAt", "ASC"),
			arguments(new FacilityFilter("updatedAt", "invalid"), "updatedAt", "DESC"),	// Invalid sort direction is converted to the default.
			arguments(new FacilityFilter("updatedAt", "DESC"), "updatedAt", "DESC"),
			arguments(new FacilityFilter("updatedAt", "desc"), "updatedAt", "DESC")
		);
	}

	@ParameterizedTest
	@MethodSource
	public void search_sort(final FacilityFilter filter, final String expectedSortOn, final String expectedSortDir)
	{
		var results = dao.search(filter, true);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedSortOn, results.sortOn, "Check sortOn");
		Assertions.assertEquals(expectedSortDir, results.sortDir, "Check sortDir");
	}

	/** Test removal after the search. */
	@Test
	public void testRemove()
	{
		Assertions.assertFalse(dao.remove(VALUE.id + 1000L), "Invalid");
		Assertions.assertTrue(dao.remove(VALUE.id), "Removed");
		Assertions.assertFalse(dao.remove(VALUE.id), "Already removed");

		auditorRemoves++;
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
		count(new FacilityFilter().withId(VALUE.id), 0L);
		count(new FacilityFilter().withName(VALUE.name), 0L);
		count(new FacilityFilter().withActive(VALUE.active), 0L);
		count(new FacilityFilter().include(NASAL_SWAB), 0L);
	}

	@Test
	public void z_00_add()
	{
		VALUE = dao.add(createValid().withActive(true).withTestTypes(NASAL_SWAB, ANTIBODY), true);
		Assertions.assertNotNull(VALUE, "Exists");
		Assertions.assertEquals(2L, VALUE.id, "Check ID");
		Assertions.assertTrue(VALUE.active, "Check active");
		assertThat(VALUE.activatedAt).as("Check activatedAt").isNotNull().isEqualTo(VALUE.createdAt);

		auditorAdds++;
	}

	@Test
	public void z_00_add_check()
	{
		count(new FacilityFilter(), 1L);

		var v = dao.getById(2L, true);
		Assertions.assertNotNull(dao.getById(2L, true), "Exists");
		Assertions.assertTrue(v.active, "Check active");
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isEqualTo(v.createdAt);
		assertThat(v.testTypes).as("Check testTypes").containsExactly(ANTIBODY.created(), NASAL_SWAB.created());

		v = dao.search(new FacilityFilter(), true).records.get(0);
		assertThat(v.testTypes).as("Check testTypes").containsExactly(ANTIBODY.created(), NASAL_SWAB.created());
	}

	@Test
	public void z_00_modify()
	{
		VALUE = dao.add(createValid().withActive(true).withId(20L).nullTestTypes(), true);
		Assertions.assertNotNull(VALUE, "Exists");
		Assertions.assertEquals(2L, VALUE.id, "Check ID");
		Assertions.assertTrue(VALUE.active, "Check active");
		assertThat(VALUE.activatedAt).as("Check activatedAt").isNotNull().isEqualTo(VALUE.createdAt).isBefore(VALUE.updatedAt);

		auditorUpdates++;
	}

	@Test
	public void z_00_modify_check()
	{
		z_00_add_check();	// No change
	}

	@Test
	public void z_01_add_others()
	{
		dao.add(createValid().withName("restrictive-0").withCity("Dallas").withState("Texas").withTestCriteriaId(null).withActive(true), true);
		dao.add(createValid().withName("restrictive-1").withCity("Austin").withState("Texas").withTestCriteriaId(CDC_CRITERIA.id).withActive(true), true);

		peopleDao.addFacilities(PERSON.id, List.of(3L));
		peopleDao.addFacilities(PERSON_1.id, List.of(2L, 4L));

		auditorAdds+= 2;
	}

	@Test
	public void z_01_add_others_count()
	{
		count(new FacilityFilter(), 3L);
		count(new FacilityFilter().withHasTestCriteriaId(true), 2L);
		count(new FacilityFilter().withHasTestCriteriaId(false), 1L);
		count(new FacilityFilter().withTestCriteriaId(CDC_CRITERIA.id), 1L);
		count(new FacilityFilter().withTestCriteriaId(OTHER.id), 1L);
		count(new FacilityFilter().withNotTestCriteriaId(CDC_CRITERIA.id), 2L);
		count(new FacilityFilter().withNotTestCriteriaId(OTHER.id), 2L);
	}

	public static Stream<Arguments> z_01_add_others_getIdsByPerson()
	{
		return Stream.of(
			arguments(PERSON, new Long[] { 3L }),
			arguments(PERSON_1, new Long[] { 2L, 4L }),
			arguments(null, new Long[0]));
	}

	@ParameterizedTest
	@MethodSource
	public void z_01_add_others_getIdsByPerson(final PeopleValue person, final Long[] ids)
	{
		assertThat(dao.getIdsByPerson(person)).isEqualTo(List.of(ids));
	}

	@Test
	public void z_01_add_others_search()
	{
		var ids = dao.search(new FacilityFilter().withNotTestCriteriaId(CDC_CRITERIA.id), true).records.stream().map(v -> v.testCriteriaId).collect(toList());
		assertThat(ids).hasSize(2).containsOnly(OTHER.id, null);

		ids = dao.search(new FacilityFilter().withNotTestCriteriaId(OTHER.id), true).records.stream().map(v -> v.testCriteriaId).collect(toList());
		assertThat(ids).hasSize(2).containsOnly(CDC_CRITERIA.id, null);
	}

	public static Stream<Arguments> z_02_getDistinctCitiesByState()
	{
		return Stream.of(
			arguments("Florida", List.of()),
			arguments("FL", List.of()),
			arguments("Texas", List.of(new CountByName("Austin", 1L), new CountByName("Dallas", 1L))),
			arguments("Georgia", List.of()),
			arguments("GA", List.of(new CountByName("Atlanta", 1L))));
	}

	@ParameterizedTest
	@MethodSource
	public void z_02_getDistinctCitiesByState(final String state, final List<CountByName> expected)
	{
		assertThat(dao.getDistinctCitiesByState(state)).isEqualTo(expected);
	}

	@Test
	public void z_02_getDistinctStates()
	{
		assertThat(dao.getDistinctStates()).containsExactly(new CountByName("GA", 1L), new CountByName("Texas", 2L));
	}

	@Test
	public void z_10_add_as_editor()
	{
		VALUE = dao.add(createValid().withName("byEditor").withResultNotificationEnabled(true).withActive(true), false);
		Assertions.assertEquals(5L, VALUE.id, "Check ID");
		Assertions.assertFalse(VALUE.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertFalse(VALUE.active, "Check active");
		Assertions.assertNull(VALUE.activatedAt, "Check activatedAt");

		auditorAdds++;
	}

	@Test
	public void z_10_add_as_editor_check()
	{
		var v = dao.getById(VALUE.id, true);
		Assertions.assertEquals("byEditor", v.name, "Check name");
		Assertions.assertFalse(VALUE.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertFalse(v.active, "Check active");
		Assertions.assertNull(v.activatedAt, "Check activatedAt");
		Assertions.assertNull(v.testTypes, "Check testTypes");

		v = dao.search(new FacilityFilter().withId(5L), true).records.get(0);
		Assertions.assertNull(v.testTypes, "Check testTypes");
	}

	@Test
	public void z_10_modify_as_editor()
	{
		var v = dao.update(VALUE.withResultNotificationEnabled(true).withActive(true).withTestTypes(NASAL_SWAB), false);
		Assertions.assertEquals(VALUE.id, v.id, "Check ID");
		Assertions.assertFalse(v.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertFalse(VALUE.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertFalse(v.active, "Check active");
		Assertions.assertFalse(VALUE.active, "Check active");
		Assertions.assertNull(v.activatedAt, "Check activatedAt");
		assertThat(v.testTypes).as("Check testTypes").containsExactly(NASAL_SWAB.created());

		auditorUpdates++;
	}

	@Test
	public void z_10_modify_as_editor_check()
	{
		var v = dao.getById(VALUE.id, true);
		Assertions.assertEquals("byEditor", v.name, "Check name");
		Assertions.assertFalse(v.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertFalse(v.active, "Check active");
		Assertions.assertNull(v.activatedAt, "Check activatedAt");
		assertThat(v.testTypes).as("Check testTypes").containsExactly(NASAL_SWAB.created());

		v = dao.search(new FacilityFilter().withId(5L), true).records.get(0);
		assertThat(v.testTypes).as("Check testTypes").containsExactly(NASAL_SWAB.created());
	}

	@Test
	public void z_11_modify_as_admin()
	{
		var v = dao.update(VALUE.withResultNotificationEnabled(true).withActive(true).withTestTypes(DONT_KNOW), true);
		Assertions.assertEquals(VALUE.id, v.id, "Check ID");
		Assertions.assertTrue(v.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertTrue(VALUE.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertTrue(v.active, "Check active");
		Assertions.assertTrue(VALUE.active, "Check active");
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isAfter(v.createdAt).isEqualTo(v.updatedAt).isCloseTo(new Date(), 500L);
		assertThat(v.testTypes).as("Check testTypes").containsExactly(DONT_KNOW.created());

		auditorUpdates++;
	}

	@Test
	public void z_11_modify_as_admin_check()
	{
		var v = dao.getById(VALUE.id, true);
		Assertions.assertEquals("byEditor", v.name, "Check name");
		Assertions.assertTrue(v.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertTrue(v.active, "Check active");
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isAfter(v.createdAt).isEqualTo(v.updatedAt).isCloseTo(new Date(), 1000L);
		assertThat(v.testTypes).as("Check testTypes").containsExactly(DONT_KNOW.created());

		v = dao.search(new FacilityFilter().withId(5L), true).records.get(0);
		assertThat(v.testTypes).as("Check testTypes").containsExactly(DONT_KNOW.created());
	}

	@Test
	public void z_12_modify_as_editor()
	{
		var v = dao.update(VALUE.withResultNotificationEnabled(false).withActive(false).emptyTestTypes(), false);
		Assertions.assertEquals(VALUE.id, v.id, "Check ID");
		Assertions.assertTrue(v.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertTrue(VALUE.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertTrue(v.active, "Check active");
		Assertions.assertTrue(VALUE.active, "Check active");
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isAfter(v.createdAt).isBefore(v.updatedAt).isCloseTo(new Date(), 500L);
		assertThat(v.testTypes).as("Check testTypes").isEmpty();

		auditorUpdates++;
	}

	@Test
	public void z_12_modify_as_editor_check()
	{
		var v = dao.getById(VALUE.id, true);
		Assertions.assertEquals("byEditor", v.name, "Check name");
		Assertions.assertTrue(v.resultNotificationEnabled, "Check resultNotificationEnabled");
		Assertions.assertTrue(v.active, "Check active");
		assertThat(v.activatedAt).as("Check activatedAt").isNotNull().isAfter(v.createdAt).isBefore(v.updatedAt).isCloseTo(new Date(), 1000L);
		Assertions.assertNull(v.testTypes, "Check testTypes");

		v = dao.search(new FacilityFilter().withId(5L), true).records.get(0);
		Assertions.assertNull(v.testTypes, "Check testTypes");
	}

	/** Helper method - calls the DAO count call and compares the expected total value.
	 *
	 * @param filter
	 * @param expectedTotal
	 */
	private void count(final FacilityFilter filter, final long expectedTotal)
	{
		Assertions.assertEquals(expectedTotal, dao.count(filter, true), "COUNT " + filter + ": Check total");
	}

	/** Helper method - checks an expected value against a supplied entity record. */
	private void check(final FacilityValue expected, final Facility record)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, record.getId(), assertId + "Check id");
		Assertions.assertEquals(expected.name, record.getName(), assertId + "Check name");
		Assertions.assertEquals(expected.address, record.getAddress(), assertId + "Check address");
		Assertions.assertEquals(expected.city, record.getCity(), assertId + "Check city");
		Assertions.assertEquals(expected.state, record.getState(), assertId + "Check state");
		Assertions.assertEquals(expected.countyId, record.getCountyId(), assertId + "Check countyId");
		Assertions.assertEquals(expected.countyName, record.getCountyName(), assertId + "Check countyName");
		assertThat(record.getLatitude()).as(assertId + "Check latitude").isEqualByComparingTo(expected.latitude);
		assertThat(record.getLongitude()).as(assertId + "Check longitude").isEqualByComparingTo(expected.longitude);
		Assertions.assertEquals(expected.phone, record.getPhone(), assertId + "Check phone");
		Assertions.assertEquals(expected.appointmentPhone, record.getAppointmentPhone(), assertId + "Check appointmentPhone");
		Assertions.assertEquals(expected.email, record.getEmail(), assertId + "Check email");
		Assertions.assertEquals(expected.url, record.getUrl(), assertId + "Check url");
		Assertions.assertEquals(expected.appointmentUrl, record.getAppointmentUrl(), assertId + "Check appointmentUrl");
		Assertions.assertEquals(expected.hours, record.getHours(), assertId + "Check hours");
		Assertions.assertEquals(expected.typeId, record.getTypeId(), assertId + "Check typeId");
		Assertions.assertEquals(expected.driveThru, record.isDriveThru(), assertId + "Check driveThru");
		Assertions.assertEquals(expected.appointmentRequired, record.isAppointmentRequired(), assertId + "Check appointmentRequired");
		Assertions.assertEquals(expected.acceptsThirdParty, record.isAcceptsThirdParty(), assertId + "Check acceptsThirdParty");
		Assertions.assertEquals(expected.referralRequired, record.isReferralRequired(), assertId + "Check referralRequired");
		Assertions.assertEquals(expected.testCriteriaId, record.getTestCriteriaId(), assertId + "Check testCriteriaId");
		Assertions.assertEquals(expected.otherTestCriteria, record.getOtherTestCriteria(), assertId + "Check otherTestCriteria");
		Assertions.assertEquals(expected.testsPerDay, record.getTestsPerDay(), assertId + "Check testsPerDay");
		Assertions.assertEquals(expected.governmentIdRequired, record.isGovernmentIdRequired(), assertId + "Check governmentIdRequired");
		Assertions.assertEquals(expected.minimumAge, record.getMinimumAge(), assertId + "Check minimumAge");
		Assertions.assertEquals(expected.doctorReferralCriteria, record.getDoctorReferralCriteria(), assertId + "Check doctorReferralCriteria");
		Assertions.assertEquals(expected.firstResponderFriendly, record.isFirstResponderFriendly(), assertId + "Check firstResponderFriendly");
		Assertions.assertEquals(expected.telescreeningAvailable, record.isTelescreeningAvailable(), assertId + "Check telescreeningAvailable");
		Assertions.assertEquals(expected.acceptsInsurance, record.isAcceptsInsurance(), assertId + "Check acceptsInsurance");
		Assertions.assertEquals(expected.insuranceProvidersAccepted, record.getInsuranceProvidersAccepted(), assertId + "Check insuranceProvidersAccepted");
		Assertions.assertEquals(expected.freeOrLowCost, record.isFreeOrLowCost(), assertId + "Check freeOrLowCost");
		Assertions.assertEquals(expected.canDonatePlasma, record.isCanDonatePlasma(), assertId + "Check canDonatePlasma");
		Assertions.assertEquals(expected.resultNotificationEnabled, record.isResultNotificationEnabled(), assertId + "Check resultNotificationEnabled");
		Assertions.assertEquals(expected.notes, record.getNotes(), assertId + "Check notes");
		Assertions.assertEquals(expected.active, record.isActive(), assertId + "Check active");
		Assertions.assertEquals(expected.activatedAt, record.getActivatedAt(), assertId + "Check activatedAt");
		Assertions.assertEquals(expected.createdAt, record.getCreatedAt(), assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, record.getUpdatedAt(), assertId + "Check updatedAt");
	}

	/** Helper method - checks an expected value against a supplied value object. */
	private void check(final FacilityValue expected, final FacilityValue value)
	{
		var assertId = "ID (" + expected.id + "): ";
		Assertions.assertEquals(expected.id, value.id, assertId + "Check id");
		Assertions.assertEquals(expected.name, value.name, assertId + "Check name");
		Assertions.assertEquals(expected.address, value.address, assertId + "Check address");
		Assertions.assertEquals(expected.city, value.city, assertId + "Check city");
		Assertions.assertEquals(expected.state, value.state, assertId + "Check state");
		Assertions.assertEquals(expected.countyId, value.countyId, assertId + "Check countyId");
		Assertions.assertEquals(expected.countyName, value.countyName, assertId + "Check countyName");
		assertThat(value.latitude).as(assertId + "Check latitude").isEqualByComparingTo(expected.latitude);
		assertThat(value.longitude).as(assertId + "Check longitude").isEqualByComparingTo(expected.longitude);
		Assertions.assertEquals(expected.phone, value.phone, assertId + "Check phone");
		Assertions.assertEquals(expected.appointmentPhone, value.appointmentPhone, assertId + "Check appointmentPhone");
		Assertions.assertEquals(expected.email, value.email, assertId + "Check email");
		Assertions.assertEquals(expected.url, value.url, assertId + "Check url");
		Assertions.assertEquals(expected.appointmentUrl, value.appointmentUrl, assertId + "Check appointmentUrl");
		Assertions.assertEquals(expected.hours, value.hours, assertId + "Check hours");
		Assertions.assertEquals(expected.typeId, value.typeId, assertId + "Check typeId");
		Assertions.assertEquals(expected.type, value.type, assertId + "Check type");
		Assertions.assertEquals(expected.driveThru, value.driveThru, assertId + "Check driveThru");
		Assertions.assertEquals(expected.appointmentRequired, value.appointmentRequired, assertId + "Check appointmentRequired");
		Assertions.assertEquals(expected.acceptsThirdParty, value.acceptsThirdParty, assertId + "Check acceptsThirdParty");
		Assertions.assertEquals(expected.referralRequired, value.referralRequired, assertId + "Check referralRequired");
		Assertions.assertEquals(expected.testCriteriaId, value.testCriteriaId, assertId + "Check testCriteriaId");
		Assertions.assertEquals(expected.testCriteria, value.testCriteria, assertId + "Check testCriteria");
		Assertions.assertEquals(expected.otherTestCriteria, value.otherTestCriteria, assertId + "Check otherTestCriteria");
		Assertions.assertEquals(expected.testsPerDay, value.testsPerDay, assertId + "Check testsPerDay");
		Assertions.assertEquals(expected.governmentIdRequired, value.governmentIdRequired, assertId + "Check governmentIdRequired");
		Assertions.assertEquals(expected.minimumAge, value.minimumAge, assertId + "Check minimumAge");
		Assertions.assertEquals(expected.doctorReferralCriteria, value.doctorReferralCriteria, assertId + "Check doctorReferralCriteria");
		Assertions.assertEquals(expected.firstResponderFriendly, value.firstResponderFriendly, assertId + "Check firstResponderFriendly");
		Assertions.assertEquals(expected.telescreeningAvailable, value.telescreeningAvailable, assertId + "Check telescreeningAvailable");
		Assertions.assertEquals(expected.acceptsInsurance, value.acceptsInsurance, assertId + "Check acceptsInsurance");
		Assertions.assertEquals(expected.insuranceProvidersAccepted, value.insuranceProvidersAccepted, assertId + "Check insuranceProvidersAccepted");
		Assertions.assertEquals(expected.freeOrLowCost, value.freeOrLowCost, assertId + "Check freeOrLowCost");
		Assertions.assertEquals(expected.canDonatePlasma, value.canDonatePlasma, assertId + "Check canDonatePlasma");
		Assertions.assertEquals(expected.resultNotificationEnabled, value.resultNotificationEnabled, assertId + "Check resultNotificationEnabled");
		Assertions.assertEquals(expected.notes, value.notes, assertId + "Check notes");
		Assertions.assertEquals(expected.active, value.active, assertId + "Check active");
		Assertions.assertEquals(expected.activatedAt, value.activatedAt, assertId + "Check activatedAt");
		Assertions.assertEquals(expected.createdAt, value.createdAt, assertId + "Check createdAt");
		Assertions.assertEquals(expected.updatedAt, value.updatedAt, assertId + "Check updatedAt");
		Assertions.assertEquals(expected.testTypes, value.testTypes, assertId + "Check testTypes");
	}
}
