package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents the statuses that be associated with People.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class PeopleStature implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final PeopleStature CELEBRITY = new PeopleStature("c", "Celebrity");
	public static final PeopleStature INFLUENCER = new PeopleStature("i", "Influencer");

	public static final List<PeopleStature> LIST = List.of(CELEBRITY, INFLUENCER);
	public static final Map<String, PeopleStature> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));

	public final String id;
	public final String name;

	public PeopleStature(@JsonProperty("id") final String id,
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

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof PeopleStature)) return false;

		var v = (PeopleStature) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}
}
