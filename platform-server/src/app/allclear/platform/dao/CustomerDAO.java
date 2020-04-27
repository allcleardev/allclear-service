package app.allclear.platform.dao;

import static com.microsoft.azure.storage.table.TableOperation.*;
import static com.microsoft.azure.storage.table.TableQuery.*;
import static com.microsoft.azure.storage.table.TableQuery.Operators.*;
import static com.microsoft.azure.storage.table.TableQuery.QueryComparisons.*;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.*;
import app.allclear.common.redis.RedisClient;
import app.allclear.platform.entity.Customer;
import app.allclear.platform.filter.CustomerFilter;
import app.allclear.platform.value.CustomerValue;

/** Data access object that provides access to the Cosmos Table - 'allclear-admins/customers'.
 * 
 * @author smalleyd
 * @version 1.1.0
 * @since 4/26/2020
 *
 */

public class CustomerDAO
{
	private static final Logger log = LoggerFactory.getLogger(CustomerDAO.class);
	public static final String TABLE = "customers";
	public static final String LIMIT_KEY = "customer:%s:%d";

	private final String env;
	private final CloudTable table;
	private final RedisClient redis;

	public CustomerDAO(final String env,  final String connectionString, final RedisClient redis)
		throws InvalidKeyException, StorageException, URISyntaxException
	{
		(table = CloudStorageAccount.parse(connectionString).createCloudTableClient().getTableReference(TABLE)).createIfNotExists();
		log.info("TABLE: " + table);

		this.env = env;
		this.redis = redis;
	}

