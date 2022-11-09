package net.unethicalite.plugins.cooker;

import lombok.Getter;
import net.runelite.api.ItemID;

@Getter
public enum Meat
{
	MEAT(ItemID.RAW_BEEF, ItemID.COOKED_MEAT, 4),
	SHRIMPS(ItemID.RAW_SHRIMPS, ItemID.SHRIMPS, 4),
	CHICKEN(ItemID.RAW_CHICKEN, ItemID.COOKED_CHICKEN, 4),
	RABBIT(ItemID.RAW_RABBIT, ItemID.RABBIT, 4),
	ANCHOVIES(ItemID.RAW_ANCHOVIES, ItemID.ANCHOVIES, 4),
	SARDINE(ItemID.RAW_SARDINE, ItemID.SARDINE, 4),
	HERRING(ItemID.RAW_HERRING, ItemID.HERRING, 4),
	MACKEREL(ItemID.RAW_MACKEREL, ItemID.MACKEREL, 4),
	TROUT(ItemID.RAW_TROUT, ItemID.TROUT, 4),
	COD(ItemID.RAW_COD, ItemID.COD, 4),
	PIKE(ItemID.RAW_PIKE, ItemID.PIKE, 4),
	SALMON(ItemID.RAW_SALMON, ItemID.SALMON, 4),
	TUNA(ItemID.RAW_TUNA, ItemID.TUNA, 4),
	LOBSTER(ItemID.RAW_LOBSTER, ItemID.LOBSTER, 4),
	BASS(ItemID.RAW_BASS, ItemID.BASS, 4),
	SWORDFISH(ItemID.RAW_SWORDFISH, ItemID.SWORDFISH, 4),
	MONKFISH(ItemID.RAW_MONKFISH, ItemID.MONKFISH, 4),
	KARAMBWAN(ItemID.RAW_KARAMBWAN, ItemID.COOKED_KARAMBWAN, 4, 1),
	SHARK(ItemID.RAW_SHARK, ItemID.SHARK, 4),
	SEA_TURTLE(ItemID.RAW_SEA_TURTLE, ItemID.SEA_TURTLE, 4),
	ANGLERFISH(ItemID.RAW_ANGLERFISH, ItemID.ANGLERFISH, 4),
	DARK_CRAB(ItemID.RAW_DARK_CRAB, ItemID.DARK_CRAB, 4),
	MANTA_RAY(ItemID.RAW_MANTA_RAY, ItemID.MANTA_RAY, 4),

	;

	private final int rawId;
	private final int cookedId;
	private final int cookTicks;
	private final int productionIndex;

	Meat(int rawId, int cookedId, int cookTicks, int productionIndex)
	{
		this.rawId = rawId;
		this.cookedId = cookedId;
		this.cookTicks = cookTicks;
		this.productionIndex = productionIndex;
	}

	Meat(int rawId, int cookedId, int cookTicks)
	{
		this(rawId, cookedId, cookTicks, 0);
	}
}
