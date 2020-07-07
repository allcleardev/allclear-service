package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static app.allclear.common.dao.OrderByBuilder.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.*;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.hibernate.NativeQueryBuilder;
import app.allclear.common.time.StopWatch;
import app.allclear.common.value.CreatedValue;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.type.*;
import app.allclear.platform.value.FacilityValue;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Data access object that handles access to the Facility entity.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
**********************************************************************************/

public class FacilityDAO extends AbstractDAO<Facility>
{
	private static final Logger log = LoggerFactory.getLogger(FacilityDAO.class);

	private static final String SELECT = "SELECT OBJECT(o) FROM Facility o";
	private static final String COUNT = "SELECT COUNT(o.id) FROM Facility o";
	private static final String COUNT_ = "SELECT COUNT(o.id) FROM facility o";
	private static final String SELECT_ = "SELECT o.id, o.name, o.address, o.city, o.state, o.latitude, o.longitude, o.phone, o.appointment_phone, o.email, o.url, o.appointment_url, o.hours, o.type_id, o.drive_thru, o.appointment_required, o.accepts_third_party, o.referral_required, o.test_criteria_id, o.other_test_criteria, o.tests_per_day, o.government_id_required, o.minimum_age, o.doctor_referral_criteria, o.first_responder_friendly, o.telescreening_available, o.accepts_insurance, o.insurance_providers_accepted, o.free_or_low_cost, o.can_donate_plasma, o.result_notification_enabled, o.notes, o.active, o.activated_at, o.created_at, o.updated_at, ST_DISTANCE_SPHERE(POINT(o.longitude, o.latitude), POINT(:fromLongitude, :fromLatitude)) AS meters FROM facility o";
	private static final OrderByBuilder ORDER = new OrderByBuilder('o', 
		"id", DESC,
		"name", ASC,
		"address", ASC,
		"city", ASC,
		"state", ASC,
		"latitude", DESC,
		"longitude", DESC,
		"phone", ASC,
		"appointmentPhone", ASC,
		"email", ASC,
		"url", ASC,
		"appointmentUrl", ASC,
		"hours", ASC,
		"typeId", ASC,
		"driveThru", DESC,
		"appointmentRequired", DESC,
		"acceptsThirdParty", DESC,
		"referralRequired", DESC,
		"testCriteriaId", ASC,
		"otherTestCriteria", ASC,
		"testsPerDay", DESC,
		"governmentIdRequired", DESC,
		"minimumAge", DESC,
		"doctorReferralCriteria", ASC,
		"firstResponderFriendly", DESC,
		"telescreeningAvailable", DESC,
		"acceptsInsurance", DESC,
		"insuranceProvidersAccepted", ASC,
		"freeOrLowCost", DESC,
		"canDonatePlasma", DESC,
		"resultNotificationEnabled", DESC,
		"notes", ASC,
		"active", DESC,
		"activatedAt", DESC,
		"createdAt", DESC,
		"updatedAt", DESC,
		"meters", ASC + ",meters");	// Need to leave off the "o." alias.

	/** Native SQL clauses. */
	public static final String FROM_ALIAS = "o";

	private static final Map<Long, List<CreatedValue>> EMPTY = Map.of();

	private final Auditor auditor;

	public FacilityDAO(final SessionFactory factory, final Auditor auditor)
	{
		super(factory);

		this.auditor = auditor;
	}

