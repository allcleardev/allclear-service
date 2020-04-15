package app.allclear.platform;

import java.io.Serializable;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ManifestUtils;
import app.allclear.common.value.ManifestValue;

/** Value object that represents the application configuration properties.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TaskConfig extends Configuration implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String env;
	public final boolean disableSwagger;
	public final DataSourceFactory read;
	public final ManifestValue manifest;

	public final String baseUrl;

	public String getVersion() { return manifest.version; }

	public TaskConfig(@JsonProperty("env") final String env,
		@JsonProperty("baseUrl") final String baseUrl,
		@JsonProperty("disableSwagger") final Boolean disableSwagger,
		@JsonProperty("read") final DataSourceFactory read)
	{
		this.env = env;
		this.read = read;
		this.baseUrl = baseUrl;
		this.manifest = ManifestUtils.getInfo(getClass());
		this.disableSwagger = Boolean.TRUE.equals(disableSwagger);
	}
}
