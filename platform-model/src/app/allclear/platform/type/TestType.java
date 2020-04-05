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

public class TestType implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final TestType FIRST = new TestType("fs", "First");
	public static final TestType SECOND = new TestType("sd", "Second");

	public static final List<TestType> LIST = List.of(FIRST, SECOND);
	public static final Map<String, TestType> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static TestType get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String name;

	public TestType(@JsonProperty("id") final String id,
		@JsonProperty("name") final String name)
	{
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(" }").toString();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof TestType)) return false;

		var v = (TestType) o;
		return Objects.equals(id, v.id) && Objects.equals(name, v.name); 
	}
}
