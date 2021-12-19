package dev.hoot.agility;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hootagility")
public interface HootAgilityConfig extends Config
{
	@ConfigItem(
			name = "Course",
			keyName = "course",
			description = "Course to complete"
	)
	default Course course()
	{
		return Course.NEAREST;
	}
}