	/** Adds a single Facility value.
	 *
	 * @param value
	 * @param admin
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public FacilityValue add(final FacilityValue value, final boolean admin) throws ValidationException
	{
		return update(value, admin);
	}

	/** Updates a single Facility value.
	 *
	 * @param value
	 * @param admin
	 * @throws ValidationException
	 */
	public FacilityValue update(final FacilityValue value, final boolean admin) throws ValidationException
	{
		var validator = new Validator();
		var cmrs = _validate(value, validator);
		var record = (Facility) cmrs[0];
		if ((null == record) && (null != value.id))
			record = findWithException(value.id);

		var s = currentSession();
		if (null != record)
		{
			record.update(value, admin);

			var rec = record;	// Needs to be effectively final to be used in lambdas below.
			update(s, record, record.getTestTypes(), value.testTypes, v -> new FacilityTestType(rec, v), "deleteFacilityTestTypes");
			if (admin)
				update(s, record, record.getPeople(), value.people, v -> new FacilityPeople(rec, person(s, v.id, validator), v), "deleteFacilityPeople");

			auditor.update(value.withId(record.getId()));
		}
		else
		{
			if (!admin)
			{
				value
					.withResultNotificationEnabled(false)	// Only admins can set Facility contractual services. ALLCLEAR-602: DLS on 7/5/2020.
					.withActive(false);	// Editors can only add inactive facilities.
			}
	
			record = persist(new Facility(value));

			var rec = record;	// Needs to be effectively final to be used in lambdas below.
			add(s, value.testTypes, v -> new FacilityTestType(rec, v));
			if (admin)
				add(s, value.people, v -> new FacilityPeople(rec, person(s, v.id, validator), v));

			auditor.add(value.withId(record.getId()));
		}

		return value;
	}

	private People person(final Session s, final String id, final Validator validator) throws ValidationException
	{
		var o = s.get(People.class, id);
		if (null == o) validator.add("people", "The Person ID '%s' is invalid.", id).check();

		return o;
	}

	private void add(final Session s, final List<CreatedValue> values, final Function<CreatedValue, ? extends FacilityChild> toEntity)
	{
		if (CollectionUtils.isEmpty(values)) return;
		values.stream().filter(v -> ((null != v) && (null != v.id))).forEach(v -> s.persist(toEntity.apply(v)));
	}

	private int update(final Session s,
		final Facility record,
		final List<? extends FacilityChild> records,
		final List<CreatedValue> values,
		final Function<CreatedValue, ? extends FacilityChild> toEntity,
		final String deleteQuery)
	{
		if (null == values) return 0;
		if (values.isEmpty()) return namedQueryX(deleteQuery).setParameter("facilityId", record.getId()).executeUpdate();

		return (int) (values.stream()	// Add new values.
			.filter(v -> ((null != v) && (null != v.id)))
			.filter(v -> !records.stream().anyMatch(o -> o.getChildId().equals(v.id)))
			.peek(v -> s.persist(toEntity.apply(v)))
			.count() +
			records.stream()	// Remove missing values.
				.filter(o -> !values.stream().anyMatch(v -> o.getChildId().equals(v.id)))
				.peek(o -> s.delete(o))
				.count());
	}

