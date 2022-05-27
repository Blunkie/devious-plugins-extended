package net.unethicalite.plugins.agility;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("unethicalagility")
public interface UnethicalAgilityConfig extends Config
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
