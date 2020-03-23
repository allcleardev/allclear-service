package app.allclear.common.dao;

import org.junit.*;

/** Unit test class that verifies the QueryFilter class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class QueryFilterTest
{
	@Test
	public void testCreation()
	{
		QueryFilter value = new QueryFilter();
		Assert.assertNotNull(value);
		Assert.assertNull(value.page);
		Assert.assertNull(value.pageSize);
	}

	@Test
	public void testProperties()
	{
		QueryFilter value = new QueryFilter(5, 55);
		Assert.assertNotNull(value);
		Assert.assertNotNull(value.page);
		Assert.assertEquals(5, value.page.intValue());
		Assert.assertNotNull(value.pageSize);
		Assert.assertEquals(55, value.pageSize.intValue());
	}

	@Test
	public void testpage()
	{
		QueryFilter value = new QueryFilter();
		Assert.assertNotNull(value);
		Assert.assertNull(value.page);
		Assert.assertNotNull(value.page());
		Assert.assertEquals(1, value.page());

		value.page = 10;
		Assert.assertNotNull(value.page);
		Assert.assertEquals(10, value.page());

		value.page = 0;
		Assert.assertNotNull(value.page);
		Assert.assertEquals(0, value.page.intValue());
		Assert.assertEquals(1, value.page());

		value.page = 10056;
		Assert.assertNotNull(value.page);
		Assert.assertEquals(10056, value.page.intValue());
		Assert.assertEquals(10056, value.page());

		value.page = -10056;
		Assert.assertNotNull(value.page);
		Assert.assertEquals(-10056, value.page.intValue());
		Assert.assertEquals(1, value.page());

		value.page = null;
		Assert.assertNull(value.page);
		Assert.assertEquals(1, value.page());
	}

	@Test
	public void testPutPageSize()
	{
		var value = new QueryFilter();
		Assert.assertNotNull(value);
		Assert.assertNull(value.pageSize);
		Assert.assertEquals(10, value.pageSize(10));

		value.pageSize = 56;
		Assert.assertNotNull(value.pageSize);
		Assert.assertEquals(56, value.pageSize.intValue());
		Assert.assertEquals(56, value.pageSize(10));

		value.pageSize = 0;
		Assert.assertNotNull(value.pageSize);
		Assert.assertEquals(0, value.pageSize.intValue());
		Assert.assertEquals(10, value.pageSize(10));

		value.pageSize = 56000;
		Assert.assertNotNull(value.pageSize);
		Assert.assertEquals(56000, value.pageSize.intValue());
		Assert.assertEquals(56000, value.pageSize(10));

		value.pageSize = -100;
		Assert.assertNotNull(value.pageSize);
		Assert.assertEquals(-100, value.pageSize.intValue());
		Assert.assertEquals(10, value.pageSize(10));

		value.pageSize = null;
		Assert.assertNull(value.pageSize);
		Assert.assertEquals(10, value.pageSize(10));
	}
}
