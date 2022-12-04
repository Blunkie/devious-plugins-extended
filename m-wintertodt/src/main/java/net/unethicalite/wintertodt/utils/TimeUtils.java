package net.unethicalite.wintertodt.utils;

import java.time.Duration;
import java.time.Instant;


public class TimeUtils
{
	/**
	 * Get time as string between two instants
	 *
	 * @param start
	 * @param finish
	 * @return
	 */
	public static String getTimeBetween(Instant start, Instant finish)
	{
		Duration duration = Duration.between(start, finish);
		return String.format("%d:%02d:%02d",
			duration.toHours(),
			duration.toMinutesPart(),
			duration.toSecondsPart());
	}
}
