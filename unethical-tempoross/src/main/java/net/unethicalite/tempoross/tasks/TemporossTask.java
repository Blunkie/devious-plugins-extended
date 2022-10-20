package net.unethicalite.tempoross.tasks;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.runelite.api.Client;
import net.runelite.api.Locatable;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.tempoross.TemporossPlugin;

import java.util.Comparator;
import java.util.List;

import static net.unethicalite.tempoross.TemporossID.ANIMATION_INTERACTING;
import static net.unethicalite.tempoross.TemporossID.ITEM_EMPTY_BUCKET;
import static net.unethicalite.tempoross.TemporossID.ITEM_WATER_BUCKET;
import static net.unethicalite.tempoross.TemporossID.NPC_FIRE;
import static net.unethicalite.tempoross.TemporossID.OBJECT_CLOUD_SHADOW;

@RequiredArgsConstructor
public abstract class TemporossTask implements Task
{
	@Delegate
	protected final TemporossPlugin context;

	protected int getPhase()
	{
		return 1 + (getWaves() / 3); // every 3 waves, phase increases by 1
	}

	protected void walkToSafePoint()
	{
		Player player = Players.getLocal();
		WorldPoint safePoint = getWorkArea().getSafePoint();
		if (safePoint.distanceTo(player.getWorldLocation()) > 3 && !player.isMoving())
		{
			Movement.walk(safePoint);
		}
	}

	protected boolean inCloud(WorldPoint point)
	{
		return TileObjects.getFirstSurrounding(point, 3, OBJECT_CLOUD_SHADOW) != null;
	}

	protected boolean needToClearFire(Client client, Locatable locatable)
	{
		return needToClearFire(client, locatable.getWorldLocation());
	}

	protected boolean needToClearFire(Client client, WorldPoint destination)
	{
		Player player = Players.getLocal();
		int bucketOfWaterCount = Inventory.getCount(ITEM_WATER_BUCKET);
		List<WorldPoint> path = player.getWorldLocation().pathTo(client, destination);
		List<NPC> firesBlockingPath = NPCs.getAll(x -> x.getId() == NPC_FIRE &&
				x.getWorldArea().toWorldPointList().stream().anyMatch(path::contains));
		NPC fire = firesBlockingPath.stream()
				.min(Comparator.comparing(x -> x.getWorldLocation().distanceTo(player.getWorldLocation())))
				.orElse(null);
		if (fire != null)
		{
			if (bucketOfWaterCount == 0 && Inventory.contains(ITEM_EMPTY_BUCKET))
			{
				if (player.getAnimation() == ANIMATION_INTERACTING)
				{
					return true;
				}

				if (player.isMoving())
				{
					return true;
				}

				getWorkArea().getPump().interact("Use");
				return true;
			}

			if (bucketOfWaterCount > 0)
			{
				fire.interact("Douse");
			}

			return true;
		}

		return false;
	}
}
