package net.unethicalite.fighter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("hootfighter")
public interface FighterConfig extends Config
{
	@ConfigSection(
			name = "General",
			description = "General settings",
			position = 0,
			closedByDefault = true
	)
	String general = "General";

	@ConfigSection(
			name = "Health",
			description = "General settings",
			position = 1,
			closedByDefault = true
	)
	String health = "Health";

	@ConfigSection(
			name = "Loot",
			description = "Loot settings",
			position = 2,
			closedByDefault = true
	)
	String loot = "Loot";

	@ConfigSection(
			name = "Prayers",
			description = "Prayers settings",
			position = 3,
			closedByDefault = true
	)
	String prayers = "Prayers";

	@ConfigSection(
			name = "Alching",
			description = "Alching settings",
			position = 4,
			closedByDefault = true
	)
	String alching = "Alching";

	@ConfigSection(
			name = "Antipoison",
			description = "Antipoison settings",
			position = 5,
			closedByDefault = true
	)
	String antipoison = "Antipoison";

	@ConfigSection(
			name = "Slayer",
			description = "Slayer settings",
			position = 6,
			closedByDefault = true
	)
	String slayer = "Slayer";

	@ConfigSection(
			name = "Antifire",
			description = "Automatically uses antifire",
			position = 7,
			closedByDefault = true
	)
	String antifire = "Antifire";

	@ConfigItem(
			keyName = "monster",
			name = "Monster",
			description = "Monster to kill",
			position = 0,
			section = general
	)
	default String monster()
	{
		return "Chicken";
	}

	@Range(max = 100)
	@ConfigItem(
			keyName = "attackRange",
			name = "Attack range",
			description = "Monster attack range",
			position = 1,
			section = general
	)
	default int attackRange()
	{
		return 15;
	}

	@ConfigItem(
			keyName = "bury",
			name = "Bury bones",
			description = "Bury bones",
			position = 2,
			section = general
	)
	default boolean buryBones()
	{
		return true;
	}

	@ConfigItem(
			keyName = "loots",
			name = "Loot Items",
			description = "Items to loot separated by comma. ex: Lobster,Tuna",
			position = 0,
			section = loot
	)
	default String loot()
	{
		return "Any";
	}

	@ConfigItem(
			keyName = "lootValue",
			name = "Loot GP value",
			description = "Items to loot by value, 0 to check by name only",
			position = 1,
			section = loot
	)
	default int lootValue()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "untradables",
			name = "Loot untradables",
			description = "Loot untradables",
			position = 2,
			section = loot
	)
	default boolean untradables()
	{
		return true;
	}

	@ConfigItem(
			keyName = "eat",
			name = "Eat food",
			description = "Eat food to heal",
			position = 0,
			section = health
	)
	default boolean eat()
	{
		return true;
	}

	@Range(max = 100)
	@ConfigItem(
			keyName = "eatHealthPercent",
			name = "Health %",
			description = "Health % to eat at",
			position = 1,
			section = health
	)
	default int healthPercent()
	{
		return 65;
	}

	@ConfigItem(
			keyName = "foods",
			name = "Food",
			description = "Food to eat, separated by comma. ex: Bones,Coins",
			position = 0,
			section = health
	)
	default String foods()
	{
		return "Any";
	}

	@ConfigItem(
			keyName = "quickPrayer",
			name = "Use Quick Prayers",
			description = "Use Quick Prayers",
			position = 0,
			section = prayers
	)
	default boolean quickPrayer()
	{
		return false;
	}

	@ConfigItem(
			keyName = "flick",
			name = "Flick",
			description = "One ticks quick prayers",
			position = 1,
			section = prayers
	)
	default boolean flick()
	{
		return false;
	}

	@ConfigItem(
			keyName = "restore",
			name = "Restore prayer",
			description = "Drinks pots to restore prayer points",
			position = 2,
			section = prayers
	)
	default boolean restore()
	{
		return false;
	}

	@ConfigItem(
			keyName = "alch",
			name = "Alch items",
			description = "Alchs items",
			position = 0,
			section = alching
	)
	default boolean alching()
	{
		return false;
	}

	@ConfigItem(
			keyName = "alchSpell",
			name = "Alch spell",
			description = "Alch spell",
			position = 1,
			section = alching
	)
	default AlchSpell alchSpell()
	{
		return AlchSpell.HIGH;
	}

	@ConfigItem(
			keyName = "alchItems",
			name = "Alch items",
			description = "Items to alch, separated by comma. ex: Maple shortbow,Rune scimitar",
			position = 2,
			section = alching
	)
	default String alchItems()
	{
		return "Weed";
	}

	@ConfigItem(
			keyName = "antipoison",
			name = "Use antipoison",
			description = "Automatically cure antipoison",
			position = 0,
			section = antipoison
	)
	default boolean antipoison()
	{
		return false;
	}

	@ConfigItem(
			keyName = "antipoisonType",
			name = "Antipoison type",
			description = "Type of antipoison potion to drink when poisoned",
			position = 1,
			section = antipoison
	)
	default AntipoisonType antipoisonType()
	{
		return AntipoisonType.ANTIPOISON;
	}

	@ConfigItem(
			keyName = "disableOnTaskCompletion",
			name = "Disable after task",
			description = "Disables plugin once slayer task is finished, so you don't continue attacking monster",
			position = 0,
			section = slayer
	)
	default boolean disableAfterSlayerTask()
	{
		return false;
	}
	
	@ConfigItem(
			keyName = "antifire",
			name = "Use antifire",
			description = "Automatically sips antifire",
			position = 0,
			section = antifire
	)
	default boolean antifire()
	{
		return false;
	}

	@ConfigItem(
			keyName = "antifireType",
			name = "Antifire type",
			description = "Type of antifire potion to drink",
			position = 1,
			section = antifire
	)
	default AntifireType antifireType()
	{
		return AntifireType.ANTIFIRE;
	}
}
