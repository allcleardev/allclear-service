package app.allclear.common.dao;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.*;

/** Unit test class that verifies the QueryResults class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class QueryResultsTest
{
	@Test
	public void testCreation()
	{
		var value = new QueryResults<Object, QueryFilter>();
		Assert.assertNotNull(value);
		Assert.assertEquals(0L, value.total);
		Assert.assertEquals(0, value.pages);
		Assert.assertEquals(0, value.page);
		Assert.assertEquals(0, value.pageSize);
		Assert.assertEquals(0, value.firstResult());
		Assert.assertNull(value.filter);
		Assert.assertNull(value.records);
		Assert.assertTrue(value.isEmpty());
		Assert.assertTrue(value.noRecords());
	}

	@Test
	public void testProperties()
	{
		var value = new QueryResults<Object, QueryFilter>(100L, new QueryFilter(3, 10), QueryResults.PAGE_SIZE_DEFAULT);
		Assert.assertNotNull(value);
		Assert.assertEquals(100L, value.total);
		Assert.assertEquals(10, value.pages);
		Assert.assertEquals(3, value.page);
		Assert.assertEquals(10, value.pageSize);
		Assert.assertEquals(20, value.firstResult());
		Assert.assertNotNull(value.filter);
		Assert.assertNull(value.records);
		Assert.assertFalse(value.isEmpty());
		Assert.assertTrue(value.noRecords());
	}

	@Test
	public void testDefaults()
	{
		var value = new QueryResults<Object, QueryFilter>(100L, new QueryFilter(), QueryResults.PAGE_SIZE_DEFAULT);
		Assert.assertNotNull(value);
		Assert.assertEquals(100L, value.total);
		Assert.assertEquals(100 / QueryResults.PAGE_SIZE_DEFAULT, value.pages);
		Assert.assertEquals(1, value.page);
		Assert.assertEquals(QueryResults.PAGE_SIZE_DEFAULT, value.pageSize);
		Assert.assertEquals(0, value.firstResult());
		Assert.assertNotNull(value.filter);
		Assert.assertNull(value.records);
		Assert.assertFalse(value.isEmpty());
		Assert.assertTrue(value.noRecords());

		value = new QueryResults<>(100L, new QueryFilter(), 5);
		Assert.assertNotNull(value);
		Assert.assertEquals(100L, value.total);
		Assert.assertEquals(20, value.pages);
		Assert.assertEquals(1, value.page);
		Assert.assertEquals(5, value.pageSize);
		Assert.assertEquals(0, value.firstResult());
		Assert.assertNotNull(value.filter);
		Assert.assertNull(value.records);
		Assert.assertFalse(value.isEmpty());
		Assert.assertTrue(value.noRecords());

		value = new QueryResults<>(100L, new QueryFilter());
		Assert.assertNotNull(value);
		Assert.assertEquals(100L, value.total);
		Assert.assertEquals(100 / QueryResults.PAGE_SIZE_DEFAULT, value.pages);
		Assert.assertEquals(1, value.page);
		Assert.assertEquals(QueryResults.PAGE_SIZE_DEFAULT, value.pageSize);
		Assert.assertEquals(0, value.firstResult());
		Assert.assertNotNull(value.filter);
		Assert.assertNull(value.records);
		Assert.assertFalse(value.isEmpty());
		Assert.assertTrue(value.noRecords());
	}

	@Test
	public void testRecords()
	{
		var value = new QueryResults<Integer, QueryFilter>(100L, new QueryFilter(200, 10));
		Assert.assertNotNull(value);
		Assert.assertEquals(100L, value.total);
		Assert.assertEquals(10, value.pages);
		Assert.assertEquals(10, value.page);	// 200 is past the last page of 100L / 10.
		Assert.assertEquals(10, value.pageSize);
		Assert.assertEquals(90, value.firstResult());
		Assert.assertNotNull(value.filter);
		Assert.assertNull(value.records);
		Assert.assertFalse(value.isEmpty());
		Assert.assertTrue(value.noRecords());

		Assert.assertNotNull(value.withRecords(new ArrayList<Integer>(0)));
		Assert.assertNotNull(value.records);
		Assert.assertTrue(value.records.isEmpty());
		Assert.assertFalse(value.isEmpty());
		Assert.assertTrue(value.noRecords());

		Assert.assertNotNull(value.withRecords(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
		Assert.assertNotNull(value.records);
		Assert.assertFalse(value.records.isEmpty());
		Assert.assertEquals(10, value.records.size());
		Assert.assertFalse(value.isEmpty());
		Assert.assertFalse(value.noRecords());
	}
}
