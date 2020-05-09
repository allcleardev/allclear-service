package app.allclear.common.time;

import java.text.NumberFormat;

/** Simple stop watch component that performs splits and overall time.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 4/1/2020
 *
 */

public class StopWatch
{
	public static final NumberFormat FORMAT_SECONDS = NumberFormat.getInstance();
	static
	{
		FORMAT_SECONDS.setMinimumFractionDigits(1);
		FORMAT_SECONDS.setMaximumFractionDigits(3);
	}

	private final long start;
	private long split;

	/** Defaults to current time epoch. */
	public StopWatch()
	{
		this(System.currentTimeMillis());
	}

	/** Populator.
	 * 
	 * @param start epoch value.
	 */
	public StopWatch(long start)
	{
		this.start = split = start;
	}

	/** Calculates the seconds between now and the last split (or start).
	 * 
	 * @return different in seconds.
	 */
	public String split()
	{
		var now = System.currentTimeMillis();
		var results = seconds(split, now);
		split = now;

		return results;
	}

	/** Calculates the seconds between now and the original start.
	 * 
	 * @return different in seconds.
	 */
	public String total()
	{
		return seconds(start, System.currentTimeMillis());
	}

	/** Calculates the difference and converts a 'seconds' string.
	 *   
	 *   @param start epoch value from a starting point.
	 */
	public static String seconds(long start, long end)
	{
		return FORMAT_SECONDS.format((double) (end - start) / 1000d) + " seconds";
	}

}
