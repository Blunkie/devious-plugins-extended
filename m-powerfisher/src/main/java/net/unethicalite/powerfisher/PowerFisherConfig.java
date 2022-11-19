package net.unethicalite.powerfisher;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("powerfisher")
public interface PowerFisherConfig extends Config
{
    @ConfigItem(keyName = "Fishing type", name = "Fishing type", description = "Fishing type", position = 1)
    default FishingType fishingType()
    {
        return FishingType.SHRIMPS_AND_ANCHOVIES;
    }

    @ConfigItem(keyName = "Destination level", name = "Destination level", description = "Stop when level is reached", position = 2)
    default int destinationLevel()
    {
        return 99;
    }

    @ConfigItem(keyName = "Start", name = "Start/Stop", description = "Start/Stop button", position = 3)
    default Button startStopButton()
    {
        return new Button();
    }
}
