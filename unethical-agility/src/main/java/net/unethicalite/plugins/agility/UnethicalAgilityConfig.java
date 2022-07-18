package net.unethicalite.plugins.agility;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

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

	@Range(max = 100)
	@ConfigItem(
			keyName = "eatHp",
			name = "Eat HP %",
			description = "Eat food when at this HP or below. Will stop if runs out of food.",
			position = 8
	)
	default int eatHp()
	{
		return 75;
	}
}
