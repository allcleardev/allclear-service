package app.allclear.platform.dao;

import static com.microsoft.azure.storage.table.TableOperation.*;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;

import app.allclear.common.errors.*;
import app.allclear.platform.entity.Facilitate;
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
		return insert(value.withChange(false));
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
		return insert(value.withChange(true));
	}

	FacilitateValue insert(final FacilitateValue value) throws ValidationException
	{
		validate(value);

		try { table.execute(TableOperation.insert(new Facilitate(value.withStatus(CrowdsourceStatus.OPEN)))); }
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

		new Validator()
			.ensureExists("value", "Value", value.value)
			.ensureLength("location", "Location", value.location, FacilitateValue.MAX_LEN_LOCATION)
			.check();

		facilityDao.validate(value.value);	// Make sure that a minimum of information is provided. DLS on 5/21/2020.
	}

	/** Promotes a single Facilitate value.
	 *
	 * @param statusId
	 * @param createdAt
	 * @return the promoted Facility
	 * @throws ObjectNotFoundException
	 * @throws ValidationException
	 */
	public FacilityValue promote(final String statusId, final String createdAt) throws ObjectNotFoundException, ValidationException
	{
		var auth = sessionDao.checkEditor();

		try
		{
			var record = findWithException(statusId, createdAt);

			var v = record.payload();
			if (record.change) facilityDao.update(v, auth.canAdmin());
			else facilityDao.add(v, auth.canAdmin());

			table.execute(merge(record.promote(auth.id, v.id)));

			return v;
		}
		catch (final StorageException ex) { throw new RuntimeException(ex); }
	}

	/** Rejects a single Facilitate value.
	 *
	 * @param statusId
	 * @param createdAt
	 * @throws ObjectNotFoundException
	 * @throws ValidationException
	 */
	public void reject(final String statusId, final String createdAt) throws ObjectNotFoundException, ValidationException
	{
		var auth = sessionDao.checkEditor();

		try
		{
			table.execute(merge(findWithException(statusId, createdAt).reject(auth.id)));
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
}
