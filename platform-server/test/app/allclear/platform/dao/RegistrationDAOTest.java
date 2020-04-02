package app.allclear.platform.dao;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.model.StartRequest;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.*;

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
	public static final String MESSAGE = "Click https://app-test.allclear.app/register?phone=%s&code=%s to complete your registration.";

	private static RegistrationDAO dao;
	private static FakeRedisClient redis;
	private static TwilioClient twilio = mock(TwilioClient.class);
	private static SMSResponse LAST_RESPONSE;

	private static Map<String, String> codes = new HashMap<>();

	@BeforeAll
	public static void up()
	{
		redis = new FakeRedisClient();
		dao = new RegistrationDAO(redis, twilio, ConfigTest.loadTest());
		when(twilio.send(any(SMSRequest.class))).thenAnswer(a -> LAST_RESPONSE = new SMSResponse((SMSRequest) a.getArgument(0)));
	}

	public static Stream<Arguments> add()
	{
		return Stream.of(
			arguments("888-555-1000", null, null, "+18885551000", false, false),
			arguments("888-555-1001", true, null, "+18885551001", true, false),
			arguments("888-555-1002", null, true, "+18885551002", false, true),
			arguments("888-555-1003", false, null, "+18885551003", false, false),
			arguments("888-555-1004", null, false, "+18885551004", false, false),
			arguments("888-555-1005", true, true, "+18885551005", true, true),
			arguments("888-555-1006", true, false, "+18885551006", true, false),
			arguments("888-555-1007", false, true, "+18885551007", false, true),
			arguments("888-555-1008", false, false, "+18885551008", false, false));
	}

	@ParameterizedTest
	@MethodSource
	public void add(final String phone, final Boolean beenTested, final Boolean haveSymptoms,
		final String expectedPhone, final boolean expectedBeenTested, final boolean expectedHaveSymptoms)
	{
		var code = dao.start(new StartRequest(phone, beenTested, haveSymptoms));
		assertThat(code).hasSize(10).matches(PATTERN_CODE);
		Assertions.assertNotNull(LAST_RESPONSE, "Check lastResponse");
		Assertions.assertEquals(String.format(MESSAGE, encode(expectedPhone, UTF_8), code), LAST_RESPONSE.body, "Check lastResponse.body");

		var o = dao.confirm(expectedPhone, code);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(expectedPhone, o.phone, "Check phone");
		Assertions.assertEquals(expectedBeenTested, o.beenTested, "Check beenTested");
		Assertions.assertEquals(expectedHaveSymptoms, o.haveSymptoms, "Check haveSymptoms");

		Assertions.assertNull(dao.request(expectedPhone, code), "Check request: after confirm");
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.confirm(expectedPhone, code)))
			.as("Check confirm: after confirm")
			.hasMessage("The supplied code is invalid.");
	}

	@ParameterizedTest
	@MethodSource("add")
	public void add_again(final String phone, final Boolean beenTested, final Boolean haveSymptoms,
		final String expectedPhone, final boolean expectedBeenTested, final boolean expectedHaveSymptoms)
	{
		codes.put(expectedPhone, dao.start(new StartRequest(phone, beenTested, haveSymptoms)));
	}

	@Test
	public void add_again_check()
	{
		var code = codes.get("+18885551008");
		assertThat(code).as("Check code").hasSize(10);

		Assertions.assertNotNull(dao.request("+18885551008", code), "Check correct phone number");
		Assertions.assertNull(dao.request("+18885551000", code), "Check mismatched phone number");

		Assertions.assertNotNull(dao.confirm("+18885551008", code), "Check correct phone number");
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.confirm("+18885551000", code)))
			.hasMessage("The supplied code is invalid.");
	}
}
