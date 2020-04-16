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

import io.swagger.annotations.*;

import com.codahale.metrics.annotation.Timed;

import app.allclear.common.errors.NotAuthorizedException;
import app.allclear.common.errors.ObjectNotFoundException;
import app.allclear.common.mediatype.UTF8MediaType;
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

	private final HmacUtils hmac; // .hmacHex(String);
	private final PeopleDAO peopleDao;

	public static final String HEADER_SIGNATURE = "X-Twilio-Signature";
	public static final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
		"<Response><Message>%s</Message></Response>";
	public static final String RESPONSE_EMPTY = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response />";

	public TwilioResource(final String authToken, final PeopleDAO peopleDao)
	{
		this.peopleDao = peopleDao;
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
		log.info("CHECK_SIGNATURE_PARAMS: {} - {} - '{}'", uri.getAbsolutePath(), params, params_);

		var sign = encoder.encodeToString(hmac.hmac(url.toString()));

		log.info("CHECK_SIGNATURE: {} / {} - {}", sig, sign, url);

		if (!sig.equals(sign)) throw new NotAuthorizedException("The signature '" + sig + "' does not match the calculated signature '" + sign + "'.");
	}

	@POST
	@Path("/alert") @Timed
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
		if (StringUtils.isNotEmpty(body) && (-1 < body.toLowerCase().indexOf("unsubscribe")))
		{
			peopleDao.unalertByPhone(from);
			log.info("UNSUBSCRIBED: {}", from);
			return String.format(RESPONSE, "You have been successfully unsubscribed from further alerts. Have a nice day.");
		}

		log.info("NO_ACTION: {}", from);
		return RESPONSE_EMPTY;
	}
}
