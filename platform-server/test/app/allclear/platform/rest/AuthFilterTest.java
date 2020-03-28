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
import app.allclear.platform.value.PeopleValue;
import app.allclear.platform.value.SessionValue;

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

	private static SessionValue PERSON;
	private static SessionValue START;

	@BeforeAll
	public static void up()
	{
		PERSON = dao.add(new PeopleValue("bob", "888-555-0000", true), true);
		START = dao.add(new StartRequest("888-555-0001", false, true));
	}

	public static Stream<Arguments> requiresAuth()
	{
		return Stream.of(
			arguments(null, true),
			arguments("", true),
			arguments("a", true),
			arguments("/a", true),
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
			arguments("peoples/starts", true)
			);
	}

	@ParameterizedTest
	@MethodSource
	public void requiresAuth(final String path, final boolean expected)
	{
		Assertions.assertEquals(expected, auth.requiresAuth(path));
	}

	public static Stream<Arguments> failure()
	{
		return Stream.of(
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
