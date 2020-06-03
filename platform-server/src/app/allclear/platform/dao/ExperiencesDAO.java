package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.hibernate.HibernateQueryBuilder;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.ExperiencesFilter;
import app.allclear.platform.value.ExperiencesValue;

/**********************************************************************************
*
*	Data access object that handles access to the Experiences entity.
*
*	@author smalleyd
*	@version 1.1.80
*	@since June 2, 2020
*
**********************************************************************************/

public class ExperiencesDAO extends AbstractDAO<Experiences>
{
	private static final String SELECT = "SELECT OBJECT(o) FROM Experiences o";
	private static final String COUNT = "SELECT COUNT(o.id) FROM Experiences o";
	private static final OrderByBuilder ORDER = new OrderByBuilder('o', 
		"id", DESC,
		"personId", ASC,
		"personName", ASC + ",p.name;INNER JOIN o.person p",
		"facilityId", ASC,
		"facilityName", ASC + ",f.name;INNER JOIN o.facility f",
		"positive", DESC,
		"createdAt", DESC);

	/** Native SQL clauses. */
	public static final String FROM_ALIAS = "o";

	private final SessionDAO sessionDao;

	public ExperiencesDAO(final SessionFactory factory, final SessionDAO sessionDao)
	{
		super(factory);
		this.sessionDao = sessionDao;
	}

	/** Adds a single Experiences value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public ExperiencesValue add(final ExperiencesValue value) throws ValidationException
	{
		var cmrs = _validate(value.withId(null).withPersonId(sessionDao.checkPerson().id));

		return value.withId(persist(new Experiences(value, cmrs)).getId());
	}

	/** Updates a single Experiences value.
	 *
	 * @param value
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 * @throws ValidationException
	 */
	public ExperiencesValue update(final ExperiencesValue value) throws ObjectNotFoundException, ValidationException
	{
		sessionDao.checkAdmin();

		var cmrs = _validate(value);
		var record = (Experiences) cmrs[0];
		if (null == record)
		{
			if (null == value.id) throw new ValidationException("id", "Please supply the Experience ID.");

			record = findWithException(value.id, false);
		}

		return value.withId(record.update(value, cmrs).getId());
	}

	/** Validates a single Experiences value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final ExperiencesValue value) throws ValidationException
	{
		_validate(value);
	}

	/** Validates a single Experiences value and returns any CMR fields.
	 *
	 * @param value
	 * @return the existing entity on updates and any CMRs.
	 * @throws ValidationException
	 */
	private Object[] _validate(final ExperiencesValue value) throws ValidationException
	{
		value.clean();
		var validator = new Validator();

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExistsAndLength("personId", "Person", value.personId, ExperiencesValue.MAX_LEN_PERSON_ID)
			.ensureExists("facilityId", "Facility", value.facilityId)
			.check();

		// Validation foreign keys.
		var session = currentSession();
		var person = session.get(People.class, value.personId);
		if (null == person)
			validator.add("personId", "The Person ID, %s, is invalid.", value.personId);
		var facility = session.get(Facility.class, value.facilityId);
		if (null == facility)
			validator.add("facilityId", "The Facility ID, %d, is invalid.", value.facilityId);

		// Throw exception if errors exist.
		validator.check();

		// Checks for an existing experience.
		var record = find(value.personId, value.facilityId);
		if ((null != record) && !record.getId().equals(value.id))
			validator.add("facilityId", "You have already provided an Experience for %s.", facility.getName()).check();

		value.denormalize(person.getName(), facility.getName());

		return new Object[] { record, person, facility };
	}

	/** Removes a single Experiences value.
	 *
	 * @param id
	 * @return TRUE if the entity is found and removed.
	 * @throws ValidationException
	 */
	public boolean remove(final Long id) throws ValidationException
	{
		sessionDao.checkAdmin();

		var record = get(id);
		if (null == record) return false;

		currentSession().delete(record);

		return true;
	}

	/** Verifies that the current user can see the reported Experience. */
	Experiences check(final Experiences record) throws NotAuthorizedException { return check(record, true); }
	Experiences check(final Experiences record, final boolean readOnly) throws NotAuthorizedException
	{
		var s = sessionDao.checkAdminOrPerson();
		if (s.canAdmin()) return record;
		if (readOnly && s.person.id.equals(record.getPersonId())) return record;

		throw new NotAuthorizedException("The Person " + s.person + " canNOT access Experience " + record + ".");
	}

	/** Finds a single Experiences entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	Experiences findWithException(final Long id) throws ObjectNotFoundException { return findWithException(id, true); }
	Experiences findWithException(final Long id, final boolean readOnly) throws ObjectNotFoundException
	{
		var record = get(id);
		if (null == record)
			throw new ObjectNotFoundException("Could not find the Experiences because id '" + id + "' is invalid.");

		return check(record, readOnly);
	}

	Experiences find(final String personId, final Long facilityId)
	{
		return namedQuery("findExperience").setParameter("personId", personId).setParameter("facilityId", facilityId).uniqueResult();
	}

	/** Gets a single Experiences value by identifier.
	 *
	 * @param id
	 * @return NULL if not found.
	 */
	public ExperiencesValue getById(final Long id)
	{
		var record = get(id);
		return (null == record) ? null : check(record).toValueX();
	}

	/** Gets a single Experiences value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is valid.
	 */
	public ExperiencesValue getByIdWithException(final Long id) throws ObjectNotFoundException
	{
		return findWithException(id).toValueX();
	}

	/** Searches the Experiences entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<ExperiencesValue, ExperiencesFilter> search(final ExperiencesFilter filter) throws ValidationException
	{
		var builder = createQueryBuilder(filter.clean(), SELECT);
		var v = new QueryResults<ExperiencesValue, ExperiencesFilter>(builder.aggregate(COUNT), filter);
		if (v.isEmpty()) return v;

		return v.withRecords(cmr(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> o.toValue()).collect(toList())));
	}

	/** Counts the number of Experiences entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	public long count(final ExperiencesFilter filter) throws ValidationException
	{
		return createQueryBuilder(filter.clean(), null).aggregate(COUNT);
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private QueryBuilder<Experiences> createQueryBuilder(final ExperiencesFilter filter, final String select)
		throws ValidationException
	{
		var s = sessionDao.checkAdminOrPerson();
		if (s.person()) filter.personId = s.person.id;

		return new HibernateQueryBuilder<Experiences>(currentSession(), select, Experiences.class)
			.add("id", "o.id = :id", filter.id)
			.add("personId", "o.personId = :personId", filter.personId)
			.add("facilityId", "o.facilityId = :facilityId", filter.facilityId)
			.add("positive", "o.positive = :positive", filter.positive)
			.add("createdAtFrom", "o.createdAt >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo);
	}

	private List<ExperiencesValue> cmr(final List<ExperiencesValue> values)
	{
		if (CollectionUtils.isEmpty(values)) return values;

		var people = namedQuery("getPeopleNamesByIds", Named.class)
			.setParameterList("ids", values.stream().map(v -> v.personId).distinct().collect(toList()))
			.stream().collect(toMap(o -> o.id, o -> o.name));
		var facilities = namedQuery("getFacilityNamesByIds", Name.class)
			.setParameterList("ids", values.stream().map(v -> v.facilityId).distinct().collect(toList()))
			.stream().collect(toMap(o -> o.id, o -> o.name));

		values.forEach(v -> v.denormalize(people.get(v.personId), facilities.get(v.facilityId)));

		return values;
	}
}
