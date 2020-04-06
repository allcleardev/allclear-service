package app.allclear.google.model;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents a single Google Geocode result entry.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public class GeocodeResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	@JsonProperty("address_components") public final List<AddressComponent> addressComponents;
	@JsonProperty("formatted_address") public final String formattedAddress;
	@JsonProperty("geometry") public final Geometry geometry;
	@JsonProperty("place_id") public final String placeId;
	@JsonProperty("plus_code") public final PlusCode plusCode;
	@JsonProperty("types") public final List<String> types;

	protected AddressComponent addressComponent(final Predicate<AddressComponent> fx) { return addressComponents.stream().filter(fx).findFirst().orElse(null); }
	public AddressComponent streetNumber() { return addressComponent(v -> v.streetNumber()); }
	public AddressComponent streetName() { return addressComponent(v -> v.streetName()); }
	public AddressComponent city() { return addressComponent(v -> v.city()); }
	public AddressComponent county() { return addressComponent(v -> v.county()); }
	public AddressComponent state() { return addressComponent(v -> v.state()); }
	public AddressComponent country() { return addressComponent(v -> v.country()); }
	public AddressComponent postalCode() { return addressComponent(v -> v.postalCode()); }

	public GeocodeResult(@JsonProperty("address_components") final List<AddressComponent> addressComponents,
		@JsonProperty("formatted_address") final String formattedAddress,
		@JsonProperty("geometry") final Geometry geometry,
		@JsonProperty("place_id") final String placeId,
		@JsonProperty("plus_code") final PlusCode plusCode,
		@JsonProperty("types") final List<String> types)
	{
		this.addressComponents = addressComponents;
		this.formattedAddress = formattedAddress;
		this.geometry = geometry;
		this.placeId = placeId;
		this.plusCode = plusCode;
		this.types = types;
	}
}
