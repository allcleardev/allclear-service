package app.allclear.common.dao;

import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/** Helper/builder class for a Hibernate criteria order-by.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class OrderByBuilder
{
	public static final String ASC = "ASC";
	public static final String DESC = "DESC";

	private static final Pattern PATTERN_COMMA = Pattern.compile(",");

	/** Sort direction list for validation. */
	private static final List<String> DIRS = Arrays.asList(ASC, DESC);	// Since it's a short list, use a list to validate instead of a HashSet. DLS on 7/13/2015.

	/** Contains the map of acceptable sort fields paired with its default sort order. */
	final Map<String, Sort> acceptable;	// Friendly for tests.

	/** Default order. */
	private final Sort defaultSort;

	/** Optional default alias to prefix fields with. */
	private final String defaultAlias;

	/** Populator.
	 * 
	 * @param field default field
	 * @param sorts map of acceptable sorts
	 */
	public OrderByBuilder(final String field, final Map<String, String> sorts) throws IllegalArgumentException
	{
		this(null, field, sorts);
	}

	/** Populator.
	 * 
	 * @param Optional default alias for the sort fields.
	 * @param field default field
	 * @param sorts map of acceptable sorts
	 */
	public OrderByBuilder(final String defaultAlias, String field, final Map<String, String> sorts) throws IllegalArgumentException
	{
		this.defaultAlias = (null != defaultAlias) ? defaultAlias + "." : null;

		if (MapUtils.isEmpty(sorts))
			throw new IllegalArgumentException("No acceptable sorts were provided.");

		if (null == (field = StringUtils.trimToNull(field)))
			throw new IllegalArgumentException("No default sort field was provided.");

		int i = 0;
		String key = null;
		acceptable = new HashMap<>(sorts.size());
		for (String key_ : sorts.keySet())
		{
			if (null == (key = StringUtils.trimToNull(key_)))
				throw new IllegalArgumentException(String.format("Sort field (%d) is missing.", ++i));

			String dir = StringUtils.trimToNull(sorts.get(key));
			if (null == dir)
				throw new IllegalArgumentException(String.format("Sort direction (%s) is missing.", key));

			acceptable.put(key, toSort(key, dir));
		}

		defaultSort = acceptable.get(field);
		if (null == defaultSort)
			throw new IllegalArgumentException(String.format("The default sort field, %s, was not provided a default sort direction.", field));
	}

	/** Populator.
	 * 
	 * @param values array of key-value pairs.
	 */
	public OrderByBuilder(final String... values) throws IllegalArgumentException
	{
		this(Character.MIN_VALUE, values);
	}

	/** Populator.
	 * 
	 * @param Optional default alias for the sort fields. MUST be 'char' to differentiate from the String variable length argument.
	 * @param values array of key-value pairs.
	 */
	public OrderByBuilder(final char defaultAlias, final String... values) throws IllegalArgumentException
	{
		this.defaultAlias = (Character.MIN_VALUE == defaultAlias) ? null : Character.toString(defaultAlias) + ".";

		if ((null == values) || (0 == values.length))
			throw new IllegalArgumentException("No acceptable sorts were provided.");

		if (2 > values.length)
			throw new IllegalArgumentException("Please provide at least one field/direction sort pair.");

		acceptable = new HashMap<>(values.length / 2);
		for (int i = 0, j = 1; j < values.length; i+=2, j+=2)
		{
			if (null == (values[i] = StringUtils.trimToNull(values[i])))
				throw new IllegalArgumentException(String.format("Sort field (%d) is missing.", (i / 2) + 1));
			if (null == (values[j] = StringUtils.trimToNull(values[j])))
				throw new IllegalArgumentException(String.format("Sort direction (%d) is missing.", (i / 2) + 1));

			acceptable.put(values[i], toSort(values[i], values[j]));
		}

		defaultSort = acceptable.get(values[0]);
	}

	/** Normalizes the sort values in the QueryResults.
	 * 
	 * @param results
	 * @return never NULL.
	 */
	@SuppressWarnings("rawtypes")
	public Sort normalize(final QueryResults results)
	{
		final Sort sort = find(results);

		results.withSortOn(sort.key).withSortDir(sort.dir);
		return sort;
	}

	/** Find the sort value based on the QueryResults.
	 * 
	 * @param results
	 * @return never NULL.
	 */
	@SuppressWarnings("rawtypes")
	public Sort find(final QueryResults results)
	{
		return find(results.sortOn, results.sortDir);
	}

	/** Find the sort value based on the specified sort field. Uses default sort direction.
	 * 
	 * @param sortOn sort field
	 * @return never NULL.
	 */
	public Sort find(final String sortOn) { return find(sortOn, null); }

	/** Find the sort value based on the specified sort field and direction.
	 * 
	 * @param sortOn sort field
	 * @param sortDir sort direction (ASC or DESC)
	 * @return never NULL.
	 */
	public Sort find(final String sortOn, final String sortDir)
	{
		Sort sort = null;
		final String key = StringUtils.trimToNull(sortOn);
		if ((null == key) ||	// Was a sort field provided?
		    (null == (sort = acceptable.get(key))))	// Is the provided sort field valid?
		{
			return defaultSort;
		}

		String dir = StringUtils.trimToNull(sortDir);
		if ((null != dir) && DIRS.contains(dir = dir.toUpperCase()) && !dir.equals(sort.dir))	// Sort direction does not match the default direction for the specified field.
			return new Sort(sort.key, sort.field, dir, ASC.equals(dir), sort.joins);	// Do NOT add the Order parameter. Indicates that this is NOT a default sort.

		// Use default sort for the specified key.
		return sort;
	}

	/** Helper method - converts a key and sort value to a Sort object. */
	private Sort toSort(final String key, String dir)
	{
		// If the direction has a semi-colon and or comma in it, split it to find the explicit field name used for the sort (example: FROM Parent p JOIN p.child c ORDER BY c.name). DLS on 7/14/2015.
		String[] joins = null;
		String field = key;

		// Break out the joins first.
		int delimiter = dir.lastIndexOf(';');
		if ((0 < delimiter) && (dir.length() - 1 > delimiter))
		{
			joins = PATTERN_COMMA.split(dir.substring(delimiter + 1));
			for (int i = 0; i < joins.length; i++)
				joins[i] = StringUtils.trimToNull(joins[i]);

			dir = dir.substring(0, delimiter);
		}

		// Break out alternate fields next.
		delimiter = dir.indexOf(',');
		if ((0 < delimiter) && (dir.length() - 1 > delimiter))
		{
			field = dir.substring(delimiter + 1);
			dir = dir.substring(0, delimiter);
		}
		else if (null != defaultAlias)	// If the 'dir' parameter doesn't contain a specific field, then prepend the defaultAlias if available.
			field = defaultAlias + field;

		if (!DIRS.contains(dir = dir.toUpperCase()))
			throw new IllegalArgumentException(String.format("The sort direction, %s, on sort field, %s, is not valid.", dir, key));

		boolean asc = ASC.equals(dir);
		return new Sort(key, field, dir, asc, joins);
	}

	/** Internal structure that represents a single Order By entry. */
	public static class Sort
	{
		public final String key;
		public final String field;
		public final String dir;
		public final boolean asc;
		public final String[] joins;

		public Sort(final String key, final String field, final String dir, final boolean asc, final String[] joins)
		{
			this.key = key;
			this.field = field;
			this.dir = dir;
			this.asc = asc; 
			this.joins = joins;
		}
	}
}
