package net.unethicalite.plugins.zulrah.tasks;

import net.runelite.api.NPC;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.plugins.zulrah.data.Constants;
import net.unethicalite.plugins.zulrah.data.Rotation;
import net.unethicalite.plugins.zulrah.framework.ZulrahTask;

public class AttackZulrah extends ZulrahTask
{
	@Override
	public boolean validate()
	{
		boolean canAttack =
				NPCs.getNearest(x -> x.getName().contains(Constants.ZULRAH_NAME) && x.getHealthRatio() != 0) != null
						&& NPCs.getNearest(Constants.ZULRAH_NAME).getAnimation() != Constants.DISAPPEAR_ANIMATION
						&& NPCs.getNearest(Constants.ZULRAH_NAME).getAnimation() != Constants.APPEAR_ANIMATION;

		return (getRotation() != null
				&& Players.getLocal().getInteracting() == null
				&& (getRotation().getSafeSpot(getOrigin()).distanceTo(Players.getLocal()) <= 8
				|| getRotation().equals(Rotation.TANZ_SOUTH_CW))
				&& !inCloud()
				&& canAttack)
				&& attackCondition();
	}

	@Override
	public int execute()
	{
		NPC zulrah = NPCs.getNearest(x -> x.getName().equals(Constants.ZULRAH_NAME) && x.getHealthRatio() != 0);
		if (zulrah != null)
		{
			zulrah.interact("Attack");
		}

		return 555;
	}

	private boolean attackCondition()
	{
		return getRotation() != Rotation.GREEN_SOUTH_E || !Players.getLocal().isMoving();
	}
}
