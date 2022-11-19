package net.unethicalite.tempoross;

import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import lombok.Getter;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;

@Getter
public class TemporossWorkArea
{
	private final WorldPoint exitNpc;

	private final WorldPoint safePoint;
	private final WorldPoint bucketPoint;
	private final WorldPoint pumpPoint;
	private final WorldPoint ropePoint;
	private final WorldPoint hammerPoint;
	private final WorldPoint harpoonPoint;
	private final WorldPoint mastPoint;
	private final WorldPoint totemPoint;
	private final WorldPoint rangePoint;

	public TemporossWorkArea(WorldPoint exitNpc, boolean isWest)
	{
		this.exitNpc = exitNpc;
		this.safePoint = exitNpc.dx(1).dy(1);

		if (isWest)
		{
			this.bucketPoint = exitNpc.dx(-3).dy(-1);
			this.pumpPoint = exitNpc.dx(-3).dy(-2);
			this.ropePoint = exitNpc.dx(-3).dy(-5);
			this.hammerPoint = exitNpc.dx(-3).dy(-6);
			this.harpoonPoint = exitNpc.dx(-2).dy(-7);
			this.mastPoint = exitNpc.dx(0).dy(-3);
			this.totemPoint = exitNpc.dx(8).dy(15);
			this.rangePoint = exitNpc.dx(3).dy(21);
		}
		else
		{
			this.bucketPoint = exitNpc.dx(3).dy(1);
			this.pumpPoint = exitNpc.dx(3).dy(2);
			this.ropePoint = exitNpc.dx(3).dy(5);
			this.hammerPoint = exitNpc.dx(3).dy(6);
			this.harpoonPoint = exitNpc.dx(2).dy(7);
			this.mastPoint = exitNpc.dx(0).dy(3);
			this.totemPoint = exitNpc.dx(-15).dy(-13);
			this.rangePoint = exitNpc.dx(-23).dy(-19);
		}
	}

	public TileObject getBucketCrate()
	{
		return TileObjects.getFirstAt(bucketPoint, x -> x.hasAction("Take"));
	}

	public TileObject getPump()
	{
		return TileObjects.getFirstAt(pumpPoint, x -> x.hasAction("Use"));
	}

	public TileObject getRopeCrate()
	{
		return TileObjects.getFirstAt(ropePoint, x -> x.hasAction("Take"));
	}

	public TileObject getHammerCrate()
	{
		return TileObjects.getFirstAt(hammerPoint, x -> x.hasAction("Take"));
	}

	public TileObject getHarpoonCrate()
	{
		return TileObjects.getFirstAt(harpoonPoint, x -> x.hasAction("Take"));
	}

	public TileObject getMast()
	{
		return TileObjects.getFirstAt(mastPoint, x -> x.hasAction("Tether", "Untether"));
	}

	public TileObject getTotem()
	{
		return TileObjects.getFirstAt(totemPoint, x -> x.hasAction("Tether", "Untether"));
	}

	public TileObject getRange()
	{
		return TileObjects.getFirstAt(rangePoint, x -> x.hasAction("Cook-at"));
	}

	public TileObject getClosestTether()
	{
		TileObject mast = getMast();
		TileObject totem = getTotem();
		if (mast != null && totem != null)
		{
			int mastDistance = mast.getWorldLocation().distanceTo(Players.getLocal().getWorldLocation());
			int totemDistance = totem.getWorldLocation().distanceTo(Players.getLocal().getWorldLocation());
			if (mastDistance < totemDistance)
			{
				return mast;
			}

			return totem;
		}

		if (mast != null)
		{
			return mast;
		}

		return totem;
	}
}
