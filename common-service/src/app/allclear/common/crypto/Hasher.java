package app.allclear.common.crypto;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import org.apache.commons.lang3.StringUtils;

/** Hashing utility.
 * 
 * @author smalleyd
 *
 */

public class Hasher
{
	/** Composes a string that is salted in preparation to be hashed. */
	public static String salt(final long id, final String password)
	{
		// Plant the primary key in the middle of the password.
		if (StringUtils.isEmpty(password)) return "" + id;

		int len = password.length();
		if (1 == len)
				return new StringBuilder(password).append(":").append(id).toString();

		len/= 2;		// Determine the midpoint.

		return new StringBuilder(password.substring(0, len)).append("-").append(id).append("-").append(password.substring(len)).toString();
	}

	/** Salts and hashes a password.
	 *
	 * @param id long timestamp
	 * @param password
	 * @return never NULL
	 */
	public static String saltAndHash(final long id, final String password)
	{
		return sha256Hex(salt(id, password));
	}
}
