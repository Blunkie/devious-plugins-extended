package net.unethicalite.plugins.cooker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("unethicalcooker")
public interface UnethicalCookerConfig extends Config
{
	@ConfigItem(
			keyName = "item",
			name = "Item",
			description = ""
	)
	default Meat item()
	{
		return Meat.KARAMBWAN;
	}
}
