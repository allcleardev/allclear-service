package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for Tests query.
*
*	@author smalleyd
*	@version 1.0.44
*	@since April 4, 2020
*
*******************************************************************************************************************/

public class TestsFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public Long id = null;
	public String personId = null;
	public String typeId = null;
	public Date takenOn = null;
	public Date takenOnFrom = null;
	public Date takenOnTo = null;
	public Long facilityId = null;
	public Boolean positive = null;
	public String notes = null;
	public Boolean hasNotes = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;

	// Mutators
	public TestsFilter withId(final Long newValue) { id = newValue; return this; }
	public TestsFilter withPersonId(final String newValue) { personId = newValue; return this; }
	public TestsFilter withTypeId(final String newValue) { typeId = newValue; return this; }
	public TestsFilter withTakenOn(final Date newValue) { takenOn = newValue; return this; }
	public TestsFilter withTakenOnFrom(final Date newValue) { takenOnFrom = newValue; return this; }
	public TestsFilter withTakenOnTo(final Date newValue) { takenOnTo = newValue; return this; }
	public TestsFilter withFacilityId(final Long newValue) { facilityId = newValue; return this; }
	public TestsFilter withPositive(final Boolean newValue) { positive = newValue; return this; }
	public TestsFilter withNotes(final String newValue) { notes = newValue; return this; }
	public TestsFilter withHasNotes(final Boolean newValue) { hasNotes = newValue; return this; }
	public TestsFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public TestsFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }
	public TestsFilter withUpdatedAtFrom(final Date newValue) { updatedAtFrom = newValue; return this; }
	public TestsFilter withUpdatedAtTo(final Date newValue) { updatedAtTo = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public TestsFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public TestsFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public TestsFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public TestsFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param personId represents the "person_id" field.
		@param typeId represents the "type_id" field.
		@param takenOn represents the "taken_on" field.
		@param takenOnFrom represents the "taken_on" field - lower boundary.
		@param takenOnTo represents the "taken_on" field - upper boundary.
		@param facilityId represents the "facility_id" field.
		@param positive represents the "positive" field.
		@param notes represents the "notes" field.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
		@param updatedAtFrom represents the "updated_at" field - lower boundary.
		@param updatedAtTo represents the "updated_at" field - upper boundary.
	*/
	public TestsFilter(final Long id,
		final String personId,
		final String typeId,
		final Date takenOn,
		final Date takenOnFrom,
		final Date takenOnTo,
		final Long facilityId,
		final Boolean positive,
		final String notes,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.id = id;
		this.personId = personId;
		this.typeId = typeId;
		this.takenOn = takenOn;
		this.takenOnFrom = takenOnFrom;
		this.takenOnTo = takenOnTo;
		this.facilityId = facilityId;
		this.positive = positive;
		this.notes = notes;
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
	public TestsFilter clean()
	{
		personId = StringUtils.trimToNull(personId);
		typeId = StringUtils.trimToNull(typeId);
		notes = StringUtils.trimToNull(notes);

		return this;
	}

	/**************************************************************************
	*
	*	Object methods
	*
	**************************************************************************/

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", personId: ").append(personId)
			.append(", typeId: ").append(typeId)
			.append(", takenOn: ").append(takenOn)
			.append(", takenOnFrom: ").append(takenOnFrom)
			.append(", takenOnTo: ").append(takenOnTo)
			.append(", facilityId: ").append(facilityId)
			.append(", positive: ").append(positive)
			.append(", notes: ").append(notes)
			.append(", hasNotes: ").append(hasNotes)
			.append(", createdAtFrom: ").append(createdAtFrom)
			.append(", createdAtTo: ").append(createdAtTo)
			.append(", updatedAtFrom: ").append(updatedAtFrom)
			.append(", updatedAtTo: ").append(updatedAtTo)
			.append(" }").toString();
	}
}
