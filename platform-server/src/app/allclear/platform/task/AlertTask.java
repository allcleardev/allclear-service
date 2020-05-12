package app.allclear.platform.task;

import java.util.Date;

import org.hibernate.Session;
import org.slf4j.*;

import app.allclear.common.hibernate.AbstractHibernateRunner;
import app.allclear.common.hibernate.DualSessionFactory;
import app.allclear.common.task.AbstractHibernateTask;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.filter.GeoFilter;
import app.allclear.platform.model.AlertRequest;

/** Task callback that looks for new Facilities near the specified.
 *  If new facilities are found, the user is messaged with a link to see their facilities.
 *  
 * @author smalleyd
 * @version 1.0.111
 * @since 4/15/2020
 *
 */

public class AlertTask extends AbstractHibernateTask<AlertRequest>
{
	private static final Logger log = LoggerFactory.getLogger(AlertTask.class);
	public static final int MILES_DEFAULT = 20;

	private final PeopleDAO dao;
	private final SessionDAO sessionDao;
	private final AbstractHibernateRunner<FacilityFilter, Long> facilitySearch;	// Ensure using read-replica instead of transaction data source.

	@Override public boolean readOnly() { return false; }
	@Override public boolean transactional() { return true; }

	public AlertTask(final DualSessionFactory factory, final PeopleDAO dao, final FacilityDAO facilityDao, final SessionDAO sessionDao)
	{
		super(factory);

		this.dao = dao;
		this.sessionDao = sessionDao;
		this.facilitySearch = new AbstractHibernateRunner<FacilityFilter, Long>(factory) {
			@Override public boolean readOnly() { return true; }
			@Override public boolean transactional() { return false; }
			@Override public Long run(final FacilityFilter filter, final Session s) { return facilityDao.count(filter); }
		};

		log.info("INITIALIZED");
	}

	@Override
	public boolean process(final AlertRequest request, final Session s) throws Exception
	{
		var record = dao.findWithException(request.personId);
		var lastAlertedAt = record.alertedAt();

		final long count = facilitySearch.run(new FacilityFilter().withActivatedAtFrom(lastAlertedAt).withFrom(new GeoFilter(record.getLatitude(), record.getLongitude(), MILES_DEFAULT)));
		if (0L < count)
		{
			log.info("FOUND ({}): {} new facilities.", request.personId, count);
			sessionDao.alert(record.getPhone(), lastAlertedAt);
		}
		else
		{
			log.info("NOT_FOUND ({}): no new facilities.", request.personId);
		}

		var now = new Date();	// Get date right after checking facility search.
		record.setAlertedOf((int) count);
		record.setAlertedAt(now);	// Always mark that the user was checked for a possible facility alert.
		record.setUpdatedAt(now);

		return true;
	}
}
