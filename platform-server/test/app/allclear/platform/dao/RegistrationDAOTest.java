package app.allclear.platform.dao;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.filter.RegistrationFilter;
import app.allclear.platform.model.StartRequest;
import app.allclear.platform.model.StartResponse;
import app.allclear.platform.value.PeopleValue;
import app.allclear.platform.value.RegistrationValue;
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
	public static final Pattern PATTERN_CODE = Pattern.compile("[A-Z0-9]{6}");
	public static final String MESSAGE = "Your AllClear passcode to register is %s or click this magic link https://app-test.allclear.app/register?phone=%s&code=%s";

	private static RegistrationDAO dao;
	private static FakeRedisClient redis;
	private static TwilioClient twilio = mock(TwilioClient.class);
	private static SMSResponse LAST_RESPONSE;

	private static Map<String, String> codes = new HashMap<>();
	private static Map<String, RegistrationValue> VALUES = new HashMap<>();

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
		assertThat(code).as("Check code").isNotNull().hasSize(6).matches(PATTERN_CODE);
		Assertions.assertNotNull(LAST_RESPONSE, "Check lastResponse");
		Assertions.assertEquals(String.format(MESSAGE, code, encode(expectedPhone, UTF_8), code), LAST_RESPONSE.body, "Check lastResponse.body");
		Assertions.assertEquals(1L, dao.search(new RegistrationFilter().withPhone(expectedPhone)).total, "Check search: before");	// CanNOT search with v1 format.

		var o = dao.confirm(expectedPhone, code);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(expectedPhone, o.phone, "Check phone");
		Assertions.assertEquals(expectedBeenTested, o.beenTested, "Check beenTested");
		Assertions.assertEquals(expectedHaveSymptoms, o.haveSymptoms, "Check haveSymptoms");

		Assertions.assertNull(dao.request(expectedPhone, code), "Check request: after confirm");
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.confirm(expectedPhone, code)))
			.as("Check confirm: after confirm")
			.hasMessage("The supplied code is invalid.");
		Assertions.assertEquals(0L, dao.search(new RegistrationFilter().withPhone(expectedPhone)).total, "Check search: before");
	}

	public static Stream<Arguments> add_again()
	{
		return Stream.of(
			arguments(new PeopleValue("first", "888-555-1000", true), "+18885551000"),
			arguments(new PeopleValue("second", "888-555-1001", true), "+18885551001"),
			arguments(new PeopleValue("third", "888-555-1002", true), "+18885551002"),
			arguments(new PeopleValue("fourth", "888-555-1003", false), "+18885551003"),
			arguments(new PeopleValue("fifth", "888-555-1004", false), "+18885551004"),
			arguments(new PeopleValue("sixth", "888-555-1005", true), "+18885551005"),
			arguments(new PeopleValue("seventh", "888-555-1006", true), "+18885551006"),
			arguments(new PeopleValue("eighth", "888-555-1007", false), "+18885551007"),
			arguments(new PeopleValue("ninth", "888-555-1008", false), "+18885551008"));
	}

	@ParameterizedTest
	@MethodSource
	public void add_again(final PeopleValue person, final String expectedPhone)
	{
		var code = dao.start(person.normalize());

		codes.put(expectedPhone, code);
		VALUES.put(expectedPhone, new RegistrationValue(dao.key(expectedPhone, code), person, (long) RegistrationDAO.EXPIRATION));
	}

	@Test
	public void add_again_check()
	{
		Assertions.assertEquals(9L, dao.search(new RegistrationFilter(1, 100)).total, "Check total: All");
		Assertions.assertEquals(1L, dao.search(new RegistrationFilter(1, 100).withPhone("+18885551000")).total, "Check total: +18885551000");
		Assertions.assertEquals(1L, dao.search(new RegistrationFilter(1, 100).withPhone("+18885551008")).total, "Check total: +18885551008");

		var code = codes.get("+18885551008");
		assertThat(code).as("Check code").isNotNull().hasSize(6).matches(PATTERN_CODE);

		Assertions.assertNotNull(dao.requestX("+18885551008", code), "Check correct phone number");
		Assertions.assertNull(dao.requestX("+18885551000", code), "Check mismatched phone number");

		var value = dao.confirm(new StartResponse("+18885551008", null, code));
		Assertions.assertNotNull(value, "Check correct phone number");
		Assertions.assertEquals("ninth", value.name, "Check value.name");
		Assertions.assertEquals("+18885551008", value.phone, "Check value.phone");

		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.confirm("+18885551000", code)))
			.hasMessage("The supplied code is invalid.");
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.confirm("+18885551008", code)))	// Removed after confirm.
			.hasMessage("The supplied code is invalid.");

		Assertions.assertEquals(1L, dao.search(new RegistrationFilter(1, 100).withPhone("+18885551000")).total, "Check total: +18885551000");
		Assertions.assertEquals(0L, dao.search(new RegistrationFilter(1, 100).withPhone("+18885551008")).total, "Check total: +18885551008");
		Assertions.assertEquals(8L, dao.search(new RegistrationFilter(1, 100)).total, "Check total: All");
	}

	@Test
	public void search()
	{
		assertThat(dao.search(new RegistrationFilter(1, 100)).records)
			.containsAll(VALUES.values().stream().filter(v -> !v.phone.equals("+18885551008")).collect(Collectors.toList()));
	}

	public static Stream<Arguments> start_invalid()
	{
		return Stream.of(
			arguments(null, "Phone is not set."),
			arguments(StringUtils.repeat('5', 6), "Phone cannot be shorter than 10 characters."),
			arguments(StringUtils.repeat('a', 12), "Phone is not set."),
			arguments(StringUtils.repeat('5', 33), "Phone cannot be longer than 32 characters."));
	}

	@ParameterizedTest
	@MethodSource
	public void start_invalid(final String phone, final String message)
	{
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.start(new StartRequest(phone, false, false))))
			.hasMessage(message);
	}

	public static Stream<Arguments> startX_invalid()
	{
		return Stream.of(
			arguments(new PeopleValue(), "Phone is not set."),
			arguments(new PeopleValue().withPhone(StringUtils.repeat('5', 6)), "Phone cannot be shorter than 10 characters."),
			arguments(new PeopleValue().withPhone(StringUtils.repeat('a', 12)), "Phone is not set."),
			arguments(new PeopleValue().withPhone(StringUtils.repeat('5', 33)), "Phone cannot be longer than 32 characters."));
	}

	@ParameterizedTest
	@MethodSource
	public void startX_invalid(final PeopleValue person, final String message)
	{
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.start(person.normalize())))
			.hasMessage(message);
	}

	@Test
	public void testRemove()
	{
		var phone = "+18885551002";
		Assertions.assertNotNull(dao.requestX(phone, codes.get(phone)));
		Assertions.assertNotNull(dao.requestX("+18885551003", codes.get("+18885551003")));

		dao.remove(dao.key(phone, codes.get(phone)));
	}

	@Test
	public void testRemove_check()
	{
		Assertions.assertNull(dao.requestX("+18885551002", codes.get("+18885551002")));
		Assertions.assertNotNull(dao.requestX("+18885551003", codes.get("+18885551003")));
		Assertions.assertEquals(7L, dao.search(new RegistrationFilter(1, 100)).total, "Check total: All");
	}
}
