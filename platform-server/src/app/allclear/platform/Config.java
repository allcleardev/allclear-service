package app.allclear.platform;

import java.io.Serializable;
import java.util.regex.Pattern;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.DWUtil;
import app.allclear.common.ManifestUtils;
import app.allclear.common.redis.RedisConfig;
import app.allclear.common.value.ManifestValue;
import app.allclear.twilio.client.TwilioConfig;

/** Value object that represents the application configuration properties.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class Config extends Configuration implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Pattern PATTERN_BASE_URL = Pattern.compile("\\$\\{baseUrl\\}");
	public static String baseUrl(final String name, final String baseUrl)
	{
		return PATTERN_BASE_URL.matcher(DWUtil.load(Config.class.getResource(name))).replaceAll(baseUrl);
	}

	public final String env;
	public final boolean disableSwagger;
	public final DataSourceFactory read;
	public final DataSourceFactory trans;
	public final ManifestValue manifest;

	public final String baseUrl;
	public final String registrationSid;
	public final String registrationPhone;
	public final String authenticationSid;
	public final String authenticationPhone;
	public final String registrationSMSMessage;
	public final String authenticationSMSMessage;

	public final String admins;
	public final RedisConfig session;
	public final TwilioConfig twilio;

	public String getVersion() { return manifest.version; }

	public Config(@JsonProperty("env") final String env,
		@JsonProperty("disableSwagger") final Boolean disableSwagger,
		@JsonProperty("read") final DataSourceFactory read,
		@JsonProperty("trans") final DataSourceFactory trans,
		@JsonProperty("baseUrl") final String baseUrl,
		@JsonProperty("registrationPhone") final String registrationPhone,
		@JsonProperty("authenticationPhone") final String authenticationPhone,
		@JsonProperty("admins") final String admins,
		@JsonProperty("session") final RedisConfig session,
		@JsonProperty("twilio") final TwilioConfig twilio)
	{
		this.env = env;
		this.trans = trans;
		this.read = (null != read) ? read : trans;
		this.manifest = ManifestUtils.getInfo(getClass());
		this.disableSwagger = Boolean.TRUE.equals(disableSwagger);

		this.baseUrl = baseUrl;
		this.registrationSid = registrationPhone.startsWith("+") ? null : registrationPhone;
		this.registrationPhone = registrationPhone.startsWith("+") ? registrationPhone : null;
		this.authenticationSid = authenticationPhone.startsWith("+") ? null : authenticationPhone;
		this.authenticationPhone = authenticationPhone.startsWith("+") ? authenticationPhone : null;
		this.registrationSMSMessage = baseUrl("/messages/sms/registration.txt", baseUrl);
		this.authenticationSMSMessage = baseUrl("/messages/sms/authentication.txt", baseUrl);

		this.admins = admins;
		this.session = session;
		this.twilio = twilio;
	}
}
