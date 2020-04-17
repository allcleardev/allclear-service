package app.allclear.platform.entity;

import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.common.value.CreatedValue;
import app.allclear.platform.type.Condition;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the conditions table.
*
*	@author smalleyd
*	@version 1.0.3
*	@since March 29, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="conditions")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="conditions")
@NamedQueries({@NamedQuery(name="deleteConditionsByPerson", query="DELETE FROM Conditions o WHERE o.personId = :personId")})
public class Conditions implements PeopleChild
{
	private final static long serialVersionUID = 1L;

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="condition_id", columnDefinition="CHAR(2)", nullable=false)
	@Id
	public String getConditionId() { return conditionId; }
	public String conditionId;
	public void setConditionId(final String newValue) { conditionId = newValue; }

	@Transient @Override public String getChildId() { return getConditionId(); }
	@Transient @Override public void setChildId(final String newValue) { setConditionId(newValue); }
	@Transient @Override public String getChildName() { return Condition.VALUES.get(getConditionId()).name; }

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

	public Conditions() {}

	public Conditions(final People person, final CreatedValue value)
	{
		this.personId = (this.person = person).getId();
		this.conditionId = value.id;
		this.createdAt = ((null != value.createdAt) ? value.createdAt : (value.createdAt = person.getUpdatedAt()));

		if (null == value.name) value.name = Condition.VALUES.get(value.id).name;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof Conditions)) return false;

		var v = (Conditions) o;
		return Objects.equals(personId, v.personId) && Objects.equals(conditionId, v.conditionId);
	}

	@Override
	public int hashCode() { return Objects.hash(personId, conditionId); }

	@Transient
	public CreatedValue toValue()
	{
		return new CreatedValue(
			getConditionId(),
			Condition.VALUES.get(getConditionId()).name,
			getCreatedAt());
	}
}
