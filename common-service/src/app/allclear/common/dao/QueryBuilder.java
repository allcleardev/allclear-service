package app.allclear.common.dao;

import static java.util.stream.Collectors.*;
import static app.allclear.common.dao.OrderByBuilder.Sort;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.*;

/** Abstract builder class that helps construct database queries including joins and where clause.
 *  Can be used by Hibernate or JDBI to implement framework specified query builders.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 * @param <T>
 */
public abstract class QueryBuilder<T>
{
	private static final Logger log = LoggerFactory.getLogger(QueryBuilder.class);
	private static final Pattern PARAM_SUBSTITUTION = Pattern.compile("\\{\\}");
	private static final String OR = " OR ";
	private static final String AND = " AND ";

	public final String select;
	public final Class<T> entity;
	public final String groupBy;
	public Sort orderBy;
	public final StringBuilder where = new StringBuilder();
	private String conjunction = AND;
	public final Map<String, Object> parameters = new HashMap<>();
	public final List<String> joins = new LinkedList<>();	// Could use a HashSet, but the number of items (1 to 2) will generally be too small to be effective. DLS on 2/22/2016.

	/** Populator.
	 * 
	 * @param select SELECT clause.
	 * @param entity output class for each record.
	 */
	public QueryBuilder(final String select, final Class<T> entity)
	{
		this(select, entity, null, null);
	}

	/** Populator.
	 * 
	 * @param select SELECT clause.
	 * @param entity output class for each record.
	 * @param groupBy GROUP BY clause - optional.
	 */
	public QueryBuilder(final String select, final Class<T> entity, final String groupBy)
	{
		this(select, entity, groupBy, null);
	}

	/** Populator.
	 * 
	 * @param select SELECT clause.
	 * @param entity output class for each record.
	 * @param orderBy Sort object for the ORDER BY clause - optional.
	 */
	public QueryBuilder(final String select, final Class<T> entity, final Sort orderBy)
	{
		this(select, entity, null, orderBy);
	}

	/** Populator.
	 * 
	 * @param select SELECT clause.
	 * @param entity output class for each record.
	 * @param groupBy GROUP BY clause - optional.
	 * @param orderBy Sort object for the ORDER BY clause - optional.
	 */
	public QueryBuilder(final String select, final Class<T> entity, final String groupBy, final Sort orderBy)
	{
		this.select = select;
		this.entity = entity;
		this.groupBy = groupBy;
		orderBy(orderBy);
	}

	/** Conjunction mutators. */
	public QueryBuilder<T> or() { conjunction = OR; return this; }
	public QueryBuilder<T> or(final boolean turnOn) { return turnOn ? or() : and(); }
	public QueryBuilder<T> and() { conjunction = AND; return this; }
	public QueryBuilder<T> and(final boolean turnOn) { return turnOn ? and() : or(); }

	/** Helper method - add to the WHERE clause. */
	private void where(final String value)
	{
		if (0 < where.length())
			where.append(conjunction);
		where.append(value);
	}

	/** Add the specified JOINs.
	 * 
	 * @param values
	 * @return SELF
	 */
	public QueryBuilder<T> joins(final String... values)
	{
		join(values);

		return this;
	}

	/** Helper method - add to the join portion of the FROM clause. */
	private void join(final String... values)
	{
		if (ArrayUtils.isNotEmpty(values))
			for (var value : values)
				if ((null != value) && !joins.contains(value))
					joins.add(value);
	}

	/** Helper method - puts parameter appends to the WHERE & JOIN clauses. */
	private void put(final String name, final String segment, final Object value, final String... joins)
	{
		parameters.put(name, value);
		where(segment);
		join(joins);
	}

