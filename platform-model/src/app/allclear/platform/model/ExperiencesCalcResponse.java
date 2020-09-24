package app.allclear.platform.model;

import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.platform.type.Experience;

/** Value object that represents output from an Experiences summary of feedback.
 * 
 * @author smalleyd
 * @version 1.1.85
 * @since 6/9/2020
 *
 */

public class ExperiencesCalcResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static class Tag implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public final String name;
		public final long count;
		public final Date last;

		public Tag(@JsonProperty("name") final String name,
			@JsonProperty("count") final long count,
			@JsonProperty("last") final Date last)
		{
			this.last = last;
			this.name = name;
			this.count = count;
		}
	}

	private static final Map<String, Tag> NO_TAGS = Experience.LIST.stream().collect(toMap(v -> v.id, v -> new Tag(v.name, 0L, null)));

	public final long total;
	public final long positives;
	public final long negatives;
	public final Map<String, Tag> tags;

	public ExperiencesCalcResponse(@JsonProperty("total") final long total,
		@JsonProperty("positives") final long positives,
		@JsonProperty("negatives") final long negatives,
		@JsonProperty("tags") final Map<String, Tag> tags)
	{
		this.tags = tags;
		this.total = total;
		this.positives = positives;
		this.negatives = negatives;
	}

	public ExperiencesCalcResponse(final long positives, final long negatives, final Map<String, Tag> tags)
	{
		this(positives + negatives, positives, negatives, tags);
	}

	public ExperiencesCalcResponse()
	{
		this(0L, 0L, 0L, NO_TAGS);
	}
}
