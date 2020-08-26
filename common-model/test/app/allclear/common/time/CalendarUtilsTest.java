package app.allclear.common.time;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.jupiter.api.*;

/** Functional test class that verifies the CalendarUtils class.
 * 
 * @author smalleyd
 * @version 1.1.129
 * @since 8/25/2020
 *
 */

public class CalendarUtilsTest
{
	@Test
	public void today()
	{
		var o = CalendarUtils.today();

		assertThat(o).isNotNull();
		assertThat(o.getTime()).isToday().isWithinHourOfDay(0).isWithinMinute(0).isWithinSecond(0).isWithinMillisecond(0);
	}
}
