package net.unethicalite.plugins.zulrah;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("unethicalzulrah")
public interface UnethicalZulrahConfig extends Config
{
	@ConfigItem(
			keyName = "rangeGear",
			name = "Range gear names (start equipped)",
			description = "Start with ranged gear equipped"
	)
	default String rangeGearNames()
	{
		return "";
	}

	@ConfigItem(
			keyName = "mageGear",
			name = "Mage gear names",
			description = ""
	)
	default String mageGearNames()
	{
		return "";
	}
}
