package net.unethicalite.plugins.birdhouses.tasks;

import net.unethicalite.api.game.Game;
import net.unethicalite.api.script.blocking_events.LoginEvent;
import net.unethicalite.plugins.birdhouses.BirdHousesConfig;
import net.unethicalite.plugins.birdhouses.BirdHousesPlugin;

import javax.inject.Inject;

public class Break extends BirdHouseTask
{
	@Inject
	private BirdHousesConfig config;

	public Break(BirdHousesPlugin context)
	{
		super(context, false);
	}

	@Override
	public boolean validate()
	{
		return config.logout();
	}

	@Override
	public int execute()
	{
		if (context.getBlockingEventManager().getLoginEvent() != null)
		{
			context.getBlockingEventManager().remove(LoginEvent.class);
			return -1;
		}

		if (Game.isLoggedIn())
		{
			Game.logout();
		}

		return -1;
	}

	@Override
	public boolean inject()
	{
		return true;
	}
}
