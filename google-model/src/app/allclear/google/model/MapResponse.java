package app.allclear.google.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the base response for all Google maps calls.
 * 
 * @author smalleyd
 * @version 1.0.55
 * @since 4/5/2020
 *
 */

public abstract class MapResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String STATUS_OK = "OK";
	public static final String STATUS_ZERO_RESULTS = "ZERO_RESULTS";
	public static final String STATUS_OVER_DAILY_LIMIT = "OVER_DAILY_LIMIT";
	public static final String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
	public static final String STATUS_REQUEST_DENIED = "REQUEST_DENIED";
	public static final String STATUS_INVALID_REQUEST = "INVALID_REQUEST";
	public static final String STATUS_UNKNOWN_ERROR = "UNKNOWN_ERROR";

	public final String status;
	@JsonProperty("error_message") public final String errorMessage;

	public boolean ok() { return STATUS_OK.equals(status); }
	public boolean zeroResults() { return STATUS_ZERO_RESULTS.equals(status); }
	public boolean overDailyLimit() { return STATUS_OVER_DAILY_LIMIT.equals(status); }
	public boolean overQueryLimit() { return STATUS_OVER_QUERY_LIMIT.equals(status); }
	public boolean requestDenied() { return STATUS_REQUEST_DENIED.equals(status); }
	public boolean invalidRequest() { return STATUS_INVALID_REQUEST.equals(status); }
	public boolean unknownError() { return STATUS_UNKNOWN_ERROR.equals(status); }

	public MapResponse(final String status, final String errorMessage)
	{
		this.status = status;
		this.errorMessage = errorMessage;
	}
}
