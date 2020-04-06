package app.allclear.google.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the Google latitude/longitude point.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public class LatLng implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final BigDecimal lat;
	public final BigDecimal lng;

	public LatLng(@JsonProperty("lat") final BigDecimal lat,
		@JsonProperty("lng") final BigDecimal lng)
	{
		this.lat = lat;
		this.lng = lng;
	}
}
