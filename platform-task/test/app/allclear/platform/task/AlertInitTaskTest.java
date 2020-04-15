package app.allclear.platform.task;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.errors.*;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.junit.jdbi.JDBiRule;
import app.allclear.platform.dao.PeopleJDBi;
import app.allclear.platform.model.AlertInitRequest;
import app.allclear.platform.type.Timezone;

/** Functional test class that verifies the Alert-Init background task.
 * 
 * @author smalleyd
 * @version 1.0.109
 * @since 4/14/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class AlertInitTaskTest
{
	public static final JDBiRule RULE = new JDBiRule();

	private static PeopleJDBi dao;
	private static AlertInitTask task;
	private static final QueueClient queue = mock(QueueClient.class);
	private static final ObjectMapper mapper = JacksonUtils.createMapper();

	private static List<String> sent = new LinkedList<>();
	private static List<String> processed = new LinkedList<>();

	private void processed(final String... personIds)
	{
		for (var personId : personIds)
			processed.add(String.format("{\"personId\":\"%s\"}", personId));
	}

	@BeforeAll
	public static void up()
	{
		dao = RULE.dbi().onDemand(PeopleJDBi.class);
		task = new AlertInitTask(mapper, dao, queue);

		when(queue.sendMessage(any(String.class))).thenAnswer(a -> { sent.add(a.getArgument(0, String.class)); return null; });
	}

	@AfterEach
	public void afterEach()
	{
		assertThat(sent).as("Check sent").isEqualTo(processed);
	}

	@Test
	public void process() throws Exception
	{
		Assertions.assertTrue(task.process(new AlertInitRequest(Timezone.EST.id)));
		processed("1111", "AAAA", "CCCC", "DDDD", "EEEE", "FFFF", "GGGG", "HHHH", "IIII", "JJJJ", "KKKK", "LLLL", "MMMM", "NNNN", "OOOO", "PPPP", "QQQQ", "RRRR", "SSSS", "TTTT", "UUUU");
	}

	public static Stream<Arguments> process_error()
	{
		return Stream.of(
			arguments("", AbortException.class, "The request is missing the timezoneId."),
			arguments(null, AbortException.class, "The request is missing the timezoneId."),
			arguments("INVALID", AbortException.class, "The timezone 'INVALID' does not receive alerts."));
	}

	@ParameterizedTest
	@MethodSource
	public void process_error(final String timezoneId, final Class<? extends Exception> expected, final String message)
	{
		assertThat(Assertions.assertThrows(expected, () -> task.process(new AlertInitRequest(timezoneId)))).hasMessage(message);
	}

	public static Stream<Arguments> process_other()
	{
		return Stream.of(
			arguments(Timezone.CST, "VVVV"),
			arguments(Timezone.MST, "WWWW"),
			arguments(Timezone.PST, "XXXX"));
	}

	@ParameterizedTest
	@MethodSource
	public void process_other(final Timezone zone, final String personId) throws Exception
	{
		Assertions.assertTrue(task.process(new AlertInitRequest(zone.id)));
		processed(personId);
	}
}
