package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.value.CreatedValue;

/** Represents the statuses that be associated with People.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class Condition implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final Condition CARDIO_RESPIRATORY_DISEASE = new Condition("cr", "Cardiovascular/Respiratory Disease");
	public static final Condition DIABETIC = new Condition("db", "Diabetic");
	public static final Condition KIDNEY_CIRRHOSIS = new Condition("kc", "Kidney/Cirrhosis failure");
	public static final Condition NONE = new Condition("no", "None");
	public static final Condition PREGNANT = new Condition("pg", "Pregnant");
	public static final Condition WEAKENED_IMMUNE_SYSTEM = new Condition("wi", "Weakened Immune System");

	public static final List<Condition> LIST = List.of(WEAKENED_IMMUNE_SYSTEM, CARDIO_RESPIRATORY_DISEASE, KIDNEY_CIRRHOSIS, PREGNANT, DIABETIC, NONE);
	public static final Map<String, Condition> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static Condition get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;

	public Condition(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name)
	{
		this.id = id;
		this.name = name;
	}

	public CreatedValue created() { return new CreatedValue(id, name, null); }

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
		if (!(o instanceof Condition)) return false;

		var v = (Condition) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}
}
