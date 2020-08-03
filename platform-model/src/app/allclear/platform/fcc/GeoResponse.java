package app.allclear.platform.fcc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the GEO response from the FCC API.
 * 
 * @author smalleyd
 * @version 1.1.113
 * @since 8/2/2020
 *
 */

public class GeoResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String STATUS_OK = "OK";

	public static class Block implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@JsonProperty("FIPS") public final String fips;
		@JsonProperty("bbox") public final BigDecimal[] bbox;

		public Block(@JsonProperty("FIPS") final String fips,
			@JsonProperty("bbox") final BigDecimal[] bbox)
		{
			this.fips = fips;
			this.bbox = bbox;
		}

		@Override
		public boolean equals(final Object o)
		{
			if (!(o instanceof Block)) return false;

			var v = (Block) o;
			return Objects.equals(fips, v.fips) && Arrays.equals(bbox, v.bbox);
		}
	}

	public static class County implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@JsonProperty("FIPS") public final String fips;
		@JsonProperty("name") public final String name;

		public County(@JsonProperty("FIPS") final String fips,
			@JsonProperty("name") final String name)
		{
			this.fips = fips;
			this.name = name;
		}

		@Override
		public boolean equals(final Object o)
		{
			if (!(o instanceof County)) return false;

			var v = (County) o;
			return Objects.equals(fips, v.fips) && Objects.equals(name, v.name);
		}
	}

	public static class State implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@JsonProperty("FIPS") public final String fips;
		@JsonProperty("code") public final String code;
		@JsonProperty("name") public final String name;

		public State(@JsonProperty("FIPS") final String fips,
			@JsonProperty("code") final String code,
			@JsonProperty("name") final String name)
		{
			this.fips = fips;
			this.code = code;
			this.name = name;
		}

		@Override
		public boolean equals(final Object o)
		{
			if (!(o instanceof State)) return false;

			var v = (State) o;
			return Objects.equals(fips, v.fips) && Objects.equals(code, v.code) && Objects.equals(name, v.name);
		}
	}

	@JsonProperty("Block") public final Block block;
	@JsonProperty("County") public final County county;
	@JsonProperty("State") public final State state;
	@JsonProperty("status") public final String status;
	@JsonProperty("messages") public final List<String> messages;
	@JsonProperty("executionTime") public final String executionTime;

	public boolean ok() { return STATUS_OK.equals(status); }
	public String message()
	{
		return CollectionUtils.isEmpty(messages) ? null : messages.stream().collect(Collectors.joining("\n"));
	}

	public GeoResponse(@JsonProperty("Block") final Block block,
		@JsonProperty("County") final County county,
		@JsonProperty("State") final State state,
		@JsonProperty("status") final String status,
		@JsonProperty("messages") final List<String> messages,
		@JsonProperty("executionTime") final String executionTime)
	{
		this.block = block;
		this.county = county;
		this.state = state;
		this.status = status;
		this.messages = messages;
		this.executionTime = executionTime;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof GeoResponse)) return false;

		var v = (GeoResponse) o;

		return Objects.equals(block, v.block) &&
		       Objects.equals(county, v.county) &&
		       Objects.equals(state, v.state) &&
		       Objects.equals(status, v.status) &&
		       Objects.equals(messages, v.messages) && 
		       Objects.equals(executionTime, v.executionTime);
	}
}