	/** Adds a single Customer value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public CustomerValue add(final CustomerValue value) throws ValidationException
	{
		_validate(value);

		try { table.execute(insert(new Customer(env, value.withId(UUID.randomUUID().toString())))); }
		catch (final StorageException ex) { throw new RuntimeException(ex); }

		return value;
	}

	/** Retrieve the specified Customer, checks for throttling, marks as accessed, and returns.
	 * 
	 * @param id
	 * @return never NULL
	 * @throws ObjectNotFoundException
	 * @throws ThrottledException
	 */
	public CustomerValue access(final String id) throws ObjectNotFoundException, ThrottledException
	{
		try
		{
			var record = _findWithException(id);
			var limit = record.getLimit();
			if (0 < limit)	// Limit exists
			{
				var count = redis.operation(j -> {
					var key = limitKey(id);	// Create a key to the second.
					var o = j.incr(key);
					j.expire(key, 5);	// Make sure that it self clears when not needed anymore.

					return o.intValue();
				});

				if (count > limit) throw new ThrottledException("Client '" + record.getName() + "' has exceeded its limit of '" + limit + "' requests per second.");
			}

			table.execute(merge(record.accessed()));

			return record.toValue();
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	public String limitKey(final String id) { return String.format(LIMIT_KEY, id, System.currentTimeMillis() / 1000L); }

	/** Updates a single Customer value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public CustomerValue update(final CustomerValue value) throws ValidationException
	{
		var validator = new Validator();
		_validate(value);
		validator.ensureExists("id", "ID", value.id).check();	// Validate after call to _validate where the ID will be trimmed.

		try
		{
			table.execute(merge(_findWithException(value.id).update(value)));
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }

		return value;
	}

	/** Validates a single Customer value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final CustomerValue value) throws ValidationException
	{
		_validate(value);
	}

	/** Validates a single Customer value and returns any CMR fields.
	 *
	 * @param value
	 * @return supplied value.
	 * @throws ValidationException
	 */
	private CustomerValue _validate(final CustomerValue value) throws ValidationException
	{
		return _validate(value, new Validator());
	}

	/** Validates a single Customer value and returns any CMR fields.
	 *
	 * @param value
	 * @param validator
	 * @return supplied value.
	 * @throws ValidationException
	 */
	private CustomerValue _validate(final CustomerValue value, final Validator validator) throws ValidationException
	{
		value.clean();

		// Throw exception after field existence checks and before FK checks.
		validator
			.ensureExistsAndLength("name", "Name", value.name, CustomerValue.MAX_LEN_NAME)
			.ensureRange("limit", "Limit", value.limit, CustomerValue.MIN_LIMIT, CustomerValue.MAX_LIMIT)
			.check();

		return value;
	}

	/** Removes a single Customer value.
	 *
	 * @param id
	 * @return TRUE if the entity is found and removed.
	 * @throws ValidationException
	 */
	public boolean remove(final String id) throws ValidationException
	{
		try
		{
			var record = find(id);
			if (null == record) return false;
	
			table.execute(delete(record));
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }

		return true;
	}

	Customer find(final String id) throws StorageException
	{
		return table.execute(retrieve(env, id, Customer.class)).getResultAsType();
	}

	/** Finds a single Customer entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	Customer findWithException(final String id) throws ObjectNotFoundException
	{
		try { return _findWithException(id); }
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Finds a single Customer entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	Customer _findWithException(final String id) throws StorageException, ObjectNotFoundException
	{
		var record = find(id);
		if (null == record)
			throw new ObjectNotFoundException("Could not find the Customer because id '" + id + "' is invalid.");

		return record;
	}

	/** Gets a single Customer value by identifier.
	 *
	 * @param id
	 * @return NULL if not found.
	 */
	public CustomerValue getById(final String id)
	{
		try
		{
			var record = find(id);
			return (null != record) ? record.toValue() : null;
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Gets a single Customer value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is valid.
	 */
	public CustomerValue getByIdWithException(final String id) throws ObjectNotFoundException
	{
		return findWithException(id).toValue();
	}

	/** Gets one or more Customer values by name wildcard search.
	 * 
	 * @param name
	 * @return never NULL.
	 */
	public List<CustomerValue> getByName(final String name)
	{
		var values = new LinkedList<CustomerValue>();
		for (var o : table.execute(from(Customer.class)
			.where("(" + generateFilterCondition("Name", GREATER_THAN_OR_EQUAL, name) + ") " + AND +
				"(" + generateFilterCondition("Name", LESS_THAN_OR_EQUAL, name + StringUtils.repeat('z', CustomerValue.MAX_LEN_NAME)) + ")"))) values.add(o.toValue());

		return values;
	}

	/** Searches the Customer entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<CustomerValue, CustomerFilter> search(final CustomerFilter filter) throws ValidationException
	{
		var values = new LinkedList<CustomerValue>();
		for (var o : table.execute(createQueryBuilder(filter))) values.add(o.toValue());

		return values.isEmpty() ? new QueryResults<>(0L, filter) : new QueryResults<>(values, filter);
	}

	/** Counts the number of Customer entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	@SuppressWarnings("unused")
	public long count(final CustomerFilter filter) throws ValidationException
	{
		long i = 0;
		for (var o : table.execute(createQueryBuilder(filter))) i++;

		return i;
	}

	public TableQuery<Customer> createQueryBuilder(final CustomerFilter filter) throws ValidationException
	{
		var filters = new LinkedList<String>();
		filters.add(generateFilterCondition("PartitionKey", EQUAL, env));
		if (null != filter.id) filters.add(generateFilterCondition("RowKey", EQUAL, filter.id));
		if (null != filter.name) filters.add(generateFilterCondition("Name", EQUAL, filter.name));
		if (null != filter.limit) filters.add(generateFilterCondition("Limit", EQUAL, filter.limit));
		if (null != filter.hasLimit) filters.add(generateFilterCondition("Limit", filter.hasLimit ? GREATER_THAN : EQUAL, 0));
		if (null != filter.limitFrom) filters.add(generateFilterCondition("Limit", GREATER_THAN_OR_EQUAL, filter.limitFrom));
		if (null != filter.limitTo) filters.add(generateFilterCondition("Limit", LESS_THAN_OR_EQUAL, filter.limitTo));
		if (null != filter.active) filters.add(generateFilterCondition("Active", EQUAL, filter.active));
		if (null != filter.hasLastAccessedAt) filters.add(generateFilterCondition("LastAccessedAt", filter.hasLastAccessedAt ? GREATER_THAN : EQUAL, 0L));
		if (null != filter.lastAccessedAtFrom) filters.add(generateFilterCondition("LastAccessedAt", GREATER_THAN_OR_EQUAL, filter.lastAccessedAtFrom.getTime()));
		if (null != filter.lastAccessedAtTo) filters.add(generateFilterCondition("LastAccessedAt", LESS_THAN_OR_EQUAL, filter.lastAccessedAtTo.getTime()));
		if (null != filter.createdAtFrom) filters.add(generateFilterCondition("CreatedAt", GREATER_THAN_OR_EQUAL, filter.createdAtFrom));
		if (null != filter.createdAtTo) filters.add(generateFilterCondition("CreatedAt", LESS_THAN_OR_EQUAL, filter.createdAtTo));
		if (null != filter.updatedAtFrom) filters.add(generateFilterCondition("UpdatedAt", GREATER_THAN_OR_EQUAL, filter.updatedAtFrom));
		if (null != filter.updatedAtTo) filters.add(generateFilterCondition("UpdatedAt", LESS_THAN_OR_EQUAL, filter.updatedAtTo));

		var query = from(Customer.class).where(filters.stream().map(o -> "(" + o + ") ").collect(Collectors.joining(AND)));

		return query;
	}
}
