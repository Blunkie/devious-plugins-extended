package net.unethicalite.plugins.butler;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

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

	@ConfigItem(
			keyName = "keyBind",
			name = "OneClick hotkey",
			description = "Hotkey for item -> butler oneclicks",
			position = 2
	)
	default Keybind keyBind()
	{
		return Keybind.SHIFT;
	}
}
