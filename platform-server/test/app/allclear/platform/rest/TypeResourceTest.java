package app.allclear.platform.rest;

import static org.fest.assertions.api.Assertions.assertThat;
import static app.allclear.testing.TestingUtils.*;

import java.util.List;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.platform.type.*;

/** Functional test class that verifies the TypeResource component.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/25/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class TypeResourceTest
{
	public final ResourceExtension RULE = new ResourceExtension.Builder().addResource(new TypeResource()).build();

	private static final String TARGET = "/types";

	private Invocation.Builder request(final String path) { return RULE.client().target(TARGET).path(path).request(UTF8MediaType.APPLICATION_JSON_TYPE); }

	@Test
	public void getConditions()
	{
		var response = request("conditions").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Condition>>() {})).isEqualTo(Condition.LIST);
	}

	@Test
	public void getCrowdsourceStatuses()
	{
		var response = request("crowdsourceStatuses").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<CrowdsourceStatus>>() {})).isEqualTo(CrowdsourceStatus.LIST);
	}

	@Test
	public void getExperiences()
	{
		var response = request("experiences").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Experience>>() {})).isEqualTo(Experience.LIST);
	}

	@Test
	public void getExposures()
	{
		var response = request("exposures").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Exposure>>() {})).isEqualTo(Exposure.LIST);
	}

	@Test
	public void getFacilityTypes()
	{
		var response = request("facilityTypes").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<FacilityType>>() {})).isEqualTo(FacilityType.LIST);
	}

	@Test
	public void getHealthWorkerStatuses()
	{
		var response = request("healthWorkerStatuses").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<HealthWorkerStatus>>() {})).isEqualTo(HealthWorkerStatus.LIST);
	}

	@Test
	public void getOriginators()
	{
		var response = request("originators").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Originator>>() {})).isEqualTo(Originator.LIST);
	}

	@Test
	public void getPeopleStatuses()
	{
		var response = request("peopleStatuses").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<PeopleStatus>>() {})).isEqualTo(PeopleStatus.LIST);
	}

	@Test
	public void getSexes()
	{
		var response = request("sexes").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Sex>>() {})).isEqualTo(Sex.LIST);
	}

	@Test
	public void getStatures()
	{
		var response = request("statures").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Stature>>() {})).isEqualTo(Stature.LIST);
	}

	@Test
	public void getSymptoms()
	{
		var response = request("symptoms").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Symptom>>() {})).isEqualTo(Symptom.LIST);
	}

	@Test
	public void getTestCriteria()
	{
		var response = request("testCriteria").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<TestCriteria>>() {})).isEqualTo(TestCriteria.LIST);
	}

	@Test
	public void getTestTypes()
	{
		var response = request("testTypes").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<TestType>>() {})).isEqualTo(TestType.LIST);
	}

	@Test
	public void getTimezones()
	{
		var response = request("timezones").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Timezone>>() {})).isEqualTo(Timezone.LIST);
	}

	@Test
	public void getVisibilities()
	{
		var response = request("visibilities").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<Visibility>>() {})).isEqualTo(Visibility.LIST);
	}
}
