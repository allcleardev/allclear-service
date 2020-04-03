package app.allclear.platform.rest;

import static app.allclear.common.value.Constants.*;

import java.math.BigDecimal;
import java.util.List;

import javax.ws.rs.*;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.FacilityDAO;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.value.FacilityValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the FacilityDAO.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
**********************************************************************************/

@Path("/facilities")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Facility")
public class FacilityResource
{
	private final FacilityDAO dao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public FacilityResource(final FacilityDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Path("/{id}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single Facility by its primary key.", response=FacilityValue.class)
	public FacilityValue get(@PathParam("id") final Long id) throws ValidationException
	{
		return dao.getByIdWithException(id);
	}

	@GET
	@Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds Facilitys by wildcard name search.", response=FacilityValue.class, responseContainer="List")
	public List<FacilityValue> find(@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name,
		@QueryParam("latitude") @ApiParam(name="latitude", value="Optional, GEO latitude of the locaiton to be searched") final BigDecimal latitude,
		@QueryParam("longitude") @ApiParam(name="longitude", value="Optional, GEO longitude of the locaiton to be searched") final BigDecimal longitude,
		@QueryParam("miles") @ApiParam(name="miles", value="Optional, max miles from the GEO location to included in the search") final Integer miles,
		@QueryParam("km") @ApiParam(name="miles", value="Optional, max kilometers from the GEO location to included in the search") final Integer km)
	{
		if ((null != latitude) && (null != longitude) && ((null != miles) || (null != km)))
			return dao.getActiveByNameAndDistance(name, latitude, longitude, (null != miles) ? milesToMeters(miles) : kmToMeters(km));

		return dao.getActiveByName(name);
	}

	@POST
	@Timed @UnitOfWork
	@ApiOperation(value="add", notes="Adds a single Facility. Returns the supplied Facility value with the auto generated identifier populated.", response=FacilityValue.class)
	public FacilityValue add(final FacilityValue value) throws ValidationException
	{
		return dao.add(value);
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single Facility. Returns the supplied Facility value with the auto generated identifier populated.", response=FacilityValue.class)
	public FacilityValue set(final FacilityValue value) throws ValidationException
	{
		return dao.update(value);
	}

	@DELETE
	@Path("/{id}") @Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates a single Facility by its primary key.")
	public OperationResponse remove(@PathParam("id") final Long id) throws ValidationException
	{
		return new OperationResponse(dao.remove(id));
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Facilitys based on the supplied filter.", response=QueryResults.class)
	public QueryResults<FacilityValue, FacilityFilter> search(final FacilityFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
