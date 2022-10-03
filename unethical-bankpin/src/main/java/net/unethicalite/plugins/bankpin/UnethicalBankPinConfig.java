package net.unethicalite.plugins.bankpin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("unethicalbankpin")
public interface UnethicalBankPinConfig extends Config
{
	@ConfigItem(
			keyName = "pin",
			name = "Pin",
			description = "Your bank pin"
	)
	default String pin()
	{
		return "0000";
	}
}
