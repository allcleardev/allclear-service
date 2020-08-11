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
import app.allclear.platform.dao.*;
import app.allclear.platform.filter.TestsFilter;
import app.allclear.platform.model.TestInitRequest;
import app.allclear.platform.model.TestReceiptRequest;
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
	private final PatientDAO patientDao;
	private final PeopleDAO peopleDao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public TestsResource(final TestsDAO dao, final PatientDAO patientDao, final PeopleDAO peopleDao)
	{
		this.dao = dao;
		this.patientDao = patientDao;
		this.peopleDao = peopleDao;
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
	@Path("/{facilityId}/{remoteId}") @Timed @UnitOfWork
	@ApiOperation(value="getByFacilityAndRemoteId", notes="", response=TestsValue.class)
	public TestsValue getByFacilityAndRemoteId(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("facilityId") final Long facilityId,
		@PathParam("remoteId") final String remoteId)
	{
		return dao.getByFacilityAndRemoteId(facilityId, remoteId);
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

	@POST
	@Path("/init") @Timed @UnitOfWork
	@ApiOperation(value="init", notes="Initializes a pending Test with patient retrieval and potential enrollment.", response=TestsValue.class)
	public TestsValue init(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final TestInitRequest request) throws ValidationException
	{
		if (!request.valid()) throw new ValidationException("phone", "Please provide either a Person ID or a the patient phone number.");

		var personId = (null != request.personId) ? request.personId :
			peopleDao.getOrAdd(request.phone, request.firstName, request.lastName).getId();

		var patient = patientDao.enroll(request.facilityId, personId);

		if ((null == patient.getEnrolledAt()) && (null == patient.getRejectedAt()))	// Send enrollment SMS message.
		{
			
		}
		
		return dao.add(patient, request.typeId, request.identifier);
	}

	@POST
	@Path("/receive") @Timed @UnitOfWork
	@ApiOperation(value="receive", notes="Receives the Test results and optionally sends the notification to the patient.", response=TestsValue.class)
	public TestsValue receive(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final TestReceiptRequest request) throws ValidationException
	{
		return dao.receive(request);
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
	@ApiOperation(value="remove", notes="Removes/deactivates a single Tests by its primary key.", response=OperationResponse.class)
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