	/** Validates a single Facility value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final FacilityValue value) throws ValidationException
	{
		_validate(value, new Validator());
	}

	/** Validates a single Facility value and returns any CMR fields.
	 *
	 * @param value
	 * @param validator
	 * @return array of CMRs entities.
	 * @throws ValidationException
	 */
	private Object[] _validate(final FacilityValue value, final Validator validator) throws ValidationException
	{
		value.clean();

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExistsAndLength("name", "Name", value.name, FacilityValue.MAX_LEN_NAME)
			.ensureExistsAndLength("address", "Address", value.address, FacilityValue.MAX_LEN_ADDRESS)
			.ensureExistsAndLength("city", "City", value.city, FacilityValue.MAX_LEN_CITY)
			.ensureExistsAndLength("state", "State", value.state, FacilityValue.MAX_LEN_STATE)
			.ensureExistsAndLatitude("latitude", "Latitude", value.latitude)
			.ensureExistsAndLongitude("longitude", "Longitude", value.longitude)
			.ensureLength("phone", "Phone Number", value.phone, FacilityValue.MAX_LEN_PHONE)
			.ensureLength("appointmentPhone", "Appointment Phone Number", value.appointmentPhone, FacilityValue.MAX_LEN_APPOINTMENT_PHONE)
			.ensureLength("email", "Email Address", value.email, FacilityValue.MAX_LEN_EMAIL)
			.ensureLength("url", "URL", value.url, FacilityValue.MAX_LEN_URL)
			.ensureLength("appointmentUrl", "Appointment URL", value.appointmentUrl, FacilityValue.MAX_LEN_APPOINTMENT_URL)
			.ensureLength("hours", "Hours", value.hours, FacilityValue.MAX_LEN_HOURS)
			.ensureLength("typeId", "Type", value.typeId, FacilityValue.MAX_LEN_TYPE_ID)
			.ensureLength("testCriteriaId", "Testing Criteria", value.testCriteriaId, FacilityValue.MAX_LEN_TEST_CRITERIA_ID)
			.ensureLength("otherTestCriteria", "Other Test Criteria", value.otherTestCriteria, FacilityValue.MAX_LEN_OTHER_TEST_CRITERIA)
			.ensureLength("doctorReferralCriteria", "Doctor Referral Criteria", value.doctorReferralCriteria, FacilityValue.MAX_LEN_DOCTOR_REFERRAL_CRITERIA)
			.ensureLength("insuranceProvidersAccepted", "Insurance Providers Accepted", value.insuranceProvidersAccepted, FacilityValue.MAX_LEN_INSURANCE_PROVIDERS_ACCEPTED)
			.ensureLength("notes", "Notes", value.notes, FacilityValue.MAX_LEN_NOTES)
			.check();

		// Validation foreign keys.
		if ((null != value.typeId) && (null == (value.type = FacilityType.get(value.typeId))))
			validator.add("typeId", "The Type ID '%s' is invalid.", value.typeId);
		if ((null != value.testCriteriaId) && (null == (value.testCriteria = TestCriteria.get(value.testCriteriaId))))
			validator.add("testCriteriaId", "The Test Criteria ID '%s' is invalid.", value.testCriteriaId);

		// Check children.
		if (CollectionUtils.isNotEmpty(value.testTypes))
			value.testTypes.stream().filter(v -> null != v).forEach(v -> validator.ensureExistsAndContains("testTypes", "Test Type", v.clean().id, TestType.VALUES));

		validator.check();

		return new Object[] { find(value.name) };
	}

	/** Removes a single Facility value.
	 *
	 * @param id
	 * @return TRUE if the entity is found and removed.
	 * @throws ValidationException
	 */
	public boolean remove(final Long id) throws ValidationException
	{
		var record = get(id);
		if (null == record) return false;

		var value = cmr(record.toValueX(), true);
		currentSession().delete(record);

		auditor.remove(value);

		return true;
	}

	/** Verifies that a facility with the specified ID exists.
	 * 
	 * @param id
	 * @return TRUE if the facility is found.
	 */
	public boolean exists(final Long id)
	{
		return (null != namedQuery("existFacilityById", Long.class).setParameter("id", id).uniqueResult());
	}

	/** Finds a single Facility entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	Facility findWithException(final Long id) throws ObjectNotFoundException
	{
		var record = get(id);
		if (null == record)
			throw new ObjectNotFoundException("Could not find the Facility because id '" + id + "' is invalid.");

		return record;
	}

	/** Finds a single Facility entity by name. */
	Facility find(final String name)
	{
		return namedQuery("findFacility").setParameter("name", name).uniqueResult();
	}

	List<Facility> findActiveByName(final String name)
	{
		return namedQuery("findActiveFacilitiesByName").setParameter("name", "%" + name + "%").setMaxResults(100).list();
	}

	List<FacilityX> findActiveByNameAndDistance(final String name, final BigDecimal latitude, final BigDecimal longitude, final long meters)
	{
		return namedQuery("findActiveFacilitiesByNameAndDistance", FacilityX.class)
			.setParameter("name", "%" + name + "%")
			.setParameter("latitude", latitude)
			.setParameter("longitude", longitude)
			.setParameter("meters", meters)
			.setMaxResults(100)
			.list();
	}

	/** Gets a single Facility value by identifier.
	 *
	 * @param id
	 * @param admin
	 * @return NULL if not found.
	 */
	public FacilityValue getById(final Long id, final boolean admin)
	{
		var record = get(id);
		return (null != record) ? cmr(record.toValueX(), admin) : null;
	}

