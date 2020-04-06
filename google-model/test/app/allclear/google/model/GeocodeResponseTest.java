package app.allclear.google.model;

import static org.fest.assertions.api.Assertions.assertThat;
import static app.allclear.testing.TestingUtils.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.*;

/** Unit test class that verifies the GeocodeResponse POJO.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 
 *
 */

public class GeocodeResponseTest
{
	@Test
	public void geocode() throws Exception
	{
		var o = loadObject("/map/geocode.json", GeocodeResponse.class);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertTrue(o.ok(), "Check ok");
		Assertions.assertFalse(o.zeroResults(), "Check zeroResults");
		Assertions.assertFalse(o.overDailyLimit(), "Check overDailyLimit");
		Assertions.assertFalse(o.overQueryLimit(), "Check overQueryLimit");
		Assertions.assertFalse(o.requestDenied(), "Check requestDenied");
		Assertions.assertFalse(o.invalidRequest(), "Check invalidRequest");
		Assertions.assertFalse(o.unknownError(), "Check unknownError");
		Assertions.assertNull(o.errorMessage, "Check errorMessage");

		assertThat(o.results).as("Check results.size").hasSize(1);

		var r = o.results.get(0);
		Assertions.assertEquals("924", r.streetNumber().shortName, "Check result.streetNumber");
		Assertions.assertEquals("Willow Avenue", r.streetName().longName, "Check result.streetName.longName");
		Assertions.assertEquals("Willow Ave", r.streetName().shortName, "Check result.streetName.shortName");
		Assertions.assertEquals("Hoboken", r.city().shortName, "Check result.city");
		Assertions.assertEquals("Hudson County", r.county().shortName, "Check result.county");
		Assertions.assertEquals("New Jersey", r.state().longName, "Check result.state.longName");
		Assertions.assertEquals("NJ", r.state().shortName, "Check result.state.shortName");
		Assertions.assertEquals("United States", r.country().longName, "Check result.country.longName");
		Assertions.assertEquals("US", r.country().shortName, "Check result.country.shortName");
		Assertions.assertEquals("07030", r.postalCode().shortName, "Check result.postalCode");

		var l = r.geometry.location;
		Assertions.assertEquals(new BigDecimal("40.7487855"), l.lat, "Check result.geometry.location.lat");
		Assertions.assertEquals(new BigDecimal("-74.0315385"), l.lng, "Check result.geometry.location.lng");
	}

	@Test
	public void geocode_emptyAddressComponents() throws Exception
	{
		var o = loadObject("/map/geocode-emptyAddressComponents.json", GeocodeResponse.class);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertTrue(o.ok(), "Check ok");
		Assertions.assertFalse(o.zeroResults(), "Check zeroResults");
		Assertions.assertFalse(o.overDailyLimit(), "Check overDailyLimit");
		Assertions.assertFalse(o.overQueryLimit(), "Check overQueryLimit");
		Assertions.assertFalse(o.requestDenied(), "Check requestDenied");
		Assertions.assertFalse(o.invalidRequest(), "Check invalidRequest");
		Assertions.assertFalse(o.unknownError(), "Check unknownError");
		Assertions.assertNull(o.errorMessage, "Check errorMessage");

		assertThat(o.results).as("Check results.size").hasSize(1);

		var r = o.results.get(0);
		Assertions.assertNull(r.streetNumber().shortName, "Check result.streetNumber");
		Assertions.assertNull(r.streetName().longName, "Check result.streetName.longName");
		Assertions.assertNull(r.streetName().shortName, "Check result.streetName.shortName");
		Assertions.assertNull(r.city().shortName, "Check result.city");
		Assertions.assertNull(r.county().shortName, "Check result.county");
		Assertions.assertNull(r.state().longName, "Check result.state.longName");
		Assertions.assertNull(r.state().shortName, "Check result.state.shortName");
		Assertions.assertNull(r.country().longName, "Check result.country.longName");
		Assertions.assertNull(r.country().shortName, "Check result.country.shortName");
		Assertions.assertNull(r.postalCode().shortName, "Check result.postalCode");

		var l = r.geometry.location;
		Assertions.assertEquals(new BigDecimal("40.7487855"), l.lat, "Check result.geometry.location.lat");
		Assertions.assertEquals(new BigDecimal("-74.0315385"), l.lng, "Check result.geometry.location.lng");
	}

