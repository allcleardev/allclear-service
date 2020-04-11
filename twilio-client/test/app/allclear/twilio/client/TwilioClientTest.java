package app.allclear.twilio.client;

import org.junit.jupiter.api.*;

import app.allclear.twilio.model.*;

/** Functional test class that verifies the TwilioClient component.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/27/2020
 *
 */

@Disabled
public class TwilioClientTest
{
	private static final TwilioClient client = new TwilioClient(TwilioConfig.test());

	@Test
	public void sendSMSMessage()
	{
		var request = new SMSRequest(null, "+16466321488", "Click this link to confirm your phone number.", "+12014107770");
		var response = client.send(request);

		Assertions.assertNull(response.error_code, "Check error_code");
		Assertions.assertNull(response.error_message, "Check error_message");
		Assertions.assertEquals("queued", response.status, "Check status");
	}

	@Test
	public void sendSMSMessage_withSid()
	{
		var request = new SMSRequest(System.getenv("TWILIO_MESSAGE_SERVICE_SID"), null, "Click this link to confirm your Message Service SID.", "+12014107770");
		var response = client.send(request);

		Assertions.assertNull(response.error_code, "Check error_code");
		Assertions.assertNull(response.error_message, "Check error_message");
		Assertions.assertEquals("accepted", response.status, "Check status");
	}
}
