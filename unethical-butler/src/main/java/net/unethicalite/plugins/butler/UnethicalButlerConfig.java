package net.unethicalite.plugins.butler;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("hootbutler")
public interface UnethicalButlerConfig extends Config
{
	@ConfigItem(
			keyName = "logType",
			name = "Log Type",
			description = "Type of log/plank you're using",
			position = 0
	)
	default LogType logType()
	{
		return LogType.MAHOGANY;
	}

	@ConfigItem(
			keyName = "butler",
			name = "Butler NPC name",
			description = "The butler's NPC name",
			position = 1
	)
	default String butler()
	{
		return "Demon butler";
	}

	@Range(max = 28)
	@ConfigItem(
			keyName = "amount",
			name = "Plank amount",
			description = "Amount of planks/logs to give",
			position = 2
	)
	default int amount()
	{
		return 18;
	}

	@ConfigItem(
			keyName = "keyBind",
			name = "OneClick hotkey",
			description = "Hotkey for item -> butler oneclicks",
			position = 3
	)
	default Keybind keyBind()
	{
		return Keybind.SHIFT;
	}
}
