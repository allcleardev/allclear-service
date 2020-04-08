package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents the statuses that be associated with People.
 * 
 * @author smalleyd
 * @version 1.0.53
 * @since 4/5/2020
 *
 */

public class HealthWorkerStatus implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final HealthWorkerStatus HEALTH_WORKER= new HealthWorkerStatus("h", "I am a Health Worker or First Responder", true);
	public static final HealthWorkerStatus LIVE_WITH = new HealthWorkerStatus("l", "I live with Health Worker or First Responder", false);

	public static final List<HealthWorkerStatus> LIST = List.of(HEALTH_WORKER, LIVE_WITH);
	public static final Map<String, HealthWorkerStatus> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static HealthWorkerStatus get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;
	public final boolean staff;	// Indicates that the status represents an actual staff member as opposed to a family member.

	public HealthWorkerStatus(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name,
		@JsonProperty("staff") final boolean staff)
	{
		this.id = id;
		this.name = name;
		this.staff = staff;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(" }").toString();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof HealthWorkerStatus)) return false;

		var v = (HealthWorkerStatus) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}
}
