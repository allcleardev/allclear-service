package app.allclear.twilio.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents a response from the SMS Message API.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/26/2020
 *
 */

public class SMSResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String account_sid;
	public final String api_version;
	public final String body;
	public final Date date_created;
	public final Date date_sent;
	public final Date date_updated;
	public final String direction;
	public final Integer error_code;
	public final String error_message;
	public final String from;
	public final String messaging_service_sid;
	public final String num_media;
	public final String num_segments;
	public final BigDecimal price;
	public final String price_unit;
	public final String sid;
	public final String status;
	public final Map<String, String> subresource_uris;
	public final String to;
	public final String uri;

	public SMSResponse() { this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null); }
	public SMSResponse(
		@JsonProperty("account_sid") final String account_sid,
		@JsonProperty("api_version") final String api_version,
		@JsonProperty("body") final String body,
		@JsonProperty("date_created") final Date date_created,
		@JsonProperty("date_sent") final Date date_sent,
		@JsonProperty("date_updated") final Date date_updated,
		@JsonProperty("direction") final String direction,
		@JsonProperty("error_code") final Integer error_code,
		@JsonProperty("error_message") final String error_message,
		@JsonProperty("from") final String from,
		@JsonProperty("messaging_service_sid") final String messaging_service_sid,
		@JsonProperty("num_media") final String num_media,
		@JsonProperty("num_segments") final String num_segments,
		@JsonProperty("price") final BigDecimal price,
		@JsonProperty("price_unit") final String price_unit,
		@JsonProperty("sid") final String sid,
		@JsonProperty("status") final String status,
		@JsonProperty("subresource_uris") final Map<String, String> subresource_uris,
		@JsonProperty("to") final String to,
		@JsonProperty("uri") final String uri)
	{
		this.account_sid = account_sid;
		this.api_version = api_version;
		this.body = body;
		this.date_created = date_created;
		this.date_sent = date_sent;
		this.date_updated = date_updated;
		this.direction = direction;
		this.error_code = error_code;
		this.error_message = StringUtils.trimToNull(error_message);
		this.from = from;
		this.messaging_service_sid = messaging_service_sid;
		this.num_media = num_media;
		this.num_segments = num_segments;
		this.price = price;
		this.price_unit = price_unit;
		this.sid = sid;
		this.status = status;
		this.subresource_uris = subresource_uris;
		this.to = to;
		this.uri = uri;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof SMSResponse)) return false;

		var v = (SMSResponse) o;
		return Objects.equals(account_sid, v.account_sid) &&
			Objects.equals(api_version, v.api_version) &&
			Objects.equals(body, v.body) &&
			Objects.equals(date_created, v.date_created) &&
			Objects.equals(date_sent, v.date_sent) &&
			Objects.equals(date_updated, v.date_updated) &&
			Objects.equals(direction, v.direction) &&
			Objects.equals(error_code, v.error_code) &&
			Objects.equals(error_message, v.error_message) &&
			Objects.equals(from, v.from) &&
			Objects.equals(messaging_service_sid, v.messaging_service_sid) &&
			Objects.equals(num_media, v.num_media) &&
			Objects.equals(num_segments, v.num_segments) &&
			Objects.equals(price, v.price) &&
			Objects.equals(price_unit, v.price_unit) &&
			Objects.equals(sid, v.sid) &&
			(status == v.status) &&
			Objects.equals(subresource_uris, v.subresource_uris) &&
			Objects.equals(to, v.to) &&
			Objects.equals(uri, v.uri);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ account_sid: ").append(account_sid)
			.append(", api_version: ").append(api_version)
			.append(", body: ").append(body)
			.append(", date_created: ").append(date_created)
			.append(", date_sent: ").append(date_sent)
			.append(", date_updated: ").append(date_updated)
			.append(", direction: ").append(direction)
			.append(", error_code: ").append(error_code)
			.append(", error_message: ").append(error_message)
			.append(", from: ").append(from)
			.append(", messaging_service_sid: ").append(messaging_service_sid)
			.append(", num_media: ").append(num_media)
			.append(", num_segments: ").append(num_segments)
			.append(", price: ").append(price)
			.append(", price_unit: ").append(price_unit)
			.append(", sid: ").append(sid)
			.append(", status: ").append(status)
			.append(", subresource_uris: ").append(subresource_uris)
			.append(", to: ").append(to)
			.append(", uri: ").append(uri)
			.append(" }").toString();
	}
}
