package app.allclear.platform.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.common.ObjectUtils;

/** Value object that represents a request to the current user's password.
 * 
 * @author smalleyd
 * @version 1.1.22
 * @since 5/1/2020
 *
 */

public class ChangePasswordRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final String currentPassword;
	public final String newPassword;
	public final String confirmPassword;

	public ChangePasswordRequest(@JsonProperty("currentPassword") final String currentPassword,
		@JsonProperty("newPassword") final String newPassword,
		@JsonProperty("confirmPassword") final String confirmPassword)
	{
		this.currentPassword = StringUtils.trimToNull(currentPassword);
		this.newPassword = StringUtils.trimToNull(newPassword);
		this.confirmPassword = StringUtils.trimToNull(confirmPassword);
	}

	@Override
	public String toString() { return ObjectUtils.toString(this); }
}
