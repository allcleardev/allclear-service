package app.allclear.platform.dao;

import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.stream.Collectors;

import org.hibernate.SessionFactory;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.hibernate.HibernateQueryBuilder;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.SymptomsLogFilter;
import app.allclear.platform.type.Symptom;
import app.allclear.platform.value.SymptomsLogValue;

/**********************************************************************************
*
*	Data access object that handles access to the SymptomsLog entity.
*
*	@author smalleyd
*	@version 1.0.80
*	@since April 8, 2020
*
**********************************************************************************/

public class SymptomsLogDAO extends AbstractDAO<SymptomsLog>
{
	private static final String SELECT = "SELECT OBJECT(o) FROM SymptomsLog o";
	private static final String COUNT = "SELECT COUNT(o.id) FROM SymptomsLog o";
	private static final OrderByBuilder ORDER = new OrderByBuilder('o', 
		"id", DESC,
		"personId", ASC,
		"symptomId", ASC,
		"startedAt", DESC,
		"endedAt", DESC);

	/** Native SQL clauses. */
	public static final String FROM_ALIAS = "o";

	public SymptomsLogDAO(final SessionFactory factory)
	{
		super(factory);
	}

	/** Adds a single SymptomsLog value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public SymptomsLogValue add(final SymptomsLogValue value) throws ValidationException
	{
		var cmrs = _validate(value.withId(null));
		return value.withId(persist(new SymptomsLog(value, (People) cmrs[1])).getId());
	}

	/** Validates a single SymptomsLog value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final SymptomsLogValue value) throws ValidationException
	{
		_validate(value);
	}

	/** Validates a single SymptomsLog value and returns any CMR fields.
	 *
	 * @param value
	 * @return array of CMRs entities.
	 * @throws ValidationException
	 */
	private Object[] _validate(final SymptomsLogValue value) throws ValidationException
	{
		value.clean();
		var validator = new Validator();

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExistsAndLength("personId", "Person", value.personId, SymptomsLogValue.MAX_LEN_PERSON_ID)
			.ensureExistsAndLength("symptomId", "Symptom", value.symptomId, SymptomsLogValue.MAX_LEN_SYMPTOM_ID)
			.ensureExists("startedAt", "Started At", value.startedAt)
			.check();

		// Validation foreign keys.
		var person = currentSession().get(People.class, value.personId);
		if (null == person)
			validator.add("personId", "The Person ID, %s, is invalid.", value.personId);
		if (null == (value.symptom = Symptom.get(value.symptomId)))
			validator.add("symptomId", "The Symptom ID, %s, is invalid.", value.symptomId);

		// Throw exception if errors exist.
		validator.check();

		return new Object[] { null, person };
	}

	/** Removes a single SymptomsLog value.
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

	/** Finds a single SymptomsLog entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	public SymptomsLog findWithException(final Long id) throws ObjectNotFoundException
	{
		var record = get(id);
		if (null == record)
			throw new ObjectNotFoundException("Could not find the SymptomsLog because id '" + id + "' is invalid.");

		return record;
	}

	/** Gets a single SymptomsLog value by identifier.
	 *
	 * @param id
	 * @return NULL if not found.
	 */
	public SymptomsLogValue getById(final Long id)
	{
		var record = get(id);
		if (null == record) return null;

		return record.toValue();
	}

	/** Gets a single SymptomsLog value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is valid.
	 */
	public SymptomsLogValue getByIdWithException(final Long id) throws ObjectNotFoundException
	{
		return findWithException(id).toValue();
	}

	/** Searches the SymptomsLog entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<SymptomsLogValue, SymptomsLogFilter> search(final SymptomsLogFilter filter) throws ValidationException
	{
		var builder = createQueryBuilder(filter.clean(), SELECT);
		var v = new QueryResults<SymptomsLogValue, SymptomsLogFilter>(builder.aggregate(COUNT), filter);
		if (v.isEmpty()) return v;

		return v.withRecords(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> o.toValue()).collect(Collectors.toList()));
	}

	/** Counts the number of SymptomsLog entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	public long count(final SymptomsLogFilter filter) throws ValidationException
	{
		return createQueryBuilder(filter.clean(), null).aggregate(COUNT);
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private QueryBuilder<SymptomsLog> createQueryBuilder(final SymptomsLogFilter filter, final String select)
		throws ValidationException
	{
		return new HibernateQueryBuilder<SymptomsLog>(currentSession(), select, SymptomsLog.class)
			.add("id", "o.id = :id", filter.id)
			.add("personId", "o.personId = :personId", filter.personId)
			.add("symptomId", "o.symptomId = :symptomId", filter.symptomId)
			.add("startedAtFrom", "o.startedAt >= :startedAtFrom", filter.startedAtFrom)
			.add("startedAtTo", "o.startedAt <= :startedAtTo", filter.startedAtTo)
			.addNotNull("o.endedAt", filter.hasEndedAt)
			.add("endedAtFrom", "o.endedAt >= :endedAtFrom", filter.endedAtFrom)
			.add("endedAtTo", "o.endedAt <= :endedAtTo", filter.endedAtTo);
	}
}
