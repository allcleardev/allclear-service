package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static app.allclear.testing.TestingUtils.*;

import java.util.Date;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.AuthorizationExceptionMapper;
import app.allclear.common.errors.NotFoundExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.filter.SessionFilter;
import app.allclear.platform.value.*;

/** Functional test that verifies the SessionResource component.
 * 
 * @author smalleyd
 * @version 1.0.68
 * @since 4/7/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class SessionResourceTest
{
	private static final FakeRedisClient redis = new FakeRedisClient();
	private static final SessionDAO dao = new SessionDAO(redis, ConfigTest.loadTest());
	private static SessionValue ADMIN;
	private static SessionValue PERSON;
	private static SessionValue PERSON_1;

	private static final String TARGET = "/sessions";

	private static final GenericType<QueryResults<SessionValue, SessionFilter>> TYPE_QUERY_RESULTS = new GenericType<QueryResults<SessionValue, SessionFilter>>() {};

	private final ResourceExtension RULE = ResourceExtension.builder()
		.addProvider(new AuthorizationExceptionMapper())
		.addProvider(new NotFoundExceptionMapper())
		.addResource(new SessionResource(dao))
		.build();

	@Test
	public void add()
	{
		var now = new Date();
		ADMIN = dao.add(new AdminValue("admin", true).withCreatedAt(now).withUpdatedAt(now), false);
		PERSON = dao.add(new PeopleValue("person", "8885551000", true).withCreatedAt(now).withUpdatedAt(now), false);
		PERSON_1 = dao.add(new PeopleValue("person_1", "8885551001", true).withCreatedAt(now).withUpdatedAt(now), false);
	}

	@Test
	public void get()
	{
		dao.current(PERSON);

		var response = request().get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		Assertions.assertEquals(PERSON, response.readEntity(SessionValue.class), "Check");
	}

	@Test
	public void getById_as_admin()
	{
		dao.current(ADMIN);

		var response = request(PERSON.id).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");
		Assertions.assertEquals(PERSON, response.readEntity(SessionValue.class), "Check");
	}

	@Test
	public void getById_as_person()
	{
		dao.current(PERSON);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(PERSON.id).get().getStatus(), "Status");
	}

	@Test
	public void search_as_admin()
	{
		dao.current(ADMIN);

		var response = request("search").post(Entity.json(new SessionFilter()));
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var results = response.readEntity(TYPE_QUERY_RESULTS);
		Assertions.assertEquals(3L, results.total, "Check total");
		assertThat(results.records).as("Check records").hasSize(3).containsOnly(ADMIN, PERSON, PERSON_1);
	}

	@Test
	public void search_as_person()
	{
		dao.current(PERSON);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request("search").post(Entity.json(new SessionFilter())).getStatus(), "Status");
	}

	@Test
	public void testRemove_as_admin()
	{
		dao.current(ADMIN);

		Assertions.assertEquals(HTTP_STATUS_OK, request(PERSON_1.id).get().getStatus(), "Status: before");

		Assertions.assertEquals(HTTP_STATUS_OK, request(PERSON_1.id).delete().getStatus(), "Status: deletion");
	}

	@Test
	public void testRemove_as_admin_check()
	{
		Assertions.assertEquals(HTTP_STATUS_NOT_FOUND, request(PERSON_1.id).get().getStatus(), "Status: after");
		Assertions.assertEquals(2L, request("search").post(Entity.json(new SessionFilter()), TYPE_QUERY_RESULTS).total, "Check total");
	}

	@Test
	public void testRemove_as_person()
	{
		dao.current(PERSON);

		Assertions.assertEquals(HTTP_STATUS_NOT_AUTHORIZED, request(PERSON.id).delete().getStatus(), "Status");
	}

	@Test
	public void testRemove_as_person_check()
	{
		dao.current(ADMIN);

		Assertions.assertEquals(2L, request("search").post(Entity.json(new SessionFilter()), TYPE_QUERY_RESULTS).total, "Check total");
	}

	@Test
	public void testRemove_self()
	{
		dao.current(PERSON);

		Assertions.assertEquals(HTTP_STATUS_OK, request().delete().getStatus(), "Status");
	}

	@Test
	public void testRemove_self_check()
	{
		dao.current(ADMIN);

		Assertions.assertEquals(1L, request("search").post(Entity.json(new SessionFilter()), TYPE_QUERY_RESULTS).total, "Check total");
	}

	private WebTarget target() { return RULE.client().target(TARGET); }
	private Invocation.Builder request() { return request(target()); }
	private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
