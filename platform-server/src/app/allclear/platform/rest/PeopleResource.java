package app.allclear.platform.rest;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.resources.Headers;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.model.*;
import app.allclear.platform.value.PeopleValue;
import app.allclear.platform.value.SessionValue;

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
	private final SessionDAO sessionDao;
	private final RegistrationDAO registrationDao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public PeopleResource(final PeopleDAO dao, final RegistrationDAO registrationDao, final SessionDAO sessionDao)
	{
		this.dao = dao;
		this.sessionDao = sessionDao;
		this.registrationDao = registrationDao;
	}

	@GET
	@Path("/{id}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single People by its primary key.", response=PeopleValue.class)
	public PeopleValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ObjectNotFoundException
	{
		var o = sessionDao.current();	// Only admins can see other application users.
		if (o.person()) return o.person;

		return dao.getByIdWithException(id);
	}

	@GET
	@Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds Peoples by wildcard name search.", response=PeopleValue.class, responseContainer="List")
	public List<PeopleValue> find(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name)
	{
		var o = sessionDao.current();	// Only admins can see other application users.
		if (o.person()) return List.of(o.person);

		return dao.getActiveByIdOrName(name);
	}

	@POST
	@Timed @UnitOfWork
	@ApiOperation(value="add", notes="Adds a single People. Returns the supplied People value with the auto generated identifier populated.", response=PeopleValue.class)
	public PeopleValue add(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final PeopleValue value) throws ValidationException
	{
		sessionDao.checkAdmin();	// Only admins can add application users.

		return dao.add(value);
	}

	@POST
	@Path("/auth") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="startAuth", notes="Starts the authentication process by sending a magic link to the user's phone number or email address.")
	public Response startAuth(final AuthRequest request) throws ValidationException
	{
		dao.check(request.phone, null);	// Do NOT use email for now as magic link via email has not been implememted yet. DLS on 3/28/2020.

		sessionDao.auth(request.phone);

		return Response.ok().build();
	}

	@POST
	@Path("/confirm") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="confirm", notes="Confirms the user phone number or email address during the registration process.", response=SessionValue.class)
	public SessionValue confirm(final StartResponse request) throws ValidationException
	{
		return sessionDao.add(registrationDao.confirm(request.phone, request.code));
	}

	@POST
	@Path("/register") @Timed @UnitOfWork
	@ApiOperation(value="register", notes="Completes the user registration process.", response=SessionValue.class)
	public SessionValue register(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("rememberMe") @ApiParam(name="rememberMe", value="Indicates to use a long term session if TRUE.", defaultValue="false", required=false) @DefaultValue("false") final Boolean rememberMe,
		final PeopleValue value) throws ValidationException
	{
		return sessionDao.promote(dao.add(value.withPhone(sessionDao.get().registration.phone).registeredByPhone()),	// The AuthFilter ensures that this is a Registration session. DLS on 3/29/2020.
			Boolean.TRUE.equals(rememberMe));
	}

	@POST
	@Path("/start") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="start", notes="Starts the registration process by sending a magic link to the user's phone number or email address.")
	public Response start(final StartRequest request) throws ValidationException
	{
		if (dao.existsByPhone(request.phone))	// Make sure that the user phone number doesn't already exist.
			throw new ValidationException("An account with that phone number already exists. Pleaes proceed to authenticate instead.");

		registrationDao.start(request);

		return Response.ok().build();
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single People. Returns the supplied People value with the auto generated identifier populated.", response=PeopleValue.class)
	public PeopleValue set(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final PeopleValue value) throws ValidationException
	{
		var o = sessionDao.current();
		if (!o.admin())	// Non-admins can only update themselves.
		{
			return sessionDao.update(o, dao.update(value.withId(o.person.id))).person;
		}

		return dao.update(value);
	}

	@PUT
	@Path("/auth") @Timed @UnitOfWork	// Authentication updates the Person record.
	@ApiOperation(value="finishAuth", notes="Confirms the authentication phone/email and token to provide a session to the caller.", response=SessionValue.class)
	public SessionValue finishAuth(final AuthResponse request) throws ObjectNotFoundException, ValidationException
	{
		if (null == request.phone) throw new ValidationException("phone", "The phone number is required.");
		if (null == request.token) throw new ValidationException("token", "The token is required.");

		sessionDao.auth(request.phone, request.token);

		return sessionDao.add(dao.authenticatedByPhone(request.phone), request.rememberMe);
	}

	@DELETE
	@Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates the current user.")
	public Response remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId) throws ValidationException
	{
		dao.remove(sessionDao.checkPerson().id);
		sessionDao.remove();

		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}") @Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates a single People by its primary key.")
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ValidationException
	{
		sessionDao.checkAdmin();	// Only admins can delete application users.

		return new OperationResponse(dao.remove(id));
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Peoples based on the supplied filter.", response=QueryResults.class)
	public QueryResults<PeopleValue, PeopleFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final PeopleFilter filter) throws ValidationException
	{
		sessionDao.checkAdmin();	// Only admins can search on application users.

		return dao.search(filter);
	}
}
