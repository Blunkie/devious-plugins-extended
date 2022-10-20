package net.unethicalite.plugins.birdhouses.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import net.unethicalite.client.Static;

import java.time.Instant;

@AllArgsConstructor
@Slf4j
public class BirdHouse
{
	private static final int BIRD_HOUSE_DURATION = 51 * 60; // Add an extra minute in case of inaccuracy

	@Delegate
	private BirdHouseLocation location;

	@Getter
	@Setter
	private BirdHouseState state;

	public boolean isComplete()
	{
		return getBuildTimestamp().plusSeconds(BIRD_HOUSE_DURATION).isBefore(Instant.now());
	}

	public Instant getBuildTimestamp()
	{
		String configValue = Static.getConfigManager().getRSProfileConfiguration(
				"timetracking",
				String.format("birdhouse.%s", location.getVarp().getId())
		);

		if (configValue == null)
		{
			return Instant.EPOCH;
		}

		String[] split = configValue.split(":");
		if (split.length < 2)
		{
			return Instant.EPOCH;
		}

		return Instant.ofEpochSecond(Long.parseLong(split[1]));
	}

	@Override
	public String toString()
	{
		return String.format("%s at %s | %s %s %s", location.toString(), getWorldPoint(), state, isComplete() ? "COMPLETED" : "IN_PROGRESS", getBuildTimestamp());
	}
}
