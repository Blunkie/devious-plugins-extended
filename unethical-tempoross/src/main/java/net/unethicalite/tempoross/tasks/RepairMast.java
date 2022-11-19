package net.unethicalite.tempoross.tasks;

import net.runelite.api.Client;
import net.runelite.api.TileObject;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.tempoross.TemporossPlugin;

import javax.inject.Inject;

import java.util.Optional;

import static net.unethicalite.tempoross.TemporossID.OBJECT_DAMAGED_MAST;

public class RepairMast extends TemporossTask
{
	@Inject
	private Client client;

	public RepairMast(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return isMastDamaged();
	}

	@Override
	public int execute()
	{
		if (isMastDamaged())
		{
			getDamagedMast().interact("Repair");
			return 1000;
		}

		return -1;
	}

	private boolean isMastDamaged()
	{
		return Optional.ofNullable(getDamagedMast())
				.map(mast -> mast.getWorldLocation().distanceToPath(client, Players.getLocal().getWorldLocation()) < 15)
				.orElse(false);
	}

	private TileObject getDamagedMast()
	{
		return TileObjects.getFirstAt(Tiles.getAt(getWorkArea().getMastPoint()), OBJECT_DAMAGED_MAST);
	}
}
