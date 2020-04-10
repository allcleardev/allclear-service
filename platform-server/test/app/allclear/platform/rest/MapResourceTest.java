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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.common.errors.ErrorInfo;
import app.allclear.common.errors.NotFoundExceptionMapper;
import app.allclear.common.errors.ValidationExceptionMapper;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.google.client.MapClient;
import app.allclear.google.model.GeocodeResponse;
import app.allclear.google.model.GeocodeResult;

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

	@Test
	public void geocode()
	{
		var response = request(target().path("geocode").queryParam("location", "First Street")).get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		var r = response.readEntity(GeocodeResult.class);
		Assertions.assertNotNull(r, "Exists");
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
