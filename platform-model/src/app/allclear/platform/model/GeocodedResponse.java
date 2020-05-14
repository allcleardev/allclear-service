package app.allclear.platform.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents a distilled version of the Google Maps Geocode response.
 * 
 * @author smalleyd
 * @version 1.0.97
 * @since 4/10/2020
 *
 */

public class GeocodedResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String streetNumber;
	public final String streetName;
	public final String city;
	public final String county;
	public final String state;
	public final String country;
	public final String postalCode;
	public final BigDecimal latitude;
	public final BigDecimal longitude;

	public GeocodedResponse(@JsonProperty("streetNumber") final String streetNumber,
		@JsonProperty("streetName") final String streetName,
		@JsonProperty("city") final String city,
		@JsonProperty("county") final String county,
		@JsonProperty("state") final String state,
		@JsonProperty("country") final String country,
		@JsonProperty("postalCode") final String postalCode,
		@JsonProperty("latitude") final BigDecimal latitude,
		@JsonProperty("longitude") final BigDecimal longitude)
	{
		this.streetNumber = streetNumber;
		this.streetName = streetName;
		this.city = city;
		this.county = county;
		this.state = state;
		this.country = country;
		this.postalCode = postalCode;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
