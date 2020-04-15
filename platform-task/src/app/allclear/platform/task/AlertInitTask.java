package app.allclear.platform.task;

import java.util.List;

import org.slf4j.*;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.errors.AbortException;
import app.allclear.common.task.TaskCallback;
import app.allclear.common.time.StopWatch;
import app.allclear.platform.dao.PeopleJDBi;
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

public class AlertInitTask implements TaskCallback<AlertInitRequest>
{
	private static final Logger log = LoggerFactory.getLogger(AlertInitTask.class);

	private static final int PAGE_SIZE = 100;

	private final PeopleJDBi dao;
	private final QueueClient queue;
	private final ObjectMapper mapper;

	public AlertInitTask(final ObjectMapper mapper, final PeopleJDBi dao, final QueueClient queue)
	{
		this.dao = dao;
		this.queue = queue;
		this.mapper = mapper;

		log.info("INITIALIZED");
	}

	@Override
	public boolean process(final AlertInitRequest request) throws Exception
	{
		if (null == request.timezoneId) throw new AbortException("The request is missing the timezoneId.");

		var zone = Timezone.get(request.timezoneId);
		if (null == zone) throw new AbortException("The timezone '" + request.timezoneId + "' does not receive alerts.");

		log.info("PROCESSING: {}", zone);

		var lastId = "";
		List<String> ids = null;
		int count = 0, batches = 0;
		var timer = new StopWatch();
		while (!(ids = dao.getActiveAlertableIds(lastId, zone, PAGE_SIZE)).isEmpty())
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
