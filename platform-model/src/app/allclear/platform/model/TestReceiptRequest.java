package app.allclear.platform.model;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents the received results of a test.
 * 
 * @author smalleyd
 * @version 1.1.126
 * @since 8/8/2020
 *
 */

public class TestReceiptRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final Long id;
	public final boolean positive;
	public final String notes;

	public TestReceiptRequest(@JsonProperty("id") final Long id,
		@JsonProperty("positive") final Boolean positive,
		@JsonProperty("notes") final String notes)
	{
		this.id = id;
		this.positive = Boolean.TRUE.equals(positive);
		this.notes = StringUtils.trimToNull(notes);
	}

	@Override
	public int hashCode() { return Objects.hashCode(id); }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof TestReceiptRequest)) return false;

		var v = (TestReceiptRequest) o;

		return Objects.equals(id, v.id) &&
			(positive == v.positive) &&
			Objects.equals(notes, v.notes);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
