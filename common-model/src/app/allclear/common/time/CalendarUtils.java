package app.allclear.common.time;

import java.util.Calendar;

/** Static methods to wrap the java.util.Calendar implementation.
 * 
 * @author smalleyd
 * @version 1.1.129
 * @since 8/25/2020
 *
 */

public class CalendarUtils
{
	public static Calendar today()
	{
		var o = Calendar.getInstance();

		o.set(Calendar.HOUR_OF_DAY, 0);
		o.set(Calendar.MINUTE, 0);
		o.set(Calendar.SECOND, 0);
		o.set(Calendar.MILLISECOND, 0);

		return o;
	}
}
