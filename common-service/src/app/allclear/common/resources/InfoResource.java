package app.allclear.common.resources;

import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.slf4j.*;

import io.dropwizard.Configuration;
import io.swagger.annotations.*;

import com.codahale.metrics.health.HealthCheckRegistry;
import app.allclear.common.DWUtil;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.common.value.HealthResponse;

/** RESTful resource that provides access to the Application configuration. health, and version.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

@Path("/info")
@Api(value="INFO")
public class InfoResource
{
	private static final Logger log = LoggerFactory.getLogger(InfoResource.class);

	private final Configuration conf;
	private final String node;
	private final HealthCheckRegistry hcr;
	private final List<String> pingChecks;
	private final Map<String, String> version;

	/** kovach wants just the pom version */
	private static String shortVersion(final String version)
	{
		if (version == null) return null;
		final int x = version.indexOf(" ");
		return x < 0 ? version : version.substring(0,x);
	}

	public InfoResource(final Configuration conf, final HealthCheckRegistry hcr, final List<String> pingChecks, final String version)
	{
		this.conf = conf;
		this.node = DWUtil.getLocalNodeName();
		this.hcr = hcr;
		this.pingChecks = pingChecks;
		(this.version = new HashMap<>(1)).put("version", shortVersion(version));
	}

	@GET
	@Path("/config")
	@Produces(UTF8MediaType.APPLICATION_JSON)
	@ApiOperation(value="config", notes="Dropwizard Configuration", response=Configuration.class)
	public Configuration config() { return conf; }

	@GET
	@Path("/health")
	@Produces(UTF8MediaType.APPLICATION_JSON)
	@ApiOperation(value="health", notes="AutoMonitor-friendly Healthcheck", response=HealthResponse.class)
	public HealthResponse health()
	{
		log.debug("healthCheck()");
		var errors = hcr.runHealthChecks()
			.entrySet()
			.stream()
			.filter(e -> !e.getValue().isHealthy())
			.map(e -> e.getKey() + ": " + e.getValue().getMessage() + ": " + e.getValue().getError())
			.collect(Collectors.toList());
		return errors.isEmpty() ? new HealthResponse(node) : new HealthResponse(node, errors.toString());
	}

	@GET
	@Path("/ping")
	@ApiOperation(value="ping", notes="LB-friendly Healthcheck")
	public Response ping()
	{
		log.debug("ping()");
		for (var check : pingChecks)
		{
			var result = hcr.runHealthCheck(check);
			if (!result.isHealthy())
			{
				log.warn("Ping failed '{}': {}", check, result.getMessage(), result.getError());
				return Response.status(503).build();
			}
		}

		return Response.ok().build();
	}

	@GET
	@Path("/version")
	@Produces(UTF8MediaType.APPLICATION_JSON)
	@ApiOperation(value="version", notes="Retrieves the version of the running application.", response=String.class, responseContainer="Map")
	public Map<String, String> version() { return version; }
}
