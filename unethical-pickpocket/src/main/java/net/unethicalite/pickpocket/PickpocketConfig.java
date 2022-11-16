package net.unethicalite.pickpocket;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;

@ConfigGroup("unethicalpickpocket")
public interface PickpocketConfig extends Config
{
	@ConfigItem(
			keyName = "bank",
			name = "Bank for food",
			description = "",
			position = 0
	)
	default boolean bank()
	{
		return true;
	}

	@ConfigItem(
			keyName = "bankLocation",
			name = "Bank Location",
			description = "",
			position = 1
	)
	default BankLocation bankLocation()
	{
		return BankLocation.ARDOUGNE_SOUTH_BANK;
	}

	@ConfigItem(
			keyName = "npcName",
			name = "Npc name",
			description = "",
			position = 2
	)
	default String npcName()
	{
		return "Knight of Ardougne";
	}

	@ConfigItem(
			keyName = "eat",
			name = "Eat",
			description = "",
			position = 3
	)
	default boolean eat()
	{
		return true;
	}

	@ConfigItem(
			keyName = "foodId",
			name = "Food ID",
			description = "",
			position = 4
	)
	default int foodId()
	{
		return 1993;
	}

	@ConfigItem(
			keyName = "eatHp",
			name = "Eat at X missing HP",
			description = "",
			position = 5
	)
	default int eatHp()
	{
		return 11;
	}
}
