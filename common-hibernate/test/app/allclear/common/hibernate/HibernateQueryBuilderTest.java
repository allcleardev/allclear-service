package app.allclear.common.hibernate;

import static app.allclear.common.dao.OrderByBuilder.*;

import java.util.*;

import org.hibernate.Session;
import org.hibernate.annotations.QueryHints;
import org.junit.*;

import app.allclear.junit.hibernate.*;
import app.allclear.common.dao.*;
import app.allclear.common.entity.*;

/** Unit test class for the QueryBuilder.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class HibernateQueryBuilderTest
{
	@ClassRule
	public static final HibernateRule RULE = new HibernateRule(Country.class, User.class);

	@Rule
	public final HibernateTransactionRule transRule = new HibernateTransactionRule(RULE);

	private Session session;
	private String SELECT = "SELECT OBJECT(o) FROM Country o";
	private String COUNT = "SELECT COUNT(o.id) FROM Country o";
	private String SELECT_USER = "SELECT OBJECT(o) FROM User u";
	private String COUNT_USER = "SELECT COUNT(o.id) FROM User u";
	private String SELECT_USERS = "SELECT OBJECT(o) FROM User o";
	private String COUNT_USERS = "SELECT COUNT(o.id) FROM User o";

	private static final OrderByBuilder ORDER = new OrderByBuilder("id", ASC, "name", ASC, "code", ASC, "numCode", ASC, "active", DESC);
	private static final OrderByBuilder ORDER_USER = new OrderByBuilder('o', "id", ASC, "name", ASC, "code", ASC,
		"numCode", ASC, "active", DESC, "userId", ASC + ",u.id", "email", ASC + ",u.email");
	private static final OrderByBuilder ORDER_USERS = new OrderByBuilder("id", DESC,
		"email", ASC, "firstName", ASC, "lastName", ASC, "companyId", ASC, "stateId", ASC, "countryId", ASC, "active", DESC);

	@Before
	public void init()
	{
		session = transRule.getSession();
	}

	/** Helper - constructs a basic QueryBuilder. */
	private HibernateQueryBuilder<Country> create()
	{
		return new HibernateQueryBuilder<>(session, SELECT, Country.class);
	}

	/** Helper - constructs a QueryBuilder with a join from the User entity. */
	private HibernateQueryBuilder<Country> createUser()
	{
		return new HibernateQueryBuilder<>(session, SELECT_USER, Country.class);
	}

	/** Helper - constructs a basic QueryBuilder for the User entity. */
	private HibernateQueryBuilder<User> createUsers()
	{
		return new HibernateQueryBuilder<>(session, SELECT_USERS, User.class);
	}

	@Test
	public void construct()
	{
		HibernateQueryBuilder<Country> value = create();
		Assert.assertNotNull("Exists", value);
		Assert.assertEquals("Check session", session, value.session);
		Assert.assertEquals("Check select", SELECT, value.select);
	}

	@Test
	public void add_active()
	{
		check(create().add("active", "o.active = :active", true), SELECT + " WHERE o.active = :active", 245);
	}

	@Test
	public void add_inactive()
	{
		check(create().add("active", "o.active = :active", false), SELECT + " WHERE o.active = :active", 0);
	}

	@Test
	public void add_in()
	{
		// Duped the join a couple of times.
		check(createUser().addIn("countryId", "u.countryId IN {}", new String[] { "JP", "CA", "US", "BM" }, "INNER JOIN u.country o", "INNER JOIN u.country o", "INNER JOIN u.country o"),
			SELECT_USER + " INNER JOIN u.country o WHERE u.countryId IN (:countryId_1,:countryId_2,:countryId_3,:countryId_4)", 2);
	}

	@Test
	public void add_in_or_inactive()
	{
		check(create().addIn("id", "o.id IN {}", new String[] { "JP", "CA", "US", "BM" }).add("active", "o.active = :active", false), SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4) AND o.active = :active", 0);
		check(create().addIn("id", "o.id IN {}", new String[] { "JP", "CA", "US", "BM" }).or().add("active", "o.active = :active", false), SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4) OR o.active = :active", 4);
		check(create().addIn("id", "o.id IN {}", new String[] { "JP", "CA", "US", "BM" }).or(true).add("active", "o.active = :active", false), SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4) OR o.active = :active", 4);
		check(create().addIn("id", "o.id IN {}", new String[] { "JP", "CA", "US", "BM" }).or(false).add("active", "o.active = :active", false), SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4) AND o.active = :active", 0);
		check(create().addIn("id", "o.id IN {}", new String[] { "JP", "CA", "US", "BM" }).and().add("active", "o.active = :active", false), SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4) AND o.active = :active", 0);
		check(create().addIn("id", "o.id IN {}", new String[] { "JP", "CA", "US", "BM" }).and(true).add("active", "o.active = :active", false), SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4) AND o.active = :active", 0);
		check(create().addIn("id", "o.id IN {}", new String[] { "JP", "CA", "US", "BM" }).and(false).add("active", "o.active = :active", false), SELECT + " WHERE o.id IN (:id_1,:id_2,:id_3,:id_4) OR o.active = :active", 4);
	}

	@Test
	public void add_literal_in()
	{
		checkUser(createUsers().addLiteralIn("id", "o.id IN {}", Arrays.asList(1, 2, 3)),
			SELECT_USERS + " WHERE o.id IN (1,2,3)", 3);
	}

	@Test
	public void add_literal_in_single()
	{
		checkUser(createUsers().addLiteralIn("id", "o.id IN {}", Arrays.asList(2)),
			SELECT_USERS + " WHERE o.id IN (2)", 1);
	}

	@Test
	public void add_contains()
	{
		check(create().addContains("name", "o.name LIKE :name", "Island"), SELECT + " WHERE o.name LIKE :name", 17);
	}

	@Test
	public void add_not_null()
	{
		check(createUser().addNotNull("u.stateId", true, "INNER JOIN u.country o"), SELECT_USER + " INNER JOIN u.country o WHERE u.stateId IS NOT NULL", 2); 
	}

	@Test
	public void add_null()
	{
		check(createUser().addNotNull("u.stateId", false, "INNER JOIN u.country o"), SELECT_USER + " INNER JOIN u.country o WHERE u.stateId IS NULL", 1);
	}

	@Test
	public void add_starts()
	{
		check(create().addStarts("name", "o.name LIKE :name", "United"), SELECT + " WHERE o.name LIKE :name", 4);
	}

	@Test
	public void add_active_with_join()
	{
		check(createUser().add("active", "u.active = :active", true, "INNER JOIN u.country o"), SELECT_USER + " INNER JOIN u.country o WHERE u.active = :active", 2);
	}

	@Test
	public void add_exists()
	{
		check(create().addExists("SELECT 1 FROM User u WHERE u.countryId = o.id", true),
			"SELECT OBJECT(o) FROM Country o WHERE EXISTS (SELECT 1 FROM User u WHERE u.countryId = o.id)", 3);
		check(create().addExists("SELECT 1 FROM User u WHERE u.countryId = o.id", false),
			"SELECT OBJECT(o) FROM Country o WHERE NOT EXISTS (SELECT 1 FROM User u WHERE u.countryId = o.id)", 242);
		check(create().addExists("SELECT 1 FROM User u WHERE u.countryId = o.id", null),
			"SELECT OBJECT(o) FROM Country o", 245);
	}

	/** Helper method - checks the SQL and the number of records returned. */
	private List<Country> check(final QueryBuilder<Country> value, final String sql, final int size)
	{
		List<Country> records = null;
		var simple = SELECT.equals(value.select);
		var count = simple ? COUNT : COUNT_USER;
		var order = simple ? ORDER : ORDER_USER;

		Assert.assertEquals("Check SQL", sql, value.build());
		Assert.assertEquals("Check size", size, (records = value.run()).size());
		Assert.assertEquals("Check count", (long) size, value.aggregate(count));

		Assert.assertEquals("Check size with order by id", size, value.orderBy(order.find("id")).run().size());
		Assert.assertEquals("Check size with order by name", size, value.orderBy(order.find("name")).run().size());
		Assert.assertEquals("Check size with order by code", size, value.orderBy(order.find("code")).run().size());
		Assert.assertEquals("Check size with order by numCode", size, value.orderBy(order.find("numCode")).run().size());
		Assert.assertEquals("Check size with order by active", size, value.orderBy(order.find("active")).run().size());
		if (!simple)
		{
			Assert.assertEquals("Check size with order by userId", size, value.orderBy(order.find("userId")).run().size());
			Assert.assertEquals("Check size with order by u.email", size, value.orderBy(order.find("email")).run().size());
		}

		// With page information.
		final int actualSize = (10 < size) ? 10 : size;
		Assert.assertEquals("Check size with order by id", actualSize, value.orderBy(order.find("id")).run(0, 10).size());
		Assert.assertEquals("Check size with order by id", actualSize, value.orderBy(order.find("id")).run(new QueryResults<String, QueryFilter>((long) size, new QueryFilter(1, 10))).size());

		// Test createAndBind.
		Assert.assertEquals("Check createAndBind", size, ((HibernateQueryBuilder<Country>) value).createAndBind().setHint(QueryHints.CACHEABLE, true).list().size());

		return records;
	}

	/** Helper method - checks the SQL and the number of records returned. */
	private List<User> checkUser(final QueryBuilder<User> value, final String sql, final int size)
	{
		List<User> records = null;

		Assert.assertEquals("Check SQL", sql, value.build());
		Assert.assertEquals("Check size", size, (records = value.run()).size());
		Assert.assertEquals("Check count", (long) size, value.aggregate(COUNT_USERS));

		Assert.assertEquals("Check size with order by id", size, value.orderBy(ORDER_USERS.find("id")).run().size());
		Assert.assertEquals("Check size with order by email", size, value.orderBy(ORDER_USERS.find("email")).run().size());
		Assert.assertEquals("Check size with order by firstName", size, value.orderBy(ORDER_USERS.find("firstName")).run().size());
		Assert.assertEquals("Check size with order by lastName", size, value.orderBy(ORDER_USERS.find("lastName")).run().size());
		Assert.assertEquals("Check size with order by companyId", size, value.orderBy(ORDER_USERS.find("companyId")).run().size());
		Assert.assertEquals("Check size with order by stateId", size, value.orderBy(ORDER_USERS.find("stateId")).run().size());
		Assert.assertEquals("Check size with order by countryId", size, value.orderBy(ORDER_USERS.find("countryId")).run().size());
		Assert.assertEquals("Check size with order by active", size, value.orderBy(ORDER_USERS.find("active")).run().size());

		// With page information.
		final int actualSize = (20 < size) ? 20 : size;
		Assert.assertEquals("Check size with order by id", actualSize, value.orderBy(ORDER_USERS.find("id")).run(0, 20).size());
		Assert.assertEquals("Check size with order by id", actualSize, value.orderBy(ORDER_USERS.find("id")).run(new QueryResults<String, QueryFilter>((long) size, new QueryFilter(1, 20))).size());

		// Test createAndBind.
		Assert.assertEquals("Check createAndBind", size, ((HibernateQueryBuilder<User>) value).createAndBind().setHint(QueryHints.CACHEABLE, true).list().size());

		return records;
	}
}
