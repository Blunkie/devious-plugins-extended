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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Extension
@PluginDescriptor(name = "Hoot Karambwan Fisher", enabledByDefault = false)
@Slf4j
public class HootKarambwanFisherPlugin extends LoopedPlugin
{
	private boolean needBait = false;
	private static final WorldArea KARAMBWANJI_AREA = new WorldArea(2780, 3002, 36, 28, 0);
	private static final WorldArea ZANARIS_BANK = new WorldArea(2381, 4455, 9, 7, 0);
	private static final WorldArea KARAMBWAN_AREA = new WorldArea(2894, 3108, 25, 16, 0);
	private static final String RAW_KARAMBWAN = "Raw karambwan";
	private static final String BAIT = "Raw karambwanji";
	private static final String NET = "Small fishing net";
	private static final String VESSEL = "Karambwan vessel";
	private static final String STAFF = "Dramen staff";

	@Override
	protected int loop()
	{
		Player local = Players.getLocal();
		int bait = Inventory.getCount(true, BAIT);

		if (Movement.isWalking())
		{
			return -1;
		}

		if (needBait)
		{
			if (bait > 3_000)
			{
				needBait = false;
				return -1;
			}

			if (!Inventory.contains(NET) || Inventory.isFull())
			{
				return fetchFromBank(
						getInvSetup(List.of(NET, BAIT, STAFF)),
						List.of(),
						Map.of(NET, 1, STAFF, 1)
				);
			}

			if (KARAMBWANJI_AREA.contains(local))
			{
				if (local.isAnimating() || local.getInteracting() != null)
				{
					log.debug("We are fishing karambwanji");
					return 1000;
				}

				NPC fishingSpot = NPCs.getNearest(f -> f.hasAction("Net") || KARAMBWANJI_AREA.contains(f));
				if (fishingSpot != null)
				{
					if (fishingSpot.distanceTo(local) > 5)
					{
						Movement.walkTo(fishingSpot);
						return -2;
					}

					log.debug("Netting fishing spot");
					fishingSpot.interact("Net");
					return -3;
				}
				else
				{
					log.debug("Cant find fishing spot, walking to area");
					Movement.walkTo(2809, 3009, 0);
				}

				return 1000;
			}



			log.debug("Moving to karambwanji area");
			Movement.walkTo(2809, 3009, 0);
			return 1000;
		}

		if (Inventory.isFull() || bait < 50 || !Inventory.contains(VESSEL) || !haveDramenStaff())
		{
			if (Bank.isOpen() && bait < 50)
			{
				if (Bank.getCount(true, BAIT) < 50)
				{
					needBait = true;
					return 1000;
				}

				return withdrawItem(BAIT, Integer.MAX_VALUE);
			}

			return fetchFromBank(
					getInvSetup(List.of(BAIT, VESSEL, RAW_KARAMBWAN, STAFF)),
					List.of(RAW_KARAMBWAN),
					Map.of(VESSEL, 1, STAFF, 1, BAIT, Integer.MAX_VALUE)
			);
		}

		if (KARAMBWAN_AREA.contains(local))
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

		Movement.walkTo(KARAMBWAN_AREA);
		return 1000;
	}

	private static int fetchFromBank(
			Predicate<Item> invSetup,
			List<String> depositItems,
			Map<String, Integer> withdrawItems
	)
	{
		Player local = Players.getLocal();
		if (!Bank.isOpen())
		{
			Item dramenStaff = Inventory.getFirst(STAFF);
			if (dramenStaff != null)
			{
				dramenStaff.interact("Wield");
				return 1000;
			}

			NPC banker = NPCs.query().actions("Collect").results().nearest();
			if (banker == null || !ZANARIS_BANK.contains(local))
			{
				Movement.walkTo(ZANARIS_BANK.getCenter());
				return 1500;
			}

			banker.interact("Bank");
			return -2;
		}

		if (Inventory.contains(invSetup.negate()))
		{
			Bank.depositInventory();
			return -3;
		}

		for (String item : depositItems)
		{
			if (!Inventory.contains(item))
			{
				continue;
			}

			Bank.depositAll(item);
			return -2;
		}

		for (var entry : withdrawItems.entrySet())
		{
			if (Equipment.contains(entry.getKey()) || Inventory.contains(entry.getKey()))
			{
				continue;
			}

			log.debug("Withdrawing {} {}", entry.getKey(), entry.getValue());

			return withdrawItem(entry.getKey(), entry.getValue());
		}

		return 1337;
	}

	private static Predicate<Item> getInvSetup(List<String> items)
	{
		return x -> items.contains(x.getName());
	}

	private static boolean haveDramenStaff()
	{
		return Inventory.contains(STAFF) || Equipment.contains(STAFF);
	}

	private static int withdrawItem(String name, int amount)
	{
		if (!Bank.contains(name))
		{
			log.error("{} not found in bank", name);
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
