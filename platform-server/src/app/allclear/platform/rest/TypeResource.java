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
	@Path("/facilityTypes") @Timed
	@ApiOperation(value="getFacilityTypes", notes="Gets a list of facility types.", response=FacilityType.class, responseContainer="List")
	public List<FacilityType> getFacilityTypes() { return FacilityType.LIST; }

	@GET
	@Path("/peopleStatuses") @Timed
	@ApiOperation(value="getPeopleStatuses", notes="Gets a list of statures that can be associated with a person.", response=PeopleStatus.class, responseContainer="List")
	public List<PeopleStatus> getPeopleStatuses() { return PeopleStatus.LIST; }

	@GET
	@Path("/peopleStatures") @Timed
	@ApiOperation(value="getPeopleStatures", notes="Gets a list of statuses that can be associated with a person.", response=PeopleStature.class, responseContainer="List")
	public List<PeopleStature> getPeopleStatures() { return PeopleStature.LIST; }

	@GET
	@Path("/sexes") @Timed
	@ApiOperation(value="getSexes", notes="Gets a list of sexes that a person can have.", response=Sex.class, responseContainer="List")
	public List<Sex> getSexes() { return Sex.LIST; }

	@GET
	@Path("/symptoms") @Timed
	@ApiOperation(value="getSymptoms", notes="Gets a list of symptoms that a person can have.", response=Symptom.class, responseContainer="List")
	public List<Symptom> getSymptoms() { return Symptom.LIST; }

	@GET
	@Path("/testCriteria") @Timed
	@ApiOperation(value="getTestCriteria", notes="Gets a list of test criteria that a facility follows.", response=TestCriteria.class, responseContainer="List")
	public List<TestCriteria> getTestCriteria() { return TestCriteria.LIST; }

	@GET
	@Path("/testTypes") @Timed
	@ApiOperation(value="getTestTypes", notes="Gets a list of test types.", response=TestType.class, responseContainer="List")
	public List<TestType> getTestTypes() { return TestType.LIST; }
}
