package app.allclear.common.dao;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** Value object that represents the results of an entity query.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class QueryResults<T, F extends QueryFilter> implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Constant - default page size. */
	public static final int PAGE_SIZE_DEFAULT = 20;

	public long total = 0L;
	public int page = 0;
	public int pages = 0;
	public int pageSize = 0;
	public String sortOn = null;
	public String sortDir = null;
	public F filter = null;
	public List<T> records = null;

	@JsonIgnore
	public boolean isEmpty() { return (0L == total); }

	@JsonIgnore
	public boolean noRecords() { return CollectionUtils.isEmpty(records); }

	/** Helper method - calculates the Hibernate Criteria.firstResult property based on the page and page size. */
	public int firstResult() { return (0 < page) ? ((page - 1) * pageSize) : 0; }

	@SuppressWarnings("rawtypes") public QueryResults withSortOn(String newValue) { sortOn = newValue; return this; }

	@SuppressWarnings("rawtypes") public QueryResults withSortDir(String newValue) { sortDir = newValue; return this; }

	public QueryResults<T, F> withRecords(List<T> newValues) { records = newValues; return this; }

	/** Empty/default. */
	public QueryResults() {}

	/** Populator.
	 * 
	 * @param total
	 * @param filter
	 */
	public QueryResults(long total, F filter)
	{
		this(total, filter, PAGE_SIZE_DEFAULT);
	}

	/** Populator.
	 * 
	 * @param total
	 * @param filter
	 * @param pageSizeDefault
	 */
	public QueryResults(final long total, final F filter, final int pageSizeDefault)
	{
		this.filter = filter;
		this.sortOn = filter.sortOn;
		this.sortDir = filter.sortDir;
		this.pages = (int) ((this.total = total) / ((long) (this.pageSize = filter.pageSize(pageSizeDefault))));
		if (0 < (total % pageSize))
			this.pages++;

		this.page = filter.page();
		if (this.pages < this.page)
			this.page = this.pages;
	}

	/** Populator.
	 * 
	 * @param records
	 * @param filter
	 */
	public QueryResults(final List<T> records, final F filter)
	{
		this.filter = filter;
		this.pageSize = (this.records = records).size();
		this.total = (long) this.pageSize;
		this.page = 1;
		this.pages = 1;
	}
}
