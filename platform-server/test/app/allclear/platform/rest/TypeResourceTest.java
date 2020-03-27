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
	public void getPeopleStatuses()
	{
		var response = request("peopleStatuses").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<PeopleStatus>>() {})).isEqualTo(PeopleStatus.LIST);
	}

	@Test
	public void getPeopleStatures()
	{
		var response = request("peopleStatures").get();
		Assertions.assertEquals(HTTP_STATUS_OK, response.getStatus(), "Status");

		assertThat(response.readEntity(new GenericType<List<PeopleStature>>() {})).isEqualTo(PeopleStature.LIST);
	}
}