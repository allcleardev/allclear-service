package app.allclear.platform.dao;

import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.*;
import org.hibernate.id.IdentifierGenerationException;

import app.allclear.common.dao.*;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.errors.Validator;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.type.PeopleStatus;
import app.allclear.platform.type.PeopleStature;
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
		"statureId", ASC,
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

	String generateId()
	{
		int i = 0;
		var id = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
		while (null != namedQuery("existsPeople", String.class).setParameter("id", id).uniqueResult())	// Find an available ID.
		{
			if (10 < i++) throw new IdentifierGenerationException("Failed to generate an available ID after 10 tries.");
			id = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
		}

		return id;
	}

	/** Adds a single People value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public PeopleValue add(final PeopleValue value) throws ValidationException
	{
		return value.withId(persist(toEntity(value, _validate(value.withId(generateId())))).getId());
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
			.ensureExistsAndLength("name", "Screen Name", value.name, PeopleValue.MAX_LEN_NAME)
			.ensureExistsAndLength("phone", "Phone Number", value.phone, PeopleValue.MAX_LEN_PHONE)
			.ensureLength("email", "Email Address", value.email, PeopleValue.MAX_LEN_EMAIL)
			.ensureLength("firstName", "First Name", value.firstName, PeopleValue.MAX_LEN_FIRST_NAME)
			.ensureLength("lastName", "Last Name", value.lastName, PeopleValue.MAX_LEN_LAST_NAME)
			.ensureLength("statusId", "Status", value.statusId, PeopleValue.MAX_LEN_STATUS_ID)
			.ensureLength("statureId", "Stature", value.statureId, PeopleValue.MAX_LEN_STATURE_ID)
			.check();

		if ((null != value.statusId) && (null == (value.status = PeopleStatus.VALUES.get(value.statusId))))
			validator.add("statusId", "The Status ID '%s' is invalid.", value.statusId);

		if ((null != value.statureId) && (null == (value.stature = PeopleStature.VALUES.get(value.statureId))))
			validator.add("statureId", "The Stature ID '%s' is invalid.", value.statureId);

		// Throw exception if errors exist.
		validator.check();

		var o = find(value.name);
		if ((null != o) && !o.getId().equals(value.id))
			validator.add("name", "The Screen Name '%s' is already taken.", value.name);

		var o2 = findByPhone(value.phone);
		if ((null != o2) && !o2.getId().equals(value.id))
			validator.add("email", "The Phone Number '%s' is already taken.", value.phone);

		var o3 = (null != value.email) ? findByEmail(value.email) : null;
		if ((null != o3) && !o3.getId().equals(value.id))
			validator.add("email", "The Email Address '%s' is already taken.", value.email);

		// Throw exception if errors exist.
		validator.check();

		return new Object[] { (null != o) ? o : (null != o2) ? o2 : o3 };
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

	People find(final String name) { return namedQuery("findPeople").setParameter("name", name).uniqueResult(); }
	People findByEmail(final String email) { return namedQuery("findPeopleByEmail").setParameter("email", email).uniqueResult(); }
	People findByPhone(final String phone) { return namedQuery("findPeopleByPhone").setParameter("phone", phone).uniqueResult(); }
	List<People> findActiveByIdOrName(final String name) { return namedQuery("findActivePeopleByIdOrName").setParameter("name", name + "%").list(); }

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

	/** Gets a list of active People by wildcard ID and/or name search.
	 * 
	 * @param name
	 * @return never NULL.
	 */
	public List<PeopleValue> getActiveByIdOrName(final String name)
	{
		return findActiveByIdOrName(name).stream().map(o -> o.toValue()).collect(Collectors.toList());
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
			.addStarts("id", "o.id LIKE :id", filter.id)
			.addStarts("name", "o.name LIKE :name", filter.name)
			.addStarts("phone", "o.phone LIKE :phone", filter.phone)
			.addStarts("email", "o.email LIKE :email", filter.email)
			.addNotNull("o.email", filter.hasEmail)
			.addContains("firstName", "o.firstName LIKE :firstName", filter.firstName)
			.addNotNull("o.firstName", filter.hasFirstName)
			.addContains("lastName", "o.lastName LIKE :lastName", filter.lastName)
			.addNotNull("o.lastName", filter.hasLastName)
			.add("dob", "o.dob = :dob", filter.dob)
			.addNotNull("o.dob", filter.hasDob)
			.add("dobFrom", "o.dob >= :dobFrom", filter.dobFrom)
			.add("dobTo", "o.dob <= :dobTo", filter.dobTo)
			.add("statusId", "o.statusId = :statusId", filter.statusId)
			.addNotNull("o.statusId", filter.hasStatusId)
			.add("statureId", "o.statureId = :statureId", filter.statureId)
			.addNotNull("o.statureId", filter.hasStatureId)
			.add("active", "o.active = :active", filter.active)
			.addNotNull("o.authAt", filter.hasAuthAt)
			.add("authAtFrom", "o.authAt >= :authAtFrom", filter.authAtFrom)
			.add("authAtTo", "o.authAt <= :authAtTo", filter.authAtTo)
			.addNotNull("o.phoneVerifiedAt", filter.hasPhoneVerifiedAt)
			.add("phoneVerifiedAtFrom", "o.phoneVerifiedAt >= :phoneVerifiedAtFrom", filter.phoneVerifiedAtFrom)
			.add("phoneVerifiedAtTo", "o.phoneVerifiedAt <= :phoneVerifiedAtTo", filter.phoneVerifiedAtTo)
			.addNotNull("o.emailVerifiedAt", filter.hasEmailVerifiedAt)
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
		record.setStatureId(value.statureId);
		record.setActive(value.active);
		record.setAuthAt(value.authAt);
		record.setPhoneVerifiedAt(value.phoneVerifiedAt);
		record.setEmailVerifiedAt(value.emailVerifiedAt);
		value.createdAt = record.getCreatedAt();
		record.setUpdatedAt(value.updatedAt = new Date());

		return record;
	}
}
