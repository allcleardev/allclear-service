package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static app.allclear.platform.type.CrowdsourceStatus.*;
import static app.allclear.platform.type.Originator.*;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import com.azure.storage.queue.QueueClient;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.OperationResponse;
import app.allclear.junit.hibernate.*;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.FacilitateFilter;
import app.allclear.platform.value.*;

/** Functional test class that verifies the FacilitateDAO component.
 * 
 * @author smalleyd
 * @version 1.1.60
 * @since 5/21/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@Disabled
public class FacilitateResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static FacilitateDAO dao;
	private static FacilityDAO facilityDao;
	private static final QueueClient queue = mock(QueueClient.class);
	private static final SessionDAO sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());

	private static SessionValue ADMIN = new SessionValue(false, new AdminValue("admin"));
	private static SessionValue CUSTOMER = new SessionValue(new CustomerValue("customer").withId("customer1"));
	private static SessionValue EDITOR = new SessionValue(false, new AdminValue("editor", false, true));
	private static SessionValue PERSON = new SessionValue(false, new PeopleValue("person", "8885551000", true).withId("person"));
	private static final FacilityValue FACILITY_1 = new FacilityValue(1);
	private static final FacilityValue FACILITY_2 = new FacilityValue(2);
	private static FacilitateValue ADD_CITIZEN;
	private static FacilitateValue ADD_PROVIDER;
	private static FacilitateValue CHANGE_CITIZEN;
	private static FacilitateValue CHANGE_PROVIDER;

	private static int notifications = 0;
	private static int notifications_ = 0;

	private static final GenericType<Map<String, Object>> TYPE_MAP = new GenericType<Map<String, Object>>() {};
	private static final GenericType<QueryResults<FacilitateValue, FacilitateFilter>> TYPE_QUERY_RESULTS = new GenericType<QueryResults<FacilitateValue, FacilitateFilter>>() {};

	private final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new FacilitateResource(dao))
		.build();

	private static final String TARGET = "/facilitates";

	@BeforeAll
	public static void up() throws Exception
	{
		var factory = DAO_RULE.getSessionFactory();

		facilityDao = new FacilityDAO(factory, new TestAuditor());
		when(queue.sendMessage(any(String.class))).thenAnswer(a -> { notifications_++; return null; });
		dao = new FacilitateDAO(System.getenv("AUDIT_LOG_CONNECTION_STRING"), facilityDao, sessionDao, queue);
	}

	@BeforeEach
	public void beforeEach()
	{
		sessionDao.current(ADMIN);
	}

	@AfterEach
	public void afterEach()
	{
		Assertions.assertEquals(notifications, notifications_, "Check notifications");
	}

	@Test
	public void add()
	{
		facilityDao.add(FACILITY_1, true);
		facilityDao.add(FACILITY_2, true);
	}

	@Test
	public void add_citizen()
	{
		count(0L);

		sessionDao.clear();

		var response = request("citizen").post(Entity.json(new FacilitateValue(new FacilityValue(3), null, true)));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		ADD_CITIZEN = response.readEntity(FacilitateValue.class);
		Assertions.assertNotNull(ADD_CITIZEN, "Exists");

		notifications++;
	}

	@Test
	public void add_provider()
	{
		count(1L);

		sessionDao.current(PERSON);

		ADD_PROVIDER = request("provider").post(Entity.json(new FacilitateValue(new FacilityValue(), "Around the corner", false)), FacilitateValue.class);
		Assertions.assertNotNull(ADD_PROVIDER, "Exists");

		notifications++;
	}

	@Test
	public void change_citizen()
	{
		count(2L);

		sessionDao.clear();

		CHANGE_CITIZEN = request("citizen").put(Entity.json(new FacilitateValue(FACILITY_1.withState("Nebraska"), "One town over", true)), FacilitateValue.class);
		Assertions.assertNotNull(CHANGE_CITIZEN, "Exists");

		notifications++;
	}

	@Test
	public void change_provider()
	{
		count(3L);

		sessionDao.current(PERSON);

		CHANGE_PROVIDER = request("provider").put(Entity.json(new FacilitateValue(FACILITY_2.withCity("Albuquerque"), null, false)), FacilitateValue.class);
		Assertions.assertNotNull(CHANGE_PROVIDER, "Exists");

		notifications++;
	}

	@Test
	public void count()
	{
		count(4L);
	}

	private FacilitateValue get(final String statusId, final String createdAt)
	{
		return request(statusId + "/" + createdAt).get(FacilitateValue.class);
	}

	private FacilitateValue get(final FacilitateValue value)
	{
		return get(value.statusId, value.createdAt());
	}

	@Test
	public void get()
	{
		var v = get(ADD_PROVIDER);
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNull(v.entityId, "Check entityId");
		check(ADD_PROVIDER, v);
	}

	public static Stream<Arguments> get_failure()
	{
		return Stream.of(
			arguments(null, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), HTTP_STATUS_NOT_AUTHORIZED, "Must be an Editor session."),
			arguments(CUSTOMER, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), HTTP_STATUS_NOT_AUTHORIZED, "Must be an Editor session."),
			arguments(PERSON, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), HTTP_STATUS_NOT_AUTHORIZED, "Must be an Editor session."),
			arguments(ADMIN, PROMOTED.id, ADD_PROVIDER.createdAt(), HTTP_STATUS_NOT_FOUND, "Could not find the Facilitate because id '"),
			arguments(EDITOR, ADD_PROVIDER.statusId, System.currentTimeMillis() + "", HTTP_STATUS_NOT_FOUND, "Could not find the Facilitate because id '"));
	}

	@ParameterizedTest
	@MethodSource
	public void get_failure(final SessionValue session, final String statusId, final String createdAt, final int status, final String message)
	{
		sessionDao.current(session);

		var response = request(statusId + "/" + createdAt).get();
		Assertions.assertEquals(status, response.getStatus(), "Status");

		var ex = response.readEntity(TYPE_MAP);
		assertThat(ex).containsKey("message");
		assertThat((String) ex.get("message")).contains(message);
	}

	@Test
	public void promote_add_citizen()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 4L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 0L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 0L);

		sessionDao.current(EDITOR);

		var v = get(ADD_CITIZEN);
		Assertions.assertEquals(OPEN.id, v.statusId, "Check statusId");
		Assertions.assertEquals(OPEN, v.status, "Check status");
		Assertions.assertNull(v.entityId, "Check entityId");
		Assertions.assertNull(v.promoterId, "Check promoterId");
		Assertions.assertNull(v.promotedAt, "Check promotedAt");
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");

		ADD_CITIZEN = request(ADD_CITIZEN.statusId + "/" + ADD_CITIZEN.createdAt() + "/promote").method(HttpMethod.POST, FacilitateValue.class);
	}

	@Test
	public void promote_add_citizen_check()
	{
		var v = get(ADD_CITIZEN);
		var facilityId = FACILITY_2.id + 1L;
		Assertions.assertEquals(PROMOTED.id, v.statusId, "Check statusId");
		Assertions.assertEquals(PROMOTED, v.status, "Check status");
		Assertions.assertEquals(facilityId, v.entityId, "Check entityId");	// Added after FACILITY_2.
		Assertions.assertEquals(EDITOR.admin.id, v.promoterId, "Check promoterId");
		assertThat(v.promotedAt).as("Check promotedAt").isNotNull().isAfter(v.createdAt).isCloseTo(new Date(), 1000L);
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");
		Assertions.assertEquals("Test Center 3", facilityDao.getById(facilityId, true).name, "Check facility.name");
	}

	@Test
	public void promote_add_citizen_count()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 3L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 1L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 0L);
	}

	@Test
	public void promote_change_provider()
	{
		var v = get(CHANGE_PROVIDER);
		Assertions.assertEquals(OPEN.id, v.statusId, "Check statusId");
		Assertions.assertEquals(OPEN, v.status, "Check status");
		Assertions.assertEquals(FACILITY_2.id, v.entityId, "Check entityId");
		Assertions.assertNull(v.promoterId, "Check promoterId");
		Assertions.assertNull(v.promotedAt, "Check promotedAt");
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");

		CHANGE_PROVIDER = request(CHANGE_PROVIDER.statusId + "/" + CHANGE_PROVIDER.createdAt() + "/promote")
			.post(Entity.json(FACILITY_2), FacilitateValue.class);
	}

	@Test
	public void promote_change_provider_check()
	{
		var v = get(CHANGE_PROVIDER);
		Assertions.assertEquals(PROMOTED.id, v.statusId, "Check statusId");
		Assertions.assertEquals(PROMOTED, v.status, "Check status");
		Assertions.assertEquals(FACILITY_2.id, v.entityId, "Check entityId");
		Assertions.assertEquals(ADMIN.admin.id, v.promoterId, "Check promoterId");
		assertThat(v.promotedAt).as("Check promotedAt").isNotNull().isAfter(v.createdAt).isCloseTo(new Date(), 1000L);
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");
		Assertions.assertEquals("Test Center 2", facilityDao.getById(FACILITY_2.id, true).name, "Check facility.name");
	}

	@Test
	public void promote_change_provider_count()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 2L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 2L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 0L);
	}

	@Test
	public void reject()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 2L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 2L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 0L);

		var v = get(CHANGE_CITIZEN);
		Assertions.assertEquals(OPEN.id, v.statusId, "Check statusId");
		Assertions.assertEquals(OPEN, v.status, "Check status");
		Assertions.assertEquals(FACILITY_1.id, v.entityId, "Check entityId");
		Assertions.assertNull(v.promoterId, "Check promoterId");
		Assertions.assertNull(v.promotedAt, "Check promotedAt");
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");

		CHANGE_CITIZEN = request(CHANGE_CITIZEN.statusId + "/" + CHANGE_CITIZEN.createdAt() + "/reject").delete(FacilitateValue.class);
	}

	@Test
	public void reject_check()
	{
		var v = get(CHANGE_CITIZEN);
		Assertions.assertEquals(REJECTED.id, v.statusId, "Check statusId");
		Assertions.assertEquals(REJECTED, v.status, "Check status");
		Assertions.assertEquals(FACILITY_1.id, v.entityId, "Check entityId");
		Assertions.assertNull(v.promoterId, "Check promoterId");
		Assertions.assertNull(v.promotedAt, "Check promotedAt");
		Assertions.assertEquals(ADMIN.admin.id, v.rejecterId, "Check rejecterId");
		assertThat(v.rejectedAt).as("Check rejectedAt").isNotNull().isAfter(v.createdAt).isCloseTo(new Date(), 1000L);
	}

	@Test
	public void reject_count()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 1L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 2L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 1L);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new FacilitateFilter(), 4L),
			arguments(new FacilitateFilter().withLocation("Around the corner"), 1L),
			arguments(new FacilitateFilter().withLocation("One town over"), 1L),
			arguments(new FacilitateFilter().withGotTested(true), 2L),
			arguments(new FacilitateFilter().withGotTested(false), 2L),
			arguments(new FacilitateFilter().withStatusId(OPEN.id), 1L),
			arguments(new FacilitateFilter().withStatusId(PROMOTED.id), 2L),
			arguments(new FacilitateFilter().withStatusId(REJECTED.id), 1L),
			arguments(new FacilitateFilter().withOriginatorId(CITIZEN.id), 2L),
			arguments(new FacilitateFilter().withOriginatorId(PROVIDER.id), 2L),
			arguments(new FacilitateFilter().withChange(true), 2L),
			arguments(new FacilitateFilter().withChange(false), 2L),
			arguments(new FacilitateFilter().withEntityId(FACILITY_1.id), 1L),
			arguments(new FacilitateFilter().withEntityId(FACILITY_2.id), 1L),
			arguments(new FacilitateFilter().withEntityId(FACILITY_2.id + 1L), 1L),
			arguments(new FacilitateFilter().withPromoterId(ADMIN.admin.id), 1L),
			arguments(new FacilitateFilter().withPromoterId(EDITOR.admin.id), 1L),
			arguments(new FacilitateFilter().withPromotedAtFrom(hourAgo), 2L),
			arguments(new FacilitateFilter().withPromotedAtTo(hourAhead), 2L),
			arguments(new FacilitateFilter().withPromotedAtFrom(hourAgo).withPromotedAtTo(hourAhead), 2L),
			arguments(new FacilitateFilter().withRejecterId(ADMIN.admin.id), 1L),
			arguments(new FacilitateFilter().withRejectedAtFrom(hourAgo), 1L),
			arguments(new FacilitateFilter().withRejectedAtTo(hourAhead), 1L),
			arguments(new FacilitateFilter().withRejectedAtFrom(hourAgo).withRejectedAtTo(hourAhead), 1L),
			arguments(new FacilitateFilter().withCreatorId(PERSON.person.id), 2L),
			arguments(new FacilitateFilter().withCreatedAtFrom(hourAgo), 4L),
			arguments(new FacilitateFilter().withCreatedAtTo(hourAhead), 4L),
			arguments(new FacilitateFilter().withCreatedAtFrom(hourAgo).withCreatedAtTo(hourAhead), 4L),
			arguments(new FacilitateFilter().withUpdatedAtFrom(hourAgo), 4L),
			arguments(new FacilitateFilter().withUpdatedAtTo(hourAhead), 4L),
			arguments(new FacilitateFilter().withUpdatedAtFrom(hourAgo).withUpdatedAtTo(hourAhead), 4L),

			// Negative tests
			arguments(new FacilitateFilter().withLocation("My street"), 0L),
			arguments(new FacilitateFilter().withStatusId("1"), 0L),
			arguments(new FacilitateFilter().withOriginatorId("1"), 0L),
			arguments(new FacilitateFilter().withEntityId(FACILITY_2.id + 2L), 0L),
			arguments(new FacilitateFilter().withPromoterId(CUSTOMER.customer.id), 0L),
			arguments(new FacilitateFilter().withPromoterId(PERSON.person.id), 0L),
			arguments(new FacilitateFilter().withPromotedAtFrom(hourAhead), 0L),
			arguments(new FacilitateFilter().withPromotedAtTo(hourAgo), 0L),
			arguments(new FacilitateFilter().withPromotedAtFrom(hourAhead).withPromotedAtTo(hourAgo), 0L),
			arguments(new FacilitateFilter().withRejecterId(CUSTOMER.customer.id), 0L),
			arguments(new FacilitateFilter().withRejecterId(EDITOR.admin.id), 0L),
			arguments(new FacilitateFilter().withRejecterId(PERSON.person.id), 0L),
			arguments(new FacilitateFilter().withRejectedAtFrom(hourAhead), 0L),
			arguments(new FacilitateFilter().withRejectedAtTo(hourAgo), 0L),
			arguments(new FacilitateFilter().withRejectedAtFrom(hourAhead).withRejectedAtTo(hourAgo), 0L),
			arguments(new FacilitateFilter().withCreatorId(ADMIN.admin.id), 0L),
			arguments(new FacilitateFilter().withCreatorId(CUSTOMER.customer.id), 0L),
			arguments(new FacilitateFilter().withCreatorId(EDITOR.admin.id), 0L),
			arguments(new FacilitateFilter().withCreatedAtFrom(hourAhead), 0L),
			arguments(new FacilitateFilter().withCreatedAtTo(hourAgo), 0L),
			arguments(new FacilitateFilter().withCreatedAtFrom(hourAhead).withCreatedAtTo(hourAgo), 0L),
			arguments(new FacilitateFilter().withUpdatedAtFrom(hourAhead), 0L),
			arguments(new FacilitateFilter().withUpdatedAtTo(hourAgo), 0L),
			arguments(new FacilitateFilter().withUpdatedAtFrom(hourAhead).withUpdatedAtTo(hourAgo), 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final FacilitateFilter filter, final long expectedTotal)
	{
		var results = request("search").post(Entity.json(filter), TYPE_QUERY_RESULTS);
		Assertions.assertNotNull(results, "Exists");
		Assertions.assertEquals(expectedTotal, results.total, "Check total");
		if (0L == expectedTotal)
			Assertions.assertNull(results.records, "Records exist");
		else
		{
			Assertions.assertNotNull(results.records, "Records exists");
			int total = (int) expectedTotal;
			if (total > results.pageSize)
			{
				if (results.page == results.pages)
					total%= results.pageSize;
				else
					total = results.pageSize;
			}
			Assertions.assertEquals(total, results.records.size(), "Check records.size");
		}
	}

	public static Stream<SessionValue> testRemove_as()
	{
		return Stream.of(null, CUSTOMER, EDITOR, PERSON);
	}

	@ParameterizedTest
	@MethodSource
	public void testRemove_as(final SessionValue session)
	{
		sessionDao.current(session);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(ADD_CITIZEN.id).delete().getStatus());
	}

	@Test
	public void testRemove_success()
	{
		Assertions.assertTrue(request(ADD_CITIZEN.id).delete(OperationResponse.class).operation);
		count(3L);
		Assertions.assertTrue(request(ADD_PROVIDER.id).delete(OperationResponse.class).operation);
		count(2L);
		Assertions.assertTrue(request(CHANGE_CITIZEN.id).delete(OperationResponse.class).operation);
		count(1L);
		Assertions.assertTrue(request(CHANGE_PROVIDER.id).delete(OperationResponse.class).operation);
		count(0L);
	}

	@Test
	public void testRemove_success_again()
	{
		Assertions.assertFalse(request(ADD_CITIZEN.id).delete(OperationResponse.class).operation);
		Assertions.assertFalse(request(ADD_PROVIDER.id).delete(OperationResponse.class).operation);
		Assertions.assertFalse(request(CHANGE_CITIZEN.id).delete(OperationResponse.class).operation);
		Assertions.assertFalse(request(CHANGE_PROVIDER.id).delete(OperationResponse.class).operation);
	}

	private void count(final long expected) { count(new FacilitateFilter(), expected); }
	private void count(final FacilitateFilter filter, final long expected)
	{
		Assertions.assertEquals(expected, dao.count(filter), "COUNT: " + filter);
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }

	private void check(final FacilitateValue expected, final FacilitateValue actual)
	{
		Assertions.assertEquals(expected.id, actual.id, "Check id");
		Assertions.assertEquals(expected.value, actual.value, "Check value");
		Assertions.assertEquals(expected.location, actual.location, "Check location");
		Assertions.assertEquals(expected.gotTested, actual.gotTested, "Check gotTested");
		Assertions.assertEquals(expected.originatorId, actual.originatorId, "Check originatorId");
		Assertions.assertEquals(expected.originator, actual.originator, "Check originator");
		Assertions.assertEquals(expected.statusId, actual.statusId, "Check statusId");
		Assertions.assertEquals(expected.status, actual.status, "Check status");
		Assertions.assertEquals(expected.change, actual.change, "Check change");
		Assertions.assertEquals(expected.entityId, actual.entityId, "Check entityId");
		Assertions.assertEquals(expected.promoterId, actual.promoterId, "Check promoterId");
		Assertions.assertEquals(expected.promotedAt, actual.promotedAt, "Check promotedAt");
		Assertions.assertEquals(expected.rejecterId, actual.rejecterId, "Check rejectedId");
		Assertions.assertEquals(expected.rejectedAt, actual.rejectedAt, "Check rejectedAt");
		Assertions.assertEquals(expected.creatorId, actual.creatorId, "Check creatorId");
		assertThat(actual.createdAt).as("Check createdAt").isInSameSecondAs(expected.createdAt);
		assertThat(actual.updatedAt).as("Check updatedAt").isInSameSecondAs(expected.updatedAt);
	}
}
