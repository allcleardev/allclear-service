package app.allclear.platform.dao;

import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.id.IdentifierGenerationException;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.value.CreatedValue;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.type.*;
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
		"sexId", ASC,
		"healthWorkerStatusId", ASC,
		"latitude", DESC,
		"longitude", DESC,
		"alertable", DESC,
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
		var q = namedQuery("existsPeople", String.class);
		var id = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
		while (null != q.setParameter("id", id).uniqueResult())	// Find an available ID.
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
		_validate(value.withId(generateId()));
		var record = persist(new People(value));

		// Save children.
		var s = currentSession();
		add(s, value.conditions, v -> new Conditions(record, v));
		add(s, value.exposures, v -> new Exposures(record, v));
		add(s, value.symptoms, v -> new Symptoms(record, v));

		return value.withId(record.getId());
	}

	private void add(final Session s, final List<CreatedValue> values, final Function<CreatedValue, ? extends PeopleChild> toEntity)
	{
		if (CollectionUtils.isEmpty(values)) return;
		values.stream().filter(v -> null != v).forEach(v -> s.persist(toEntity.apply(v)));
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
		if (null == record) record = findWithException(value.id);

		// Save children.
		var r = record;	// Must be effectively final.
		var s = currentSession();
		update(s, record, value.conditions, record.getConditions(), "deleteConditionsByPerson", v -> new Conditions(r, v));
		update(s, record, value.exposures, record.getExposures(), "deleteExposuresByPerson", v -> new Exposures(r, v));
		update(s, record, value.symptoms, record.getSymptoms(), "deleteSymptomsByPerson", v -> new Symptoms(r, v));

		return value.withId(record.update(value).getId());
	}

	private int update(final Session s,
		final People record,
		final List<CreatedValue> values,
		final List<? extends PeopleChild> existing,
		final String deleteQuery,
		final Function<CreatedValue, ? extends PeopleChild> toEntity)
	{
		if (null == values) return 0;	// No change
		if (values.isEmpty()) return namedQueryX(deleteQuery).setParameter("personId", record.getId()).executeUpdate();

		// Add new items.
		var count = values.stream().filter(v -> {
				if (null == v) return false;
				var o = existing.stream().filter(e -> e.getChildId().equals(v.id)).findFirst().orElse(null);

				if (null == o) return true;

				v.withName(o.getChildName()).withCreatedAt(o.getCreatedAt());	// Denormalize supplied values.
				return false;
			})
			.peek(v -> s.persist(toEntity.apply(v)))
			.count();


		// Remove deleted items.
		return (int) (count + existing.stream().filter(e -> values.stream().noneMatch(v -> (null != v) && v.id.equals(e.getChildId()))).peek(e -> s.delete(e)).count());
	}

	/** Based on the phone number, the Person is retrieved and marked authenticated.
	 * 
	 * @param phone
	 * @return never NULL.
	 * @throws ObjectNotFoundException
	 */
	public PeopleValue authenticatedByPhone(final String phone) throws ObjectNotFoundException
	{
		var record = findByPhone(phone);
		if (null == record) throw new ObjectNotFoundException("Could not find the account with phone number '" + phone + "'.");

		return record.auth().toValue();
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
			.ensureLength("sexId", "Sex", value.sexId, PeopleValue.MAX_LEN_SEX_ID)
			.ensureLength("healthWorkerStatusId", "HealthWorkerStatusId", value.healthWorkerStatusId, PeopleValue.MAX_LEN_HEALTH_WORKER_STATUS_ID)
			.ensureLatitude("latitude", "Latitude", value.latitude)
			.ensureLongitude("longitude", "Longitude", value.longitude)
			.check();

		// Check enum values.
		if ((null != value.statusId) && (null == (value.status = PeopleStatus.get(value.statusId))))
			validator.add("statusId", "The Status ID '%s' is invalid.", value.statusId);

		if ((null != value.statureId) && (null == (value.stature = PeopleStature.get(value.statureId))))
			validator.add("statureId", "The Stature ID '%s' is invalid.", value.statureId);

		if ((null != value.sexId) && (null == (value.sex = Sex.get(value.sexId))))
			validator.add("sexId", "The Sex ID '%s' is invalid.", value.sexId);

		if ((null != value.healthWorkerStatusId) && (null == (value.healthWorkerStatus = HealthWorkerStatus.get(value.healthWorkerStatusId))))
			validator.add("healthWorkerStatusId", "The Health Worker Status ID '%s' is invalid.", value.healthWorkerStatusId);

		// Check children.
		if (CollectionUtils.isNotEmpty(value.conditions))
			value.conditions.stream().filter(v -> null != v).forEach(v -> validator.ensureExistsAndContains("conditions", "Condition", v.clean().id, Condition.VALUES));
		if (CollectionUtils.isNotEmpty(value.exposures))
			value.exposures.stream().filter(v -> null != v).forEach(v -> validator.ensureExistsAndContains("exposures", "Exposure", v.clean().id, Exposure.VALUES));
		if (CollectionUtils.isNotEmpty(value.symptoms))
			value.symptoms.stream().filter(v -> null != v).forEach(v -> validator.ensureExistsAndContains("symptoms", "Symptom", v.clean().id, Symptom.VALUES));

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
			throw new ObjectNotFoundException("Could not find the People because id '" + id + "' is invalid.");

		return record;
	}

	public boolean existsByEmail(final String email) { return (null != namedQuery("getPeopleIdByEmail", String.class).setParameter("email", email).uniqueResult()); } 
	public boolean existsByPhone(final String phone) { return (null != namedQuery("getPeopleIdByPhone", String.class).setParameter("phone", phone).uniqueResult()); }

	People find(final String name) { return namedQuery("findPeople").setParameter("name", name).uniqueResult(); }
	People findByEmail(final String email) { return namedQuery("findPeopleByEmail").setParameter("email", email).uniqueResult(); }
	People findByPhone(final String phone) { return namedQuery("findPeopleByPhone").setParameter("phone", phone).uniqueResult(); }
	List<People> findActiveByIdOrName(final String name) { return namedQuery("findActivePeopleByIdOrName").setParameter("name", name + "%").list(); }

	/** Checks for the existence of an account. Used to ensure account validity before attempting to authenticate.
	 *  Allow for authentication of inactive accounts which can be used to reactivate an account.
	 *
	 * @param phone
	 * @param email
	 * @throws ValidationException
	 */
	public void check(final String phone, final String email) throws ValidationException
	{
		if (null != phone)
		{
			if (existsByPhone(phone)) return;	// Success

			throw new ValidationException("phone", "The phone number '" + phone + "' is not associated with an existing user.");
		}

		if (null != email)
		{
			if (existsByEmail(email)) return;	// Success

			throw new ValidationException("email", "The email address '" + email + "' is not associated with an existing user.");
		}

		throw new ValidationException("Please provide a phone number.");
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

		return record.toValueX();
	}

	/** Gets a single People value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ValidationException if the identifier is valid.
	 */
	public PeopleValue getByIdWithException(final String id) throws ValidationException
	{
		return findWithException(id).toValueX();
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

		return v.withRecords(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> o.toValue()).collect(Collectors.toList()));
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
			.add("sexId", "o.sexId LIKE :sexId", filter.sexId)
			.addNotNull("o.sexId", filter.hasSexId)
			.addContains("healthWorkerStatusId", "o.healthWorkerStatusId LIKE :healthWorkerStatusId", filter.healthWorkerStatusId)
			.addNotNull("o.healthWorkerStatusId", filter.hasHealthWorkerStatusId)
			.add("latitude", "o.latitude = :latitude", filter.latitude)
			.addNotNull("o.latitude", filter.hasLatitude)
			.add("latitudeFrom", "o.latitude >= :latitudeFrom", filter.latitudeFrom)
			.add("latitudeTo", "o.latitude <= :latitudeTo", filter.latitudeTo)
			.add("longitude", "o.longitude = :longitude", filter.longitude)
			.addNotNull("o.longitude", filter.hasLongitude)
			.add("longitudeFrom", "o.longitude >= :longitudeFrom", filter.longitudeFrom)
			.add("longitudeTo", "o.longitude <= :longitudeTo", filter.longitudeTo)
			.add("alertable", "o.alertable = :alertable", filter.alertable)
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
			.add("updatedAtTo", "o.updatedAt <= :updatedAtTo", filter.updatedAtTo)
			.addIn("includeConditions", "EXISTS (SELECT 1 FROM Conditions c WHERE c.personId = o.id AND c.conditionId IN {})", filter.includeConditions)
			.addIn("excludeConditions", "NOT EXISTS (SELECT 1 FROM Conditions c WHERE c.personId = o.id AND c.conditionId IN {})", filter.excludeConditions)
			.addIn("includeExposures", "EXISTS (SELECT 1 FROM Exposures c WHERE c.personId = o.id AND c.exposureId IN {})", filter.includeExposures)
			.addIn("excludeExposures", "NOT EXISTS (SELECT 1 FROM Exposures c WHERE c.personId = o.id AND c.exposureId IN {})", filter.excludeExposures)
			.addIn("includeSymptoms", "EXISTS (SELECT 1 FROM Symptoms c WHERE c.personId = o.id AND c.symptomId IN {})", filter.includeSymptoms)
			.addIn("excludeSymptoms", "NOT EXISTS (SELECT 1 FROM Symptoms c WHERE c.personId = o.id AND c.symptomId IN {})", filter.excludeSymptoms);
	}
}
