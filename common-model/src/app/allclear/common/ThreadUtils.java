package app.allclear.common;

/** Utility class that wraps the Thread.sleep call to catch the checked exception.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ThreadUtils
{
	/** Wraps the Thread.sleep call to catch the InterruptedException exception and
	 *  re-throw as a RuntimeException.
	 *
	 * @param millis milliseconds to sleep.
	 */
	public static void sleep(final long millis)
	{
		try { Thread.sleep(millis); }
		catch (final InterruptedException ex) { throw new RuntimeException(ex); }
	}
}
