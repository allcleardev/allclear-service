package app.allclear.google.client;

import static org.fest.assertions.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.*;

/** Functional test class that verifies the MapClient component.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

@Disabled	// Requires Google Map key in the environment variable which is NOT on the build server. DLS on 4/5/2020.
public class MapClientTest
{
	private static final MapClient client = new MapClient();

	@Test
	public void success()
	{
		var o = client.geocode("924 Willow Ave, Hoboken, NJ");
		assertThat(o.results).as("Check results.size").hasSize(1);

		var result = o.results.get(0);
		Assertions.assertEquals("924 Willow Ave, Hoboken, NJ 07030, USA", result.formattedAddress, "Check result.formattedAddress");
		Assertions.assertEquals("924", result.streetNumber().shortName, "Check result.streetNumber");
		Assertions.assertEquals("Willow Ave", result.streetName().shortName, "Check result.streetName");
		Assertions.assertEquals("Hoboken", result.city().shortName, "Check result.city");
		Assertions.assertEquals("Hudson County", result.county().shortName, "Check result.county");
		Assertions.assertEquals("New Jersey", result.state().longName, "Check result.state.longName");
		Assertions.assertEquals("NJ", result.state().shortName, "Check result.state.shortName");
		Assertions.assertEquals("United States", result.country().longName, "Check result.country.longName");
		Assertions.assertEquals("US", result.country().shortName, "Check result.country.shortName");
		Assertions.assertEquals("07030", result.postalCode().longName, "Check result.postalCode.longName");
		Assertions.assertEquals(new BigDecimal("40.7487855"), result.geometry.location.lat, "Check result.geometry.location.lat");
		Assertions.assertEquals(new BigDecimal("-74.0315385"), result.geometry.location.lng, "Check result.geometry.location.lng");
	}
}
