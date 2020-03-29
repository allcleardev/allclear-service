package app.allclear.platform.rest;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.PeopleFilter;
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
	public PeopleValue get(@PathParam("id") final String id) throws ObjectNotFoundException
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

	@POST
	@Path("/auth") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="startAuth", notes="Starts the authentication process by sending a magic link to the user's phone number or email address.")
	public Response startAuth(@QueryParam("phone") @ApiParam(name="phone", value="If the phone number is provided, the magic link is sent via SMS.", required=false) final String phone,
		@QueryParam("email") @ApiParam(name="email", value="If the email address is provided, the magic link is sent via Email.", required=false) final String email)
			throws ValidationException
	{
		dao.check(phone, null);	// Do NOT use email for now as magic link via email has not been implememted yet. DLS on 3/28/2020.

		sessionDao.auth(phone);

		return Response.ok().build();
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single People. Returns the supplied People value with the auto generated identifier populated.", response=PeopleValue.class)
	public PeopleValue set(final PeopleValue value) throws ValidationException
	{
		return dao.update(value);
	}

	@PUT
	@Path("/auth") @Timed @UnitOfWork	// Authentication updates the Person record.
	@ApiOperation(value="finishAuth", notes="Confirms the authentication phone/email and token to provide a session to the caller.", response=SessionValue.class)
	public SessionValue finishAuth(@QueryParam("phone") @ApiParam(name="phone", value="The phone number to which the token was sent.", required=false) final String phone,
		@QueryParam("email") @ApiParam(name="email", value="The email address to which the token was sent.", required=false) final String email,
		@QueryParam("token") @ApiParam(name="token", value="The secret token sent to the account.", required=true) final String token,
		@QueryParam("rememberMe") @ApiParam(name="rememberMe", value="Indicates to use a long term session if TRUE.", required=false) final Boolean rememberMe)
			throws ObjectNotFoundException, ValidationException
	{
		if (StringUtils.isEmpty(phone)) throw new ValidationException("phone", "The phone number is required.");
		if (StringUtils.isEmpty(token)) throw new ValidationException("token", "The token is required.");

		sessionDao.auth(phone, token);

		return sessionDao.add(dao.authenticatedByPhone(phone), Boolean.TRUE.equals(rememberMe));
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
