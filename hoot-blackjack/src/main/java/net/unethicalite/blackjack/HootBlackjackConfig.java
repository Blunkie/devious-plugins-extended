package net.unethicalite.blackjack;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hootblackjack")
public interface HootBlackjackConfig extends Config
{
	@ConfigItem(
			keyName = "blackjackSpot",
			name = "Blackjack Spot",
			description = "",
			position = 0
	)
	default BlackjackSpot blackjackSpot()
	{
		return BlackjackSpot.BANDIT_ONE;
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

	@ConfigItem(
			keyName = "buyWines",
			name = "Buy wines",
			description = "",
			position = 3
	)
	default boolean buyWines()
	{
		return true;
	}
}
