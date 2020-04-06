package app.allclear.google.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the Google viewport.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public class Geometry implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final LatLng location;
	@JsonProperty("location_type") public final String locationType;
	public final Viewport viewport;

	public Geometry(@JsonProperty("location") final LatLng location,
		@JsonProperty("location_type") final String locationType,
		@JsonProperty("viewport") final Viewport viewport)
	{
		this.location = location;
		this.locationType = locationType;
		this.viewport = viewport;
	}
}
