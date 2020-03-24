package app.allclear.common.resources;

import java.util.List;

import javax.ws.rs.*;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.dao.QueryFilter;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.task.*;
import app.allclear.common.value.CountResults;
import app.allclear.common.value.OperationResponse;

/** Jersey resource class that provides access to the task queue.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

@Path("/queues")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api("Queues")
public class QueueResource
{
	private final TaskManager task;

	/** Populator.
	 * 
	 * @param task
	 */
	public QueueResource(final TaskManager task)
	{
		this.task = task;
	}

	@GET
	@Path("/{name}/count") @Timed
	@ApiOperation(value="count", notes="Retrieves the number of requests on the specified queue.", response=CountResults.class)
	public CountResults count(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name)
		throws Exception
	{
		return new CountResults(task.countRequests(name));
	}

	@GET
	@Path("/{name}/dlq/count") @Timed
	@ApiOperation(value="countDLQ", notes="Retrieves the number of requests on the specified queue.", response=CountResults.class)
	public CountResults countDLQ(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name)
		throws Exception
	{
		return new CountResults(task.countDLQ(name));
	}

	@GET
	@Path("/{name}/{id}") @Timed
	@ApiOperation(value="get", notes="Retrieves a single request from the specific queue by the identifier.", response=TaskRequest.class)
	public TaskRequest<?> get(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name,
		@PathParam("id") @ApiParam(name="id", value="Represents the identifier of the request.") final String id) throws Exception
	{
		var values = task.listRequests(name);
		if (null == values)
			return null;

		return values.stream().filter(v -> id.equals(v.id)).findFirst().orElse(null);
	}

	@GET
	@Path("/{name}/dlq/{id}") @Timed
	@ApiOperation(value="getDLQ", notes="Retrieves a single request from the specific queue by the identifier.", response=TaskRequest.class)
	public TaskRequest<?> getDLQ(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name,
		@PathParam("id") @ApiParam(name="id", value="Represents the identifier of the request.") final String id) throws Exception
	{
		var values = task.listDLQ(name);
		if (null == values)
			return null;

		return values.stream().filter(v -> id.equals(v.id)).findFirst().orElse(null);
	}

	@POST
	@Path("/{name}") @Timed
	@ApiOperation(value="runAll", notes="Runs all requests in the queue.", response=CountResults.class)
	public CountResults runAll(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name) throws Exception
	{
		return new CountResults(task.process(name));
	}

	@POST
	@Path("/{name}/dlq") @Timed
	@ApiOperation(value="reRunDLQ", notes="Moves all requests in the DLQ back to the operational queue.", response=CountResults.class)
	public CountResults reRunDLQ(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name) throws Exception
	{
		return new CountResults(task.moveRequests(name));
	}

	@POST
	@Path("/{name}/{id}") @Timed
	@ApiOperation(value="run", notes="Runs a single request in the queue.", response=OperationResponse.class)
	public OperationResponse run(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name,
		@PathParam("id") @ApiParam(name="id", value="") final String id) throws Exception
	{
		return new OperationResponse(task.process(name, id));
	}

	@POST
	@Path("/{name}/dlq/{id}") @Timed
	@ApiOperation(value="runDLQ", notes="Runs a single request in the DLQ.", response=OperationResponse.class)
	public OperationResponse runDLQ(@PathParam("name") @ApiParam(name="name", value="Represents the name of the DLQ.") final String name,
		@PathParam("id") @ApiParam(name="id", value="") final String id) throws Exception
	{
		return new OperationResponse(task.processDLQ(name, id));
	}

	@PUT
	@Path("/{name}") @Timed
	@ApiOperation(value="update", notes="Updates a single request in the queue.", response=TaskRequest.class)
	public TaskRequest<?> update(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name,
		final TaskRequest<String> request) throws Exception
	{
		return task.modifyRequest(name, request);
	}

	@PUT
	@Path("/{name}/dlq") @Timed
	@ApiOperation(value="updateDLQ", notes="Updates a single request in the DLQ.", response=TaskRequest.class)
	public TaskRequest<?> updateDLQ(@PathParam("name") @ApiParam(name="name", value="Represents the name of the DLQ.") final String name,
		final TaskRequest<String> request) throws Exception
	{
		return task.modifyDLQ(name, request);
	}

	@DELETE
	@Path("/{name}/{id}") @Timed
	@ApiOperation(value="remove", notes="Removes a single request from the queue by the identifier.", response=OperationResponse.class)
	public OperationResponse remove(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name,
		final @PathParam("id") @ApiParam(name="id", value="Represents the identifier of the request.") String id) throws Exception
	{
		return new OperationResponse(task.removeRequest(name, id));
	}

	@DELETE
	@Path("/{name}/dlq/{id}") @Timed
	@ApiOperation(value="remove", notes="Removes a single request from the DLQ by the identifier.", response=OperationResponse.class)
	public OperationResponse removeDLQ(@PathParam("name") @ApiParam(name="name", value="Represents the name of the DLQ.") final String name,
		final @PathParam("id") @ApiParam(name="id", value="Represents the identifier of the request.") String id) throws Exception
	{
		return new OperationResponse(task.removeDLQ(name, id));
	}

	@DELETE
	@Path("/{name}") @Timed
	@ApiOperation(value="clear", notes="Clears all request from the specified queue.", response=CountResults.class)
	public CountResults clear(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name) throws Exception
	{
		return new CountResults(task.clearRequests(name));
	}

	@DELETE
	@Path("/{name}/dlq") @Timed
	@ApiOperation(value="clearDLQ", notes="Clears all request from the specified DLQ.", response=CountResults.class)
	public CountResults clearDLQ(@PathParam("name") @ApiParam(name="name", value="Represents the name of the DLQ.") final String name) throws Exception
	{
		return new CountResults(task.clearDLQ(name));
	}

	@POST
	@Path("/{name}/search") @Timed
	@ApiOperation(value="search", notes="Searches the Queue based on the supplied filter.", response=QueryResults.class)
	public QueryResults<TaskRequest<?>, QueryFilter> search(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name,
		final QueryFilter filter) throws Exception
	{
		final QueryResults<TaskRequest<?>, QueryFilter> value = new QueryResults<>((long) task.countRequests(name), filter);

		return value.withRecords(task.listRequests(name, value.page, value.pageSize));
	}

	@POST
	@Path("/{name}/dlq/search") @Timed
	@ApiOperation(value="searchDLQ", notes="Searches the Dead-Letter-Queue based on the supplied filter.", response=QueryResults.class)
	public QueryResults<TaskRequest<?>, QueryFilter> searchDLQ(@PathParam("name") @ApiParam(name="name", value="Represents the name of the queue.") final String name,
		final QueryFilter filter) throws Exception
	{
		final QueryResults<TaskRequest<?>, QueryFilter> value = new QueryResults<>((long) task.countDLQ(name), filter);

		return value.withRecords(task.listDLQ(name, value.page, value.pageSize));
	}

	@GET
	@Path("/stats") @Timed
	@ApiOperation(value="stats", notes="Retrieves the statistics for each operator in the TaskManager.", response=OperatorStats.class, responseContainer="List")
	public List<OperatorStats> stats()
	{
		return task.stats();
	}
}
