package app.allclear.google.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the Google address component.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public class AddressComponent implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static AddressComponent EMPTY = new AddressComponent(null, null, null);

	@JsonProperty("long_name") public final String longName;
	@JsonProperty("short_name") public final String shortName;
	@JsonProperty("types") public final List<String> types;

	public boolean streetNumber() { return types.contains("street_number"); }
	public boolean streetName() { return types.contains("route"); }
	public boolean city() { return types.contains("locality"); }
	public boolean county() { return types.contains("administrative_area_level_2"); }
	public boolean state() { return types.contains("administrative_area_level_1"); }
	public boolean country() { return types.contains("country"); }
	public boolean postalCode() { return types.contains("postal_code"); }

	public AddressComponent(@JsonProperty("long_name") final String longName,
		@JsonProperty("short_name") final String shortName,
		@JsonProperty("types") final List<String> types)
	{
		this.longName = longName;
		this.shortName = shortName;
		this.types = null != types ? types : List.of();
	}
}
