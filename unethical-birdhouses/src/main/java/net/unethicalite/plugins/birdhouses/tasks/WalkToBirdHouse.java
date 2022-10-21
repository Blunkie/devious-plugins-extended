package net.unethicalite.plugins.birdhouses.tasks;

import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.plugins.birdhouses.BirdHousesPlugin;
import net.unethicalite.plugins.birdhouses.model.BirdHouse;

public class WalkToBirdHouse extends BirdHouseTask
{
	public WalkToBirdHouse(BirdHousesPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return getNextBirdHouse()
				.map(BirdHouse::getWorldPoint)
				.map(point -> Players.getLocal().distanceTo(point) > 10)
				.orElse(false);
	}

	@Override
	public int execute()
	{
		if (!Movement.isWalking())
		{
			getNextBirdHouse().ifPresent(house -> Movement.walkTo(house.getWorldPoint()));
		}

		return -1;
	}
}
