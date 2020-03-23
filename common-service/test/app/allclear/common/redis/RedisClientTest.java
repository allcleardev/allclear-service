package app.allclear.common.redis;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.*;

import org.junit.*;
import org.junit.runners.MethodSorters;

import app.allclear.junit.redis.RedisServerRule;

/** Functional test class that verifies the RedisClient class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisClientTest extends AbstractRedisClientTest
{
	@ClassRule
	public static final RedisServerRule SERVER = new RedisServerRule();

	@BeforeClass
	public static void up()
	{
		client = new RedisClient(new RedisConfig("localhost", 6378, 200L, 5));
		init();
	}

	@Test
	public void test_00_add()
	{
		Assert.assertTrue("Empty", client.isEmpty());
		Assert.assertEquals("Size", 0, client.size());

		int count = 0;
		for (Map.Entry<String, String> entry : MAP.entrySet())
		{
			Assert.assertEquals("Pre-count", count++, client.size());
			client.put(entry.getKey(), entry.getValue());
			Assert.assertEquals("Post-count", count, client.size());
		}

		Assert.assertEquals("Size", MAP.size(), client.size());
		Assert.assertFalse("Empty", client.isEmpty());
	}

	@Test
	public void test_01_containsKey()
	{
		Assert.assertEquals("Size", MAP.size(), client.size());
		Assert.assertTrue("Check 07030", client.containsKey("07030"));
		Assert.assertTrue("Check 90210", client.containsKey("90210"));
		Assert.assertTrue("Check 10012", client.containsKey("10012"));
		Assert.assertFalse("Check invalid", client.containsKey("invalid"));
	}

	@Test
	public void test_01_expires()
	{
		// Expirations not set yet.
		Assert.assertNull("Check 07030", client.expires("07030"));
		Assert.assertNull("Check 90210", client.expires("90210"));
		Assert.assertNull("Check 10012", client.expires("10012"));
		Assert.assertNull("Check invalid", client.expires("invalid"));
	}

	@Test
	public void test_01_get()
	{
		Assert.assertEquals("Size", MAP.size(), client.size());
		Assert.assertEquals("Check 07030", "Hoboken", client.get("07030"));
		Assert.assertEquals("Check 90210", "Beverly Hills", client.get("90210"));
		Assert.assertEquals("Check 10012", "New York", client.get("10012"));
		Assert.assertNull("Check invalid", client.get("invalid"));
	}

	@Test
	public void test_01_keys()
	{
		var keys = client.keys("9");
		Assert.assertEquals("Size", 1, keys.size());
		Assert.assertFalse("Check 07030", keys.contains("07030"));
		Assert.assertTrue("Check 90210", keys.contains("90210"));
		Assert.assertFalse("Check 10012", keys.contains("10012"));
	}

	@Test
	public void test_01_keySet()
	{
		var keys = client.keySet();
		Assert.assertEquals("Size", MAP.size(), keys.size());
		Assert.assertTrue("Check 07030", keys.contains("07030"));
		Assert.assertTrue("Check 90210", keys.contains("90210"));
		Assert.assertTrue("Check 10012", keys.contains("10012"));
		Assert.assertFalse("Check invalid", keys.contains("invalid"));
	}

	@Test
	public void test_01_ping() throws Exception
	{
		Assert.assertNotNull(client.ping());
	}

	@Test
	public void test_02_expire()
	{
		Assert.assertTrue("Expire 07030", client.expire("07030", 1000));
		Assert.assertTrue("Expire 90210", client.expire("90210", 2000));
		Assert.assertTrue("Expire 10012", client.expire("10012", 3000));
	}

	@Test
	public void test_02_expires()
	{
		assertThat(client.expires("07030")).isGreaterThanOrEqualTo(990).isLessThanOrEqualTo(1000).as("Check 07030");
		assertThat(client.expires("90210")).isGreaterThanOrEqualTo(1990).isLessThanOrEqualTo(2000).as("Check 90210");
		assertThat(client.expires("10012")).isGreaterThanOrEqualTo(2990).isLessThanOrEqualTo(3000).as("Check 10012");
		Assert.assertNull("Check invalid", client.expires("invalid"));
	}

	@Test
	public void test_03_clear()
	{
		Assert.assertEquals("Size", MAP.size(), client.size());

		client.clear();

		Assert.assertEquals("Size", 0, client.size());
	}

	@Test
	public void test_03_putAll()
	{
		Assert.assertEquals("Size", 0, client.size());

		client.putAll(MAP);

		Assert.assertEquals("Size", MAP.size(), client.size());
	}

	@Test
	public void test_04_get()
	{
		test_01_get();
	}

	@Test
	public void test_04_remove()
	{
		Assert.assertEquals("Size", MAP.size(), client.size());

		client.remove("90210");
		client.remove("invalid");

		Assert.assertEquals("Size", MAP.size() - 1, client.size());
	}

	@Test
	public void test_05_containsKey()
	{
		Assert.assertEquals("Size", MAP.size() - 1, client.size());
		Assert.assertTrue("Check 07030", client.containsKey("07030"));
		Assert.assertFalse("Check 90210", client.containsKey("90210"));
		Assert.assertTrue("Check 10012", client.containsKey("10012"));
		Assert.assertFalse("Check invalid", client.containsKey("invalid"));
	}

	@Test
	public void test_10_push()
	{
		Assert.assertEquals("Size", 0, client.queueSize("queue:auditLog"));

		client.push("queue:auditLog", "request 1");
		client.push("queue:auditLog", "request 2");
		client.push("queue:auditLog", "request 3");

		Assert.assertEquals("Size", 3, client.queueSize("queue:auditLog"));
	}

	@Test
	public void test_11_listQueue()
	{
		Assert.assertEquals("Size", 3, client.queueSize("queue:auditLog"));

		var values = client.list("queue:auditLog");
		Assert.assertEquals("Check values.size", 3, values.size());
		assertThat(values).isEqualTo(Arrays.asList("request 1", "request 2", "request 3")).as("Check values");
	}

	@Test
	public void test_12_listQueue_paged()
	{
		var values = client.list("queue:auditLog", 1, 1);
		Assert.assertEquals("Check values.size", 1, values.size());
		assertThat(values).isEqualTo(Arrays.asList("request 1")).as("Check values");

		values = client.list("queue:auditLog", 2, 1);
		Assert.assertEquals("Check values.size", 1, values.size());
		assertThat(values).isEqualTo(Arrays.asList("request 2")).as("Check values");

		values = client.list("queue:auditLog", 3, 1);
		Assert.assertEquals("Check values.size", 1, values.size());
		assertThat(values).isEqualTo(Arrays.asList("request 3")).as("Check values");

		values = client.list("queue:auditLog", 1, 2);
		Assert.assertEquals("Check values.size", 2, values.size());
		assertThat(values).isEqualTo(Arrays.asList("request 1", "request 2")).as("Check values");

		values = client.list("queue:auditLog", 2, 2);
		Assert.assertEquals("Check values.size", 1, values.size());
		assertThat(values).isEqualTo(Arrays.asList("request 3")).as("Check values");
	}

	@Test
	public void test_13_pop()
	{
		Assert.assertEquals("Size", 3, client.queueSize("queue:auditLog"));

		Assert.assertEquals("request 1", client.pop("queue:auditLog"));

		Assert.assertEquals("Size", 2, client.queueSize("queue:auditLog"));
	}

	@Test
	public void test_14_clear()
	{
		Assert.assertEquals("Size", 2, client.queueSize("queue:auditLog"));

		client.unqueue("queue:auditLog");

		Assert.assertEquals("Size", 0, client.queueSize("queue:auditLog"));
	}

	@Test
	public void test_20_addToSet()
	{
		Assert.assertEquals("Size", 0, client.setSize("set:values"));

		for (int i = 1; i <= 10; i++)
		{
			Assert.assertEquals("Check size", i - 1, client.setSize("set:values"));
			client.set("set:values", "value " + i);
			Assert.assertEquals("Check size", i, client.setSize("set:values"));
		}

		Assert.assertEquals("Size", 10, client.setSize("set:values"));
	}

	@Test
	public void test_21_getSet()
	{
		var values = client.set("set:values");
		Assert.assertEquals("Size", 10, values.size());
		assertThat(values).isEqualTo(IntStream.rangeClosed(1, 10).mapToObj(i -> "value " + i).collect(Collectors.toSet())).as("Check values");
	}

	@Test
	public void test_22_removeFromSet()
	{
		Assert.assertEquals("Size", 10, client.setSize("set:values"));

		client.unset("set:values", "value 1", "value 3", "value 5", "value 7", "value 9");

		Assert.assertEquals("Size", 5, client.setSize("set:values"));
	}

	@Test
	public void test_22_removeFromSet_check()
	{
		assertThat(client.set("set:values")).isEqualTo(new HashSet<String>(Arrays.asList("value 2", "value 4", "value 6", "value 8", "value 10")));
	}

	@Test
	public void test_30_addHash()
	{
		Assert.assertEquals("Size", 0, client.hashSize("hash:values"));

		for (int i = 1; i <= 10; i++)
		{
			Assert.assertEquals("Size", i - 1, client.hashSize("hash:values"));

			client.hash("hash:values", "field_" + i, "value " + i);
			
			Assert.assertEquals("Size", i, client.hashSize("hash:values"));
		}

		Assert.assertEquals("Size", 10, client.hashSize("hash:values"));
	}

	@Test
	public void test_30_check()
	{
		Assert.assertEquals("Size", 10, client.hashSize("hash:values"));

		for (int i = 1; i <= 10; i++)
			Assert.assertEquals("Check field_" + i, "value " + i, client.hash("hash:values", "field_" + i));
	}

	@Test
	public void test_30_checkHash()
	{
		var values = client.hash("hash:values");
		Assert.assertEquals("Size", 10, values.size());

		for (int i = 1; i <= 10; i++)
			Assert.assertEquals("Check field_" + i, "value " + i, values.get("field_" + i));
	}

	@Test
	public void test_30_removeFromHash()
	{
		Assert.assertEquals("Size", 10, client.hashSize("hash:values"));

		client.unhash("hash:values", "field_2", "field_4", "field_6", "field_8", "field_10");

		Assert.assertEquals("Size", 5, client.hashSize("hash:values"));
	}

	@Test
	public void test_30_removeFromHash_check()
	{
		Assert.assertEquals("Size", 5, client.hashSize("hash:values"));

		for (int i = 1; i <= 10; i+=2)
			Assert.assertEquals("Check field_" + i, "value " + i, client.hash("hash:values", "field_" + i));
	}
}
