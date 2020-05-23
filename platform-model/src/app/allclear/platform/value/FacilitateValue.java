package app.allclear.platform.value;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import app.allclear.common.ObjectUtils;
import app.allclear.platform.type.CrowdsourceStatus;
import app.allclear.platform.type.Originator;

/** Value object that represents a facility change request.
 * 
 * @author smalleyd
 * @version 1.1.59
 * @since 5/20/2020
 *
 */

public class FacilitateValue implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final int MAX_LEN_LOCATION = 128;

	// Members
	public String id = null;
	public FacilityValue value = null;
	public String location = null;
	public boolean gotTested = false;
	public String originatorId = null;
	public Originator originator = null;
	public String statusId = null;
	public CrowdsourceStatus status = null;
	public boolean change = false;	// Is this a change request or a request to add a new facility?
	public Long entityId = null;	// Represents the Facility ID with which change request is associated.
	public String promoterId = null;
	public Date promotedAt = null;
	public String rejecterId = null;
	public Date rejectedAt = null;
	public String creatorId = null;
	public Date createdAt = null;
	public Date updatedAt = null;

	// Accessors
	public String createdAt() { return createdAt.getTime() + ""; }

	// Mutators
	public FacilitateValue withId(final String newValue) { this.id = newValue; return this; }
	public FacilitateValue withValue(final FacilityValue newValue) { this.value = newValue; return this; }
	public FacilitateValue withLocation(final String newValue) { this.location = newValue; return this; }
	public FacilitateValue withGotTested(final boolean newValue) { this.gotTested = newValue; return this; }
	public FacilitateValue withOriginatorId(final String newValue) { this.originatorId = newValue; return this; }
	public FacilitateValue withOriginator(final Originator newValue) { this.originatorId = (this.originator = newValue).id; return this; }
	public FacilitateValue withStatusId(final String newValue) { this.statusId = newValue; return this; }
	public FacilitateValue withStatus(final CrowdsourceStatus newValue) { this.statusId = (this.status = newValue).id; return this; }
	public FacilitateValue withChange(final boolean newValue) { this.change = newValue; return this; }
	public FacilitateValue withEntityId(final Long newValue) { this.entityId = newValue; return this; }
	public FacilitateValue withPromoterId(final String newValue) { this.promoterId = newValue; return this; }
	public FacilitateValue withPromotedAt(final Date newValue) { this.promotedAt = newValue; return this; }
	public FacilitateValue withRejecterId(final String newValue) { this.rejecterId = newValue; return this; }
	public FacilitateValue withRejectedAt(final Date newValue) { this.rejectedAt = newValue; return this; }
	public FacilitateValue withCreatorId(final String newValue) { this.creatorId = newValue; return this; }
	public FacilitateValue withCreatedAt(final Date newValue) { this.createdAt = newValue; return this; }
	public FacilitateValue withUpdatedAt(final Date newValue) { this.updatedAt = newValue; return this; }

	public FacilitateValue() {}

	public FacilitateValue(
		final FacilityValue value,
		final String location,
		final boolean gotTested)
	{
		this.value = value;
		this.location = location;
		this.gotTested = gotTested;
	}

	public FacilitateValue(final String id,
		final FacilityValue value,
		final String location,
		final boolean gotTested,
		final Originator originator,
		final CrowdsourceStatus status,
		final boolean change,
		final Long entityId,
		final String promoterId,
		final Date promotedAt,
		final String rejecterId,
		final Date rejectedAt,
		final String creatorId,
		final Date createdAt,
		final Date updatedAt)
	{
		this.id = id;
		this.value = value;
		this.location = location;
		this.gotTested = gotTested;
		this.originatorId = (this.originator = originator).id;
		this.statusId = (this.status = status).id;
		this.change = change;
		this.entityId = entityId;
		this.promoterId = promoterId;
		this.promotedAt = promotedAt;
		this.rejecterId = rejecterId;
		this.rejectedAt = rejectedAt;
		this.creatorId = creatorId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public void clean()
	{
		if (null != value) value.clean();

		location = StringUtils.trimToNull(location);
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof FacilitateValue)) return false;

		var v = (FacilitateValue) o;

		return Objects.equals(id, v.id) &&
			Objects.equals(value, v.value) &&
			Objects.equals(location, v.location) &&
			(gotTested == v.gotTested) &&
			Objects.equals(originatorId, v.originatorId) &&
			Objects.equals(statusId, v.statusId) &&
			(change == v.change) &&
			Objects.equals(entityId, v.entityId) &&
			Objects.equals(promoterId, v.promoterId) &&
			((promotedAt == v.promotedAt) || DateUtils.truncatedEquals(promotedAt, v.promotedAt, Calendar.SECOND)) &&
			Objects.equals(rejecterId, v.rejecterId) &&
			((rejectedAt == v.rejectedAt) || DateUtils.truncatedEquals(rejectedAt, v.rejectedAt, Calendar.SECOND)) &&
			Objects.equals(creatorId, v.creatorId) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(updatedAt, v.updatedAt, Calendar.SECOND);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
