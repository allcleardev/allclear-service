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
	private final ThreadLocal<SessionValue> current = new ThreadLocal<>();

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

	/** Promotes a registration session to a person session maintaining the same ID.
	 * 
	 * @param person
	 * @param rememberMe
	 * @return never NULL
	 */
	public SessionValue promote(final PeopleValue person, final boolean rememberMe)
	{
		try
		{
			var o = get().promote(rememberMe, person);
			redis.put(key(o.id), mapper.writeValueAsString(o), o.seconds());

			return o;
		}
		catch (final IOException ex) { throw new RuntimeException(ex); }
	}

	/** Gets the session value associated with the current thread.
	 * 
	 * @return NULL if there is no current session
	 */
	public SessionValue current() { return current.get(); }

	/** Internal/test usage - sets the current threads associated session.
	 * 
	 * @param value
	 * @return the supplied session value
	 */
	SessionValue current(final SessionValue value)
	{
		current.set(value);

		return value;
	}

	/** Set the current threads associated session based on the supplied session ID.
	 * 
	 * @param id
	 * @return never NULL
	 * @throws NotAuthenticatedException
	 */
	public SessionValue current(final String id) throws NotAuthenticatedException
	{
		return current(get(id));
	}
	
	/** Internal/test usage - clears the current session. */
	void clear()
	{
		current.remove();
	}

	/** Gets the current session.
	 * 
	 * @return never NULL
	 * @throws NotAuthenticatedException
	 */
	public SessionValue get() throws NotAuthenticatedException
	{
		var v = current.get();
		if (null == v) throw new NotAuthenticatedException("No current session is available.");

		return v;
	}

	/** Gets the session value by the specified ID and extends the expiration.
	 * 
	 * @param id
	 * @return never NULL
	 * @throws NotAuthenticatedException if not found
	 */
	public SessionValue get(final String id) throws NotAuthenticatedException
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

	/** Removes the current session. */
	public void remove()
	{
		var v = current();
		if (null != v) remove(v.id);
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
