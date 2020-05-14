package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;
import app.allclear.common.value.CreatedValue;

/** Represents the statuses that be associated with People.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class Exposure implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final Exposure CLOSE_CONTACT = new Exposure("cc", "Close contact with someone");
	public static final Exposure LIVE_WITH = new Exposure("lw", "Live with someone");
	public static final Exposure NO_EXPOSURE = new Exposure("ne", "No Exposure");
	public static final Exposure UNSURE = new Exposure("us", "Unsure");

	public static final List<Exposure> LIST = List.of(LIVE_WITH, CLOSE_CONTACT, UNSURE, NO_EXPOSURE);
	public static final Map<String, Exposure> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static Exposure get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;

	public Exposure(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name)
	{
		this.id = id;
		this.name = name;
	}

	public CreatedValue created() { return new CreatedValue(id, name, null); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Exposure)) return false;

		var v = (Exposure) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}

	@Override
	public int hashCode() { return id.hashCode(); }
}
