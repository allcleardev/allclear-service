 package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Represents the statuses that can be associated with Crowdsourcing actions.
 * 
 * @author smalleyd
 * @version 1.1.59
 * @since 5/21/2020
 *
 */

public class CrowdsourceStatus implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final CrowdsourceStatus OPEN = new CrowdsourceStatus("o", "Open");
	public static final CrowdsourceStatus PROMOTED = new CrowdsourceStatus("p", "Promoted");
	public static final CrowdsourceStatus REJECTED = new CrowdsourceStatus("r", "Rejected");

	public static final List<CrowdsourceStatus> LIST = List.of(OPEN, PROMOTED, REJECTED);
	public static final Map<String, CrowdsourceStatus> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static CrowdsourceStatus get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;

	public CrowdsourceStatus(@JsonProperty("id") final String id,
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
		if (!(o instanceof CrowdsourceStatus)) return false;

		var v = (CrowdsourceStatus) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}

	@Override
	public int hashCode() { return id.hashCode(); }
}
