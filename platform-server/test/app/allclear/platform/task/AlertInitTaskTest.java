package app.allclear.platform.task;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import com.azure.storage.queue.QueueClient;

import app.allclear.common.errors.*;
import app.allclear.junit.hibernate.*;
import app.allclear.platform.App;
import app.allclear.platform.dao.PeopleDAO;
import app.allclear.platform.entity.People;
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
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static PeopleDAO dao;
	private static AlertInitTask task;
	private static final QueueClient queue = mock(QueueClient.class);

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
		var factory = DAO_RULE.getSessionFactory();
		dao = new PeopleDAO(factory);
		task = new AlertInitTask(factory, dao, queue);

		when(queue.sendMessage(any(String.class))).thenAnswer(a -> { sent.add(a.getArgument(0, String.class)); return null; });
	}

	@AfterEach
	public void afterEach()
	{
		assertThat(sent).as("Check sent").isEqualTo(processed);
	}

	@ParameterizedTest
	@CsvFileSource(resources="/feeds/people.csv", numLinesToSkip=1)
	public void add(final String id, final BigDecimal latitude, final BigDecimal longitude, final boolean alertable, final boolean active)
	{
		transRule.getSession().persist(new People(id, latitude, longitude, alertable, active));
	}

	public static Stream<Arguments> getActiveAlertableIdsByLongitude()
	{
		return Stream.of(
			arguments(Timezone.CST, "VVVV"),
			arguments(Timezone.MST, "WWWW"),
			arguments(Timezone.PST, "XXXX"));
	}

	@ParameterizedTest
	@MethodSource
	public void getActiveAlertableIdsByLongitude(final Timezone zone, final String expectedId)
	{
		var ids = dao.getActiveAlertableIdsByLongitude("", zone, 20);
		assertThat(ids).as("Check page 1").hasSize(1).containsExactly(expectedId);
	}

	@Test
	public void getActiveAlertableIdsByLongitude_EST()
	{
		var zone = Timezone.EST;
		var ids = dao.getActiveAlertableIdsByLongitude("", zone, 20);
		assertThat(ids).as("Check page 1").hasSize(20).containsExactly("1111", "AAAA", "CCCC", "DDDD", "EEEE", "FFFF", "GGGG", "HHHH", "IIII", "JJJJ", "KKKK", "LLLL", "MMMM", "NNNN", "OOOO", "PPPP", "QQQQ", "RRRR", "SSSS", "TTTT");
		assertThat(dao.getActiveAlertableIdsByLongitude(ids.get(19), zone, 20)).as("Check page 2").hasSize(1).containsExactly("UUUU");
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
