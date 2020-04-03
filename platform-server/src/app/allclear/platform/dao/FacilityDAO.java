package app.allclear.platform.dao;

import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.*;

import app.allclear.common.dao.*;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.errors.Validator;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.type.FacilityType;
import app.allclear.platform.type.TestCriteria;
import app.allclear.platform.value.FacilityValue;

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
	private static final String SELECT = "SELECT OBJECT(o) FROM Facility o";
	private static final String COUNT = "SELECT COUNT(o.id) FROM Facility o";
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
		"notes", ASC,
		"active", DESC,
		"createdAt", DESC,
		"updatedAt", DESC);

	/** Native SQL clauses. */
	public static final String FROM_ALIAS = "o";

	public FacilityDAO(final SessionFactory factory)
	{
		super(factory);
	}

	/** Adds a single Facility value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public FacilityValue add(final FacilityValue value) throws ValidationException
	{
		_validate(value.withId(null));
		return value.withId(persist(new Facility(value)).getId());
	}

	/** Updates a single Facility value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public FacilityValue update(final FacilityValue value) throws ValidationException
	{
		 _validate(value)
		 	.ensureExists("id", "ID", value.id)
		 	.check();

		var record = findWithException(value.id);

		return value.withId(record.update(value).getId());
	}

	/** Validates a single Facility value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final FacilityValue value) throws ValidationException
	{
		_validate(value);
	}

	/** Validates a single Facility value and returns any CMR fields.
	 *
	 * @param value
	 * @return array of CMRs entities.
	 * @throws ValidationException
	 */
	private Validator _validate(final FacilityValue value) throws ValidationException
	{
		value.clean();
		var validator = new Validator();

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

		validator.check();

		return validator;
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

		currentSession().delete(record);

		return true;
	}

	/** Finds a single Facility entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ValidationException if the identifier is invalid.
	 */
	Facility findWithException(final Long id) throws ValidationException
	{
		var record = get(id);
		if (null == record)
			throw new ValidationException("id", "Could not find the Facility because id '" + id + "' is invalid.");

		return record;
	}

	List<Facility> findActiveByName(final String name)
	{
		return namedQuery("findActiveFacilitiesByName").setParameter("name", "%" + name + "%").list();
	}

	/** Gets a single Facility value by identifier.
	 *
	 * @param id
	 * @return NULL if not found.
	 */
	public FacilityValue getById(final Long id)
	{
		var record = get(id);
		return (null != record) ? record.toValue() : null;
	}

	/** Gets a single Facility value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ValidationException if the identifier is valid.
	 */
	public FacilityValue getByIdWithException(final Long id) throws ValidationException
	{
		return findWithException(id).toValue();
	}

	/** Gets a list of active Facility values by wild card name search.
	 * 
	 * @param name
	 * @return never NULL.
	 */
	public List<FacilityValue> getActiveByName(final String name)
	{
		return findActiveByName(name).stream().map(o -> o.toValue()).collect(Collectors.toList());
	}
	/** Searches the Facility entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<FacilityValue, FacilityFilter> search(final FacilityFilter filter) throws ValidationException
	{
		var builder = createQueryBuilder(filter.clean(), SELECT);
		var v = new QueryResults<FacilityValue, FacilityFilter>(builder.aggregate(COUNT), filter);
		if (v.isEmpty()) return v;

		return v.withRecords(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> o.toValue()).collect(Collectors.toList()));
	}

	/** Counts the number of Facility entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	public long count(final FacilityFilter filter) throws ValidationException
	{
		return createQueryBuilder(filter.clean(), null).aggregate(COUNT);
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private QueryBuilder<Facility> createQueryBuilder(final FacilityFilter filter, final String select)
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
			.addContains("typeId", "o.typeId LIKE :typeId", filter.typeId)
			.addNotNull("o.typeId", filter.hasTypeId)
			.add("driveThru", "o.driveThru = :driveThru", filter.driveThru)
			.add("appointmentRequired", "o.appointmentRequired = :appointmentRequired", filter.appointmentRequired)
			.addNotNull("o.appointmentRequired", filter.hasAppointmentRequired)
			.add("acceptsThirdParty", "o.acceptsThirdParty = :acceptsThirdParty", filter.acceptsThirdParty)
			.addNotNull("o.acceptsThirdParty", filter.hasAcceptsThirdParty)
			.add("referralRequired", "o.referralRequired = :referralRequired", filter.referralRequired)
			.addContains("testCriteriaId", "o.testCriteriaId LIKE :testCriteriaId", filter.testCriteriaId)
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
			.addContains("notes", "o.notes LIKE :notes", filter.notes)
			.addNotNull("o.notes", filter.hasNotes)
			.add("active", "o.active = :active", filter.active)
			.add("createdAtFrom", "o.createdAt >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo)
			.add("updatedAtFrom", "o.updatedAt >= :updatedAtFrom", filter.updatedAtFrom)
			.add("updatedAtTo", "o.updatedAt <= :updatedAtTo", filter.updatedAtTo);
	}
}
