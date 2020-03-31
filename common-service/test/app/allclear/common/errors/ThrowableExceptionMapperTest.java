package app.allclear.common.errors;

import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.mediatype.UTF8MediaType;

/** Funtional test class that verifies the ThrowableExceptionMapper.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ThrowableExceptionMapperTest
{
	private static final ObjectMapper MAPPER = JacksonUtils.createMapper();
	private static final Pattern PATTERN_WHITESPACE = Pattern.compile("\\s");

	public ThrowableExceptionMapper mapper = new ThrowableExceptionMapper();

	// @Test(expected=JsonMappingException.class) // Exception is NOT thrown in Java 9 or later. DLS on 9/26/2018.
	public void serializeSSLHandshakeException_failure() throws Exception
	{
		MAPPER.writeValueAsString(new SSLHandshakeException("Certificate is bad."));
	}

	@Test
	public void checkIllegalArgumentException() throws Exception
	{
		final String message = "The name is field is too short";
		try
		{
			throw new IllegalArgumentException(message);
		}

		catch (IllegalArgumentException ex)
		{
			ErrorInfo v = new ErrorInfo(ex);
			verifySerialization("IllegalArgumentException: ", MAPPER.writeValueAsString(v), message);
			verifyResponse(mapper.toResponse(ex), message, v);
		}
	}

	@Test
	public void checkSSLHandshakeException() throws Exception
	{
		final String message = "Certificate is NOT good.";
		try
		{
			throw new SSLHandshakeException(message);
		}

		catch (SSLHandshakeException ex)
		{
			ErrorInfo v = new ErrorInfo(ex);
			verifySerialization("SSLHandshakeException: ", MAPPER.writeValueAsString(v), message);
			verifyResponse(mapper.toResponse(ex), message, v);
		}
	}

	/** Checks the results of serializing an ErrorInfo object. */
	@SuppressWarnings("unchecked")
	private void verifySerialization(String assertId, String value, String message) throws Exception
	{
		Assert.assertNotNull(assertId + "Serialized", value);
		Assert.assertFalse(assertId + "NOT empty", value.isEmpty());

		Map<String, Object> results = MAPPER.readValue(value, Map.class);
		Assert.assertNotNull(assertId + "Results exist", results);
		Assert.assertFalse(assertId + "Results are NOT empty", results.isEmpty());
		Assert.assertTrue(assertId + "Has message", results.containsKey("error"));
		Assert.assertTrue(assertId + "Message is String", results.get("error") instanceof String);
		Assert.assertEquals(assertId + "Check message", message, results.get("error"));
		Assert.assertTrue(assertId + "Has stack trace", results.containsKey("stacktrace"));
		Assert.assertTrue(assertId + "Stack trace is List", results.get("stacktrace") instanceof String);
		Assert.assertFalse(assertId + "Stack trace is NOT empty", ((String) results.get("stacktrace")).isEmpty());
	}

	public void verifyResponse(Response response, String message, ErrorInfo v)
	{
		Assert.assertNotNull("Response exists", response);
		Assert.assertEquals("Response status", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
		Assert.assertEquals("Response type", clean(UTF8MediaType.APPLICATION_JSON), clean(response.getMetadata().getFirst("Content-Type").toString()));
		Assert.assertTrue("Response entity is ErrorInfo", response.getEntity() instanceof ErrorInfo);
		ErrorInfo results = (ErrorInfo) response.getEntity();
		Assert.assertEquals("Response entity message", message, results.error);
		Assert.assertNotNull("Response entity stacktrace exists", results.stacktrace);
		Assert.assertEquals("Response entity stacktrace", v.stacktrace, results.stacktrace);
	}

	/** Helper method - removes white space. */
	private String clean(String value)
	{
		if (StringUtils.isEmpty(value))
			return value;

		return PATTERN_WHITESPACE.matcher(value).replaceAll("");
	}
}
