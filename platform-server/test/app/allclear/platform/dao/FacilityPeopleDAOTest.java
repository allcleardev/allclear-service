package app.allclear.platform.dao;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.hibernate.*;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.value.CreatedValue;
import app.allclear.platform.App;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.value.FacilityValue;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Functional test for the data access object that handles access to the FacilityPeople entity.
*
*	@author smalleyd
*	@version 1.1.101
*	@since July 5, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class FacilityPeopleDAOTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static FacilityDAO dao = null;
	private static PeopleDAO peopleDao = null;
	private static FacilityValue VALUE = null;
	private static FacilityValue VALUE_1 = null;
	private static List<PeopleValue> PEOPLE = null;

	private static FacilityValue cloned() { return SerializationUtils.clone(VALUE); }
	private static FacilityValue cloned_1() { return SerializationUtils.clone(VALUE_1); }
	private static List<CreatedValue> get() { return dao.getById(VALUE.id, true).people; }
	private static List<CreatedValue> get_1() { return dao.getById(VALUE_1.id, true).people; }

	private static String id(final int i) { return PEOPLE.get(i).id; }
	private static List<String> ids(final int... indices)
	{
		return Arrays.stream(indices).mapToObj(i -> id(i)).collect(toList());
	}
	private static List<CreatedValue> created(final int... indices)
	{
		return Arrays.stream(indices).mapToObj(i -> PEOPLE.get(i).created()).collect(toList());
	}

	@BeforeAll
	public static void up()
	{
		var factory = DAO_RULE.getSessionFactory();
		peopleDao = new PeopleDAO(factory);
		dao = new FacilityDAO(factory, new TestAuditor());
	}

	@Test
	public void add()
	{
		PEOPLE = IntStream.range(0, 10).mapToObj(i -> peopleDao.add(new PeopleValue(i))).collect(toList());
		VALUE = dao.add(new FacilityValue(0).withPeople(created(7, 1, 4)), true);
		VALUE_1 = dao.add(new FacilityValue(1).withPeople(created(1, 4, 7)), false);
	}

	@Test
	public void add_activate()
	{
		dao.findWithException(VALUE_1.id).setActive(true);
	}

	@Test
	public void add_invalidPersonId()
	{
		assertThat(assertThrows(ValidationException.class, () -> dao.add(new FacilityValue(2).withPeople(id(0), "NOT-ID", id(6)), true)))
			.hasMessage("The Person ID 'NOT-ID' is invalid.");

		transRule.rollback();
	}

	@Test
	public void add_invalidPersonId_nonAdmin()
	{
		Assertions.assertEquals(4L, dao.add(new FacilityValue(2).withPeople(id(0), "NOT-ID", id(6)), false).id);	// No error because the people are NOT added.

		transRule.rollback();
	}

	public static Stream<Arguments> getById()
	{
		return Stream.of(
			arguments(VALUE.id, true, created(1, 4, 7)),
			arguments(VALUE.id, false, null),
			arguments(VALUE_1.id, true, List.of()),
			arguments(VALUE_1.id, false, null));
	}

	@ParameterizedTest
	@MethodSource
	public void getById(final Long id, final boolean admin, final List<CreatedValue> expected)
	{
		assertThat(dao.getById(id, admin).people).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource({"getById"})
	public void getByIdWithException(final Long id, final boolean admin, final List<CreatedValue> expected)
	{
		assertThat(dao.getByIdWithException(id, admin).people).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource({"getById"})
	public void getById_search(final Long id, final boolean admin, final List<CreatedValue> expected)
	{
		var o = assertThat(dao.search(new FacilityFilter().withId(id), admin).records.get(0).people);
		if (CollectionUtils.isNotEmpty(expected))
			o.isEqualTo(expected);
		else
			o.isNull();
	}

	public static Stream<Arguments> getByNameWithException()
	{
		return Stream.of(
			arguments(VALUE.name, true, created(1, 4, 7)),
			arguments(VALUE.name, false, null),
			arguments(VALUE_1.name, true, List.of()),
			arguments(VALUE_1.name, false, null));
	}

	@ParameterizedTest
	@MethodSource
	public void getByNameWithException(final String name, final boolean admin, final List<CreatedValue> expected)
	{
		assertThat(dao.getByNameWithException(name, admin).people).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource({"getByNameWithException"})
	public void getByName_search(final String name, final boolean admin, final List<CreatedValue> expected)
	{
		var o = assertThat(dao.search(new FacilityFilter().withName(name), admin).records.get(0).people);
		if (CollectionUtils.isNotEmpty(expected))
			o.isEqualTo(expected);
		else
			o.isNull();
	}

	@Test
	public void modify_00()
	{
		dao.update(cloned().withPeople(created(2, 5, 8)), false);
		dao.update(cloned_1().withPeople(created(2, 5, 8)), true);
	}

	@Test
	public void modify_00_check()
	{
		assertThat(get()).as("Check zero").isEqualTo(created(1, 4, 7));
		assertThat(get_1()).as("Check first").isEqualTo(created(2, 5, 8));

		var values = dao.search(new FacilityFilter(), true).records;
		assertThat(values.get(1).people).as("Check zero: search").isEqualTo(created(1, 4, 7));
		assertThat(values.get(0).people).as("Check first: search").isEqualTo(created(2, 5, 8));
	}

	@Test
	public void modify_01()
	{
		dao.update(cloned().nullPeople(), false);
		dao.update(cloned_1().nullPeople(), true);
	}

	@Test
	public void modify_01_check()
	{
		modify_00_check();
	}

	@Test
	public void modify_02()
	{
		dao.update(cloned().emptyPeople(), false);
		dao.update(cloned_1().emptyPeople(), true);
	}

	@Test
	public void modify_02_check()
	{
		assertThat(get()).as("Check zero").isEqualTo(created(1, 4, 7));
		assertThat(get_1()).as("Check first").isEmpty();

		var values = dao.search(new FacilityFilter(), true).records;
		assertThat(values.get(1).people).as("Check zero: search").isEqualTo(created(1, 4, 7));
		assertThat(values.get(0).people).as("Check first: search").isNull();
	}

	@Test
	public void modify_03()
	{
		dao.update(cloned().withPeople(created(2, 5, 8)), true);
		dao.update(cloned_1().withPeople(created(2, 5, 8)), false);
	}

	@Test
	public void modify_03_check()
	{
		assertThat(get()).as("Check zero").isEqualTo(created(2, 5, 8));
		assertThat(get_1()).as("Check first").isEmpty();

		var values = dao.search(new FacilityFilter(), false).records;
		assertThat(values.get(1).people).as("Check zero: search").isNull();
		assertThat(values.get(0).people).as("Check first: search").isNull();
	}

	@Test
	public void modify_04()
	{
		dao.update(cloned().withPeople(created(5, 9, 3)), true);
		dao.update(cloned_1().withPeople(created(5, 9, 3)), false);
	}

	@Test
	public void modify_04_check()
	{
		assertThat(get()).as("Check zero").isEqualTo(created(3, 5, 9));
		assertThat(get_1()).as("Check first").isEmpty();
	}

	@Test
	public void modify_05()
	{
		dao.update(cloned().withPeople(created(6)), true);
		dao.update(cloned_1().withPeople(created(6)), false);
	}

	@Test
	public void modify_05_check()
	{
		assertThat(get()).as("Check zero").isEqualTo(created(6));
		assertThat(get_1()).as("Check first").isEmpty();
	}

	public static Stream<Arguments> search()
	{
		return Stream.of(
			arguments(ids(6), true, 1L),
			arguments(ids(6), false, 2L),
			arguments(ids(4, 5, 7), true, 0L),
			arguments(ids(4, 5, 7), false, 2L));
	}

	@ParameterizedTest
	@MethodSource
	public void search(final List<String> people, final boolean admin, final long total)
	{
		Assertions.assertEquals(total, dao.search(new FacilityFilter().withPeople(people), admin).total);
	}

	@ParameterizedTest
	@CsvSource({"1", "2"})
	public void testRemove(final Long id)
	{
		Assertions.assertTrue(dao.remove(id));
	}
}
