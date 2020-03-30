package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import app.allclear.common.value.CreatedValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the conditions table.
*
*	@author smalleyd
*	@version 1.0.3
*	@since March 29, 2020
*
**********************************************************************************/

public interface PeopleChild extends Serializable
{
	public String getPersonId();
	public void setPersonId(final String newValue);

	public String getChildId();
	public void setChildId(final String newValue);

	public Date getCreatedAt();
	public void setCreatedAt(final Date newValue);

	public People getPerson();
	public void setPerson(final People newValue);
	public void putPerson(final People newValue);

	public CreatedValue toValue();
}
