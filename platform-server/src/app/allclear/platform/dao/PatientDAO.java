package app.allclear.platform.dao;

import static java.util.stream.Collectors.toList;
import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.List;
import java.util.stream.Stream;

import org.hibernate.SessionFactory;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.hibernate.HibernateQueryBuilder;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.PatientFilter;
import app.allclear.platform.type.Visibility;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Data access object that handles access to the Patient entity.
*
*	@author smalleyd
*	@version 1.1.111
*	@since July 18, 2020
*
**********************************************************************************/

public class PatientDAO extends AbstractDAO<Patient>
{
	private static final String SELECT = "SELECT OBJECT(o) FROM Patient o";
	private static final String COUNT = "SELECT COUNT(o.id) FROM Patient o";
	private static final OrderByBuilder ORDER = new OrderByBuilder('o', 
		"id", DESC,
		"facilityId", ASC,
		"facilityName", ASC + ",f.name;INNER JOIN o.facility f",
		"personId", ASC,
		"personName", ASC + ",p.name;INNER JOIN o.person p",
		"alertable", DESC,
		"enrolledAt", DESC,
		"rejectedAt", DESC,
		"createdAt", DESC,
		"updatedAt", DESC);

	/** Native SQL clauses. */
	public static final String FROM_ALIAS = "o";

	private final SessionDAO sessionDao;

	public PatientDAO(final SessionFactory factory, final SessionDAO sessionDao)
	{
		super(factory);
		this.sessionDao = sessionDao;
	}

	/** Adds a single Patient value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public PatientValue add(final PatientValue value) throws ValidationException
	{
		return value.withId(persist(new Patient(value, _validate(value.withId(null)))).getId());
	}

	/** Updates a single Patient value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public PatientValue update(final PatientValue value) throws ValidationException
	{
		var cmrs = _validate(value);
		var record = (Patient) cmrs[0];
		if (null == record)
			record = findWithException(value.id);

		return value.withId(check(record, true).update(value, cmrs).getId());
	}

	/** Validates a single Patient value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final PatientValue value) throws ValidationException
	{
		_validate(value);
	}

	/** Validates a single Patient value and returns any CMR fields.
	 *
	 * @param value
	 * @return array of CMRs entities.
	 * @throws ValidationException
	 */
	private Object[] _validate(final PatientValue value) throws ValidationException
	{
		value.clean();
		var validator = new Validator();

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExists("facilityId", "Facility", value.facilityId)
			.ensureExistsAndLength("personId", "Person", value.personId, PatientValue.MAX_LEN_PERSON_ID);

		// Can't have both enrolled and rejected timestamps.
		if ((null != value.enrolledAt) && (null != value.rejectedAt))
			validator.add("rejectedAt", "A Patient cannot be both enrolled and rejected.");

		validator.check();

		// Validation foreign keys.
		var session = currentSession();
		var facility = session.get(Facility.class, value.facilityId);
		if (null == facility)
			validator.add("facilityId", "The Facility ID, %d, is invalid.", value.facilityId);
		var person = session.get(People.class, value.personId);
		if (null == person)
			validator.add("personId", "The Person ID, %s, is invalid.", value.personId);

		// Throw exception if errors exist.
		validator.check();

		value.denormalize(facility.getName(), person.getName());

		// Check for duplicate.
		var record = find(value.facilityId, value.personId);
		if ((null != record) && !record.getId().equals(value.id))
			validator.add("personId", "The Person '%s' is already a patient with Facility '%s'.",
				person.getName(), facility.getName()).check();

		return new Object[] { record, facility, person };
	}

	/** Removes a single Patient value.
	 *
	 * @param id
	 * @return TRUE if the entity is found and removed.
	 * @throws ValidationException
	 */
	public boolean remove(final Long id) throws ValidationException
	{
		var record = get(id);
		if (null == record) return false;

		currentSession().delete(check(record, false, true));

		return true;
	}

	Patient check(final Patient record) { return check(record, false); }
	Patient check(final Patient record, final boolean update) { return check(record, update, false); }
	Patient check(final Patient record, final boolean update, final boolean removal) { return check(record, sessionDao.checkAdminOrPerson(), update, removal); }
	Patient check(final Patient record, final SessionValue session, final boolean update, final boolean removal)
	{
		if (session.admin() ||	// Admins can see everything.
			session.person.id.equals(record.getPersonId()))	// A patient can view, update, and remove patients.
				return record;

		if (!session.person.associated())
			throw new NotAuthorizedException("The Person " + session.person + " is not associated with any Facilities.");

		if (update ||	// A facility associate can view and remove patients only.
			session.person.associations.stream().noneMatch(a -> a.id.equals(record.getFacilityId())))	// Not an associate at the patient's facility.
				throw new NotAuthorizedException("The Facility Associate " + session.person + " cannot edit the Patient " + record + ".");

		return record;
	}

