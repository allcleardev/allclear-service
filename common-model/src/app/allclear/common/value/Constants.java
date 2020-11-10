package app.allclear.common.value;

import java.util.Date;

/** Global general purpose constants.
 * 
 * @author smalleyd
 * @version 1.0.33
 * @since 4/3/2020
 *
 */

public class Constants
{
	public static final long KM_TO_METERS = 1000L;
	public static final long MILES_TO_METERS = 1609;

	public static long kmToMeters(final int km) { return ((long) km) * KM_TO_METERS; }
	public static long milesToMeters(final int miles) { return ((long) miles) * MILES_TO_METERS; }

	public static final long MILLISECOND_MINUTES = 60000L;
	public static long minutes(final long value) { return MILLISECOND_MINUTES * value; }
	public static long minutes(final int value) { return minutes((long) value); }

	public static final long MILLISECOND_HOURS = minutes(60L);
	public static long hours(final long value) { return MILLISECOND_HOURS * value; }
	public static long hours(final int value) { return hours((long) value); }

	public static final long DURATION_LOCK = minutes(5L);
	public static Date lockedTill() { return new Date(System.currentTimeMillis() + DURATION_LOCK); }

	public static final long DURATION_REVIEW = hours(72L);
	public static Date reviewedFrom() { return new Date(System.currentTimeMillis() - DURATION_REVIEW); }
}
