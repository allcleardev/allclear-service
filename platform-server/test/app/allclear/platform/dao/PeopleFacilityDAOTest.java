package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.platform.App;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.value.*;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the PeopleFacility entity.
*
*	@author smalleyd
*	@version 1.0.108
*	@since April 13, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleFacilityDAOTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static PeopleDAO dao = null;
	private static FacilityDAO facilityDao = null;
	private static PeopleValue VALUE = null;
	private static List<FacilityValue> FACILITIES = null;

	private static List<FacilityValue> facilities(final int... indices)
	{
		return Arrays.stream(indices).mapToObj(i -> FACILITIES.get(i)).collect(toList());
	}

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		dao = new PeopleDAO(factory);
		facilityDao = new FacilityDAO(factory);
	}

	@Test
	public void add()
	{
		var value = dao.add(VALUE = new PeopleValue("min", "888-555-1000", true));
		Assertions.assertNotNull(value, "Exists");

		FACILITIES = IntStream.range(0, 25).mapToObj(i -> facilityDao.add(new FacilityValue(i), true)).collect(toList());
		assertThat(FACILITIES).as("Check facilities").hasSize(25);
	}

	public static Stream<Arguments> add_errors()
	{
		return Stream.of(
			arguments(VALUE.id, null, ValidationException.class, "Please provide one or more facilities to bookmark."),
			arguments(VALUE.id, List.of(), ValidationException.class, "Please provide one or more facilities to bookmark."),
			arguments(VALUE.id, List.of(5L, 10L, 50L, 20L, 25L), ValidationException.class, "The Facility ID '50' is invalid."),
			arguments(VALUE.id, List.of(5L, 10L, 50L, 20L, 25L, 70L), ValidationException.class, "The Facility ID '50' is invalid.\nThe Facility ID '70' is invalid."),
			arguments(VALUE.id, FACILITIES.stream().map(v -> v.id).collect(toList()), ValidationException.class, "You may only have a total of 20 bookmarked facilities."),
			arguments("invalid", List.of(5L, 10L, 20L, 25L), ObjectNotFoundException.class, "Could not find the People because id 'invalid' is invalid."));
	}

	@ParameterizedTest
	@MethodSource
	public void add_errors(final String personId, final List<Long> facilityIds, final Class<? extends Exception> clazz, final String message)
	{
		assertThat(assertThrows(clazz, () -> dao.addFacilities(personId, facilityIds))).hasMessage(message);
	}

	public static Stream<Arguments> count()
	{
		return Stream.of(
			arguments(0L, new PeopleFilter().withHasFacilities(true), "Check hasFacilities: true"),
			arguments(0L, new PeopleFilter().withIncludeFacilities(3L, 9L, 15L, 21L), "Check inclusions: 1"),
			arguments(0L, new PeopleFilter().withIncludeFacilities(6L, 12L, 18L, 24L), "Check inclusions: 2"),
			arguments(0L, new PeopleFilter().withIncludeFacilities(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L), "Check inclusions: all"),
			arguments(1L, new PeopleFilter().withExcludeFacilities(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L), "Check exclusions: all"),
			arguments(1L, new PeopleFilter().withExcludeFacilities(6L, 12L, 18L, 24L), "Check exclusions: 2"),
			arguments(1L, new PeopleFilter().withExcludeFacilities(3L, 9L, 15L, 21L), "Check exclusions: 1"),
			arguments(1L, new PeopleFilter().withHasFacilities(false), "Check hasFacilities: false"));
	}

	@ParameterizedTest
	@MethodSource
	public void count(final long total, final PeopleFilter filter, final String message)
	{
		Assertions.assertEquals(total, dao.count(filter), message);
	}

	@Test
	public void get()
	{
		Assertions.assertNull(dao.getById(VALUE.id).facilities);
	}

	@Test
	public void modify_00()
	{
		Assertions.assertEquals(4, dao.addFacilities(VALUE.id, List.of(3L, 9L, 15L, 21L)));
	}

	public static Stream<Arguments> modify_00_count()
	{
		return Stream.of(
			arguments(1L, new PeopleFilter().withHasFacilities(true), "Check hasFacilities: true"),
			arguments(1L, new PeopleFilter().withIncludeFacilities(3L, 9L, 15L, 21L), "Check inclusions: 1"),
			arguments(0L, new PeopleFilter().withIncludeFacilities(6L, 12L, 18L, 24L), "Check inclusions: 2"),
			arguments(1L, new PeopleFilter().withIncludeFacilities(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L), "Check inclusions: all"),
			arguments(0L, new PeopleFilter().withExcludeFacilities(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L), "Check exclusions: all"),
			arguments(1L, new PeopleFilter().withExcludeFacilities(6L, 12L, 18L, 24L), "Check exclusions: 2"),
			arguments(0L, new PeopleFilter().withExcludeFacilities(3L, 9L, 15L, 21L), "Check exclusions: 1"),
			arguments(0L, new PeopleFilter().withHasFacilities(false), "Check hasFacilities: false"));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_00_count(final long total, final PeopleFilter filter, final String message)
	{
		Assertions.assertEquals(total, dao.count(filter), message);
	}

	@Test
	public void modify_00_get()
	{
		Assertions.assertEquals(facilities(2, 8, 14, 20), dao.getById(VALUE.id).facilities);
	}

	@Test
	public void modify_01()
	{
		Assertions.assertEquals(4, dao.addFacilities(VALUE.id, List.of(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L)));	// Only 4 new facilities.
	}

	public static Stream<Arguments> modify_01_count()
	{
		return Stream.of(
			arguments(1L, new PeopleFilter().withHasFacilities(true), "Check hasFacilities: true"),
			arguments(1L, new PeopleFilter().withIncludeFacilities(3L, 9L, 15L, 21L), "Check inclusions: 1"),
			arguments(1L, new PeopleFilter().withIncludeFacilities(6L, 12L, 18L, 24L), "Check inclusions: 2"),
			arguments(1L, new PeopleFilter().withIncludeFacilities(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L), "Check inclusions: all"),
			arguments(0L, new PeopleFilter().withExcludeFacilities(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L), "Check exclusions: all"),
			arguments(0L, new PeopleFilter().withExcludeFacilities(6L, 12L, 18L, 24L), "Check exclusions: 2"),
			arguments(0L, new PeopleFilter().withExcludeFacilities(3L, 9L, 15L, 21L), "Check exclusions: 1"),
			arguments(0L, new PeopleFilter().withHasFacilities(false), "Check hasFacilities: false"));
	}

	@ParameterizedTest
	@MethodSource
	public void modify_01_count(final long total, final PeopleFilter filter, final String message)
	{
		Assertions.assertEquals(total, dao.count(filter), message);
	}

	@Test
	public void modify_01_get()
	{
		Assertions.assertEquals(facilities(2, 5, 8, 11, 14, 17, 20, 23), dao.getById(VALUE.id).facilities);
	}

	@Test
	public void modify_02()	// No change
	{
		Assertions.assertEquals(0, dao.addFacilities(VALUE.id, List.of(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L)));
	}

	@ParameterizedTest
	@MethodSource("modify_01_count")
	public void modify_02_count(final long total, final PeopleFilter filter, final String message)	// No change
	{
		Assertions.assertEquals(total, dao.count(filter), message);
	}

	@Test
	public void modify_02_get()	// No change
	{
		modify_01_get();
	}

	@Test
	public void remove_00()	// Not exceptions are thrown if the personId or facility IDs list is empty. Just returns zero.
	{
		Assertions.assertEquals(0, dao.removeFacilities("invalid", List.of(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L)));
		Assertions.assertEquals(0, dao.removeFacilities(VALUE.id, List.of(2L, 5L, 8L, 11L, 14L, 17L, 20L, 23L)));
		Assertions.assertEquals(0, dao.removeFacilities(VALUE.id, List.of()));
		Assertions.assertEquals(0, dao.removeFacilities(VALUE.id, null));
	}

	@ParameterizedTest
	@MethodSource("modify_01_count")
	public void remove_00_count(final long total, final PeopleFilter filter, final String message)	// No change
	{
		Assertions.assertEquals(total, dao.count(filter), message);
	}

	@Test
	public void remove_00_get()	// No change
	{
		modify_02_get();
	}

	@Test
	public void remove_01()
	{
		Assertions.assertEquals(4, dao.removeFacilities(VALUE.id, List.of(0L, 3L, 5L, 9L, 13L, 15L, 17L, 21L, 25L, 50L)));
	}

	public static Stream<Arguments> remove_01_count()
	{
		return Stream.of(
			arguments(1L, new PeopleFilter().withHasFacilities(true), "Check hasFacilities: true"),
			arguments(0L, new PeopleFilter().withIncludeFacilities(3L, 9L, 15L, 21L), "Check inclusions: 1"),
			arguments(1L, new PeopleFilter().withIncludeFacilities(6L, 12L, 18L, 24L), "Check inclusions: 2"),
			arguments(1L, new PeopleFilter().withIncludeFacilities(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L), "Check inclusions: all"),
			arguments(0L, new PeopleFilter().withExcludeFacilities(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L), "Check exclusions: all"),
			arguments(0L, new PeopleFilter().withExcludeFacilities(6L, 12L, 18L, 24L), "Check exclusions: 2"),
			arguments(1L, new PeopleFilter().withExcludeFacilities(3L, 9L, 15L, 21L), "Check exclusions: 1"),
			arguments(0L, new PeopleFilter().withHasFacilities(false), "Check hasFacilities: false"));
	}

	@ParameterizedTest
	@MethodSource
	public void remove_01_count(final long total, final PeopleFilter filter, final String message)
	{
		Assertions.assertEquals(total, dao.count(filter), message);
	}

	@Test
	public void remove_01_get()
	{
		Assertions.assertEquals(facilities(5, 11, 17, 23), dao.getById(VALUE.id).facilities);
	}

	@Test
	public void remove_02()
	{
		Assertions.assertEquals(4, dao.removeFacilities(VALUE.id, List.of(0L, 6L, 5L, 12L, 13L, 18L, 17L, 24L, 25L, 50L)));
	}

	@ParameterizedTest
	@MethodSource("count")
	public void remove_02_count(final long total, final PeopleFilter filter, final String message)
	{
		Assertions.assertEquals(total, dao.count(filter), message);
	}

	@Test
	public void remove_02_get()
	{
		Assertions.assertNull(dao.getById(VALUE.id).facilities);
	}

	@Test
	public void remove_03()	// No change
	{
		Assertions.assertEquals(0, dao.removeFacilities(VALUE.id, List.of(3L, 6L, 9L, 12L, 15L, 18L, 21L, 24L)));
	}

	@ParameterizedTest
	@MethodSource("count")
	public void remove_03_count(final long total, final PeopleFilter filter, final String message)	// No change
	{
		Assertions.assertEquals(total, dao.count(filter), message);
	}

	@Test
	public void remove_03_get()	// No change
	{
		remove_02_get();
	}

	@Test
	public void testRemove()
	{
		// Test the removal of the Person to ensure the deletion cascades.
		Assertions.assertTrue(dao.remove(VALUE.id));
	}
}
