package app.allclear.platform.dao;

import static app.allclear.common.dao.OrderByBuilder.*;
import static com.microsoft.azure.storage.table.TableQuery.from;
import static com.microsoft.azure.storage.table.TableQuery.generateFilterCondition;
import static com.microsoft.azure.storage.table.TableQuery.Operators.AND;
import static com.microsoft.azure.storage.table.TableQuery.QueryComparisons.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.table.*;

import app.allclear.common.dao.*;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.hibernate.AbstractDAO;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.platform.entity.*;
import app.allclear.platform.filter.AuditLogFilter;
import app.allclear.platform.value.Auditable;
import app.allclear.platform.value.AuditLogValue;

/**********************************************************************************
*
*	Data access object that handles access to the AuditLog entity.
*
*	@author smalleyd
*	@version 1.1.46
*	@since May 10, 2020
*
**********************************************************************************/

public class AuditLogDAO extends AbstractDAO<AuditLog> implements Auditor
{
	@SuppressWarnings("unused")
	private static final OrderByBuilder ORDER = new OrderByBuilder('o', 
		"id", ASC,
		"actionAt", ASC,
		"actorType", ASC,
		"actionBy", ASC,
		"action", ASC,
		"payload", ASC);

	/** Native SQL clauses. */
	public static final String FROM_ALIAS = "o";

	private final SessionDAO sessionDao;
	private final CloudTableClient client;
	private final ObjectMapper mapper = JacksonUtils.createMapperMS();
	private final Map<String, CloudTable> tables = new ConcurrentHashMap<>();

	public AuditLogDAO(final SessionFactory factory, final SessionDAO sessionDao, final String connectionString)
		throws InvalidKeyException, URISyntaxException
	{
		super(factory);	// Need the SessionFactory for the afterTrans handler.

		this.sessionDao = sessionDao;
		this.client = CloudStorageAccount.parse(connectionString).createCloudTableClient();
	}

	private CloudTable table_(final String name)
	{
		try
		{
			var o = client.getTableReference(name);
			o.createIfNotExists();

			return o;
		}
		catch (final StorageException | URISyntaxException ex) { throw new RuntimeException(ex); }
	}
	private CloudTable table(final String name) { return tables.computeIfAbsent(name, k -> table_(k)); }
	@Override public AuditLogValue add(final Auditable value) { return insert(value, "add"); }
	@Override public AuditLogValue update(final Auditable value) { return insert(value, "update"); }
	@Override public AuditLogValue remove(final Auditable value) { return insert(value, "remove"); }
	public AuditLogValue insert(final Auditable value, final String action)
	{
		var s = sessionDao.current();
		var o = new AuditLogValue(value.id(), System.currentTimeMillis(), s.type, s.name, action,
			json(value),
			value.updatedAt());

		afterTrans(() -> {
			try { table(value.tableName()).execute(TableOperation.insert(new AuditLog(o))); }
			catch (final StorageException ex) { throw new RuntimeException(ex); }
		});

		return o;
	}

	private String json(final Auditable value)
	{
		try { return mapper.writeValueAsString(value); }
		catch (final IOException ex) { throw new RuntimeException(ex); }
	}

	/** Searches the AuditLog entity based on the supplied filter.
	 *
	 * @param name entity/table name
	 * @param filter
	 * @return never NULL.
	 * @throws ValidationException
	 */
	public QueryResults<AuditLogValue, AuditLogFilter> search(final String name, final AuditLogFilter filter) throws ValidationException
	{
		var values = new LinkedList<AuditLogValue>();
		for (var o : table(name).execute(createQueryBuilder(filter))) values.add(o.toValue());

		return values.isEmpty() ? new QueryResults<>(0L, filter) : new QueryResults<>(values, filter);
	}

	/** Counts the number of AuditLog entities based on the supplied filter.
	 *
	 * @param name entity/table name
	 * @param value
	 * @return zero if none found.
	 * @throws ValidationException
	 */
	@SuppressWarnings("unused")
	public long count(final String name, final AuditLogFilter filter) throws ValidationException
	{
		long i = 0;
		for (var o : table(name).execute(createQueryBuilder(filter))) i++;

		return i;
	}

	/** Helper method - creates the a standard Hibernate query builder. */
	private TableQuery<AuditLog> createQueryBuilder(final AuditLogFilter filter)
		throws ValidationException
	{
		var filters = new LinkedList<String>();
		if (null != filter.id) filters.add(generateFilterCondition("PartitionKey", EQUAL, filter.id));
		if (null != filter.actionAt) filters.add(generateFilterCondition("RowKey", EQUAL, filter.actionAt.toString()));
		if (null != filter.actorType) filters.add(generateFilterCondition("ActorType", EQUAL, filter.actorType));
		if (null != filter.actionBy) filters.add(generateFilterCondition("ActionBy", EQUAL, filter.actionBy));
		if (null != filter.action) filters.add(generateFilterCondition("Action", EQUAL, filter.action));
		if (null != filter.payload) filters.add(generateFilterCondition("Payload", EQUAL, filter.payload));
		if (null != filter.timestampFrom) filters.add(generateFilterCondition("ActionAt", GREATER_THAN_OR_EQUAL, filter.timestampFrom.getTime()));
		if (null != filter.timestampTo) filters.add(generateFilterCondition("ActionAt", LESS_THAN_OR_EQUAL, filter.timestampTo.getTime()));

		var query = from(AuditLog.class).where(filters.stream().map(o -> "(" + o + ") ").collect(Collectors.joining(AND)));

		return query;
	}

	/** Removes all the audit log entries for the specified entity. Used to clean up after TESTS.
	 * 
	 * @param name
	 * @param id
	 * @return number of entries removed.
	 * @throws StorageException
	 */
	public int clear(final String name, final String id) throws StorageException
	{
		int i = 0;
		var table = table(name);
		for (var o : table.execute(createQueryBuilder(new AuditLogFilter().withId(id))))
		{
			i++;
			table.execute(TableOperation.delete(o));
		}

		return i;
	}
}
