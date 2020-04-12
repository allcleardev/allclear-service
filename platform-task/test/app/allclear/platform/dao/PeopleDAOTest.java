package app.allclear.platform.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.jdbi.JDBiRule;

/** Functional test that verifies the PeopleDAO class.
 * 
 * @author smalleyd
 * @version 1.0.104
 * @since 4/11/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleDAOTest
{
	public static final JDBiRule RULE = new JDBiRule();

	private static PeopleDAO dao;

	@BeforeAll
	public static void up()
	{
		dao = RULE.dbi().onDemand(PeopleDAO.class);
	}

	@Test
	public void getActiveAlertableIds()
	{
		var ids = dao.getActiveAlertableIds("", 20);
		assertThat(ids).as("Check page 1").hasSize(20).containsExactly("1111", "AAAA", "CCCC", "DDDD", "EEEE", "FFFF", "GGGG", "HHHH", "IIII", "JJJJ", "KKKK", "LLLL", "MMMM", "NNNN", "OOOO", "PPPP", "QQQQ", "RRRR", "SSSS", "TTTT");
		assertThat(dao.getActiveAlertableIds(ids.get(19), 20)).as("Check page 2").hasSize(1).containsExactly("UUUU");
	}
}
