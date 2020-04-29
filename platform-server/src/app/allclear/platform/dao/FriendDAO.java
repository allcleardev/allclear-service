package app.allclear.platform.dao;

import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.slf4j.*;

import app.allclear.common.dao.*;
import app.allclear.common.errors.*;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.hibernate.HibernateQueryBuilder;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.FriendFilter;
import app.allclear.platform.value.FriendValue;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Data access object that handles access to the Friend entity.
*
*	@author smalleyd
*	@version 1.1.9
*	@since April 27, 2020
*
**********************************************************************************/

public class FriendDAO extends AbstractDAO<Friend>
{
	private static final Logger log = LoggerFactory.getLogger(FriendDAO.class);

	private static final String SELECT = "SELECT OBJECT(o) FROM Friend o";
	private static final String COUNT = "SELECT COUNT(o.personId) FROM Friend o";
	private static final OrderByBuilder ORDER = new OrderByBuilder('o', 
		"personId", ASC,
		"personName", ASC + ",p.name;INNER JOIN o.person p",
		"inviteeId", ASC,
		"inviteeName", ASC + ",i.name;INNER JOIN o.invitee p",
		"acceptedAt", DESC,
		"rejectedAt", DESC,
		"createdAt", DESC);

	public FriendDAO(final SessionFactory factory)
	{
		super(factory);
	}

	/** Adds a single Friend value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public FriendValue add(final FriendValue value) throws ValidationException
	{
		validate(value);
		if (null != find(value)) throw new ValidationException("The friendship request already exists.");

		persist(new Friend(value));

		return value;
	}

	/** Updates a single Friend value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public FriendValue update(final FriendValue value) throws ValidationException
	{
		validate(value);

		return findWithException(value.personId, value.inviteeId).update(value);
	}

	/** Starts a friendship request with the specified user.
	 * 
	 * @param person
	 * @param inviteeId
	 * @return never NULL
	 * @throws ValidationException
	 */
	public FriendValue start(final PeopleValue person, final String inviteeId) throws ValidationException
	{
		var value = validate(new FriendValue(person, inviteeId));

		var record = find(value);
		if (null != record)
		{
			if (record.rejected()) throw new ValidationException("The friendship request cannot be issued.");	// Do NOT allow a user to request a friendship if the invitee has already previously rejected. DLS on 4/28/2020.
			if (record.accepted()) throw new ValidationException("The friendship request has already been accepted.");

			return record.toValue();	// Already exists.
		}

		persist(new Friend(value));

		return value;
	}

	/** Accepts a friendship request to the current user.
	 * 
	 * @param invitee
	 * @param personId
	 * @return the new permanent Friendship value.
	 * @throws ValidationException
	 */
	public FriendValue accept(final PeopleValue invitee, final String personId) throws ValidationException
	{
		var record = find(personId, invitee.id);
		if (null == record) throw new ValidationException("The friendship request does not exist.");
		if (record.accepted()) throw new ValidationException("The friendship request has already been accepted.");
		if (record.rejected()) log.warn("User {} is accepting a friendship request by '{}' that he/she had previously rejected.", invitee, record.getPersonId());

		// TODO: create the permanent FRIENDSHIP record. DLS on 4/28/2020.
		return record.accept().toValue();
	}

	/** Rejects a friendship request to the current user.
	 * 
	 * @param invitee
	 * @param personId
	 * @return the existing Friend value.
	 * @throws ValidationException
	 */
	public FriendValue reject(final PeopleValue invitee, final String personId) throws ValidationException
	{
		var record = find(personId, invitee.id);
		if (null == record) throw new ValidationException("The friendship request does not exist.");
		if (record.rejected()) throw new ValidationException("The friendship request has already been rejected.");

		// TODO: if accepted, remove the permanent FRIENDSHIP record too before rejecting. Should be returned from the "Friend.reject" method.
		return record.reject().toValue();
	}

