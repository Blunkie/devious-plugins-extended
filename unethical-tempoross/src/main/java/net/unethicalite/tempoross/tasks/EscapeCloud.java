package net.unethicalite.tempoross.tasks;

import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.tempoross.TemporossPlugin;

public class EscapeCloud extends TemporossTask
{
	public EscapeCloud(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return inCloud(Players.getLocal().getWorldLocation());
	}

	@Override
	public int execute()
	{
		if (Movement.isWalking())
		{
			return -1;
		}

		walkToSafePoint();
		return -2;
	}
}
