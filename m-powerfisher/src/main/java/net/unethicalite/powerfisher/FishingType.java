package net.unethicalite.powerfisher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

@AllArgsConstructor
@Getter
public enum FishingType
{
	SHRIMPS_AND_ANCHOVIES(new String[]{"net"}, new int[]{ItemID.RAW_SHRIMPS, ItemID.RAW_ANCHOVIES}, "Net"),
	TROUT_AND_SALMON(new String[]{"rod", "feathers"}, new int[]{ItemID.RAW_TROUT, ItemID.RAW_SALMON}, "Lure"),
	BARBARIAN_ROD(new String[]{"rod", "feathers"}, new int[]{ItemID.LEAPING_TROUT, ItemID.LEAPING_SALMON, ItemID.LEAPING_STURGEON}, "Use-rod");

	private String[] requiredItems;
	private int[] fishToDrop;
	private String action;
}