	/** Gets a single Facility value by identifier.
	 *
	 * @param id
	 * @param admin
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is valid.
	 */
	public FacilityValue getByIdWithException(final Long id, final boolean admin) throws ObjectNotFoundException
	{
		return cmr(findWithException(id).toValueX(), admin);
	}

	/** Gets a single Facility value by name.
	 *
	 * @param name
	 * @param admin
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the name is valid.
	 */
	public FacilityValue getByNameWithException(final String name, final boolean admin) throws ObjectNotFoundException
	{
		var record = find(name);
		if ((null == record) || !record.isActive()) throw new ObjectNotFoundException("The Facility '" + name + "' does not exist.");

		return cmr(record.toValueX(), admin);
	}

	/** Gets a list of active Facility values by wild card name search.
	 * 
	 * @param name
	 * @return never NULL.
	 */
	public List<FacilityValue> getActiveByName(final String name)
	{
		return findActiveByName(name).stream().map(o -> o.toValue()).collect(toList());
	}

	/** Gets a list of active Facility values by wild card name search and distance search.
	 * 
	 * @param name
	 * @return never NULL.
	 */
	public List<FacilityValue> getActiveByNameAndDistance(final String name, final BigDecimal latitude, final BigDecimal longitude, final long meters)
	{
		return findActiveByNameAndDistance(name, latitude, longitude, meters).stream().map(o -> o.toValue()).collect(toList());
	}

	/** Gets the Activated timestamps for facilities within the specified range. Used by the AlertTask to determine if a user has new facilities in their area.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param meters
	 * @param size
	 * @return never NULL
	 */
	public Long countActivatedAtByDistance(
		final Date activatedAtFrom,
		final BigDecimal latitude,
		final BigDecimal longitude,
		final long meters,
		final int pageSize)
	{
		return namedQuery("countFacilitiesActivatedAtByDistance", Total.class)
			.setParameter("activatedAtFrom", activatedAtFrom)
			.setParameter("latitude", latitude)
			.setParameter("longitude", longitude)
			.setParameter("meters", meters)
			.setParameter("pageSize", pageSize)
			.uniqueResultOptional().orElse(Total.ZERO).total;
	}

	/** Gets a person's favorite facility IDs. Returns an empty list if the user is an admin.
	 * 
	 * @param person
	 * @return never NULL.
	 * @see ALLCLEAR-259
	 */
	public List<Long> getIdsByPerson(final PeopleValue person)
	{
		if (null == person) return List.of();
	
		return namedQuery("getFacilityIdsByPerson", Long.class).setParameter("personId", person.id).list();
	}

	/** Gets the distinct list of facility cities by state.
	 * 
	 * @param state
	 * @return never NULL
	 */
	public List<CountByName> getDistinctCitiesByState(final String state)
	{
		return namedQuery("getFacilityCitiesByState", CountByName.class).setParameter("state", state).list();
	}

	/** Gets the distinct list of facility states.
	 * 
	 * @return never NULL
	 */
	public List<CountByName> getDistinctStates()
	{
		return namedQuery("getFacilityStates", CountByName.class).list();
	}

