package app.allclear.common.azure;

import java.util.List;

import javax.ws.rs.*;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.task.*;

/** Jersey resource class that provides access to the QueueManager.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 4/19/2020
 *
 */

@Path("/queues")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api("Queues")
public class QueueResource
{
	private final QueueManager queue;

	/** Populator.
	 * 
	 * @param queue
	 */
	public QueueResource(final QueueManager queue)
	{
		this.queue = queue;
	}

	@GET
	@Path("/stats") @Timed
	@ApiOperation(value="stats", notes="Retrieves the statistics for each operator in the QueueManager.", response=OperatorStats.class, responseContainer="List")
	public List<OperatorStats> stats()
	{
		return queue.stats();
	}
}
