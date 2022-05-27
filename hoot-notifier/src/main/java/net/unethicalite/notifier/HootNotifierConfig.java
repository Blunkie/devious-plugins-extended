package net.unethicalite.notifier;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hootnotifier")
public interface HootNotifierConfig extends Config
{
	@ConfigItem(
			keyName = "objectIds",
			name = "Object IDs",
			description = "Objects to track"
	)
	default String objectIds()
	{
		return "";
	}

	@ConfigItem(
			keyName = "npcIds",
			name = "NPC IDs",
			description = "NPCs to track"
	)
	default String npcIds()
	{
		return "";
	}
}
