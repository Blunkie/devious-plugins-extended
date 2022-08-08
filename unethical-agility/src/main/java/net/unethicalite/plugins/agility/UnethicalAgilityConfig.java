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

	@ConfigItem(
			keyName = "useSummerPies",
			name = "Use Summer Pies",
			description = "Enable using Summer Pies to boost Agility",
			position = 20
	)
	default boolean useSummerPies()
	{
		return false;
	}

	@Range(min = 1, max = 5)
	@ConfigItem(
			keyName = "minBoostAmount",
			name = "Min Boost Amount",
			description = "The minimum amount you want your Agility to be boosted",
			position = 30,
			hidden = true,
			unhide = "useSummerPies"
	)
	default int minBoostAmount()
	{
		return 1;
	}

	@ConfigItem(
			keyName = "stopWhenOutOfSummerPies",
			name = "Stop when out of Summer Pies",
			description = "Enable this to prevent trying to do a course you don't have the Agility level for",
			position = 40,
			hidden = true,
			unhide = "useSummerPies"
	)
	default boolean stopWhenOutOfSummerPies()
	{
		return true;
	}

	@ConfigItem(
			keyName = "useStaminas",
			name = "Use Staminas",
			description = "Uses Stamina potions if there are any in your inventory",
			position = 50
	)
	default boolean useStaminas()
	{
		return true;
	}

	@ConfigItem(
			keyName = "minEnergyAmount",
			name = "Min",
			description = "Minimum energy to boost at",
			position = 60,
			hidden = true,
			unhide = "useStaminas"
	)
	default int minEnergyAmount()
	{
		return 20;
	}

	@ConfigItem(
			keyName = "maxEnergyAmount",
			name = "Max",
			description = "Maximum energy to boost at",
			position = 70,
			hidden = true,
			unhide = "useStaminas"
	)
	default int maxEnergyAmount()
	{
		return 40;
	}

	@ConfigItem(
			keyName = "alchItem",
			name = "Item to Alch",
			description = "Item to alch."
	)
	default int itemToAlch()
	{
		return 851;
	} // maple long bow

	@ConfigItem(
			keyName = "shouldAlch",
			name = "Should Alch",
			description = "If selected, plugin will alch the item set in Item to Alch"
	)
	default boolean shouldAlch()
	{
		return false;
	}
}