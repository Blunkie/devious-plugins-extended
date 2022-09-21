package net.unethicalite.scripts.kebabs.tasks;

import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;

public class BankKebabs implements ScriptTask
{
	private static final WorldPoint BANK_TILE = new WorldPoint(3268, 3167, 0);

	@Override
	public boolean validate()
	{
		return Inventory.isFull() || !Inventory.contains(ItemID.COINS_995);
	}

	@Override
	public int execute()
	{
		Player local = Players.getLocal();
		if (!Bank.isOpen())
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

			TileObject booth = TileObjects.getFirstAt(BANK_TILE, x -> x.hasAction("Bank", "Collect"));
			if (booth == null || booth.distanceTo(local) > 20 || !Reachable.isInteractable(booth))
			{
				Movement.walkTo(BANK_TILE);
				return 1000;
			}

			booth.interact("Bank");
			return 3000;
		}

		Item gold = Inventory.getFirst(ItemID.COINS_995);
		if (gold == null || gold.getQuantity() < 1000)
		{
			Bank.withdraw(ItemID.COINS_995, 5000, Bank.WithdrawMode.ITEM);
			return 1000;
		}

		if (Inventory.contains(ItemID.KEBAB))
		{
			Bank.depositAll(ItemID.KEBAB);
			return 1000;
		}

		return 1000;
	}
}
