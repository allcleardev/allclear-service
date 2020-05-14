package app.allclear.platform.filter;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.dao.QueryFilter;

/** Value object that represents a registration request search filter.
 * 
 * @author smalleyd
 * @version 1.0.39
 * @since 4/3/2020
 *
 */

public class RegistrationFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	public String phone;

	public RegistrationFilter withPhone(final String newValue) { phone = newValue; return this; }

	public RegistrationFilter clean()
	{
		phone = StringUtils.trimToNull(phone);

		return this;
	}

	/** Default/empty. */
	public RegistrationFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public RegistrationFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public RegistrationFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public RegistrationFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
