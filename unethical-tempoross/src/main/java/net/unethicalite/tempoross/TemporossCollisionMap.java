package net.unethicalite.tempoross;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.movement.pathfinder.LocalCollisionMap;

public class TemporossCollisionMap extends LocalCollisionMap
{
	public TemporossCollisionMap(boolean blockDoors)
	{
		super(blockDoors);
	}

	@Override
	public boolean n(int x, int y, int z)
	{
		if (!super.n(x, y, z))
		{
			return false;
		}

		WorldPoint north = new WorldPoint(x, y, z).dy(1);

		return NPCs.query()
				.ids(TemporossID.NPC_FIRE)
				.filter(npc -> npc.getWorldArea().contains(north))
				.results()
				.isEmpty();
	}

	@Override
	public boolean e(int x, int y, int z)
	{
		if (!super.e(x, y, z))
		{
			return false;
		}

		WorldPoint east = new WorldPoint(x, y, z).dx(1);

		return NPCs.query()
				.ids(TemporossID.NPC_FIRE)
				.filter(npc -> npc.getWorldArea().contains(east))
				.results()
				.isEmpty();
	}
}
