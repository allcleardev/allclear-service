package app.allclear.platform.dao;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.errors.NotAuthenticatedException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.redis.RedisClient;
import app.allclear.platform.Config;
import app.allclear.platform.model.StartRequest;
import app.allclear.platform.value.*;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.SMSRequest;

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
	private static final int AUTH_DURATION = 5 * 60;	// Five minutes
	private static final String AUTH_KEY = "authentication:%s:%s";
	private final ObjectMapper mapper = JacksonUtils.createMapper();

	public static String key(final String id) { return String.format(ID, id); }
	public static String authKey(final String phone, final String token) { return String.format(AUTH_KEY, phone, token); }

	private final String authFrom;
	private final RedisClient redis;
	private final String authMessage;
	private final TwilioClient twilio;
	private final ThreadLocal<SessionValue> current = new ThreadLocal<>();

	public SessionDAO(final RedisClient redis, final Config conf) { this(redis, null, conf); }
	public SessionDAO(final RedisClient redis, final TwilioClient twilio, final Config conf)
	{
		this.redis = redis;
		this.twilio = twilio;
		this.authFrom = conf.authenticationPhone;
		this.authMessage = conf.authenticationSMSMessage;
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

	/** Adds an administrative user session.
	 * 
	 * @param admin
	 * @param rememberMe
	 * @return never NULL.
	 */
	public SessionValue add(final AdminValue admin, final boolean rememberMe)
	{
		return add(new SessionValue(rememberMe, admin));
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

	/** Sends an authentication token to the specified user.
	 * 
	 * @param phone
	 * @return the token to be used in tests.
	 */
	public String auth(final String phone)
	{
		var token = RandomStringUtils.randomAlphanumeric(10).toUpperCase();

		twilio.send(new SMSRequest(authFrom, String.format(authMessage, encode(phone, UTF_8), encode(token, UTF_8)), phone));
		redis.put(authKey(phone, token), phone, AUTH_DURATION);

		return token;
	}

	/** Confirms the authentication token.
	 * 
	 * @param phone
	 * @param token
	 * @throws ValidationException if the phone/token combination cannot be found
	 */
	public void auth(final String phone, final String token) throws ValidationException
	{
		var key = authKey(phone, token);
		redis.operation(j -> {
			if (!j.exists(key)) throw new ValidationException("Confirmation failed.");	// Do NOT give too much information on bad authentication requests.
			return j.del(key);
		});
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
	public SessionValue current(final SessionValue value)
	{
		current.set(value);

		return value;
	}

	SessionValue current(final PeopleValue value) { return current(new SessionValue(false, value)); }	// For tests

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
	
	/** Clears the current session. */
	public void clear()
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
