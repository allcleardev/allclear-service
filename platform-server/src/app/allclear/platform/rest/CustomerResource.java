package app.allclear.platform.rest;

import java.util.List;

import javax.ws.rs.*;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.resources.Headers;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.CustomerDAO;
import app.allclear.platform.filter.CustomerFilter;
import app.allclear.platform.value.CustomerValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the CustomerDAO.
*
*	@author smalleyd
*	@version 1.1.0
*	@since April 26, 2020
*
**********************************************************************************/

@Path("/customers")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Customer")
public class CustomerResource
{
	private final CustomerDAO dao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public CustomerResource(final CustomerDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Path("/{id}") @Timed
	@ApiOperation(value="get", notes="Gets a single Customer by its primary key.", response=CustomerValue.class)
	public CustomerValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ObjectNotFoundException
	{
		return dao.getByIdWithException(id);
	}

	@GET
	@Timed
	@ApiOperation(value="find", notes="Finds Customers by wildcard name search.", response=CustomerValue.class, responseContainer="List")
	public List<CustomerValue> find(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name)
	{
		return dao.getByName(name);
	}

	@POST
	@Timed
	@ApiOperation(value="add", notes="Adds a single Customer. Returns the supplied Customer value with the auto generated identifier populated.", response=CustomerValue.class)
	public CustomerValue add(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final CustomerValue value) throws ValidationException
	{
		return dao.add(value);
	}

	@PUT
	@Timed
	@ApiOperation(value="set", notes="Updates an existing single Customer. Returns the supplied Customer value with the auto generated identifier populated.", response=CustomerValue.class)
	public CustomerValue set(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final CustomerValue value) throws ValidationException
	{
		return dao.update(value);
	}

	@DELETE
	@Path("/{id}") @Timed
	@ApiOperation(value="remove", notes="Removes/deactivates a single Customer by its primary key.", response=OperationResponse.class)
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ValidationException
	{
		return new OperationResponse(dao.remove(id));
	}

	@POST
	@Path("/search") @Timed
	@ApiOperation(value="search", notes="Searches the Customers based on the supplied filter.", response=QueryResults.class)
	public QueryResults<CustomerValue, CustomerFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final CustomerFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
