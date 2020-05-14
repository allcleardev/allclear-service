package app.allclear.platform.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents a credentials authentication request.
 * 
 * @author smalleyd
 * @version 1.0.16
 * @since 4/2/2020
 *
 */

public class AuthenticationRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String userName;
	public final String password;
	public final boolean rememberMe;

	public AuthenticationRequest(@JsonProperty("userName") final String userName,
		@JsonProperty("password") final String password,
		@JsonProperty("rememberMe") final Boolean rememberMe)
	{
		this.userName = StringUtils.trimToNull(userName);
		this.password = StringUtils.trimToNull(password);
		this.rememberMe = Boolean.TRUE.equals(rememberMe);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
