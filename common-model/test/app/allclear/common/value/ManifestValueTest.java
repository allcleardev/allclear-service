package app.allclear.common.value;

import org.junit.*;

/** Unit test class that verifies the ManifestValue POJO.
 * 
 * @author smalleyd
 * @version 1.3.9.11
 * @since 6/24/2019
 *
 */

public class ManifestValueTest
{
	@Test
	public void create()
	{
		var value = new ManifestValue("Vendor 1", "Title 1", "1.2.3.4");

		Assert.assertEquals("Vendor 1", value.vendor);
		Assert.assertEquals("Title 1", value.title);
		Assert.assertEquals("1.2.3.4", value.version);
	}
}