	/** Finds a single Patient entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	Patient findWithException(final Long id) throws ObjectNotFoundException
	{
		var record = get(id);
		if (null == record)
			throw new ObjectNotFoundException("Could not find the Patient because id '" + id + "' is invalid.");

		return record;
	}

	/** Finds a single Patient entity by facility and person.
	 * 
	 * @param facilityId
	 * @param personId
	 * @return NULL if not found.
	 */
	Patient find(final Long facilityId, final String personId)
	{
		return namedQuery("findPatient").setParameter("facilityId", facilityId).setParameter("personId", personId).uniqueResult();
	}

	/** Finds the patients that a specific associate can see by a wildcard name search.
	 * 
	 * @param associateId
	 * @param name
	 * @return never NULL.
	 */
	Stream<People> findEnrolledByAssociateAndName(final String associateId, final String name)
	{
		return namedQuery("findEnrolledPatientsByAssociateAndName", People.class)
			.setParameter("associateId", associateId)
			.setParameter("name", "%" + name + "%")
			.stream();
	}

	/** Finds the patients associated with a specific facility by a wildcard name search.
	 * 
	 * @param facilityId
	 * @param name
	 * @return never NULL.
	 */
	Stream<People> findEnrolledByFacilityAndName(final Long facilityId, final String name)
	{
		return namedQuery("findEnrolledPatientsByFacilityAndName", People.class)
			.setParameter("facilityId", facilityId)
			.setParameter("name", "%" + name + "%")
			.stream();
	}

	/** Gets a single Patient value by identifier.
	 *
	 * @param id
	 * @return NULL if not found.
	 */
	public PatientValue getById(final Long id)
	{
		var record = get(id);
		if (null == record) return null;

		return check(record).toValue();
	}

	/** Gets a single Patient value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is valid.
	 */
	public PatientValue getByIdWithException(final Long id) throws ObjectNotFoundException
	{
		return check(findWithException(id)).toValue();
	}

	/** Gets the patients associated with a specific facility (set of facilities) by a wildcard name search.
	 * 
	 * @param facilityId
	 * @param name
	 * @return never NULL.
	 * @throws NotAuthorizedException
	 */
	public List<PeopleValue> getEnrolledByFacilityAndName(final Long facilityId, final String name)
		throws NotAuthorizedException
	{
		var s = sessionDao.checkAdminOrPerson();
		final Stream<People> records;

		if (s.person())
		{
			if (!s.person.associated()) throw new NotAuthorizedException("The Person " + s.person + " is not associated with any Facilities.");

			records = findEnrolledByAssociateAndName(s.person.id, name);
		}
		else
			records = findEnrolledByFacilityAndName(facilityId, name);

		return records
			.map(o -> o.toValue(Visibility.ALL))
			.collect(toList());
	}

	/** Searches the Patient entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<PatientValue, PatientFilter> search(final PatientFilter filter) throws ValidationException
	{
		var builder = createQueryBuilder(filter.clean(), SELECT);
		var v = new QueryResults<PatientValue, PatientFilter>(builder.aggregate(COUNT), filter);
		if (v.isEmpty()) return v;

		return v.withRecords(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> o.toValue()).collect(toList()));
	}

	/** Counts the number of Patient entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	public long count(final PatientFilter filter) throws ValidationException
	{
		return createQueryBuilder(filter.clean(), null).aggregate(COUNT);
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private QueryBuilder<Patient> createQueryBuilder(final PatientFilter filter, final String select)
		throws ValidationException
	{
		var s = sessionDao.checkAdminOrPerson();
		String associateId = null;

		if (s.person())
		{
			if (s.person.associated()) associateId = s.person.id;
			else filter.personId = s.person.id;	// Patient can only see their records.
		}

		return new HibernateQueryBuilder<Patient>(currentSession(), select, Patient.class)
			.add("id", "o.id = :id", filter.id)
			.add("facilityId", "o.facilityId = :facilityId", filter.facilityId)
			.add("personId", "o.personId LIKE :personId", filter.personId)
			.add("associateId", "a.personId = :associateId", associateId, "INNER JOIN o.facility f", "INNER JOIN f.people a")
			.add("alertable", "o.alertable = :alertable", filter.alertable)
			.addNotNull("o.enrolledAt", filter.hasEnrolledAt)
			.add("enrolledAtFrom", "o.enrolledAt >= :enrolledAtFrom", filter.enrolledAtFrom)
			.add("enrolledAtTo", "o.enrolledAt <= :enrolledAtTo", filter.enrolledAtTo)
			.addNotNull("o.rejectedAt", filter.hasRejectedAt)
			.add("rejectedAtFrom", "o.rejectedAt >= :rejectedAtFrom", filter.rejectedAtFrom)
			.add("rejectedAtTo", "o.rejectedAt <= :rejectedAtTo", filter.rejectedAtTo)
			.add("createdAtFrom", "o.createdAt >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo)
			.add("updatedAtFrom", "o.updatedAt >= :updatedAtFrom", filter.updatedAtFrom)
			.add("updatedAtTo", "o.updatedAt <= :updatedAtTo", filter.updatedAtTo);
	}
}
