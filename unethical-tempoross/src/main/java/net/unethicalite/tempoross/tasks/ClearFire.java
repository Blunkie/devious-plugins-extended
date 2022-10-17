package net.unethicalite.tempoross.tasks;

import net.unethicalite.api.items.Inventory;
import net.unethicalite.tempoross.TemporossPlugin;

import static net.unethicalite.tempoross.TemporossID.ITEM_WATER_BUCKET;

public class ClearFire extends TemporossTask
{
	public ClearFire(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return getFireToClear() != null && Inventory.contains(ITEM_WATER_BUCKET);
	}

	@Override
	public int execute()
	{
		getFireToClear().interact("Douse");
		return -2;
	}
}
