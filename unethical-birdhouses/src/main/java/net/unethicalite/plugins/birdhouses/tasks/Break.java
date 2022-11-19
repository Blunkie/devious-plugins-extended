package net.unethicalite.plugins.birdhouses.tasks;

import net.unethicalite.api.game.Game;
import net.unethicalite.plugins.birdhouses.BirdHousesConfig;
import net.unethicalite.plugins.birdhouses.BirdHousesPlugin;

import javax.inject.Inject;

public class Break extends BirdHouseTask
{
	@Inject
	private BirdHousesConfig config;

	public Break(BirdHousesPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return config.logout();
	}

	@Override
	public int execute()
	{
		if (Game.isLoggedIn())
		{
			Game.logout();
		}

		return -1;
	}
}
