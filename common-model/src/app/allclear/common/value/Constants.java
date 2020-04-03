package app.allclear.common.value;

/** Global general purpose constants.
 * 
 * @author smalleyd
 * @version 1.0.33
 * @since 4/3/2020
 *
 */

public class Constants
{
	public static long KM_TO_METERS = 1000L;
	public static long MILES_TO_METERS = 1609;

	public static long kmToMeters(final int km) { return ((long) km) * KM_TO_METERS; }
	public static long milesToMeters(final int miles) { return ((long) miles) * MILES_TO_METERS; }
}
