package app.allclear.platform.rest;

import static app.allclear.common.value.Constants.*;

import java.math.BigDecimal;
import java.util.List;

import javax.ws.rs.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.resources.Headers;
import app.allclear.common.time.StopWatch;
import app.allclear.common.value.OperationResponse;
import app.allclear.google.client.MapClient;
import app.allclear.google.model.GeocodeResult;
import app.allclear.platform.dao.FacilityDAO;
import app.allclear.platform.dao.SessionDAO;
import app.allclear.platform.entity.CountByName;
import app.allclear.platform.filter.FacilityFilter;
import app.allclear.platform.type.TestCriteria;
import app.allclear.platform.value.FacilityValue;

/**********************************************************************************
*
*	Jersey RESTful resource that provides access to the FacilityDAO.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
**********************************************************************************/

@Path("/facilities")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
@Api(value="Facility")
public class FacilityResource
{
	private static final Logger log = LoggerFactory.getLogger(FacilityResource.class);

	private final FacilityDAO dao;
	private final SessionDAO sessionDao;
	private final MapClient map;

	/** Populator.
	 * 
	 * @param dao
	 */
	public FacilityResource(final FacilityDAO dao, final SessionDAO sessionDao, final MapClient map)
	{
		this.dao = dao;
		this.sessionDao = sessionDao;
		this.map = map;
	}

	@GET
	@Path("/{id}") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single Facility by its primary key.", response=FacilityValue.class)
	public FacilityValue get(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final Long id) throws ObjectNotFoundException
	{
		var o = dao.getByIdWithException(id);
		if (!sessionDao.current().admin() && !o.active) throw new ObjectNotFoundException("The ID '" + id + "' is unavailable.");

		return o;
	}

