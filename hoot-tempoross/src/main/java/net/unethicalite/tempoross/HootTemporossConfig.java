package net.unethicalite.tempoross;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hoottempoross")
public interface HootTemporossConfig extends Config
{
	@ConfigItem(
			keyName = "cook",
			name = "Cook",
			description = "Cook harpoonfish"
	)
	default boolean cook()
	{
		return false;
	}
}
