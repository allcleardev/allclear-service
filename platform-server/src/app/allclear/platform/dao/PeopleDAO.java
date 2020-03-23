package app.allclear.platform.dao;

import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.stream.Collectors;

import org.hibernate.*;

import app.allclear.common.dao.*;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.errors.Validator;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.type.PeopleStatus;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Data access object that handles access to the People entity.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 22, 2020
*
**********************************************************************************/

public class PeopleDAO extends AbstractDAO<People>
{
	private static final String SELECT = "SELECT OBJECT(o) FROM People o";
	private static final String COUNT = "SELECT COUNT(o.id) FROM People o";
	private static final OrderByBuilder ORDER = new OrderByBuilder('o', 
		"id", ASC,
		"name", ASC,
		"phone", ASC,
		"email", ASC,
		"firstName", ASC,
		"lastName", ASC,
		"dob", DESC,
		"statusId", ASC,
		"active", DESC,
		"authAt", DESC,
		"phoneVerifiedAt", DESC,
		"emailVerifiedAt", DESC,
		"createdAt", DESC,
		"updatedAt", DESC);

	public PeopleDAO(final SessionFactory factory)
	{
		super(factory);
	}

	/** Adds a single People value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public PeopleValue add(final PeopleValue value) throws ValidationException
	{
		return value.withId(persist(toEntity(value, _validate(value.withId(null)))).getId());
	}

	/** Updates a single People value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public PeopleValue update(final PeopleValue value) throws ValidationException
	{
		var cmrs = _validate(value);
		var record = (People) cmrs[0];
		if (null == record)
			record = findWithException(value.id);

		return value.withId(toEntity(value, record, cmrs).getId());
	}

	/** Validates a single People value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final PeopleValue value) throws ValidationException
	{
		_validate(value);
	}

	/** Validates a single People value and returns any CMR fields.
	 *
	 * @param value
	 * @return array of CMRs entities.
	 * @throws ValidationException
	 */
	private Object[] _validate(final PeopleValue value) throws ValidationException
	{
		value.clean();
		var validator = new Validator();

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExistsAndLength("id", "ID", value.id, PeopleValue.MAX_LEN_ID)
			.ensureExistsAndLength("name", "Name", value.name, PeopleValue.MAX_LEN_NAME)
			.ensureExistsAndLength("phone", "Phone", value.phone, PeopleValue.MAX_LEN_PHONE)
			.ensureLength("email", "Email", value.email, PeopleValue.MAX_LEN_EMAIL)
			.ensureLength("firstName", "First Name", value.firstName, PeopleValue.MAX_LEN_FIRST_NAME)
			.ensureLength("lastName", "Last Name", value.lastName, PeopleValue.MAX_LEN_LAST_NAME)
			.ensureLength("statusId", "Status", value.statusId, PeopleValue.MAX_LEN_STATUS_ID)
			.check();

		if (!PeopleStatus.VALUES.containsKey(value.statusId))
			validator.add("statusId", "The Status ID '%s' is invalid.", value.statusId);

		// Throw exception if errors exist.
		validator.check();

		return new Object[] {  };
	}

	/** Removes a single People value.
	 *
	 * @param id
	 * @return TRUE if the entity is found and removed.
	 * @throws ValidationException
	 */
	public boolean remove(final String id) throws ValidationException
	{
		var record = get(id);
		if (null == record) return false;

		currentSession().delete(record);

		return true;
	}

	/** Finds a single People entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ValidationException if the identifier is invalid.
	 */
	public People findWithException(final String id) throws ValidationException
	{
		var record = get(id);
		if (null == record)
			throw new ValidationException("id", "Could not find the People because id '" + id + "' is invalid.");

		return record;
	}

	/** Gets a single People value by identifier.
	 *
	 * @param id
	 * @return NULL if not found.
	 */
	public PeopleValue getById(final String id)
	{
		var record = get(id);
		if (null == record) return null;

		return toValue(record);
	}

