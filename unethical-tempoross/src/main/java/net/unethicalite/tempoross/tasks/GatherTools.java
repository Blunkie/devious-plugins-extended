package net.unethicalite.tempoross.tasks;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.tempoross.TemporossPlugin;

import javax.inject.Inject;

import static net.unethicalite.tempoross.TemporossID.ANIMATION_INTERACTING;
import static net.unethicalite.tempoross.TemporossID.ITEM_EMPTY_BUCKET;
import static net.unethicalite.tempoross.TemporossID.ITEM_HAMMER;
import static net.unethicalite.tempoross.TemporossID.ITEM_HARPOON;
import static net.unethicalite.tempoross.TemporossID.ITEM_ROPE;
import static net.unethicalite.tempoross.TemporossID.ITEM_WATER_BUCKET;

public class GatherTools extends TemporossTask
{
	@Inject
	private Client client;

	public GatherTools(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return Inventory.getCount(ITEM_HARPOON) != 1 || needBuckets() || needRope() || needBuckets();
	}

	@Override
	public int execute()
	{
		Player local = Players.getLocal();
		int animation = local.getAnimation();
		int harpoonCount = Inventory.getCount(ITEM_HARPOON);
		if (harpoonCount != 1)
		{
			if (local.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (harpoonCount > 1)
			{
				Inventory.getFirst(ITEM_HARPOON).interact("Drop");
				return -3;
			}

			if (needToClearFire(client, getWorkArea().getHarpoonCrate()) && getPhase() == 1)
			{
				return -2;
			}

			getWorkArea().getHarpoonCrate().interact("Take");
			return -2;
		}

		if (needBuckets())
		{
			if (local.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (Inventory.getCount(ITEM_EMPTY_BUCKET, ITEM_WATER_BUCKET) > 5)
			{
				Inventory.getFirst(ITEM_EMPTY_BUCKET).interact("Drop");
				return -3;
			}

			if (needToClearFire(client, getWorkArea().getBucketCrate()) && getPhase() == 1)
			{
				return -2;
			}

			getWorkArea().getBucketCrate().interact("Take");
			return -2;
		}

		if (needRope())
		{
			if (local.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (Inventory.getCount(ITEM_ROPE) > 1)
			{
				Inventory.getFirst(ITEM_ROPE).interact("Drop");
				return -3;
			}

			if (needToClearFire(client, getWorkArea().getRopeCrate()) && getPhase() == 1)
			{
				return -2;
			}

			getWorkArea().getRopeCrate().interact("Take");
			return -2;
		}

		if (needHammer())
		{
			if (local.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (Inventory.getCount(ITEM_HAMMER) > 1)
			{
				Inventory.getFirst(ITEM_HAMMER).interact("Drop");
				return -3;
			}

			if (needToClearFire(client, getWorkArea().getHammerCrate()) && getPhase() == 1)
			{
				return -2;
			}

			getWorkArea().getHammerCrate().interact("Take");
			return -2;
		}

		return -1;
	}

	private boolean needBuckets()
	{
		int bucketCount = Inventory.getCount(ITEM_EMPTY_BUCKET, ITEM_WATER_BUCKET);
		return bucketCount != 5 && getScriptState() != TemporossPlugin.State.ATTACK_TEMPOROSS;
	}

	private boolean needRope()
	{
		int ropeCount = Inventory.getCount(ITEM_ROPE);
		return ropeCount != 1 && getScriptState() != TemporossPlugin.State.ATTACK_TEMPOROSS;
	}

	private boolean needHammer()
	{
		int hammerCount = Inventory.getCount(ITEM_HAMMER);
		return hammerCount != 1 && getScriptState() != TemporossPlugin.State.ATTACK_TEMPOROSS;
	}
}
