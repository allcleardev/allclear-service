package app.allclear.platform.task;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static app.allclear.junit.hibernate.HibernateTransactionRule.doTrans;
import static app.allclear.testing.TestingUtils.*;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.common.hibernate.DualSessionFactory;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.junit.hibernate.HibernateRule;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.model.AlertRequest;
import app.allclear.platform.value.*;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.*;

/** Functional test class that verifies the AlertTask queue callback.
 * 
 * @author smalleyd
 * @version 1.0.111
 * @since 4/15/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class AlertTaskTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);

	private static final FakeRedisClient redis = new FakeRedisClient();
	private static final TwilioClient twilio = mock(TwilioClient.class);
	private static AlertTask task;
	private static PeopleDAO dao;
	private static final FacilityDAO facilityDao = mock(FacilityDAO.class);	// MUST mock because H2 can't perform the same mySQL spatial query.
	private static final SessionDAO sessionDao = new SessionDAO(redis, twilio, ConfigTest.loadTest());

	private static PeopleValue PERSON;

	private static Date LAST_ALERTED_AT = null;
	private static SMSResponse LAST_SMS_RESPONSE = null;

	private void changeCount(final long value)
	{
		when(facilityDao.countActivatedAtByDistance(any(Date.class), any(BigDecimal.class), any(BigDecimal.class), any(Long.class), any(Integer.class))).thenReturn(value);
	}

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new PeopleDAO(factory);
		task = new AlertTask(new DualSessionFactory(factory), dao, facilityDao, sessionDao);

		when(twilio.send(any(SMSRequest.class))).thenAnswer(a -> LAST_SMS_RESPONSE = new SMSResponse(a.getArgument(0, SMSRequest.class)));
	}

	@Test
	public void add()
	{
		doTrans(DAO_RULE, s -> {
			PERSON = dao.add(new PeopleValue("loki", "888-555-1000", true).withLatitude(new BigDecimal("40")).withLongitude(new BigDecimal("-70")).withAuthAt(hourAgo()));
		});
	}

	@Test
	public void add_get()
	{
		doTrans(DAO_RULE, s -> {
			var v = dao.getById(PERSON.id);
			Assertions.assertNull(v.alertedOf, "Check alertedOf");
			Assertions.assertNull(v.alertedAt, "Check alertedAt");
			assertThat(v.updatedAt).as("Check updatedAt").isNotNull().isCloseTo(new Date(), 1000L);
		});
	}

	@Test
	public void process_00() throws Exception
	{
		changeCount(1L);

		Assertions.assertNull(LAST_SMS_RESPONSE, "Check LAST_SMS_RESPONSE: before");
		Assertions.assertTrue(task.process(new AlertRequest(PERSON.id)));
		Assertions.assertNotNull(LAST_SMS_RESPONSE, "Check LAST_SMS_RESPONSE: after");
	}

	@Test
	public void process_00_auth()
	{
		var token = token();
		Assertions.assertEquals(24 * 3600, redis.ttl(SessionDAO.authKey(PERSON.phone, token)));
		sessionDao.auth(PERSON.phone, token);	// No error
	}

	@Test
	public void process_00_check() throws Exception
	{
		doTrans(DAO_RULE, s -> {
			var v = dao.getById(PERSON.id);
			Assertions.assertEquals(1, v.alertedOf, "Check alertedOf");
			assertThat(LAST_ALERTED_AT = v.alertedAt).as("Check alertedAt").isNotNull().isCloseTo(new Date(), 1000L);
			Assertions.assertEquals(v.alertedAt, v.updatedAt, "Check updatedAt");
		});
	}

	@Test
	public void process_01() throws Exception
	{
		changeCount(0L);
		LAST_SMS_RESPONSE = null;

		Assertions.assertTrue(task.process(new AlertRequest(PERSON.id)));
		Assertions.assertNull(LAST_SMS_RESPONSE, "Check LAST_SMS_RESPONSE: after");	// No new facilities since last run.
	}

	@Test
	public void process_01_check() throws Exception
	{
		doTrans(DAO_RULE, s -> {
			var v = dao.getById(PERSON.id);
			Assertions.assertEquals(0, v.alertedOf, "Check alertedOf");
			assertThat(v.alertedAt).as("Check alertedAt").isNotNull().isCloseTo(new Date(), 1000L).isAfter(LAST_ALERTED_AT);	// Always changes
			Assertions.assertEquals(v.alertedAt, v.updatedAt, "Check updatedAt");

			LAST_ALERTED_AT = v.alertedAt;
		});
	}

	@Test
	public void process_02() throws Exception
	{
		changeCount(5L);

		Assertions.assertNull(LAST_SMS_RESPONSE, "Check LAST_SMS_RESPONSE: before");
		Assertions.assertTrue(task.process(new AlertRequest(PERSON.id)));
		Assertions.assertNotNull(LAST_SMS_RESPONSE, "Check LAST_SMS_RESPONSE: after");
	}

	@Test
	public void process_02_auth()
	{
		var token = token();
		Assertions.assertEquals(24 * 3600, redis.ttl(SessionDAO.authKey(PERSON.phone, token)));
		sessionDao.auth(PERSON.phone, token);	// No error
	}

	@Test
	public void process_02_check() throws Exception
	{
		doTrans(DAO_RULE, s -> {
			var v = dao.getById(PERSON.id);
			Assertions.assertEquals(5, v.alertedOf, "Check alertedOf");
			assertThat(v.alertedAt).as("Check alertedAt").isNotNull().isCloseTo(new Date(), 1000L).isAfter(LAST_ALERTED_AT);
			Assertions.assertEquals(v.alertedAt, v.updatedAt, "Check updatedAt");

			LAST_ALERTED_AT = v.alertedAt;
		});
	}

	private String token()
	{
		var body = LAST_SMS_RESPONSE.body;
		var i = body.indexOf("token=") + 6;

		return body.substring(i);
	}
}
