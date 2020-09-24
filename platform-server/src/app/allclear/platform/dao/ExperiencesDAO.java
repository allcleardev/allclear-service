package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.hibernate.HibernateQueryBuilder;
import app.allclear.common.time.CalendarUtils;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.ExperiencesFilter;
import app.allclear.platform.model.ExperiencesCalcResponse;
import app.allclear.platform.type.Experience;
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
		"createdAt", DESC,
		"updatedAt", DESC);

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
		var record = persist(new Experiences(value, cmrs));

		if (CollectionUtils.isNotEmpty(value.tags))
		{
			var s = currentSession();
			value.tags.stream().filter(v -> null != v).forEach(v -> s.persist(new ExperiencesTag(record, v)));
		}

		return value.withId(record.getId());
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

		if (null != value.tags)
		{
			var s = currentSession();
			if (value.tags.isEmpty())
				s.getNamedQuery("deleteExperiencesTagsById").setParameter("id", record.getId()).executeUpdate();
			else
			{
				var r = record;	// MUST be effectively final.
				var children = record.getTags();
				var set = children.stream().map(o -> o.getTagId()).collect(toSet());
				value.tags.stream().filter(v -> null != v).filter(v -> !set.contains(v.getId()))	// Add new tags.
					.forEach(v -> {
						s.persist(new ExperiencesTag(r, v));
						set.add(v.getId());
					});
				children.stream()	// Remove children not in the supplied list.
					.filter(o -> value.tags.stream().noneMatch(v -> (null != v) && o.getTagId().equals(v.getId())))
					.forEach(o -> s.delete(o));
			}
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

		// Check children.
		if (CollectionUtils.isNotEmpty(value.tags))
			value.tags.stream().filter(v -> null != v).forEach(v -> validator.ensureExistsAndContains("tags", "Tags", v.clean().getId(), Experience.VALUES));

		// Throw exception if errors exist.
		validator.check();

		// Checks for an existing experience on adds.
		if ((null == value.id) && (0L < countTodayExperiencesByPerson(value.personId, value.facilityId)))
			validator.add("facilityId", "You have already provided an Experience for %s today.", facility.getName()).check();

		value.denormalize(person.getName(), facility.getName());

		return new Object[] { null, person, facility };
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

	Map<Boolean, Long> calcPositivesByFacility(final Long facilityId)
	{
		return namedQuery("countExperiencesPositivesByFacility", CountByBoolean.class)
			.setParameter("facilityId", facilityId)
			.stream()
			.collect(toMap(o -> o.id, o -> o.total));
	}

	Map<String, CountByNameAndDate> calcTagsByFacility(final Long facilityId)
	{
		return namedQuery("countExperiencesTagsByFacility", CountByNameAndDate.class)
			.setParameter("facilityId", facilityId)
			.stream()
			.collect(toMap(o -> o.name, o -> o));
	}

	/** Aggregates the Experiences facet data for a single Facility.
	 * 
	 * @param facilityId
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public ExperiencesCalcResponse calcByFacility(final Long facilityId) throws ValidationException
	{
		if (null == facilityId) throw new ValidationException("facilityId", "Must provide a Facility identifier.");

		var positives = calcPositivesByFacility(facilityId);
		if (positives.isEmpty()) return new ExperiencesCalcResponse();

		var tags = calcTagsByFacility(facilityId);

		return new ExperiencesCalcResponse(positives.getOrDefault(true, 0L),
			positives.getOrDefault(false, 0L),
			Experience.LIST.stream().collect(toMap(v -> v.id, v -> {
				var t = tags.getOrDefault(v.id, CountByNameAndDate.EMPTY);
				return new ExperiencesCalcResponse.Tag(v.name, t.total, t.last);
			})));
	}

	/** Counts the number of experiences by the person for the specified facility today.
	 * 
	 * @param personId
	 * @param facilityId
	 * @return zero if none found.
	 */
	public long countTodayExperiencesByPerson(final String personId, final Long facilityId)
	{
		return countRecentExperiencesByPerson(personId, facilityId, CalendarUtils.today().getTime());
	}

	/** Counts the number of experiences by the person for the specified facility since the specified creation date.
	 * 
	 * @param personId
	 * @param facilityId
	 * @param createdAt
	 * @return zero if none found.
	 */
	public long countRecentExperiencesByPerson(final String personId, final Long facilityId, final Date createdAt)
	{
		return namedQuery("countRecentExperiencesByPerson", Long.class)
			.setParameter("personId", personId)
			.setParameter("facilityId", facilityId)
			.setParameter("createdAt", createdAt)
			.uniqueResult();
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
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo)
			.add("updatedAtFrom", "o.updatedAt >= :updatedAtFrom", filter.updatedAtFrom)
			.add("updatedAtTo", "o.updatedAt <= :updatedAtTo", filter.updatedAtTo)
			.addIn("includeTags", "EXISTS (SELECT 1 FROM ExperiencesTag t WHERE t.experienceId = o.id AND t.tagId IN {})", filter.includeTags)
			.addIn("excludeTags", "NOT EXISTS (SELECT 1 FROM ExperiencesTag t WHERE t.experienceId = o.id AND t.tagId IN {})", filter.excludeTags);
	}

	private List<ExperiencesValue> cmr(final List<ExperiencesValue> values)
	{
		if (CollectionUtils.isEmpty(values)) return values;

		var ids = values.stream().map(v -> v.id).collect(toList());
		var people = namedQuery("getPeopleNamesByIds", Named.class)
			.setParameterList("ids", values.stream().map(v -> v.personId).distinct().collect(toList()))
			.stream().collect(toMap(o -> o.id, o -> o.name));
		var facilities = namedQuery("getFacilityNamesByIds", Name.class)
			.setParameterList("ids", values.stream().map(v -> v.facilityId).distinct().collect(toList()))
			.stream().collect(toMap(o -> o.id, o -> o.name));
		var tags = namedQuery("findExperiencesTagsByIds", ExperiencesTag.class)
			.setParameterList("ids", ids)
			.stream().collect(groupingBy(o -> o.getExperienceId(), mapping(o -> o.toValue(), toList())));

		values.forEach(v -> v.denormalize(people.get(v.personId), facilities.get(v.facilityId)).withTags(tags.get(v.id)));

		return values;
	}
}
