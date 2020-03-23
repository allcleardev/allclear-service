package app.allclear.common.hibernate;

import javax.ws.rs.*;

import io.dropwizard.hibernate.UnitOfWork;

import app.allclear.common.entity.Country;
import app.allclear.common.mediatype.UTF8MediaType;

/** Represents a RESTful resource class to access the test Country entity.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Path("/countries")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
public class CountryResource
{
	private final CountryDAO dao;

	public CountryResource(final CountryDAO dao)
	{
		this.dao = dao;
	}

	@POST
	@UnitOfWork(readOnly=false, transactional=true)
	public Country add(final Country record) { return dao.add(record); }

	@GET @Path("/{id}")
	@UnitOfWork(readOnly=true, transactional=false)
	public Country find(@PathParam("id") final String id) { return dao.find(id); }
}
