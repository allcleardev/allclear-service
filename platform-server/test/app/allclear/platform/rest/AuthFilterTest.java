package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.allclear.common.errors.NotAuthenticatedException;
import app.allclear.common.redis.FakeRedisClient;
import app.allclear.common.resources.Headers;
import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.model.StartRequest;
import app.allclear.platform.value.*;

/** Functional test class that verifies the AuthFilter interceptor.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/26/2020
 *
 */

public class AuthFilterTest
{
	private static final FakeRedisClient redis = new FakeRedisClient();
	private static final SessionDAO dao = new SessionDAO(redis, ConfigTest.loadTest());
	private static final AuthFilter auth = new AuthFilter(dao);

	private static SessionValue ADMIN;
	private static SessionValue SUPER;
	private static SessionValue PERSON;
	private static SessionValue START;

	@BeforeAll
	public static void up()
	{
		ADMIN = dao.add(new AdminValue("randy", false), false);
		PERSON = dao.add(new PeopleValue("bob", "888-555-0000", true), true);
		START = dao.add(new StartRequest("888-555-0001", false, true));
		SUPER = dao.add(new AdminValue("sandy", true), true);
	}

	public static Stream<Arguments> admin()
	{
		return Stream.of(
			arguments(null, false),
			arguments("", false),
			arguments("a", false),
			arguments("/a", false),
			arguments("/admins", false),
			arguments("admins", true),
			arguments("admins/self", true),
			arguments("admins/search", true),
			arguments("/admins", false),
			arguments("/admins/self", false),
			arguments("/admins/search", false),
			arguments("info/config", true),
			arguments("/info/config", false),
			arguments("info/health", false),
			arguments("info/ping", false),
			arguments("info/version", false),
			arguments("peoples/admin", false),
			arguments("types/admin", false),
			arguments("/peoples", false),
			arguments("types/peopleStatuses", false),
			arguments("/types/peopleStatuses", false),
			arguments("types/peopleStatuses", false),
			arguments("type/peopleStatuses", false),
			arguments("peoples/auth", false),
			arguments("peoples/authenticated", false),
			arguments("peoples/confirm", false),
			arguments("peoples-confirm", false),
			arguments("peoples/start", false),
			arguments("/peoples/start", false),
			arguments("peoples/starts", false));
	}

	@ParameterizedTest
	@MethodSource
	public void admin(final String path, final boolean expected)
	{
		Assertions.assertEquals(expected, auth.admin(path));
	}

	public static Stream<Arguments> requiresAuth()
	{
		return Stream.of(
			arguments(null, true),
			arguments("", true),
			arguments("a", true),
			arguments("/a", true),
			arguments("admins", true),
			arguments("admins/self", true),
			arguments("admins/search", true),
			arguments("admins/auth", false),
			arguments("admin/auth", true),
			arguments("info/config", true),
			arguments("info/health", false),
			arguments("info/ping", false),
			arguments("info/version", false),
			arguments("/info/version", true),
			arguments("/peoples", true),
			arguments("types/peopleStatuses", false),
			arguments("/types/peopleStatuses", true),
			arguments("types/peopleStatuses", false),
			arguments("type/peopleStatuses", true),
			arguments("peoples/auth", false),
			arguments("peoples/authenticated", true),
			arguments("peoples/confirm", false),
			arguments("peoples-confirm", true),
			arguments("peoples/start", false),
			arguments("/peoples/start", true),
			arguments("peoples/starts", true));
	}

	@ParameterizedTest
	@MethodSource
	public void requiresAuth(final String path, final boolean expected)
	{
		Assertions.assertEquals(expected, auth.requiresAuth(path));
	}

	public static Stream<Arguments> self()
	{
		return Stream.of(
			arguments(null, false),
			arguments("", false),
			arguments("a", false),
			arguments("/a", false),
			arguments("self", false),
			arguments("/self", true),
			arguments("/admins", false),
			arguments("admins", false),
			arguments("admins/self", true),
			arguments("admins/search", false),
			arguments("/admins", false),
			arguments("/admins/self", true),
			arguments("/admins/selfs", false),
			arguments("/admins/search", false),
			arguments("peoples/admin", false),
			arguments("types/admin", false),
			arguments("/peoples", false),
			arguments("/peoples/self", true),
			arguments("types/peopleStatuses", false),
			arguments("/types/peopleStatuses", false),
			arguments("types/peopleStatuses", false),
			arguments("type/peopleStatuses", false),
			arguments("peoples/auth", false),
			arguments("peoples/authenticated", false),
			arguments("peoples/confirm", false),
			arguments("peoples-confirm", false),
			arguments("peoples/start", false),
			arguments("/peoples/start", false),
			arguments("peoples/starts", false));
	}

