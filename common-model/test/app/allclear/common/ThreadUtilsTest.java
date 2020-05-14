package app.allclear.common;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit test class that verifies the ThreadUtils helpers.
 * 
 * @author smalleyd
 * @version 1.1.57
 * @since 5/14/2020
 *
 */

public class ThreadUtilsTest
{
	@Test
	public void sleep()
	{
		var time = System.currentTimeMillis();

		ThreadUtils.sleep(2000L);

		assertThat(time).isLessThanOrEqualTo(System.currentTimeMillis() - 2000L);
	}
}
