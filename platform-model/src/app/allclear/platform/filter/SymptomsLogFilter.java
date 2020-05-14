package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for SymptomsLog query.
*
*	@author smalleyd
*	@version 1.0.80
*	@since April 8, 2020
*
*******************************************************************************************************************/

public class SymptomsLogFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public Long id = null;
	public String personId = null;
	public String symptomId = null;
	public Date startedAtFrom = null;
	public Date startedAtTo = null;
	public Boolean hasEndedAt = null;
	public Date endedAtFrom = null;
	public Date endedAtTo = null;

	// Mutators
	public SymptomsLogFilter withId(final Long newValue) { id = newValue; return this; }
	public SymptomsLogFilter withPersonId(final String newValue) { personId = newValue; return this; }
	public SymptomsLogFilter withSymptomId(final String newValue) { symptomId = newValue; return this; }
	public SymptomsLogFilter withStartedAtFrom(final Date newValue) { startedAtFrom = newValue; return this; }
	public SymptomsLogFilter withStartedAtTo(final Date newValue) { startedAtTo = newValue; return this; }
	public SymptomsLogFilter withHasEndedAt(final Boolean newValue) { hasEndedAt = newValue; return this; }
	public SymptomsLogFilter withEndedAtFrom(final Date newValue) { endedAtFrom = newValue; return this; }
	public SymptomsLogFilter withEndedAtTo(final Date newValue) { endedAtTo = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public SymptomsLogFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public SymptomsLogFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public SymptomsLogFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public SymptomsLogFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param personId represents the "person_id" field.
		@param symptomId represents the "symptom_id" field.
		@param startedAtFrom represents the "started_at" field - lower boundary.
		@param startedAtTo represents the "started_at" field - upper boundary.
		@param endedAtFrom represents the "ended_at" field - lower boundary.
		@param endedAtTo represents the "ended_at" field - upper boundary.
	*/
	public SymptomsLogFilter(final Long id,
		final String personId,
		final String symptomId,
		final Date startedAtFrom,
		final Date startedAtTo,
		final Date endedAtFrom,
		final Date endedAtTo)
	{
		this.id = id;
		this.personId = personId;
		this.symptomId = symptomId;
		this.startedAtFrom = startedAtFrom;
		this.startedAtTo = startedAtTo;
		this.endedAtFrom = endedAtFrom;
		this.endedAtTo = endedAtTo;
	}

	/**************************************************************************
	*
	*	Helper methods
	*
	**************************************************************************/

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public SymptomsLogFilter clean()
	{
		personId = StringUtils.trimToNull(personId);
		symptomId = StringUtils.trimToNull(symptomId);

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
