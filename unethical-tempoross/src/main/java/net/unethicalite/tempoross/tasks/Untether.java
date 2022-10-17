package net.unethicalite.tempoross.tasks;

import net.unethicalite.api.entities.Players;
import net.unethicalite.tempoross.TemporossPlugin;

import static net.unethicalite.tempoross.TemporossID.GRAPHIC_TETHERED;

public class Untether extends TemporossTask
{
	public Untether(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return getWorkArea().getClosestTether() != null && Players.getLocal().getGraphic() == GRAPHIC_TETHERED;
	}

	@Override
	public int execute()
	{
		getWorkArea().getClosestTether().interact("Untether");
		return -4;
	}
}
