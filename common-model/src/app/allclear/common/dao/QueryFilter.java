package app.allclear.common.dao;

import java.io.Serializable;

/** Base value object that represents the basic details of all entity queries.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class QueryFilter implements Serializable
{
	private static final long serialVersionUID = 1L;

	// Members
	public Integer page = null;
	public Integer pageSize = null;
	public String sortOn = null;
	public String sortDir = null;

	// Accessors
	public int page() { return ((null == page) || (1 > page)) ? 1 : page; }

	// Mutators
	public int pageSize(int defaultValue)
	{
		if ((null == pageSize) || (1 > pageSize)) pageSize = defaultValue;

		return pageSize;
	}

	/** Default/empty. */
	public QueryFilter() {}

	/** Populator.
	 * 
	 * @param page
	 * @param pageSize
	 */
	public QueryFilter(final int page, final int pageSize)
	{
		this.page = page;
		this.pageSize = pageSize;
	}

	/** Populator.
	 * 
	 * @param sortOn
	 * @param sortDir
	 */
	public QueryFilter(final String sortOn, final String sortDir)
	{
		this.sortOn = sortOn;
		this.sortDir = sortDir;
	}

	/** Populator.
	 * 
	 * @param page
	 * @param pageSize
	 * @param sortOn
	 * @param sortDir
	 */
	public QueryFilter(final int page, final int pageSize, final String sortOn, final String sortDir)
	{
		this.page = page;
		this.pageSize = pageSize;
		this.sortOn = sortOn;
		this.sortDir = sortDir;
	}
}
