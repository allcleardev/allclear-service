package app.allclear.platform;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.DWUtil;
import app.allclear.common.ManifestUtils;
import app.allclear.common.value.ManifestValue;
import app.allclear.redis.JedisConfig;
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
	public final String alertSid;
	public final String alertPhone;
	public final String registrationSid;
	public final String registrationPhone;
	public final String authSid;
	public final String authPhone;
	public final String registrationSMSMessage;
	public final String alertSMSMessage;
	public final String authSMSMessage;

	public final String queue;	// Connection string to the queue space.
	public final String admins;
	public final String auditLog;
	public final JedisConfig geocode;
	public final JedisConfig session;
	public final TwilioConfig twilio;

	public String getVersion() { return manifest.version; }

	public Config(@JsonProperty("env") final String env,
		@JsonProperty("disableSwagger") final Boolean disableSwagger,
		@JsonProperty("read") final DataSourceFactory read,
		@JsonProperty("trans") final DataSourceFactory trans,
		@JsonProperty("baseUrl") final String baseUrl,
		@JsonProperty("authPhone") final String authPhone,
		@JsonProperty("alertPhone") final String alertPhone,
		@JsonProperty("registrationPhone") final String registrationPhone,
		@JsonProperty("queue") final String queue,
		@JsonProperty("admins") final String admins,
		@JsonProperty("auditLog") final String auditLog,
		@JsonProperty("geocode") final JedisConfig geocode,
		@JsonProperty("session") final JedisConfig session,
		@JsonProperty("twilio") final TwilioConfig twilio)
	{
		this.env = env;
		this.trans = trans;
		this.read = (null != read) ? read : trans;
		this.manifest = ManifestUtils.getInfo(getClass());
		this.disableSwagger = Boolean.TRUE.equals(disableSwagger);

		this.baseUrl = baseUrl;
		this.authSid = authPhone.startsWith("+") ? null : authPhone;
		this.authPhone = authPhone.startsWith("+") ? authPhone : null;
		if (StringUtils.isNotEmpty(alertPhone))
		{
			this.alertSid = alertPhone.startsWith("+") ? null : alertPhone;
			this.alertPhone = alertPhone.startsWith("+") ? alertPhone : null;
		}
		else
		{
			this.alertSid = authSid;
			this.alertPhone = this.authPhone;
		}
		this.registrationSid = registrationPhone.startsWith("+") ? null : registrationPhone;
		this.registrationPhone = registrationPhone.startsWith("+") ? registrationPhone : null;
		this.authSMSMessage = baseUrl("/messages/sms/auth.txt", baseUrl);
		this.alertSMSMessage = baseUrl("/messages/sms/alert.txt", baseUrl);
		this.registrationSMSMessage = baseUrl("/messages/sms/registration.txt", baseUrl);

		this.queue = queue;
		this.admins = admins;
		this.auditLog = auditLog;
		this.geocode = geocode;
		this.session = session;
		this.twilio = twilio;
	}
}
