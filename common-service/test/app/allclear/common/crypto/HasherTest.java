package app.allclear.common.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/** Unit test class that verifies the behaviors of the Constants class.
 * 
 * @author smalleyd
 * @version 3.3.184
 * @since 1/6/2017
 *
 */

public class HasherTest
{
	@ParameterizedTest
	@CsvFileSource(resources="/crypto/hasher.csv", numLinesToSkip=1)
	public void saltAndHash(final Long id, final String password, final String expected, final String expectedToken)
	{
		Assertions.assertEquals(expected, Hasher.salt(id, password), "Check salt");
		Assertions.assertEquals(expectedToken, Hasher.saltAndHash(id, password), "Check saltAndHash");
	}

	@ParameterizedTest
	@CsvFileSource(resources="/crypto/hasher.csv", numLinesToSkip=1)
	public void saltAndHash(final String id, final String password, final String expected, final String expectedToken)
	{
		Assertions.assertEquals(expected, Hasher.salt(id, password), "Check salt");
		Assertions.assertEquals(expectedToken, Hasher.saltAndHash(id, password), "Check saltAndHash");
	}
}
