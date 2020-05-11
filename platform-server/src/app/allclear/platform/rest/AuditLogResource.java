package app.allclear.platform.rest;

import java.util.List;
import javax.ws.rs.*;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.platform.dao.AuditLogDAO;
import app.allclear.platform.filter.AuditLogFilter;
import app.allclear.platform.value.AuditLogValue;
import app.allclear.platform.value.FacilityValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the AuditLogDAO.
*
*	@author smalleyd
*	@version 1.1.46
*	@since May 10, 2020
*
**********************************************************************************/

@Path("/auditLogs")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="AuditLog")
public class AuditLogResource
{
	private final AuditLogDAO dao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public AuditLogResource(final AuditLogDAO dao)
	{
		this.dao = dao;
	}

	@GET
	@Path("/facilities/{id}") @Timed
	@ApiOperation(value="getFacilityLogs", notes="Gets the audit log entries for specified Facility.", response=AuditLogValue.class, responseContainer="List")
	public List<AuditLogValue> getFacilityLogs(@PathParam("id") final String id) throws ValidationException
	{
		var o = dao.search(FacilityValue.TABLE, new AuditLogFilter().withId(id));
		return o.noRecords() ? List.of() : o.records; 
	}

	@POST
	@Path("/facilities/search") @Timed
	@ApiOperation(value="search", notes="Searches the Facility audit log entries based on the supplied filter.", response=QueryResults.class)
	public QueryResults<AuditLogValue, AuditLogFilter> search(final AuditLogFilter filter) throws ValidationException
	{
		return dao.search(FacilityValue.TABLE, filter);
	}
}
