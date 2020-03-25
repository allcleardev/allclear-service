package app.allclear.platform.dao;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.allclear.common.errors.Validator;
import app.allclear.common.jackson.JacksonUtils;
import app.allclear.common.redis.RedisClient;
import app.allclear.platform.model.StartRequest;

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
	private static final int EXPIRATION = 10 * 60;	// Ten minutes
	private static final ObjectMapper mapper = JacksonUtils.createMapper();
	private static final TypeReference<Map<String, String>> TYPE_MAP = new TypeReference<Map<String, String>>() {};

	private final RedisClient redis;

	public RegistrationDAO(final RedisClient redis) { this.redis = redis; }

	public String key(final String phone, final String code) { return String.format(ID, phone, code); }

	/** Initiates registration & phone number confirmation process.
	 * 
	 * @param request
	 * @return the confirmation code
	 */
	public String start(final StartRequest request)
	{
		new Validator().ensureLength("phone", "Phone", request.phone, 10, 32)
			.ensurePattern("phone", "Phone", request.phone, Validator.PATTERN_PHONE)
			.check();
		
		return redis.operation(c -> {
			int i = 0;
			var code = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
			var key = key(request.phone, code);
			while (c.exists(key))
			{
				if (10 < i++) throw new IllegalArgumentException("CanNOT generate identifier - 10 tries.");
				key = key(request.phone, code = RandomStringUtils.randomAlphanumeric(10).toUpperCase());
			}

			c.hset(key, mapper.convertValue(request, TYPE_MAP));
			c.expire(key, EXPIRATION);

			return code;
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
		var key = key(phone, code);
		var map = redis.hash(key);
		if (MapUtils.isEmpty(map)) return null;

		return mapper.convertValue(map, StartRequest.class);
	}
}
