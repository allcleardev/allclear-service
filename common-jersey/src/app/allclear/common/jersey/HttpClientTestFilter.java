package app.allclear.common.jersey;

import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.*;

/** Jersey client request filter that logs the details of the requests and aborts the operation
 *  with an empty OK response.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class HttpClientTestFilter implements ClientRequestFilter
{
	private static final Logger log = LoggerFactory.getLogger(HttpClientTestFilter.class);

	/** Represents the response payload that the filter returns as the "entity". */
	private static Object response = null;
	private static Queue<Object> responses = new LinkedList<>();	// Provides for consecutive responses across several calls.

	/** Sets the current response payload to be returned by the filter.
	 * 
	 * @param value
	 */
	public static void setResponse(final Object value)
	{
		response = value;
	}

	/** Queues one or more responses to be returned in the order supplied. */
	public static void addResponses(final Object... values)
	{
		for (var v : values) responses.offer(v);
	}

	/** Resets the current response payload to NULL. */
	public static void clearResponse()
	{
		response = null;
		responses.clear();
	}

	@Override
	public void filter(final ClientRequestContext context)
	{
		var headers = context.getStringHeaders().entrySet()
			.stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ", "{ ", " }"));

		log.info("Method: {}, URL: {}, Headers: {}, Entity: {}", context.getMethod(), context.getUri().toString(), headers, context.getEntity());

		var builder = Response.ok();
		if (!responses.isEmpty())
			builder.type(MediaType.APPLICATION_JSON_TYPE).entity(responses.poll());

		else if (null != response)
			builder.type(MediaType.APPLICATION_JSON_TYPE).entity(response);

		context.abortWith(builder.build());
	}
}
