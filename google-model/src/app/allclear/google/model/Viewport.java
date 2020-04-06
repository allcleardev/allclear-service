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

public class Viewport implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final LatLng northeast;
	public final LatLng southwest;

	public Viewport(@JsonProperty("northeast") final LatLng northeast,
		@JsonProperty("southwest") final LatLng southwest)
	{
		this.northeast = northeast;
		this.southwest = southwest;
	}
}
