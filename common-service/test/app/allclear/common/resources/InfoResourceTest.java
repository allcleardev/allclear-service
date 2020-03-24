package app.allclear.common.resources;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.Configuration;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.HealthResponse;

/** Functional test class that verifies the Info REST resource.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

@ExtendWith(DropwizardExtensionsSupport.class)
public class InfoResourceTest
{
	private static final HealthCheckRegistry hcr = new HealthCheckRegistry();
	private static final List<String> pings = List.of("pingable");
	private static final HealthCheck firstCheck = mock(HealthCheck.class);
	private static final HealthCheck secondCheck = mock(HealthCheck.class);
	private static final HealthCheck pingableCheck = mock(HealthCheck.class);

	private static final GenericType<Map<String, Object>> TYPE_MAP = new GenericType<Map<String, Object>>() {};

	public static final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new InfoResource(new Configuration(), hcr, pings, "4.0.5"))
		.build();

	private static final String TARGET = "/info";

	@BeforeAll
	public static void up()
	{
		hcr.register("first", firstCheck);
		hcr.register("second", secondCheck);
		hcr.register("pingable", pingableCheck);
	}

	@BeforeEach
	public void before()
	{
		when(firstCheck.execute()).thenReturn(HealthCheck.Result.healthy());
		when(secondCheck.execute()).thenReturn(HealthCheck.Result.healthy());
		when(pingableCheck.execute()).thenReturn(HealthCheck.Result.healthy());
	}

	@Test
	public void config()
	{
		var value = request("config").get(TYPE_MAP);

		Assert.assertNotNull("Exists", value);
		Assert.assertTrue("Check server", value.containsKey("server"));
		Assert.assertTrue("Check logging", value.containsKey("logging"));
		Assert.assertTrue("Check metrics", value.containsKey("metrics"));
		Assert.assertFalse("Check INVALID", value.containsKey("INVALID"));
	}

	@Test
	public void health_success()
	{
		var value = request("health").get(HealthResponse.class);

		Assert.assertNotNull("Exists", value);
		assertThat(value.timestamp).isCloseTo(new Date(), 1000L).as("Check timestamp");
		Assert.assertEquals("Check status", "success", value.status);
		Assert.assertNull("message", value.message);
	}

	@Test
	public void health_failure()
	{
		when(secondCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("Bad Error"));
		var value = request("health").get(HealthResponse.class);

		Assert.assertNotNull("Exists", value);
		assertThat(value.timestamp).isCloseTo(new Date(), 1000L).as("Check timestamp");
		Assert.assertEquals("Check status", "fail", value.status);
		Assert.assertNotNull("message Exists", value.message);
		Assert.assertEquals("Check message", "[second: Bad Error: null]", value.message);
	}

	@Test
	public void health_multi_failure()
	{
		when(secondCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("Bad Error"));
		when(pingableCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("Error"));
		var value = request("health").get(HealthResponse.class);

		Assert.assertNotNull("Exists", value);
		assertThat(value.timestamp).isCloseTo(new Date(), 1000L).as("Check timestamp");
		Assert.assertEquals("Check status", "fail", value.status);
		Assert.assertNotNull("message Exists", value.message);
		Assert.assertEquals("Check message", "[pingable: Error: null, second: Bad Error: null]", value.message);
	}

	@Test
	public void ping_success()
	{
		Assert.assertEquals(200, request("ping").get().getStatus());
	}

	@Test
	public void ping_failure()
	{
		when(pingableCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("Error"));
		Assert.assertEquals(503, request("ping").get().getStatus());
	}

	@Test
	public void version()
	{
		var value = request("version").get(TYPE_MAP);
		Assert.assertNotNull("Exists", value);

		var version = (String) value.get("version");
		Assert.assertEquals("Check version", "4.0.5", version);
	}

	private WebTarget target() { return RULE.client().target(TARGET); }
	private Invocation.Builder request(final String path) { return target().path(path).request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