	/** Adds a simple WHERE clause segment with a single JOIN segment.
	 * 
	 * @param name
	 * @param segment
	 * @param value
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> add(final String name, final String segment, final Object value, final String... joins)
	{
		if (null != value)
			put(name, segment, value, joins);

		return this;
	}

	/** Adds a WHERE segment that expands into a list of value. Segment must include an IN operator.
	 * 
	 * @param name
	 * @param segment
	 * @param values array of values.
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> addIn(final String name, final String segment, final Object[] values, final String... joins)
	{
		if (ArrayUtils.isEmpty(values))
			return this;

		return addIn(name, segment, Arrays.stream(values), joins);
	}

	/** Adds a WHERE segment that expands into a list of value. Segment must include an IN operator.
	 * 
	 * @param name
	 * @param segment
	 * @param values collection of values.
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> addIn(final String name, final String segment, final Collection<?> values, final String... joins)
	{
		if (CollectionUtils.isEmpty(values))
			return this;

		return addIn(name, segment, values.stream(), joins);
	}

	/** Adds a WHERE segment that expands into a list of value. Segment must include an IN operator.
	 * 
	 * @param name
	 * @param segment
	 * @param values stream of values.
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> addIn(final String name, final String segment, final Stream<?> values, final String... joins)
	{
		// Build the IN segment.
		var i = new int[] { 0 };
		var inSegment = values.map(v -> {
			var param = (name + "_" + (++i[0]));
			parameters.put(param, v);
			return ":" + param;
		}).collect(joining(",", "(", ")"));

		where(PARAM_SUBSTITUTION.matcher(segment).replaceAll(inSegment));
		join(joins);

		return this;
	}

	/** Adds a WHERE segment that expands into a list of value. Segment must include an IN operator.
	 *  This IN version uses literals instead of parameter binding. Provides optimization for big queries.
	 * 
	 * @param name
	 * @param segment
	 * @param values array of values.
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> addLiteralIn(final String name, final String segment, final Collection<?> values, final String... joins)
	{
		if (CollectionUtils.isEmpty(values))
			return this;

		where(PARAM_SUBSTITUTION.matcher(segment).replaceAll((String) values.stream().map(v -> v.toString()).collect(joining(",", "(", ")"))));
		join(joins);

		return this;
	}

	/** Adds an EXISTS or NOT EXISTS segment.
	 * 
	 * @param name
	 * @param segment
	 * @param value TRUE adds an IS NOT NULL segment.
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> addExists(final String segment, final Boolean value, final String... joins)
	{
		if (null != value)
		{
			where((value ? "" : "NOT ") + "EXISTS (" + segment + ")");
			join(joins);
		}

		return this;
	}

	/** Adds an IS NULL or IS NOT NULL segment.
	 * 
	 * @param field
	 * @param value TRUE adds an IS NOT NULL segment.
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> addNotNull(final String field, final Boolean value, final String... joins)
	{
		if (null != value)
		{
			where(field + " IS" + (value ? " NOT" : "") + " NULL");
			join(joins);
		}

		return this;
	}

	/** Adds a LIKE segment where the VALUE is anywhere in the field with a JOIN segment.
	 * 
	 * @param name
	 * @param segment
	 * @param value
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> addContains(final String name, final String segment, final String value, final String... joins)
	{
		if (null != value)
			put(name, segment, "%" + value + "%", joins);

		return this;
	}

	/** Adds a LIKE segment where the VALUE starts the field with a JOIN segment.
	 * 
	 * @param name
	 * @param segment
	 * @param value
	 * @param joins
	 * @return SELF
	 */
	public QueryBuilder<T> addStarts(final String name, final String segment, final String value, final String... joins)
	{
		if (null != value)
			put(name, segment, value + "%", joins);

		return this;
	}

	/** Sets the sort. */
	public QueryBuilder<T> orderBy(final Sort value)
	{
		if (null != (orderBy = value))
			join(orderBy.joins);

		return this;
	}

	/** Builds the full SQL statement after all WHERE and JOIN segments have been added.
	 * 
	 * @return never NULL.
	 */
	public String build() { return build(select); }

	/** Builds the full SQL statement after all WHERE and JOIN segments have been added.
	 * 
	 * @param select substitute alternate SELECT clause. Used for COUNTs.
	 * @return never NULL.
	 */
	public String build(final String select)
	{
		final StringBuilder results = new StringBuilder(select);
		for (String join : joins)
			results.append(" ").append(join);

		if (0 < where.length())
			results.append(" WHERE ").append(where.toString());

		if (null != groupBy)
			results.append(" ").append(groupBy);

		if (null != orderBy)
			results.append(" ORDER BY ").append(orderBy.field).append(" ").append(orderBy.dir);

		if (log.isDebugEnabled())
			log.debug("QUERY-BUILDER-SQL: {} [ {} ]", results.toString(), parameters.entrySet().stream().map(o -> o.getKey() + ": " + o.getValue()).collect(joining(", ")));

		return results.toString();
	}

	/** Builds the query, binds the parameters, and executes the query.
	 * 
	 * @return the resulting records.
	 */
	public abstract List<T> run();

	/** Builds the query, binds the parameters, and executes the query.
	 *
	 * @param firstResult a row number, numbered from <tt>0</tt>
	 * @param maxResults the maximum number of rows 
	 * @return the resulting records.
	 */
	public abstract List<T> run(final int firstResult, final int maxResults);

	/** Builds the query, binds the parameters, and executes the query. Convenience method for
	 *  <tt>run(firstResult, maxResults)</tt>.
	 *
	 * @param results provides the firstResult and maxResults. 
	 * @return the resulting records.
	 */
	public abstract List<T> run(final QueryResults<?, ?> results);

	/** Builds the query, binds the parameters, and executes the query.
	 * 
	 * @param select substitute alternate SELECT clause. Used for COUNTs.
	 * @return the resulting records.
	 */
	public abstract List<T> run(final String select);

	/** Builds the query, binds the parameters, and executes the query.
	 * 
	 * @param select aggregate SELECT and FROM clause.
	 * @return the aggregate value.
	 */
	public abstract long aggregate(final String select);
}
