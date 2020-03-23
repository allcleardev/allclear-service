package app.allclear.common.dao;

import java.util.List;

import org.junit.*;

/** Unit test class that verifies the SQLQueryBuilder class.
 * 
 * @author smalleyd
 * @version 1.3.5.9
 * @since 10/5/2018
 *
 */

public class SQLQueryBuilderTest
{
	@Test
	public void testSelect()
	{
		var b = where(new SQLQueryBuilder("SELECT id, name, age FROM person"));
		var where = "WHERE first = :first AND " +
			"second LIKE :second AND " +
			"third LIKE :third AND " +
			"fourth IN (:fourth_1,:fourth_2,:fourth_3) AND " +
			"fifth IN (:fifth_1,:fifth_2,:fifth_3) AND " +
			"sixth IN (21,28,35) AND " +
			"seventh IS NULL AND " +
			"eighth IS NOT NULL AND " +
			"EXISTS (SELECT 1 FROM pet WHERE pet.person_id = person.id) AND " +
			"NOT EXISTS (SELECT 1 FROM house WHERE house.person_id = person.id)";
		Assert.assertEquals("SELECT id, name, age FROM person " + where, b.build());
		Assert.assertEquals("SELECT COUNT(DISTINCT id) FROM person " + where, b.build("SELECT COUNT(DISTINCT id) FROM person"));

		b = where(new SQLQueryBuilder("SELECT id, name, age FROM person").or());
		var whereOr = "WHERE first = :first OR " +
			"second LIKE :second OR " +
			"third LIKE :third OR " +
			"fourth IN (:fourth_1,:fourth_2,:fourth_3) OR " +
			"fifth IN (:fifth_1,:fifth_2,:fifth_3) OR " +
			"sixth IN (21,28,35) OR " +
			"seventh IS NULL OR " +
			"eighth IS NOT NULL OR " +
			"EXISTS (SELECT 1 FROM pet WHERE pet.person_id = person.id) OR " +
			"NOT EXISTS (SELECT 1 FROM house WHERE house.person_id = person.id)";
		Assert.assertEquals("SELECT id, name, age FROM person " + whereOr, b.build());
		Assert.assertEquals("SELECT COUNT(DISTINCT id) FROM person " + whereOr, b.build("SELECT COUNT(DISTINCT id) FROM person"));

		b = where(new SQLQueryBuilder("SELECT id, name, age FROM person").and());
		Assert.assertEquals("SELECT id, name, age FROM person " + where, b.build());
		Assert.assertEquals("SELECT COUNT(DISTINCT id) FROM person " + where, b.build("SELECT COUNT(DISTINCT id) FROM person"));
	}

	private SQLQueryBuilder where(final QueryBuilder<Void> builder)
	{
		return (SQLQueryBuilder) builder.add("first", "first = :first", "first")
			.addStarts("second", "second LIKE :second", "second")
			.addContains("third", "third LIKE :third", "third")
			.addIn("fourth", "fourth IN {}", List.of("fourth_a", "fourth_b", "fourth_c"))
			.addIn("fifth", "fifth IN {}", new Integer[] { 5, 7, 11 })
			.addLiteralIn("sixth", "sixth IN {}", List.of(21L, 28L, 35L))
			.addNotNull("seventh", false)
			.addNotNull("eighth", true)
			.addNotNull("ninth", null)
			.addExists("SELECT 1 FROM pet WHERE pet.person_id = person.id", true)
			.addExists("SELECT 1 FROM house WHERE house.person_id = person.id", false);
	}

