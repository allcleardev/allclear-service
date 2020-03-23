package app.allclear.platform.rest;

import java.util.List;

import javax.ws.rs.*;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.PeopleDAO;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the PeopleDAO.
*
*	@author smalleyd
*	@version 1.0.0
*	@since March 23, 2020
*
**********************************************************************************/

@Path("/peoples")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="People")
public class PeopleResource
{
	private final PeopleDAO dao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public PeopleResource(final PeopleDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Path("/{id}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single People by its primary key.", response=PeopleValue.class)
	public PeopleValue get(@PathParam("id") final String id) throws ValidationException
	{
		return dao.getByIdWithException(id);
	}

	@GET
	@Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds Peoples by wildcard name search.", response=PeopleValue.class, responseContainer="List")
	public List<PeopleValue> find(@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name)
	{
		return dao.getActiveByIdOrName(name);
	}

	@POST
	@Timed @UnitOfWork
	@ApiOperation(value="add", notes="Adds a single People. Returns the supplied People value with the auto generated identifier populated.", response=PeopleValue.class)
	public PeopleValue add(final PeopleValue value) throws ValidationException
	{
		return dao.add(value);
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single People. Returns the supplied People value with the auto generated identifier populated.", response=PeopleValue.class)
	public PeopleValue set(final PeopleValue value) throws ValidationException
	{
		return dao.update(value);
	}

	@DELETE
	@Path("/{id}") @Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates a single People by its primary key.")
	public OperationResponse remove(@PathParam("id") final String id) throws ValidationException
	{
		return new OperationResponse(dao.remove(id));
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Peoples based on the supplied filter.", response=QueryResults.class)
	public QueryResults<PeopleValue, PeopleFilter> search(final PeopleFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
