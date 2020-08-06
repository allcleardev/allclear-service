package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.*;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.id.IdentifierGenerationException;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.hibernate.NativeQueryBuilder;
import app.allclear.common.value.CreatedValue;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.model.PeopleAuthRequest;
import app.allclear.platform.model.PeopleFindRequest;
import app.allclear.platform.type.*;
import app.allclear.platform.value.*;

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
	public static final int MAX_FACILITIES = 20;

	private static final String SELECT = "SELECT OBJECT(o) FROM People o";
	private static final String SELECT_NAMES = "SELECT o.id, o.name FROM people o";
	private static final String COUNT = "SELECT COUNT(o.id) FROM People o";
	private static final OrderByBuilder.Sort SORT_NAME = new OrderByBuilder.Sort("name", "o.name", "ASC", true, null);
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
		"locationName", ASC,
		"alertable", DESC,
		"active", DESC,
		"authAt", DESC,
		"phoneVerifiedAt", DESC,
		"emailVerifiedAt", DESC,
		"alertedOf", DESC,
		"alertedAt", DESC,
		"createdAt", DESC,
		"updatedAt", DESC);

	private static final String WHERE_HEALTH_WORKER_STATUS = "o.healthWorkerStatusId = :healthWorkerStatusId";
	private static final String WHERE_INCLUDE_CONDITIONS = "EXISTS (SELECT 1 FROM Conditions c WHERE c.personId = o.id AND c.conditionId IN {})";
	private static final String WHERE_EXCLUDE_CONDITIONS = "NOT EXISTS (SELECT 1 FROM Conditions c WHERE c.personId = o.id AND c.conditionId IN {})";
	private static final String WHERE_INCLUDE_EXPOSURES = "EXISTS (SELECT 1 FROM Exposures c WHERE c.personId = o.id AND c.exposureId IN {})";
	private static final String WHERE_EXCLUDE_EXPOSURES = "NOT EXISTS (SELECT 1 FROM Exposures c WHERE c.personId = o.id AND c.exposureId IN {})";
	private static final String WHERE_INCLUDE_SYMPTOMS = "EXISTS (SELECT 1 FROM Symptoms c WHERE c.personId = o.id AND c.symptomId IN {})";
	private static final String WHERE_EXCLUDE_SYMPTOMS = "NOT EXISTS (SELECT 1 FROM Symptoms c WHERE c.personId = o.id AND c.symptomId IN {})";

	private static final String NOT_ME = "<> '" + Visibility.ME.id + "'";
	private static final String FRIEND_HEALTH_WORKER_STATUS = WHERE_HEALTH_WORKER_STATUS + " AND fa.visibilityHealthWorkerStatusId " + NOT_ME;
	private static final String FRIEND_INCLUDE_CONDITIONS = WHERE_INCLUDE_CONDITIONS + " AND fa.visibilityConditions " + NOT_ME;
	private static final String FRIEND_EXCLUDE_CONDITIONS = WHERE_EXCLUDE_CONDITIONS + " AND fa.visibilityConditions " + NOT_ME;
	private static final String FRIEND_INCLUDE_EXPOSURES = WHERE_INCLUDE_EXPOSURES + " AND fa.visibilityExposures " + NOT_ME;
	private static final String FRIEND_EXCLUDE_EXPOSURES = WHERE_EXCLUDE_EXPOSURES + " AND fa.visibilityExposures " + NOT_ME;
	private static final String FRIEND_INCLUDE_SYMPTOMS = WHERE_INCLUDE_SYMPTOMS + " AND fa.visibilitySymptoms " + NOT_ME;
	private static final String FRIEND_EXCLUDE_SYMPTOMS = WHERE_EXCLUDE_SYMPTOMS + " AND fa.visibilitySymptoms " + NOT_ME;

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
	 * @param admin
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public PeopleValue add(final PeopleValue value, final boolean admin) throws ValidationException
	{
		_validate(value.withId(generateId()));
		var record = persist(new People(value));

		// Save children.
		var s = currentSession();
		s.persist(new PeopleField(record, value.createdAt));	// Create the default Field access record when added.
		add(s, value.conditions, v -> new Conditions(record, v));
		add(s, value.exposures, v -> new Exposures(record, v));
		add(s, value.symptoms, v -> new Symptoms(record, v));
		add(s, value.symptoms, v -> new SymptomsLog(record, v));

		if (admin)
		{
			if (CollectionUtils.isNotEmpty(value.associations))
			{
				var added = new HashSet<Long>(value.associations.size());
				value.associations.stream()
					.filter(v -> (null != v) && (null != v.id) && !added.contains(v.id))
					.map(v -> s.get(Facility.class, v.id))
					.filter(o -> null != o)
					.forEach(o -> { s.persist(new FacilityPeople(o, record, value.createdAt)); added.add(o.getId()); });
			}
		}

		return value.withId(record.getId());
	}

	/** Defaults to non-admin. */
	public PeopleValue add(final PeopleValue value) throws ValidationException
	{
		return add(value, false);
	}

	private void add(final Session s, final List<CreatedValue> values, final Function<CreatedValue, ? extends PeopleChild> toEntity)
	{
		if (CollectionUtils.isEmpty(values)) return;
		values.stream().filter(v -> null != v).forEach(v -> s.persist(toEntity.apply(v)));
	}

	/** Associates one or more facilities with a person.
	 * 
	 * @param id person identifier
	 * @param facilityIds
	 * @return number of associations added.
	 * @throws ObjectNotFoundException
	 * @throws ValidationException
	 */
	public int addFacilities(final String id, final List<Long> facilityIds) throws ObjectNotFoundException, ValidationException
	{
		if (CollectionUtils.isEmpty(facilityIds)) throw new ValidationException("facilityIds", "Please provide one or more facilities to bookmark.");

		var now = new Date();
		var s = currentSession();
		var validator = new Validator();
		var record = findWithException(id);
		var existingIds = new HashSet<>(namedQuery("getFacilityIdsByPerson", Long.class).setParameter("personId", id).list());	// Needs to be modifiable.
		var updatable = facilityIds.stream()
			.filter(i -> null != i)
			.filter(i -> !existingIds.contains(i))
			.map(i -> {
				var o = s.get(Facility.class, i);
				if (null == o) validator.add("facilityId", "The Facility ID '%d' is invalid.", i);

				return o;
			})
			.filter(o -> (null != o))
			.peek(o -> existingIds.add(o.getId()))
			.collect(toList());

		validator.check();	// Were there any invalid facility IDs.

		int size = updatable.size();
		if (MAX_FACILITIES < (size + existingIds.size()))
			validator.add("facilityIds", "You may only have a total of %d bookmarked facilities.", MAX_FACILITIES).check();

		updatable.forEach(o -> s.persist(new PeopleFacility(record, o, now)));

		return updatable.size();
	}

	/** Updates a single People value.
	 *
	 * @param value
	 * @param admin is the current user an administrator.
	 * @throws ValidationException
	 */
	public PeopleValue update(final PeopleValue value, final boolean admin) throws ValidationException
	{
		var cmrs = _validate(value);
		var record = (People) cmrs[0];
		if (null == record) record = findWithException(value.id);

		// Save children.
		var r = record;	// Must be effectively final.
		var s = currentSession();
		update(s, record, value.conditions, record.getConditions(), "deleteConditionsByPerson", v -> new Conditions(r, v));
		update(s, record, value.exposures, record.getExposures(), "deleteExposuresByPerson", v -> new Exposures(r, v));
		update(s, record, value.symptoms, record.getSymptoms(), v -> new Symptoms(r, v), v -> new SymptomsLog(r, v), "updateSymptomsLog");

		if (admin)
		{
			if (null != value.associations)
			{
				if (value.associations.isEmpty()) namedQueryX("deleteFacilityPeopleByPerson").setParameter("personId", record.getId()).executeUpdate();
				else
				{
					var existing = r.getAssociations();
					var existingIds = existing.stream().map(o -> o.getFacilityId()).collect(toSet());
					value.associations.stream()
						.filter(v -> (null != v) && (null != v.id) && !existingIds.contains(v.id))
						.map(v -> s.get(Facility.class, v.id))
						.filter(o -> null != o)
						.forEach(o -> { s.persist(new FacilityPeople(o, r, value.createdAt)); existingIds.add(o.getId()); });
	
					existing.stream()
						.filter(o -> value.associations.stream().filter(v -> null != v).noneMatch(v -> o.getFacilityId().equals(v.id)))
						.forEach(o -> s.delete(o));
				}
			}
		}

		return value.withId(record.update(value, admin).getId());
	}

	public PeopleValue update(final PeopleValue value) { return update(value, true); }

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

	private int update(final Session s,
		final People record,
		final List<CreatedValue> values,
		final List<? extends PeopleChild> existing,
		final Function<CreatedValue, ? extends PeopleChild> toEntity,
		final Function<CreatedValue, ? extends PeopleChild> toLogEntity,
		final String updateQuery)
	{
		if (null == values) return 0;	// No change
		// if (values.isEmpty()) return namedQueryX(deleteQuery).setParameter("personId", record.getId()).executeUpdate(); MUST delete each symptom one at a time to log change. DLS on 4/9/2020.

		// Add new items.
		var count = values.stream().filter(v -> {
				if (null == v) return false;
				var o = existing.stream().filter(e -> e.getChildId().equals(v.id)).findFirst().orElse(null);

				if (null == o) return true;

				v.withName(o.getChildName()).withCreatedAt(o.getCreatedAt());	// Denormalize supplied values.
				return false;
			})
			.peek(v -> {
				s.persist(toEntity.apply(v));
				s.persist(toLogEntity.apply(v));
			})
			.count();


		// Remove deleted items.
		var update = namedQueryX(updateQuery);
		return (int) (count + existing.stream()
			.filter(e -> values.stream().noneMatch(v -> (null != v) && v.id.equals(e.getChildId())))
			.peek(e -> {
				update.setParameter("personId", e.getPersonId()).setParameter("childId", e.getChildId()).setParameter("createdAt", e.getCreatedAt()).executeUpdate();
				s.delete(e);
			})
			.count());
	}

	/** Updates the specified user's field access settings.
	 * 
	 * @param value
	 * @return the supplied value.
	 * @throws ValidationException
	 */
	public PeopleFieldValue update(final PeopleFieldValue value) throws ObjectNotFoundException, ValidationException
	{
		value.clean();
		var validator = new Validator();

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExistsAndLength("id", "ID", value.id, PeopleFieldValue.MAX_LEN_ID)
			.ensureExistsAndContains("visibilityHealthWorkerStatusId", "Visibility Health Worker Status", value.visibilityHealthWorkerStatusId, Visibility.VALUES)
			.ensureExistsAndContains("visibilityConditions", "Visibility Conditions", value.visibilityConditions, Visibility.VALUES)
			.ensureExistsAndContains("visibilityExposures", "Visibility Exposures", value.visibilityExposures, Visibility.VALUES)
			.ensureExistsAndContains("visibilitySymptoms", "Visibility Symptoms", value.visibilitySymptoms, Visibility.VALUES)
			.check();

		return findFieldWithException(value.id).update(value);
	}

	/** Authenticates a person with standard credentials. Optionally, it can change their password, too.
	 *  When authenticating for the first time, the person MUST provide a new password.
	 *  
	 * @param request
	 * @return the authenticated person
	 * @throws ValidationException
	 */
	public PeopleValue authenticate(final PeopleAuthRequest request) throws ValidationException
	{
		var validator = new Validator()
			.ensureExists("name", "Name", request.name)
			.ensureExists("password", "Password", request.password)
			.check();

		// Find the person by ID, name, phone, or email.
		var record = findOne(request.name);
		if (null == record)
			throw new ValidationException(Validator.CODE_INVALID_LOGIN_ID, "name", "Invalid credentials");

		// If the user has previously logged in, then don't force the user to change their password.
		if ((null != record.getAuthAt()) && (null == request.newPassword))
		{
			if (record.auth(request.password)) return record.toValue();

			throw new ValidationException(Validator.CODE_INVALID_CREDENTIALS, "password", "Invalid credentials");
		}

		// Before changing the user's password, check their credentials.
		if (!record.checkPassword(request.password))
			throw new ValidationException(Validator.CODE_INVALID_CREDENTIALS, "password", "Invalid credentials");

		// If a new password has been supplied, change it.
		if (null != request.newPassword)
		{
			validator.ensureLength("newPassword", "New Password", request.newPassword, PeopleValue.MIN_LEN_PASSWORD, PeopleValue.MAX_LEN_PASSWORD)
				.ensureExists("confirmPassword", "Password Confirmation", request.confirmPassword)
				.check();

			if (!request.newPassword.equals(request.confirmPassword))
				throw new ValidationException(Validator.CODE_NEW_PASSWORD_MISMATCH, "confirmPassword", "The 'Password Confirmation' does not match the 'New Password'.");

			return record.updatePassword(request.newPassword).toValue();
		}

		// The password supplied is valid BUT since the user hasn't authenticated yet, the user must change their password.
		throw new ValidationException(Validator.CODE_MUST_CHANGE_PASSWORD, "newPassword", "Please change your password.");
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

		return record.auth().toValue();	// MUST get the full profile. Needed for Facility restrictions. DLS on 4/12/2020.
	}

	/** Validates a single People value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public PeopleValue validate(final PeopleValue value) throws ValidationException
	{
		_validate(value);

		return value;
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
			.ensureLength("password", "Password", value.password, PeopleValue.MIN_LEN_PASSWORD, PeopleValue.MAX_LEN_PASSWORD)
			.ensureLength("firstName", "First Name", value.firstName, PeopleValue.MAX_LEN_FIRST_NAME)
			.ensureLength("lastName", "Last Name", value.lastName, PeopleValue.MAX_LEN_LAST_NAME)
			.ensureLength("statusId", "Status", value.statusId, PeopleValue.MAX_LEN_STATUS_ID)
			.ensureLength("statureId", "Stature", value.statureId, PeopleValue.MAX_LEN_STATURE_ID)
			.ensureLength("sexId", "Sex", value.sexId, PeopleValue.MAX_LEN_SEX_ID)
			.ensureExistsAndLength("healthWorkerStatusId", "HealthWorkerStatusId", value.healthWorkerStatusId, PeopleValue.MAX_LEN_HEALTH_WORKER_STATUS_ID)
			.ensureLatitude("latitude", "Latitude", value.latitude)
			.ensureLongitude("longitude", "Longitude", value.longitude)
			.ensureLength("locationName", "Location Name", value.locationName, PeopleValue.MAX_LEN_LOCATION_NAME)
			.check();

		// Check enum values.
		if ((null != value.statusId) && (null == (value.status = PeopleStatus.get(value.statusId))))
			validator.add("statusId", "The Status ID '%s' is invalid.", value.statusId);

		if ((null != value.statureId) && (null == (value.stature = Stature.get(value.statureId))))
			validator.add("statureId", "The Stature ID '%s' is invalid.", value.statureId);

		if ((null != value.sexId) && (null == (value.sex = Sex.get(value.sexId))))
			validator.add("sexId", "The Sex ID '%s' is invalid.", value.sexId);

		if (null == (value.healthWorkerStatus = HealthWorkerStatus.get(value.healthWorkerStatusId)))
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

	/** Switch on the ACTIVE field on the user associated with the Person ID.
	 * 
	 * @param id
	 * @return TRUE if the user is found and is currently inactive.
	 * @throws ObjectNotFoundException
	 */
	public boolean activate(final String id) throws ObjectNotFoundException
	{
		var record = findWithException(id);

		if (record.isActive()) return false;

		record.setActive(true);
		record.setUpdatedAt(new Date());

		return true;
	}

	/** Switch on the ALERTABLE field on the user associated with the phone.
	 * 
	 * @param phone
	 * @return TRUE if the user is found and is currently unalertable.
	 * @throws ObjectNotFoundException
	 */
	public boolean alertByPhone(final String phone) throws ObjectNotFoundException
	{
		var record = findByPhone(phone);
		if (null == record) throw new ObjectNotFoundException("The Phone Number '" + phone + "' could not be found.");

		if (record.isAlertable()) return false;

		record.setAlertable(true);
		record.setUpdatedAt(new Date());

		return true;
	}

	/** Switch off the ALERTABLE field on the user associated with the phone.
	 * 
	 * @param phone
	 * @return TRUE if the user is found and is currently alertable.
	 * @throws ObjectNotFoundException
	 */
	public boolean unalertByPhone(final String phone) throws ObjectNotFoundException
	{
		var record = findByPhone(phone);
		if (null == record) throw new ObjectNotFoundException("The Phone Number '" + phone + "' could not be found.");

		if (!record.isAlertable()) return false;

		record.setAlertable(false);
		record.setUpdatedAt(new Date());

		return true;
	}

	/** Removes one or more facility associations from a person.
	 * 
	 * @param id person identifier
	 * @param facilityIds
	 * @return number of associations removed.
	 * @throws ObjectNotFoundException
	 * @throws ValidationException
	 */
	public int removeFacilities(final String id, final List<Long> facilityIds) throws ObjectNotFoundException, ValidationException
	{
		if (CollectionUtils.isEmpty(facilityIds)) return 0;

		var s = currentSession();
		var query = namedQuery("findPeopleFacility", PeopleFacility.class).setParameter("personId", id);
		return (int) facilityIds.stream()
			.filter(i -> null != i)
			.map(i -> query.setParameter("facilityId", i).uniqueResult())
			.filter(o -> null != o)
			.peek(o -> s.delete(o))
			.count();
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
	People findFriend(final String personId, final String friendId)
	{
		return namedQuery("findPeopleViaFriendship")
			.setParameter("personId", personId)
			.setParameter("friendId", friendId)
			.uniqueResult();
	}

	People findOne(final String name) { return namedQuery("findActivePersonByIdOrName").setParameter("name", name).setMaxResults(1).uniqueResult(); }
	List<People> findActiveByIdOrName(final String name) { return namedQuery("findActivePeopleByIdOrName").setParameter("name", name + "%").list(); }

	PeopleField findField(final String id) { return currentSession().get(PeopleField.class, id); }
	PeopleField findFieldWithException(final String id) throws ObjectNotFoundException
	{
		var o = findField(id);
		if (null != o) return o;

		throw new ObjectNotFoundException("The People Field '" + id + "' is invalid.");
	}

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

	/** Gets a single People entity by phone number. If NOT found, add an inactive new member.
	 * 
	 * @param phone
	 * @param firstName
	 * @param lastName
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public People getOrAdd(final String phone, final String firstName, final String lastName) throws ValidationException
	{
		var record = findByPhone(phone);
		if (null != record) return record;

		return persist(new People(validate(new PeopleValue(phone, firstName, lastName).withId(generateId()))));
	}

	/** Gets the identifier of a person by their phone number.
	 * 
	 * @param phone
	 * @return NULL if not found.
	 */
	public String getIdByPhone(final String phone)
	{
		var record = findByPhone(phone);

		return (null != record) ? record.getId() : null;
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

		return record.toValue();
	}

	/** Gets a single People value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ValidationException if the identifier is valid.
	 */
	public PeopleValue getByIdWithException(final String id) throws ValidationException
	{
		return findWithException(id).toValue();
	}

	/** Gets a single Friend of the specified Person.
	 * 
	 * @param personId
	 * @param friendId
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the friend is not a user or not a friend of the person.
	 */
	public PeopleValue getFriend(final String personId, final String friendId) throws ObjectNotFoundException
	{
		var o = findFriend(personId, friendId);
		if (null == o) throw new ObjectNotFoundException("Could not find friend '" + friendId + "'.");

		return o.toValueX(Visibility.FRIENDS);
	}

	/** Gets a single Person Field Access value for the specified Person.
	 * 
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException
	 */
	public PeopleFieldValue getFieldWithException(final String id) throws ObjectNotFoundException
	{
		return findFieldWithException(id).toValue();
	}

	/** Gets a list of People IDs that are active, alertable, and within a specific timezone.
	 * 
	 * @param lastId
	 * @param zone
	 * @param pageSize
	 * @return never NULL
	 */
	public List<String> getActiveAlertableIdsByLongitude(final String lastId, final Timezone zone, final int pageSize)
	{
		return namedQuery("getActiveAlertablePeopleIdsByLongitude", String.class)
			.setParameter("lastId", lastId)
			.setParameter("longitudeFrom", zone.longitudeFrom)
			.setParameter("longitudeTo", zone.longitudeTo)
			.setMaxResults(pageSize)
			.list();
	}

	/** Gets a list of active People by wildcard ID and/or name search.
	 * 
	 * @param name
	 * @return never NULL.
	 */
	public List<PeopleValue> getActiveByIdOrName(final String name)
	{
		return findActiveByIdOrName(name).stream().map(o -> o.toValue(Visibility.ME)).collect(toList());
	}

	/** Finds a list of People names by name or phone number.
	 * 
	 * @param request
	 * @return never NULL
	 * @throws ValidationException if the request does not contain any filter parameters.
	 */
	public List<Named> find(final PeopleFindRequest request) throws ValidationException
	{
		if (!request.valid()) throw new ValidationException("Please provide at least one name or one phone number.");

		return new NativeQueryBuilder<Named>(currentSession(), SELECT_NAMES, Named.class, "o")
			.addIn("names", "o.name IN {}", request.names)
			.addIn("phones", "o.phone IN {}", request.phones)
			.add("active", "o.active = :active", true)
			.orderBy(SORT_NAME)
			.run();
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

		return v.withRecords(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> o.toValue(filter.who())).collect(toList()));
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
		var friends = Visibility.FRIENDS.equals(filter.who());
		var whereHealthWorkerStatus = WHERE_HEALTH_WORKER_STATUS;
		var whereIncludeConditions = WHERE_INCLUDE_CONDITIONS;
		var whereExcludeConditions = WHERE_EXCLUDE_CONDITIONS;
		var whereIncludeExposures = WHERE_INCLUDE_EXPOSURES;
		var whereExcludeExposures = WHERE_EXCLUDE_EXPOSURES;
		var whereIncludeSymptoms = WHERE_INCLUDE_SYMPTOMS;
		var whereExcludeSymptoms = WHERE_EXCLUDE_SYMPTOMS;
	
		if (friends)
		{
			whereHealthWorkerStatus = FRIEND_HEALTH_WORKER_STATUS;
			whereIncludeConditions = FRIEND_INCLUDE_CONDITIONS;
			whereExcludeConditions = FRIEND_EXCLUDE_CONDITIONS;
			whereIncludeExposures = FRIEND_INCLUDE_EXPOSURES;
			whereExcludeExposures = FRIEND_EXCLUDE_EXPOSURES;
			whereIncludeSymptoms = FRIEND_INCLUDE_SYMPTOMS;
			whereExcludeSymptoms = FRIEND_EXCLUDE_SYMPTOMS;
		}
		
		var o = createQueryBuilder(select)
			.addStarts("id", "o.id LIKE :id", filter.id)
			.addStarts("name", "o.name LIKE :name", filter.name)
			.addContains("nameX", "o.name LIKE :nameX", filter.nameX)
			.addStarts("phone", "o.phone LIKE :phone", filter.phone)
			.addStarts("email", "o.email LIKE :email", filter.email)
			.addNotNull("o.email", filter.hasEmail)
			.addNotNull("o.password", filter.hasPassword)
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
			.add("sexId", "o.sexId = :sexId", filter.sexId)
			.addNotNull("o.sexId", filter.hasSexId)
			.add("healthWorkerStatusId", whereHealthWorkerStatus, filter.healthWorkerStatusId)
			.add("latitude", "o.latitude = :latitude", filter.latitude)
			.addNotNull("o.latitude", filter.hasLatitude)
			.add("latitudeFrom", "o.latitude >= :latitudeFrom", filter.latitudeFrom)
			.add("latitudeTo", "o.latitude <= :latitudeTo", filter.latitudeTo)
			.add("longitude", "o.longitude = :longitude", filter.longitude)
			.addNotNull("o.longitude", filter.hasLongitude)
			.add("longitudeFrom", "o.longitude >= :longitudeFrom", filter.longitudeFrom)
			.add("longitudeTo", "o.longitude <= :longitudeTo", filter.longitudeTo)
			.addContains("locationName", "o.locationName LIKE :locationName", filter.locationName)
			.addNotNull("o.locationName", filter.hasLocationName)
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
			.add("alertedOf", "o.alertedOf = :alertedOf", filter.alertedOf)
			.addNotNull("o.alertedOf", filter.hasAlertedOf)
			.add("alertedOfFrom", "o.alertedOf >= :alertedOfFrom", filter.alertedOfFrom)
			.add("alertedOfTo", "o.alertedOf <= :alertedOfTo", filter.alertedOfTo)
			.addNotNull("o.alertedAt", filter.hasAlertedAt)
			.add("alertedAtFrom", "o.alertedAt >= :alertedAtFrom", filter.alertedAtFrom)
			.add("alertedAtTo", "o.alertedAt <= :alertedAtTo", filter.alertedAtTo)
			.add("createdAtFrom", "o.createdAt >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo)
			.add("updatedAtFrom", "o.updatedAt >= :updatedAtFrom", filter.updatedAtFrom)
			.add("updatedAtTo", "o.updatedAt <= :updatedAtTo", filter.updatedAtTo)
			.add("friendId", "iv.personId = :friendId", filter.friendId, "INNER JOIN o.invitees iv")	// Do NOT need to filter out rejected invitation as only admins can call.
			.add("inviteeId", "fr.inviteeId = :inviteeId", filter.inviteeId, "INNER JOIN o.friends fr")
			.add("friendshipId", "fs.friendId = :friendshipId", filter.friendshipId, "INNER JOIN o.friendships fs")
			.addIn("includeConditions", whereIncludeConditions, filter.includeConditions)
			.addIn("excludeConditions", whereExcludeConditions, filter.excludeConditions)
			.addIn("includeExposures", whereIncludeExposures, filter.includeExposures)
			.addIn("excludeExposures", whereExcludeExposures, filter.excludeExposures)
			.addIn("includeSymptoms", whereIncludeSymptoms, filter.includeSymptoms)
			.addIn("excludeSymptoms", whereExcludeSymptoms, filter.excludeSymptoms)
			.addExists("SELECT 1 FROM Tests t WHERE t.personId = o.id", filter.hasTakenTest)
			.addExists("SELECT 1 FROM Tests t WHERE t.personId = o.id AND t.positive = TRUE", filter.hasPositiveTest)
			.addExists("SELECT 1 FROM PeopleFacility c WHERE c.personId = o.id", filter.hasFacilities)
			.addIn("includeFacilities", "EXISTS (SELECT 1 FROM PeopleFacility c WHERE c.personId = o.id AND c.facilityId IN {})", filter.includeFacilities)
			.addIn("excludeFacilities", "NOT EXISTS (SELECT 1 FROM PeopleFacility c WHERE c.personId = o.id AND c.facilityId IN {})", filter.excludeFacilities)
			.addExists("SELECT 1 FROM FacilityPeople d WHERE d.personId = o.id", filter.hasAssociations)
			.addIn("includeAssociations", "EXISTS (SELECT 1 FROM FacilityPeople d WHERE d.personId = o.id AND d.facilityId IN {})", filter.includeAssociations)
			.addIn("excludeAssociations", "NOT EXISTS (SELECT 1 FROM FacilityPeople d WHERE d.personId = o.id AND d.facilityId IN {})", filter.excludeAssociations)
			.addContains("visibilityHealthWorkerStatusId", "fa.visibilityHealthWorkerStatusId LIKE :visibilityHealthWorkerStatusId", filter.visibilityHealthWorkerStatusId, "INNER JOIN o.field fa")
			.addContains("visibilityConditions", "fa.visibilityConditions LIKE :visibilityConditions", filter.visibilityConditions, "INNER JOIN o.field fa")
			.addContains("visibilityExposures", "fa.visibilityExposures LIKE :visibilityExposures", filter.visibilityExposures, "INNER JOIN o.field fa")
			.addContains("visibilitySymptoms", "fa.visibilitySymptoms LIKE :visibilitySymptoms", filter.visibilitySymptoms, "INNER JOIN o.field fa");

		if (friends || (null != select)) o.joins("INNER JOIN o.field fa");

		return o;
	}
}
