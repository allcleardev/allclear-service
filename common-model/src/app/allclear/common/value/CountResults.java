package app.allclear.common.value;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the number of values processed by an endpoint.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class CountResults implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final int count;
	public final List<OperationResponse> responses;

	public long toLong() { return (long) count; }

	public CountResults(@JsonProperty("count") final int count,
		@JsonProperty("responses") final List<OperationResponse> responses)
	{
		this.count = count;
		this.responses = responses;
	}

	public CountResults(final int count) { this(count, null); }
	public CountResults(long count) { this((int) count); }
	public CountResults(final List<OperationResponse> responses)
	{
		this((null != responses) ? responses.size() : 0, responses);
	}
}
