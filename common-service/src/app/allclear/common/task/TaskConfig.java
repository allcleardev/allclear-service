package app.allclear.common.task;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Value object that represents the configuration properties for the background TaskManager.
 *  
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class TaskConfig implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final boolean test;	// Test mode?
	public final int sleep;	// Specifies the pause/sleep duration (seconds) between queue processing. Zero indicates no pause. Defaults to 2 seconds.
	public final int threads;	// Number of background threads to process queue items

	public long getSleepInMillis() { return ((long) sleep) * 1000L; }

	public TaskConfig(@JsonProperty("test") final Boolean test,
		@JsonProperty("sleep") final Integer sleep,
		@JsonProperty("threads") final Integer threads)
	{
		this.test = Boolean.TRUE.equals(test);	// Defaults to off.
		this.sleep = (null != sleep) ? sleep : 2;
		this.threads = (null != threads) ? threads : 1;
	}
}
