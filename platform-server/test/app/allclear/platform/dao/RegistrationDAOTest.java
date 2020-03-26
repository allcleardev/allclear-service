package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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

	private static Map<String, String> codes = new HashMap<>();

	@BeforeAll
	public static void up()
	{
		redis = new FakeRedisClient();
		dao = new RegistrationDAO(redis);
	}

	public static Stream<Arguments> add()
	{
		return Stream.of(
			arguments("888-555-1000", null, null, false, false),
			arguments("888-555-1001", true, null, true, false),
			arguments("888-555-1002", null, true, false, true),
			arguments("888-555-1003", false, null, false, false),
			arguments("888-555-1004", null, false, false, false),
			arguments("888-555-1005", true, true, true, true),
			arguments("888-555-1006", true, false, true, false),
			arguments("888-555-1007", false, true, false, true),
			arguments("888-555-1008", false, false, false, false));
	}

	@ParameterizedTest
	@MethodSource
	public void add(final String phone, final Boolean beenTested, final Boolean haveSymptoms, final boolean expectedBeenTested, final boolean expectedHaveSymptoms)
	{
		var code = dao.start(new StartRequest(phone, beenTested, haveSymptoms));
		assertThat(code).hasSize(10).matches(PATTERN_CODE);

		var o = dao.confirm(phone, code);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertEquals(phone, o.phone, "Check phone");
		Assertions.assertEquals(expectedBeenTested, o.beenTested, "Check beenTested");
		Assertions.assertEquals(expectedHaveSymptoms, o.haveSymptoms, "Check haveSymptoms");

		Assertions.assertNull(dao.request(phone, code), "Check request: after confirm");
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.confirm(phone, code)))
			.as("Check confirm: after confirm")
			.hasMessage("The supplied code is invalid.");
	}

	@ParameterizedTest
	@MethodSource("add")
	public void add_again(final String phone, final Boolean beenTested, final Boolean haveSymptoms, final boolean expectedBeenTested, final boolean expectedHaveSymptoms)
	{
		codes.put(phone, dao.start(new StartRequest(phone, beenTested, haveSymptoms)));
	}

	@Test
	public void add_again_check()
	{
		var code = codes.get("888-555-1008");
		assertThat(code).as("Check code").hasSize(10);

		Assertions.assertNotNull(dao.request("888-555-1008", code), "Check correct phone number");
		Assertions.assertNull(dao.request("888-555-1000", code), "Check mismatched phone number");

		Assertions.assertNotNull(dao.confirm("888-555-1008", code), "Check correct phone number");
		assertThat(Assertions.assertThrows(ValidationException.class, () -> dao.confirm("888-555-1000", code)))
			.hasMessage("The supplied code is invalid.");
	}
}
