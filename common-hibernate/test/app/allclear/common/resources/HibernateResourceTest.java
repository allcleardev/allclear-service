package app.allclear.common.resources;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.*;

import app.allclear.junit.hibernate.HibernateRule;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.testing.TestingUtils;

/** Functional test class that verifies the Hibernate RESTful Resource.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

@ExtendWith(DropwizardExtensionsSupport.class)
public class HibernateResourceTest
{
	public static final HibernateRule DAO_RULE = new HibernateRule(new Class<?>[] { FakeEntity.class });
	public final ResourceExtension RULE = ResourceExtension.builder()
		.addResource(new HibernateResource(DAO_RULE.getSessionFactory()))
		.build();

	@Test
	public void getStats()
	{
		var response = RULE.client().target("/info").path("/hibernate/stats").request(UTF8MediaType.APPLICATION_JSON_TYPE).get();
		Assert.assertEquals("Status", TestingUtils.HTTP_STATUS_OK, response.getStatus());

		var results = response.readEntity(String.class);
		assertThat(results)
			.as("Exists")
			.isNotNull()
			.as("stats")
			.contains(",sessions opened=0,sessions closed=0,transactions=0,successful transactions=0,optimistic lock failures=0,flushes=0,connections obtained=0,statements prepared=0,statements closed=0,second level cache puts=0,second level cache hits=0,second level cache misses=0,entities loaded=0,entities updated=0,entities inserted=0,entities deleted=0,entities fetched=0,collections loaded=0,collections updated=0,collections removed=0,collections recreated=0,collections fetched=0,naturalId queries executed to database=0,naturalId cache puts=0,naturalId cache hits=0,naturalId cache misses=0,naturalId max query time=0,queries executed to database=0,query cache puts=0,query cache hits=0,query cache misses=0,update timestamps cache puts=0,update timestamps cache hits=0,update timestamps cache misses=0,max query time=0,query plan cache hits=0,query plan cache misses=0");
	}

	@Test
	public void clearCache()
	{
		var response = RULE.client().target("/info/hibernate/cache").request(UTF8MediaType.APPLICATION_JSON_TYPE).delete();
		Assert.assertEquals("Check status", TestingUtils.HTTP_STATUS_OK, response.getStatus());
	}

	@Entity
	@Cache(region="fake_entity", usage=CacheConcurrencyStrategy.READ_WRITE)
	public static class FakeEntity
	{
		@Id
		@Column
		public String getId() { return id; }
		public String id;
		public void setId(String newValue) { id = newValue; }
	}
}
