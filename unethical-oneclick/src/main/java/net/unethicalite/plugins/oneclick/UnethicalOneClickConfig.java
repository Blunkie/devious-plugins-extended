package net.unethicalite.plugins.oneclick;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("hootoneclick")
public interface UnethicalOneClickConfig extends Config
{
	@ConfigSection(
			name = "Game Objects",
			description = "Replace Game Object interactions",
			closedByDefault = true,
			position = 0
	)
	String gameObjs = "Game Objects";

	@ConfigItem(
			keyName = "gameObjects",
			name = "Config",
			description = "Usage = ObjectName:ReplacedAction,Object2:Use ItemName",
			section = gameObjs,
			position = 0
	)
	default String gameObjectConfig()
	{
		return "";
	}

	@ConfigSection(
			name = "NPCs",
			description = "Replace NPC interactions",
			closedByDefault = true,
			position = 1
	)
	String npcs = "NPCs";

	@ConfigItem(
			keyName = "npcs",
			name = "Config",
			description = "Usage = ObjectName:ReplacedAction,Object2:Use ItemName",
			section = npcs,
			position = 0
	)
	default String npcConfig()
	{
		return "";
	}

	@ConfigSection(
			name = "Ground Items",
			description = "Replace Ground Item interactions",
			closedByDefault = true,
			position = 2
	)
	String groundItems = "Ground Items";

	@ConfigItem(
			keyName = "groundItems",
			name = "Config",
			description = "Usage = ObjectName:ReplacedAction,Object2:Use ItemName",
			section = groundItems,
			position = 0
	)
	default String groundItemConfig()
	{
		return "";
	}

	@ConfigSection(
			name = "Items",
			description = "Replace Item interactions",
			closedByDefault = true,
			position = 3
	)
	String items = "Items";

	@ConfigItem(
			keyName = "items",
			name = "Config",
			description = "Usage = ObjectName:ReplacedAction,Object2:Use ItemName",
			section = items,
			position = 0
	)
	default String itemConfig()
	{
		return "";
	}

	@ConfigSection(
			name = "Players",
			description = "Replace Player interactions",
			closedByDefault = true,
			position = 4
	)
	String players = "Players";

	@ConfigItem(
			keyName = "players",
			name = "Config",
			description = "Usage = ObjectName:ReplacedAction,Object2:Use ItemName",
			section = players,
			position = 0
	)
	default String playerConfig()
	{
		return "";
	}

	@ConfigItem(
			keyName = "exactEntityNames",
			name = "Use exact entity names",
			description = "Check for exact entity names",
			position = 5
	)
	default boolean exactEntityNames()
	{
		return true;
	}

	@ConfigItem(
			keyName = "exactItemNames",
			name = "Use exact used item names",
			description = "Check for exact item names for items to use",
			position = 6
	)
	default boolean exactItemNames()
	{
		return true;
	}
}
