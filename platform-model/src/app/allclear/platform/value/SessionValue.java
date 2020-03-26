package app.allclear.platform.value;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

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

	// Members
	public final String id;
	public final boolean rememberMe;
	public final long duration;	// Millseconds
	public final PeopleValue person;
	public final StartRequest registration;
	public final Date expiresAt;
	public final Date lastAccessedAt;
	public final Date createdAt;

	// Accessors
	public int seconds() { return (int) (duration / 1000L); }

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
			person,
			null,
			new Date(now.getTime() + duration),
			now,
			createdAt);
	}

	public SessionValue(final StartRequest registration)
	{
		this(
			false,
			DURATION_SHORT,
			null,
			registration,
			new Date());
	}

	public SessionValue(final boolean rememberMe, final PeopleValue person)
	{
		this(rememberMe,
			rememberMe ? DURATION_LONG : DURATION_SHORT,
			person,
			null,
			new Date());
	}

	public SessionValue(final boolean rememberMe,
		final long duration,
		final PeopleValue person,
		final StartRequest registration,
		final Date createdAt)
	{
		this(UUID.randomUUID().toString(),
			rememberMe,
			duration,
			person,
			registration,
			new Date(createdAt.getTime() + duration),
			createdAt,
			createdAt);
	}

	public SessionValue(@JsonProperty("id") final String id,
		@JsonProperty("rememberMe") final boolean rememberMe,
		@JsonProperty("duration") final long duration,
		@JsonProperty("person") final PeopleValue person,
		@JsonProperty("registration") final StartRequest registration,
		@JsonProperty("expiresAt") final Date expiresAt,
		@JsonProperty("lastAccessedAt") final Date lastAccessedAt,
		@JsonProperty("createdAt") final Date createdAt)
	{
		this.id = id;
		this.rememberMe = rememberMe;
		this.duration = duration;
		this.person = person;
		this.registration = registration;
		this.expiresAt = expiresAt;
		this.lastAccessedAt = lastAccessedAt;
		this.createdAt = createdAt;
	}
}
