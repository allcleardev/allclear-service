package app.allclear.testing;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.*;

import org.junit.*;

import app.allclear.testing.TestingUtils;

/** Unit test class that verifies the JacksonUtils class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TestingUtilsTest
{
	@Test
	public void test_parse_date() throws Exception
	{
		var value = TestingUtils.loadObject("/data/testingUtils/date.json", DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check createdAt", TestingUtils.timestamp("2016-05-23T13:38:00-0000"), value.createdAt);
	}

	@Test
	public void test_parse_date_epoch() throws Exception
	{
		var value = TestingUtils.loadObject("/data/testingUtils/date_epoch.json", DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check createdAt", TestingUtils.timestamp("2016-05-23T13:38:00-0000"), value.createdAt);
	}

	@Test
	public void test_parse_date_legacy() throws Exception
	{
		var value = TestingUtils.loadObject("/data/testingUtils/date_legacy.json", DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check createdAt", TestingUtils.timestamp("2016-05-23T13:38:00-0000"), value.createdAt);
	}

	@Test
	public void test_hourAgo()
	{
		Assert.assertTrue((new Date()).after(TestingUtils.hourAgo()));
	}

	@Test
	public void test_hourAhead()
	{
		Assert.assertTrue((new Date()).before(TestingUtils.hourAhead()));
	}

	@Test
	public void test_toMap()
	{
		assertThat(TestingUtils.toMap("a", "11", "v", "31", "b", "21")).isEqualTo(Map.of("a", "11", "b", "21", "v", "31"));
	}

	@Test
	public void test_toMap_with_initial()
	{
		var initial = TestingUtils.toMap("first", "red", "second", "green", "third", "indigo", "b", "violet");
		assertThat(initial).as("Check initial").isEqualTo(Map.of("b", "violet", "third", "indigo", "second", "green", "first", "red"));

		assertThat(TestingUtils.toMap(initial, "a", "11", "v", "31", "b", "21")).as("Check merged")
			.isEqualTo(Map.of("b", "21", "third", "indigo", "second", "green", "first", "red", "a", "11", "v", "31"));
	}
}
