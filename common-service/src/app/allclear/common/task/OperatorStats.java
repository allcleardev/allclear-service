package app.allclear.common.task;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents statistics for a TaskOperator.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class OperatorStats implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String name;
	public final int queueSize;
	public final int dlqSize;
	public final int successes;
	public final int skips;
	public final int errors;

	public OperatorStats(@JsonProperty("name") final String name,
		@JsonProperty("queueSize") final int queueSize,
		@JsonProperty("dlqSize") final int dlqSize,
		@JsonProperty("successes") final int successes,
		@JsonProperty("skips") final int skips,
		@JsonProperty("errors") final int errors)
	{
		this.name = name;
		this.queueSize = queueSize;
		this.dlqSize = dlqSize;
		this.successes = successes;
		this.skips = skips;
		this.errors = errors;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ name: '").append(name)
			.append("', queueSize: ").append(queueSize)
			.append(", dlqSize: ").append(dlqSize)
			.append(", successes: ").append(successes)
			.append(", skips: ").append(skips)
			.append(", errors: ").append(errors)
			.append(" }").toString();
	}
}
