package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Represents the type of facility.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class FacilityType implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final FacilityType COMMUNITY_HEALTH_CLINIC = new FacilityType("ch", "Community Health Clinic");
	public static final FacilityType HOSPITAL = new FacilityType("ho", "Hospital");
	public static final FacilityType MEDICAL_CENTER = new FacilityType("mc", "Medical Center");
	public static final FacilityType POP_UP_MOBILE_SITE = new FacilityType("pu", "Pop-up/Mobile Site");
	public static final FacilityType PRIVATE_DOCTORS_OFFICE = new FacilityType("pv", "Private Doctor's Office");	// ALLCLEAR-574: DLS on 5/24/2020.
	public static final FacilityType PUBLIC_HEALTH_DEPT = new FacilityType("pd", "Public Health Department");
	public static final FacilityType URGENT_CARE = new FacilityType("uc", "Urgent Care");

	public static final List<FacilityType> LIST = List.of(COMMUNITY_HEALTH_CLINIC, HOSPITAL, MEDICAL_CENTER, POP_UP_MOBILE_SITE, PRIVATE_DOCTORS_OFFICE, PUBLIC_HEALTH_DEPT, URGENT_CARE);
	public static final Map<String, FacilityType> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static FacilityType get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;

	public FacilityType(@JsonProperty("id") final String id,
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
		if (!(o instanceof FacilityType)) return false;

		var v = (FacilityType) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}

	@Override
	public int hashCode() { return id.hashCode(); }
}
