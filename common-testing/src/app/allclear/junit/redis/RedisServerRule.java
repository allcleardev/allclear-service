package app.allclear.junit.redis;

import org.junit.rules.ExternalResource;

import io.dropwizard.testing.junit5.DropwizardExtension;

import redis.embedded.RedisServer;

/** JUnit external resource that manages the starting and stopping of an embedded Redis server.
 * 
 * @author smalleyd
 * @version 3.0.69
 * @since 6/12/2015
 *
 */

public class RedisServerRule extends ExternalResource implements DropwizardExtension
{
	/** Default/empty. */
	public RedisServerRule()
	{
		this(6378);	// Do NOT use 6379 in case a local version is running. DLS on 6/12/2015.
	}

	/** Populator.
	 * 
	 * @param port
	 */
	public RedisServerRule(int port)
	{
		this.port = port;
	}

	/** Represents the server port. */
	private int port = 6379;

	/** Represents the embedded server. */
	private RedisServer server = null;

	@Override
	public void before() throws Exception
	{
		server = new RedisServer(port);
		server.start();
	}

	@Override
	public void after()
	{
		server.stop();
	}
}
