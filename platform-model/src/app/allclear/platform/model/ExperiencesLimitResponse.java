package app.allclear.platform.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents the limits reached by a user for their Experiences.
 * 
 * @author smalleyd
 * @version 1.1.153
 * @since 12/5/2020
 *
 */

public class ExperiencesLimitResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final long total;
	public final long byFacility;

	public ExperiencesLimitResponse(@JsonProperty("total") final long total,
		@JsonProperty("byFacility") final long byFacility)
	{
		this.total = total;
		this.byFacility = byFacility;
	}

	public ExperiencesLimitResponse(final long[] values)
	{
		total = values[0];
		byFacility = values[1];
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof ExperiencesLimitResponse)) return false;

		var v = (ExperiencesLimitResponse) o;
		return ((total == v.total) && (byFacility == v.byFacility));
	}

	@Override
	public int hashCode() { return Objects.hash(total, byFacility); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
