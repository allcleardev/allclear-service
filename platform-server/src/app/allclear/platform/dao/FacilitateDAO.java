package app.allclear.platform.dao;

import static com.microsoft.azure.storage.table.TableOperation.*;
import static com.microsoft.azure.storage.table.TableQuery.from;
import static com.microsoft.azure.storage.table.TableQuery.generateFilterCondition;
import static com.microsoft.azure.storage.table.TableQuery.Operators.AND;
import static com.microsoft.azure.storage.table.TableQuery.QueryComparisons.EQUAL;
import static com.microsoft.azure.storage.table.TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL;
import static com.microsoft.azure.storage.table.TableQuery.QueryComparisons.LESS_THAN_OR_EQUAL;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.*;
import app.allclear.platform.entity.Facilitate;
import app.allclear.platform.filter.FacilitateFilter;
import app.allclear.platform.type.CrowdsourceStatus;
import app.allclear.platform.type.Originator;
import app.allclear.platform.value.FacilitateValue;
import app.allclear.platform.value.FacilityValue;

/** Data component that provides access the Azure Cosmos Facilitate table.
 *  A Facilitate entity represents a request for a new/change Facility.
 *
 * @author smalleyd
 * @version 1.1.60
 * @since 5/21/2020
 *
 */

public class FacilitateDAO
{
	private static final Logger log = LoggerFactory.getLogger(FacilitateDAO.class);
	public static final String TABLE = "facilitate";

	private final CloudTable table;
	private final SessionDAO sessionDao;
	private final FacilityDAO facilityDao;

	public FacilitateDAO(final String connectionString, final FacilityDAO facilityDao, final SessionDAO sessionDao)
		throws InvalidKeyException, StorageException, URISyntaxException
	{
		(table = CloudStorageAccount.parse(connectionString).createCloudTableClient().getTableReference(TABLE)).createIfNotExists();
		log.info("TABLE: " + table);

		this.sessionDao = sessionDao;
		this.facilityDao = facilityDao;
	}

	public FacilitateValue addByCitizen(final FacilitateValue value) throws ValidationException
	{
		return add(value.withOriginator(Originator.CITIZEN));
	}

	public FacilitateValue addByProvider(final FacilitateValue value) throws ValidationException
	{
		return add(value.withOriginator(Originator.PROVIDER));
	}

	FacilitateValue add(final FacilitateValue value) throws ValidationException
	{
		return insertIt(value.withChange(false));
	}

	public FacilitateValue changeByCitizen(final FacilitateValue value) throws ValidationException
	{
		return change(value.withOriginator(Originator.CITIZEN));
	}

	public FacilitateValue changeByProvider(final FacilitateValue value) throws ValidationException
	{
		return change(value.withOriginator(Originator.PROVIDER));
	}

	FacilitateValue change(final FacilitateValue value) throws ValidationException
	{
		return insertIt(value.withChange(true));
	}

	FacilitateValue insertIt(final FacilitateValue value) throws ValidationException
	{
		validate(value);

		try { table.execute(insert(new Facilitate(value.withStatus(CrowdsourceStatus.OPEN)))); }
		catch (final StorageException ex) { throw new RuntimeException(ex); }

		return value;
	}

