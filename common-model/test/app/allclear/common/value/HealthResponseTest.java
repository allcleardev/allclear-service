package app.allclear.common.value;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.*;

import app.allclear.testing.TestingUtils;

/** Unit test class that verifies the HealthResponse POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class HealthResponseTest
{
	private static Date TIMESTAMP;

	@BeforeClass
	public static void up() throws Exception
	{
		TIMESTAMP = TestingUtils.timestamp("2019-06-24T15:48:30-0000");
	}

	@Test
	public void create()
	{
		var v = new HealthResponse(TIMESTAMP, "Node 1", "Status 1", "Message 1", "Debug 1");

		Assert.assertEquals(TIMESTAMP, v.timestamp);
		Assert.assertEquals("Node 1", v.node);
		Assert.assertEquals("Status 1", v.status);
		Assert.assertEquals("Message 1", v.message);
		Assert.assertEquals("Debug 1", v.debug);
	}

	@Test
	public void create_error()
	{
		var v = new HealthResponse("Node 1", "Error 1");

		Assert.assertNotEquals(TIMESTAMP, v.timestamp);
		assertThat(v.timestamp).isCloseTo(new Date(), 500L);
		Assert.assertEquals("Node 1", v.node);
		Assert.assertEquals("fail", v.status);
		Assert.assertEquals("Error 1", v.message);
		Assert.assertNull(v.debug);
	}

	@Test
	public void create_node()
	{
		var v = new HealthResponse("Node 1");

		Assert.assertNotEquals(TIMESTAMP, v.timestamp);
		assertThat(v.timestamp).isCloseTo(new Date(), 500L);
		Assert.assertEquals("Node 1", v.node);
		Assert.assertEquals("success", v.status);
		Assert.assertNull(v.message);
		Assert.assertNull(v.debug);
	}

	@Test
	public void create_status()
	{
		var v = new HealthResponse("Node 1", "Status 1", "Message 1");

		Assert.assertNotEquals(TIMESTAMP, v.timestamp);
		assertThat(v.timestamp).isCloseTo(new Date(), 500L);
		Assert.assertEquals("Node 1", v.node);
		Assert.assertEquals("Status 1", v.status);
		Assert.assertEquals("Message 1", v.message);
		Assert.assertNull(v.debug);
	}
}
