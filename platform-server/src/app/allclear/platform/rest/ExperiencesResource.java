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
import app.allclear.platform.dao.ExperiencesDAO;
import app.allclear.platform.filter.ExperiencesFilter;
import app.allclear.platform.model.ExperiencesCalcResponse;
import app.allclear.platform.model.ExperiencesLimitResponse;
import app.allclear.platform.value.ExperiencesValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the ExperiencesDAO.
*
*	@author smalleyd
*	@version 1.1.80
*	@since June 2, 2020
*
**********************************************************************************/

@Path("/experiences")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Experiences")
public class ExperiencesResource
{
	private final ExperiencesDAO dao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public ExperiencesResource(final ExperiencesDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Path("/{id}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single Experiences by its primary key.", response=ExperiencesValue.class)
	public ExperiencesValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final Long id) throws ObjectNotFoundException, ValidationException
	{
		return dao.getByIdWithException(id);
	}

	@GET
	@Path("/calc") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="calcByFacility", notes="Aggregates Experiences facets for the specified facility.", response=ExperiencesCalcResponse.class)
	public ExperiencesCalcResponse calc(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("facilityId") final Long facilityId) throws ValidationException
	{
		return dao.calcByFacility(facilityId);
	}

	@GET
	@Path("/limit") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="limit", notes="Checks the specified facility for potential rate limits reached by the current user. Throws exception if one of the limits has been reached.", response=ExperiencesLimitResponse.class)
	public ExperiencesLimitResponse limit(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("facilityId") final Long facilityId) throws ValidationException
	{
		return dao.limit(facilityId);
	}

	@POST
	@Timed @UnitOfWork
	@ApiOperation(value="add", notes="Adds a single Experiences. Returns the supplied Experiences value with the auto generated identifier populated.", response=ExperiencesValue.class)
	public ExperiencesValue add(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final ExperiencesValue value) throws ValidationException
	{
		return dao.add(value);
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single Experiences. Returns the supplied Experiences value with the auto generated identifier populated.", response=ExperiencesValue.class)
	public ExperiencesValue set(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final ExperiencesValue value) throws ObjectNotFoundException, ValidationException
	{
		return dao.update(value);
	}

	@DELETE
	@Path("/{id}") @Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates a single Experiences by its primary key.", response=OperationResponse.class)
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final Long id) throws ValidationException
	{
		return new OperationResponse(dao.remove(id));
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Experiencess based on the supplied filter.", response=QueryResults.class)
	public QueryResults<ExperiencesValue, ExperiencesFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final ExperiencesFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
