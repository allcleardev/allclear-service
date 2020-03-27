package app.allclear.common;

import io.dropwizard.lifecycle.Managed;

/** Wrapper class that converts a "Closeable" resource into a Dropwizard Managed resource.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/27/2020
 *
 */

public class AutoCloseableManager implements Managed
{
	public final AutoCloseable resource;

	public AutoCloseableManager(final AutoCloseable resource)
	{
		this.resource = resource;
	}

	@Override
	public void start() {}	// Already started when resource is constructed.

	@Override
	public void stop() throws Exception { resource.close(); }
}
