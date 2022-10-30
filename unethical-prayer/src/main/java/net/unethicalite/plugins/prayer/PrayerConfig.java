package net.unethicalite.plugins.prayer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.util.Set;

@ConfigGroup("unethicalprayer")
public interface PrayerConfig extends Config
{
	@ConfigItem(
			keyName = "npcs",
			name = "NPCs to pray against",
			description = ""
	)
	default Set<PrayerNpc> npcs()
	{
		return Set.of();
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
			name = "Toggle on if targeted",
			description = "Turns the prayer on if a new NPC attacks you"
	)
	default boolean turnOnIfTargeted()
	{
		return false;
	}

	@ConfigItem(
			keyName = "turnOnIfTargeting",
			name = "Toggle on if new target",
			description = "Turns the prayer on when you attack"
	)
	default boolean turnOnIfTargeting()
	{
		return false;
	}
}
