package net.unethicalite.scripts.kebabs.tasks;

import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Dialog;
import net.runelite.api.DialogOption;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;

public class BuyKebabs implements ScriptTask
{
	@Override
	public boolean validate()
	{
		return !Inventory.isFull() && Inventory.contains(ItemID.COINS_995);
	}

	@Override
	public int execute()
	{
		if (!Movement.isRunEnabled())
		{
			Movement.toggleRun();
			return 1000;
		}

		if (Movement.isWalking())
		{
			return 1000;
		}

		NPC karim = NPCs.getNearest("Karim");
		if (karim == null)
		{
			Movement.walkTo(3274, 3181, 0);
			return 1000;
		}

		if (!Reachable.isInteractable(karim))
		{
			Movement.walkTo(karim);
			return 1000;
		}

		Dialog.invokeDialog(
				DialogOption.NPC_CONTINUE,
				DialogOption.CHAT_OPTION_TWO,
				DialogOption.PLAYER_CONTINUE
		);

		karim.interact("Talk-to");
		return 300;
	}
}
