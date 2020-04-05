package app.allclear.common.resources;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.*;

import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.log.*;
import app.allclear.common.mediatype.UTF8MediaType;

/** RESTful Jersey resource to manage application logger.
 * 
 * @author smalleyd
 * @version 1.0.45
 * @since 4/5/2020
 *
 */

@Path("/logs")
@Api(value="Log")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
public class LogResource
{
	private static final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

	@GET
	@Path("/{name}")
	@ApiOperation(value="getByName", notes="Gets the details of a single log entry by name.")
	public LogValue getByName(@PathParam("name") @ApiParam(name="name", value="Represents the full class of the log.") final String name)
	{
		var value = context.getLogger(name);
		if (null == value) return null;

		return toValue(value);
	}

	@GET
	@ApiOperation(value="getAll", notes="Gets the details of all the logs in the application.")
	public List<LogValue> getAll()
	{
		return context.getLoggerList().stream().map(o -> toValue(o)).collect(Collectors.toList());
	}

	@POST
	@ApiOperation(value="submit", notes="Updates the reporting level of a single log.")
	public LogValue submit(final LogValue value)
	{
		context.getLogger(value.name).setLevel(Level.valueOf(value.level));

		return value;
	}

	@POST
	@Path("/search")
	@ApiOperation(value="search", notes="Searches the logs by the specified filter.")
	public QueryResults<LogValue, LogFilter> search(final LogFilter filter)
	{
		filter.clean();
		var records = context.getLoggerList().stream().filter(o -> filter.match(o))
			.map(o -> toValue(o))
			.collect(Collectors.toList());

		var results = new QueryResults<LogValue, LogFilter>((long) records.size(), filter);
		if (records.isEmpty()) return results;

		int first = results.firstResult();
		int last = first + results.pageSize;
		if (records.size() < last) last = records.size();

		return results.withRecords(records.subList(first, last));
	}

	/** Helper method - converts a Logger to a LogValue. */
	@ApiOperation(value="toValue", hidden=true)	// Make sure that Swagger doesn't try to document the method with the "Logger" object. It has proeperties that it can't handle. DLS on 6/22/2015.
	private LogValue toValue(Logger record)
	{
		return new LogValue(record.getName(), record.getEffectiveLevel().toString());
	}
}
