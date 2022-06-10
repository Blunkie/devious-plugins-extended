package dev.hoot.plugins.karambwanfisher;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldArea;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(name = "Hoot Karambwan Fisher", enabledByDefault = false)
@Slf4j
public class HootKarambwanFisherPlugin extends LoopedPlugin
{
	private final WorldArea zanarisBank = new WorldArea(2381, 4455, 9, 7, 0);
	private final WorldArea karambwanArea = new WorldArea(2894, 3108, 25, 16, 0);

	@Override
	protected int loop()
	{
		int bait = Inventory.getCount(true, "Raw karambwanji");
		Player local = Players.getLocal();
		if (Inventory.isFull() || bait < 50 || !Inventory.contains("Karambwan vessel") || !haveDramenStaff())
		{
			if (Bank.isOpen())
			{
				if (Inventory.isFull())
				{
					Bank.depositInventory();
					return -3;
				}

				if (bait < 50)
				{
					log.debug("Looking for bait");
					return withdrawItem("Raw karambwanji", Integer.MAX_VALUE);
				}

				if (!Inventory.contains("Karambwan vessel"))
				{
					log.debug("Looking for karambwan vessel");
					return withdrawItem("Karambwan vessel", 1);
				}

				if (!haveDramenStaff())
				{
					log.debug("Looking for dramen staff");
					return withdrawItem("Dramen staff", 1);
				}

				return -1;
			}

			Item dramenStaff = Inventory.getFirst("Dramen staff");
			if (dramenStaff != null)
			{
				dramenStaff.interact("Wield");
				return 1000;
			}

			if (zanarisBank.contains(local))
			{
				NPCs.query().actions("Collect").results().nearest().interact("Bank");
				return 1500;
			}

			Movement.walkTo(zanarisBank);
			return 1000;
		}

		if (karambwanArea.contains(local))
		{
			NPC fishingSpot = NPCs.query().actions("Fish").results().nearest();
			if (fishingSpot == null)
			{
				log.warn("Fishing spot not found");
				return 1000;
			}

			if (local.isAnimating() || local.getInteracting() != null)
			{
				log.debug("We are fishing");
				return 1000;
			}

			fishingSpot.interact("Fish");
			return 1000;
		}

		Movement.walkTo(karambwanArea);
		return 1000;
	}

	private boolean haveDramenStaff()
	{
		return Inventory.contains("Dramen staff") || Equipment.contains("Dramen staff");
	}

	private int withdrawItem(String name, int amount)
	{
		if (!Bank.contains(name))
		{
			System.out.println(name + " not found in bank");
			return 1000;
		}

		if (amount == Integer.MAX_VALUE)
		{
			Bank.withdrawAll(name, Bank.WithdrawMode.ITEM);
		}
		else
		{
			Bank.withdraw(name, amount, Bank.WithdrawMode.ITEM);
		}

		return -3;
	}
}
