package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;
import app.allclear.common.value.NamedValue;

/** Represents the experience options that can be associated with a single person/facility experience.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class Experience implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final Experience SHORT_WAIT_TIME = new Experience("st", "Short Wait Time", true, 1);
	public static final Experience GOOD_HYGIENE = new Experience("gh", "Good Hygiene", true, 2);
	public static final Experience FRIENDLY_STAFF = new Experience("fs", "Friendly Staff", true, 3);
	public static final Experience PPE_PROVIDED = new Experience("pp", "PPE Provided", true, 4);
	public static final Experience SOCIAL_DISTANCING_ENFORCED = new Experience("se", "Social Distancing Enforced", true, 5);
	public static final Experience GOOD_BEDSIDE_MANNER = new Experience("gm", "Good Bedside Manner", true, 6);
	public static final Experience LONG_WAIT_TIME = new Experience("lt", "Long Wait Time", false, 1);
	public static final Experience OVERLY_CROWDED = new Experience("oc", "Overly Crowded", false, 2);
	public static final Experience COULD_NOT_GET_TESTED = new Experience("ct", "Couldn't Get Tested", false, 3);
	public static final Experience POOR_HYGIENE = new Experience("ph", "Poor Hygiene", false, 4);
	public static final Experience SEEMED_UNDERSTAFFED = new Experience("su", "Seemed Understaffed", false, 5);
	public static final Experience CONFUSING_APPOINTMENT_PROCESS = new Experience("ca", "Confusing Appointment Process", false, 6);

	public static final List<Experience> LIST = List.of(SHORT_WAIT_TIME, GOOD_HYGIENE, FRIENDLY_STAFF, PPE_PROVIDED, SOCIAL_DISTANCING_ENFORCED, GOOD_BEDSIDE_MANNER,
		LONG_WAIT_TIME, OVERLY_CROWDED, COULD_NOT_GET_TESTED, POOR_HYGIENE, SEEMED_UNDERSTAFFED, CONFUSING_APPOINTMENT_PROCESS);
	public static final Map<String, Experience> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static Experience get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;
	public final boolean positive;
	public final int ordinal;

	public Experience(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name,
		@JsonProperty("positive") final boolean positive,
		@JsonProperty("ordinal") final int ordinal)
	{
		this.id = id;
		this.name = name;
		this.positive = positive;
		this.ordinal = ordinal;
	}

	public NamedValue named() { return new NamedValue(id, name); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Experience)) return false;

		var v = (Experience) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name) && (positive == v.positive) && (ordinal == v.ordinal); 
	}

	@Override
	public int hashCode() { return id.hashCode(); }
}
