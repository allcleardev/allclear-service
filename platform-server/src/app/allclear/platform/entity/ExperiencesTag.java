package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.common.value.NamedValue;
import app.allclear.platform.type.Experience;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the experiences_tag table.
*
*	@author smalleyd
*	@version 1.1.81
*	@since June 3, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="experiences_tag")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="experiences_tag")
@NamedQueries({@NamedQuery(name="deleteExperiencesTagsById", query="DELETE FROM ExperiencesTag o WHERE o.experienceId = :id"),
	@NamedQuery(name="findExperiencesTagsByIds", query="SELECT OBJECT(o) FROM ExperiencesTag o WHERE o.experienceId IN (:ids) ORDER BY o.experienceId, o.tagId")})
@NamedNativeQueries({@NamedNativeQuery(name="countExperiencesTagsByFacility", query="SELECT o.tag_id AS name, COUNT(o.experience_id) AS total FROM experiences_tag o INNER JOIN experiences x ON o.experience_id = x.id WHERE x.facility_id = :facilityId GROUP BY o.tag_id", resultClass=CountByName.class)})
public class ExperiencesTag implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Column(name="experience_id", columnDefinition="BIGINT", nullable=false)
	@Id
	public Long getExperienceId() { return experienceId; }
	public Long experienceId;
	public void setExperienceId(final Long newValue) { experienceId = newValue; }

	@Column(name="tag_id", columnDefinition="CHAR(2)", nullable=false)
	@Id
	public String getTagId() { return tagId; }
	public String tagId;
	public void setTagId(final String newValue) { tagId = newValue; }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="experience_id", nullable=false, updatable=false, insertable=false)
	public Experiences getExperience() { return experience; }
	public Experiences experience;
	public void setExperience(final Experiences newValue) { experience = newValue; }
	public void putExperience(final Experiences newValue) { experienceId = (experience = newValue).getId(); }

	public ExperiencesTag() {}

	public ExperiencesTag(final Experiences experience, final NamedValue value)
	{
		this.experienceId = (this.experience = experience).getId();
		this.tagId = value.getId();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof ExperiencesTag)) return false;

		var v = (ExperiencesTag) o;
		return Objects.equals(experienceId, v.experienceId) &&
			Objects.equals(tagId, v.tagId);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(experienceId, tagId);
	}

	@Transient
	public NamedValue toValue()
	{
		return new NamedValue(getTagId(), Experience.get(getTagId()).name);
	}
}
