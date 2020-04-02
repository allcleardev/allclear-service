package app.allclear.platform.rest;

import java.util.List;

import javax.ws.rs.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.resources.Headers;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.AdminDAO;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.filter.AdminFilter;
import app.allclear.platform.model.AuthenticationRequest;
import app.allclear.platform.value.AdminValue;
import app.allclear.platform.value.SessionValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the AdminDAO.
*
*	@author smalleyd
*	@version 1.0.14
*	@since April 1, 2020
*
**********************************************************************************/

@Path("/admins")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Admin")
public class AdminResource
{
	private static final Logger log = LoggerFactory.getLogger(AdminResource.class);

	private final AdminDAO dao;
	private final SessionDAO sessionDao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public AdminResource(final AdminDAO dao, final SessionDAO sessionDao)
	{
		this.dao = dao;
		this.sessionDao = sessionDao;

		log.info("INITIALIZED");
	}

	@GET
	@Path("/{id}") @Timed
	@ApiOperation(value="get", notes="Gets a single Admin by its primary key.", response=AdminValue.class)
	public AdminValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ValidationException
	{
		return dao.getByIdWithException(id);
	}

	@GET
	@Path("/self") @Timed
	@ApiOperation(value="get", notes="Gets the current user's Admin profile.", response=AdminValue.class)
	public AdminValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId) throws ValidationException
	{
		return dao.getByIdWithException(sessionDao.current().admin.id);
	}

	@GET
	@Timed
	@ApiOperation(value="find", notes="Finds Admins by wildcard name search.", response=AdminValue.class, responseContainer="List")
	public List<AdminValue> find(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name)
	{
		return null;	// dao.getActiveByIdOrName(name);
	}

	@POST
	@Timed
	@ApiOperation(value="add", notes="Adds a single Admin. Returns the supplied Admin value with the auto generated identifier populated.", response=AdminValue.class)
	public AdminValue add(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final AdminValue value) throws ValidationException
	{
		return dao.add(value);
	}

	@POST
	@Path("/auth") @Timed
	@ApiOperation(value="add", notes="Authenticates a user.", response=SessionValue.class)
	public SessionValue authenticate(final AuthenticationRequest request) throws ValidationException
	{
		if (null != sessionDao.current()) throw new ValidationException("You are already authenticated.");

		return sessionDao.add(dao.authenticate(request), request.rememberMe);
	}

	@PUT
	@Timed
	@ApiOperation(value="set", notes="Updates an existing single Admin. Returns the supplied Admin value with the auto generated identifier populated.", response=AdminValue.class)
	public AdminValue set(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final AdminValue value) throws ValidationException
	{
		return dao.update(value);
	}

	@PUT
	@Path("/self") @Timed
	@ApiOperation(value="add", notes="Updates the current user's Admin profile.", response=AdminValue.class)
	public AdminValue setSelf(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final AdminValue value) throws ValidationException
	{
		return dao.update(value.withId(sessionDao.current().admin.id));
	}

	@DELETE
	@Path("/{id}") @Timed
	@ApiOperation(value="remove", notes="Removes/deactivates a single Admin by its primary key.")
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ValidationException
	{
		return new OperationResponse(dao.remove(id));
	}

	@DELETE
	@Path("/self") @Timed
	@ApiOperation(value="remove", notes="Logs out the current Administrator.")
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId) throws ValidationException
	{
		sessionDao.remove();
		return OperationResponse.SUCCESS;
	}

	@POST
	@Path("/search") @Timed
	@ApiOperation(value="search", notes="Searches the Admins based on the supplied filter.", response=QueryResults.class)
	public QueryResults<AdminValue, AdminFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final AdminFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
