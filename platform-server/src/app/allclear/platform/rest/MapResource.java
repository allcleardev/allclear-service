package app.allclear.platform.rest;

import javax.ws.rs.*;

import org.apache.commons.lang3.StringUtils;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;

import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.google.client.MapClient;
import app.allclear.google.model.*;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the Google Maps API.
*
*	@author smalleyd
*	@version 1.0.97
*	@since April 10, 2020
*
**********************************************************************************/

@Path("/maps")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Google Maps")
public class MapResource
{
	private final MapClient map;

	/** Populator.
	 * 
	 * @param dao
	 */
	public MapResource(final MapClient map)
	{
		this.map = map;
	}

	@GET
	@Path("/geocode") @Timed
	@ApiOperation(value="geocode", notes="Geocodes the supplied location via Google Maps.", response=GeocodeResult.class)
	public GeocodeResult geocode(@QueryParam("location") @ApiParam(name="location", value="Represents the location name to Geocode.", required=true) final String location)
		throws ObjectNotFoundException, ValidationException
	{
		if (StringUtils.isEmpty(location)) throw new ValidationException("location", "Must provide the 'location' query parameter.");

		var value = map.geocode(location);
		if (value.ok() && value.isNotEmpty()) return value.results.get(0);

		if (value.zeroResults()) throw new ObjectNotFoundException("The location '" + location + "' could not be Geocoded.");

		throw new ValidationException("location", value.errorMessage);
	}
}
