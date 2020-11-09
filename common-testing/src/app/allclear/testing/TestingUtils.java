package app.allclear.testing;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

/** Class that contains static constants and helper methods for the REST tests.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TestingUtils
{
	public static final ObjectMapper jsonMapper = new ObjectMapper();
	private static final long MILLISECONDS_DAY = 24L * 60L * 60L * 1000L;
	private static final long MILLISECONDS_HOUR = 60L * 60L * 1000L;
	private static final DateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static final DateFormat TIMESTAMP_FORMATTER_LEGACY = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static
	{
		var utc = TimeZone.getTimeZone("UTC");
		TIMESTAMP_FORMATTER.setTimeZone(utc);
		TIMESTAMP_FORMATTER_LEGACY.setTimeZone(utc);

		jsonMapper.setDateFormat(TIMESTAMP_FORMATTER);
		jsonMapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		var module = new SimpleModule();
		module.addDeserializer(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonParser parser, DeserializationContext context) throws JsonProcessingException, IOException {
				var node = parser.getCodec().readValue(parser, String.class);
				if (StringUtils.isEmpty(node))
					return null;
				try
				{
					return new Date(Long.valueOf(node));
				}
				catch (NumberFormatException ex)
				{
					try
					{
						if (node.endsWith("Z"))
							return TIMESTAMP_FORMATTER_LEGACY.parse(node);

						return TIMESTAMP_FORMATTER.parse(node);
					}
					catch (ParseException e) { throw new IOException(e); }
				}
			}
		});
		jsonMapper.registerModule(module);
	}

	/** HTTP status. */
	public static final int HTTP_STATUS_OK = Response.Status.OK.getStatusCode();
	public static final int HTTP_STATUS_NO_CONTENT = Response.Status.NO_CONTENT.getStatusCode();
	public static final int HTTP_STATUS_NOT_AUTHORIZED = Response.Status.UNAUTHORIZED.getStatusCode();
	public static final int HTTP_STATUS_AUTHENTICATE = Response.Status.FORBIDDEN.getStatusCode();
	public static final int HTTP_STATUS_NOT_FOUND = Response.Status.NOT_FOUND.getStatusCode();
	public static final int HTTP_STATUS_VALIDATION_EXCEPTION = 422;
	public static final int HTTP_STATUS_RUNTIME_EXCEPTION = 500;

	/** Headers. */
	public static final String HEADER_LOCALE = HttpHeaders.ACCEPT_LANGUAGE;

	/** Retrieves object from test resource JSON payload.
	 * 
	 * @param fileName
	 * @param clazz
	 * @return never NULL.
	 * @throws IOException
	 */
	public static <T> T loadObject(String fileName, Class<T> clazz) throws IOException
	{
		return jsonMapper.readValue(TestingUtils.class.getResourceAsStream(fileName), clazz);
	}

	/** Retrieves object from test resource JSON payload.
	 * 
	 * @param fileName
	 * @param type Generic type reference.
	 * @return never NULL.
	 * @throws IOException
	 */
	public static <T> T loadObject(String fileName, TypeReference<T> type) throws IOException
	{
		return jsonMapper.readValue(TestingUtils.class.getResourceAsStream(fileName), type);
	}

	/** Retrieves object from a JSON payload file.
	 * 
	 * @param fileName
	 * @param clazz
	 * @return never NULL.
	 * @throws IOException
	 */
	public static <T> T loadFile(String fileName, Class<T> clazz) throws IOException
	{
		return jsonMapper.readValue(new File(fileName), clazz);
	}

	/** Increments the long-value of a map based on a generic key.
	 * 
	 * @param counts
	 * @param key
	 */
	public static <T> void incrementMap(Map<T, Long> counts, T key)
	{
		var value = counts.get(key);
		if (null == value)
			value = 1L;
		else
			value++;

		counts.put(key, value);
	}

	/** Converts an array of strings to a Map with keys and values.
	 * 
	 * @param values an array of key-value pairs.
	 * @return the created map
	 */
	public static Map<String, String> toMap(final String... values)
	{
		var results = new HashMap<String, String>(values.length / 2);
		for (int i = 0; i < values.length - 1; i+=2)
			results.put(values[i], values[i + 1]);

		return results;
	}

	/** Converts an array of strings to a Map with keys and values.
	 * 
	 * @param initial initial set of key-value pairs
	 * @param values additional key-value pairs.
	 * @return the created map
	 */
	public static Map<String, String> toMap(final Map<String, String> initial, final String... values)
	{
		var results = new HashMap<String, String>(initial);
		for (int i = 0; i < values.length - 1; i+=2)
			results.put(values[i], values[i + 1]);

		return results;
	}

	/** Calendar with time values zero'd out. */
	private static Calendar CAL = Calendar.getInstance();
	private static Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	static
	{
		CAL.set(Calendar.HOUR_OF_DAY, 0);
		CAL.set(Calendar.MINUTE, 0);
		CAL.set(Calendar.SECOND, 0);
		CAL.set(Calendar.MILLISECOND, 0);

		UTC.set(Calendar.HOUR_OF_DAY, 0);
		UTC.set(Calendar.MINUTE, 0);
		UTC.set(Calendar.SECOND, 0);
		UTC.set(Calendar.MILLISECOND, 0);
	}

	/** Generates a Date value without time. */
	public static Date date(int year, int month, int date)
	{
		CAL.set(Calendar.YEAR, year);
		CAL.set(Calendar.MONTH, month - 1);
		CAL.set(Calendar.DATE, date);

		return CAL.getTime();
	}

	/** Generates a Date value without time (UTC). */
	public static Date utc(int year, int month, int date)
	{
		UTC.set(Calendar.YEAR, year);
		UTC.set(Calendar.MONTH, month - 1);
		UTC.set(Calendar.DATE, date);

		return UTC.getTime();
	}

	/** Zeros the time portion of a date. */
	public static Date zero(Date value)
	{
		if (null == value)
			return value;

		var cal = Calendar.getInstance();
		cal.setTime(value);

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	/** Parses a string to a date/time based on the standard timestamp formatter.
	 * 
	 * @return never NULL
	 * @throws ParseException
	 */
	public static Date timestamp(String value)
	{
		try { return TIMESTAMP_FORMATTER.parse(value); }
		catch(final ParseException ex) { throw new RuntimeException(ex); }
	}

	/** Generates a date/time string from a date object based on the standard timestamp formatter.
	 * 
	 * @param value
	 * @return never NULL.
	 */
	public static String timestamp(Date value) { return TIMESTAMP_FORMATTER.format(value); }

	public static Date days(final Date value, final int i) { return days(value, (long) i); }
	public static Date days(final Date value, final long i) { return new Date(value.getTime() + (i * MILLISECONDS_DAY)); }
	public static Date hours(final Date value, final int i) { return hours(value, (long) i); }
	public static Date hours(final Date value, final long i) { return new Date(value.getTime() + (i * MILLISECONDS_HOUR)); }
	public static Date hourAgo() { return new Date(System.currentTimeMillis() - MILLISECONDS_HOUR); }
	public static Date hourAhead() { return new Date(System.currentTimeMillis() + MILLISECONDS_HOUR); }
	public static Date hourAgo(final Date value) { return hours(value, -1L); }
	public static Date hourAhead(final Date value) { return hours(value, 1L); }
	public static Date seconds(final Date value, final int i) { return seconds(value, (long) i); }
	public static Date seconds(final Date value, final long i) { return new Date(value.getTime() + (i * 1000L)); }
}
