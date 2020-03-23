package app.allclear.common.jackson;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;

import org.junit.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import app.allclear.testing.TestingUtils;

/** Unit test class that verifies the JacksonUtils class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class JacksonUtilsTest
{
	public static final long DAY = 24L * 60L * 60L * 1000L;

	public static final ObjectMapper MAPPER = JacksonUtils.createMapper();
	public static final ObjectMapper MAPPER_MS = JacksonUtils.createMapperMS();
	public static final ObjectMapper MAPPER_DYNAMO_DB = JacksonUtils.createMapperDynamoDB();

	/** Helper method - loads a resource as an input stream. */
	private InputStream load(final String fileName)
	{
		return this.getClass().getResourceAsStream("/data/jacksonUtils/" + fileName);
	}

	@Test
	public void test_parse_date() throws Exception
	{
		final DateTest value = MAPPER.readValue(load("date.json"), DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check created_at", JacksonUtils.timestamp("2016-05-23T13:38:00-0000"), value.createdAt);
	}

	@Test
	public void test_parse_date_dynamoDB() throws Exception
	{
		final DateTest value = MAPPER_DYNAMO_DB.readValue(load("date_epoch.json"), DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check created_at", JacksonUtils.timestamp("2016-05-23T13:38:00-0000"), value.createdAt);

		final String output = MAPPER_DYNAMO_DB.writeValueAsString(value);
		Assert.assertEquals("Check output", "{\"id\":1234,\"createdAt\":1464010680000}", output);
	}

	@Test
	public void test_parse_date_epoch() throws Exception
	{
		final DateTest value = MAPPER.readValue(load("date_epoch.json"), DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check created_at", JacksonUtils.timestamp("2016-05-23T13:38:00-0000"), value.createdAt);
	}

	@Test
	public void test_parse_date_legacy() throws Exception
	{
		final DateTest value = MAPPER.readValue(load("date_legacy.json"), DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check created_at", JacksonUtils.timestamp("2016-05-23T13:38:00-0000"), value.createdAt);
	}

	@Test
	public void test_timestamp() throws Exception
	{
		final String formatter = "2016-05-05T15:27:28+0000";
		final Date parsed = JacksonUtils.timestamp(formatter);
		assertThat(parsed).isCloseTo(TestingUtils.date(2016, 5, 5), DAY);

		Assert.assertEquals(formatter, JacksonUtils.timestamp(parsed));
	}

	@Test(expected=ParseException.class)
	public void test_timestamp_unparsable() throws Exception
	{
		JacksonUtils.timestamp("2016-05-05T15:27:28Z");
	}

	@Test
	public void test_mapper_with_milliseconds() throws Exception
	{
		final DateTest value = MAPPER_MS.readValue(load("date_milliseconds.json"), DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(12345), value.id);
		Assert.assertEquals("Check created_at", "2016-05-23T13:38:00.371+0000", JacksonUtils.timestampMS(value.createdAt));
	}

	@Test
	public void test_mapper_with_milliseconds_epoch() throws Exception
	{
		final DateTest value = MAPPER_MS.readValue(load("date_epoch.json"), DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check created_at", "2016-05-23T13:38:00+0000", JacksonUtils.timestamp(value.createdAt));
	}

	@Test
	public void test_mapper_with_milliseconds_legacy() throws Exception
	{
		DateTest value = MAPPER_MS.readValue(load("date_legacy.json"), DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check created_at", JacksonUtils.timestamp("2016-05-23T13:38:00-0000"), value.createdAt);
	}

	@Test
	public void test_mapper_with_milliseconds_standard() throws Exception
	{
		final DateTest value = MAPPER_MS.readValue(load("date.json"), DateTest.class);
		Assert.assertEquals("Check id", Integer.valueOf(1234), value.id);
		Assert.assertEquals("Check created_at", "2016-05-23T13:38:00+0000", JacksonUtils.timestamp(value.createdAt));
	}

	@Test
	public void test_timestamp_parse_with_milliseconds() throws Exception
	{
		assertThat(JacksonUtils.timestampMS("2016-05-05T15:27:28.567-0000")).isCloseTo(JacksonUtils.timestamp("2016-05-05T15:27:28-0000"), 568L).as("Check parse");
		Assert.assertEquals("Check format", "2016-05-05T15:27:28.567+0000", JacksonUtils.timestampMS(JacksonUtils.timestampMS("2016-05-05T15:27:28.567-0000")));
	}

	@Test(expected=ParseException.class)
	public void test_timestamp_parse_with_milliseconds_error() throws Exception
	{
		JacksonUtils.timestamp("2016-05-05T15:27:28.567-0000");
	}

	@Test(expected=ParseException.class)
	public void test_timestamp_parse_without_milliseconds_error() throws Exception
	{
		JacksonUtils.timestampMS("2016-05-05T15:27:28-0000");
	}
}
