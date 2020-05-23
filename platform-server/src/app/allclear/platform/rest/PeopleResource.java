package app.allclear.platform.rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.azure.storage.queue.QueueClient;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.errors.Validator;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.resources.Headers;
import app.allclear.common.value.CountResults;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.*;
import app.allclear.platform.entity.Named;
import app.allclear.platform.filter.PeopleFilter;
import app.allclear.platform.model.*;
import app.allclear.platform.type.Visibility;
import app.allclear.platform.value.*;

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
	private final QueueClient alertQueue;
	private final RegistrationDAO registrationDao;
	private final ObjectMapper mapper = JacksonUtils.createMapperAzure();

	/** Populator.
	 * 
	 * @param dao
	 */
	public PeopleResource(final PeopleDAO dao, final RegistrationDAO registrationDao, final SessionDAO sessionDao, final QueueClient alertQueue)
	{
		this.dao = dao;
		this.alertQueue = alertQueue;
		this.sessionDao = sessionDao;
		this.registrationDao = registrationDao;
	}

	@GET
	@Path("/{id}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single People by its primary key.", response=PeopleValue.class)
	public PeopleValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ObjectNotFoundException
	{
		var o = sessionDao.checkAdminOrPerson();	// Only admins can see other application users.
		if (o.admin() || o.person.id.equals(id)) return dao.getByIdWithException(id);

		return dao.getFriend(o.person.id, id);
	}

	@GET
	@Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds Peoples by wildcard name search.", response=PeopleValue.class, responseContainer="List")
	public List<PeopleValue> find(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name)
	{
		var o = sessionDao.checkAdminOrPerson();	// Only admins can see other application users.
		if (o.person()) return List.of(dao.getByIdWithException(o.person.id));

		return dao.getActiveByIdOrName(name);
	}

	@GET
	@Path("/{id}/fields") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="getField", notes="Gets a single Person Field Access by its primary key.", response=PeopleFieldValue.class)
	public PeopleFieldValue getField(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ObjectNotFoundException
	{
		sessionDao.checkAdmin();	// Only admins can see other application user's field access.
		return dao.getFieldWithException(id);
	}

	@GET
	@Path("/fields") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="getField", notes="Gets the current user's Field Access.", response=PeopleFieldValue.class)
	public PeopleFieldValue getField(@HeaderParam(Headers.HEADER_SESSION) final String sessionId) throws ObjectNotFoundException
	{
		return dao.getFieldWithException(sessionDao.checkPerson().id);
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
	@Path("/{id}/alert") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="alert", notes="Sends a New Facility Alert to the specified user.")
	public Response alert(@PathParam("id") final String id) throws IOException, ObjectNotFoundException
	{
		sessionDao.checkAdmin();	// Only admins can manually trigger a New Facility Alert.

		alertQueue.sendMessage(mapper.writeValueAsString(new AlertRequest(dao.findWithException(id).getId())));

		return Response.ok().build();
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
	@Path("/{id}/auth") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="authenticate", notes="Authenticates the specified user on behalf of an Administrator.")
	public SessionValue authenticate(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") @ApiParam(name="id", value="Represents the person identifier.", required=true) final String id,
		@QueryParam("rememberMe") @ApiParam(name="rememberMe", value="Optionally, indicates to use a long term session.", required=false) @DefaultValue("false") final Boolean rememberMe) throws ObjectNotFoundException
	{
		sessionDao.checkAdmin();

		return sessionDao.add(dao.getByIdWithException(id), Boolean.TRUE.equals(rememberMe));
	}

	@POST
	@Path("/confirm") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="confirm", notes="Confirms the user phone number or email address during the registration process.", response=SessionValue.class)
	public SessionValue confirm(final StartResponse request) throws ValidationException
	{
		return sessionDao.add(registrationDao.confirm(request.phone, request.code));
	}

	@POST
	@Path("/facilities") @Timed @UnitOfWork
	@ApiOperation(value="addFacilities", notes="Associates one or more facilities with a person.", response=PeopleValue.class)
	public CountResults addFacilities(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final List<Long> facilityIds) throws ObjectNotFoundException, ValidationException
	{
		return new CountResults(dao.addFacilities(sessionDao.checkPerson().id, facilityIds));
	}

	@POST
	@Path("/{id}/facilities") @Timed @UnitOfWork
	@ApiOperation(value="addFacilities", notes="Associates one or more facilities with a person. Administrator usage only.", response=PeopleValue.class)
	public CountResults addFacilities(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id,
		final List<Long> facilityIds) throws ObjectNotFoundException, ValidationException
	{
		sessionDao.checkAdmin();	// Only admin can perform this operation on select users.

		return new CountResults(dao.addFacilities(id, facilityIds));
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
			throw new ValidationException("An account with that phone number already exists. Please sign in above instead.");

		registrationDao.start(request);

		return Response.ok().build();
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single People. Returns the supplied People value with the auto generated identifier populated.", response=PeopleValue.class)
	public PeopleValue set(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final PeopleValue value) throws ValidationException
	{
		var o = sessionDao.checkAdminOrPerson();
		if (o.person())	// Non-admins can only update themselves.
		{
			return sessionDao.update(o, dao.update(value.withId(o.person.id), false)).person;
		}

		return dao.update(value, true);
	}

	@PUT
	@Path("/fields") @Timed @UnitOfWork
	@ApiOperation(value="setField", notes="Updates an existing single People. Returns the supplied People value with the auto generated identifier populated.", response=PeopleValue.class)
	public PeopleFieldValue setField(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final PeopleFieldValue value) throws ObjectNotFoundException, ValidationException
	{
		var o = sessionDao.checkAdminOrPerson();
		if (o.person())	value.id = o.person.id;

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

	@PUT
	@Path("/confirm") @Timed @UnitOfWork	// USE /confirm so that AuthFilter permissions still work.
	@ApiOperation(value="confirm", notes="Finishes the new v2 user registration process. Confirms the user phone number or email address during the registration process.", response=SessionValue.class)
	public SessionValue confirm(@QueryParam("rememberMe") @ApiParam(name="rememberMe", value="Indicates to use a long term session if TRUE.", defaultValue="false", required=false) @DefaultValue("false") final Boolean rememberMe,
		final StartResponse request) throws ValidationException
	{
		return sessionDao.add(dao.add(registrationDao.confirm(request).registeredByPhone()),
			Boolean.TRUE.equals(rememberMe));
	}

	@PUT
	@Path("/start") @Timed @UnitOfWork(readOnly=true, transactional=false)	// USE /start so that AuthFilter permissions still work.
	@ApiOperation(value="start", notes="Starts the new v2 user registration process.")
	public Response start(final PeopleValue value) throws ValidationException
	{
		dao.validate(value.withId("TEMP123"));	// Set temporary ID so that it passes validation.

		if (dao.existsByPhone(value.normalize().phone))	// Make sure that the user phone number doesn't already exist.
			throw new ValidationException(Validator.CODE_ALREADY_REGISTERED, "phone", "An account with that phone number already exists. Please sign in above instead.");

		registrationDao.start(value.withId(null));	// Remove the temporary ID before caching.

		return Response.ok().build();
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
	@ApiOperation(value="remove", notes="Removes/deactivates a single People by its primary key.", response=OperationResponse.class)
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id) throws ValidationException
	{
		sessionDao.checkAdmin();	// Only admins can delete application users.

		return new OperationResponse(dao.remove(id));
	}

	@DELETE
	@Path("/facilities") @Timed @UnitOfWork
	@ApiOperation(value="removeFacilities", notes="Removes one or more facility associations from a person.", response=CountResults.class)
	public CountResults removeFacilities(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final List<Long> facilityIds) throws ObjectNotFoundException, ValidationException
	{
		return new CountResults(dao.removeFacilities(sessionDao.checkPerson().id, facilityIds));
	}

	@DELETE
	@Path("/{id}/facilities") @Timed @UnitOfWork
	@ApiOperation(value="removeFacilities", notes="Removes one or more facility associations from a person. Administrator usage only.", response=CountResults.class)
	public CountResults removeFacilities(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final String id,
		final List<Long> facilityIds) throws ObjectNotFoundException, ValidationException
	{
		sessionDao.checkAdmin();	// Only admin can perform this operation on select users.

		return new CountResults(dao.removeFacilities(id, facilityIds));
	}

	@POST
	@Path("/find") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds a list of People names by their screen names or phone numbers. Provides a simplified search.", response=Named.class, responseContainer="List")
	public List<Named> find(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final PeopleFindRequest request) throws ValidationException
	{
		return dao.find(request);
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Peoples based on the supplied filter.", response=QueryResults.class)
	public QueryResults<PeopleValue, PeopleFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final PeopleFilter filter) throws ValidationException
	{
		var s = sessionDao.checkAdminOrPerson();
		if (s.person()) filter.who(Visibility.FRIENDS).withActive(true).withFriendshipId(s.person.id);	// Non-admins can only see their friends.

		return dao.search(filter);
	}
}
