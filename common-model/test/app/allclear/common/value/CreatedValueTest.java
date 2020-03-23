package app.allclear.common.value;

import java.util.Date;

import org.junit.*;

import app.allclear.testing.TestingUtils;

/** Unit test class that verifies the CreatedValue POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class CreatedValueTest
{
	private static Date CREATED_AT;

	@BeforeClass
	public static void up() throws Exception
	{
		CREATED_AT = TestingUtils.timestamp("2019-06-24T17:05:48-0000");
	}

	@Test
	public void create()
	{
		var v = new CreatedValue("50L", "The number fifty", CREATED_AT);

		Assert.assertEquals("50L", v.id);
		Assert.assertEquals("The number fifty", v.name);
		Assert.assertEquals(CREATED_AT, v.createdAt);
		Assert.assertEquals("50L".hashCode(), v.hashCode());
		Assert.assertEquals(new CreatedValue("50L", "The number fifty", CREATED_AT), v);
	}

	@Test
	public void create_id()
	{
		var v = new CreatedValue("50");

		Assert.assertEquals("50", v.id);
		Assert.assertNull(v.name);
		Assert.assertNull(v.createdAt);
		Assert.assertEquals("50".hashCode(), v.hashCode());
		Assert.assertEquals(new CreatedValue("50", null, null), v);
	}
}
