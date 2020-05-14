package app.allclear.platform.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents the user response after clicking on the magic link to verify the phone/email during the registration process.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/29/2020
 *
 */

public class StartResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String phone;	// The phone number to which the code was sent.
	public final String email;	// The email address to which the code was sent.	// NOT IMPLEMENTED yet
	public final String code;	// The secret code sent to the account.

	public StartResponse(@JsonProperty("phone") final String phone,
		@JsonProperty("email") final String email,
		@JsonProperty("code") final String code)
	{
		this.phone = StringUtils.trimToNull(phone);
		this.email = StringUtils.trimToNull(email);
		this.code = StringUtils.trimToNull(code);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
