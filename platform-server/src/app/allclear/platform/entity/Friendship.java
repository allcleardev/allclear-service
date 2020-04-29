package app.allclear.platform.entity;

import java.util.*;

import javax.persistence.*;

// import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.common.value.CreatedValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the friendship table.
*
*	@author smalleyd
*	@version 1.1.9
*	@since April 29, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="friendship")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="friendship")
@NamedQueries({@NamedQuery(name="findFriendship", query="SELECT OBJECT(o) FROM Friendship o WHERE o.personId = :personId AND o.friendId = :friendId"),
	@NamedQuery(name="findFriendshipX", query="SELECT OBJECT(o) FROM Friendship o WHERE ((o.personId = :personId) AND (o.friendId = :friendId)) OR ((o.friendId = :personId) AND (o.personId = :friendId))")})
public class Friendship implements PeopleChild
{
	private final static long serialVersionUID = 1L;

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="friend_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getFriendId() { return friendId; }
	public String friendId;
	public void setFriendId(final String newValue) { friendId = newValue; }

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
	@JoinColumn(name="friend_id", nullable=false, updatable=false, insertable=false)
	public People getFriend() { return friend; }
	public People friend;
	public void setFriend(final People newValue) { friend = newValue; }
	public void putFriend(final People newValue) { friendId = (friend = newValue).getId(); }

	@Transient @Override public String getChildId() { return getFriendId(); }
	@Transient @Override public void setChildId(final String newValue) { setFriendId(newValue); }
	@Transient @Override public String getChildName() { return getFriend().getName(); }

	public Friendship() {}

	public Friendship(final String personId,
		final String friendId,
		final Date createdAt)
	{
		this.personId = personId;
		this.friendId = friendId;
		this.createdAt = createdAt;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Friendship)) return false;

		var v = (Friendship) o;
		return Objects.equals(personId, v.personId) &&
			Objects.equals(friendId, v.friendId); /* &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND); */
	}

	@Override
	public int hashCode() { return Objects.hash(personId, friendId); }

	@Transient
	public CreatedValue toValue()
	{
		return new CreatedValue(
			getFriendId(),
			getFriend().getName(),
			getCreatedAt());
	}
}
