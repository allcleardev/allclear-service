package app.allclear.common.jersey;

import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.HttpHeaders.*;

import java.util.List;
import java.util.LinkedList;

import javax.ws.rs.container.*;

/** Jersey container filter that provides Cross Domain access to services.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class CrossDomainHeadersFilter implements ContainerResponseFilter
{
	private static final List<String> HEADERS = List.of("Origin", "X-Requested-With", ACCEPT, ACCEPT_LANGUAGE, AUTHORIZATION, CONTENT_TYPE);

	private final String headers;

	public CrossDomainHeadersFilter(final String... headers)
	{
		var v = new LinkedList<String>(HEADERS);
		for (var h : headers) v.add(h);

		this.headers = v.stream().collect(joining(","));
	}

	@Override
	public void filter(final ContainerRequestContext request, final ContainerResponseContext response)
	{
		var httpHeaders = response.getHeaders();
		httpHeaders.add("Access-Control-Allow-Origin", "*");
		httpHeaders.add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
		httpHeaders.add("Access-Control-Allow-Credentials", "true");
		httpHeaders.add("Access-Control-Allow-Headers", headers);
	}
}
