package app.allclear.platform.filter;

import static java.util.stream.Collectors.toList;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.dao.QueryFilter;
import app.allclear.platform.type.Experience;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for Experiences query.
*
*	@author smalleyd
*	@version 1.1.80
*	@since June 2, 2020
*
*******************************************************************************************************************/

public class ExperiencesFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public Long id = null;
	public String personId = null;
	public Long facilityId = null;
	public Boolean positive = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;
	public List<String> includeTags = null;
	public List<String> excludeTags = null;

	// Mutators
	public ExperiencesFilter withId(final Long newValue) { id = newValue; return this; }
	public ExperiencesFilter withPersonId(final String newValue) { personId = newValue; return this; }
	public ExperiencesFilter withFacilityId(final Long newValue) { facilityId = newValue; return this; }
	public ExperiencesFilter withPositive(final Boolean newValue) { positive = newValue; return this; }
	public ExperiencesFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public ExperiencesFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }
	public ExperiencesFilter withUpdatedAtFrom(final Date newValue) { updatedAtFrom = newValue; return this; }
	public ExperiencesFilter withUpdatedAtTo(final Date newValue) { updatedAtTo = newValue; return this; }
	public ExperiencesFilter withIncludeTags(final List<String> newValues) { includeTags = newValues; return this; }
	public ExperiencesFilter withIncludeTags(final Experience... newValues) { return withIncludeTags(Arrays.asList(newValues).stream().map(v -> v.id).collect(toList())); }
	public ExperiencesFilter withExcludeTags(final List<String> newValues) { excludeTags = newValues; return this; }
	public ExperiencesFilter withExcludeTags(final Experience... newValues) { return withExcludeTags(Arrays.asList(newValues).stream().map(v -> v.id).collect(toList())); }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public ExperiencesFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public ExperiencesFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public ExperiencesFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public ExperiencesFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param personId represents the "person_id" field.
		@param facilityId represents the "facility_id" field.
		@param positive represents the "positive" field.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
		@param updatedAtFrom represents the "updated_at" field - lower boundary.
		@param updatedAtTo represents the "updated_at" field - upper boundary.
	*/
	public ExperiencesFilter(final Long id,
		final String personId,
		final Long facilityId,
		final Boolean positive,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.id = id;
		this.personId = personId;
		this.facilityId = facilityId;
		this.positive = positive;
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
	public ExperiencesFilter clean()
	{
		personId = StringUtils.trimToNull(personId);

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
