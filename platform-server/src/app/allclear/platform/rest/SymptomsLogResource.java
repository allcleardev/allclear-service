package app.allclear.platform.rest;

import javax.ws.rs.*;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;
import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.dao.SymptomsLogDAO;
import app.allclear.platform.filter.SymptomsLogFilter;
import app.allclear.platform.value.SymptomsLogValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the SymptomsLogDAO.
*
*	@author smalleyd
*	@version 1.0.80
*	@since April 8, 2020
*
**********************************************************************************/

@Path("/symptomsLogs")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="SymptomsLog")
public class SymptomsLogResource
{
	private final SymptomsLogDAO dao;
	private final SessionDAO sessionDao;

	/** Populator.
	 * 
	 * @param dao
	 */
	public SymptomsLogResource(final SymptomsLogDAO dao, final SessionDAO sessionDao)
	{
		this.dao = dao;
		this.sessionDao = sessionDao;
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the SymptomsLogs based on the supplied filter.", response=QueryResults.class)
	public QueryResults<SymptomsLogValue, SymptomsLogFilter> search(final SymptomsLogFilter filter) throws ValidationException
	{
		var o = sessionDao.current();
		if (o.person()) filter.withPersonId(o.person.id);

		return dao.search(filter);
	}
}
