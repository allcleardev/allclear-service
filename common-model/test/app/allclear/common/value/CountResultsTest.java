package app.allclear.common.value;

import java.util.Arrays;
import java.util.List;

import org.junit.*;

/** Unit test class that verifies the CountResults POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class CountResultsTest
{
	public static final List<OperationResponse> RESPONSES = Arrays.asList(new OperationResponse(true), new OperationResponse("Failure"), new OperationResponse(false));

	@Test
	public void create()
	{
		var value = new CountResults(6, RESPONSES);

		Assert.assertEquals(6, value.count);
		Assert.assertEquals(6L, value.toLong());
		Assert.assertEquals(3, value.responses.size());
		Assert.assertEquals(RESPONSES, value.responses);
	}

	@Test
	public void create_int()
	{
		var value = new CountResults(123);

		Assert.assertEquals(123, value.count);
		Assert.assertEquals(123L, value.toLong());
		Assert.assertNull(value.responses);
	}

	@Test
	public void create_long()
	{
		var value = new CountResults(1234);

		Assert.assertEquals(1234, value.count);
		Assert.assertEquals(1234L, value.toLong());
		Assert.assertNull(value.responses);
	}

	@Test
	public void create_responses()
	{
		var value = new CountResults(Arrays.asList(new OperationResponse(true), new OperationResponse("Failure"), new OperationResponse(false)));

		Assert.assertEquals(3, value.count);
		Assert.assertEquals(3L, value.toLong());
		Assert.assertEquals(3, value.responses.size());
		Assert.assertEquals(RESPONSES, value.responses);
	}

	@Test
	public void create_responses_null()
	{
		var value = new CountResults((List<OperationResponse>) null);

		Assert.assertEquals(0, value.count);
		Assert.assertEquals(0L, value.toLong());
		Assert.assertNull(value.responses);
	}
}
