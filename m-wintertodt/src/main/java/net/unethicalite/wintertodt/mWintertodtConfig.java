package net.unethicalite.wintertodt;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("mwintertodt")
public interface mWintertodtConfig extends Config
{
    @ConfigItem(keyName = "Food", name = "Food", description = "The food to use", position = 1)
    default FoodType foodType()
    {
        return FoodType.TUNA;
    }

    @Range(max = 15)
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
        return BrazierLocation.EAST;
    }

    @Range(max = 24)
    @ConfigItem(keyName = "Max resources", name = "Max resources", description = "Max amount of Bruma kindling/roots", position = 5)
    default int maxResources()
    {
        return 8;
    }

    @Range(max = 99)
    @ConfigItem(keyName = "Destination level", name = "Destination level", description = "Stop when level is reached", position = 6)
    default int destinationLevel()
    {
        return 99;
    }

    @ConfigItem(keyName = "Overlay enabled", name = "Overlay enabled", description = "Enables overlay", position = 7)
    default boolean overlayEnabled()
    {
        return true;
    }

    @ConfigItem(keyName = "Start", name = "Start/Stop", description = "Start/Stop button", position = 8)
    default Button startStopButton()
    {
        return new Button();
    }
}
