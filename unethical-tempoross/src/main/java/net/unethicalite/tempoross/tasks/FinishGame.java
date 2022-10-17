package net.unethicalite.tempoross.tasks;

import net.runelite.api.NPC;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.tempoross.TemporossPlugin;

public class FinishGame extends TemporossTask
{
	public FinishGame(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return NPCs.getNearest(x -> x.hasAction("Leave")) != null;
	}

	@Override
	public int execute()
	{
		NPC leave = NPCs.getNearest(x -> x.hasAction("Leave"));
		if (leave != null)
		{
			if (leave.distanceTo(Players.getLocal()) > 6)
			{
				Movement.walkTo(leave);
				return -2;
			}

			leave.interact("Leave");
			return -6;
		}

		return -1;
	}
}
