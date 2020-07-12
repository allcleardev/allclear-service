package app.allclear.platform.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents a request to authenticate a person and optional change
 *  their password.
 *
 * @author smalleyd
 * @version 1.1.104
 * @since 7/10/2020
 *
 */

public class PeopleAuthRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public String name;	// Can be Person ID, screen name, phone number, or email address.	// NOT ideal to be mutable but necessary for PeopleResource.changePassword call. DLS on 7/11/2020.
	public final String password;
	public final boolean rememberMe;
	public final String newPassword;
	public final String confirmPassword;

	// Set with the current user's ID on backend of change password call.
	public PeopleAuthRequest name(final String newValue) { name = newValue; return this; }

	public PeopleAuthRequest(@JsonProperty("name") final String name,
		@JsonProperty("password") final String password,
		@JsonProperty("rememberMe") final Boolean rememberMe,
		@JsonProperty("newPassword") final String newPassword,
		@JsonProperty("confirmPassword") final String confirmPassword)
	{
		this.name = StringUtils.trimToNull(name);
		this.password = StringUtils.trimToNull(password);
		this.rememberMe = Boolean.TRUE.equals(rememberMe);
		this.newPassword = StringUtils.trimToNull(newPassword);
		this.confirmPassword = StringUtils.trimToNull(confirmPassword);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }

}
