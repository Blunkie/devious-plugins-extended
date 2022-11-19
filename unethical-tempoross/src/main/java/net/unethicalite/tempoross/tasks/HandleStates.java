package net.unethicalite.tempoross.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.tempoross.TemporossPlugin;

import javax.inject.Inject;
import java.util.Set;

import static net.unethicalite.tempoross.TemporossID.ANIMATION_COOK;
import static net.unethicalite.tempoross.TemporossID.ITEM_RAW_FISH;
import static net.unethicalite.tempoross.TemporossID.NPC_DOUBLE_FISH_SPOT;
import static net.unethicalite.tempoross.TemporossID.NPC_SINGLE_FISH_SPOT;
import static net.unethicalite.tempoross.TemporossID.NPC_SINGLE_FISH_SPOT_SECOND;
import static net.unethicalite.tempoross.TemporossID.NPC_VULN_WHIRLPOOL;

@Slf4j
public class HandleStates extends TemporossTask
{
	@Inject
	private Client client;

	public HandleStates(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return getScriptState() != null;
	}

	@Override
	public int execute()
	{
		Player local = Players.getLocal();
		switch (getScriptState())
		{
			case INITIAL_CATCH:
			case SECOND_CATCH:
			case THIRD_CATCH:
				NPC fishSpot = NPCs.getNearest(it ->
						NPC_DOUBLE_FISH_SPOT == it.getId()
								&& !inCloud(it.getWorldLocation())
								&& it.getWorldLocation().distanceTo(getWorkArea().getRangePoint()) <= 20
				);

				if (fishSpot == null)
				{
					fishSpot = NPCs.getNearest(it ->
							Set.of(NPC_SINGLE_FISH_SPOT, NPC_SINGLE_FISH_SPOT_SECOND).contains(it.getId())
									&& !inCloud(it.getWorldLocation())
									&& it.getWorldLocation().distanceTo(getWorkArea().getRangePoint()) <= 20
					);
				}

				if (fishSpot != null)
				{
					if (fishSpot.equals(local.getInteracting()) && !Dialog.isOpen())
					{
						return 1000;
					}

					if (needToClearFire(client, fishSpot))
					{
						return -2;
					}

					fishSpot.interact("Harpoon");
				}
				else
				{
					// if fish are null walk to the totem pole since it's in the center of the fish spots.
					if (!Movement.walkTo(getWorkArea().getTotemPoint()))
					{
						log.debug("Path was blocked");
					}
				}

				return 1000;

			case INITIAL_COOK:
			case SECOND_COOK:
			case THIRD_COOK:
				TileObject range = getWorkArea().getRange();
				int rawFishCount = Inventory.getCount(ITEM_RAW_FISH);
				if (range != null && rawFishCount > 0)
				{
					if ((local.getAnimation() == ANIMATION_COOK || local.isMoving()) && !Dialog.isOpen())
					{
						return 1000;
					}

					if (needToClearFire(client, range))
					{
						return -2;
					}

					range.interact("Cook-at");
					return 1200;
				}
				else if (range == null)
				{
					if (!Movement.walkTo(getWorkArea().getRangePoint()))
					{
						log.debug("Path was incomplete");
					}
					return 1000;
				}

			case EMERGENCY_FILL:
			case SECOND_FILL:
			case INITIAL_FILL:
				NPC ammoCrate = NPCs.getNearest(x -> x.hasAction("Fill")
						&& x.getWorldLocation().distanceTo(getWorkArea().getSafePoint()) <= 10
						&& x.hasAction("Check-ammo")
						&& !inCloud(x.getWorldLocation()));
				if (ammoCrate != null && (!ammoCrate.equals(local.getInteracting()) || Dialog.isOpen()))
				{
					if (needToClearFire(client, ammoCrate))
					{
						return -2;
					}

					ammoCrate.interact("Fill");
					return 1000;
				}
				else if (ammoCrate == null)
				{
					log.warn("Can't find the ammo crate");
					if (needToClearFire(client, getWorkArea().getSafePoint()))
					{
						return -2;
					}

					walkToSafePoint();
				}
				break;

			case ATTACK_TEMPOROSS:
				NPC temporossPool = NPCs.getNearest(NPC_VULN_WHIRLPOOL);
				if (temporossPool != null && (!temporossPool.equals(local.getInteracting()) || Dialog.isOpen()))
				{
					if (needToClearFire(client, temporossPool))
					{
						return -2;
					}

					temporossPool.interact("Harpoon");
					return 1000;
				}
				else if (temporossPool == null)
				{
					if (TemporossPlugin.energy > 0)
					{
						setTemporossVulnerable(false);
						setScriptState(null);
						return -1;
					}

					if (Movement.isWalking())
					{
						return -2;
					}

					if (getWorkArea().getBossPoint().distanceTo(Players.getLocal()) > 3)
					{
						Movement.walkTo(getWorkArea().getBossPoint());
					}
				}

				break;
		}

		return -1;
	}
}
