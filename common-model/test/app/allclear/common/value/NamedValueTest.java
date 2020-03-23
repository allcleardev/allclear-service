package app.allclear.common.value;

import org.junit.*;

/** Unit test class that verifies the NamedValue POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class NamedValueTest
{
	@Test
	public void create()
	{
		var v = new NamedValue("50L", "The number fifty");

		Assert.assertEquals("50L", v.getId());
		Assert.assertEquals("The number fifty", v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals("50L".hashCode(), v.hashCode());
		Assert.assertEquals(new NamedValue("50L", "The number fifty"), v);
	}

	@Test
	public void create_empty()
	{
		var v = new NamedValue();

		Assert.assertNull(v.getId());
		Assert.assertNull(v.getName());
		Assert.assertTrue(v.empty());
		Assert.assertEquals(0, v.hashCode());
		Assert.assertEquals(new NamedValue(null, null), v);
	}

	@Test
	public void create_int()
	{
		var v = new NamedValue("50");

		Assert.assertEquals("50", v.getId());
		Assert.assertNull(v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals("50".hashCode(), v.hashCode());
		Assert.assertEquals(new NamedValue("50", null), v);
	}

	@Test
	public void create_long()
	{
		var v = new NamedValue("50L");

		Assert.assertEquals("50L", v.getId());
		Assert.assertNull(v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals("50L".hashCode(), v.hashCode());
		Assert.assertEquals(new NamedValue("50L", null), v);
	}

	@Test
	public void create_name()
	{
		var v = new NamedValue(null, "The number fifty");

		Assert.assertNull(v.getId());
		Assert.assertEquals("The number fifty", v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals("The number fifty".hashCode(), v.hashCode());
		Assert.assertEquals(new NamedValue(null, "The number fifty"), v);
	}
}