	@Test
	public void geocode_nullAddressComponents() throws Exception
	{
		var o = loadObject("/map/geocode-nullAddressComponents.json", GeocodeResponse.class);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertTrue(o.ok(), "Check ok");
		Assertions.assertFalse(o.zeroResults(), "Check zeroResults");
		Assertions.assertFalse(o.overDailyLimit(), "Check overDailyLimit");
		Assertions.assertFalse(o.overQueryLimit(), "Check overQueryLimit");
		Assertions.assertFalse(o.requestDenied(), "Check requestDenied");
		Assertions.assertFalse(o.invalidRequest(), "Check invalidRequest");
		Assertions.assertFalse(o.unknownError(), "Check unknownError");
		Assertions.assertNull(o.errorMessage, "Check errorMessage");

		assertThat(o.results).as("Check results.size").hasSize(1);

		var r = o.results.get(0);
		Assertions.assertNull(r.streetNumber().shortName, "Check result.streetNumber");
		Assertions.assertNull(r.streetName().longName, "Check result.streetName.longName");
		Assertions.assertNull(r.streetName().shortName, "Check result.streetName.shortName");
		Assertions.assertNull(r.city().shortName, "Check result.city");
		Assertions.assertNull(r.county().shortName, "Check result.county");
		Assertions.assertNull(r.state().longName, "Check result.state.longName");
		Assertions.assertNull(r.state().shortName, "Check result.state.shortName");
		Assertions.assertNull(r.country().longName, "Check result.country.longName");
		Assertions.assertNull(r.country().shortName, "Check result.country.shortName");
		Assertions.assertNull(r.postalCode().shortName, "Check result.postalCode");

		var l = r.geometry.location;
		Assertions.assertEquals(new BigDecimal("40.7487855"), l.lat, "Check result.geometry.location.lat");
		Assertions.assertEquals(new BigDecimal("-74.0315385"), l.lng, "Check result.geometry.location.lng");
	}

	@Test
	public void geocode_requestDenied() throws Exception
	{
		var o = loadObject("/map/geocode-requestDenied.json", GeocodeResponse.class);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertFalse(o.ok(), "Check ok");
		Assertions.assertFalse(o.zeroResults(), "Check zeroResults");
		Assertions.assertFalse(o.overDailyLimit(), "Check overDailyLimit");
		Assertions.assertFalse(o.overQueryLimit(), "Check overQueryLimit");
		Assertions.assertTrue(o.requestDenied(), "Check requestDenied");
		Assertions.assertFalse(o.invalidRequest(), "Check invalidRequest");
		Assertions.assertFalse(o.unknownError(), "Check unknownError");
		Assertions.assertEquals("This API project is not authorized to use this API.", o.errorMessage, "Check errorMessage");

		assertThat(o.results).as("Check results.size").hasSize(0);
	}

	@Test
	public void geocode_zeroResults() throws Exception
	{
		var o = loadObject("/map/geocode-zeroResults.json", GeocodeResponse.class);
		Assertions.assertNotNull(o, "Exists");
		Assertions.assertFalse(o.ok(), "Check ok");
		Assertions.assertTrue(o.zeroResults(), "Check zeroResults");
		Assertions.assertFalse(o.overDailyLimit(), "Check overDailyLimit");
		Assertions.assertFalse(o.overQueryLimit(), "Check overQueryLimit");
		Assertions.assertFalse(o.requestDenied(), "Check requestDenied");
		Assertions.assertFalse(o.invalidRequest(), "Check invalidRequest");
		Assertions.assertFalse(o.unknownError(), "Check unknownError");
		Assertions.assertNull(o.errorMessage, "Check errorMessage");

		assertThat(o.results).as("Check results.size").hasSize(0);
	}
}
