package app.allclear.common.resources;

import static app.allclear.testing.TestingUtils.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/** Functional test class that verifies the HeapDumpResource class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

@ExtendWith(DropwizardExtensionsSupport.class)
public class HeapDumpResourceTest
{
	private static final String TARGET = "/heap";

	public static final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new HeapDumpResource())
		.build();

	@Test
	public void testGet()
	{
		var response = target().request(MediaType.APPLICATION_OCTET_STREAM_TYPE).get();

		Assert.assertEquals("Status", HTTP_STATUS_OK, response.getStatus());
	}

	@Test
	public void testGet_unlive()
	{
		var response = target().queryParam("live", "false").request(MediaType.APPLICATION_OCTET_STREAM_TYPE).get();

		Assert.assertEquals("Status", HTTP_STATUS_OK, response.getStatus());
	}

	@Test
	public void testGetHistogram()
	{
		var response = target().path("histogram").request(MediaType.TEXT_PLAIN).get();

		Assert.assertEquals("Status", HTTP_STATUS_OK, response.getStatus());
	}

	private WebTarget target()
	{
		return RULE.client().target(TARGET);
	}
}