	/** Validates a single Friend value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public FriendValue validate(final FriendValue value) throws ValidationException
	{
		value.clean();
		var validator = new Validator();

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExistsAndLength("personId", "Person", value.personId, FriendValue.MAX_LEN_PERSON_ID)
			.ensureExistsAndLength("inviteeId", "Invitee", value.inviteeId, FriendValue.MAX_LEN_INVITEE_ID)
			.check();

		if (value.personId.equals(value.inviteeId)) validator.add("inviteId", "You cannot send a friend request to yourself.").check();

		// Validation foreign keys.
		var session = currentSession();
		var person = session.get(People.class, value.personId);
		var invitee = session.get(People.class, value.inviteeId);
		if ((null == person) || !person.isActive()) validator.add("personId", "The Person ID, %s, is invalid.", value.personId);
		if ((null == invitee) || !invitee.isActive()) validator.add("inviteeId", "The Invitee ID, %s, is invalid.", value.inviteeId);

		// Throw exception if errors exist.
		validator.check();

		// Populate CMR fields.
		value.withPersonName(person.getName()).withInviteeName(invitee.getName());

		return value;
	}

	boolean remove(final FriendValue value) throws ValidationException
	{
		return remove(value.personId, value.inviteeId);
	}

	/** Removes a single Friend value.
	 *
	 * @param personId
	 * @param inviteeId
	 * @return TRUE if the entity is found and removed.
	 * @throws ValidationException
	 */
	public boolean remove(final String personId, final String inviteeId) throws ValidationException
	{
		var record = find(personId, inviteeId);
		if (null == record) return false;

		currentSession().delete(record);

		return true;
	}

	/** Finds a single Friend entity by value.person and value.invitee.
	 * 
	 * @param value
	 * @return NULL if not found.
	 */
	Friend find(final FriendValue value) { return find(value.personId, value.inviteeId); }

	/** Finds a single Friend entity by person and invitee.
	 * 
	 * @param personId
	 * @param inviteeId
	 * @return NULL if not found.
	 */
	Friend find(final String personId, final String inviteeId)
	{
		return namedQuery("findFriend")
			.setParameter("personId", personId)
			.setParameter("inviteeId", inviteeId)
			.uniqueResult();
	}

	/** Finds a single Friend entity by identifier.
	 *
	 * @param personId
	 * @param inviteeId
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	Friend findWithException(final String personId, final String inviteeId) throws ObjectNotFoundException
	{
		var record = find(personId, inviteeId);
		if (null == record)
			throw new ObjectNotFoundException("Could not find the Friend for PersonID '" + personId + "' and InviteeID '" + inviteeId + "'.");

		return record;
	}

	/** Gets a single Friend value by identifier.
	 *
	 * @param personId
	 * @param inviteeId
	 * @return NULL if not found.
	 */
	public FriendValue getById(final String personId, final String inviteeId)
	{
		var record = find(personId, inviteeId);
		return (null != record) ? record.toValue() : null;
	}

	/** Gets a single Friend value by identifier.
	 *
	 * @param personId
	 * @param inviteeId
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is valid.
	 */
	public FriendValue getByIdWithException(final String personId, final String inviteeId) throws ObjectNotFoundException
	{
		return findWithException(personId, inviteeId).toValue();
	}

	/** Searches the Friend entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<FriendValue, FriendFilter> search(final FriendFilter filter) throws ValidationException
	{
		var builder = createQueryBuilder(filter.clean(), SELECT);
		var v = new QueryResults<FriendValue, FriendFilter>(builder.aggregate(COUNT), filter);
		if (v.isEmpty()) return v;

		return v.withRecords(builder.orderBy(ORDER.normalize(v)).run(v).stream().map(o -> o.toValue()).collect(Collectors.toList()));
	}

	/** Counts the number of Friend entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	public long count(final FriendFilter filter) throws ValidationException
	{
		return createQueryBuilder(filter.clean(), null).aggregate(COUNT);
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private QueryBuilder<Friend> createQueryBuilder(final FriendFilter filter, final String select)
		throws ValidationException
	{
		return new HibernateQueryBuilder<Friend>(currentSession(), select, Friend.class)
			.add("personId", "o.personId = :personId", filter.personId)
			.add("inviteeId", "o.inviteeId = :inviteeId", filter.inviteeId)
			.add("userId", "(((o.personId = :userId) AND (o.rejectedAt IS NULL)) OR (o.inviteeId = :userId))", filter.userId)	// Non-admins can see all the invitations sent to them BUT only the non-rejected ones they have sent. DLS on 4/27/2020.
			.addNotNull("o.acceptedAt", filter.hasAcceptedAt)
			.add("acceptedAtFrom", "o.acceptedAt >= :acceptedAtFrom", filter.acceptedAtFrom)
			.add("acceptedAtTo", "o.acceptedAt <= :acceptedAtTo", filter.acceptedAtTo)
			.addNotNull("o.rejectedAt", filter.hasRejectedAt)
			.add("rejectedAtFrom", "o.rejectedAt >= :rejectedAtFrom", filter.rejectedAtFrom)
			.add("rejectedAtTo", "o.rejectedAt <= :rejectedAtTo", filter.rejectedAtTo)
			.add("createdAtFrom", "o.createdAt >= :createdAtFrom", filter.createdAtFrom)
			.add("createdAtTo", "o.createdAt <= :createdAtTo", filter.createdAtTo);
	}
}
