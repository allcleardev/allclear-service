package app.allclear.platform.task;

import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;

import app.allclear.platform.ConfigTest;
import app.allclear.platform.dao.AdminDAO;
import app.allclear.platform.type.Originator;
import app.allclear.platform.value.FacilitateValue;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.SMSRequest;
import app.allclear.twilio.model.SMSResponse;

/** Functional test class that verifies the FacilitateTask component.
 * 
 * @author smalleyd
 * @version 1.1.93
 * @since 6/21/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class FacilitateTaskTest
{
	private static FacilitateTask task;
	private static final AdminDAO adminDao = mock(AdminDAO.class);
	private static final TwilioClient twilio = mock(TwilioClient.class);

	private static int sends = 0;
	private static int sends_ = 0;
	private static String lastMessage = null;
	private static String lastMessage_ = null;

	@BeforeAll
	public static void up()
	{
		task = new FacilitateTask(adminDao, twilio, ConfigTest.loadTest());

		when(twilio.send(any(SMSRequest.class))).thenAnswer(a -> {
			var req = a.getArgument(0, SMSRequest.class);

			sends_++;
			lastMessage_ = req.body;

			return new SMSResponse(req);
		});
	}

	@AfterEach
	public void afterEach()
	{
		Assertions.assertEquals(sends, sends_, "Check sends");
		Assertions.assertEquals(lastMessage, lastMessage_, "Check lastMessage");
	}

	@Test
	public void process_00_empty() throws Exception
	{
		when(adminDao.getAlertablePhoneNumbers()).thenReturn(Set.of());

		Assertions.assertTrue(task.process(new FacilitateValue().withChange(false).withOriginator(Originator.CITIZEN)));
	}

	@Test
	public void process_01_null() throws Exception
	{
		when(adminDao.getAlertablePhoneNumbers()).thenReturn(null);

		Assertions.assertTrue(task.process(new FacilitateValue().withChange(false).withOriginator(Originator.CITIZEN)));
	}

	@Test
	public void process_02_addCitizen() throws Exception
	{
		when(adminDao.getAlertablePhoneNumbers()).thenReturn(Set.of("+18885552000"));

		Assertions.assertTrue(task.process(new FacilitateValue().withChange(false).withOriginator(Originator.CITIZEN)));

		sends++;
		lastMessage = "A New Facility Request has been added by a Citizen. Click here to view all change requests - https://api-test.allclear.app/manager/index.html.";
	}

	@Test
	public void process_02_addProvider() throws Exception
	{
		when(adminDao.getAlertablePhoneNumbers()).thenReturn(Set.of("+18885552002", "+18885552003", "+18885552004"));

		Assertions.assertTrue(task.process(new FacilitateValue().withChange(false).withOriginator(Originator.PROVIDER)));

		sends+= 3;
		lastMessage = "A New Facility Request has been added by a Provider. Click here to view all change requests - https://api-test.allclear.app/manager/index.html.";
	}

	@Test
	public void process_02_changeCitizen() throws Exception
	{
		when(adminDao.getAlertablePhoneNumbers()).thenReturn(Set.of("+18885552005", "+18885552006"));

		Assertions.assertTrue(task.process(new FacilitateValue().withChange(true).withOriginator(Originator.CITIZEN)));

		sends+=2;
		lastMessage = "A Facility Change Request has been added by a Citizen. Click here to view all change requests - https://api-test.allclear.app/manager/index.html.";
	}

	@Test
	public void process_02_changeProvider() throws Exception
	{
		when(adminDao.getAlertablePhoneNumbers()).thenReturn(Set.of("+18885552007", "+18885552008", "+18885552009", "+18885552010"));

		Assertions.assertTrue(task.process(new FacilitateValue().withChange(true).withOriginator(Originator.PROVIDER)));

		sends+= 4;
		lastMessage = "A Facility Change Request has been added by a Provider. Click here to view all change requests - https://api-test.allclear.app/manager/index.html.";
	}

	@Test
	public void process_99_throws() throws Exception
	{
		when(adminDao.getAlertablePhoneNumbers()).thenReturn(Set.of("+18885552011"));
		when(twilio.send(any(SMSRequest.class))).thenThrow(RuntimeException.class);

		Assertions.assertTrue(task.process(new FacilitateValue().withChange(false).withOriginator(Originator.CITIZEN)));
	}
}
