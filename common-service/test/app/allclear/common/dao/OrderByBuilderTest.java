package app.allclear.common.dao;

import java.io.IOException;
import java.util.*;

import org.junit.*;
import org.apache.commons.lang3.ArrayUtils;

import app.allclear.common.dao.QueryFilter;
import app.allclear.common.dao.QueryResults;
import app.allclear.testing.TestingUtils;

/** Unit test class that verifies the OrderByBuilder class.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class OrderByBuilderTest
{
	private static final String[] SORTS = new String[] {
		"foreColor", "desc,fore.color",	// First, so that it is the default.
		"id", "DESC",
		"name", "asc",
		"code", "ASC",
		"backgroundColor", "asc,c.background_color",
		"alignmentName", "desc,a.name;INNER JOIN o.alignmentXref x , INNER JOIN x.alignment a",
		"createdAt", "desc" };

	@Test
	public void find()
	{
		find("invalid", null, "foreColor", "fore.color", "DESC", false);
		find("invalid", "invalid", "foreColor", "fore.color", "DESC", false);
		find("invalid", "asc", "foreColor", "fore.color", "DESC", false);
		find("invalid", "ASC", "foreColor", "fore.color", "DESC", false);
		find("invalid", "desc", "foreColor", "fore.color", "DESC", false);
		find("invalid", "DESC", "foreColor", "fore.color", "DESC", false);

		find("id", null, "id", "id", "DESC", false);
		find("id", "invalid", "id", "id", "DESC", false);
		find("id", "asc", "id", "id", "ASC", true);
		find("id", "ASC", "id", "id", "ASC", true);
		find("id", "desc", "id", "id", "DESC", false);
		find("id", "DESC", "id", "id", "DESC", false);

		find("name", null, "name", "name", "ASC", true);
		find("name", "invalid", "name", "name", "ASC", true);
		find("name", "asc", "name", "name", "ASC", true);
		find("name", "ASC", "name", "name", "ASC", true);
		find("name", "desc", "name", "name", "DESC", false);
		find("name", "DESC", "name", "name", "DESC", false);

		find("alignmentName", null, "alignmentName", "a.name", "DESC", false, "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		find("alignmentName", "invalid", "alignmentName", "a.name", "DESC", false, "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		find("alignmentName", "asc", "alignmentName", "a.name", "ASC", true, "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		find("alignmentName", "ASC", "alignmentName", "a.name", "ASC", true, "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		find("alignmentName", "desc", "alignmentName", "a.name", "DESC", false, "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		find("alignmentName", "DESC", "alignmentName", "a.name", "DESC", false, "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
	}

	private void find(final String sortOn, final String sortDir, final String key, final String field, final String dir, final boolean asc, final String... joins)
	{
		var sort = new OrderByBuilder(SORTS).find(sortOn, sortDir);
		Assert.assertEquals("Check key", key, sort.key);
		Assert.assertEquals("Check field", field, sort.field);
		Assert.assertEquals("Check dir", dir, sort.dir);
		Assert.assertEquals("Check asc", asc, sort.asc);
		if (0 == joins.length)
			Assert.assertNull("Check joins", sort.joins);
		else
			Assert.assertArrayEquals("Check joins", joins, sort.joins);
	}

	@Test
	public void initWithMap() throws Exception
	{
		check(new OrderByBuilder("foreColor", loadValidMap()));
	}

	@Test
	public void initWithMapAndAlias() throws Exception
	{
		check("o.", new OrderByBuilder("o", "foreColor", loadValidMap()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void initWithMap_emptyDefault() throws Exception
	{
		new OrderByBuilder("   \n \t", loadValidMap());
	}

	@Test(expected=IllegalArgumentException.class)
	public void initWithMap_missingDefault() throws Exception
	{
		new OrderByBuilder(null, loadValidMap());
	}

	@Test(expected=IllegalArgumentException.class)
	@SuppressWarnings("unchecked")
	public void initWithMap_emptyMap()
	{
		new OrderByBuilder("default", Collections.EMPTY_MAP);
	}

	@Test(expected=IllegalArgumentException.class)
	public void initWithMap_nullMap()
	{
		new OrderByBuilder("default", null);
	}

	@Test
	public void initWithArray()
	{
		check(new OrderByBuilder(SORTS));
	}

	@Test
	public void initWithArrayAndAlias()
	{
		check("o.", new OrderByBuilder('o', SORTS));
	}

	@Test(expected=IllegalArgumentException.class)
	public void initWithArray_empty()
	{
		new OrderByBuilder(new String[] {});
	}

	@Test(expected=IllegalArgumentException.class)
	public void initWithArray_missing()
	{
		new OrderByBuilder((String) null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void initWithArray_invalidSortDir()
	{
		new OrderByBuilder("id", "invalidSortDir");
	}

	/** Helper method - checks the valid builder. */
	private void check(final OrderByBuilder builder)
	{
		check("", builder);
	}

	/** Helper method - checks the valid builder. */
	private void check(final String alias, final OrderByBuilder builder)
	{
		check(builder, createResults(null, null), "foreColor", "DESC", "fore.color");
		check(builder, createResults("invalid", null), "foreColor", "DESC", "fore.color");
		check(builder, createResults(null, "invalid"), "foreColor", "DESC", "fore.color");
		check(builder, createResults("invalid", "invalid"), "foreColor", "DESC", "fore.color");

		check(builder, createResults("id", null), "id", "DESC", alias + "id");
		check(builder, createResults("id", "invalid"), "id", "DESC", alias + "id");
		check(builder, createResults("id", "asc"), "id", "ASC", alias + "id");
		check(builder, createResults("id", "ASC"), "id", "ASC", alias + "id");
		check(builder, createResults("id", "desc"), "id", "DESC", alias + "id");
		check(builder, createResults("id", "DESC"), "id", "DESC", alias + "id");

		check(builder, createResults("code", null), "code", "ASC", alias + "code");
		check(builder, createResults("code", "invalid"), "code", "ASC", alias + "code");
		check(builder, createResults("code", "asc"), "code", "ASC", alias + "code");
		check(builder, createResults("code", "ASC"), "code", "ASC", alias + "code");
		check(builder, createResults("code", "desc"), "code", "DESC", alias + "code");
		check(builder, createResults("code", "DESC"), "code", "DESC", alias + "code");

		check(builder, createResults("backgroundColor", null), "backgroundColor", "ASC", "c.background_color");
		check(builder, createResults("backgroundColor", "invalid"), "backgroundColor", "ASC", "c.background_color");
		check(builder, createResults("backgroundColor", "asc"), "backgroundColor", "ASC", "c.background_color");
		check(builder, createResults("backgroundColor", "ASC"), "backgroundColor", "ASC", "c.background_color");
		check(builder, createResults("backgroundColor", "desc"), "backgroundColor", "DESC", "c.background_color");
		check(builder, createResults("backgroundColor", "DESC"), "backgroundColor", "DESC", "c.background_color");

		check(builder, createResults("alignmentName", null), "alignmentName", "DESC", "a.name", "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		check(builder, createResults("alignmentName", "invalid"), "alignmentName", "DESC", "a.name", "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		check(builder, createResults("alignmentName", "asc"), "alignmentName", "ASC", "a.name", "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		check(builder, createResults("alignmentName", "ASC"), "alignmentName", "ASC", "a.name", "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		check(builder, createResults("alignmentName", "desc"), "alignmentName", "DESC", "a.name", "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
		check(builder, createResults("alignmentName", "DESC"), "alignmentName", "DESC", "a.name", "INNER JOIN o.alignmentXref x", "INNER JOIN x.alignment a");
	}

	/** Helper method - checks the state of a QueryResults after the call OrderByBuilder.build. */
	private void check(final OrderByBuilder builder,
		final QueryResults<String, QueryFilter> buildResults,
		final String expectedSortOn,
		final String expectedSortDir,
		final String expectedField,
		final String... expectedJoins)
	{
		final QueryResults<String, QueryFilter> normalizeResults = createResults(buildResults.sortOn, buildResults.sortDir);	// Build before buildResults is modified in "builder.build" call below. DLS on 1/9/2017.
		final OrderByBuilder.Sort sort = builder.normalize(normalizeResults);
		Assert.assertEquals("Check normalizeResults.sortOn", expectedSortOn, normalizeResults.sortOn);
		Assert.assertEquals("Check normalizeResults.sortDir", expectedSortDir, normalizeResults.sortDir);
		Assert.assertEquals("Check sort.key", expectedSortOn, sort.key);
		Assert.assertEquals("Check sort.field", expectedField, sort.field);
		Assert.assertEquals("Check sort.dir", expectedSortDir, sort.dir);
		Assert.assertEquals("Check sort.asc", OrderByBuilder.ASC.equals(expectedSortDir), sort.asc);
		if (ArrayUtils.isEmpty(expectedJoins))
			Assert.assertNull("Check sort.joins", sort.joins);
		else
			Assert.assertArrayEquals("Check sort.joins", expectedJoins, sort.joins);
	}

	/** Helper method - creates a QueryResults with the specified sortOn and sortDir values. */
	@SuppressWarnings("unchecked")
	private QueryResults<String, QueryFilter> createResults(final String sortOn, final String sortDir)
	{
		return new QueryResults<>().withSortOn(sortOn).withSortDir(sortDir);
	}

	/** Helper method - loads the valid Map of values. */
	@SuppressWarnings("unchecked")
	private Map<String, String> loadValidMap() throws IOException
	{
		return TestingUtils.loadObject("/data/orderByBuilder/valid.json", Map.class);
	}
}
