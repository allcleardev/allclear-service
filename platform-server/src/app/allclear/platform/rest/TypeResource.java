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
	@Path("/peopleStatuses") @Timed
	@ApiOperation(value="getPeopleStatuses", notes="", response=PeopleStatus.class, responseContainer="List")
	public List<PeopleStatus> getPeopleStatuses() { return PeopleStatus.LIST; }

	@GET
	@Path("/peopleStatures") @Timed
	@ApiOperation(value="getPeopleStatures", notes="", response=PeopleStature.class, responseContainer="List")
	public List<PeopleStature> getPeopleStatures() { return PeopleStature.LIST; }
}
