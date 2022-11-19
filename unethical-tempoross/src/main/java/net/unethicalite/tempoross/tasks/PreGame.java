package net.unethicalite.tempoross.tasks;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.tempoross.TemporossPlugin;

import javax.inject.Inject;

import static net.unethicalite.tempoross.TemporossID.ITEM_EMPTY_BUCKET;
import static net.unethicalite.tempoross.TemporossID.OBJECT_LOBBY_LADDER;
import static net.unethicalite.tempoross.TemporossID.OBJECT_LOBBY_PUMP;

public class PreGame extends TemporossTask
{
	@Inject
	private Client client;

	public PreGame(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return !client.isInInstancedRegion();
	}

	@Override
	public int execute()
	{
		setWaves(0);
		setWorkArea(null);
		setIncomingWave(false);
		setScriptState(TemporossPlugin.State.INITIAL_CATCH);

		Player local = Players.getLocal();

		if (local.isMoving() || local.isAnimating())
		{
			return -5;
		}

		TileObject startLadder = TileObjects.getFirstAt(3135, 2840, 0, OBJECT_LOBBY_LADDER);
		if (startLadder == null)
		{
			return -1;
		}

		// If east of ladder, we're not in the room.
		if (local.getWorldLocation().getX() > startLadder.getWorldLocation().getX())
		{
			startLadder.interact("Quick-climb");
			return -6;
		}

		int emptyBuckets = Inventory.getCount(ITEM_EMPTY_BUCKET);
		TileObject waterPump = TileObjects.getFirstAt(3135, 2832, 0, OBJECT_LOBBY_PUMP);
		if (waterPump != null && emptyBuckets > 0)
		{
			waterPump.interact("Use");
			return -6;
		}

		return -1;
	}
}
