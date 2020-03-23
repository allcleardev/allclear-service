package app.allclear.common.hibernate;

import java.util.*;

import org.hibernate.Session;
import org.junit.*;

import app.allclear.junit.hibernate.*;
import app.allclear.common.dao.QueryBuilder;
import app.allclear.common.entity.Country;
import app.allclear.common.entity.CountryCount;

/** Unit test class for the NativeQueryBuilder.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class NativeQueryBuilderTest
{
	@ClassRule
	public static final HibernateRule RULE = new HibernateRule(Country.class, CountryCount.class);

	@Rule
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(RULE);

	private Session session;
	private String COUNT = "SELECT COUNT(o.id) FROM country o";
	private String SELECT = "SELECT o.id, o.name, o.code, o.num_code, o.active FROM country o";
	private String SELECT_COUNTRY_COUNT = "SELECT o.name, COUNT(u.id) AS count_total FROM country o INNER JOIN user u ON o.id = u.country_id";
	private String GROUP_BY_COUNTRY_COUNT = "GROUP BY o.name";

	@Before
	public void init()
	{
		session = transRule.getSession();
	}

	/** Helper - constructs a basic NativeQueryBuilder. */
	private NativeQueryBuilder<Country> create()
	{
		return new NativeQueryBuilder<>(session, SELECT, Country.class, "o");
	}

	/** Helper - runs and checks the NativeQueryBuilder.aggregate method. */
	private NativeQueryBuilder<Country> aggregate(final QueryBuilder<Country> builder, final long count)
	{
		Assert.assertEquals("Check count", count, builder.aggregate(COUNT));

		return (NativeQueryBuilder<Country>) builder;
	}

	@Test
	public void construct()
	{
		var value = create();
		Assert.assertNotNull("Exists", value);
		Assert.assertEquals("Check session", session, value.session);
		Assert.assertEquals("Check select", SELECT, value.select);
		Assert.assertEquals("Check entity", Country.class, value.entity);
		Assert.assertEquals("Check alias", "o", value.alias);
	}

	@Test
	public void add()
	{
		Assert.assertEquals(SELECT + " WHERE o.active = :active",
			create().add("active", "o.active = :active", true).build());
	}

	@Test
	public void add_empty()
	{
		Assert.assertEquals(SELECT,
			create().add("active", "o.active = :active", null).build());
	}

	@Test
	public void add_with_join()
	{
		Assert.assertEquals(SELECT + " INNER JOIN user u ON o.id = u.country_id WHERE u.email = :email",
			create().add("email", "u.email = :email", "test+1@jibe.com", "INNER JOIN user u ON o.id = u.country_id").build());
	}

	@Test
	public void add_empty_with_join()
	{
		Assert.assertEquals(SELECT,
			create().add("email", "u.email = :email", null, "INNER JOIN user u ON o.id = u.country_id").build());
	}

	@Test
	public void add_in_array()
	{
		check(create().addIn("id", "o.id IN {}", new String[] { "US", "CA", "FR", "JP", "GB" }),
			SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4,:id_5)",
			5);
	}

	@Test
	public void add_in_array_one()
	{
		check(create().addIn("id", "o.id IN {}", new String[] { "GB" }),
			SELECT + " WHERE o.id IN (:id_1)",
			1);
	}

	@Test
	public void add_in_array_with_join()
	{
		check(create().addIn("countryId", "u.country_id IN {}", new String[] { "US", "CA", "FR", "JP", "GB" }, "INNER JOIN user u ON o.id = u.country_id"),
			SELECT + " INNER JOIN user u ON o.id = u.country_id WHERE u.country_id IN (:countryId_1,:countryId_2,:countryId_3,:countryId_4,:countryId_5)",
			3);
	}

	@Test
	public void add_in_array_empty()
	{
		check(create().addIn("id", "o.id IN {}", new String[] {}),
			SELECT,
			245);
	}

	@Test
	public void add_in_array_empty_with_join()
	{
		check(create().addIn("countryId", "u.country_id IN {}", new String[] {}, "INNER JOIN user u ON o.id = u.country_id"),
			SELECT,
			245);
	}

	@Test
	public void add_in_array_null()
	{
		check(create().addIn("id", "o.id IN {}", (Long[]) null),
			SELECT,
			245);
	}

	@Test
	public void add_not_in_array()
	{
		check(create().addIn("id", "o.id NOT IN {}", new String[] { "US", "CA", "FR", "JP", "GB" }),
			SELECT + " WHERE o.id NOT IN (:id_1,:id_2,:id_3,:id_4,:id_5)",
			240);
	}

	@Test
	public void add_not_in_array_empty()
	{
		check(create().addIn("id", "o.id NOT IN {}", new String[] {}),
			SELECT,
			245);
	}

	@Test
	public void add_not_in_array_null()
	{
		check(create().addIn("id", "o.id NOT IN {}", (Integer[]) null),
			SELECT,
			245);
	}

	@Test
	public void add_in_list()
	{
		check(create().addIn("id", "o.id IN {}", Arrays.asList("US", "CA", "FR", "JP", "GB")),
			SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4,:id_5)",
			5);
	}

	@Test
	public void add_in_list_one()
	{
		check(create().addIn("id", "o.id IN {}", Arrays.asList("GB")),
			SELECT + " WHERE o.id IN (:id_1)",
			1);
	}

	@Test
	public void add_in_list_with_join()
	{
		check(create().addIn("countryId", "u.country_id IN {}", Arrays.asList("US", "ZM", "FR", "JP", "GB"), "INNER JOIN user u ON o.id = u.country_id"),
			SELECT + " INNER JOIN user u ON o.id = u.country_id WHERE u.country_id IN (:countryId_1,:countryId_2,:countryId_3,:countryId_4,:countryId_5)",
			2);
	}

	@Test
	public void add_in_list_empty()
	{
		check(create().addIn("id", "o.id IN {}", new ArrayList<String>(0)),
			SELECT,
			245);
	}

	@Test
	public void add_in_list_null()
	{
		check(create().addIn("id", "o.id IN {}", (List<String>) null),
			SELECT,
			245);
	}

	@Test
	public void add_in_list_null_with_join()
	{
		check(create().addIn("countryId", "u.country_id IN {}", (List<String>) null, "INNER JOIN user u ON o.id = u.country_id"),
			SELECT,
			245);
	}

	@Test
	public void add_not_in_list()
	{
		check(create().addIn("id", "o.id NOT IN {}", Arrays.asList("US", "CA", "FR", "JP", "GB")),
			SELECT + " WHERE o.id NOT IN (:id_1,:id_2,:id_3,:id_4,:id_5)",
			240);
	}

	@Test
	public void add_not_in_list_empty()
	{
		check(create().addIn("id", "o.id NOT IN {}", new ArrayList<String>(0)),
			SELECT,
			245);
	}

	@Test
	public void add_not_in_list_null()
	{
		check(create().addIn("id", "o.id NOT IN {}", (List<String>) null),
			SELECT,
			245);
	}

	@Test
	public void add_null()
	{
		Assert.assertEquals(SELECT + " WHERE o.name IS NULL",
			create().addNotNull("o.name", false).build());
	}

	@Test
	public void add_not_null()
	{
		Assert.assertEquals(SELECT + " WHERE o.name IS NOT NULL",
			create().addNotNull("o.name", true).build());
	}

	@Test
	public void add_null_empty()
	{
		Assert.assertEquals(SELECT,
			create().addNotNull("o.name", null).build());
	}

	@Test
	public void add_null_with_join()
	{
		Assert.assertEquals(SELECT + " INNER JOIN user u ON o.id = u.country_id WHERE u.email IS NULL",
			create().addNotNull("u.email", false, "INNER JOIN user u ON o.id = u.country_id").build());
	}

	@Test
	public void add_not_null_with_join()
	{
		Assert.assertEquals(SELECT + " INNER JOIN user u ON o.id = u.country_id WHERE u.email IS NOT NULL",
			create().addNotNull("u.email", true, "INNER JOIN user u ON o.id = u.country_id").build());
	}

	@Test
	public void add_null_empty_with_join()
	{
		Assert.assertEquals(SELECT,
			create().addNotNull("u.email", null, "INNER JOIN user u ON o.id = u.country_id").build());
	}

	@Test
	public void add_contains()
	{
		var value = create().addContains("name", "o.name LIKE :name", "Canada");
		Assert.assertEquals("Check SQL", SELECT + " WHERE o.name LIKE :name", value.build());
		Assert.assertEquals("Check parameters.size", 1, value.parameters.size());
		Assert.assertEquals("Check parameters.name", "%Canada%", value.parameters.get("name"));
	}

	@Test
	public void add_contains_empty()
	{
		var value = create().addContains("name", "o.name LIKE :name", null);
		Assert.assertEquals("Check SQL", SELECT,
			value.build());
		Assert.assertTrue("Check parameters.size", value.parameters.isEmpty());
	}

	@Test
	public void add_contains_with_join()
	{
		var value = create().addContains("email", "u.email LIKE :email", "@jibe.com", "INNER JOIN user u ON o.id = u.country_id");
		Assert.assertEquals("Check SQL", SELECT + " INNER JOIN user u ON o.id = u.country_id WHERE u.email LIKE :email", value.build());
		Assert.assertEquals("Check parameters.size", 1, value.parameters.size());
		Assert.assertEquals("Check parameters.email", "%@jibe.com%", value.parameters.get("email"));
	}

	@Test
	public void add_contains_empty_with_join()
	{
		var value = create().addContains("email", "u.email LIKE :email", null, "INNER JOIN user u ON o.id = u.country_id");
		Assert.assertEquals("Check SQL", SELECT, value.build());
		Assert.assertTrue("Check parameters.size", value.parameters.isEmpty());
	}

	@Test
	public void add_starts()
	{
		var value = create().addStarts("name", "o.name LIKE :name", "Canada");
		Assert.assertEquals("Check SQL", SELECT + " WHERE o.name LIKE :name", value.build());
		Assert.assertEquals("Check parameters.size", 1, value.parameters.size());
		Assert.assertEquals("Check parameters.name", "Canada%", value.parameters.get("name"));
	}

	@Test
	public void add_starts_empty()
	{
		var value = create().addStarts("name", "o.name LIKE :name", null);
		Assert.assertEquals("Check SQL", SELECT, value.build());
		Assert.assertTrue("Check parameters.size", value.parameters.isEmpty());
	}

	@Test
	public void add_starts_with_join()
	{
		var value = create().addStarts("email", "u.email LIKE :email", "test@", "INNER JOIN user u ON o.id = u.country_id");
		Assert.assertEquals("Check SQL", SELECT + " INNER JOIN user u ON o.id = u.country_id WHERE u.email LIKE :email", value.build());
		Assert.assertEquals("Check parameters.size", 1, value.parameters.size());
		Assert.assertEquals("Check parameters.email", "test@%", value.parameters.get("email"));
	}

	@Test
	public void add_starts_empty_with_join()
	{
		var value = create().addStarts("email", "u.email LIKE :email", null, "INNER JOIN user u ON o.id = u.country_id");
		Assert.assertEquals("Check SQL", SELECT, value.build());
		Assert.assertTrue("Check parameters.size", value.parameters.isEmpty());
	}

	@Test
	public void add_multi_where()
	{
		var value = create()
			.addContains("email", "u.email LIKE :email", "@jibe", "INNER JOIN user u ON o.id = u.country_id")
			.addStarts("name", "o.name LIKE :name", "United")
			.addNotNull("u.state_id", true, "INNER JOIN user u ON o.id = u.country_id");
		Assert.assertEquals("Check SQL", SELECT + " INNER JOIN user u ON o.id = u.country_id WHERE u.email LIKE :email AND o.name LIKE :name AND u.state_id IS NOT NULL", value.build());
		Assert.assertEquals("Check parameters.size", 2, value.parameters.size());
		Assert.assertEquals("Check parameters.email", "%@jibe%", value.parameters.get("email"));
		Assert.assertEquals("Check parameter.name", "United%", value.parameters.get("name"));
	}

	@Test
	public void add_multi_join()
	{
		var value = create()
			.add("firstProduct", "c.first_product = :firstProduct", false, "INNER JOIN user u ON o.id = u.country_id", "INNER JOIN company c ON u.company_id = c.id");
		Assert.assertEquals("Check SQL",
			SELECT + " INNER JOIN user u ON o.id = u.country_id INNER JOIN company c ON u.company_id = c.id WHERE c.first_product = :firstProduct",
			value.build());
		Assert.assertEquals("Check parameters.size", 1, value.parameters.size());
	}

	@Test
	public void add_multi_where_and_multi_join()
	{
		var value = create()
			.addContains("email", "u.email LIKE :email", "@jibe", "INNER JOIN user u ON o.id = u.country_id")
			.addStarts("name", "o.name LIKE :name", "United")
			.addNotNull("u.state_id", true, "INNER JOIN user u ON o.id = u.country_id")
			.add("firstProduct", "c.first_product = :firstProduct", true, "INNER JOIN user u ON o.id = u.country_id", "INNER JOIN company c ON u.company_id = c.id");
		Assert.assertEquals("Check SQL",
			SELECT + " INNER JOIN user u ON o.id = u.country_id INNER JOIN company c ON u.company_id = c.id WHERE u.email LIKE :email AND o.name LIKE :name AND u.state_id IS NOT NULL AND c.first_product = :firstProduct",
			value.build());
		Assert.assertEquals("Check parameters.size", 3, value.parameters.size());
		Assert.assertEquals("Check parameters.email", "%@jibe%", value.parameters.get("email"));
		Assert.assertEquals("Check parameter.name", "United%", value.parameters.get("name"));
	}

	/** Helper method - checks the SQL and the number of records returned. */
	private List<Country> check(final QueryBuilder<Country> value, String sql, int size)
	{
		List<Country> records = null;

		Assert.assertEquals("Check SQL", sql, value.build());
		Assert.assertEquals("Check size", size, (records = value.run()).size());

		return records;
	}

	@Test
	public void run_contains()
	{
		final List<Country> records = aggregate(create().addContains("name", "o.name LIKE :name", "Island"), 17L).run();
		Assert.assertEquals("Check size", 17, records.size());
	}

	@Test
	public void run_not_null()
	{
		List<Country> records = aggregate(create().addNotNull("u.state_id", true, "INNER JOIN user u ON o.id = u.country_id"), 2L).run();
		Assert.assertEquals("Check size", 2, records.size());
	}

	@Test
	public void run_null()
	{
		List<Country> records = aggregate(create().addNotNull("u.state_id", false, "INNER JOIN user u ON o.id = u.country_id"), 1L).run();
		Assert.assertEquals("Check size", 1, records.size());
	}

	@Test
	public void run_starts()
	{
		List<Country> records = aggregate(create().addStarts("name", "o.name LIKE :name", "United"), 4L).run();
		Assert.assertEquals("Check size", 4, records.size());
	}

	@Test
	public void run_active_with_join()
	{
		List<Country> records = aggregate(create().add("active", "u.active = :active", true, "INNER JOIN user u ON o.id = u.country_id"), 2L).run();
		Assert.assertEquals("Check size", 2, records.size());
	}

	@Test
	public void run_country_count()
	{
		var value = new NativeQueryBuilder<CountryCount>(session, SELECT_COUNTRY_COUNT, CountryCount.class, "o", GROUP_BY_COUNTRY_COUNT);
		Assert.assertNotNull("Exists", value);
		Assert.assertEquals("Check session", session, value.session);
		Assert.assertEquals("Check select", SELECT_COUNTRY_COUNT, value.select);
		Assert.assertEquals("Check entity", CountryCount.class, value.entity);
		Assert.assertEquals("Check alias", "o", value.alias);
		Assert.assertEquals("Check groupBy", GROUP_BY_COUNTRY_COUNT, value.groupBy);

		List<CountryCount> records = value.run();
		Assert.assertEquals("Check size", 3, records.size());
		for (CountryCount record : records)
			Assert.assertEquals("Check count", 1, record.total.intValue());
	}
}