	@ParameterizedTest
	@MethodSource
	public void self(final String path, final boolean expected)
	{
		Assertions.assertEquals(expected, auth.self(path));
	}

	public static Stream<Arguments> failure()
	{
		return Stream.of(
			arguments("admins", "", "Session ID is required."),
			arguments("admins", null, "Session ID is required."),
			arguments("admins", "INVALID", "The ID 'INVALID' is invalid."),
			arguments("admins/self", "", "Session ID is required."),
			arguments("admins/self", null, "Session ID is required."),
			arguments("admins/self", "INVALID", "The ID 'INVALID' is invalid."),
			arguments("admins", PERSON.id, "Requires an Administrative Session."),
			arguments("admins", START.id, "Requires an Administrative Session."),
			arguments("admins", ADMIN.id, "Requires a Super-Admin Session."),
			arguments("admins/auth", PERSON.id, "Requires an Administrative Session."),	// admins/auth if NO session is provided.
			arguments("admins/auth", START.id, "Requires an Administrative Session."),
			arguments("admins/auth", ADMIN.id, "Requires a Super-Admin Session."),
			arguments("admins/self", PERSON.id, "Requires an Administrative Session."),
			arguments("admins/self", START.id, "Requires an Administrative Session."),
			arguments("info/config", ADMIN.id, "Requires a Super-Admin Session."),
			arguments("info/config", PERSON.id, "Requires an Administrative Session."),
			arguments("info/config", START.id, "Requires an Administrative Session."),
			arguments("peoples", "", "Session ID is required."),
			arguments("peoples", null, "Session ID is required."),
			arguments("peoples", "INVALID", "The ID 'INVALID' is invalid."),
			arguments("types/peopleStatuses", "INVALID", "The ID 'INVALID' is invalid."),
			arguments("peoples/register", PERSON.id, "Requires a Registration Session."),
			arguments("peoples", START.id, "Requires a Non-registration Session.")
			);
	}

	@ParameterizedTest
	@MethodSource
	public void failure(final String path, final String sessionId, final String message) throws Exception
	{
		assertThat(Assertions.assertThrows(NotAuthenticatedException.class, () -> auth.filter(context(path, sessionId))))
			.hasMessage(message);
	}

	public static Stream<Arguments> success()
	{
		return Stream.of(
			arguments("admins", SUPER.id),
			arguments("admins/auth", ""),
			arguments("admins/auth", null),
			arguments("admins/auth", SUPER.id),
			arguments("admins/self", SUPER.id),
			arguments("admins/search", SUPER.id),
			arguments("admins/self", ADMIN.id),
			arguments("info/config", SUPER.id),
			arguments("peoples/start", ""),
			arguments("peoples/confirm", null),
			arguments("peoples/confirm", PERSON.id),
			arguments("peoples/confirm", START.id),
			arguments("peoples", PERSON.id),
			arguments("peoples/register", START.id),
			arguments("types/peopleStatuses", null),
			arguments("types/peopleStatuses", START.id),
			arguments("types/peopleStatuses", PERSON.id)
			);
	}

	@ParameterizedTest
	@MethodSource
	public void success(final String path, final String sessionId) throws Exception
	{
		auth.filter(context(path, sessionId));

		if (StringUtils.isEmpty(sessionId))
			Assertions.assertNull(dao.current());
		else
			Assertions.assertEquals(sessionId, dao.current().id);
	}

	private ContainerRequestContext context(final String path, final String sessionId)
	{
		var uri = mock(UriInfo.class);
		when(uri.getPath()).thenReturn(path);

		var value = mock(ContainerRequestContext.class);
		when(value.getUriInfo()).thenReturn(uri);
		when(value.getHeaderString(eq(Headers.HEADER_SESSION))).thenReturn(sessionId);

		return value;
	}
}
