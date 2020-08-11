package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.apache.commons.lang3.StringUtils.repeat;
import static app.allclear.testing.TestingUtils.*;
import static app.allclear.platform.type.TestType.*;

import java.util.Date;
import java.util.stream.Stream;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.*;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.model.TestInitRequest;
import app.allclear.platform.model.TestReceiptRequest;
import app.allclear.platform.type.TestType;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Tests entity.
*
*	@author smalleyd
*	@version 1.0.44
*	@since April 4, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class TestsResourceInitTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static TestsDAO dao = null;
	private static FacilityDAO facilityDao = null;
	private static PatientDAO patientDao = null;
	private static PeopleDAO peopleDao = null;
	private static SessionDAO sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());
	private static SessionValue ADMIN = null;
	private static SessionValue EDITOR = null;
	private static FacilityValue FACILITY = null;
	private static FacilityValue FACILITY_1 = null;
	private static PeopleValue ASSOCIATE = null;
	private static SessionValue ASSOCIATE_ = null;
	private static PeopleValue ASSOCIATE_1 = null;
	private static SessionValue ASSOCIATE_1_ = null;
	private static PeopleValue PATIENT = null;
	private static PeopleValue PATIENT_1 = null;
	private static Long ID = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new AuthorizationExceptionMapper())
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new TestsResource(dao, patientDao, peopleDao)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/tests";

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new TestsDAO(factory, sessionDao);
		facilityDao = new FacilityDAO(factory, new TestAuditor());
		patientDao = new PatientDAO(factory, sessionDao);
		peopleDao = new PeopleDAO(factory);
	}

	@BeforeEach
	public void beforeEach()
	{
		if (null != ADMIN) sessionDao.current(ADMIN);
	}

	@Test
	public void add()
	{
		sessionDao.current(ADMIN = sessionDao.add(new AdminValue("admin"), false));
		EDITOR = sessionDao.add(new AdminValue("editor", false, true), false);
		FACILITY = facilityDao.add(new FacilityValue(0), true);
		FACILITY_1 = facilityDao.add(new FacilityValue(1), true);
		PATIENT = peopleDao.add(new PeopleValue(0));
		PATIENT_1 = peopleDao.add(new PeopleValue(1));
		ASSOCIATE = peopleDao.add(new PeopleValue(10).withAssociations(FACILITY));
		ASSOCIATE_ = new SessionValue(false, ASSOCIATE);
		ASSOCIATE_1 = peopleDao.add(new PeopleValue(11).withAssociations(FACILITY_1));
		ASSOCIATE_1_ = new SessionValue(false, ASSOCIATE_1);

		Assertions.assertNull(patientDao.getByFacilityAndPerson(FACILITY.id, PATIENT.id), "Check patient 0");
		Assertions.assertNull(patientDao.getByFacilityAndPerson(FACILITY.id, PATIENT_1.id), "Check patient 1");
	}

	public static Stream<SessionValue> init_fail()
	{
		return Stream.of(ADMIN, new SessionValue(false, PATIENT), new SessionValue(false, PATIENT_1));
	}

	@ParameterizedTest
	@MethodSource
	public void init_fail(final SessionValue session)
	{
		sessionDao.current(session);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request("init")
			.post(Entity.json(new TestInitRequest(FACILITY.id, PATIENT.id, TestType.ANTIBODY.id, "remote-1"))).getStatus());
	}

	public static Stream<Arguments> init_invalid()
	{
		var typeId = ANTIBODY.id;

		return Stream.of(
			arguments(new TestInitRequest(FACILITY.id, PATIENT.id, null, "remote-1"), "Type is not set."),
			arguments(new TestInitRequest(FACILITY.id, PATIENT.id, "", "remote-1"), "Type is not set."),
			arguments(new TestInitRequest(FACILITY.id, PATIENT.id, "1", "remote-1"), "'1' is not a valid Type."),
			arguments(new TestInitRequest(FACILITY.id, PATIENT.id, typeId, repeat('1', TestsValue.MAX_LEN_REMOTE_ID + 1)), "Remote ID '11111111111111111111111111111111111111111111111111111111111111111' is longer than the expected size of 64."),
			arguments(new TestInitRequest(null, PATIENT.id, typeId, "remote-1"), "Facility is not set."),
			arguments(new TestInitRequest(FACILITY.id, null, typeId, "remote-1"), "Please provide either a Person ID or a the patient phone number."),
			arguments(new TestInitRequest(FACILITY.id, repeat('1', PeopleValue.MAX_LEN_PHONE + 1), null, null, typeId, "remote-1"), "Phone Number '+1111111111111111111111111111111111' is longer than the expected size of 32."),
			arguments(new TestInitRequest(FACILITY.id, "8881115555", repeat('1', PeopleValue.MAX_LEN_FIRST_NAME + 1), null, typeId, "remote-1"), "First Name '111111111111111111111111111111111' is longer than the expected size of 32."),
			arguments(new TestInitRequest(FACILITY.id, "8881115555", null, repeat('1', PeopleValue.MAX_LEN_LAST_NAME + 1), typeId, "remote-1"), "Last Name '111111111111111111111111111111111' is longer than the expected size of 32."));
	}

	@ParameterizedTest
	@MethodSource
	public void init_invalid(final TestInitRequest value, final String message)
	{
		sessionDao.current(ASSOCIATE_);

		var response = request("init").post(Entity.json(value));
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");
		assertThat(response.readEntity(ErrorInfo.class).message).as("Check error message").contains(message);
	}

	@Test
	public void init_succes()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, request(FACILITY.id, "remote-1").getStatus());
	}

	@Test
	public void init_success()
	{
		sessionDao.current(ASSOCIATE_);

		var type = ANTIBODY;
		var response = request("init")
			.post(Entity.json(new TestInitRequest(FACILITY.id, PATIENT.id, type.id, "remote-1")));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(FACILITY.id, value.facilityId, "Check facilityId");
		Assertions.assertEquals(PATIENT.id, value.personId, "Check personId");
		Assertions.assertEquals(type.id, value.typeId, "Check typeId");
		Assertions.assertEquals(type,  value.type, "Check type");
		Assertions.assertEquals("remote-1", value.remoteId, "Check remoteId");
		Assertions.assertFalse(value.positive, "Check positive");
		Assertions.assertNull(value.notes, "Check notes");

		ID = value.id;
	}

	public static Stream<SessionValue> init_success_check()
	{
		return Stream.of(ADMIN, new SessionValue(false, PATIENT), ASSOCIATE_);
	}

	@ParameterizedTest
	@MethodSource
	public void init_success_check(final SessionValue session)
	{
		sessionDao.current(session);

		var response = request(FACILITY.id, "remote-1");
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var type = ANTIBODY;
		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(FACILITY.id, value.facilityId, "Check facilityId");
		Assertions.assertEquals(PATIENT.id, value.personId, "Check personId");
		Assertions.assertEquals(type.id, value.typeId, "Check typeId");
		Assertions.assertEquals(type,  value.type, "Check type");
		Assertions.assertEquals("remote-1", value.remoteId, "Check remoteId");
		Assertions.assertFalse(value.positive, "Check positive");
		Assertions.assertNull(value.notes, "Check notes");
	}

	public static Stream<SessionValue> init_success_fail()
	{
		return Stream.of(EDITOR, new SessionValue(false, PATIENT_1), ASSOCIATE_1_);
	}

	@ParameterizedTest
	@MethodSource
	public void init_success_fail(final SessionValue session)
	{
		sessionDao.current(session);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(FACILITY.id, "remote-1").getStatus());
	}

	@Test
	public void init_success_invalid_dupe()
	{
		sessionDao.current(ASSOCIATE_);

		var response = request("init")
			.post(Entity.json(new TestInitRequest(FACILITY.id, PATIENT.id, ANTIBODY.id, "remote-1")));
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");
		assertThat(response.readEntity(ErrorInfo.class).message).as("Check error message").isEqualTo("The Remote ID 'remote-1' is already in use.");
	}

	public static Stream<SessionValue> receive_fail()
	{
		return Stream.of(ADMIN, ASSOCIATE_1_, new SessionValue(false, PATIENT), new SessionValue(false, PATIENT_1));
	}

	@ParameterizedTest
	@MethodSource
	public void receive_fail(final SessionValue session)
	{
		sessionDao.current(session);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request("receive")
			.post(Entity.json(new TestReceiptRequest(ID, true, "special notes"))).getStatus());
	}

	public static Stream<Arguments> receive_invalid()
	{
		return Stream.of(
			arguments(new TestReceiptRequest(null, true, "special notes"), "ID is not set."),
			arguments(new TestReceiptRequest(ID, true, StringUtils.repeat('a', TestsValue.MAX_LEN_NOTES + 1)), "Notes 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa...' is longer than the expected size of 65535."),
			arguments(new TestReceiptRequest(ID + 1000L, true, "special notes"), "The ID, 1001, is invalid."));
	}

	@ParameterizedTest
	@MethodSource
	public void receive_invalid(final TestReceiptRequest value, final String message)
	{
		sessionDao.current(ASSOCIATE_);

		var response = request("receive").post(Entity.json(value));
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");
		assertThat(response.readEntity(ErrorInfo.class).message).as("Check error message").contains(message);
	}

	@ParameterizedTest
	@MethodSource("init_success_check")
	public void receive_succes(final SessionValue session)	// Make sure that after the failure that nothing has changed.
	{
		init_success_check(session);
	}

	@Test
	public void receive_success_00()
	{
		sessionDao.current(ASSOCIATE_);

		var response = request("receive").post(Entity.json(new TestReceiptRequest(ID, true, "special notes")));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertTrue(value.positive, "Check positive");
		Assertions.assertEquals("special notes", value.notes, "Check notes");
		assertThat(value.updatedAt).as("Check updatedAt").isNotNull().isCloseTo(new Date(), 500L).isAfter(value.createdAt);
	}

	public static Stream<SessionValue> receive_success_00_check()
	{
		return Stream.of(ADMIN, new SessionValue(false, PATIENT), ASSOCIATE_);
	}

	@ParameterizedTest
	@MethodSource
	public void receive_success_00_check(final SessionValue session)
	{
		sessionDao.current(session);

		var response = request(FACILITY.id, "remote-1");
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var type = ANTIBODY;
		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(FACILITY.id, value.facilityId, "Check facilityId");
		Assertions.assertEquals(PATIENT.id, value.personId, "Check personId");
		Assertions.assertEquals(type.id, value.typeId, "Check typeId");
		Assertions.assertEquals(type,  value.type, "Check type");
		Assertions.assertEquals("remote-1", value.remoteId, "Check remoteId");
		Assertions.assertTrue(value.positive, "Check positive");
		Assertions.assertEquals("special notes", value.notes, "Check notes");
		assertThat(value.updatedAt).as("Check updatedAt").isNotNull().isCloseTo(new Date(), 500L).isAfter(value.createdAt);
	}

	@Test
	public void receive_success_01()
	{
		sessionDao.current(ASSOCIATE_);

		var response = request("receive").post(Entity.json(new TestReceiptRequest(ID, false, "False positive")));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertFalse(value.positive, "Check positive");
		Assertions.assertEquals("False positive", value.notes, "Check notes");
		assertThat(value.updatedAt).as("Check updatedAt").isNotNull().isCloseTo(new Date(), 500L).isAfter(value.createdAt);
	}

	public static Stream<SessionValue> receive_success_01_check()
	{
		return Stream.of(ADMIN, new SessionValue(false, PATIENT), ASSOCIATE_);
	}

	@ParameterizedTest
	@MethodSource
	public void receive_success_01_check(final SessionValue session)
	{
		sessionDao.current(session);

		var response = request(FACILITY.id, "remote-1");
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus());

		var type = ANTIBODY;
		var value = response.readEntity(TestsValue.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(FACILITY.id, value.facilityId, "Check facilityId");
		Assertions.assertEquals(PATIENT.id, value.personId, "Check personId");
		Assertions.assertEquals(type.id, value.typeId, "Check typeId");
		Assertions.assertEquals(type,  value.type, "Check type");
		Assertions.assertEquals("remote-1", value.remoteId, "Check remoteId");
		Assertions.assertFalse(value.positive, "Check positive");
		Assertions.assertEquals("False positive", value.notes, "Check notes");
		assertThat(value.updatedAt).as("Check updatedAt").isNotNull().isCloseTo(new Date(), 500L).isAfter(value.createdAt);
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
	private Response request(final Long facilityId, final String remoteId) { return request(facilityId + "/" + remoteId).get(); }
}
