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

public class Visibility implements Serializable, Comparable<Visibility>
{
	private static final long serialVersionUID = 1L;

	public static final Visibility ALL = new Visibility("a", "All", 100);
	public static final Visibility FRIENDS = new Visibility("f", "Friends", 200);
	public static final Visibility ME = new Visibility("m", "Me", 300);

	public static final Visibility DEFAULT = FRIENDS;

	public static final List<Visibility> LIST = List.of(ALL, FRIENDS, ME);
	public static final Map<String, Visibility> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static Visibility get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;
	public final int rank;

	public boolean available(final Visibility who)
	{
		return (0 >= compareTo(who));
	}

	@Override
	public int compareTo(final Visibility other) { return rank - other.rank; }

	public Visibility(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name,
		@JsonProperty("rank") final int rank)
	{
		this.id = id;
		this.name = name;
		this.rank = rank;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", rank: ").append(rank)
			.append(" }").toString();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Visibility)) return false;

		var v = (Visibility) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name) && (rank == v.rank); 
	}

	@Override
	public int hashCode() { return id.hashCode(); }
}
