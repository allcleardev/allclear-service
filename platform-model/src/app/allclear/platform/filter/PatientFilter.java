package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for Patient query.
*
*	@author smalleyd
*	@version 1.1.111
*	@since July 18, 2020
*
*******************************************************************************************************************/

public class PatientFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public Long id = null;
	public Long facilityId = null;
	public String personId = null;
	public Boolean alertable = null;
	public Boolean hasEnrolledAt = null;
	public Date enrolledAtFrom = null;
	public Date enrolledAtTo = null;
	public Boolean hasRejectedAt = null;
	public Date rejectedAtFrom = null;
	public Date rejectedAtTo = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;

	// Mutators
	public PatientFilter withId(final Long newValue) { id = newValue; return this; }
	public PatientFilter withFacilityId(final Long newValue) { facilityId = newValue; return this; }
	public PatientFilter withPersonId(final String newValue) { personId = newValue; return this; }
	public PatientFilter withAlertable(final Boolean newValue) { alertable = newValue; return this; }
	public PatientFilter withHasEnrolledAt(final Boolean newValue) { hasEnrolledAt = newValue; return this; }
	public PatientFilter withEnrolledAtFrom(final Date newValue) { enrolledAtFrom = newValue; return this; }
	public PatientFilter withEnrolledAtTo(final Date newValue) { enrolledAtTo = newValue; return this; }
	public PatientFilter withHasRejectedAt(final Boolean newValue) { hasRejectedAt = newValue; return this; }
	public PatientFilter withRejectedAtFrom(final Date newValue) { rejectedAtFrom = newValue; return this; }
	public PatientFilter withRejectedAtTo(final Date newValue) { rejectedAtTo = newValue; return this; }
	public PatientFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public PatientFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }
	public PatientFilter withUpdatedAtFrom(final Date newValue) { updatedAtFrom = newValue; return this; }
	public PatientFilter withUpdatedAtTo(final Date newValue) { updatedAtTo = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public PatientFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public PatientFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public PatientFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public PatientFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param facilityId represents the "facility_id" field.
		@param personId represents the "person_id" field.
		@param alertable represents the "alertable" field.
		@param enrolledAtFrom represents the "enrolled_at" field - lower boundary.
		@param enrolledAtTo represents the "enrolled_at" field - upper boundary.
		@param rejectedAtFrom represents the "rejected_at" field - lower boundary.
		@param rejectedAtTo represents the "rejected_at" field - upper boundary.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
		@param updatedAtFrom represents the "updated_at" field - lower boundary.
		@param updatedAtTo represents the "updated_at" field - upper boundary.
	*/
	public PatientFilter(final Long id,
		final Long facilityId,
		final String personId,
		final Boolean alertable,
		final Date enrolledAtFrom,
		final Date enrolledAtTo,
		final Date rejectedAtFrom,
		final Date rejectedAtTo,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.id = id;
		this.facilityId = facilityId;
		this.personId = personId;
		this.alertable = alertable;
		this.enrolledAtFrom = enrolledAtFrom;
		this.enrolledAtTo = enrolledAtTo;
		this.rejectedAtFrom = rejectedAtFrom;
		this.rejectedAtTo = rejectedAtTo;
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
	public PatientFilter clean()
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