	@GET
	@Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="find", notes="Finds Facilitys by wildcard name search.", response=FacilityValue.class, responseContainer="List")
	public List<FacilityValue> find(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@QueryParam("name") @ApiParam(name="name", value="Value for the wildcard search") final String name,
		@QueryParam("latitude") @ApiParam(name="latitude", value="Optional, GEO latitude of the locaiton to be searched") BigDecimal latitude,
		@QueryParam("longitude") @ApiParam(name="longitude", value="Optional, GEO longitude of the locaiton to be searched") BigDecimal longitude,
		@QueryParam("location") @ApiParam(name="location", value="Optional, GEO location text to be searched") final String location,
		@QueryParam("miles") @ApiParam(name="miles", value="Optional, max miles from the GEO location to include in the search") final Integer miles,
		@QueryParam("km") @ApiParam(name="km", value="Optional, max kilometers from the GEO location to include in the search") final Integer km)
	{
		if (null != location)
		{
			var o = geocode(location);
			if (null != o)
			{
				var l = o.geometry.location;
				latitude = l.lat;
				longitude = l.lng;
			}
		}
		if ((null != latitude) && (null != longitude) && ((null != miles) || (null != km)))
			return dao.getActiveByNameAndDistance(name, latitude, longitude, (null != miles) ? milesToMeters(miles) : kmToMeters(km));

		return dao.getActiveByName(name);
	}

	@GET
	@Path("/cities") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="getDistinctStates", notes="Gets the distinct list of facility cities by state.", response=CountByName.class, responseContainer="List")
	public List<CountByName> getDistinctCitiesByState(@QueryParam("state") final String state)
	{
		if (StringUtils.isBlank(state))
			throw new ValidationException("state", "Please provide a 'state' parameter.");

		return dao.getDistinctCitiesByState(state);
	}

	@GET
	@Path("/name") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="get", notes="Gets a single Facility by its name.", response=FacilityValue.class)
	public FacilityValue get(@QueryParam("name") final String name) throws ObjectNotFoundException
	{
		if (StringUtils.isBlank(name))
			throw new ValidationException("name", "Please provide a 'name' parameter.");

		return dao.getByNameWithException(name);
	}

	@GET
	@Path("/states") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="getDistinctStates", notes="Gets the distinct list of facility states.", response=CountByName.class, responseContainer="List")
	public List<CountByName> getDistinctStates() { return dao.getDistinctStates(); }

	@POST
	@Timed @UnitOfWork
	@ApiOperation(value="add", notes="Adds a single Facility. Returns the supplied Facility value with the auto generated identifier populated.", response=FacilityValue.class)
	public FacilityValue add(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FacilityValue value) throws ValidationException
	{
		return dao.add(populate(value), sessionDao.checkEditor().canAdmin());
	}

	@PUT
	@Timed @UnitOfWork
	@ApiOperation(value="set", notes="Updates an existing single Facility. Returns the supplied Facility value with the auto generated identifier populated.", response=FacilityValue.class)
	public FacilityValue set(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FacilityValue value) throws ValidationException
	{
		return dao.update(populate(value), sessionDao.checkEditor().canAdmin());
	}

	@DELETE
	@Path("/{id}") @Timed @UnitOfWork
	@ApiOperation(value="remove", notes="Removes/deactivates a single Facility by its primary key.")
	public OperationResponse remove(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		@PathParam("id") final Long id) throws ValidationException
	{
		sessionDao.checkAdmin();

		return new OperationResponse(dao.remove(id));
	}

	@POST
	@Path("/search") @Timed @UnitOfWork(readOnly=true, transactional=false)
	@ApiOperation(value="search", notes="Searches the Facilitys based on the supplied filter.", response=QueryResults.class)
	public QueryResults<FacilityValue, FacilityFilter> search(@HeaderParam(Headers.HEADER_SESSION) final String sessionId,
		final FacilityFilter filter) throws ValidationException
	{
		var timer = new StopWatch();

		if ((null != filter.from) && (null != filter.from.location))
		{
			var o = geocode(filter.from.location);
			if (null != o) filter.from = filter.from.copy(o.geometry.location);
			log.info("GEOCODED: {} in {}", filter.from.location, timer.split());
		}

		var s = sessionDao.currentOrAnon();
		if (!s.admin()) filter.withActive(true);	// Non-admins (people, customers, and anonymous) can only see active facilities. DLS on 5/1/2020.

		var restricted = (s.person() && !s.person.meetsCdcPriority3());	// Does the current user have restrictions imposed with regards to facilities that are open to them?
		if (filter.restrictive && restricted)
			filter.withNotTestCriteriaId(TestCriteria.CDC_CRITERIA.id);

		log.info("RESTRICTED: {} / {} in {}", filter.restrictive, restricted, timer.split());

		var results = dao.search(filter);
		log.info("SEARCHED: {} in {}", results.total, timer.split());
		if (!results.noRecords())
		{
			var favoriteIds = dao.getIdsByPerson(s.person);
			results.records.forEach(v -> v.favorite(favoriteIds).restricted = (restricted && v.restricted()));
			log.info("FAVORITED: {} in {}", favoriteIds.size(), timer.split());
		}

		log.info("TOTAL: {} in {}", results.total, timer.total());

		return results;
	}

	FacilityValue populate(final FacilityValue value)
	{
		if ((null != value.address) &&
		    ((null == value.city) || (null == value.state) || (null == value.latitude) || (null == value.longitude)))
		{
			var o = geocode(value.address);
			if (null != o)
			{
				if (null == value.city) value.city = o.city().shortName;
				if (null == value.state) value.state = o.state().longName;
				if (null == value.latitude) value.latitude = o.geometry.location.lat;
				if (null == value.longitude) value.longitude = o.geometry.location.lng;
			}
		}

		return value;
	}

	GeocodeResult geocode(final String location)
	{
		try
		{
			var o = map.geocode(location);
			if (o.ok() && CollectionUtils.isNotEmpty(o.results))
				return o.results.get(0);
		}
		catch (final Exception ex) { log.warn(ex.getMessage(), ex); }

		return null;
	}
}
