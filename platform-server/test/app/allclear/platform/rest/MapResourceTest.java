package app.allclear.platform.rest;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static app.allclear.testing.TestingUtils.*;

import java.math.BigDecimal;
import java.util.stream.Stream;
import javax.ws.rs.client.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.common.errors.ErrorInfo;
import app.allclear.common.errors.NotFoundExceptionMapper;
import app.allclear.common.errors.ValidationExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.google.client.MapClient;
import app.allclear.google.model.GeocodeResponse;
import app.allclear.platform.fcc.GeoResponse;
import app.allclear.platform.model.GeocodedResponse;

/**********************************************************************************
*
*	Functional test for the Jersey RESTful resource MapResource.
*
*	@author smalleyd
*	@version 1.0.97
*	@since April 10, 2020
*
**********************************************************************************/

@TestMethodOrder(MethodOrderer.Alphanumeric.class)	// Ensure that the methods are executed in order listed.
@ExtendWith(DropwizardExtensionsSupport.class)
public class MapResourceTest
{
	private static MapClient map = mock(MapClient.class);

	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new NotFoundExceptionMapper())
		.addResource(new ValidationExceptionMapper())
		.addResource(new MapResource(map)).build();

	/** Primary URI to test. */
	private static final String TARGET = "/maps";

	@BeforeAll
	public static void up() throws Exception
	{
		when(map.geocode(contains("Street"))).thenReturn(loadObject("/google/map/geocode.json", GeocodeResponse.class));
		when(map.geocode(contains("Avenue"))).thenReturn(loadObject("/google/map/geocode-requestDenied.json", GeocodeResponse.class));
		when(map.geocode(contains("Lane"))).thenReturn(loadObject("/google/map/geocode-zeroResults.json", GeocodeResponse.class));
	}

	@ParameterizedTest
	@CsvSource({"29.3066829,-94.7970101,galveston,",
	            "40.7342964,-74.056983,jersey_city,'FCC0001: The coordinate lies on the boundary of mulitple blocks, the block contains the clicked location is selected. For a complete list use showall=true to display ''intersection'' element in the Block'"})
	public void block(final String latitude, final String longitude, final String expected, final String message) throws Exception
	{
		var expected_ = loadObject("/fcc/block-" + expected + ".json", GeoResponse.class);
		var response = request(target().path("block").queryParam("latitude", latitude).queryParam("longitude", longitude)).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var value = response.readEntity(GeoResponse.class);
		Assertions.assertNotNull(value, "Exists");
		Assertions.assertEquals(expected_, value, "Check value");
		Assertions.assertEquals(message, value.message(), "Check message");
	}

	@ParameterizedTest
	@CsvSource({"90.3066829,-94.7970101,Latitude '90.3066829' is greater than the accepted value of 90.",
	            "29.3066829,-180.7970101,Longitude '-180.7970101' is less than the accepted value of -180."})
	public void block_fail(final String latitude, final String longitude, final String message)
	{
		var response = request(target().path("block").queryParam("latitude", latitude).queryParam("longitude", longitude)).get();
		Assertions.assertEquals(HTTP_STATUS_VALIDATION_EXCEPTION, response.getStatus(), "Status");

		var error = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(error, "Exists");
		Assertions.assertEquals(message, error.message, "Check message");
	}

	@Test
	public void geocode()
	{
		var response = request(target().path("geocode").queryParam("location", "First Street")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var r = response.readEntity(GeocodedResponse.class);
		Assertions.assertNotNull(r, "Exists");
		Assertions.assertEquals("924", r.streetNumber, "Check result.streetNumber");
		Assertions.assertEquals("Willow Ave", r.streetName, "Check result.streetName.shortName");
		Assertions.assertEquals("Hoboken", r.city, "Check result.city");
		Assertions.assertEquals("Hudson County", r.county, "Check result.county");
		Assertions.assertEquals("New Jersey", r.state, "Check result.state.longName");
		Assertions.assertEquals("US", r.country, "Check result.country.shortName");
		Assertions.assertEquals("07030", r.postalCode, "Check result.postalCode");
		Assertions.assertEquals(new BigDecimal("40.7487855"), r.latitude, "Check result.geometry.location.lat");
		Assertions.assertEquals(new BigDecimal("-74.0315385"), r.longitude, "Check result.geometry.location.lng");
	}

	public static Stream<Arguments> geocode_failure()
	{
		return Stream.of(
			arguments("", HTTP_STATUS_VALIDATION_EXCEPTION, "Must provide the 'location' query parameter."),
			arguments(null, HTTP_STATUS_VALIDATION_EXCEPTION, "Must provide the 'location' query parameter."),
			arguments("First Lane", HTTP_STATUS_NOT_FOUND, "The location 'First Lane' could not be Geocoded."),
			arguments("First Avenue", HTTP_STATUS_VALIDATION_EXCEPTION, "This API project is not authorized to use this API."));
	}

	@ParameterizedTest
	@MethodSource
	public void geocode_failure(final String location, final int status, final String message)
	{
		var response = request(target().path("geocode").queryParam("location", location)).get();
		Assertions.assertEquals(status, response.getStatus(), "Status");

		var error = response.readEntity(ErrorInfo.class);
		Assertions.assertNotNull(error, "Exists");
		Assertions.assertEquals(message, error.message, "Check message");
	}

	private WebTarget target() { return RULE.client().target(TARGET); }
	@SuppressWarnings("unused") private Invocation.Builder request(final String path) { return request(target().path(path)); }
	private Invocation.Builder request(final WebTarget target) { return target.request(UTF8MediaType.APPLICATION_JSON_TYPE); }
}