	/** Gets a single People value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ValidationException if the identifier is valid.
	 */
	public PeopleValue getByIdWithException(final String id) throws ValidationException
	{
		return toValue(findWithException(id));
	}

	/** Searches the People entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<PeopleValue, PeopleFilter> search(final PeopleFilter filter) throws ValidationException
	{
		var builder = createQueryBuilder(filter.clean(), SELECT);
		var v = new QueryResults<PeopleValue, PeopleFilter>(builder.aggregate(COUNT), filter);
		if (v.isEmpty()) return v;

		return v.withRecords(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> toValue(o)).collect(Collectors.toList()));
	}

	/** Counts the number of People entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	public long count(final PeopleFilter filter) throws ValidationException
	{
		return createQueryBuilder(filter.clean(), null).aggregate(COUNT);
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private QueryBuilder<People> createQueryBuilder(final PeopleFilter filter, final String select)
		throws ValidationException
	{
		return createQueryBuilder(select)
			.addContains("id", "o.id LIKE :id", filter.id)
			.addContains("name", "o.name LIKE :name", filter.name)
			.addContains("phone", "o.phone LIKE :phone", filter.phone)
			.addContains("email", "o.email LIKE :email", filter.email)
			.addContains("firstName", "o.firstName LIKE :firstName", filter.firstName)
			.addContains("lastName", "o.lastName LIKE :lastName", filter.lastName)
			.add("dob", "o.dob = :dob", filter.dob)
			.add("dobFrom", "o.dob >= :dobFrom", filter.dobFrom)
			.add("dobTo", "o.dob <= :dobTo", filter.dobTo)
			.addContains("statusId", "o.statusId LIKE :statusId", filter.statusId)
			.add("active", "o.active = :active", filter.active)
			.add("authAtFrom", "o.authAt >= :authAtFrom", filter.authAtFrom)
			.add("authAtTo", "o.authAt <= :authAtTo", filter.authAtTo)
			.add("phoneVerifiedAtFrom", "o.phoneVerifiedAt >= :phoneVerifiedAtFrom", filter.phoneVerifiedAtFrom)
			.add("phoneVerifiedAtTo", "o.phoneVerifiedAt <= :phoneVerifiedAtTo", filter.phoneVerifiedAtTo)
			.add("emailVerifiedAtFrom", "o.emailVerifiedAt >= :emailVerifiedAtFrom", filter.emailVerifiedAtFrom)
			.add("emailVerifiedAtTo", "o.emailVerifiedAt <= :emailVerifiedAtTo", filter.emailVerifiedAtTo)
			.add("createdAtFrom", "o.createdAt >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo)
			.add("updatedAtFrom", "o.updatedAt >= :updatedAtFrom", filter.updatedAtFrom)
			.add("updatedAtTo", "o.updatedAt <= :updatedAtTo", filter.updatedAtTo);
	}

	/** Helper method - creates a non-transactional value from a transactional entity. */
	private PeopleValue toValue(final People record) { return record.toValue(); }

	/** Helper method - creates a transactional entity from a non-transactional value. */
	public People toEntity(final PeopleValue value, final Object[] cmrs) { return new People(value.initDates()); }

	/** Helper method - populates the transactional entity from the non-transactional value. */
	public People toEntity(final PeopleValue value, final People record, final Object[] cmrs)
	{
		record.setName(value.name);
		record.setPhone(value.phone);
		record.setEmail(value.email);
		record.setFirstName(value.firstName);
		record.setLastName(value.lastName);
		record.setDob(value.dob);
		record.setStatusId(value.statusId);
		record.setActive(value.active);
		record.setAuthAt(value.authAt);
		record.setPhoneVerifiedAt(value.phoneVerifiedAt);
		record.setEmailVerifiedAt(value.emailVerifiedAt);
		value.createdAt = record.getCreatedAt();
		record.setUpdatedAt(value.updatedAt);

		return record;
	}
}
