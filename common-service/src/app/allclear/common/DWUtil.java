package app.allclear.common;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.*;

import com.google.common.util.concurrent.Uninterruptibles;

/** Dropwizard related utilities.
 *
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 */

public class DWUtil
{
	private static final Logger logger = LoggerFactory.getLogger(DWUtil.class);
	public static final String APP_CREDENTIALS_FILE = "appCredentialsFile";

	/** Loads the application credentials file into system properties. */
	public static StringSubstitutor loadAppCredentials()
	{
		String fileName = System.getProperty(APP_CREDENTIALS_FILE);
		if (null != fileName)
		{
			logger.info("{} found '{}'.", APP_CREDENTIALS_FILE, fileName);

			File file = new File(fileName);
			if (!file.exists())
				throw new IllegalArgumentException(String.format("The application credentials file '%s' does not exist.", fileName));
			else if (file.isDirectory())
				throw new IllegalArgumentException(String.format("The application credentials file '%s' is a directory. Must be a file.", fileName));
	
			try
			{
				Properties prop = new Properties();
				prop.load(new FileInputStream(file));
	
				if (!prop.isEmpty())
					for (Map.Entry<Object, Object> e : prop.entrySet())
						System.setProperty((String) e.getKey(), (String) e.getValue());

				logger.info("{} '{}' loaded {} properties.", APP_CREDENTIALS_FILE, fileName, prop.size());
			}
	
			catch (IOException ex)
			{
				logger.error(ex.getMessage(), ex);
				throw new IllegalArgumentException(String.format("Could not load application credentials file '%s'. %s", fileName, ex.getMessage()));
			}
		}
		else
			logger.info("{} not found.", APP_CREDENTIALS_FILE);

		return new StringSubstitutor(StringLookupFactory.INSTANCE.systemPropertyStringLookup());
	}

	/** Guess the DW application's base url from configuration.
	 */
	public static String guessApplicationUrl(Configuration cfg) {
		String host;
		try { host = InetAddress.getLocalHost().getHostName(); } catch (UnknownHostException e) { host = "localhost"; }
		final ServerFactory serverFactory = cfg.getServerFactory();
		if (serverFactory instanceof SimpleServerFactory) {
			final SimpleServerFactory server = (SimpleServerFactory) serverFactory;
			final HttpConnectorFactory connector = (HttpConnectorFactory) server.getConnector();
			final int port = connector.getPort();
			return "http://" + host + ":" + port;
		}
		else if (serverFactory instanceof DefaultServerFactory) {
			final DefaultServerFactory server = (DefaultServerFactory) serverFactory;
			final HttpConnectorFactory connector = (HttpConnectorFactory) server.getApplicationConnectors().get(0);
			final int port = connector.getPort();
			return "http://" + host + ":" + port;
		}
		// TODO support other ServerFactory's and ConnectorFactory's as needed
		throw new RuntimeException(String.format("cannot guess application url from %s",serverFactory));
	}

	/* We want all stdout and stderr to go to the same log file(s).
	 * For rolling log files (as jetty likes it), we borrow some functionality from jetty.
	 */
	/** Redirect to rolling logfiles with pattern: ./logs/yyyy_mm_dd.stderrout.log */
	public static void redirectStdToFiles() {
		redirectStdToFiles("./logs/yyyy_mm_dd.stderrout.log");
	}
	/** Redirect to rolling logfiles with given pattern and 90 day retention. */
	public static void redirectStdToFiles(final String filenamePattern) {
		redirectStdToFiles(filenamePattern, 90);
	}
	/** Redirect to rolling logfiles with given pattern and retention and GMT timezone. */
	public static void redirectStdToFiles(final String filenamePattern, final int retainDays) {
		final TimeZone gmt = TimeZone.getTimeZone("GMT");
		redirectStdToFiles(filenamePattern, retainDays, gmt);
	}
	/** Redirect to one file, with given name */
	public static void redirectStdToFile(final String filename) {
		redirectStdToFiles(filename, -1, null);
	}

	/** Redirect to one or rolling logfiles.
	 * For rolling logfiles, specify a TimeZone and retainDays.
	 */
	public static void redirectStdToFiles(final String filenamePattern, final int retainDays, final TimeZone tz) {
		final PrintStream ps;
		try {
			final OutputStream os = retainDays < 0 || tz == null ? new FileOutputStream(filenamePattern, true) :
																   new RolloverFileOutputStream(filenamePattern, false, retainDays, tz);
			ps = new PrintStream(os);
		} catch (IOException e) {
			logger.info("Not redirecting stderr/stdout to {}: {}", filenamePattern, e.toString());
			return;
		}
		logger.info("Redirecting stderr/stdout to {}, retainDays {}, timeZone {}", filenamePattern, retainDays, tz);
		Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS); // wait for logging to flush
		System.setOut(ps);
		System.setErr(ps);
		logger.info("stderr/stdout redirected to this file");
	}

	/** Get local node name. Support AWS specific mechanism.
	 */
	public static String getLocalNodeName()
	{
		if (null == localNodeName)
		{
			try { localNodeName = InetAddress.getLocalHost().getHostName(); }
			catch (Exception e)
			{
				logger.warn("getHostName() failed", e);
				localNodeName = "unknown";
			}
			System.setProperty("node.name", localNodeName);
		}
		return localNodeName;
	}
	private static String localNodeName = null;
}
