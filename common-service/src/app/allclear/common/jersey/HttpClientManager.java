package app.allclear.common.jersey;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.function.Function;

import javax.net.ssl.*;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

// import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.*;

import io.dropwizard.lifecycle.Managed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import app.allclear.common.mediatype.UTF8MediaType;

/** Dropwizard managed component that wraps the RESTful calls to an underlying RESTful service.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class HttpClientManager implements Managed
{
	public static final int ERROR = 400;

	protected final HttpClientConfig conf;
	protected Client client = null;
	protected ObjectMapper mapper;
	protected final Logger log;

	/** Creates a Jackson JAXB provider for a custom ObjectMapper. */
	private static JacksonJaxbJsonProvider provider(final ObjectMapper mapper)
	{
		var value = new JacksonJaxbJsonProvider();
		value.setMapper(mapper);

		return value;
	}

	public HttpClientManager(final HttpClientConfig conf)
	{
		this.conf = conf;
		this.log = LoggerFactory.getLogger(getClass());	// Make sure to use the derived class.
	}

	@Override
	public void start() throws Exception
	{
		if (null != client)	// Already started?
			return;

		// var mapper = getObjectMapper();
		var builder = ClientBuilder.newBuilder();
		// if (null != mapper)
			// builder.register(provider(mapper));
			// builder.withConfig(new ClientConfig(provider(mapper)));

		if (!conf.checkCert)
		{
			try
			{
				var context = SSLContext.getInstance("TLS");
				context.init(null, new TrustManager[] { new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
					public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
					public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
				}}, new SecureRandom());
				client = builder/*.register(MultiPartFeature.class) */.sslContext(context).hostnameVerifier(new javax.net.ssl.HostnameVerifier() {
					@Override
					public boolean verify(String a, javax.net.ssl.SSLSession s) { return true; }
				}
				).build();
			}
			catch (Exception ex) { throw new RuntimeException(ex); }
		}
		else
			client = builder/*.register(MultiPartFeature.class) */.build();

		client.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true).property(ClientProperties.READ_TIMEOUT, 60000);
		if (conf.noExecute)
			client.register(new HttpClientTestFilter());

		if (null != (mapper = getObjectMapper())) client.register(provider(mapper));
	}

	@Override
	public void stop() throws Exception
	{
		if (null != client)
		{
			client.close();
			client = null;
		}
	}

	/** Override to provide a custom ObjectMapper. */
	public ObjectMapper getObjectMapper() { return null; }

	/** Override to change the header name for the REST service's key header property. Defaults to "key". */
	public String getHeaderKeyName() { return "key"; }

	/** Override to return a custom 'key' value. For example OAuth tokens are NOT static, but change as they expire. */
	public String getHeaderKeyValue() { return conf.key; }

	/** Applies a single header for the API key. */
	public Invocation.Builder wrapBuilder(final Invocation.Builder b)
	{
		return b.header(getHeaderKeyName(), getHeaderKeyValue());
	}

	protected Object map(final Object request)	// Hack: for unknown reason, the client.register is not honored when run in an environment with multiple Jersey Client instances. QS does NOT respect the ObjectMapper added to the Jersey Client. Tried many different configs but nothing worked. Instead am converting to JSON here. DLS on 1/23/2020.
	{
		if ((null == mapper) || (request instanceof String)) return request;

		try { return mapper.writeValueAsString(request); }
		catch (final RuntimeException ex) { throw ex; }
		catch (final Exception ex) { throw new RuntimeException(ex); }
	}

	/** Helper method - make a DELETE request to the underlying REST service. */
	protected <T> T delete(final WebTarget resource, final Class<T> clazz) throws HttpClientException
	{
		return request(resource, i -> i.delete(), r -> r.readEntity(clazz));
	}

	/** Helper method - make a DELETE request to the underlying REST service. */
	protected <T> T delete(final WebTarget resource, final GenericType<T> type) throws HttpClientException
	{
		return request(resource, i -> i.delete(), r -> r.readEntity(type));
	}

	/** Helper method - make a DELETE request to the underlying REST service. */
	protected <T> T delete(final WebTarget resource, final Object request, final Class<T> clazz) throws HttpClientException
	{
		return request(resource, i -> i.method(HttpMethod.DELETE, Entity.entity(map(request), UTF8MediaType.APPLICATION_JSON_TYPE)), r -> r.readEntity(clazz));
	}

	/** Helper method - make a DELETE request to the underlying REST service. */
	protected <T> T delete(final WebTarget resource, final Object request, final GenericType<T> type) throws HttpClientException
	{
		return request(resource, i -> i.method(HttpMethod.DELETE, Entity.entity(map(request), UTF8MediaType.APPLICATION_JSON_TYPE)), r -> r.readEntity(type));
	}

	/** Helper method - make a GET request to the underlying REST service. */
	protected <T> T get(final WebTarget resource, final Class<T> clazz) throws HttpClientException
	{
		return request(resource, i -> i.get(), r -> r.readEntity(clazz));
	}

	/** Helper method - make a GET request to the underlying REST service. */
	protected <T> T get(final WebTarget resource, final GenericType<T> type) throws HttpClientException
	{
		return request(resource, i -> i.get(), r -> r.readEntity(type));
	}

	/** Helper method - make a POST request to the underlying REST service. */
	protected <T> T post(final WebTarget resource, final Object request, final Class<T> clazz) throws HttpClientException
	{
		return request(resource, i -> i.post(Entity.entity(map(request), UTF8MediaType.APPLICATION_JSON_TYPE)), r -> r.readEntity(clazz));
	}

	/** Helper method - make a POST request to the underlying REST service. */
	protected <T> T post(final WebTarget resource, final Object request, final GenericType<T> type) throws HttpClientException
	{
		return request(resource, i -> i.post(Entity.entity(map(request), UTF8MediaType.APPLICATION_JSON_TYPE)), r -> r.readEntity(type));
	}

	/** Helper method - make a POST request without a payload to the underlying REST service. */
	protected <T> T post(final WebTarget resource, final Class<T> clazz) throws HttpClientException
	{
		return request(resource, i -> i.method(HttpMethod.POST), r -> r.readEntity(clazz));
	}

	/** Helper method - make a POST request without a payload to the underlying REST service. */
	protected <T> T post(final WebTarget resource, final GenericType<T> type) throws HttpClientException
	{
		return request(resource, i -> i.method(HttpMethod.POST), r -> r.readEntity(type));
	}

	/** Helper method - make a PUT request to the underlying REST service. */
	protected <T> T put(final WebTarget resource, final Object request, final Class<T> clazz) throws HttpClientException
	{
		return request(resource, i -> i.put(Entity.entity(map(request), UTF8MediaType.APPLICATION_JSON_TYPE)), r -> r.readEntity(clazz));
	}

	/** Helper method - make a PUT request to the underlying REST service. */
	protected <T> T put(final WebTarget resource, final Object request, final GenericType<T> type) throws HttpClientException
	{
		return request(resource, i -> i.put(Entity.entity(map(request), UTF8MediaType.APPLICATION_JSON_TYPE)), r -> r.readEntity(type));
	}

	/** Helper method - make a PUT request without a payload to the underlying REST service. */
	protected <T> T put(final WebTarget resource, final Class<T> clazz) throws HttpClientException
	{
		return request(resource, i -> i.method(HttpMethod.PUT), r -> r.readEntity(clazz));
	}

	/** Helper method - make a PUT request without a payload to the underlying REST service. */
	protected <T> T put(final WebTarget resource, final GenericType<T> type) throws HttpClientException
	{
		return request(resource, i -> i.method(HttpMethod.PUT), r -> r.readEntity(type));
	}

	/** Helper method - sends a request to the underlying REST service. */
	protected <T> T request(final WebTarget resource, final Function<SyncInvoker, Response> executor, final Function<Response, T> handler) throws HttpClientException
	{
		var response = executor.apply(wrapBuilder(resource.request(UTF8MediaType.APPLICATION_JSON_TYPE)));
		if (ERROR <= response.getStatus())
			throw new HttpClientException(response.readEntity(String.class), resource.getUri().toString(),response.getStatus());

		return handler.apply(response);
	}
}
