package app.allclear.platform.dao;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.errors.NotAuthenticatedException;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.redis.RedisClient;
import app.allclear.platform.model.StartRequest;
import app.allclear.platform.value.*;

/** Data access object that manages user sessions.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/25/2020
 *
 */

public class SessionDAO
{
	private static final String ID = "session:%s";
	private final ObjectMapper mapper = JacksonUtils.createMapper();

	public static String key(final String id) { return String.format(ID, id); }

	private final RedisClient redis;

	public SessionDAO(final RedisClient redis)
	{
		this.redis = redis;
	}

	/** Adds a short-term registration session.
	 * 
	 * @param request
	 * @return never NULL.
	 */
	public SessionValue add(final StartRequest request)
	{
		return add(new SessionValue(request));
	}

	/** Adds a regular user session.
	 * 
	 * @param person
	 * @param rememberMe
	 * @return never NULL.
	 */
	public SessionValue add(final PeopleValue person, final boolean rememberMe)
	{
		return add(new SessionValue(rememberMe, person));
	}

	SessionValue add(final SessionValue value)
	{
		try
		{
			redis.put(key(value.id), mapper.writeValueAsString(value), value.seconds());
		}
		catch (final IOException ex) { throw new RuntimeException(ex); }

		return value;
	}

	/** Gets the session value by the specified ID and extends the expiration.
	 * 
	 * @param id
	 * @return never NULL
	 * @throws NotAuthenticatedException if not found
	 */
	SessionValue get(final String id) throws NotAuthenticatedException
	{
		return redis.operation(j -> {
			var key = key(id);
			var v = j.get(key);
			if (null == v) throw new NotAuthenticatedException("The ID '" + id + "' is invalid.");

			try
			{
				var o = mapper.readValue(v, SessionValue.class);

				o.accessed();
				j.setex(key, o.seconds(), mapper.writeValueAsString(o));

				return o;
			}
			catch (final IOException ex) { throw new RuntimeException(ex); }
		});
	}

	/** Removes a single session.
	 * 
	 * @param id
	 */
	public void remove(final String id)
	{
		redis.remove(key(id));
	}
}
