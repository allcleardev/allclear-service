package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Represents the types of Originators/Actors for an operation.
 * 
 * @author smalleyd
 * @version 1.1.59
 * @since 5/21/2020
 *
 */

public class Originator implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final Originator CITIZEN = new Originator("c", "Citizen");
	public static final Originator PROVIDER = new Originator("p", "Provider");

	public static final List<Originator> LIST = List.of(CITIZEN, PROVIDER);
	public static final Map<String, Originator> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static Originator get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;

	public Originator(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name)
	{
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Originator)) return false;

		var v = (Originator) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}

	@Override
	public int hashCode() { return id.hashCode(); }
}
