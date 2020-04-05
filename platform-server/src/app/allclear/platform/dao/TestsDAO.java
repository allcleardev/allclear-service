package app.allclear.platform.dao;

import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.TestsFilter;
import app.allclear.platform.type.TestType;
import app.allclear.platform.value.TestsValue;

/**********************************************************************************
*
*	Data access object that handles access to the Tests entity.
*
*	@author smalleyd
*	@version 1.0.44
*	@since April 4, 2020
*
**********************************************************************************/

public class TestsDAO extends AbstractDAO<Tests>
{
	private static final String SELECT = "SELECT OBJECT(o) FROM Tests o";
	private static final String COUNT = "SELECT COUNT(o.id) FROM Tests o";
	private static final OrderByBuilder ORDER = new OrderByBuilder('o', 
		"id", DESC,
		"personId", ASC,
		"typeId", ASC,
		"takenOn", DESC,
		"facilityId", ASC,
		"positive", DESC,
		"notes", ASC,
		"createdAt", DESC,
		"updatedAt", DESC);

	/** Native SQL clauses. */
	public static final String FROM_ALIAS = "o";

	private final SessionDAO sessionDao;

	public TestsDAO(final SessionFactory factory, final SessionDAO sessionDao)
	{
		super(factory);

		this.sessionDao = sessionDao;
	}

	/** Adds a single Tests value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public TestsValue add(final TestsValue value) throws ValidationException
	{
		var cmrs = _validate(value.withId(null));
		return value.withId(persist(new Tests(value, (People) cmrs[1], (Facility) cmrs[2])).getId());
	}

	/** Updates a single Tests value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public TestsValue update(final TestsValue value) throws ValidationException
	{
		var cmrs = _validate(value);
		var record = (Tests) cmrs[0];
		if (null == record)
			record = findWithException(value.id);

		return value.withId(record.update(value, (People) cmrs[1], (Facility) cmrs[2]).getId());
	}

	/** Validates a single Tests value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final TestsValue value) throws ValidationException
	{
		_validate(value);
	}

	/** Validates a single Tests value and returns any CMR fields.
	 *
	 * @param value
	 * @return array of CMRs entities.
	 * @throws ValidationException
	 */
	private Object[] _validate(final TestsValue value) throws ValidationException
	{
		value.clean();
		var validator = new Validator();
		var auth = sessionDao.current();
		if (!auth.admin()) value.personId = auth.person.id;

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExistsAndLength("personId", "Person", value.personId, TestsValue.MAX_LEN_PERSON_ID)
			.ensureExistsAndContains("typeId", "Type", value.typeId, TestType.VALUES)
			.ensureExists("takenOn", "Taken On", value.takenOn)
			.ensureExists("facilityId", "Facility", value.facilityId)
			.ensureLength("notes", "Notes", value.notes, TestsValue.MAX_LEN_NOTES)
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

		value.personName = person.getName();
		value.facilityName = facility.getName();
		value.type = TestType.get(value.typeId);

		return new Object[] { null, person, facility };
	}

	/** Removes a single Tests value.
	 *
	 * @param id
	 * @return TRUE if the entity is found and removed.
	 * @throws ValidationException
	 */
	public boolean remove(final Long id) throws ValidationException
	{
		var record = get(id);
		if (null == record) return false;

		currentSession().delete(check(record));

		return true;
	}

	Tests check(final Tests record) throws NotAuthorizedException
	{
		var auth = sessionDao.current();
		if (auth.admin() || auth.person.id.equals(record.getPersonId())) return record;

		throw new NotAuthorizedException("The Person '" + auth.person + "' cannot access '" + record + "'.");
	}

	/** Finds a single Tests entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	Tests findWithException(final Long id) throws ObjectNotFoundException
	{
		var record = get(id);
		if (null == record)
			throw new ObjectNotFoundException("Could not find the Tests because id '" + id + "' is invalid.");

		return check(record);
	}

	List<Tests> findByPerson(final String personId)
	{
		var s = sessionDao.current();
		return namedQuery("findTestsByPerson")
			.setParameter("personId", s.admin() ? personId : s.person.id)
			.list();
	}

	/** Gets a single Tests value by identifier.
	 *
	 * @param id
	 * @return NULL if not found.
	 */
	public TestsValue getById(final Long id)
	{
		var record = get(id);
		if (null == record) return null;

		return check(record).toValue();
	}

	/** Gets a single Tests value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is valid.
	 */
	public TestsValue getByIdWithException(final Long id) throws ObjectNotFoundException
	{
		return findWithException(id).toValue();
	}

	/** Gets a list of Test values for a person.
	 * 
	 * @param personId
	 * @return never NULL.
	 */
	public List<TestsValue> getByPerson(final String personId)
	{
		return findByPerson(personId).stream().map(o -> o.toValue()).collect(Collectors.toList());
	}

	/** Searches the Tests entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<TestsValue, TestsFilter> search(final TestsFilter filter) throws ValidationException
	{
		var builder = createQueryBuilder(filter.clean(), SELECT);
		var v = new QueryResults<TestsValue, TestsFilter>(builder.aggregate(COUNT), filter);
		if (v.isEmpty()) return v;

		return v.withRecords(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> o.toValue()).collect(Collectors.toList()));
	}

	/** Counts the number of Tests entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	public long count(final TestsFilter filter) throws ValidationException
	{
		return createQueryBuilder(filter.clean(), null).aggregate(COUNT);
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private QueryBuilder<Tests> createQueryBuilder(final TestsFilter filter, final String select)
		throws ValidationException
	{
		var auth = sessionDao.current();
		if (!auth.admin()) filter.personId = auth.person.id;

		return createQueryBuilder(select)
			.add("id", "o.id = :id", filter.id)
			.addContains("personId", "o.personId LIKE :personId", filter.personId)
			.addContains("typeId", "o.typeId LIKE :typeId", filter.typeId)
			.add("takenOn", "o.takenOn = :takenOn", filter.takenOn)
			.add("takenOnFrom", "o.takenOn >= :takenOnFrom", filter.takenOnFrom)
			.add("takenOnTo", "o.takenOn <= :takenOnTo", filter.takenOnTo)
			.add("facilityId", "o.facilityId = :facilityId", filter.facilityId)
			.add("positive", "o.positive = :positive", filter.positive)
			.addContains("notes", "o.notes LIKE :notes", filter.notes)
			.addNotNull("o.notes", filter.hasNotes)
			.add("createdAtFrom", "o.createdAt >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo)
			.add("updatedAtFrom", "o.updatedAt >= :updatedAtFrom", filter.updatedAtFrom)
			.add("updatedAtTo", "o.updatedAt <= :updatedAtTo", filter.updatedAtTo);
	}
}
