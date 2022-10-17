package net.unethicalite.tempoross;

import lombok.Getter;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Players;

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
	private final WorldPoint bossPoint;

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
			this.bossPoint = totemPoint.dx(3).dy(-11);
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
			this.bossPoint = totemPoint.dx(4).dy(9);
		}
	}

	public TileObject getBucketCrate()
	{
		return EntityUtils.getSafeObject(bucketPoint, x -> x.hasAction("Take"));
	}

	public TileObject getPump()
	{
		return EntityUtils.getSafeObject(pumpPoint, x -> x.hasAction("Use"));
	}

	public TileObject getRopeCrate()
	{
		return EntityUtils.getSafeObject(ropePoint, x -> x.hasAction("Take"));
	}

	public TileObject getHammerCrate()
	{
		return EntityUtils.getSafeObject(hammerPoint, x -> x.hasAction("Take"));
	}

	public TileObject getHarpoonCrate()
	{
		return EntityUtils.getSafeObject(harpoonPoint, x -> x.hasAction("Take"));
	}

	public TileObject getMast()
	{
		return EntityUtils.getSafeObject(mastPoint, x -> x.hasAction("Tether", "Untether"));
	}

	public TileObject getTotem()
	{
		return EntityUtils.getSafeObject(totemPoint, x -> x.hasAction("Tether", "Untether"));
	}

	public TileObject getRange()
	{
		return EntityUtils.getSafeObject(rangePoint, x -> x.hasAction("Cook-at"));
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
