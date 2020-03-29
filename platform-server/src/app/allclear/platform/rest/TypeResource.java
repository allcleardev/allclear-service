package app.allclear.platform.rest;

import java.util.List;
import javax.ws.rs.*;

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;

import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.platform.type.*;

/** Jersey RESTful resource that retrieves the master/type data.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/25/2020
 *
 */

@Path("/types")
@Api("Types")
@Consumes(UTF8MediaType.APPLICATION_JSON)
@Produces(UTF8MediaType.APPLICATION_JSON)
public class TypeResource
{
	@GET
	@Path("/conditions") @Timed
	@ApiOperation(value="getConditions", notes="Gets a list of conditions.", response=Condition.class, responseContainer="List")
	public List<Condition> getConditions() { return Condition.LIST; }

	@GET
	@Path("/exposures") @Timed
	@ApiOperation(value="getExposures", notes="Gets a list of exposure levels.", response=Exposure.class, responseContainer="List")
	public List<Exposure> getExposures() { return Exposure.LIST; }

	@GET
	@Path("/peopleStatuses") @Timed
	@ApiOperation(value="getPeopleStatuses", notes="Gets a list of statures that can be associated with a person.", response=PeopleStatus.class, responseContainer="List")
	public List<PeopleStatus> getPeopleStatuses() { return PeopleStatus.LIST; }

	@GET
	@Path("/peopleStatures") @Timed
	@ApiOperation(value="getPeopleStatures", notes="Gets a list of statuses that can be associated with a person.", response=PeopleStature.class, responseContainer="List")
	public List<PeopleStature> getPeopleStatures() { return PeopleStature.LIST; }

	@GET
	@Path("/symptoms") @Timed
	@ApiOperation(value="getSymptoms", notes="Gets a list of symptoms that a person can have.", response=Symptom.class, responseContainer="List")
	public List<Symptom> getSymptoms() { return Symptom.LIST; }
}
