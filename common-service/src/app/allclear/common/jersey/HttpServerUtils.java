package app.allclear.common.jersey;

import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.*;

/** Utility class that provides helpers for Drop Wizard/Jersey configuration.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class HttpServerUtils
{
	/** Gets the application server port.
	 * 
	 * @param conf
	 * @param defaultPort
	 * @return HTTP port.
	 */
	public static int getPort(final Configuration conf, final int defaultPort)
	{
		var serverFactory = conf.getServerFactory();
		if (serverFactory instanceof SimpleServerFactory)
			return ((HttpConnectorFactory) ((SimpleServerFactory) serverFactory).getConnector()).getPort();

		else if (serverFactory instanceof DefaultServerFactory)
			return ((HttpConnectorFactory) ((DefaultServerFactory) serverFactory).getApplicationConnectors().get(0)).getPort();

		// Use default port.
		return defaultPort;
	}
}
