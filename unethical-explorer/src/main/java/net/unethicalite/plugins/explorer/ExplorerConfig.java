package net.unethicalite.plugins.explorer;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("unethicalexplorer")
public interface ExplorerConfig extends Config
{
	@ConfigItem(
		keyName = "keyBind",
		name = "Stop explorer hotkey",
		description = "Hotkey to stop the explorer",
		position = 0
	)
	default Keybind stopKeyBind()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "closeMap",
		name = "Close map on selection",
		description = "Close the world map after selecting a destination",
		position = 1
	)
	default boolean closeMap()
	{
		return true;
	}

	@ConfigItem(
			keyName = "coords",
			name = "Custom coords",
			description = "Walk to the specified coordinates",
			position = 2
	)
	default String coords()
	{
		return "3220 3220 0";
	}

	@ConfigItem(
			keyName = "walk",
			name = "Walk to",
			description = "Walk to set coordinates",
			position = 3
	)
	default Button walk()
	{
		return new Button();
	}
}
