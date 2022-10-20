package net.unethicalite.plugins.birdhouses.tasks;

import net.unethicalite.plugins.birdhouses.BirdHousesPlugin;

public class GatherTools extends BirdHouseTask
{
	public GatherTools(BirdHousesPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return false;
	}

	@Override
	public int execute()
	{
		return 0;
	}
}
