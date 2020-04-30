package app.allclear.platform.entity;

import org.junit.jupiter.api.*;

import app.allclear.platform.value.AdminValue;

/** Unit test class that verifies the Admin entity.
 * 
 * @author smalleyd
 * @version 1.0.16
 * @since 4/2/2020
 *
 */

public class AdminTest
{
	@Test
	public void check()
	{
		var o = new Admin("test", new AdminValue("larry").withPassword("Password_1"));
		Assertions.assertEquals("test", o.getPartitionKey(), "Check partitionKey");
		Assertions.assertTrue(o.check("Password_1"), "Check valid");
		Assertions.assertFalse(o.check("password_1"), "Check valid");
		Assertions.assertFalse(o.check("Password_2"), "Check invalid");

		o.update(new AdminValue("larry").withPassword("Password_2"));
		Assertions.assertEquals("test", o.getPartitionKey(), "Check partitionKey");
		Assertions.assertTrue(o.check("Password_2"), "Check updated");
		Assertions.assertFalse(o.check("password_2"), "Check invalid");
		Assertions.assertFalse(o.check("Password_1"), "Check invalid");
	}
}
