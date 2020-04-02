package app.allclear.common.crypto;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test class that verifies the behaviors of the Constants class.
 * 
 * @author smalleyd
 * @version 3.3.184
 * @since 1/6/2017
 *
 */

public class HasherTest
{
	public static Stream<Arguments> saltAndHash()
	{
		return Stream.of(
			arguments(571L, null, "571", "f292c8c5c2fe9fd30ef1c632e6936edabe42f087e3cb50ceef0324b729383d82"),
			arguments(571L, "", "571", "f292c8c5c2fe9fd30ef1c632e6936edabe42f087e3cb50ceef0324b729383d82"),
			arguments(571L, "A", "A:571", "426ee1aa3bb45f58b7877c9909629e785d94d6a29ef6ae79ace5c19daa09562b"),
			arguments(571L, "AB", "A-571-B", "c16b2d01911f5a52cfe59d422a7cb6d0a3d3b8874c6f456526f138536b217163"),
			arguments(571L, "ABC", "A-571-BC", "0662c5bf7669fdf9dfcd6849612a9e8e816a09b242fcd42ac90e890c272ae704"),
			arguments(571L, "ABCD", "AB-571-CD", "dffba480dc9f9973b62b536a286e39a64d1ce941d674c46da490aef631c3dc56"),
			arguments(571L, "ABCDXYZ", "ABC-571-DXYZ", "e8da88eb7886c0ef712f2c6f44ba64c351a36777b2a9f664129d4d06e62cf33f"),
			arguments(571L, "ABCDWXYZ", "ABCD-571-WXYZ", "4a30a9026fc95acc4dd8ddae14490f3e639dbf422280b152d71686091fbda896"));
	}

	@ParameterizedTest
	@MethodSource
	public void saltAndHash(final Long id, final String password, final String expected, final String expectedToken)
	{
		Assertions.assertEquals(expected, Hasher.salt(id, password), "Check salt");
		Assertions.assertEquals(expectedToken, Hasher.saltAndHash(id, password), "Check saltAndHash");
	}
}
