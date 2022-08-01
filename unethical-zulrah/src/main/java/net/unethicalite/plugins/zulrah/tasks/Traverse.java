package net.unethicalite.plugins.zulrah.tasks;

import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.plugins.zulrah.data.Rotation;
import net.unethicalite.plugins.zulrah.framework.ZulrahTask;

public class Traverse extends ZulrahTask
{
	@Override
	public boolean validate()
	{
		return getRotation() != null
				&& (!Players.getLocal().isMoving() || Players.getLocal().getInteracting() != null)
				&& !isCloud(getRotation().getSafeSpot(getOrigin()))
				&& (!getRotation().getSafeSpot(getOrigin()).equals(Players.getLocal().getWorldLocation())
				&& getRotation() != Rotation.MAGMA_CENTER_NW
				&& getRotation() != Rotation.MAGMA_CENTER_NE)
				&& !Dialog.isOpen();
	}

	@Override
	public int execute()
	{
		Player local = Players.getLocal();
		WorldPoint westPillar = getOrigin().dx(-3).dy(3);
		WorldPoint eastPillar = getOrigin().dx(3).dy(3);

		if (getRotation().isCenter() && !Players.getLocal().getWorldLocation().equals(getRotation().getSafeSpot(getOrigin())))
		{
			if (westPillar.distanceTo(local) > eastPillar.distanceTo(local))
			{
				Movement.walkTo(getRotation().getSafeSpot(getOrigin()));
				return 1200;
			}

			Movement.walk(getRotation().getSafeSpot(getOrigin()));
			return 1200;
		}

		Movement.walk(getRotation().getSafeSpot(getOrigin()));
		return 500;
	}

	@Override
	public boolean isBlocking()
	{
		return false;
	}
}
