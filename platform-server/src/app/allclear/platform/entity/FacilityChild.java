package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import app.allclear.common.value.CreatedValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the children entities of a facility.
*
*	@author smalleyd
*	@version 1.1.44
*	@since May 9, 2020
*
**********************************************************************************/

public interface FacilityChild extends Serializable
{
	public Long getFacilityId();
	public void setFacilityId(final Long newValue);

	public String getChildId();
	public void setChildId(final String newValue);

	public String getChildName();

	public Date getCreatedAt();
	public void setCreatedAt(final Date newValue);

	public Facility getFacility();
	public void setFacility(final Facility newValue);
	public void putFacility(final Facility newValue);

	public CreatedValue toValue();
}
