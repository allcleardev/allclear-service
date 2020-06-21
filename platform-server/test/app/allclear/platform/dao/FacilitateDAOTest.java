package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static app.allclear.platform.type.CrowdsourceStatus.*;
import static app.allclear.platform.type.Originator.*;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.azure.storage.queue.QueueClient;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.common.errors.*;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.junit.hibernate.*;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
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
public class FacilitateDAOTest
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

		sessionDao.current(PERSON);

		ADD_CITIZEN = dao.addByCitizen(new FacilitateValue(new FacilityValue(3), null, true));

		notifications++;
	}

	public static Stream<Arguments> add_citizen_failure()
	{
		var valid = new FacilitateValue(new FacilityValue(), null, true);
		return Stream.of(
			arguments(ADMIN, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(CUSTOMER, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(EDITOR, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(null, new FacilitateValue(null, null, true), ValidationException.class, "Value is not set."),
			arguments(PERSON, new FacilitateValue(new FacilityValue(), StringUtils.repeat('A', FacilitateValue.MAX_LEN_LOCATION + 1), true), ValidationException.class, "Location 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' is longer than the expected size of 128."));
	}

	@ParameterizedTest
	@MethodSource
	public void add_citizen_failure(final SessionValue session, final FacilitateValue value, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(Assertions.assertThrows(ex, () -> dao.addByCitizen(value))).hasMessage(message);
	}

	@Test
	public void add_provider()
	{
		count(1L);

		sessionDao.clear();

		ADD_PROVIDER = dao.addByProvider(new FacilitateValue(new FacilityValue(), "Around the corner", false));

		notifications++;
	}

	public static Stream<Arguments> add_provider_failure()
	{
		var valid = new FacilitateValue(new FacilityValue(), null, true);
		return Stream.of(
			arguments(ADMIN, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(CUSTOMER, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(EDITOR, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(null, new FacilitateValue(null, null, true), ValidationException.class, "Value is not set."),
			arguments(PERSON, new FacilitateValue(new FacilityValue(), StringUtils.repeat('A', FacilitateValue.MAX_LEN_LOCATION + 1), true), ValidationException.class, "Location 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' is longer than the expected size of 128."));
	}

	@ParameterizedTest
	@MethodSource
	public void add_provider_failure(final SessionValue session, final FacilitateValue value, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(Assertions.assertThrows(ex, () -> dao.addByProvider(value))).hasMessage(message);
	}

	@Test
	public void change_citizen()
	{
		count(2L);

		sessionDao.current(PERSON);

		CHANGE_CITIZEN = dao.changeByCitizen(new FacilitateValue(FACILITY_1.withState("Nebraska"), "One town over", true));

		notifications++;
	}

	public static Stream<Arguments> change_citizen_failure()
	{
		var valid = new FacilitateValue(FACILITY_1, "One town over", true);
		return Stream.of(
			arguments(ADMIN, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(CUSTOMER, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(EDITOR, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(null, new FacilitateValue(null, null, true), ValidationException.class, "Value is not set."),
			arguments(PERSON, new FacilitateValue(FACILITY_1, StringUtils.repeat('A', FacilitateValue.MAX_LEN_LOCATION + 1), true), ValidationException.class, "Location 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' is longer than the expected size of 128."),
			arguments(null, new FacilitateValue(new FacilityValue(), null, true), ValidationException.class, "Value ID is not set."),
			arguments(null, new FacilitateValue(new FacilityValue().withId(1000L), null, true), ValidationException.class, "The Facility '1000' does not exist."));
	}

	@ParameterizedTest
	@MethodSource
	public void change_citizen_failure(final SessionValue session, final FacilitateValue value, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(Assertions.assertThrows(ex, () -> dao.changeByCitizen(value))).hasMessage(message);
	}

	@Test
	public void change_provider()
	{
		count(3L);

		sessionDao.clear();

		CHANGE_PROVIDER = dao.changeByProvider(new FacilitateValue(FACILITY_2.withCity("Albuquerque"), null, false));

		notifications++;
	}

	public static Stream<Arguments> change_provider_failure()
	{
		var valid = new FacilitateValue(FACILITY_2, null, false);
		return Stream.of(
			arguments(ADMIN, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(CUSTOMER, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(EDITOR, valid, NotAuthorizedException.class, "Must be a Person or Anonymous."),
			arguments(null, new FacilitateValue(null, null, true), ValidationException.class, "Value is not set."),
			arguments(PERSON, new FacilitateValue(FACILITY_2, StringUtils.repeat('A', FacilitateValue.MAX_LEN_LOCATION + 1), true), ValidationException.class, "Location 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' is longer than the expected size of 128."),
			arguments(null, new FacilitateValue(new FacilityValue(), null, true), ValidationException.class, "Value ID is not set."),
			arguments(null, new FacilitateValue(new FacilityValue().withId(1000L), null, true), ValidationException.class, "The Facility '1000' does not exist."));
	}

	@ParameterizedTest
	@MethodSource
	public void change_provider_failure(final SessionValue session, final FacilitateValue value, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(Assertions.assertThrows(ex, () -> dao.changeByProvider(value))).hasMessage(message);
	}

	@Test
	public void count()
	{
		count(4L);
	}

	@Test
	public void get_add_provider()
	{
		var v = dao.getById(ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt());
		Assertions.assertNotNull(v, "Exists");
		Assertions.assertNull(v.entityId, "Check entityId");
		check(ADD_PROVIDER, v);
	}

	public static Stream<FacilitateValue> getById()
	{
		return Stream.of(ADD_CITIZEN, ADD_PROVIDER, CHANGE_CITIZEN, CHANGE_PROVIDER);
	}

	@ParameterizedTest
	@MethodSource
	public void getById(final FacilitateValue value)
	{
		var v = dao.getById(value.statusId, value.createdAt());
		Assertions.assertNotNull(v, "Exists");
		check(value, v);
	}

	public static Stream<Arguments> getById_failure()
	{
		return Stream.of(
			arguments(null, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), NotAuthorizedException.class, "Must be an Editor session."),
			arguments(CUSTOMER, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), NotAuthorizedException.class, "Must be an Editor session."),
			arguments(PERSON, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), NotAuthorizedException.class, "Must be an Editor session."));
	}

	@ParameterizedTest
	@MethodSource
	public void getById_failure(final SessionValue session, final String statusId, final String createdAt, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(Assertions.assertThrows(ex, () -> dao.getById(statusId, createdAt))).hasMessage(message);
	}

	@Test
	public void getById_invalid()
	{
		Assertions.assertNull(dao.getById(PROMOTED.id, ADD_PROVIDER.createdAt()));
	}

	@ParameterizedTest
	@MethodSource("getById")
	public void getByIdWithException(final FacilitateValue value)
	{
		var v = dao.getByIdWithException(value.statusId, value.createdAt());
		Assertions.assertNotNull(v, "Exists");
		check(value, v);
	}

	public static Stream<Arguments> getByIdWithException_failure()
	{
		return Stream.of(
			arguments(null, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), NotAuthorizedException.class, "Must be an Editor session."),
			arguments(CUSTOMER, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), NotAuthorizedException.class, "Must be an Editor session."),
			arguments(PERSON, ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt(), NotAuthorizedException.class, "Must be an Editor session."),
			arguments(ADMIN, PROMOTED.id, ADD_PROVIDER.createdAt(), ObjectNotFoundException.class, "Could not find the Facilitate because id '"),
			arguments(EDITOR, ADD_PROVIDER.statusId, System.currentTimeMillis() + "", ObjectNotFoundException.class, "Could not find the Facilitate because id '"));
	}

	@ParameterizedTest
	@MethodSource
	public void getByIdWithException_failure(final SessionValue session, final String statusId, final String createdAt, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(Assertions.assertThrows(ex, () -> dao.getByIdWithException(statusId, createdAt)))
			.hasMessageContaining(message);
	}

	@Test
	public void promote_add_citizen()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 4L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 0L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 0L);

		sessionDao.current(EDITOR);

		var v = dao.getByIdWithException(ADD_CITIZEN.statusId, ADD_CITIZEN.createdAt());
		Assertions.assertEquals(OPEN.id, v.statusId, "Check statusId");
		Assertions.assertEquals(OPEN, v.status, "Check status");
		Assertions.assertNull(v.entityId, "Check entityId");
		Assertions.assertNull(v.promoterId, "Check promoterId");
		Assertions.assertNull(v.promotedAt, "Check promotedAt");
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");

		ADD_CITIZEN = dao.promote(ADD_CITIZEN.statusId, ADD_CITIZEN.createdAt(), new FacilityValue(30));
	}

	@Test
	public void promote_add_citizen_check()
	{
		var facilityId = FACILITY_2.id + 1L;
		var v = dao.getByIdWithException(ADD_CITIZEN.statusId, ADD_CITIZEN.createdAt());
		Assertions.assertEquals(PROMOTED.id, v.statusId, "Check statusId");
		Assertions.assertEquals(PROMOTED, v.status, "Check status");
		Assertions.assertEquals(facilityId, v.entityId, "Check entityId");	// Added after FACILITY_2.
		Assertions.assertEquals(EDITOR.admin.id, v.promoterId, "Check promoterId");
		assertThat(v.promotedAt).as("Check promotedAt").isNotNull().isAfter(v.createdAt).isCloseTo(new Date(), 500L);
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");
		Assertions.assertEquals("Test Center 30", facilityDao.getById(facilityId).name, "Check facility.name");
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
		var v = dao.getByIdWithException(CHANGE_PROVIDER.statusId, CHANGE_PROVIDER.createdAt());
		Assertions.assertEquals(OPEN.id, v.statusId, "Check statusId");
		Assertions.assertEquals(OPEN, v.status, "Check status");
		Assertions.assertEquals(FACILITY_2.id, v.entityId, "Check entityId");
		Assertions.assertNull(v.promoterId, "Check promoterId");
		Assertions.assertNull(v.promotedAt, "Check promotedAt");
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");

		CHANGE_PROVIDER = dao.promote(CHANGE_PROVIDER.statusId, CHANGE_PROVIDER.createdAt(), null);
	}

	@Test
	public void promote_change_provider_check()
	{
		var v = dao.getByIdWithException(CHANGE_PROVIDER.statusId, CHANGE_PROVIDER.createdAt());
		Assertions.assertEquals(PROMOTED.id, v.statusId, "Check statusId");
		Assertions.assertEquals(PROMOTED, v.status, "Check status");
		Assertions.assertEquals(FACILITY_2.id, v.entityId, "Check entityId");
		Assertions.assertEquals(ADMIN.admin.id, v.promoterId, "Check promoterId");
		assertThat(v.promotedAt).as("Check promotedAt").isNotNull().isAfter(v.createdAt).isCloseTo(new Date(), 500L);
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");
		Assertions.assertEquals("Test Center 2", facilityDao.getById(FACILITY_2.id).name, "Check facility.name");
	}

	@Test
	public void promote_change_provider_count()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 2L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 2L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 0L);
	}

	@ParameterizedTest
	@MethodSource("getByIdWithException_failure")
	public void promote_failure(final SessionValue session, final String statusId, final String createdAt, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(Assertions.assertThrows(ex, () -> dao.promote(statusId, createdAt, null)))
			.hasMessageContaining(message);
	}

	@Test
	public void reject()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 2L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 2L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 0L);

		var v = dao.getByIdWithException(CHANGE_CITIZEN.statusId, CHANGE_CITIZEN.createdAt());
		Assertions.assertEquals(OPEN.id, v.statusId, "Check statusId");
		Assertions.assertEquals(OPEN, v.status, "Check status");
		Assertions.assertEquals(FACILITY_1.id, v.entityId, "Check entityId");
		Assertions.assertNull(v.promoterId, "Check promoterId");
		Assertions.assertNull(v.promotedAt, "Check promotedAt");
		Assertions.assertNull(v.rejecterId, "Check rejecterId");
		Assertions.assertNull(v.rejectedAt, "Check rejectedAt");

		CHANGE_CITIZEN = dao.reject(CHANGE_CITIZEN.statusId, CHANGE_CITIZEN.createdAt());
	}

	@Test
	public void reject_check()
	{
		var v = dao.getByIdWithException(CHANGE_CITIZEN.statusId, CHANGE_CITIZEN.createdAt());
		Assertions.assertEquals(REJECTED.id, v.statusId, "Check statusId");
		Assertions.assertEquals(REJECTED, v.status, "Check status");
		Assertions.assertEquals(FACILITY_1.id, v.entityId, "Check entityId");
		Assertions.assertNull(v.promoterId, "Check promoterId");
		Assertions.assertNull(v.promotedAt, "Check promotedAt");
		Assertions.assertEquals(ADMIN.admin.id, v.rejecterId, "Check rejecterId");
		assertThat(v.rejectedAt).as("Check rejectedAt").isNotNull().isAfter(v.createdAt).isCloseTo(new Date(), 500L);
	}

	@Test
	public void reject_count()
	{
		count(new FacilitateFilter().withStatusId(OPEN.id), 1L);
		count(new FacilitateFilter().withStatusId(PROMOTED.id), 2L);
		count(new FacilitateFilter().withStatusId(REJECTED.id), 1L);
	}

	@ParameterizedTest
	@MethodSource("getByIdWithException_failure")
	public void reject_failure(final SessionValue session, final String statusId, final String createdAt, final Class<? extends Exception> ex, final String message)
	{
		sessionDao.current(session);

		assertThat(Assertions.assertThrows(ex, () -> dao.reject(statusId, createdAt)))
			.hasMessageContaining(message);
	}

	public static Stream<Arguments> search()
	{
		var hourAgo = hourAgo();
		var hourAhead = hourAhead();

		return Stream.of(
			arguments(new FacilitateFilter(), 4L),
			arguments(new FacilitateFilter(1, 1), 1L),
			arguments(new FacilitateFilter(1, 2), 2L),
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
		var results = dao.search(filter);
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

		Assertions.assertThrows(NotAuthorizedException.class, () -> dao.remove(ADD_CITIZEN.statusId, ADD_CITIZEN.createdAt()));
	}

	@Test
	public void testRemove_success()
	{
		Assertions.assertTrue(dao.remove(ADD_CITIZEN.statusId, ADD_CITIZEN.createdAt()));
		count(3L);
		Assertions.assertTrue(dao.remove(ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt()));
		count(2L);
		Assertions.assertTrue(dao.remove(CHANGE_CITIZEN.statusId, CHANGE_CITIZEN.createdAt()));
		count(1L);
		Assertions.assertTrue(dao.remove(CHANGE_PROVIDER.statusId, CHANGE_PROVIDER.createdAt()));
		count(0L);
	}

	@Test
	public void testRemove_success_again()
	{
		Assertions.assertFalse(dao.remove(ADD_CITIZEN.statusId, ADD_CITIZEN.createdAt()));
		Assertions.assertFalse(dao.remove(ADD_PROVIDER.statusId, ADD_PROVIDER.createdAt()));
		Assertions.assertFalse(dao.remove(CHANGE_CITIZEN.statusId, CHANGE_CITIZEN.createdAt()));
		Assertions.assertFalse(dao.remove(CHANGE_PROVIDER.statusId, CHANGE_PROVIDER.createdAt()));
	}

	private void count(final long expected) { count(new FacilitateFilter(), expected); }
	private void count(final FacilitateFilter filter, final long expected)
	{
		Assertions.assertEquals(expected, dao.count(filter), "COUNT: " + filter);
	}

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
