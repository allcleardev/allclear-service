package app.allclear.common.value;

import org.junit.*;

/** Unit test class that verifies the NameValue POJO.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class NameValueTest
{
	@Test
	public void create()
	{
		var v = new NameValue(50L, "The number fifty");

		Assert.assertEquals(Long.valueOf(50L), v.getId());
		Assert.assertEquals("The number fifty", v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals(50, v.hashCode());
		Assert.assertEquals(new NameValue(50L, "The number fifty"), v);
	}

	@Test
	public void create_empty()
	{
		var v = new NameValue();

		Assert.assertNull(v.getId());
		Assert.assertNull(v.getName());
		Assert.assertTrue(v.empty());
		Assert.assertEquals(0, v.hashCode());
		Assert.assertEquals(new NameValue(null, null), v);
	}

	@Test
	public void create_int()
	{
		var v = new NameValue(50);

		Assert.assertEquals(Long.valueOf(50L), v.getId());
		Assert.assertNull(v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals(50, v.hashCode());
		Assert.assertEquals(new NameValue(50L, null), v);
	}

	@Test
	public void create_long()
	{
		var v = new NameValue(50L);

		Assert.assertEquals(Long.valueOf(50L), v.getId());
		Assert.assertNull(v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals(50, v.hashCode());
		Assert.assertEquals(new NameValue(50L, null), v);
	}

	@Test
	public void create_name()
	{
		var v = new NameValue(null, "The number fifty");

		Assert.assertNull(v.getId());
		Assert.assertEquals("The number fifty", v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals("The number fifty".hashCode(), v.hashCode());
		Assert.assertEquals(new NameValue(null, "The number fifty"), v);
	}

	@Test
	public void create_string()
	{
		var v = new NameValue("50");

		Assert.assertEquals(Long.valueOf(50L), v.getId());
		Assert.assertNull(v.getName());
		Assert.assertFalse(v.empty());
		Assert.assertEquals(50, v.hashCode());
		Assert.assertEquals(new NameValue(50L, null), v);
	}
}
