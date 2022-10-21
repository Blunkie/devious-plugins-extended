package net.unethicalite.plugins.birdhouses.tasks;

import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.plugins.birdhouses.BirdHousesPlugin;

public class WaitAtBank extends BirdHouseTask
{
	public WaitAtBank(BirdHousesPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return true;
	}

	@Override
	public int execute()
	{
		if (Movement.isWalking())
		{
			return -1;
		}

		if (!FOSSIL_ISLAND_CHEST_POINT.dx(-1).equals(Players.getLocal().getWorldLocation()))
		{
			Movement.walkTo(FOSSIL_ISLAND_CHEST_POINT.dx(-1));
			return -3;
		}

		return -1;
	}
}
