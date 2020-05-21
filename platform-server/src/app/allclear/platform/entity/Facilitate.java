package app.allclear.platform.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.table.TableServiceEntity;

import app.allclear.common.jackson.JacksonUtils;
import app.allclear.platform.type.CrowdsourceStatus;
import app.allclear.platform.type.Originator;
import app.allclear.platform.value.FacilitateValue;
import app.allclear.platform.value.FacilityValue;

/** Azure Cosmos entity that represents change requests to Facilities.
 * 
 * @author smalleyd
 * @version 1.1.60
 * @since 5/21/2020
 *
 */

public class Facilitate extends TableServiceEntity implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final String FORMAT_ID = "%s/%s";
	private static final ObjectMapper mapper = JacksonUtils.createMapperMS();

	public String statusId() { return getPartitionKey(); }
	public void status(final CrowdsourceStatus newValue) { setPartitionKey(newValue.id); }

	public String getPayload() { return payload; }
	public String payload;
	public void setPayload(final String newValue) { payload = newValue; }
	public FacilityValue payload()
	{
		try { return mapper.readValue(payload, FacilityValue.class); }
		catch (final IOException ex) { throw new RuntimeException(ex); }
	}
	public void payload(final FacilityValue newValue)
	{
		try { payload = mapper.writeValueAsString(newValue); }
		catch (final IOException ex) { throw new RuntimeException(ex); }
	}

	public String getLocation() { return location; }
	public String location;
	public void setLocation(final String newValue) { location = newValue; }

	public boolean getGotTested() { return gotTested; }
	public boolean gotTested = false;
	public void setGotTested(final boolean newValue) { gotTested = newValue; }

	public String getOriginatorId() { return originatorId; }
	public String originatorId = null;
	public void setOriginatorId(final String newValue) { originatorId = newValue; }

	public boolean getChange() { return change; }
	public boolean change = false;
	public void setChange(final boolean newValue) { change = newValue; }

	public Long getPromotedId() { return promotedId; }
	public Long promotedId;
	public void setPromotedId(final Long newValue) { promotedId = newValue; }

	public String getPromoterId() { return promoterId; }
	public String promoterId = null;
	public void setPromoterId(final String newValue) { promoterId = newValue; }
	public Facilitate promote(final String userId, final Long promotedId)
	{
		this.promotedId = promotedId;
		this.promoterId = userId;
		this.promotedAt = new Date();
		status(CrowdsourceStatus.PROMOTED);

		return this;
	}

	public Date getPromotedAt() { return promotedAt; }
	public Date promotedAt = null;
	public void setPromotedAt(final Date newValue) { promotedAt = newValue; }

	public String getRejecterId() { return rejecterId; }
	public String rejecterId = null;
	public void setRejecterId(final String newValue) { rejecterId = newValue; }
	public Facilitate reject(final String userId)
	{
		this.rejecterId = userId;
		this.rejectedAt = new Date();
		status(CrowdsourceStatus.REJECTED);

		return this;
	}

	public Date getRejectedAt() { return rejectedAt; }
	public Date rejectedAt = null;
	public void setRejectedAt(final Date newValue) { rejectedAt = newValue; }

	public String getCreatorId() { return creatorId; }
	public String creatorId = null;
	public void setCreatorId(final String newValue) { creatorId = newValue; }

	public long getCreatedAt() { return createdAt; }
	public long createdAt;	// Needed for searches.
	public void setCreatedAt(final long newValue) { createdAt = newValue; }
	public Date createdAt() { return new Date(createdAt); }

	public long getUpdatedAt() { return updatedAt; }
	public long updatedAt;	// Needed for searches.
	public void setUpdatedAt(final long newValue) { updatedAt = newValue; }

	public Facilitate(final FacilitateValue value)
	{
		super(value.statusId, (value.createdAt = value.updatedAt = new Date()).getTime() + "");

		this.payload(value.value);
		this.location = value.location;
		this.gotTested = value.gotTested;
		this.originatorId = value.originatorId;
		this.change = value.change;
		this.creatorId = value.creatorId;
		this.createdAt = value.createdAt.getTime();
		this.updatedAt = value.updatedAt.getTime();
	}

	public FacilitateValue toValue()
	{
		return new FacilitateValue(
			String.format(FORMAT_ID, getPartitionKey(), getRowKey()),
			payload(),
			location,
			gotTested,
			Originator.get(originatorId),
			CrowdsourceStatus.get(statusId()),
			change,
			promotedId,
			promoterId,
			promotedAt,
			rejecterId,
			rejectedAt,
			creatorId,
			createdAt(),
			getTimestamp());
	}
}
