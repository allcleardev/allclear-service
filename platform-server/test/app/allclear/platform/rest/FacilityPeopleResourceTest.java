package app.allclear.platform.rest;

import static java.util.stream.Collectors.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.junit.hibernate.*;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.value.CreatedValue;
import app.allclear.common.value.OperationResponse;
import app.allclear.google.client.MapClient;
import app.allclear.platform.App;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.*;
import app.allclear.platform.entity.Facility;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.value.*;

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
public class FacilityPeopleResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(App.ENTITIES);
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(DAO_RULE);

	private static FacilityDAO dao = null;
	private static PeopleDAO peopleDao = null;
	private static SessionDAO sessionDao = null;
	private static FacilityValue VALUE = null;
	private static FacilityValue VALUE_1 = null;
	private static List<PeopleValue> PEOPLE = null;
	private static final SessionValue ADMIN = new SessionValue(false, new AdminValue("admin"));
	private static final SessionValue EDITOR = new SessionValue(false, new AdminValue("editor", false, true));
	private static SessionValue PERSON = null;

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new FacilityResource(dao, sessionDao, mock(MapClient.class)))
		.build();

	/** Primary URI to test. */
	private static final String TARGET = "/facilities";
	private static final GenericType<QueryResults<FacilityValue, FacilityFilter>> TYPE_QUERY_RESULTS =
		new GenericType<QueryResults<FacilityValue, FacilityFilter>>() {};

	private static FacilityValue cloned() { return SerializationUtils.clone(VALUE); }
	private static FacilityValue cloned_1() { return SerializationUtils.clone(VALUE_1); }

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
		sessionDao = new SessionDAO(new FakeRedisClient(), ConfigTest.loadTest());
	}

	@Test
	public void add()
	{
		PEOPLE = IntStream.range(0, 10).mapToObj(i -> peopleDao.add(new PeopleValue(i))).collect(toList());
		PERSON = new SessionValue(false, PEOPLE.get(0));

		sessionDao.current(EDITOR);
		VALUE = request().post(Entity.json(new FacilityValue(0).withPeople(created(7, 1, 4))), FacilityValue.class);

		sessionDao.current(ADMIN);
		VALUE_1 = request().post(Entity.json(new FacilityValue(1).withPeople(created(1, 4, 7))), FacilityValue.class);
	}

	@Test
	public void add_activate()
	{
		transRule.getSession().get(Facility.class, VALUE.withActive(true).id).setActive(true);
	}

	public static Stream<Arguments> getById()
	{
		return Stream.of(
			arguments(VALUE, ADMIN, List.of()),
			arguments(VALUE, EDITOR, List.of()),
			arguments(VALUE, PERSON, null),
			arguments(VALUE_1, ADMIN, created(1, 4, 7)),
			arguments(VALUE_1, EDITOR, created(1, 4, 7)),
			arguments(VALUE_1, PERSON, null));
	}

	@ParameterizedTest
	@MethodSource
	public void getById(final FacilityValue value, final SessionValue session, final List<CreatedValue> expected)
	{
		sessionDao.current(session);

		assertThat(request(value.id.toString()).get(FacilityValue.class).people).isEqualTo(expected);
	}

	@ParameterizedTest
	@MethodSource({"getById"})
	public void get_search(final FacilityValue value, final SessionValue session, final List<CreatedValue> expected)
	{
		sessionDao.current(session);

		var o = assertThat(request("search").post(Entity.json(new FacilityFilter().withId(value.id)), TYPE_QUERY_RESULTS).records.get(0).people);
		if (CollectionUtils.isNotEmpty(expected))
			o.isEqualTo(expected);
		else
			o.isNull();
	}

	@ParameterizedTest
	@MethodSource({"getById"})
	public void getByNameWithException(final FacilityValue value, final SessionValue session, final List<CreatedValue> expected)
	{
		sessionDao.current(session);

		assertThat(request(target().path("name").queryParam("name", value.name)).get(FacilityValue.class).people).isEqualTo(expected);
	}

	private List<CreatedValue> get() { return request(VALUE.id.toString()).get(FacilityValue.class).people; }
	private List<CreatedValue> get_1() { return request(VALUE_1.id.toString()).get(FacilityValue.class).people; }

	@Test
	public void modify()
	{
		sessionDao.current(ADMIN);
		request().put(Entity.json(cloned().withPeople(created(2, 5, 8))));

		sessionDao.current(EDITOR);
		request().put(Entity.json(cloned_1().withPeople(created(2, 5, 8))));
	}

	public static Stream<SessionValue> modify_check_as_admin() { return Stream.of(ADMIN, EDITOR); }

	@ParameterizedTest
	@MethodSource
	public void modify_check_as_admin(final SessionValue session)
	{
		sessionDao.current(session);

		assertThat(get()).as("Check zero").isEqualTo(created(2, 5, 8));
		assertThat(get_1()).as("Check first").isEqualTo(created(1, 4, 7));

		var values = request("search").post(Entity.json(new FacilityFilter()), TYPE_QUERY_RESULTS).records;
		assertThat(values.get(1).people).as("Check zero: search").isEqualTo(created(2, 5, 8));
		assertThat(values.get(0).people).as("Check first: search").isEqualTo(created(1, 4, 7));
	}

	@Test
	public void modify_check_as_person()
	{
		sessionDao.current(PERSON);

		assertThat(get()).as("Check zero").isNull();
		assertThat(get_1()).as("Check first").isNull();

		var values = request("search").post(Entity.json(new FacilityFilter()), TYPE_QUERY_RESULTS).records;
		assertThat(values.get(1).people).as("Check zero: search").isNull();
		assertThat(values.get(0).people).as("Check first: search").isNull();
	}

	@ParameterizedTest
	@CsvSource({"1", "2"})
	public void remove(final String id)
	{
		sessionDao.current(ADMIN);

		Assertions.assertTrue(request(id).delete(OperationResponse.class).operation);
	}

	/** Helper method - creates the base WebTarget. */
	private WebTarget target() { return RULE.client().target(TARGET); }

	/** Helper method - creates the request from the WebTarget. */
	private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
