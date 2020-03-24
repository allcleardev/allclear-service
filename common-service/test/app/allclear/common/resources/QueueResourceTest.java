package app.allclear.common.resources;

import static org.mockito.Mockito.*;

import java.util.*;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.*;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.testing.TestingUtils;
import app.allclear.common.dao.QueryFilter;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.task.*;
import app.allclear.common.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Label entity.
*
*	@author David Small
*	@version 1.0.0
*	@since 3/24/2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class QueueResourceTest
{
	private static final Logger log = LoggerFactory.getLogger(QueueResourceTest.class);
	private static final TaskManager manager = mock(TaskManager.class);

	public static final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new QueueResource(manager)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/queues/auditLog";

	/** Generic types for reading values from responses. */
	private static final GenericType<QueryResults<TaskRequest<?>, QueryFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<TaskRequest<?>, QueryFilter>>() {};
	private static final GenericType<List<OperatorStats>> TYPE_LIST_OPERATOR_STATS = new GenericType<List<OperatorStats>>() {};

	private static final List<TaskRequest<?>> VALUES = Arrays.asList(new TaskRequest<>("abc"), new TaskRequest<>("def"), new TaskRequest<>("ghi"), new TaskRequest<>("jkl"), new TaskRequest<>("xyz"));

	private static final List<TaskRequest<?>> DLQ_VALUES = Arrays.asList(new TaskRequest<>("100"), new TaskRequest<>("200"), new TaskRequest<>("300"), new TaskRequest<>("400"), new TaskRequest<>("500"), new TaskRequest<>("600"));

	@BeforeAll
	@SuppressWarnings(value={"rawtypes", "unchecked"})
	public static void up() throws Exception
	{
		when(manager.countRequests(any(String.class))).thenReturn(5);
		when(manager.listRequests(any(String.class))).thenReturn(VALUES);
		when(manager.listRequests(any(String.class), eq(1), eq(20))).thenReturn(VALUES);
		when(manager.countDLQ(any(String.class))).thenReturn(6);
		when(manager.listDLQ(any(String.class))).thenReturn(DLQ_VALUES);
		when(manager.listDLQ(any(String.class), eq(1), eq(20))).thenReturn(DLQ_VALUES);
		when(manager.removeRequest(any(String.class), any(String.class))).thenReturn(true);
		when(manager.clearRequests(any(String.class))).thenReturn(4);
		when(manager.process(any(String.class))).thenReturn(2);
		when(manager.process("auditLog", "abc")).thenReturn(false);
		when(manager.process("auditLog", "xyz")).thenReturn(true);
		when(manager.modifyRequest(any(String.class), any(TaskRequest.class))).thenReturn(new TaskRequest());
		when(manager.stats()).thenReturn(Arrays.asList(new OperatorStats("queue1", 1, 2, 3, 4, 5)));
	}

	@Test
	public void testCount()
	{
		var response = request("count").get();
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(CountResults.class);
		Assert.assertNotNull("Exists", results);
		Assert.assertEquals("Check count", 5, results.count);
	}

	@Test
	public void get()
	{
		var v = VALUES.get(2);
		var response = get(v.id);
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var value = response.readEntity(TaskRequest.class);
		Assert.assertNotNull("Exists", value);
		Assert.assertEquals("Check ID", v.id, value.id);
	}

	/** Helper method - calls the GET endpoint. */
	private Response get(String id)
	{
		return request(id).get();
	}

	@Test
	public void runAll()
	{
		var response = request().post(Entity.entity(null, UTF8MediaType.APPLICATION_JSON_TYPE));
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(CountResults.class);
		Assert.assertNotNull("Exists", results);
		Assert.assertEquals("Check count", 2, results.count);
	}

	@Test
	public void runABC()
	{
		var response = request("abc").post(Entity.entity(null, UTF8MediaType.APPLICATION_JSON_TYPE));
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(OperationResponse.class);
		Assert.assertNotNull("Exists", results);
		Assert.assertFalse("Check count", results.operation);
	}

	@Test
	public void runXYZ()
	{
		var response = request("xyz").post(Entity.entity(null, UTF8MediaType.APPLICATION_JSON_TYPE));
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(OperationResponse.class);
		Assert.assertNotNull("Exists", results);
		Assert.assertTrue("Check count", results.operation);
	}

	@Test
	public void runAllDLQ()
	{
		var response = request("dlq").method(HttpMethod.POST);
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(CountResults.class);
		Assert.assertNotNull("Exists", results);
		Assert.assertEquals("Check count", 0, results.count);
	}

	@Test
	public void search()
	{
		search(new QueryFilter(1, 20), 5L);
	}

	/** Helper method - calls the search endpoint and verifies the counts and records. */
	private void search(final QueryFilter filter, final long expectedTotal)
	{
		var response = request("search")
			.post(Entity.entity(filter, UTF8MediaType.APPLICATION_JSON_TYPE));
		var assertId = "SEARCH " + filter + ": ";
		Assert.assertEquals(assertId + "Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assert.assertNotNull(assertId + "Exists", results);
		Assert.assertEquals(assertId + "Check total", expectedTotal, results.total);
		if (0L == expectedTotal)
			Assert.assertNull(assertId + "Records exist", results.records);
		else
		{
			Assert.assertNotNull(assertId + "Records exist", results.records);
			int total = (int) expectedTotal;
			if (total > results.pageSize)
			{
				if (results.page == results.pages)
					total%= results.pageSize;
				else
					total = results.pageSize;
			}
			Assert.assertEquals(assertId + "Check records.size", total, results.records.size());
		}
	}

	@Test
	public void searchDLQ()
	{
		searchDLQ(new QueryFilter(1, 20), 6L);
	}

	/** Helper method - calls the search endpoint and verifies the counts and records. */
	private void searchDLQ(QueryFilter filter, long expectedTotal)
	{
		var response = dlqRequest("search")
			.post(Entity.entity(filter, UTF8MediaType.APPLICATION_JSON_TYPE));
		var assertId = "SEARCH " + filter + ": ";
		Assert.assertEquals(assertId + "Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assert.assertNotNull(assertId + "Exists", results);
		Assert.assertEquals(assertId + "Check total", expectedTotal, results.total);
		if (0L == expectedTotal)
			Assert.assertNull(assertId + "Records exist", results.records);
		else
		{
			Assert.assertNotNull(assertId + "Records exist", results.records);
			int total = (int) expectedTotal;
			if (total > results.pageSize)
			{
				if (results.page == results.pages)
					total%= results.pageSize;
				else
					total = results.pageSize;
			}
			Assert.assertEquals(assertId + "Check records.size", total, results.records.size());
		}
	}

	@Test
	public void stats()
	{
		log.debug("Manager.stats: {}", manager.stats());

		// Can't use target() or request() because TARGET = /queues/auditLog not just /queues.
		var response = RULE.client().target("/queues/stats").request(UTF8MediaType.APPLICATION_JSON_TYPE).get();
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var values = response.readEntity(TYPE_LIST_OPERATOR_STATS);
		Assert.assertNotNull("Exists", values);
		Assert.assertEquals("Check size", 1, values.size());

		var value = values.get(0);
		Assert.assertEquals("Check name", "queue1", value.name);
		Assert.assertEquals("Check queueSize", 1, value.queueSize);
		Assert.assertEquals("Check dlqSize", 2, value.dlqSize);
		Assert.assertEquals("Check successes", 3, value.successes);
		Assert.assertEquals("Check skips", 4, value.skips);
		Assert.assertEquals("Check errors", 5, value.errors);
	}

	/** Test removal after the search. */
	@Test
	public void testRemove()
	{
		remove("123", true);
	}

	/** Helper method - call the DELETE endpoint. */
	private void remove(String id, boolean success)
	{
		var response = request(id).delete();
		var assertId = "DELETE (" + id + ", " + success + "): ";
		Assert.assertEquals(assertId + "Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(OperationResponse.class);
		Assert.assertNotNull(assertId + "Exists", results);
		Assert.assertEquals(assertId + "Check value", success, results.operation);
	}

	@Test
	public void testRemoveAll()
	{
		var response = request().delete();
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(CountResults.class);
		Assert.assertNotNull("Exists", results);
		Assert.assertEquals("Check count", 4, results.count);
	}

	@Test
	public void update()
	{
		var response = request()
			.put(Entity.entity(new TaskRequest<String>("id-123", "{ \"field\": \"value\" }", 1, 1L), UTF8MediaType.APPLICATION_JSON_TYPE));
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var request = response.readEntity(TaskRequest.class);
		Assert.assertNotNull("Exists", request);
		Assert.assertNotNull("ID exists", request.id);
		Assert.assertNotEquals("Check id", "id-123", request.id);
		Assert.assertNull("Check value", request.value);
		Assert.assertEquals("Check tries", 0, request.tries);
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request() { return target().request(UTF8MediaType.APPLICATION_JSON_TYPE); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request(String path) { return target().path(path).request(UTF8MediaType.APPLICATION_JSON_TYPE); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder dlqRequest(String path) { return target().path("dlq").path(path).request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
