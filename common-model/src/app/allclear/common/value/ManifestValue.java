package app.allclear.common.value;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Class that represents the values of a JAR manifest.
 * 
 * @author smalleyd
 * @version 2.0.5
 * @since 1/19/2016
 *
 */

public class ManifestValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String vendor;
	public final String title;
	public final String version;

	public ManifestValue(@JsonProperty("vendor") final String vendor,
		@JsonProperty("title") final String title,
		@JsonProperty("version") final String version)
	{
		this.vendor = vendor;
		this.title = title;
		this.version = version;
	}
}
