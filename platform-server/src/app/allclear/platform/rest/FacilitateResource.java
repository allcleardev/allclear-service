package app.allclear.platform.rest;

import javax.ws.rs.*;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.resources.Headers;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.FacilitateDAO;
import app.allclear.platform.filter.FacilitateFilter;
import app.allclear.platform.value.FacilitateValue;
import app.allclear.platform.value.FacilityValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the FacilitateDAO.
*
*	@author smalleyd
*	@version 1.1.61
*	@since 5/23/2020
*
**********************************************************************************/

@Path("/facilitates")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Facilitate")
public class FacilitateResource
{
	private final FacilitateDAO dao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public FacilitateResource(final FacilitateDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Path("/{statusId}/{createdAt}") @Timed
	@ApiOperation(value="get", notes="Gets a single Facilitate request by its primary key.", response=FacilitateValue.class)
	public FacilitateValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("statusId") final String statusId,
		@PathParam("createdAt") final String createdAt) throws ObjectNotFoundException
	{
		return dao.getByIdWithException(statusId, createdAt);
	}

	@POST
	@Path("/citizen") @Timed
	@ApiOperation(value="addByCitizen", notes="Adds a single request by a non-provider (citizen) for a new facility. Returns the supplied Facilitate value with the auto generated identifier populated.", response=FacilitateValue.class)
	public FacilitateValue addByCitizen(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FacilitateValue value) throws ValidationException
	{
		return dao.addByCitizen(value);
	}

	@POST
	@Path("/provider") @Timed
	@ApiOperation(value="addByProvider", notes="Adds a single request by a facility provider/owner for a new facility. Returns the supplied Facilitate value with the auto generated identifier populated.", response=FacilitateValue.class)
	public FacilitateValue addByProvider(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FacilitateValue value) throws ValidationException
	{
		return dao.addByProvider(value);
	}

	@POST
	@Path("/{statusId}/{createdAt}/promote") @Timed @UnitOfWork	// For Facility add/update.
	@ApiOperation(value="promote", notes="Promotes a single Facilitate request by its primary key.", response=FacilitateValue.class)
	public FacilitateValue promote(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("statusId") final String statusId,
		@PathParam("createdAt") final String createdAt,
		final FacilityValue value) throws ObjectNotFoundException, ValidationException
	{
		return dao.promote(statusId, createdAt, value);
	}

	@PUT
	@Path("/citizen") @Timed @UnitOfWork(readOnly=true, transactional=false)	// For potential Facility lookup.
	@ApiOperation(value="changeByCitizen", notes="Adds a single request by a non-provider (citizen) to change an existing facility. Returns the supplied Facilitate value with the auto generated identifier populated.", response=FacilitateValue.class)
	public FacilitateValue changeByCitizen(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FacilitateValue value) throws ValidationException
	{
		return dao.changeByCitizen(value);
	}

	@PUT
	@Path("/provider") @Timed @UnitOfWork(readOnly=true, transactional=false)	// For potential Facility lookup.
	@ApiOperation(value="changeByProvider", notes="Adds a single request by a facility provider/owner to change an existing facility. Returns the supplied Facilitate value with the auto generated identifier populated.", response=FacilitateValue.class)
	public FacilitateValue changeByProvider(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FacilitateValue value) throws ValidationException
	{
		return dao.changeByProvider(value);
	}

	@DELETE
	@Path("/{statusId}/{createdAt}/reject") @Timed
	@ApiOperation(value="reject", notes="Rejects a single Facilitate request by its primary key.", response=FacilitateValue.class)
	public FacilitateValue reject(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("statusId") final String statusId,
		@PathParam("createdAt") final String createdAt) throws ObjectNotFoundException
	{
		return dao.reject(statusId, createdAt);
	}

	@DELETE
	@Path("/{statusId}/{createdAt}") @Timed
	@ApiOperation(value="remove", notes="Removes/deactivates a single Facilitate by its primary key.", response=OperationResponse.class)
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("statusId") final String statusId,
		@PathParam("createdAt") final String createdAt) throws ValidationException
	{
		return new OperationResponse(dao.remove(statusId, createdAt));
	}

	@POST
	@Path("/search") @Timed
	@ApiOperation(value="search", notes="Searches the Facilitates based on the supplied filter.", response=QueryResults.class)
	public QueryResults<FacilitateValue, FacilitateFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FacilitateFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
