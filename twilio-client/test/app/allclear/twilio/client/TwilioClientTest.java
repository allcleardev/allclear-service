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

public class TwilioClientTest
{
	private static final TwilioClient client = new TwilioClient(TwilioConfig.test());

	@Test @Disabled
	public void sendSMSMessage()
	{
		var request = new SMSRequest("+18885550001", "Click this link to confirm your phone number.", "+12014107770");
		var response = client.send(request);

		Assertions.assertNull(response.error_code, "Check error_code");
		Assertions.assertNull(response.error_message, "Check error_message");
		Assertions.assertEquals("sent", response.status, "Check status");
	}
}
