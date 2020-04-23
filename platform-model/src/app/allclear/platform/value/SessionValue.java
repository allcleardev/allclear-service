package app.allclear.platform.value;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.time.DateUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.platform.model.StartRequest;

/** Value object that represents an authenticated session to interact
 *  with the server-side resources.
 *
 * @author smalleyd
 * @versiion
 *
 */
public class SessionValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final long DURATION_SHORT = 30L * 60L * 1000L;
	public static final long DURATION_LONG = 30L * 24L * 60L * 60L * 1000L;
	public static SessionValue anonymous() { return new SessionValue(); }	// ALLCLEAR-320: DLS on 4/23/2020.

	// Members
	public final String id;
	public final boolean rememberMe;
	public final long duration;	// Millseconds
	public final AdminValue admin;
	public final PeopleValue person;
	public final StartRequest registration;
	public final Date expiresAt;
	public final Date lastAccessedAt;
	public final Date createdAt;

	// Accessors
	public int seconds() { return (int) (duration / 1000L); }
	public boolean admin() { return (null != admin); }
	public boolean supers() { return admin() && admin.supers; }
	public boolean person() { return (null != person); }
	public boolean registration() { return (null != registration); }
	public String name() { return (admin()) ? admin.id : (person() ? person.name : registration.phone); }

	// Mutators
	public SessionValue accessed()
	{
		var now = System.currentTimeMillis();
		expiresAt.setTime(now + duration);
		lastAccessedAt.setTime(now);

		return this;
	}

	// Promotes an existing Registration session into a Person session.
	public SessionValue promote(final boolean rememberMe, final PeopleValue person)
	{
		var now = new Date();
		var duration = rememberMe ? DURATION_LONG : DURATION_SHORT;
		return new SessionValue(id,
			rememberMe,
			duration,
			null,
			person,
			null,
			new Date(now.getTime() + duration),
			now,
			createdAt);
	}

	private SessionValue()	// Anonymous. ALLCLEAR-320: DLS on 4/23/2020.
	{
		this(
			false,
			DURATION_SHORT,
			null,
			null,
			null,
			new Date());
	}

	public SessionValue(final StartRequest registration)
	{
		this(
			false,
			DURATION_SHORT,
			null,
			null,
			registration,
			new Date());
	}

	public SessionValue(final boolean rememberMe, final AdminValue admin)
	{
		this(rememberMe,
			rememberMe ? DURATION_LONG : DURATION_SHORT,
			admin,
			null,
			null,
			new Date());
	}

	public SessionValue(final boolean rememberMe, final PeopleValue person)
	{
		this(rememberMe,
			rememberMe ? DURATION_LONG : DURATION_SHORT,
			null,
			person,
			null,
			new Date());
	}

	public SessionValue(final boolean rememberMe,
		final long duration,
		final AdminValue admin,
		final PeopleValue person,
		final StartRequest registration,
		final Date createdAt)
	{
		this(UUID.randomUUID().toString(),
			rememberMe,
			duration,
			admin,
			person,
			registration,
			new Date(createdAt.getTime() + duration),
			createdAt,
			createdAt);
	}

	public SessionValue(@JsonProperty("id") final String id,
		@JsonProperty("rememberMe") final boolean rememberMe,
		@JsonProperty("duration") final long duration,
		@JsonProperty("admin") final AdminValue admin,
		@JsonProperty("person") final PeopleValue person,
		@JsonProperty("registration") final StartRequest registration,
		@JsonProperty("expiresAt") final Date expiresAt,
		@JsonProperty("lastAccessedAt") final Date lastAccessedAt,
		@JsonProperty("createdAt") final Date createdAt)
	{
		this.id = id;
		this.rememberMe = rememberMe;
		this.duration = duration;
		this.admin = admin;
		this.person = person;
		this.registration = registration;
		this.expiresAt = expiresAt;
		this.lastAccessedAt = lastAccessedAt;
		this.createdAt = createdAt;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof SessionValue)) return false;

		var v = (SessionValue) o;
		return Objects.equals(id, v.id) &&
			(rememberMe == v.rememberMe) &&
			(duration == v.duration) && 
			Objects.equals(admin, v.admin) &&
			Objects.equals(person, v.person) &&
			Objects.equals(registration, v.registration) &&
			DateUtils.truncatedEquals(expiresAt, v.expiresAt, Calendar.MINUTE) &&
			DateUtils.truncatedEquals(lastAccessedAt, v.lastAccessedAt, Calendar.MINUTE) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.MINUTE);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", rememberMe: ").append(rememberMe)
			.append(", duration: ").append(duration)
			.append(", admin: ").append(admin)
			.append(", person: ").append(person)
			.append(", registration: ").append(registration)
			.append(", expiresAt: ").append(expiresAt)
			.append(", lastAccessedAt: ").append(lastAccessedAt)
			.append(", createdAt: ").append(createdAt)
			.append(" }").toString();
	}
}
