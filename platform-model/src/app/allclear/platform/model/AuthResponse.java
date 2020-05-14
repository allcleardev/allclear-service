package app.allclear.platform.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents the user response after clicking on the magic link to complete the authentication process.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/29/2020
 *
 */

public class AuthResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String phone;	// The phone number to which the token was sent.
	public final String email;	// The email address to which the token was sent.	// NOT IMPLEMENTED yet
	public final String token;	// The secret token sent to the account.
	public final boolean rememberMe;	// Indicates to use a long term session if TRUE.

	public AuthResponse(@JsonProperty("phone") final String phone,
		@JsonProperty("email") final String email,
		@JsonProperty("token") final String token,
		@JsonProperty("rememberMe") final Boolean rememberMe)
	{
		this.phone = StringUtils.trimToNull(phone);
		this.email = StringUtils.trimToNull(email);
		this.token = StringUtils.trimToNull(token);
		this.rememberMe = Boolean.TRUE.equals(rememberMe);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
