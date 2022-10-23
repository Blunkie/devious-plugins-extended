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
		return !FOSSIL_ISLAND_CHEST_POINT.dx(-1).equals(Players.getLocal().getWorldLocation());
	}

	@Override
	public int execute()
	{
		if (Movement.isWalking())
		{
			return -1;
		}

		Movement.walkTo(FOSSIL_ISLAND_CHEST_POINT.dx(-1));
		return -3;
	}
}