	/** Searches the Facility entity based on the supplied filter.
	 *
	 * @param filter
	 * @param admin
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<FacilityValue, FacilityFilter> search(final FacilityFilter filter, final boolean admin) throws ValidationException
	{
		var timer = new StopWatch();

		if ((null != filter.from) && filter.from.valid())
		{
			filter.sortOn = "meters";	// Only sort by meters. Default to returning the closest first. DLS on 4/3/2020.
			var builder = createNativeQuery(filter.clean(), SELECT_, FacilityX.class, admin);
			log.info("BUILT_QUERY: {}", timer.split());
			var v = new QueryResults<FacilityValue, FacilityFilter>(builder.aggregate(COUNT_), filter);
			log.info("COUNT: {}", timer.split());
			if (v.isEmpty()) return v;

			var records = builder.orderBy(ORDER.normalize(v)).run(v);
			log.info("QUERIED: {}", timer.split());

			return v.withRecords(cmr(records.stream().map(o -> o.toValue()).collect(toList()), admin));
		}
		else
		{
			var builder = createQueryBuilder(filter.clean(), SELECT, admin);
			log.info("BUILT_QUERY: {}", timer.split());
			var v = new QueryResults<FacilityValue, FacilityFilter>(builder.aggregate(COUNT), filter);
			log.info("COUNT: {}", timer.split());
			if (v.isEmpty()) return v;

			var records = builder.orderBy(ORDER.normalize(v)).run(v);
			log.info("QUERIED: {}", timer.split());

			return v.withRecords(cmr(records.stream().map(o -> o.toValue()).collect(toList()), admin));
		}
	}

	/** Counts the number of Facility entities based on the supplied filter.
	 *
	 * @param filter
	 * @param admin
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	public long count(final FacilityFilter filter, final boolean admin) throws ValidationException
	{
		if ((null != filter.from) && filter.from.valid())
			return createNativeQuery(filter.clean(), null, FacilityX.class, admin).aggregate(COUNT_);
		else
			return createQueryBuilder(filter.clean(), null, admin).aggregate(COUNT);
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private QueryBuilder<Facility> createQueryBuilder(final FacilityFilter filter, final String select, final boolean admin)
		throws ValidationException
	{
		return createQueryBuilder(select)
			.add("id", "o.id = :id", filter.id)
			.addContains("name", "o.name LIKE :name", filter.name)
			.addContains("address", "o.address LIKE :address", filter.address)
			.addContains("city", "o.city LIKE :city", filter.city)
			.addContains("state", "o.state LIKE :state", filter.state)
			.add("latitudeFrom", "o.latitude >= :latitudeFrom", filter.latitudeFrom)
			.add("latitudeTo", "o.latitude <= :latitudeTo", filter.latitudeTo)
			.add("longitudeFrom", "o.longitude >= :longitudeFrom", filter.longitudeFrom)
			.add("longitudeTo", "o.longitude <= :longitudeTo", filter.longitudeTo)
			.addContains("phone", "o.phone LIKE :phone", filter.phone)
			.addNotNull("o.phone", filter.hasPhone)
			.addContains("appointmentPhone", "o.appointmentPhone LIKE :appointmentPhone", filter.appointmentPhone)
			.addNotNull("o.appointmentPhone", filter.hasAppointmentPhone)
			.addContains("email", "o.email LIKE :email", filter.email)
			.addNotNull("o.email", filter.hasEmail)
			.addContains("url", "o.url LIKE :url", filter.url)
			.addNotNull("o.url", filter.hasUrl)
			.addContains("appointmentUrl", "o.appointmentUrl LIKE :appointmentUrl", filter.appointmentUrl)
			.addNotNull("o.appointmentUrl", filter.hasAppointmentUrl)
			.addContains("hours", "o.hours LIKE :hours", filter.hours)
			.addNotNull("o.hours", filter.hasHours)
			.add("typeId", "o.typeId = :typeId", filter.typeId)
			.addNotNull("o.typeId", filter.hasTypeId)
			.add("driveThru", "o.driveThru = :driveThru", filter.driveThru)
			.add("appointmentRequired", "o.appointmentRequired = :appointmentRequired", filter.appointmentRequired)
			.addNotNull("o.appointmentRequired", filter.hasAppointmentRequired)
			.add("acceptsThirdParty", "o.acceptsThirdParty = :acceptsThirdParty", filter.acceptsThirdParty)
			.addNotNull("o.acceptsThirdParty", filter.hasAcceptsThirdParty)
			.add("referralRequired", "o.referralRequired = :referralRequired", filter.referralRequired)
			.add("testCriteriaId", "o.testCriteriaId = :testCriteriaId", filter.testCriteriaId)
			.add("notTestCriteriaId", "((o.testCriteriaId <> :notTestCriteriaId) OR (o.testCriteriaId IS NULL))", filter.notTestCriteriaId)
			.addNotNull("o.testCriteriaId", filter.hasTestCriteriaId)
			.addContains("otherTestCriteria", "o.otherTestCriteria LIKE :otherTestCriteria", filter.otherTestCriteria)
			.addNotNull("o.otherTestCriteria", filter.hasOtherTestCriteria)
			.add("testsPerDay", "o.testsPerDay = :testsPerDay", filter.testsPerDay)
			.addNotNull("o.testsPerDay", filter.hasTestsPerDay)
			.add("testsPerDayFrom", "o.testsPerDay >= :testsPerDayFrom", filter.testsPerDayFrom)
			.add("testsPerDayTo", "o.testsPerDay <= :testsPerDayTo", filter.testsPerDayTo)
			.add("governmentIdRequired", "o.governmentIdRequired = :governmentIdRequired", filter.governmentIdRequired)
			.add("minimumAge", "o.minimumAge = :minimumAge", filter.minimumAge)
			.addNotNull("o.minimumAge", filter.hasMinimumAge)
			.add("minimumAgeFrom", "o.minimumAge >= :minimumAgeFrom", filter.minimumAgeFrom)
			.add("minimumAgeTo", "o.minimumAge <= :minimumAgeTo", filter.minimumAgeTo)
			.addContains("doctorReferralCriteria", "o.doctorReferralCriteria LIKE :doctorReferralCriteria", filter.doctorReferralCriteria)
			.addNotNull("o.doctorReferralCriteria", filter.hasDoctorReferralCriteria)
			.add("firstResponderFriendly", "o.firstResponderFriendly = :firstResponderFriendly", filter.firstResponderFriendly)
			.add("telescreeningAvailable", "o.telescreeningAvailable = :telescreeningAvailable", filter.telescreeningAvailable)
			.add("acceptsInsurance", "o.acceptsInsurance = :acceptsInsurance", filter.acceptsInsurance)
			.addContains("insuranceProvidersAccepted", "o.insuranceProvidersAccepted LIKE :insuranceProvidersAccepted", filter.insuranceProvidersAccepted)
			.addNotNull("o.insuranceProvidersAccepted", filter.hasInsuranceProvidersAccepted)
			.add("freeOrLowCost", "o.freeOrLowCost = :freeOrLowCost", filter.freeOrLowCost)
			.add("canDonatePlasma", "o.canDonatePlasma = :canDonatePlasma", filter.canDonatePlasma)
			.add("resultNotificationEnabled", "o.resultNotificationEnabled = :resultNotificationEnabled", filter.resultNotificationEnabled)
			.addContains("notes", "o.notes LIKE :notes", filter.notes)
			.addNotNull("o.notes", filter.hasNotes)
			.add("active", "o.active = :active", filter.active)
			.addNotNull("o.activatedAt", filter.hasActivatedAt)
			.add("activatedAtFrom", "o.activatedAt >= :activatedAtFrom", filter.activatedAtFrom)
			.add("activatedAtTo", "o.activatedAt <= :activatedAtTo", filter.activatedAtTo)
			.add("createdAtFrom", "o.createdAt >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo)
			.add("updatedAtFrom", "o.updatedAt >= :updatedAtFrom", filter.updatedAtFrom)
			.add("updatedAtTo", "o.updatedAt <= :updatedAtTo", filter.updatedAtTo)
			.addIn("people", "EXISTS (SELECT 1 FROM FacilityPeople pp WHERE pp.facilityId = o.id AND pp.personId IN {})", admin ? filter.people : null)
			.addIn("includeTestTypes", "EXISTS (SELECT 1 FROM FacilityTestType tt WHERE tt.facilityId = o.id AND tt.testTypeId IN {})", filter.includeTestTypes)
			.addIn("excludeTestTypes", "NOT EXISTS (SELECT 1 FROM FacilityTestType tt WHERE tt.facilityId = o.id AND tt.testTypeId IN {})", filter.excludeTestTypes);
	}

	/** Helper method - creates the a native SQL query. */
	private <T> QueryBuilder<T> createNativeQuery(final FacilityFilter filter, final String select, final Class<T> entityClass, final boolean admin)
		throws ValidationException
	{
		var o = new NativeQueryBuilder<>(currentSession(), select, entityClass, FROM_ALIAS, null)
			.add("id", "o.id = :id", filter.id)
			.addContains("name", "o.name LIKE :name", filter.name)
			.addContains("address", "o.address LIKE :address", filter.address)
			.addContains("city", "o.city LIKE :city", filter.city)
			.addContains("state", "o.state LIKE :state", filter.state)
			.add("latitudeFrom", "o.latitude >= :latitudeFrom", filter.latitudeFrom)
			.add("latitudeTo", "o.latitude <= :latitudeTo", filter.latitudeTo)
			.add("longitudeFrom", "o.longitude >= :longitudeFrom", filter.longitudeFrom)
			.add("longitudeTo", "o.longitude <= :longitudeTo", filter.longitudeTo)
			.addContains("phone", "o.phone LIKE :phone", filter.phone)
			.addNotNull("o.phone", filter.hasPhone)
			.addContains("appointmentPhone", "o.appointment_phone LIKE :appointmentPhone", filter.appointmentPhone)
			.addNotNull("o.appointment_phone", filter.hasAppointmentPhone)
			.addContains("email", "o.email LIKE :email", filter.email)
			.addNotNull("o.email", filter.hasEmail)
			.addContains("url", "o.url LIKE :url", filter.url)
			.addNotNull("o.url", filter.hasUrl)
			.addContains("appointmentUrl", "o.appointment_url LIKE :appointmentUrl", filter.appointmentUrl)
			.addNotNull("o.appointment_url", filter.hasAppointmentUrl)
			.addContains("hours", "o.hours LIKE :hours", filter.hours)
			.addNotNull("o.hours", filter.hasHours)
			.add("typeId", "o.type_id = :typeId", filter.typeId)
			.addNotNull("o.type_id", filter.hasTypeId)
			.add("driveThru", "o.drive_thru = :driveThru", filter.driveThru)
			.add("appointmentRequired", "o.appointment_required = :appointmentRequired", filter.appointmentRequired)
			.addNotNull("o.appointment_required", filter.hasAppointmentRequired)
			.add("acceptsThirdParty", "o.accepts_third_party = :acceptsThirdParty", filter.acceptsThirdParty)
			.addNotNull("o.accepts_third_party", filter.hasAcceptsThirdParty)
			.add("referralRequired", "o.referral_required = :referralRequired", filter.referralRequired)
			.add("testCriteriaId", "o.test_criteria_id = :testCriteriaId", filter.testCriteriaId)
			.add("notTestCriteriaId", "((o.test_criteria_id <> :notTestCriteriaId) OR (o.test_criteria_id IS NULL))", filter.notTestCriteriaId)
			.addNotNull("o.test_criteria_id", filter.hasTestCriteriaId)
			.addContains("otherTestCriteria", "o.other_test_criteria LIKE :otherTestCriteria", filter.otherTestCriteria)
			.addNotNull("o.other_test_criteria", filter.hasOtherTestCriteria)
			.add("testsPerDay", "o.tests_per_day = :testsPerDay", filter.testsPerDay)
			.addNotNull("o.tests_per_day", filter.hasTestsPerDay)
			.add("testsPerDayFrom", "o.tests_per_day >= :testsPerDayFrom", filter.testsPerDayFrom)
			.add("testsPerDayTo", "o.tests_per_day <= :testsPerDayTo", filter.testsPerDayTo)
			.add("governmentIdRequired", "o.government_id_required = :governmentIdRequired", filter.governmentIdRequired)
			.add("minimumAge", "o.minimum_age = :minimumAge", filter.minimumAge)
			.addNotNull("o.minimum_age", filter.hasMinimumAge)
			.add("minimumAgeFrom", "o.minimum_age >= :minimumAgeFrom", filter.minimumAgeFrom)
			.add("minimumAgeTo", "o.minimum_age <= :minimumAgeTo", filter.minimumAgeTo)
			.addContains("doctorReferralCriteria", "o.doctor_referral_criteria LIKE :doctorReferralCriteria", filter.doctorReferralCriteria)
			.addNotNull("o.doctor_referral_criteria", filter.hasDoctorReferralCriteria)
			.add("firstResponderFriendly", "o.first_responder_friendly = :firstResponderFriendly", filter.firstResponderFriendly)
			.add("telescreeningAvailable", "o.telescreening_available = :telescreeningAvailable", filter.telescreeningAvailable)
			.add("acceptsInsurance", "o.accepts_insurance = :acceptsInsurance", filter.acceptsInsurance)
			.addContains("insuranceProvidersAccepted", "o.insurance_providers_accepted LIKE :insuranceProvidersAccepted", filter.insuranceProvidersAccepted)
			.addNotNull("o.insurance_providers_accepted", filter.hasInsuranceProvidersAccepted)
			.add("freeOrLowCost", "o.free_or_low_cost = :freeOrLowCost", filter.freeOrLowCost)
			.add("canDonatePlasma", "o.can_donate_plasma = :canDonatePlasma", filter.canDonatePlasma)
			.add("resultNotificationEnabled", "o.result_notification_enabled = :resultNotificationEnabled", filter.resultNotificationEnabled)
			.addContains("notes", "o.notes LIKE :notes", filter.notes)
			.addNotNull("o.notes", filter.hasNotes)
			.add("active", "o.active = :active", filter.active)
			.addNotNull("o.activated_at", filter.hasActivatedAt)
			.add("activatedAtFrom", "o.activated_at >= :activatedAtFrom", filter.activatedAtFrom)
			.add("activatedAtTo", "o.activated_at <= :activatedAtTo", filter.activatedAtTo)
			.add("createdAtFrom", "o.created_at >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.created_at <= :createdAtTo", filter.createdAtTo)
			.add("updatedAtFrom", "o.updated_at >= :updatedAtFrom", filter.updatedAtFrom)
			.add("updatedAtTo", "o.updated_at <= :updatedAtTo", filter.updatedAtTo)
			.addIn("people", "EXISTS (SELECT 1 FROM facility_people pp WHERE pp.facility_id = o.id AND pp.person_id IN {})", admin ? filter.people : null)
			.addIn("includeTestTypes", "EXISTS (SELECT 1 FROM facility_test_type tt WHERE tt.facility_id = o.id AND tt.test_type_id IN {})", filter.includeTestTypes)
			.addIn("excludeTestTypes", "NOT EXISTS (SELECT 1 FROM facility_test_type tt WHERE tt.facility_id = o.id AND tt.test_type_id IN {})", filter.excludeTestTypes)
			.add("fromMeters", "ST_DISTANCE_SPHERE(POINT(o.longitude, o.latitude), POINT(:fromLongitude, :fromLatitude)) <= :fromMeters", filter.from.meters());

		o.parameters.put("fromLatitude", filter.from.latitude);
		o.parameters.put("fromLongitude", filter.from.longitude);

		return o;
	}

	private FacilityValue cmr(final FacilityValue value, final boolean admin)
	{
		if (admin)
			value.withPeople(namedQuery("findFacilityPeopleByFacility", Created.class).setParameter("facilityId", value.id).stream().map(o -> o.toValue()).collect(toList()));

		return value;
	}

	private List<FacilityValue> cmr(final List<FacilityValue> values, final boolean admin)	// Populate children entities.
	{
		if (values.isEmpty()) return values;

		var ids = values.stream().map(v -> v.id).collect(toList());
		var people = admin ? namedQuery("findFacilityPeopleByFacilities", Created.class).setParameterList("facilityIds", ids).stream()
			.collect(groupingBy(o -> o.parentId, mapping(o -> o.toValue(), toList()))) : EMPTY;	
		var testTypes = namedQuery("findFacilityTestTypes", FacilityTestType.class).setParameterList("facilityIds", ids).stream()
		 	.collect(groupingBy(o -> o.getFacilityId(), mapping(o -> o.toValue(), toList())));

		values.forEach(v -> v.withPeople(people.get(v.id)).withTestTypes(testTypes.get(v.id)));

		return values;
	}
}
