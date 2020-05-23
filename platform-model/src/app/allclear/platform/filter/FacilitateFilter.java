package app.allclear.platform.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.common.dao.QueryFilter;

/** Value object that represents a filter for facility change request searches.
 * 
 * @author smalleyd
 * @version 1.1.60
 * @since 5/22/2020
 *
 */

public class FacilitateFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;
	public static final int MAX_LEN_LOCATION = 128;

	// Members
	public String location = null;
	public Boolean gotTested = null;
	public String originatorId = null;
	public String statusId = null;
	public Boolean change = null;	// Is this a change request or a request to add a new facility?
	public Long entityId = null;	// Represents the Facility ID with which the change request is associated.
	public String promoterId = null;
	public Date promotedAtFrom = null;
	public Date promotedAtTo = null;
	public String rejecterId = null;
	public Date rejectedAtFrom = null;
	public Date rejectedAtTo = null;
	public String creatorId = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;

	// Mutators
	public FacilitateFilter withLocation(final String newValue) { this.location = newValue; return this; }
	public FacilitateFilter withGotTested(final Boolean newValue) { this.gotTested = newValue; return this; }
	public FacilitateFilter withOriginatorId(final String newValue) { this.originatorId = newValue; return this; }
	public FacilitateFilter withStatusId(final String newValue) { this.statusId = newValue; return this; }
	public FacilitateFilter withChange(final Boolean newValue) { this.change = newValue; return this; }
	public FacilitateFilter withEntityId(final Long newValue) { this.entityId = newValue; return this; }
	public FacilitateFilter withPromoterId(final String newValue) { this.promoterId = newValue; return this; }
	public FacilitateFilter withPromotedAtFrom(final Date newValue) { this.promotedAtFrom = newValue; return this; }
	public FacilitateFilter withPromotedAtTo(final Date newValue) { this.promotedAtTo = newValue; return this; }
	public FacilitateFilter withRejecterId(final String newValue) { this.rejecterId = newValue; return this; }
	public FacilitateFilter withRejectedAtFrom(final Date newValue) { this.rejectedAtFrom = newValue; return this; }
	public FacilitateFilter withRejectedAtTo(final Date newValue) { this.rejectedAtTo = newValue; return this; }
	public FacilitateFilter withCreatorId(final String newValue) { this.creatorId = newValue; return this; }
	public FacilitateFilter withCreatedAtFrom(final Date newValue) { this.createdAtFrom = newValue; return this; }
	public FacilitateFilter withCreatedAtTo(final Date newValue) { this.createdAtTo = newValue; return this; }
	public FacilitateFilter withUpdatedAtFrom(final Date newValue) { this.updatedAtFrom = newValue; return this; }
	public FacilitateFilter withUpdatedAtTo(final Date newValue) { this.updatedAtTo = newValue; return this; }

	public FacilitateFilter() {}
	public FacilitateFilter(final int page, final int pageSize) { super(page, pageSize); }
	public FacilitateFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }
	public FacilitateFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	public FacilitateFilter(
		final String location,
		final Boolean gotTested,
		final String originatorId,
		final String statusId,
		final Boolean change,
		final Long entityId,
		final String promoterId,
		final Date promotedAtFrom,
		final Date promotedAtTo,
		final String rejecterId,
		final Date rejectedAtFrom,
		final Date rejectedAtTo,
		final String creatorId,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.location = location;
		this.gotTested = gotTested;
		this.originatorId = originatorId;
		this.statusId = statusId;
		this.change = change;
		this.entityId = entityId;
		this.promoterId = promoterId;
		this.promotedAtFrom = promotedAtFrom;
		this.promotedAtTo = promotedAtTo;
		this.rejecterId = rejecterId;
		this.rejectedAtFrom = rejectedAtFrom;
		this.rejectedAtTo = rejectedAtTo;
		this.creatorId = creatorId;
		this.createdAtFrom = createdAtFrom;
		this.createdAtTo = createdAtTo;
		this.updatedAtFrom = updatedAtFrom;
		this.updatedAtTo = updatedAtTo;
	}

	public FacilitateFilter clean()
	{
		location = StringUtils.trimToNull(location);
		originatorId = StringUtils.trimToNull(originatorId);
		statusId = StringUtils.trimToNull(statusId);
		promoterId = StringUtils.trimToNull(promoterId);
		rejecterId = StringUtils.trimToNull(rejecterId);
		creatorId = StringUtils.trimToNull(creatorId);

		return this;
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
