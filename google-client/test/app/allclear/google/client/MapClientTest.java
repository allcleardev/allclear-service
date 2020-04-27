package app.allclear.google.client;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.allclear.redis.FakeJedisPool;

/** Functional test class that verifies the MapClient component.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

// @Disabled	// Requires Google Map key in the environment variable which is NOT on the build server. DLS on 4/5/2020.
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class MapClientTest
{
	private static final MapClient client = new MapClient(new FakeJedisPool());

	public static Stream<Arguments> success()
	{
		// Alternates for the cache.
		return Stream.of(arguments("924 Willow Ave, Hoboken, NJ", 0, 1L),
			arguments("924 willow ave, hoboken, nj", 1, 1L),
			arguments("924 WILLOW AVE, HOBOKEN, NJ", 2, 1L),
			arguments("924 willow ave, hoboken, new jersey", 2, 2L),
			arguments("924 willow avenue, hoboken, nj", 2, 3L),
			arguments("924 Willow AVE, Hoboken, NJ", 3, 3L),
			arguments("924 willow ave, hoboken, New Jersey", 4, 3L),
			arguments("924 willow avenue, hoboken, nj", 5, 3L));
	}

	@Test
	public void before()
	{
		Assertions.assertEquals(0L, client.cacheSize(), "Check cacheSize");
		Assertions.assertEquals(0, client.geocodeCacheHits(), "Check geocodeCacheHits");
	}

	@ParameterizedTest
	@MethodSource
	public void success(final String address, final int geocodeCacheHits, final long cacheSize)
	{
		var o = client.geocode(address);
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

		Assertions.assertEquals(cacheSize, client.cacheSize(), "Check cacheSize");
		Assertions.assertEquals(geocodeCacheHits, client.geocodeCacheHits(), "Chek geocodeCacheHits");
	}

	@Test
	public void success_00()
	{
		var o = client.geocode("20200 54th Ave W, Lynnwood, WA 98036");
		Assertions.assertTrue(o.ok(), "Check ok");
	}

	@Test
	public void success_00_check()
	{
		Assertions.assertEquals(4L, client.cacheSize(), "Check cacheSize");
		Assertions.assertEquals(5, client.geocodeCacheHits());	// No change
	}
}
