package app.allclear.common.value;

import org.junit.*;

/** Unit test class that verifies the OperationResponse POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class OperationResponseTest
{
	@Test
	public void create_fail()
	{
		var value = new OperationResponse(false);

		Assert.assertFalse(value.operation);
		Assert.assertNull(value.message);
		Assert.assertEquals(value, new OperationResponse(false));
	}

	@Test
	public void create_message()
	{
		var value = new OperationResponse("Invalid operation");

		Assert.assertFalse(value.operation);
		Assert.assertEquals("Invalid operation", value.message);
		Assert.assertEquals(value, new OperationResponse("Invalid operation"));
	}

	@Test
	public void create_success()
	{
		var value = new OperationResponse(true);

		Assert.assertTrue(value.operation);
		Assert.assertNull(value.message);
		Assert.assertEquals(value, new OperationResponse(true));
	}
}
