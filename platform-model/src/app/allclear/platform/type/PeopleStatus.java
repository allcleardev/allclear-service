package app.allclear.platform.type;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents the statuses that be associated with People.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class PeopleStatus implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final PeopleStatus HEALTHY = new PeopleStatus("h", "Healthy");
	public static final PeopleStatus INFECTED = new PeopleStatus("i", "Infected");
	public static final PeopleStatus RECOVERED = new PeopleStatus("r", "Recovered");
	public static final PeopleStatus SYMPTOMATIC = new PeopleStatus("s", "Symptomatic");

	public static final Map<String, PeopleStatus> VALUES = Map.of(HEALTHY.id, HEALTHY, INFECTED.id, INFECTED, RECOVERED.id, RECOVERED, SYMPTOMATIC.id, SYMPTOMATIC);

	public final String id;
	public final String name;

	public PeopleStatus(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name)
	{
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(" }").toString();
	}
}
