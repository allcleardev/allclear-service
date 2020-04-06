package app.allclear.google.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the response from the Google Geocoding API.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public class GeocodeResponse extends MapResponse
{
	private static final long serialVersionUID = 1L;

	public final List<GeocodeResult> results;

	public GeocodeResponse(@JsonProperty("status") final String status,
		@JsonProperty("results") final List<GeocodeResult> results,
		@JsonProperty("error_message") final String errorMessage)
	{
		super(status, errorMessage);
		this.results = results;
	}
}
