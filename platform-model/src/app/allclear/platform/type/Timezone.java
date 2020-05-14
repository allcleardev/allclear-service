package app.allclear.platform.type;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Represents the support timezones and their longitudinal constraints.
 * 
 * 	@author smalleyd
 *  @version 1.0.108
 *  @since 4/12/2020
 */

public class Timezone implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final Timezone EST = new Timezone("EST", -85, -70);	// Timezones in US don't adhere perfectly to the longitudinal lines exactly.
	public static final Timezone CST = new Timezone("CST", -100, -85);
	public static final Timezone MST = new Timezone("MST", -115, -100);
	public static final Timezone PST = new Timezone("PST", -135, -115);

	public static final List<Timezone> LIST = List.of(EST, CST, MST, PST);
	public static final Map<String, Timezone> VALUES = LIST.stream().collect(Collectors.toMap(v -> v.id, v -> v));
	public static Timezone get(final int i) { return LIST.get(i); }
	public static Timezone get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;
	public final BigDecimal longitudeFrom;
	public final BigDecimal longitudeTo;

	public Timezone(final String id, final int longitudeFrom, final int longitudeTo)
	{
		this(id, TimeZone.getTimeZone(id).getDisplayName(), new BigDecimal(longitudeFrom), new BigDecimal(longitudeTo));
	}

	public Timezone(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name,
		@JsonProperty("longitudeFrom") final BigDecimal longitudeFrom,
		@JsonProperty("longitudeTo") final BigDecimal longitudeTo)
	{
		this.id = id;
		this.name = name;
		this.longitudeFrom = longitudeFrom;
		this.longitudeTo = longitudeTo;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Timezone)) return false;

		var v = (Timezone) o;
		return Objects.equals(id, v.id) &&
			Objects.equals(name, v.name) &&
			Objects.equals(longitudeFrom, v.longitudeFrom) &&
			Objects.equals(longitudeTo, v.longitudeTo);
	}

	@Override
	public int hashCode() { return id.hashCode(); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
