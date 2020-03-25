package app.allclear.platform.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the request to start the registration process.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class StartRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String phone;
	public final boolean beenTested;
	public final boolean haveSymptoms;

	public StartRequest(@JsonProperty("phone") final String phone,
		@JsonProperty("beenTested") final Boolean beenTested,
		@JsonProperty("haveSymptoms") final Boolean haveSymptoms)
	{
		this.phone = phone;
		this.beenTested = Boolean.TRUE.equals(beenTested);
		this.haveSymptoms = Boolean.TRUE.equals(haveSymptoms);
	}
}
