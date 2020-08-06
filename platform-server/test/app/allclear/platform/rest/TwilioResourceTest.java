package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.common.errors.NotAuthorizedException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.junit.hibernate.*;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.value.*;

/** Functional test class that verifies the TwilioResource RESTful component.
 * 
 * @author smalleyd
 * @version 1.1.125
 * @since 8/6/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class TwilioResourceTest
{
	private static TwilioResource rest;
	private static FacilityDAO facilityDao;
	private static PatientDAO patientDao;
	private static PeopleDAO peopleDao;

	private static FacilityValue FACILITY;
	private static PeopleValue PERSON;
	private static PeopleValue PERSON_1;

	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);
	public final ResourceExtension RULE = ResourceExtension.builder().addResource(rest).build();

	private static final String TARGET = "/twilio";

	@BeforeAll
	public static void beforeAll()
	{
		var conf = ConfigTest.loadTest();
		var factory = DAO_RULE.getSessionFactory();
		var sessionDao = new SessionDAO(new FakeRedisClient(), conf);

		peopleDao = new PeopleDAO(factory);
		patientDao = new PatientDAO(factory, sessionDao);
		facilityDao = new FacilityDAO(factory, new TestAuditor());
		rest = new TwilioResource("key", patientDao, peopleDao, conf) {
			@Override
			protected void checkSignature(final UriInfo uri,
				final String signature,
				final MultivaluedMap<String, String> params) throws NotAuthorizedException
			{
				return;	// Always return successfully to test downstream functionality.
			}
		};

		sessionDao.current(new SessionValue(false, new AdminValue("admin")));
	}

	@ParameterizedTest
	@CsvSource({",",
	            "'',",
	            "'   \t   ',",
	            "y,true",
	            "yes,true",
	            "Y,true",
	            "YES,true",
	            "Yes,true",
	            "yEs,true",
	            "yeS,true",
	            " yeS,true",
	            "yeS ,true",
	            "  yeS  \t \t,true",
	            "n,false",
	            "no,false",
	            "N,false",
	            "NO,false",
	            "No,false",
	            "nO,false",
	            " nO,false",
	            "nO ,false",
	            " \t \t nO  \t \t ,false"})
	public void isYesOrNo(final String value, final Boolean expected)
	{
		Assertions.assertEquals(expected, rest.isYesOrNo(value));
	}

	@ParameterizedTest
	@CsvSource({"abc,'<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response><Message><Body>abc</Body></Message></Response>'",
	            "You have been successfully unsubscribed from further alerts. Have a nice day.,'<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response><Message><Body>You have been successfully unsubscribed from further alerts. Have a nice day.</Body></Message></Response>'",
	            "You have been successfully subscribed to our new facility alerts. Welcome aboard!,'<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response><Message><Body>You have been successfully subscribed to our new facility alerts. Welcome aboard!</Body></Message></Response>'"})
	public void response(final String value, final String expected)
	{
		Assertions.assertEquals(expected, rest.response(value));
	}

	@Test
	public void add()
	{
		PERSON = peopleDao.add(new PeopleValue("person", "888-555-1000", false));
		PERSON_1 = peopleDao.add(new PeopleValue("person1", "888-555-1001", false));
		FACILITY = facilityDao.add(new FacilityValue(0).withName("Zero"), true);

		patientDao.add(new PatientValue(FACILITY.id, PERSON.id, false, null, null));
	}

	@ParameterizedTest
	@CsvSource({"888-555-1000,invalid,Please respond with either 'Yes' or 'No'.",
	            "invalid,Yes,Please sign up at",
	            "888-555-1001,Yes,You do not have any outstanding test notification requests.",
	            "888-555-1000,Yes,You have been successfully enrolled to receive test notifications from Zero.",
	            "888-555-1000,Yes,You do not have any outstanding test notification requests.",
	            "888-555-1000,No,The test notification request from Zero has been rejected. Have a nice day.",
	            "888-555-1000,No,You do not have any outstanding test notification requests.",
	            "888-555-1000,Yes,You have been successfully enrolled to receive test notifications from Zero."})
	public void handlePatient(final String phone, final String body, final String expected)
	{
		assertThat(patient(phone, body))
			.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response><Message><Body>")
			.contains(expected)
			.endsWith("</Body></Message></Response>");
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }
	// private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_XML_TYPE); }
	private String patient(final String phone, final String body)
	{
		return request("patient").post(Entity.form(new Form("From", phone).param("Body", body)), String.class);
	}
}
