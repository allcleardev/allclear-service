package app.allclear.platform.rest;

import static app.allclear.common.resources.Headers.HEADER_SESSION;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.container.*;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.errors.NotAuthenticatedException;
import app.allclear.platform.dao.SessionDAO;

/** Jersey request interceptor to handle session management.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/26/2020
 *
 */

public class AuthFilter implements ContainerRequestFilter
{
	public static final String PATH_ADMINS = "admins";
	public static final List<String> PATHS_ADMINS = List.of("customers", "logs", "maps", "queues", "registrations");
	public static final String PATH_INFO_CONFIG = "info/config";
	public static final String PATH_SELF = "/self";
	public static final String PATH_TWILIO = "twilio/";	// Uses Digest Auth
	public static final String PATH_TYPES = "types/";
	public static final List<String> PATHS_NO_AUTH = List.of("admins/auth", "facilities/search", "info/health", "info/ping", "info/version", "peoples/auth", "peoples/confirm", "peoples/start", "swagger.json");
	public static final String PATH_REGISTER = "peoples/register";

	private final SessionDAO dao;

	public AuthFilter(final SessionDAO dao)
	{
		this.dao = dao;
	}

	@Override
	public void filter(final ContainerRequestContext request) throws IOException
	{
		var path = StringUtils.trimToNull(request.getUriInfo().getPath());
		var sessionId = StringUtils.trimToNull(request.getHeaderString(HEADER_SESSION));
		if (null == sessionId)
		{
			if (requiresAuth(path)) throw new NotAuthenticatedException("Session ID is required.");

			dao.clear();	// No current session.
			return;	// Otherwise, exit. There is no session ID to resolve.
		}

		var session = dao.current(sessionId);
		if (PATH_REGISTER.equals(path))
		{
			if (!session.registration()) throw new NotAuthenticatedException("Requires a Registration Session.");
		}
		else if (admin(path))
		{
			if (!session.admin()) throw new NotAuthenticatedException("Requires an Administrative Session.");
			if (!self(path) && !session.supers()) throw new NotAuthenticatedException("Requires a Super-Admin Session.");	// Only super-admins can administer Admins. But allow all admins to administer themselves.
		}
		else if (admins(path) && !session.admin())
			throw new NotAuthenticatedException("Requires an Administrative Session.");
		else if (session.registration() && requiresAuth(path))
			throw new NotAuthenticatedException("Requires a Non-registration Session.");
	}

	boolean admin(final String path)
	{
		return ((null != path) && (path.equals(PATH_INFO_CONFIG) || path.startsWith(PATH_ADMINS)));
	}

	boolean admins(final String path)
	{
		return ((null != path) && PATHS_ADMINS.stream().anyMatch(v -> path.startsWith(v)));
	}

	boolean requiresAuth(final String path)
	{
		return ((null == path) || !(path.startsWith(PATH_TWILIO) || path.startsWith(PATH_TYPES) || PATHS_NO_AUTH.contains(path)));
	}

	boolean self(final String path)
	{
		return ((null != path) && path.endsWith(PATH_SELF));
	}
}
