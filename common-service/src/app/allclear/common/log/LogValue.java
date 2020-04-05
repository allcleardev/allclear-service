package app.allclear.common.log;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the application log details.
 * 
 * @author smalleyd
 * @version 1.0.45
 * @since 4/5/2020
 *
 */

public class LogValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String id;
	public final String name;
	public final String level;

	public LogValue(@JsonProperty("name") String name, @JsonProperty("level") String level)
	{
		this.id = name;
		this.name = name;
		this.level = level;
	}
}
