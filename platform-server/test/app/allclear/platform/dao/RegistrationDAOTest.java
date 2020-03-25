package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.model.StartRequest;

/** Functional test class that verifies RegistrationDAO component.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class RegistrationDAOTest
{
	public static final Pattern PATTERN_CODE = Pattern.compile("[A-Z0-9]{10}");

	private static RegistrationDAO dao;
	private static FakeRedisClient redis;

	private static String code;
	private static StartRequest request;

	@BeforeAll
	public static void up()
	{
		redis = new FakeRedisClient();
		dao = new RegistrationDAO(redis);
	}

	@Test
	public void add()
	{
		assertThat(code = dao.start(new StartRequest("888-555-1000", null, null)))
			.hasSize(10)
			.matches(PATTERN_CODE);
	}

	@Test
	public void check()
	{
		var o = dao.request("888-555-1000", code);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertFalse(o.beenTested, "Check beenTested");
		Assertions.assertFalse(o.haveSymptoms, "Check haveSymptoms");
	}
}
