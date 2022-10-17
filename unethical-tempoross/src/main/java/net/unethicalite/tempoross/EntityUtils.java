package net.unethicalite.tempoross;

import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;

import java.util.function.Predicate;

public class EntityUtils
{
	public static TileObject getSafeObject(WorldPoint tile, Predicate<TileObject> filter)
	{
		return TileObjects.getFirstAt(tile, t ->
		{
			if (!filter.test(t))
			{
				return false;
			}

			NPC nearestFire = getNearestFire(t);
			return nearestFire == null || nearestFire.distanceTo(t) > 1;
		});
	}

	public static NPC getNearestFire(SceneEntity entity)
	{
		return NPCs.query()
				.ids(TemporossID.NPC_FIRE)
				.distance(entity, 3)
				.results()
				.nearest(entity);
	}
}
