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

public class Symptom implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final Symptom DIARRHEA = new Symptom("dh", "Diarrhea");
	public static final Symptom DRY_COUGH = new Symptom("dc", "Dry Cough");
	public static final Symptom FATIGUE = new Symptom("fg", "Fatigue");
	public static final Symptom FEVER = new Symptom("fv", "Fever");
	public static final Symptom MUSCLE_ACHE = new Symptom("ma", "Muscle ache");
	public static final Symptom NAUSEA_VOMITING = new Symptom("nv", "Nausea and/or vomiting");
	public static final Symptom RUNNY_NOSE = new Symptom("rn", "Runny Nose/Nasal Congestion");
	public static final Symptom SHORTNESS_OF_BREATH = new Symptom("sb", "Shortness of Breath");
	public static final Symptom SORE_THROAT = new Symptom("st", "Sore Throat");

	public static final List<Symptom> LIST = List.of(DIARRHEA, DRY_COUGH, FATIGUE, FEVER, MUSCLE_ACHE, NAUSEA_VOMITING, RUNNY_NOSE, SHORTNESS_OF_BREATH, SORE_THROAT);
	public static final Map<String, Symptom> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));

	public final String id;
	public final String name;

	public Symptom(@JsonProperty("id") final String id,
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
		if (!(o instanceof Symptom)) return false;

		var v = (Symptom) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}
}
