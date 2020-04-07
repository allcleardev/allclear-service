package app.allclear.platform.filter;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.dao.QueryFilter;

/** Value object that represents a session request search filter.
 * 
 * @author smalleyd
 * @version 1.0.68
 * @since 4/7/2020
 *
 */

public class SessionFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	public String id;

	public SessionFilter withId(final String newValue) { id = newValue; return this; }

	public SessionFilter clean()
	{
		id = StringUtils.trimToNull(id);

		return this;
	}

	/** Default/empty. */
	public SessionFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public SessionFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public SessionFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public SessionFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }
}
