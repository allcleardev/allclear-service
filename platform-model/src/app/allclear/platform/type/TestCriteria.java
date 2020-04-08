package app.allclear.platform.type;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents the statuses that be associated with People.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TestCriteria implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final TestCriteria CDC_CRITERIA = new TestCriteria("cc", "CDC Criteria", true);
	public static final TestCriteria OTHER = new TestCriteria("ot", "Other", false);

	public static final List<TestCriteria> LIST = List.of(CDC_CRITERIA, OTHER);
	public static final Map<String, TestCriteria> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static TestCriteria get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;
	public final boolean restricted;

	public TestCriteria(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name,
		@JsonProperty("restricted") final boolean restricted)
	{
		this.id = id;
		this.name = name;
		this.restricted = restricted;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", restricted: ").append(restricted)
			.append(" }").toString();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof TestCriteria)) return false;

		var v = (TestCriteria) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name) && (restricted == v.restricted); 
	}
}
