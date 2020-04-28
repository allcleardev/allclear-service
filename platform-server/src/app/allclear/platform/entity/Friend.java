package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.platform.value.FriendValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the friend table.
*
*	@author smalleyd
*	@version 1.1.9
*	@since April 27, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="friend")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="friend")
@NamedQueries(@NamedQuery(name="findFriend", query="SELECT OBJECT(o) FROM Friend o WHERE o.personId = :personId AND o.inviteeId = :inviteeId"))
public class Friend implements Serializable
{
	private final static long serialVersionUID = 1L;

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="invitee_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getInviteeId() { return inviteeId; }
	public String inviteeId;
	public void setInviteeId(final String newValue) { inviteeId = newValue; }

	@Column(name="accepted_at", columnDefinition="DATETIME", nullable=true)
	public Date getAcceptedAt() { return acceptedAt; }
	public Date acceptedAt;
	public void setAcceptedAt(final Date newValue) { acceptedAt = newValue; }
	@Transient public boolean accepted() { return null != acceptedAt; }
	@Transient public Friend accept()
	{
		acceptedAt = new Date();
		rejectedAt = null;	// In case had been previously rejected.

		return this;
	}

	@Column(name="rejected_at", columnDefinition="DATETIME", nullable=true)
	public Date getRejectedAt() { return rejectedAt; }
	public Date rejectedAt;
	public void setRejectedAt(final Date newValue) { rejectedAt = newValue; }
	@Transient public boolean rejected() { return null != rejectedAt; }
	@Transient public Friend reject()
	{
		acceptedAt = null;	// In case had been previously accepted.
		rejectedAt = new Date();

		return this;
	}

	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="person_id", nullable=false, updatable=false, insertable=false)
	public People getPerson() { return person; }
	public People person;
	public void setPerson(final People newValue) { person = newValue; }
	public void putPerson(final People newValue) { personId = (person = newValue).getId(); }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="invitee_id", nullable=false, updatable=false, insertable=false)
	public People getInvitee() { return invitee; }
	public People invitee;
	public void setInvitee(final People newValue) { invitee = newValue; }
	public void putInvitee(final People newValue) { inviteeId = (invitee = newValue).getId(); }

	public Friend() {}

	public Friend(final People person,
		final People invitee)
	{
		this.personId = (this.person = person).getId();
		this.inviteeId = (this.invitee = invitee).getId();
		this.acceptedAt = null;
		this.rejectedAt = null;
		this.createdAt = new Date();
	}

	public Friend(final FriendValue value)
	{
		this.personId = value.personId;
		this.inviteeId = value.inviteeId;
		this.acceptedAt = value.acceptedAt;
		this.rejectedAt = value.rejectedAt;
		this.createdAt = value.createdAt = new Date();
	}

	public FriendValue update(final FriendValue value)
	{
		this.acceptedAt = value.acceptedAt;
		this.rejectedAt = value.rejectedAt;
		value.createdAt = this.getCreatedAt();

		return value;
	}

	@Override
	public int hashCode() { return personId.hashCode() ^ inviteeId.hashCode(); }

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Friend)) return false;

		var v = (Friend) o;
		return Objects.equals(personId, v.personId) &&
			Objects.equals(inviteeId, v.inviteeId) &&
			DateUtils.truncatedEquals(acceptedAt, v.acceptedAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(rejectedAt, v.rejectedAt, Calendar.SECOND) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND);
	}

	@Transient
	public FriendValue toValue()
	{
		return new FriendValue(
			getPersonId(),
			getPerson().getName(),
			getInviteeId(),
			getInvitee().getName(),
			getAcceptedAt(),
			getRejectedAt(),
			getCreatedAt());
	}
}
