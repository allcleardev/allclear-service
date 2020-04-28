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
import app.allclear.platform.dao.FriendDAO;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.filter.FriendFilter;
import app.allclear.platform.value.FriendValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the FriendDAO.
*
*	@author smalleyd
*	@version 1.1.9
*	@since April 27, 2020
*
**********************************************************************************/

@Path("/friends")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Friend")
public class FriendResource
{
	private final FriendDAO dao;
	private final SessionDAO sessionDao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public FriendResource(final FriendDAO dao, final SessionDAO sessionDao)
	{
		this.dao = dao;
		this.sessionDao = sessionDao;
	}

	@GET
	@Path("/{personId}/{inviteeId}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single Friend by its primary key. For Administrators.", response=FriendValue.class)
	public FriendValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("personId") final String personId,
		@PathParam("inviteeId") final String inviteeId) throws ValidationException
	{
		sessionDao.checkAdmin();

		return dao.getByIdWithException(personId, inviteeId);
	}

	@GET
	@Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds Friends by wildcard name search.", response=FriendValue.class, responseContainer="List")
	public List<FriendValue> find(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name)
	{
		return null;	// dao.getActiveByIdOrName(name);
	}

	@POST
	@Timed @UnitOfWork
	@ApiOperation(value="add", notes="Adds a single Friend request. For Administrators.", response=FriendValue.class)
	public FriendValue add(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FriendValue value) throws ValidationException
	{
		sessionDao.checkAdmin();

		return dao.add(value);
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single Friend. For Administrators.", response=FriendValue.class)
	public FriendValue set(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FriendValue value) throws ValidationException
	{
		sessionDao.checkAdmin();

		return dao.update(value);
	}

	@DELETE
	@Path("/{personId}/{inviteeId}") @Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates a single Friend by its primary key. For Administrators.")
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("personId") final String personId,
		@PathParam("inviteeId") final String inviteeId) throws ValidationException
	{
		sessionDao.checkAdmin();

		return new OperationResponse(dao.remove(personId, inviteeId));
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Friends based on the supplied filter.", response=QueryResults.class)
	public QueryResults<FriendValue, FriendFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FriendFilter filter) throws ValidationException
	{
		var s = sessionDao.current();
		if (s.person())	filter.withUserId(s.person.id);	// A non-administrator can only see their requests that have not been rejected.

		return dao.search(filter);
	}
}