	/** Validates the Facility add/change request.
	 * 
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final FacilitateValue value) throws NotAuthorizedException, ValidationException
	{
		var auth = sessionDao.current();
		if (null != auth)
		{
			if (auth.person()) value.creatorId = auth.person.id;
			else throw new NotAuthorizedException("Must be a Person or Anonymous.");
		}

		value.clean();

		var validator = new Validator();
		validator.ensureExists("value", "Value", value.value)
			.ensureLength("location", "Location", value.location, FacilitateValue.MAX_LEN_LOCATION)
			.check();

		if (value.change)	// Make sure the facility exists for changes.
		{
			validator.ensureExists("value", "Value ID", value.value.id).check();
			if (!facilityDao.exists(value.value.id))
				validator.add("value", "The Facility '%d' does not exist.", value.value.id).check();

			value.entityId = value.value.id;
		}
	}

	/** Promotes a single Facilitate value.
	 *
	 * @param statusId
	 * @param createdAt
	 * @param value a modified facility value to be added or updated instead of the facility value associated with the request.
	 * @return the promoted Facilitate
	 * @throws ObjectNotFoundException
	 * @throws ValidationException
	 */
	public FacilitateValue promote(final String statusId, final String createdAt, final FacilityValue value)
		throws ObjectNotFoundException, ValidationException
	{
		var auth = sessionDao.checkEditor();

		try
		{
			var record = findWithException(statusId, createdAt);

			var v = (null != value) ? value : record.payload();
			if (record.change) facilityDao.update(v, auth.canAdmin());
			else facilityDao.add(v, auth.canAdmin());

			table.execute(delete(record));	// MUST remove the existing value. When the status changes, the partition key will change too making this a remove and insert operation. DLS on 5/22/2020.
			table.execute(insert(record.promote(auth.id, v.id)));

			return record.toValue().withValue(v);	// Perform toValue after promoted.
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Rejects a single Facilitate value.
	 *
	 * @param statusId
	 * @param createdAt
	 * @return the rejected Facilitate
	 * @throws ObjectNotFoundException
	 * @throws ValidationException
	 */
	public FacilitateValue reject(final String statusId, final String createdAt) throws ObjectNotFoundException, ValidationException
	{
		var auth = sessionDao.checkEditor();

		try
		{
			var record = findWithException(statusId, createdAt);
			table.execute(delete(record));	// MUST remove the existing value. When the status changes, the partition key will change too making this a remove and insert operation. DLS on 5/22/2020.
			table.execute(insert(record.reject(auth.id)));

			return record.toValue();
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Removes a single Facilitate value.
	 *
	 * @param statusId
	 * @param createdAt
	 * @return TRUE if the entity is found and removed.
	 * @throws ValidationException
	 */
	public boolean remove(final String statusId, final String createdAt) throws ValidationException
	{
		sessionDao.checkAdmin();

		try
		{
			var record = find(statusId, createdAt);
			if (null == record) return false;
	
			table.execute(delete(record));
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }

		return true;
	}

	Facilitate find(final String statusId, final String createdAt) throws StorageException
	{
		return table.execute(retrieve(statusId, createdAt, Facilitate.class)).getResultAsType();
	}

	/** Finds a single Facilitate entity by statusId and createdAt (partitionKey and rowKey).
	 *
	 * @param statusId
	 * @param createdAt
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifiers are invalid.
	 */
	Facilitate findWithException(final String statusId, final String createdAt) throws ObjectNotFoundException
	{
		try
		{
			var record = find(statusId, createdAt);
			if (null == record)
				throw new ObjectNotFoundException("Could not find the Facilitate because id '" + statusId + "/" + createdAt + "' is invalid.");

			return record;
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Gets a single Facilitate value by statusId and createdAt (partitionKey and rowKey).
	 *
	 * @param statusId
	 * @param createdAt
	 * @return NULL if not found.
	 */
	public FacilitateValue getById(final String statusId, final String createdAt)
	{
		sessionDao.checkEditor();

		try
		{
			var record = find(statusId, createdAt);
			return (null != record) ? record.toValue() : null;
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Gets a single Facilitate value by statusId and createdAt (partitionKey and rowKey).
	 *
	 * @param statusId
	 * @param createdAt
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifiers are valid.
	 */
	public FacilitateValue getByIdWithException(final String statusId, final String createdAt) throws ObjectNotFoundException
	{
		sessionDao.checkEditor();

		return findWithException(statusId, createdAt).toValue();
	}

	/** Searches the Facilitate entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<FacilitateValue, FacilitateFilter> search(final FacilitateFilter filter) throws ValidationException
	{
		var pageSize = filter.pageSize(100);
		var values = new LinkedList<FacilitateValue>();
		for (var o : table.execute(createQueryBuilder(filter)))
		{
			if (1 > pageSize--) break;
			values.add(o.toValue());
		}

		return values.isEmpty() ? new QueryResults<>(0L, filter) : new QueryResults<>(values, filter);
	}

	/** Counts the number of Facilitate entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	@SuppressWarnings("unused")
	public long count(final FacilitateFilter filter) throws ValidationException
	{
		long i = 0;
		for (var o : table.execute(createQueryBuilder(filter))) i++;

		return i;
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private TableQuery<Facilitate> createQueryBuilder(final FacilitateFilter filter)
		throws ValidationException
	{
		var auth = sessionDao.checkEditorOrPerson();
		if (auth.person()) filter.creatorId = auth.person.id;

		var filters = new LinkedList<String>();
		if (null != filter.statusId) filters.add(generateFilterCondition("PartitionKey", EQUAL, filter.statusId));
		if (null != filter.location) filters.add(generateFilterCondition("Location", EQUAL, filter.location));
		if (null != filter.gotTested) filters.add(generateFilterCondition("GotTested", EQUAL, filter.gotTested));
		if (null != filter.originatorId) filters.add(generateFilterCondition("OriginatorId", EQUAL, filter.originatorId));
		if (null != filter.change) filters.add(generateFilterCondition("Change", EQUAL, filter.change));
		if (null != filter.entityId) filters.add(generateFilterCondition("EntityId", EQUAL, filter.entityId));
		if (null != filter.promoterId) filters.add(generateFilterCondition("PromoterId", EQUAL, filter.promoterId));
		if (null != filter.promotedAtFrom) filters.add(generateFilterCondition("PromotedAt", GREATER_THAN_OR_EQUAL, filter.promotedAtFrom));
		if (null != filter.promotedAtTo) filters.add(generateFilterCondition("PromotedAt", LESS_THAN_OR_EQUAL, filter.promotedAtTo));
		if (null != filter.rejecterId) filters.add(generateFilterCondition("RejecterId", EQUAL, filter.rejecterId));
		if (null != filter.rejectedAtFrom) filters.add(generateFilterCondition("RejectedAt", GREATER_THAN_OR_EQUAL, filter.rejectedAtFrom));
		if (null != filter.rejectedAtTo) filters.add(generateFilterCondition("RejectedAt", LESS_THAN_OR_EQUAL, filter.rejectedAtTo));
		if (null != filter.creatorId) filters.add(generateFilterCondition("CreatorId", EQUAL, filter.creatorId));
		if (null != filter.createdAtFrom) filters.add(generateFilterCondition("CreatedAt", GREATER_THAN_OR_EQUAL, filter.createdAtFrom));
		if (null != filter.createdAtTo) filters.add(generateFilterCondition("CreatedAt", LESS_THAN_OR_EQUAL, filter.createdAtTo));
		if (null != filter.updatedAtFrom) filters.add(generateFilterCondition("UpdatedAt", GREATER_THAN_OR_EQUAL, filter.updatedAtFrom));
		if (null != filter.updatedAtTo) filters.add(generateFilterCondition("UpdatedAt", LESS_THAN_OR_EQUAL, filter.updatedAtTo));

		var query = from(Facilitate.class);
		if (!filters.isEmpty()) query.where(filters.stream().map(o -> "(" + o + ") ").collect(Collectors.joining(AND)));

		return query;
	}
}
