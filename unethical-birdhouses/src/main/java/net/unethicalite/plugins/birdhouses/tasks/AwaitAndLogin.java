package net.unethicalite.plugins.birdhouses.tasks;

import net.unethicalite.api.game.Game;
import net.unethicalite.api.plugins.PluginStoppedException;
import net.unethicalite.plugins.birdhouses.BirdHousesPlugin;

public class AwaitAndLogin extends BirdHouseTask
{
	public AwaitAndLogin(BirdHousesPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return !Game.isLoggedIn();
	}

	@Override
	public int execute()
	{
		if (getAvailableBirdHouses().size() != 4)
		{
			return -1;
		}

		int sleep = getLoginEvent().login();
		if (sleep == -1000)
		{
			throw new PluginStoppedException();
		}

		return sleep;
	}
}
