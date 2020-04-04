package app.allclear.platform.dao;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.dao.QueryResults;
import app.allclear.common.errors.Validator;
import app.allclear.common.errors.ValidationException;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.redis.RedisClient;
import app.allclear.platform.Config;
import app.allclear.platform.filter.RegistrationFilter;
import app.allclear.platform.model.StartRequest;
import app.allclear.platform.value.RegistrationValue;
import app.allclear.twilio.client.TwilioClient;
import app.allclear.twilio.model.*;

/** Data access component that manages the person registration process.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class RegistrationDAO
{
	private static final String ID = "registration:%s:%s";
	private static final String MATCH = "registration:*";
	public static final int CODE_LENGTH = 10;
	public static final int EXPIRATION = 10 * 60;	// Ten minutes
	private static final ObjectMapper mapper = JacksonUtils.createMapper();
	private static final TypeReference<Map<String, String>> TYPE_MAP = new TypeReference<Map<String, String>>() {};

	private final String from;
	private final String message;
	private final RedisClient redis;
	private final TwilioClient twilio;

	public RegistrationDAO(final RedisClient redis, final TwilioClient twilio, final Config conf)
	{
		this.redis = redis;
		this.twilio = twilio;
		this.from = conf.registrationPhone;
		this.message = conf.registrationSMSMessage;
	}

	public String key(final String phone, final String code) { return String.format(ID, phone, code); }

	/** Initiates registration & phone number confirmation process.
	 * 
	 * @param request
	 * @return the confirmation code
	 * @throws ValidationException
	 */
	public String start(final StartRequest request) throws ValidationException
	{
		new Validator().ensureLength("phone", "Phone", request.phone, 10, 32)
			.ensurePattern("phone", "Phone", request.phone, Validator.PATTERN_PHONE)
			.check();
		
		return redis.operation(c -> {
			int i = 0;
			var code = RandomStringUtils.randomAlphanumeric(CODE_LENGTH).toUpperCase();
			var key = key(request.phone, code);
			while (c.exists(key))
			{
				if (10 < i++) throw new IllegalArgumentException("CanNOT generate identifier - 10 tries.");
				key = key(request.phone, code = RandomStringUtils.randomAlphanumeric(CODE_LENGTH).toUpperCase());
			}

			twilio.send(new SMSRequest(from, String.format(message, code, encode(request.phone, UTF_8), encode(code, UTF_8)), request.phone));
			c.hset(key, mapper.convertValue(request, TYPE_MAP));
			c.expire(key, EXPIRATION);

			return code;
		});
	}

	/** Confirms the phone and registration code. Retrieves the original request.
	 * 
	 * @param phone
	 * @param code
	 * @return never NULL.
	 * @throws ValidationException if the phone and/or registration code are invalid.
	 */
	public StartRequest confirm(final String phone, final String code) throws ValidationException
	{
		return redis.operation(j -> {
			var key = key(phone, code);
			var o = request(j, key);
			if (null == o) throw new ValidationException("code", "The supplied code is invalid.");

			j.del(key);	// Once confirmed, delete the key.

			return o;
		});
	}

	/** Gets the original start request based on the phone number and registration confirmation code.
	 * 
	 * @param phone
	 * @param code
	 * @return NULL if not found
	 */
	StartRequest request(final String phone, final String code)
	{
		return redis.operation(j -> request(j, key(phone, code)));
	}

	/** Gets the original start request based on the phone number and registration confirmation code.
	 * 
	 * @param jedis
	 * @param key
	 * @return NULL if not found
	 */
	StartRequest request(final Jedis jedis, final String key)
	{
		var map = jedis.hgetAll(key);
		if (MapUtils.isEmpty(map)) return null;

		return mapper.convertValue(map, StartRequest.class);
	}

	/** Remove a registration request.
	 * 
	 * @param key
	 */
	public void remove(final String key)
	{
		redis.remove(key);
	}

	/** Searches the registration requests.
	 * 
	 * @param filter
	 * @return never NULL.
	 */
	public QueryResults<RegistrationValue, RegistrationFilter> search(final RegistrationFilter filter)
	{
		filter.clean();
		return redis.operation(j -> {
			var cursor = (filter.page() - 1) + "";
			var match = (null != filter.phone) ? key(filter.phone, "*") : MATCH;
			var o = j.scan(cursor, new ScanParams().count(filter.pageSize(100)).match(match));

			if (CollectionUtils.isEmpty(o.getResult())) return new QueryResults<RegistrationValue, RegistrationFilter>(0L, filter);

			var v = o.getResult().stream().map(i -> new RegistrationValue(i, request(j, i), j.ttl(i))).collect(Collectors.toList());
			var r = new QueryResults<RegistrationValue, RegistrationFilter>(v, filter);
			r.page = Integer.parseInt(o.getCursor());
			r.pages++;	// Always add one.

			return r;
		});
	}
}
