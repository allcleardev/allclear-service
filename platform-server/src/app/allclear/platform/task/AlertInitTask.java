package app.allclear.platform.task;

import java.util.List;

import org.hibernate.*;
import org.slf4j.*;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.errors.AbortException;
import app.allclear.common.hibernate.DualSessionFactory;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.task.AbstractHibernateTask;
import app.allclear.common.time.StopWatch;
import app.allclear.platform.dao.PeopleDAO;
import app.allclear.platform.model.AlertInitRequest;
import app.allclear.platform.model.AlertRequest;
import app.allclear.platform.type.Timezone;

/** Task callback that handles Facility Alert initialization.
 * 
 * @author smalleyd
 * @version 1.0.109
 * @since 4/14/2020
 *
 */

public class AlertInitTask extends AbstractHibernateTask<AlertInitRequest>
{
	private static final Logger log = LoggerFactory.getLogger(AlertInitTask.class);

	private static final int PAGE_SIZE = 100;

	private final PeopleDAO dao;
	private final QueueClient queue;
	private final ObjectMapper mapper = JacksonUtils.createMapperAzure();

	@Override public boolean readOnly() { return true; }
	@Override public boolean transactional() { return false; }

	AlertInitTask(final SessionFactory factory, final PeopleDAO dao, final QueueClient queue)	// For tests
	{
		this(new DualSessionFactory(factory), dao, queue);
	}

	public AlertInitTask(final DualSessionFactory factory, final PeopleDAO dao, final QueueClient queue)
	{
		super(factory);

		this.dao = dao;
		this.queue = queue;

		log.info("INITIALIZED");
	}

	@Override
	public boolean process(final AlertInitRequest request, final Session s) throws Exception
	{
		if (null == request.timezoneId) throw new AbortException("The request is missing the timezoneId.");

		var zone = Timezone.get(request.timezoneId);
		if (null == zone) throw new AbortException("The timezone '" + request.timezoneId + "' does not receive alerts.");

		log.info("PROCESSING: {}", zone);

		var lastId = "";
		List<String> ids = null;
		int count = 0, batches = 0;
		var timer = new StopWatch();
		while (!(ids = dao.getActiveAlertableIdsByLongitude(lastId, zone, PAGE_SIZE)).isEmpty())
		{
			for (var id : ids)
				queue.sendMessage(mapper.writeValueAsString(new AlertRequest(lastId = id)));

			int size = ids.size();
			count+= size;
			log.info("BATCH ({}): {} - {} out of {} in {} - {}", zone, ++batches, size, count, timer.split(), ids);
		}

		log.info("PROCESSED ({}): {} in {}", zone, count, timer.total());

		return true;
	}
}
