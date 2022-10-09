package net.unethicalite.plugins.zulrah;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.unethicalite.plugins.zulrah.config.MagePrayer;
import net.unethicalite.plugins.zulrah.config.RangePrayer;

@ConfigGroup("unethicalzulrah")
public interface UnethicalZulrahConfig extends Config
{
	@ConfigItem(
			keyName = "rangeGear",
			name = "Ranged gear names",
			description = ""
	)
	default String rangeGearNames()
	{
		return "Ancient d'hide body,Ancient chaps,Infinity boots,Toxic blowpipe,Ava's assembler";
	}

	@ConfigItem(
			keyName = "mageGear",
			name = "Mage gear names",
			description = "Ahrim's robetop,Ahrim's robeskirt,Trident of the swamp,Book of darkness"
	)
	default String mageGearNames()
	{
		return "Ahrim's robetop,Ahrim's robeskirt,Trident of the swamp,Book of darkness";
	}

	@ConfigItem(
			keyName = "magePrayer",
			name = "Magic prayer",
			description = "Mage prayer setting"
	)
	default MagePrayer magePrayer()
	{
		return MagePrayer.MYSTIC_MIGHT;
	}

	@ConfigItem(
			keyName = "rangePrayer",
			name = "Range prayer",
			description = "Range prayer setting"
	)
	default RangePrayer rangePrayer()
	{
		return RangePrayer.EAGLE_EYE;
	}
}
