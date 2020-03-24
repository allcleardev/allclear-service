package app.allclear.common.jersey;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the configuration for the Question Service RESTful client.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

public class HttpClientConfig implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String baseUrl;
	public final boolean checkCert;
	public final String key;
	public final boolean noExecute;

	public HttpClientConfig(@JsonProperty("baseUrl") final String baseUrl,
		@JsonProperty("checkCert") final boolean checkCert,
		@JsonProperty("key") final String key,
		@JsonProperty("noExecute") final Boolean noExecute)
	{
		this.baseUrl = baseUrl;
		this.checkCert = checkCert;
		this.key = key;
		this.noExecute = Boolean.TRUE.equals(noExecute);	// NULL should set this to FALSE.
	}
}
