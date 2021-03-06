package app.allclear.platform.model;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents a request to start the Facility Alert task.
 * 
 * @author smalleyd
 * @version 1.0.109
 * @since 4/14/2020
 *
 */

public class AlertInitRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String timezoneId;

	public AlertInitRequest(@JsonProperty("timezoneId") final String timezoneId)
	{
		this.timezoneId = StringUtils.trimToNull(timezoneId);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof AlertInitRequest)) return false;

		var v = (AlertInitRequest) o;
		return Objects.equals(timezoneId, v.timezoneId);
	}

	@Override
	public int hashCode() { return Objects.hashCode(timezoneId); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
