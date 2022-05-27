package net.unethicalite.pickpocket;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hootpickpocket")
public interface HootPickpocketConfig extends Config
{
	@ConfigItem(
			keyName = "npcName",
			name = "Npc name",
			description = "",
			position = 1
	)
	default String npcName()
	{
		return "Knight of Ardougne";
	}

	@ConfigItem(
			keyName = "eat",
			name = "Eat",
			description = "",
			position = 1
	)
	default boolean eat()
	{
		return true;
	}

	@ConfigItem(
			keyName = "foodId",
			name = "Food ID",
			description = "",
			position = 1
	)
	default int foodId()
	{
		return 1993;
	}

	@ConfigItem(
			keyName = "eatHp",
			name = "Eat at X missing HP",
			description = "",
			position = 2
	)
	default int eatHp()
	{
		return 11;
	}
}
