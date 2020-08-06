package app.allclear.platform.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.Base64;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.*;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;

import app.allclear.common.errors.NotAuthorizedException;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.mediatype.UTF8MediaType;
import app.allclear.platform.Config;
import app.allclear.platform.dao.PatientDAO;
import app.allclear.platform.dao.PeopleDAO;

/** Jersey RESTful resource that accepts responses from Twilio.
 * 
 * @author smalleyd
 * @version 1.0.111
 * @since 4/15/2020
 * @see https://www.twilio.com/docs/usage/security
 * @see https://www.twilio.com/docs/sms/twiml
 * 
 */

@Path("/twilio")
@Consumes(UTF8MediaType.APPLICATION_FORM_URLENCODED)
@Produces(UTF8MediaType.APPLICATION_XML)
@Api("Twilio")
public class TwilioResource
{
	private static final Logger log = LoggerFactory.getLogger(TwilioResource.class);
	private static final Base64.Encoder encoder = Base64.getEncoder();
	static final Pattern PATTERN_HTTP = Pattern.compile("http:");

	private final Config conf;
	private final HmacUtils hmac; // .hmacHex(String);
	private final PeopleDAO peopleDao;
	private final PatientDAO patientDao;

	public static final String HEADER_SIGNATURE = "X-Twilio-Signature";
	public static final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
		"<Response><Message><Body>%s</Body></Message></Response>";
	public static final String RESPONSE_EMPTY = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response />";

	public TwilioResource(final String authToken, PatientDAO patientDao, final PeopleDAO peopleDao, final Config conf)
	{
		this.conf = conf;
		this.peopleDao = peopleDao;
		this.patientDao = patientDao;
		this.hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, authToken);
	}

	private void checkSignature(final UriInfo uri,
		final String signature,
		final MultivaluedMap<String, String> params) throws NotAuthorizedException
	{
		var sig = StringUtils.trimToNull(signature);
		if (null == sig) throw new NotAuthorizedException("No signature was found.");

		var url = PATTERN_HTTP.matcher(uri.getRequestUri().toString()).replaceFirst("https:");	// For some reason, Jersey reports the call as HTTP even though sent via HTTPS. DLS on 4/16/2020.
		var params_ = params.entrySet()
			.stream()
			.sorted((a, b) -> a.getKey().compareTo(b.getKey()))
			.map(e -> e.getKey() + e.getValue().get(0))
			.collect(Collectors.joining());
		if (null != params_) url+=params_;

		var sign = encoder.encodeToString(hmac.hmac(url.toString()));

		log.info("CHECK_SIGNATURE: {} / {} - {}", sig, sign, url);

		if (!sig.equals(sign)) throw new NotAuthorizedException("The signature '" + sig + "' does not match the calculated signature '" + sign + "'.");
	}

	@POST
	@Path("/alert") @UnitOfWork @Timed
	@ApiOperation(value="alert", notes="Handles responses to our SMS alerts.")
	public String handleAlert(@Context final UriInfo uri,
		@HeaderParam(HEADER_SIGNATURE) final String signature,
		final MultivaluedMap<String, String> params,
		@FormParam("From") final String from,
		@FormParam("Body") final String body) throws NotAuthorizedException, ObjectNotFoundException
	{
		log.info("ALERT: {} - {}", from, body);

		checkSignature(uri, signature, params);

		// Has the user asked to be unsubscribed?
		if (StringUtils.isNotEmpty(body))
		{
			var body_ = body.toLowerCase();
			if ((-1 < body_.indexOf("unsubscribe")) || (-1 < body_.indexOf("stop")))
			{
				peopleDao.unalertByPhone(from);
				log.info("UNSUBSCRIBED: {}", from);
				return response("You have been successfully unsubscribed from further alerts. Have a nice day.");
			}
			else if (-1 < body_.indexOf("start"))
			{
				peopleDao.alertByPhone(from);
				log.info("SUBSCRIBED: {}", from);
				return response("You have been successfully subscribed to our new facility alerts. Welcome aboard!");
			}
		}

		log.warn("NO_ACTION: {}", from);
		return RESPONSE_EMPTY;
	}

	@POST
	@Path("/patient") @UnitOfWork @Timed
	@ApiOperation(value="patient", notes="Handles responses to our SMS patient enrollment requests.")
	public String handlePatient(@Context final UriInfo uri,
		@HeaderParam(HEADER_SIGNATURE) final String signature,
		final MultivaluedMap<String, String> params,
		@FormParam("From") final String from,
		@FormParam("Body") final String body) throws NotAuthorizedException, ObjectNotFoundException
	{
		log.info("PATIENT: {} - {}", from, body);

		checkSignature(uri, signature, params);

		// Has the respondent accepted or rejected the request?
		var result = isYesOrNo(body);
		if (null == result)
		{
			log.warn("PATIENT_BAD_RESPONSE: {} - {}", from, body);
			return response("Please respond with either 'Yes' or 'No'.");
		}
		else
		{
			var id = peopleDao.getIdByPhone(from);
			if (null == id)
			{
				log.warn("PATIENT_PHONE_NUMBER_NOT_FOUND: {} - {}", from, body);
				return response("Please sign up at " + conf.baseUrl + " first.");
			}
			else if (result)
			{
				peopleDao.activate(id);
				var facilityName = patientDao.accept(id);
				if (null != facilityName)
				{
					log.info("PATIENT_ACCEPTED: {} - {}", from, facilityName);
					return response("You have been successfully enrolled to receive test notifications from " + facilityName + ".");
				}
			}
			else
			{
				var facilityName = patientDao.reject(id);
				if (null != facilityName)
				{
					log.info("PATIENT_REJECTED: {}", from);
					return response("The test notification request from " + facilityName + " has been rejected. Have a nice day.");
				}
			}
		}

		log.warn("PATIENT_REQUEST_NOT_FOUND: {}", from);
		return response("You do have any outstanding test notification requests.");
	}

	String response(final String body)
	{
		return String.format(RESPONSE, body);
	}

	Boolean isYesOrNo(String body)
	{
		if (StringUtils.isEmpty(body = StringUtils.trimToNull(body))) return null;

		body = body.toLowerCase();
		if ("y".equals(body) || "yes".equals(body)) return true;
		if ("n".equals(body) || "no".equals(body)) return false;

		return null;
	}
}
