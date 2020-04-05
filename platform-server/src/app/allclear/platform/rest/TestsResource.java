package app.allclear.platform.rest;

import java.util.List;

import javax.ws.rs.*;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.resources.Headers;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.TestsDAO;
import app.allclear.platform.filter.TestsFilter;
import app.allclear.platform.value.TestsValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the TestsDAO.
*
*	@author smalleyd
*	@version 1.0.44
*	@since April 4, 2020
*
**********************************************************************************/

@Path("/tests")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Tests")
public class TestsResource
{
	private final TestsDAO dao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public TestsResource(final TestsDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Path("/{id}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single Tests by its primary key.", response=TestsValue.class)
	public TestsValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final Long id) throws ValidationException
	{
		return dao.getByIdWithException(id);
	}

	@GET
	@Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds Tests by wildcard name search.", response=TestsValue.class, responseContainer="List")
	public List<TestsValue> find(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("personId") @ApiParam(name="personId", value="Represents the person for whom to retrieve the tests.") final String personId)
	{
		return dao.getByPerson(personId);
	}

	@POST
	@Timed @UnitOfWork
	@ApiOperation(value="add", notes="Adds a single Tests. Returns the supplied Tests value with the auto generated identifier populated.", response=TestsValue.class)
	public TestsValue add(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final TestsValue value) throws ValidationException
	{
		return dao.add(value);
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single Tests. Returns the supplied Tests value with the auto generated identifier populated.", response=TestsValue.class)
	public TestsValue set(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final TestsValue value) throws ValidationException
	{
		return dao.update(value);
	}

	@DELETE
	@Path("/{id}") @Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates a single Tests by its primary key.")
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final Long id) throws ValidationException
	{
		return new OperationResponse(dao.remove(id));
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Tests based on the supplied filter.", response=QueryResults.class)
	public QueryResults<TestsValue, TestsFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final TestsFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
