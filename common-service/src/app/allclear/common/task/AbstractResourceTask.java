package app.allclear.common.task;

import java.util.Map;

import org.slf4j.*;

import app.allclear.common.mediatype.UTF8MediaType;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;

/** Background task that provides helper methods to process the request by making a local RESTful call.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public abstract class AbstractResourceTask<T> implements TaskCallback<T>
{
	private static final Logger log = LoggerFactory.getLogger(AbstractResourceTask.class);

	/** Represents the port of the local server. */
	private final WebTarget resource;
	private final String title;

	/** Populator.
	 * 
	 * @param port
	 * @param path
	 * @param title
	 */
	public AbstractResourceTask(final int port, final String path, final String title)
	{
		var url = "http://localhost:" + port + "/" + path;
		log.info("Base URL: " + url);

		this.resource = ClientBuilder.newClient().target(url);
		this.title = title;
	}

	/** Make the HTTP call to the local resource.
	 * 
	 * @param id
	 * @return TRUE on success
	 * @throws Exception
	 */
	public boolean doProcess(final Number id) throws Exception
	{
		return doProcess(id.toString());
	}

	/** Make the HTTP call to the local resource.
	 * 
	 * @param id
	 * @return TRUE on success
	 * @throws Exception
	 */
	public boolean doProcess(final String id) throws Exception
	{
		var response = invoke(resource.path(id).request(UTF8MediaType.APPLICATION_JSON_TYPE)).method(HttpMethod.POST);
		if (300 > response.getStatus())	// Success.
			return true;	// Always return TRUE.

		var result = response.readEntity(Map.class);
		String message = (String) result.get("message");
		if (null == message)
			message = String.format("Status %d - %s", response.getStatus(), response.getStatusInfo().getReasonPhrase());

		throw new RuntimeException(String.format("%s: %s - failed to submit. Message: %s", title, id, message));
	}

	/** Make the HTTP call to the local resource.
	 * 
	 * @param value
	 * @return TRUE on success
	 * @throws Exception
	 */
	public boolean doProcess(final T value) throws Exception
	{
		var response = invoke(resource.request(UTF8MediaType.APPLICATION_JSON_TYPE)).post(Entity.entity(value, UTF8MediaType.APPLICATION_JSON_TYPE));
		if (300 > response.getStatus())	// Success.
			return true;	// Always return TRUE.

		var result = response.readEntity(Map.class);
		var message = (String) result.get("message");
		if (null == message)
			message = String.format("Status %d - %s", response.getStatus(), response.getStatusInfo().getReasonPhrase());

		throw new RuntimeException(String.format("%s: %s - failed to submit. Message: %s", title, value, message));
	}

	/** Helper method - optionally override to set application specific headers. */
	protected Invocation.Builder invoke(final Invocation.Builder builder)
	{
		return builder;
	}
}
