package app.allclear.common.value;

import java.util.Date;

import org.junit.*;

import app.allclear.testing.TestingUtils;

/** Unit test class that verifies the CreateValue POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class CreateValueTest
{
	private static Date CREATED_AT;

	@BeforeClass
	public static void up() throws Exception
	{
		CREATED_AT = TestingUtils.timestamp("2019-06-24T18:03:18-0000");
	}

	@Test
	public void create()
	{
		var v = new CreateValue(50L, "The number fifty", CREATED_AT);

		Assert.assertEquals(Long.valueOf(50L), v.id);
		Assert.assertEquals("The number fifty", v.name);
		Assert.assertEquals(CREATED_AT, v.createdAt);
		Assert.assertEquals(50, v.hashCode());
		Assert.assertEquals(new CreateValue(50L, "The number fifty"), v);
	}

	@Test
	public void create_id()
	{
		var v = new CreateValue(50L);

		Assert.assertEquals(Long.valueOf(50L), v.id);
		Assert.assertNull(v.name);
		Assert.assertNull(v.createdAt);
		Assert.assertEquals(50, v.hashCode());
		Assert.assertEquals(new CreateValue(50L, null), v);
	}

	@Test
	public void create_id_and_name()
	{
		var v = new CreateValue(50L, "The number fifty");

		Assert.assertEquals(Long.valueOf(50L), v.id);
		Assert.assertEquals("The number fifty", v.name);
		Assert.assertNull(v.createdAt);
		Assert.assertEquals(50, v.hashCode());
		Assert.assertEquals(new CreateValue(50L, "The number fifty"), v);
	}

	@Test
	public void create_name()
	{
		var v = new CreateValue("The number fifty");

		Assert.assertNull(v.id);
		Assert.assertEquals("The number fifty", v.name);
		Assert.assertNull(v.createdAt);
		Assert.assertEquals("The number fifty".hashCode(), v.hashCode());
		Assert.assertEquals(new CreateValue(null, "The number fifty"), v);
	}
}
