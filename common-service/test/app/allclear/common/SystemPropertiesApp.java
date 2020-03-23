package app.allclear.common;

import java.io.File;

import org.slf4j.*;

/** Tests the loading of system properties in the application entry point (main).
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class SystemPropertiesApp
{
	private static final Logger log = LoggerFactory.getLogger(SystemPropertiesApp.class);

	public static void main(String... args)
	{
		String fileName = System.getProperty(DWUtil.APP_CREDENTIALS_FILE);
		File file = new File(fileName);
		log.warn("appCredentialsFile: '{}', size: {}", fileName, file.length());
	}
}
