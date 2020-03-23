package app.allclear.common.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import app.allclear.common.mediatype.UTF8MediaType;

/** RESTful resource class that exposes the Hibernate statistics.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@Path("/info/hibernate")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Hibernate") // same as ServiceInfoResource so that it appears under ServiceInfo in swagger
public class HibernateResource
{
	private final SessionFactory factory;

	public HibernateResource(final SessionFactory factory)
	{
		this.factory = factory;
	}

	@GET
	@Path("/stats")
	@ApiOperation(value="getHibernateStats", notes="Retrieves the Hibernate statistics.", response=Statistics.class)
	public String getHibernateStats() throws Exception
	{
		var o = factory.getStatistics();

		return (null != o) ? o.toString() : null;	// CanNOT export Statistics object directly as it has a circular reference that causes error - com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Direct self-reference leading to cycle (through reference chain: org.hibernate.stat.internal.StatisticsImpl["managementBean"]). DLS on 1/10/2020.
	}

	@DELETE
	@Path("/cache")
	@ApiOperation(value="clearCache", notes="Clears the cache of the Hibernate elements.")
	public Response clearCache()
	{
		factory.getCache().evictAllRegions();	// Should be the preferred way to clear the Hibernate cache.

		return Response.ok().build();
	}
}
