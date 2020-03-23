package app.allclear.common;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

/** Simple DropWizard configuration for testing substitution.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class SimpleConfiguration extends Configuration
{
	public DataSourceFactory dataSource;
}
