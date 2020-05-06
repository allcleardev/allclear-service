package app.allclear.platform.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.resources.Headers;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.filter.SessionFilter;
import app.allclear.platform.value.SessionValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the SessionDAO.
*
*	@author smalleyd
*	@version 1.0.14
*	@since April 1, 2020
*
**********************************************************************************/

@Path("/sessions")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Session")
public class SessionResource
{
	private final SessionDAO dao;

	public SessionResource(final SessionDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Timed
	@ApiOperation(value="get", notes="Gets the user's current session.", response=SessionValue.class)
	public SessionValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId)
	{
		return dao.get();
	}

	@GET
	@Path("/{id}") @Timed
	@ApiOperation(value="get", notes="Gets the specified session.", response=SessionValue.class)
	public SessionValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ObjectNotFoundException
	{
		dao.checkAdmin();

		var o = dao.find(id);
		if (null == o) throw new ObjectNotFoundException();

		return o;
	}

	@DELETE
	@Timed
	@ApiOperation(value="remove", notes="Removes the current user's session - logout.")
	public Response remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId)
	{
		dao.remove();

		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}") @Timed
	@ApiOperation(value="remove", notes="Removes the specified session.")
	public Response remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id)
	{
		dao.checkAdmin();
		dao.remove(id);

		return Response.ok().build();
	}

	@POST
	@Path("/search") @Timed
	@ApiOperation(value="search", notes="Search/scan the user sessions.")
	public QueryResults<SessionValue, SessionFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final SessionFilter filter)
	{
		dao.checkAdmin();

		return dao.search(filter);
	}
}
