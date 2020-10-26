package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.*;
import app.allclear.platform.App;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the Experiences entity.
*
*	@author smalleyd
*	@version 1.1.139
*	@since 10/26/2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class ExperiencesDAODupeTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static ExperiencesDAO dao = null;
	private static FacilityDAO facilityDao = null;
	private static PeopleDAO peopleDao = null;
	private static final SessionDAO sessionDao = new FakeSessionDAO();
	private static List<FacilityValue> FACILITIES = null;
	private static SessionValue SESSION = null;
	private static SessionValue SESSION_1 = null;
	private static final SessionValue ADMIN = new SessionValue(false, new AdminValue("admin"));

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new ExperiencesDAO(factory, sessionDao);
		facilityDao = new FacilityDAO(factory, new TestAuditor());
		peopleDao = new PeopleDAO(factory);
	}

	@BeforeEach
	public void beforeEach()
	{
		sessionDao.current(ADMIN);
	}

	@Test
	public void add()
	{
		FACILITIES = IntStream.range(0, 10).mapToObj(i -> facilityDao.add(new FacilityValue(i), true)).collect(toList());
		SESSION = new SessionValue(false, peopleDao.add(new PeopleValue("zero", "888-555-1000", true)));
		SESSION_1 = new SessionValue(false, peopleDao.add(new PeopleValue("one", "888-555-1001", true)));
	}

	public static Stream<Arguments> add_success()
	{
		return Stream.of(
			arguments(SESSION, 0, false, 0L, 0L),
			arguments(SESSION, 2, true, 1L, 0L),
			arguments(SESSION, 4, false, 2L, 0L),
			arguments(SESSION, 6, false, 3L, 0L),
			arguments(SESSION, 8, true, 4L, 0L),
			arguments(SESSION_1, 9, false, 0L, 0L),
			arguments(SESSION_1, 7, true, 1L, 0L),
			arguments(SESSION_1, 5, false, 2L, 0L),
			arguments(SESSION_1, 3, true, 3L, 0L));
	}

	@ParameterizedTest
	@MethodSource
	public void add_success(final SessionValue session, final int facilityIndex, final boolean positive, final long first, final long second)
	{
		var facilityId = FACILITIES.get(facilityIndex).id;
		assertThat(dao.countTodayExperiencesByPerson(session.person.id, facilityId)).as("Check before").containsSequence(first, second);

		sessionDao.current(session);
		dao.add(new ExperiencesValue(facilityId, positive));

		assertThat(dao.countTodayExperiencesByPerson(session.person.id, facilityId)).as("Check after").containsSequence(first + 1L, second + 1L);
	}

	public static Stream<Arguments> dupe()
	{
		return Stream.of(
			arguments(SESSION, 1, 5L, 0L, "You have already provided five Experiences today."),
			arguments(SESSION, 3, 5L, 0L, "You have already provided five Experiences today."),
			arguments(SESSION, 5, 5L, 0L, "You have already provided five Experiences today."),
			arguments(SESSION, 7, 5L, 0L, "You have already provided five Experiences today."),
			arguments(SESSION, 9, 5L, 0L, "You have already provided five Experiences today."),
			arguments(SESSION, 0, 5L, 1L, "You have already provided five Experiences today.\nYou have already provided an Experience for Test Center 0 today."),
			arguments(SESSION, 2, 5L, 1L, "You have already provided five Experiences today.\nYou have already provided an Experience for Test Center 2 today."),
			arguments(SESSION, 4, 5L, 1L, "You have already provided five Experiences today.\nYou have already provided an Experience for Test Center 4 today."),
			arguments(SESSION, 6, 5L, 1L, "You have already provided five Experiences today.\nYou have already provided an Experience for Test Center 6 today."),
			arguments(SESSION, 8, 5L, 1L, "You have already provided five Experiences today.\nYou have already provided an Experience for Test Center 8 today."),
			arguments(SESSION_1, 9, 4L, 1L, "You have already provided an Experience for Test Center 9 today."),
			arguments(SESSION_1, 7, 4L, 1L, "You have already provided an Experience for Test Center 7 today."),
			arguments(SESSION_1, 5, 4L, 1L, "You have already provided an Experience for Test Center 5 today."),
			arguments(SESSION_1, 3, 4L, 1L, "You have already provided an Experience for Test Center 3 today."));
	}

	@ParameterizedTest
	@MethodSource
	public void dupe(final SessionValue session, final int facilityIndex, final long first, final long second, final String message)
	{
		var facilityId = FACILITIES.get(facilityIndex).id;
		sessionDao.current(session);
		assertThat(assertThrows(ValidationException.class, () -> dao.add(new ExperiencesValue(facilityId, true))))
			.hasMessage(message);

		assertThat(dao.countTodayExperiencesByPerson(session.person.id, facilityId)).as("Check before").containsSequence(first, second);
	}
}
