package app.allclear.common.resources;

import static org.fest.assertions.api.Assertions.assertThat;
import static app.allclear.testing.TestingUtils.HTTP_STATUS_OK;

import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import ch.qos.logback.classic.LoggerContext;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.log.*;
import app.allclear.common.mediatype.UTF8MediaType;

/** Functional test class that verifies the Log RESTful resource.
 * 
 * @author smalleyd
 * @version 1.0.45
 * @since 4/5/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class LogResourceTest
{
	public final ResourceExtension RULE = ResourceExtension.builder().addResource(new LogResource()).build();

	private static final GenericType<QueryResults<LogValue, LogFilter>> TYPE_QUERY_RESULTS = new GenericType<QueryResults<LogValue, LogFilter>>() {};

	private static LogValue[] VALUES = null;
	private static long TOTAL_LOGGERS;

	@Test
	public void get()
	{
		var response = RULE.client().target("/logs").request(UTF8MediaType.APPLICATION_JSON_TYPE).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var values = VALUES = response.readEntity(LogValue[].class);
		Assertions.assertNotNull(values, "Exists");
		// Assertions.assertEquals(65, values.length, "Check length");
		assertThat(values.length).as("Check length").isGreaterThan(0);	// Can't guarantee the number of logs. DLS on 6/16/2015.

		for (var value : values) getByName(value);
	}

	/** Helper method - gets a log by name. */
	private void getByName(final LogValue expected)
	{
		if (StringUtils.isEmpty(expected.name) || "/".equals(expected.name))
			return;

		var assertId = "GET (" + expected.name + ", " + expected.level + "): ";
		var response = RULE.client().target("/logs").path(expected.name)
			.request(UTF8MediaType.APPLICATION_JSON_TYPE)
			.get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), assertId + "Status");

		var value = response.readEntity(LogValue.class);
		Assertions.assertNotNull(value, assertId + "Exists");
		Assertions.assertEquals(expected.name, value.name, assertId + "Check name");
		Assertions.assertEquals(expected.level, value.level, assertId + "Check level");
	}

	@Test
	public void search()
	{
		TOTAL_LOGGERS = (long) ((LoggerContext) LoggerFactory.getILoggerFactory()).getLoggerList().size();
		search(new LogFilter(), TOTAL_LOGGERS);
		// search(new LogFilter().withName("JERSEY"), 15L);
		search(new LogFilter().withLevel("INFO"), 1L);
		search(new LogFilter().withLevel("WARN"), TOTAL_LOGGERS - 2);	// Subtract the INFO and DEBUG loggers.
		search(new LogFilter().withLevel("DEBUG"), 1L);
		search(new LogFilter().withLevel("OFF"), 0L);
	}

	private void search(final LogFilter filter, final long expectedCount)
	{
		var assertId = "SEARCH " + filter + ": ";
		var response = RULE.client().target("/logs/search")
			.request(UTF8MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.entity(filter, UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), assertId + "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assertions.assertNotNull(results, assertId + "Exists");
		Assertions.assertEquals(expectedCount, results.total, assertId + "Check size");
	}

	@Test
	public void update()
	{
		for (LogValue value : VALUES)
			update(new LogValue(value.name, "DEBUG"));
	}

	@Test
	public void update_post()
	{
		search(new LogFilter().withLevel("DEBUG"), TOTAL_LOGGERS);
		search(new LogFilter().withLevel("WARN"), 0L);
	}

	/** Helper method - calls the log level update endpoint. */
	private void update(final LogValue value)
	{
		var assertId = "UPDATE (" + value.name + ", " + value.level + "): ";
		var response = RULE.client().target("/logs")
			.request(UTF8MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.entity(value, UTF8MediaType.APPLICATION_JSON_TYPE));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), assertId + "Status");

		var result = response.readEntity(LogValue.class);
		Assertions.assertNotNull(result, assertId + "Exists");
	}
}
