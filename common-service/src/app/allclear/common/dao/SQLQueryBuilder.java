package app.allclear.common.dao;

import static app.allclear.common.dao.OrderByBuilder.Sort;

import java.util.List;

/** Default implementation of the AbstractQueryBuilder to only construct the SQL.
 *  All abstract methods throw an UnsupportedOperationException.
 *
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */
public class SQLQueryBuilder extends QueryBuilder<Void>
{
	public SQLQueryBuilder(final String select)
	{
		super(select, Void.class);
	}

	public SQLQueryBuilder(final String select, final String groupBy)
	{
		super(select, Void.class, groupBy);
	}

	public SQLQueryBuilder(final String select, final Sort orderBy)
	{
		super(select, Void.class, orderBy);
	}

	public SQLQueryBuilder(final String select, final String groupBy, final Sort orderBy)
	{
		super(select, Void.class, groupBy, orderBy);
	}

	@Override
	public List<Void> run() { throw new UnsupportedOperationException("Not implemented"); }

	@Override
	public List<Void> run(int firstResult, int maxResults) { throw new UnsupportedOperationException("Not implemented"); }

	@Override
	public List<Void> run(QueryResults<?, ?> results) { throw new UnsupportedOperationException("Not implemented"); }

	@Override
	public List<Void> run(String select) { throw new UnsupportedOperationException("Not implemented"); }

	@Override
	public long aggregate(String select) { throw new UnsupportedOperationException("Not implemented"); }
}
