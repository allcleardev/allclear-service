package app.allclear.common.resources;

import java.io.*;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.*;

import com.sun.management.HotSpotDiagnosticMXBean;

/** RESTful resource class that generates a JVM heap dump.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

@Path("/heap")
@Api(tags="Heap")
public class HeapDumpResource
{
	private static final Logger logger = LoggerFactory.getLogger(HeapDumpResource.class);

	private static final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
	private static final String DIAGNOSTIC_COMMAND_BEAN_NAME = "com.sun.management:type=DiagnosticCommand";
	private static volatile HotSpotDiagnosticMXBean hotspotMBean;

	public synchronized void init() throws IOException
	{
		if (hotspotMBean == null)
			hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value="getHeapDump", notes="Get a dump of the Heap. Use curl and redirect to file.")
	public Response getHeapDump(
		@ApiParam(value="exclude memory waiting for GC", defaultValue="true") @QueryParam("live") final Boolean live) throws IOException
	{
		logger.info("getHeapDump({})", live);
		init();
		var file = new File("heap.hprof");
		file.delete();	// Make sure that it doesn't exist before calling dumpHeap.

		hotspotMBean.dumpHeap(file.getAbsolutePath(), (null == live) ? true : live);
		final StreamingOutput out = o -> {
			try (var in = new FileInputStream(file))
			{
				logger.info("{} bytes of heap dump.", IOUtils.copy(in, o));
			}
			finally { file.delete(); }
		};
		return Response.ok(out).build();
	}

	@GET
	@Path("/histogram")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value="getHistogram", notes="Get a class histogram of the Heap.")
	public String getHistogram() throws Exception
	{
		logger.info("getHistogram()");
		var bean = new ObjectName(DIAGNOSTIC_COMMAND_BEAN_NAME);
		return (String) server.invoke(bean,
				"gcClassHistogram",
				new Object[] {null},
				new String[]{String[].class.getName()});
	}
}
