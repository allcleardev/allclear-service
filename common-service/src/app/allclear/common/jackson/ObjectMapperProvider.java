package app.allclear.common.jackson;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Jersey context-resolver to retrieves the default Jackson ObjectMapper to be used for RESTful marshalling
 *  of requests and responses.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ObjectMapperProvider implements ContextResolver<ObjectMapper>
{
	private final ObjectMapper mapper;

	public ObjectMapperProvider()
	{
		mapper = JacksonUtils.createMapper();
	}

	@Override
	public ObjectMapper getContext(final Class<?> type) { return mapper; }
}
