package app.allclear.common.jackson;

import java.io.IOException;
import java.text.*;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import io.dropwizard.jackson.Jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/** Utility class that provides standard helpers for the Jackson parsers.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class JacksonUtils
{
	private JacksonUtils() {}	// Eliminate construction.

	/** Represents the standard timestamp formatter/parser. */
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	private static final FastDateFormat TIMESTAMP_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZ", UTC);
	private static final FastDateFormat TIMESTAMP_FORMATTER_LEGACY = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'", UTC);
	private static final FastDateFormat TIMESTAMP_FORMATTER_MILLI = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ", UTC);

	/** Creates SimpleModule to parse date. Uses FastDateFormat object to avoid concurrency issues. */
	public static SimpleModule createDateParserModule()
	{
		return new SimpleModule().addDeserializer(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonParser parser, DeserializationContext context) throws JsonProcessingException, IOException {
				String node = parser.getCodec().readValue(parser, String.class);
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
							return timestampZ(node);

						return timestamp(node);
					}
					catch (ParseException e) { throw new IOException(e); }
				}
			}
		});
	}

	/** Creates SimpleModule to parse timestamps with optional milliseconds. Uses FastDateFormat object to avoid concurrency issues. */
	public static SimpleModule createTimestampParserModule()
	{
		return new SimpleModule().addDeserializer(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonParser parser, DeserializationContext context) throws JsonProcessingException, IOException {
				String node = parser.getCodec().readValue(parser, String.class);
				if (StringUtils.isEmpty(node))
					return null;
				try
				{
					return new Date(Long.valueOf(node));
				}
				catch (NumberFormatException ex)
				{
					try { return timestampMS(node);	}	// Attempt to parse with millisecond formatter.
					catch (ParseException e)
					{
						try
						{
							if (node.endsWith("Z"))
								return timestampZ(node);

							return timestamp(node);
						}
						catch (ParseException pe) { throw new IOException(pe); }
					}
				}
			}
		});
	}

	/** DynamoDB numbers come out as floats. The default Jackson date parser only works with integers. */
	public static SimpleModule createDynamoDBParserModule()
	{
		return new SimpleModule().addDeserializer(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(final JsonParser parser, final DeserializationContext context) throws JsonProcessingException, IOException {
				final String node = parser.getCodec().readValue(parser, String.class);
				if (StringUtils.isEmpty(node))
					return null;

				return new Date(Long.valueOf(node));
			}
		});
	}

	/** Parses a string to a date/time based on the standard timestamp formatter.
	 *  
	 * @return never NULL
	 * @throws ParseException
	 */
	public static Date timestamp(String value) throws ParseException { return TIMESTAMP_FORMATTER.parse(value); }

	/** Parses a string to a date/time based on the standard timestamp formatter.
	 *  Uses the legacy formatter.
	 *  
	 * @return never NULL
	 * @throws ParseException
	 */
	public static Date timestampZ(String value) throws ParseException { return TIMESTAMP_FORMATTER_LEGACY.parse(value); }

	/** Parses a string to a date/time based on the standard timestamp formatter with milliseconds.
	 *  
	 * @return never NULL
	 * @throws ParseException
	 */
	public static Date timestampMS(String value) throws ParseException { return TIMESTAMP_FORMATTER_MILLI.parse(value); }

	/** Generates a date/time string from a date object based on the standard timestamp formatter.
	 * 
	 * @param value
	 * @return never NULL.
	 */
	public static String timestamp(Date value) { return TIMESTAMP_FORMATTER.format(value); }

	/** Generates a date/time string from a date object based on the standard timestamp formatter.
	 *  Uses the legacy formatter.
	 * 
	 * @param value
	 * @return never NULL.
	 */
	public static String timestampZ(Date value) { return TIMESTAMP_FORMATTER_LEGACY.format(value); }

	/** Generates a date/time string from a date object based on the standard timestamp formatter with milliseconds.
	 * 
	 * @param value
	 * @return never NULL.
	 */
	public static String timestampMS(Date value) { return TIMESTAMP_FORMATTER_MILLI.format(value); }

	/** Helper method - creates a Dropwizard friendly JSON object-mapper.
	 * 
	 * @return never NULL.
	 */
	public static final ObjectMapper createMapper() { return configure(Jackson.newObjectMapper()); }

	/** Helper method - creates a Dropwizard friendly JSON object-mapper with millisecond date formatter/parser.
	 * 
	 * @return never NULL.
	 */
	public static final ObjectMapper createMapperMS() { return configureMS(Jackson.newObjectMapper()); }

	/** Helper method - creates a Dropwizard friendly JSON object-mapper with millisecond date formatter/parser.
	 * 
	 * @return never NULL.
	 */
	public static final ObjectMapper createMapperDynamoDB() { return configureDynamoDB(Jackson.newObjectMapper()); }

	/** Helper method - customizes the Jackson object-mapper.
	 *
	 * @param mapper
	 * @return the supplied re-configured mapper.
	 */
	public static ObjectMapper configure(final ObjectMapper mapper)
	{
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.registerModule(createDateParserModule());
	}

	/** Helper method - customizes the Jackson object-mapper with the millisecond date formatter/parser.
	 *
	 * @param mapper
	 * @return the supplied re-configured mapper.
	 */
	public static ObjectMapper configureMS(final ObjectMapper mapper)
	{
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.registerModule(createTimestampParserModule());	// Need custom date parser for legacy UTC date formats. DLS on 5/21/2016.
	}

	/** Helper method - customizes the Jackson object-mapper for DynamoDB maps.
	 *
	 * @param mapper
	 * @return the supplied re-configured mapper.
	 */
	public static ObjectMapper configureDynamoDB(final ObjectMapper mapper)
	{
		mapper.setDateFormat(null); // Defaults to serialize as a long.
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
			.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.registerModule(createDynamoDBParserModule());
	}
}
