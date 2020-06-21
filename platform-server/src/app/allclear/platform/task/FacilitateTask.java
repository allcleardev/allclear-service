package app.allclear.platform.task;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.*;

import app.allclear.common.task.TaskCallback;
import app.allclear.platform.Config;
import app.allclear.platform.dao.AdminDAO;
import app.allclear.platform.value.FacilitateValue;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.SMSRequest;

/** Task callback that handles notifications for new Facilitate requests.
 *  
 *  Sends SMS messages to select Admins when new Facility change requests are received.
 * 
 * @author smalleyd
 * @version 1.1.93
 * @since 6/21/2020
 *
 */

public class FacilitateTask implements TaskCallback<FacilitateValue>
{
	private static final Logger log = LoggerFactory.getLogger(FacilitateTask.class);

	private static final String WHO = " (%s)";
	private static final String MESSAGE = "A %s Request has been added by a %s. Click here to view all change requests - %s.";

	private final Config conf;
	private final AdminDAO adminDao;
	private final TwilioClient twilio;

	public FacilitateTask(final AdminDAO adminDao, final TwilioClient twilio, final Config conf)
	{
		this.adminDao = adminDao;
		this.twilio = twilio;
		this.conf = conf;

		log.info("INITIALIZED");
	}

	@Override
	public boolean process(final FacilitateValue value) throws Exception
	{
		var phones = adminDao.getAlertablePhoneNumbers();
		if (CollectionUtils.isEmpty(phones))
		{
			log.warn("No administrative phone numbers were found for {}.", value);
			return true;
		}

		var who = (null != value.creatorId) ? String.format(WHO, value.creatorId) : "";  
		var message = String.format(MESSAGE,
			value.change ? "Facility Change" : "New Facility",
			value.originator.name + who,
			conf.adminUrl) ;

		phones.forEach(phone -> {
			try { twilio.send(new SMSRequest(conf.registrationSid, conf.registrationPhone, message, phone)); }
			catch (final Exception ex)
			{
				log.error("Could NOT send Facilitate Request notification {} to {}: {} - {} - {}", value, phone, ex.getClass(), ex.getMessage(), ex.getStackTrace());
			}
		});

		return true;
	}
}