	@Test
	public void testAddInArray()
	{
		var select = "SELECT id FROM person";
		Assert.assertEquals(select, new SQLQueryBuilder(select).addIn("first", "first IN {}", (Integer[]) null).build());
		Assert.assertEquals(select, new SQLQueryBuilder(select).addIn("first", "first IN {}", new Integer[0]).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1)", new SQLQueryBuilder(select).addIn("first", "first IN {}", new Integer[] {4}).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1,:first_2)", new SQLQueryBuilder(select).addIn("first", "first IN {}", new Integer[] {4, 8}).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1,:first_2,:first_3)",
			new SQLQueryBuilder(select).addIn("first", "first IN {}", new Integer[] {4, 8, 12}).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1,:first_2,:first_3,:first_4)",
			new SQLQueryBuilder(select).addIn("first", "first IN {}", new Integer[] {4, 8, 12, 16}).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1,:first_2,:first_3,:first_4,:first_5)",
			new SQLQueryBuilder(select).addIn("first", "first IN {}", new Integer[] {4, 8, 12, 16, 20}).build());
	}

	@Test
	public void testAddInCollection()
	{
		var select = "SELECT id FROM person";
		Assert.assertEquals(select, new SQLQueryBuilder(select).addIn("first", "first IN {}", (List<Integer>) null).build());
		Assert.assertEquals(select, new SQLQueryBuilder(select).addIn("first", "first IN {}", List.of()).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1)", new SQLQueryBuilder(select).addIn("first", "first IN {}", List.of(4)).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1,:first_2)", new SQLQueryBuilder(select).addIn("first", "first IN {}", List.of(4, 8)).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1,:first_2,:first_3)",
			new SQLQueryBuilder(select).addIn("first", "first IN {}", List.of(4, 8, 12)).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1,:first_2,:first_3,:first_4)",
			new SQLQueryBuilder(select).addIn("first", "first IN {}", List.of(4, 8, 12, 16)).build());
		Assert.assertEquals(select + " WHERE first IN (:first_1,:first_2,:first_3,:first_4,:first_5)",
			new SQLQueryBuilder(select).addIn("first", "first IN {}", List.of(4, 8, 12, 16, 20)).build());
	}

	@Test
	public void testAddInLiteral()
	{
		var select = "SELECT id FROM person";
		Assert.assertEquals(select, new SQLQueryBuilder(select).addLiteralIn("first", "first IN {}", (List<Integer>) null).build());
		Assert.assertEquals(select, new SQLQueryBuilder(select).addLiteralIn("first", "first IN {}", List.of()).build());
		Assert.assertEquals(select + " WHERE first IN (4)", new SQLQueryBuilder(select).addLiteralIn("first", "first IN {}", List.of(4)).build());
		Assert.assertEquals(select + " WHERE first IN (4,8)", new SQLQueryBuilder(select).addLiteralIn("first", "first IN {}", List.of(4, 8)).build());
		Assert.assertEquals(select + " WHERE first IN (4,8,12)",
			new SQLQueryBuilder(select).addLiteralIn("first", "first IN {}", List.of(4, 8, 12)).build());
		Assert.assertEquals(select + " WHERE first IN (4,8,12,16)",
			new SQLQueryBuilder(select).addLiteralIn("first", "first IN {}", List.of(4, 8, 12, 16)).build());
		Assert.assertEquals(select + " WHERE first IN (4,8,12,16,20)",
			new SQLQueryBuilder(select).addLiteralIn("first", "first IN {}", List.of(4, 8, 12, 16, 20)).build());
	}

	@Test
	public void testGroupBy()
	{
		var b = new SQLQueryBuilder("SELECT last_name, COUNT(*) FROM person", "GROUP BY last_name");
		Assert.assertEquals("SELECT last_name, COUNT(*) FROM person GROUP BY last_name", b.build());
		Assert.assertEquals("SELECT first_name, COUNT(*) FROM person GROUP BY last_name", b.build("SELECT first_name, COUNT(*) FROM person"));

		var o = orderBy();
		Assert.assertEquals("SELECT last_name, COUNT(*) FROM person GROUP BY last_name ORDER BY last_name DESC",
			b.orderBy(o.find("last_name", null)).build());
		Assert.assertEquals("SELECT last_name, COUNT(*) FROM person GROUP BY last_name ORDER BY last_name ASC",
			b.orderBy(o.find("last_name", "asc")).build());
		Assert.assertEquals("SELECT last_name, COUNT(*) FROM person WHERE first_name = :firstName GROUP BY last_name ORDER BY last_name ASC",
			b.add("firstName", "first_name = :firstName", "Dave").orderBy(o.find("last_name", "asc")).build());
		Assert.assertEquals("SELECT last_name, COUNT(*) FROM person INNER JOIN pet ON person.id = pet.person_id WHERE first_name = :firstName AND pet.name = :petName GROUP BY last_name ORDER BY last_name ASC",
			b.add("petName", "pet.name = :petName", "rover", "INNER JOIN pet ON person.id = pet.person_id")	// Layers on.
			.orderBy(o.find("last_name", "asc")).build());
		Assert.assertEquals("SELECT last_name, COUNT(*) FROM person INNER JOIN pet ON person.id = pet.person_id WHERE first_name = :firstName AND pet.name = :petName GROUP BY last_name ORDER BY city DESC",
			b.add("houseZip", "house.zip = :houseZip", null, "INNER JOIN house ON person.id = house.person_id")	// Not added since NULL.
			.orderBy(o.find("city", "desc")).build());
		Assert.assertEquals("SELECT city, COUNT(*) FROM person INNER JOIN pet ON person.id = pet.person_id INNER JOIN house ON person.id = house.person_id WHERE first_name = :firstName AND pet.name = :petName AND house.zip = :houseZip GROUP BY last_name ORDER BY city DESC",
			b.add("houseZip", "house.zip = :houseZip", "90210", "INNER JOIN house ON person.id = house.person_id")
			.orderBy(o.find("city", "desc")).build("SELECT city, COUNT(*) FROM person"));
	}

	@Test
	public void testOrderBy()
	{
		var o = orderBy();
		var b = new SQLQueryBuilder("SELECT id, first_name, last_name, city, postal_code FROM person");

		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY first_name ASC",
			b.orderBy(o.find("first_name", null)).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY last_name DESC",
			b.orderBy(o.find("last_name", null)).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY first_name ASC",
			b.orderBy(o.find("first_name", "ASC")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY last_name ASC",
			b.orderBy(o.find("last_name", "ASC")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY first_name ASC",
			b.orderBy(o.find("first_name", "asc")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY last_name ASC",
			b.orderBy(o.find("last_name", "asc")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY first_name DESC",
			b.orderBy(o.find("first_name", "DESC")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY last_name DESC",
			b.orderBy(o.find("last_name", "DESC")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY first_name DESC",
			b.orderBy(o.find("first_name", "desc")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY last_name DESC",
			b.orderBy(o.find("last_name", "desc")).build());

		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY first_name ASC",
			b.orderBy(o.find("postal_code", null)).build());	// Invalid - resorts to default.
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY postal_code DESC",
			b.orderBy(o.find("postalCode", null)).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY postal_code DESC",
			b.orderBy(o.find("postalCode")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY postal_code DESC",
			b.orderBy(o.find("postalCode", "invalid")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY postal_code ASC",
			b.orderBy(o.find("postalCode", "ASC")).build());
		Assert.assertEquals("SELECT id, first_name, last_name, city, postal_code FROM person ORDER BY postal_code ASC",
			b.orderBy(o.find("postalCode", "asc")).build());
	}

	private OrderByBuilder orderBy()
	{
		return new OrderByBuilder("first_name", "ASC", "last_name", "DESC", "city", "ASC", "postalCode", "DESC,postal_code");
	}
}
