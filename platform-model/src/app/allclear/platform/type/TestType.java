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

	public static final TestType RT_PCR = new TestType("rp", "rt-PCR", "Nasal Swab Test");
	public static final TestType IGM_IGR_RAPID_TEST = new TestType("ii", "igM/igG Rapid Test", "Antibody Test");
	public static final TestType DONT_KNOW = new TestType("dk", "Don't Know", "Don't Know");

	public static final List<TestType> LIST = List.of(RT_PCR, IGM_IGR_RAPID_TEST, DONT_KNOW);
	public static final Map<String, TestType> VALUES = LIST.stream().collect(Collectors.toUnmodifiableMap(v -> v.id, v -> v));
	public static TestType get(final String id) { return VALUES.get(id); }
	public static boolean exists(final String id) { return VALUES.containsKey(id); }

	public final String id;
	public final String code;
	public final String name;

	public TestType(@JsonProperty("id") final String id,
		@JsonProperty("code") final String code,
		@JsonProperty("name") final String name)
	{
		this.id = id;
		this.code = code;
		this.name = name;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", code: ").append(code)
			.append(", name: ").append(name)
			.append(" }").toString();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof TestType)) return false;

		var v = (TestType) o;
		return Objects.equals(id, v.id) && Objects.equals(code, v.code) && Objects.equals(name, v.name); 
	}

	@Override
	public int hashCode() { return id.hashCode(); }
}
