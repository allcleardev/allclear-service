package app.allclear.platform.filter;

import static app.allclear.common.value.Constants.*;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents GEO radius search filter.
 * 
 * @author smalleyd
 * @version 1.0.33
 * @since 4/3/2020
 *
 */

public class GeoFilter implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final BigDecimal latitude;
	public final BigDecimal longitude;
	public final Integer miles;
	public final Integer km;

	public GeoFilter(@JsonProperty("latitude") final BigDecimal latitude,
		@JsonProperty("longitude") final BigDecimal longitude,
		@JsonProperty("miles") final Integer miles,
		@JsonProperty("km") final Integer km)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.miles = miles;
		this.km = km;
	}

	public long meters() { return ((null != miles) ? milesToMeters(miles) : kmToMeters(km)); }
	public boolean valid()
	{
		return ((null != latitude) && (null != longitude) && ((null != miles) || (null != km)));
	}
}
