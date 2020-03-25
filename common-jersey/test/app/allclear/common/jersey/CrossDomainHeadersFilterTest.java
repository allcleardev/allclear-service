package app.allclear.common.jersey;

import static javax.ws.rs.core.HttpHeaders.*;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.ws.rs.container.*;
import javax.ws.rs.core.AbstractMultivaluedMap;

import org.junit.*;

/** Functional test class that verifies the CrossDomainHeadersFilter Jersey resource.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class CrossDomainHeadersFilterTest
{
	private static class SimpleMap extends AbstractMultivaluedMap<String, Object>
	{
		public SimpleMap()
		{
			super(new HashMap<String, List<Object>>());
		}
	}

	private static final String ALLOWED = "Origin,X-Requested-With," + ACCEPT + "," + ACCEPT_LANGUAGE + "," + AUTHORIZATION + "," + CONTENT_TYPE;

	private static final SimpleMap headers = new SimpleMap();
	private static final ContainerResponseContext response = mock(ContainerResponseContext.class);

	@BeforeClass
	public static void up()
	{
		when(response.getHeaders()).thenReturn(headers);
	}

	@Before
	public void beforeEach()
	{
		headers.clear();
	}

	@Test
	public void testEmpty()
	{
		new CrossDomainHeadersFilter().filter(null, response);
		check("");
	}

	@Test
	public void testOne()
	{
		new CrossDomainHeadersFilter("X-Jibe-One").filter(null, response);
		check(",X-Jibe-One");
	}

	@Test
	public void testTwo()
	{
		new CrossDomainHeadersFilter("X-Jibe-One", "X-Jibe_Two").filter(null, response);
		check(",X-Jibe-One,X-Jibe_Two");
	}

	@Test
	public void testThree()
	{
		new CrossDomainHeadersFilter("a", "b", "c").filter(null, response);
		check(",a,b,c");
	}

	@Test
	public void testFour()
	{
		new CrossDomainHeadersFilter("1", "2", "3", "4").filter(null, response);
		check(",1,2,3,4");
	}

	@Test
	public void testFive()
	{
		new CrossDomainHeadersFilter("X-Jibe-One", "X-Jibe_Two", "X-Jibe_a", "X-Jibe_b", "X-Jibe_c").filter(null, response);
		check(",X-Jibe-One,X-Jibe_Two,X-Jibe_a,X-Jibe_b,X-Jibe_c");
	}

	private void check(final String allowed)
	{
		Assert.assertEquals("Check Access-Control-Allow-Origin", "*", headers.getFirst("Access-Control-Allow-Origin"));
		Assert.assertEquals("Check Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS", headers.getFirst("Access-Control-Allow-Methods"));
		Assert.assertEquals("Check Access-Control-Allow-Credentials", "true", headers.getFirst("Access-Control-Allow-Credentials"));
		Assert.assertEquals("Check Access-Control-Allow-Headers", ALLOWED + allowed, headers.getFirst("Access-Control-Allow-Headers"));
	}
}
