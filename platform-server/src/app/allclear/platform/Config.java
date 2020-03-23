package app.allclear.platform;

import java.io.Serializable;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the application configuration properties.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class Config extends Configuration implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String env;
	public final DataSourceFactory trans;

	public Config(@JsonProperty("env") final String env,
		@JsonProperty("trans") final DataSourceFactory trans)
	{
		this.env = env;
		this.trans = trans;
	}
}
