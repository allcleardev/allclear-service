package app.allclear.common.hibernate;

import java.io.Serializable;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a Dropwizard test configuration.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class DropwizardConfig extends Configuration implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final DataSourceFactory dataSource;

	public DropwizardConfig(@JsonProperty("dataSource") final DataSourceFactory dataSource)
	{
		this.dataSource = dataSource;
	}
}
