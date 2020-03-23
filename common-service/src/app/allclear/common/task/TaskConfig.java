package app.allclear.common.task;

import java.io.Serializable;

/** Value object that represents the configuration properties for the background TaskManager.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TaskConfig implements Serializable
{
	/** Constant - serial version UID. */
	public static final long serialVersionUID = 1L;

	/** Should the TaskManager run in the "test" mode? */
	public boolean isTest() { return test; }
	private boolean test = false;
	public void setTest(boolean newValue) { test = newValue; }

	/** Specifies the pause/sleep duration (seconds) between queue processing.
	 *  Zero indicates no pause. Defaults to 2 seconds.
	 */
	public int getSleep() { return sleep; }
	public long getSleepInMillis() { return ((long) sleep) * 1000L; }
	private int sleep = 2;
	public void setSleep(int newValue) { sleep = newValue; }

	/** Specifies the number of background threads available to process the queue items. */
	public int getThreads() { return threads; }
	private int threads = 1;
	public void setThreads(int newValue) { threads = newValue; }
}
