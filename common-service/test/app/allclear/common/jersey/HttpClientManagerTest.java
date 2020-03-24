package app.allclear.common.jersey;

import org.junit.*;

import app.allclear.common.entity.Country;
import app.allclear.common.entity.User;
import app.allclear.common.value.OperationResponse;

/** Functional test class that verifies the HttpClientManager.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class HttpClientManagerTest
{
	@Test
	public void testDefaultImplementation() throws Exception
	{
		var m = new DefaultHttpClientManager();

		m.start();

		HttpClientTestFilter.setResponse(new User("test@jibe.com", null, null, null, null));
		var user = m.getUser("user1", true);
		Assert.assertNotNull("Exists", user);
		Assert.assertEquals("Check email", "test@jibe.com", user.email);

		HttpClientTestFilter.clearResponse();
		m.activateUser("user2");
		m.deactivateUser("user3");
		m.stop();
	}

	@Test
	public void testAltKeyImplementation() throws Exception
	{
		var m = new AltKeyHttpClientManager("access-xyz", "secure-890");

		m.start();
		m.postCountry(new Country("US", "United States", "USA", "003"));
		m.findCountriesByPartialNameSearch("land");
		m.stop();
	}

	@Test
	public void testNoKeyImplementation() throws Exception
	{
		var m = new NoKeyHttpClientManager();

		m.start();
		m.deleteCountry("UK");
		m.deleteCountry(new Country("CA", "Canada", "CAN", "405"));
		m.stop();
	}

	@Test
	public void testResponsesQueue() throws Exception
	{
		var m = new DefaultHttpClientManager();

		m.start();

		HttpClientTestFilter.addResponses(new User("test@jibe.com", null, null, null, null), OperationResponse.SUCCESS, new OperationResponse("failed"));

		Assert.assertEquals("Check user", "test@jibe.com", m.getUser("123", true).email);
		Assert.assertTrue("Check success", m.activateUser("123").operation);

		var o = m.deactivateUser("123");
		Assert.assertFalse("Check deactivateUser: operation", o.operation);
		Assert.assertEquals("Check deactivateUser: message", "failed", o.message);

		Assert.assertNull("Response queue is empty.", m.getUser("123", true));
	}
}
