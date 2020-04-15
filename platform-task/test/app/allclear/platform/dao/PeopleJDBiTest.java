package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.jdbi.JDBiRule;
import app.allclear.platform.type.Timezone;

/** Functional test that verifies the PeopleDAO class.
 * 
 * @author smalleyd
 * @version 1.0.104
 * @since 4/11/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleJDBiTest
{
	public static final JDBiRule RULE = new JDBiRule();

	private static PeopleJDBi dao;

	@BeforeAll
	public static void up()
	{
		dao = RULE.dbi().onDemand(PeopleJDBi.class);
	}

	public static Stream<Arguments> getActiveAlertableIds()
	{
		return Stream.of(
			arguments(Timezone.CST, "VVVV"),
			arguments(Timezone.MST, "WWWW"),
			arguments(Timezone.PST, "XXXX"));
	}

	@ParameterizedTest
	@MethodSource
	public void getActiveAlertableIds(final Timezone zone, final String expectedId)
	{
		var ids = dao.getActiveAlertableIds("", zone, 20);
		assertThat(ids).as("Check page 1").hasSize(1).containsExactly(expectedId);
	}

	@Test
	public void getActiveAlertableIds_EST()
	{
		var zone = Timezone.EST;
		var ids = dao.getActiveAlertableIds("", zone, 20);
		assertThat(ids).as("Check page 1").hasSize(20).containsExactly("1111", "AAAA", "CCCC", "DDDD", "EEEE", "FFFF", "GGGG", "HHHH", "IIII", "JJJJ", "KKKK", "LLLL", "MMMM", "NNNN", "OOOO", "PPPP", "QQQQ", "RRRR", "SSSS", "TTTT");
		assertThat(dao.getActiveAlertableIds(ids.get(19), zone, 20)).as("Check page 2").hasSize(1).containsExactly("UUUU");
	}
}
