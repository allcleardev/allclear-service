package app.allclear.platform.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents a request to run the Facility Alert task for the specific user.
 * 
 * @author smalleyd
 * @version 1.0.109
 * @since 4/14/2020
 *
 */

public class AlertRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String personId;

	public AlertRequest(@JsonProperty("personId") final String personId)
	{
		this.personId = personId;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof AlertRequest)) return false;

		var v = (AlertRequest) o;
		return Objects.equals(personId, v.personId);
	}

	@Override
	public int hashCode() { return Objects.hashCode(personId); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
