package app.allclear.platform.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.platform.dao.RegistrationDAO;
import app.allclear.platform.filter.RegistrationFilter;
import app.allclear.platform.value.RegistrationValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the RegistrationDAO.
*
*	@author smalleyd
*	@version 1.0.40
*	@since 4/4/2020
*
**********************************************************************************/

@Path("/registrations")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Registration")

public class RegistrationResource
{
	private final RegistrationDAO dao;

	public RegistrationResource(final RegistrationDAO dao)
	{
		this.dao = dao;
	}

	@DELETE
	@Path("/{key}") @Timed
	@ApiOperation(value="remove", notes="Removes a single Registration request via its key.")
	public Response remove(@PathParam("key") final String key)
	{
		dao.remove(key);

		return Response.ok().build();
	}

	@POST
	@Path("/search") @Timed
	@ApiOperation(value="search", notes="Retrieves Registrations requests via search criteria.")
	public QueryResults<RegistrationValue, RegistrationFilter> search(final RegistrationFilter filter) throws ValidationException
	{
		return dao.search(filter);
	}
}
