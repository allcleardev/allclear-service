package app.allclear.platform.rest;

import java.util.List;

import javax.ws.rs.*;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.OperationResponse;
import app.allclear.platform.dao.PatientDAO;
import app.allclear.platform.filter.PatientFilter;
import app.allclear.platform.value.PatientValue;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the PatientDAO.
*
*	@author smalleyd
*	@version 1.1.111
*	@since July 18, 2020
*
**********************************************************************************/

@Path("/patients")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Patient")
public class PatientResource
{
	private final PatientDAO dao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public PatientResource(final PatientDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Path("/{id}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single Patient by its primary key.", response=PatientValue.class)
	public PatientValue get(@PathParam("id") final Long id) throws ObjectNotFoundException
	{
		return dao.getByIdWithException(id);
	}

	@GET
	@Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds Patients by wildcard name search.", response=PeopleValue.class, responseContainer="List")
	public List<PeopleValue> find(@QueryParam("facilityId") final Long facilityId,
		@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name)
	{
		return dao.getEnrolledByFacilityAndName(facilityId, name);
	}

	@POST
	@Timed @UnitOfWork
	@ApiOperation(value="add", notes="Adds a single Patient. Returns the supplied Patient value with the auto generated identifier populated.", response=PatientValue.class)
	public PatientValue add(final PatientValue value) throws ValidationException
	{
		return dao.add(value);
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single Patient. Returns the supplied Patient value with the auto generated identifier populated.", response=PatientValue.class)
	public PatientValue set(final PatientValue value) throws ValidationException
	{
		return dao.update(value);
	}

	@DELETE
	@Path("/{id}") @Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates a single Patient by its primary key.", response=OperationResponse.class)
	public OperationResponse remove(@PathParam("id") final Long id) throws ValidationException
	{
		return new OperationResponse(dao.remove(id));
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Patients based on the supplied filter.", response=QueryResults.class)
	public QueryResults<PatientValue, PatientFilter> search(final PatientFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
