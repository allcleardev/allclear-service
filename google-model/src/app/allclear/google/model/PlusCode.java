package app.allclear.google.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the Google address component.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public class PlusCode implements Serializable
{
	private static final long serialVersionUID = 1L;

	@JsonProperty("compound_code") public final String compoundCode;
	@JsonProperty("global_code") public final String globalCode;

	public PlusCode(@JsonProperty("compound_code") final String compoundCode,
		@JsonProperty("global_code") final String globalCode)
	{
		this.compoundCode = compoundCode;
		this.globalCode = globalCode;
	}
}
