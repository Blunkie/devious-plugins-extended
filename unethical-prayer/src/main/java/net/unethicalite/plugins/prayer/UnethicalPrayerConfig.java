package net.unethicalite.plugins.prayer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("unethicalprayer")
public interface UnethicalPrayerConfig extends Config
{
	@ConfigItem(
			keyName = "configs",
			name = "Configuration",
			description = "Usage: NPCName:Prayer:AnimationID:AttackSpeedTicks, ex. TzTok-Jad:PROTECT_FROM_MAGIC:7592:8"
	)
	default String configs()
	{
		return "TzTok-Jad:PROTECT_FROM_MAGIC:7592:8\nTzTok-Jad:PROTECT_FROM_MISSILES:7593:8";
	}

	@ConfigItem(
			keyName = "turnOffAfterAttack",
			name = "Toggle off after attack",
			description = "Turns the prayer off after NPC has attacked"
	)
	default boolean turnOffAfterAttack()
	{
		return false;
	}

	@ConfigItem(
			keyName = "turnOnIfTargeted",
			name = "Toggle on if new target",
			description = "Turns the prayer on if a new NPC attacks you"
	)
	default boolean turnOnIfTargeted()
	{
		return false;
	}
}
