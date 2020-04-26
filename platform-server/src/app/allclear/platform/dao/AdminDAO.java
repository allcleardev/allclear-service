package app.allclear.platform.dao;

import static com.microsoft.azure.storage.table.TableOperation.*;
import static com.microsoft.azure.storage.table.TableQuery.*;
import static com.microsoft.azure.storage.table.TableQuery.Operators.*;
import static com.microsoft.azure.storage.table.TableQuery.QueryComparisons.*;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.*;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableQuery;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.*;
import app.allclear.platform.entity.Admin;
import app.allclear.platform.filter.AdminFilter;
import app.allclear.platform.model.AuthenticationRequest;
import app.allclear.platform.value.AdminValue;

/** Data access object that provides access to the Cosmos Table - 'allclear-admins'.
 * 
 * @author smalleyd
 * @version 1.0.14
 * @since 4/1/2020
 *
 */

public class AdminDAO
{
	private static final Logger log = LoggerFactory.getLogger(AdminDAO.class);
	public static final String TABLE = "admins";

	private final CloudTable table;

	public AdminDAO(final String connectionString) throws InvalidKeyException, StorageException, URISyntaxException
	{
		(table = CloudStorageAccount.parse(connectionString).createCloudTableClient().getTableReference(TABLE)).createIfNotExists();
		log.info("TABLE: " + table);
	}

	/** Adds a single Admin value.
	 *
	 * @param value
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public AdminValue add(final AdminValue value) throws ValidationException
	{
		return update(value);
	}

	/** Authenticates an administrative account.
	 * 
	 * @param request
	 * @return never NULL
	 * @throws ValidationException if the credentials are invalid.
	 */
	public AdminValue authenticate(final AuthenticationRequest request) throws ValidationException
	{
		new Validator()
			.ensureExists("userName", "User Name", request.userName)
			.ensureExists("password", "Password", request.password)
			.check();

		try
		{
			var o = find(request.userName);
			if (null == o) throw new ValidationException("Invalid credentials");	// Do NOT reveal reason to potential malicious user.
			if (!o.check(request.password)) throw new ValidationException("Invalid credentials");

			return o.toValue();
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Updates a single Admin value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public AdminValue update(final AdminValue value) throws ValidationException
	{
		try
		{
			var validator = new Validator();
			_validate(value, validator);
			var record = find(value.id);
			if (null == record)
			{
				validator	// MUST have password on addition.
					.ensureExistsAndLength("password", "Password", value.password, AdminValue.MAX_LEN_PASSWORD).check();
	
				table.execute(insert(record = new Admin(value)));
			}
			else
			{
				table.execute(merge(record.update(value)));
			}
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }

		return value;
	}

	/** Validates a single Admin value.
	 *
	 * @param value
	 * @throws ValidationException
	 */
	public void validate(final AdminValue value) throws ValidationException
	{
		_validate(value, new Validator());
	}

	/** Validates a single Admin value and returns any CMR fields.
	 *
	 * @param value
	 * @return array of CMRs entities.
	 * @throws ValidationException
	 */
	private void _validate(final AdminValue value, final Validator validator) throws ValidationException
	{
		value.clean();

		// Throw exception after field existence checks and before FK checks.
		validator.ensureExistsAndLength("id", "ID", value.id, AdminValue.MAX_LEN_ID)
			.ensureLength("password", "Password", value.password, AdminValue.MIN_LEN_PASSWORD, AdminValue.MAX_LEN_PASSWORD)
			.ensureExistsAndLength("email", "Email", value.email, AdminValue.MAX_LEN_EMAIL)
			.ensureExistsAndLength("firstName", "First Name", value.firstName, AdminValue.MAX_LEN_FIRST_NAME)
			.ensureExistsAndLength("lastName", "Last Name", value.lastName, AdminValue.MAX_LEN_LAST_NAME)
			.check();
	}

	/** Removes a single Admin value.
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

	Admin find(final String id) throws StorageException
	{
		return table.execute(retrieve(Admin.PARTITION, id, Admin.class)).getResultAsType();
	}

	/** Finds a single Admin entity by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is invalid.
	 */
	public Admin findWithException(final String id) throws ObjectNotFoundException
	{
		try
		{
			var record = find(id);
			if (null == record)
				throw new ObjectNotFoundException("Could not find the Admin because id '" + id + "' is invalid.");

			return record;
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Gets a single Admin value by identifier.
	 *
	 * @param id
	 * @return NULL if not found.
	 */
	public AdminValue getById(final String id)
	{
		try
		{
			var record = find(id);
			return (null != record) ? record.toValue() : null;
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Gets a single Admin value by identifier.
	 *
	 * @param id
	 * @return never NULL.
	 * @throws ObjectNotFoundException if the identifier is valid.
	 */
	public AdminValue getByIdWithException(final String id) throws ObjectNotFoundException
	{
		return findWithException(id).toValue();
	}

	/** Searches the Admin entity based on the supplied filter.
	 *
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<AdminValue, AdminFilter> search(final AdminFilter filter) throws ValidationException
	{
		var values = new LinkedList<AdminValue>();
		for (var o : table.execute(createQueryBuilder(filter))) values.add(o.toValue());

		return values.isEmpty() ? new QueryResults<>(0L, filter) : new QueryResults<>(values, filter);
	}

	/** Counts the number of Admin entities based on the supplied filter.
	 *
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	@SuppressWarnings("unused")
	public long count(final AdminFilter filter) throws ValidationException
	{
		long i = 0;
		for (var o : table.execute(createQueryBuilder(filter))) i++;

		return i;
	}

	public TableQuery<Admin> createQueryBuilder(final AdminFilter filter) throws ValidationException
	{
		var filters = new LinkedList<String>();
		filters.add(generateFilterCondition("PartitionKey", EQUAL, Admin.PARTITION));
		if (null != filter.id) filters.add(generateFilterCondition("RowKey", EQUAL, filter.id));
		if (null != filter.email) filters.add(generateFilterCondition("Email", EQUAL, filter.email));
		if (null != filter.firstName) filters.add(generateFilterCondition("FirstName", EQUAL, filter.firstName));
		if (null != filter.lastName) filters.add(generateFilterCondition("LastName", EQUAL, filter.lastName));
		if (null != filter.supers) filters.add(generateFilterCondition("Supers", EQUAL, filter.supers));
		if (null != filter.createdAtFrom) filters.add(generateFilterCondition("CreatedAt", GREATER_THAN_OR_EQUAL, filter.createdAtFrom));
		if (null != filter.createdAtTo) filters.add(generateFilterCondition("CreatedAt", LESS_THAN_OR_EQUAL, filter.createdAtTo));
		if (null != filter.updatedAtFrom) filters.add(generateFilterCondition("UpdatedAt", GREATER_THAN_OR_EQUAL, filter.updatedAtFrom));
		if (null != filter.updatedAtTo) filters.add(generateFilterCondition("UpdatedAt", LESS_THAN_OR_EQUAL, filter.updatedAtTo));

		var query = from(Admin.class).where(filters.stream().map(o -> "(" + o + ") ").collect(Collectors.joining(AND)));

		return query;
	}
}
