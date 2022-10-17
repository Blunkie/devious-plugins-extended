package net.unethicalite.tempoross.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.TileObject;
import net.unethicalite.api.entities.Players;
import net.unethicalite.tempoross.TemporossPlugin;

import static net.unethicalite.tempoross.TemporossID.GRAPHIC_TETHERED;
import static net.unethicalite.tempoross.TemporossID.GRAPHIC_TETHERING;

@Slf4j
public class Tether extends TemporossTask
{
	public Tether(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return isIncomingWave();
	}

	@Override
	public int execute()
	{
		TileObject tether = getWorkArea().getClosestTether();
		if (!isTethered())
		{
			if (tether == null)
			{
				log.warn("Can't find tether object");
				return -1;
			}

			tether.interact("Tether");
			return -3;
		}

		return -2;
	}

	private boolean isTethered()
	{
		int graphic = Players.getLocal().getGraphic();
		int anim = Players.getLocal().getAnimation();
		return anim != 832 && (graphic == GRAPHIC_TETHERED || graphic == GRAPHIC_TETHERING);
	}
}
