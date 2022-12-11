package net.unethicalite.wintertodt;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("mwintertodt")
public interface mWintertodtConfig extends Config
{
	@ConfigItem(keyName = "Food name", name = "Food name", description = "The food to use", position = 1)
	default String foodName()
	{
		return "Tuna";
	}

	@Range(max = 16)
	@ConfigItem(keyName = "Food amount", name = "Food amount", description = "The food amount to take from bank", position = 2)
	default int foodAmount()
	{
		return 7;
	}

	@Range(max = 100)
	@ConfigItem(keyName = "Health percent", name = "Health %", description = "Health % to eat at", position = 3)
	default int healthPercent()
	{
		return 65;
	}

	@ConfigItem(keyName = "Brazier location", name = "Brazier location", description = "The brazier to use", position = 4)
	default BrazierLocation brazierLocation()
	{
		return BrazierLocation.RANDOM;
	}

	@ConfigItem(keyName = "Fix brazier", name = "Fix broken brazier", description = "Fixes broken brazier if has hammer in inventory", position = 5)
	default boolean fixBrokenBrazier()
	{
		return true;
	}

	@ConfigItem(keyName = "Light brazier", name = "Light unlit brazier", description = "Light unlit brazier if has tinderbox in inventory", position = 6)
	default boolean lightUnlitBrazier()
	{
		return true;
	}

	@ConfigItem(keyName = "Fletching enabled", name = "Fletching enabled", description = "Enables fletching if has knife in inventory", position = 7)
	default boolean fletchingEnabled()
	{
		return true;
	}

	@Range(max = 24)
	@ConfigItem(keyName = "Max resources", name = "Max resources", description = "Max amount of Bruma kindling/roots in inventory before feeding the brazier", position = 8)
	default int maxResources()
	{
		return 8;
	}

	@ConfigItem(keyName = "Overlay enabled", name = "Overlay enabled", description = "Enables overlay", position = 9)
	default boolean overlayEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "Start", name = "Start/Stop", description = "Start/Stop button", position = 10)
	default Button startStopButton()
	{
		return new Button();
	}
}
