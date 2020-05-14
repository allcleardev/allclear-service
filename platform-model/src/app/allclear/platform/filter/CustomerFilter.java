package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for Customer query.
*
*	@author smalleyd
*	@version 1.1.0
*	@since April 26, 2020
*
*******************************************************************************************************************/

public class CustomerFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public String id = null;
	public String name = null;
	public Integer limit = null;
	public Boolean hasLimit = null;
	public Integer limitFrom = null;
	public Integer limitTo = null;
	public Boolean active = null;
	public Boolean hasLastAccessedAt = null;
	public Date lastAccessedAtFrom = null;
	public Date lastAccessedAtTo = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;

	// Mutators
	public CustomerFilter withId(final String newValue) { id = newValue; return this; }
	public CustomerFilter withName(final String newValue) { name = newValue; return this; }
	public CustomerFilter withLimit(final Integer newValue) { limit = newValue; return this; }
	public CustomerFilter withHasLimit(final Boolean newValue) { hasLimit = newValue; return this; }
	public CustomerFilter withLimitFrom(final Integer newValue) { limitFrom = newValue; return this; }
	public CustomerFilter withLimitTo(final Integer newValue) { limitTo = newValue; return this; }
	public CustomerFilter withActive(final Boolean newValue) { active = newValue; return this; }
	public CustomerFilter withHasLastAccessedAt(final Boolean newValue) { hasLastAccessedAt = newValue; return this; }
	public CustomerFilter withLastAccessedAtFrom(final Date newValue) { lastAccessedAtFrom = newValue; return this; }
	public CustomerFilter withLastAccessedAtTo(final Date newValue) { lastAccessedAtTo = newValue; return this; }
	public CustomerFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public CustomerFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }
	public CustomerFilter withUpdatedAtFrom(final Date newValue) { updatedAtFrom = newValue; return this; }
	public CustomerFilter withUpdatedAtTo(final Date newValue) { updatedAtTo = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public CustomerFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public CustomerFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public CustomerFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public CustomerFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param name represents the "name" field.
		@param limit represents the "limit" field.
		@param limitFrom represents the "limit" field - lower boundary.
		@param limitTo represents the "limit" field - upper boundary.
		@param active represents the "active" field.
		@param lastAccessedAtFrom represents the "last_accessed_at" field - lower boundary.
		@param lastAccessedAtTo represents the "last_accessed_at" field - upper boundary.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
		@param updatedAtFrom represents the "updated_at" field - lower boundary.
		@param updatedAtTo represents the "updated_at" field - upper boundary.
	*/
	public CustomerFilter(final String id,
		final String name,
		final Integer limit,
		final Integer limitFrom,
		final Integer limitTo,
		final Boolean active,
		final Date lastAccessedAtFrom,
		final Date lastAccessedAtTo,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.id = id;
		this.name = name;
		this.limit = limit;
		this.limitFrom = limitFrom;
		this.limitTo = limitTo;
		this.active = active;
		this.lastAccessedAtFrom = lastAccessedAtFrom;
		this.lastAccessedAtTo = lastAccessedAtTo;
		this.createdAtFrom = createdAtFrom;
		this.createdAtTo = createdAtTo;
		this.updatedAtFrom = updatedAtFrom;
		this.updatedAtTo = updatedAtTo;
	}

	/**************************************************************************
	*
	*	Helper methods
	*
	**************************************************************************/

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public CustomerFilter clean()
	{
		id = StringUtils.trimToNull(id);
		name = StringUtils.trimToNull(name);

		return this;
	}

	/**************************************************************************
	*
	*	Object methods
	*
	**************************************************************************/

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
