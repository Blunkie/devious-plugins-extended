package dev.hoot.pickpocket;

import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.unethicalite.api.commons.Rand;
import dev.unethicalite.api.entities.NPCs;
import dev.unethicalite.api.entities.Players;
import dev.unethicalite.api.game.Combat;
import dev.unethicalite.api.items.Bank;
import dev.unethicalite.api.items.Inventory;
import dev.unethicalite.api.movement.Movement;
import dev.unethicalite.api.movement.Reachable;
import dev.unethicalite.api.movement.pathfinder.BankLocation;
import dev.unethicalite.api.plugins.LoopedPlugin;
import dev.unethicalite.api.widgets.Dialog;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@PluginDescriptor(name = "Hoot Pickpocket", enabledByDefault = false)
@Extension
@Slf4j
public class HootPickpocketPlugin extends LoopedPlugin
{
	@Inject
	private HootPickpocketConfig config;

	private WorldPoint lastNpcPosition = null;

	@Override
	protected int loop()
	{
		Item jug = Inventory.getFirst("Jug");
		if (jug != null && config.foodId() == ItemID.JUG_OF_WINE)
		{
			jug.interact("Drop");
			return -1;
		}

		Item pouch = Inventory.getFirst("Coin pouch");
		if (pouch != null && pouch.getQuantity() > 5)
		{
			pouch.interact("Open-all");
			return -1;
		}

		if (config.eat())
		{
			if (config.eatHp() >= Combat.getMissingHealth())
			{
				Item food = Inventory.getFirst(config.foodId());
				if (food != null)
				{
					food.interact(0);
					return -1;
				}

				if (Bank.isOpen())
				{
					Bank.withdraw(config.foodId(), 10, Bank.WithdrawMode.ITEM);
					return -1;
				}

				if (Movement.isWalking())
				{
					return -4;
				}

				NPC banker = NPCs.getNearest("Banker");
				if (banker != null)
				{
					banker.interact("Bank");
					return -1;
				}

				Movement.walkTo(BankLocation.getNearest());
				return -4;
			}
		}

		NPC target = NPCs.getNearest(config.npcName());
		if (target != null)
		{
			lastNpcPosition = target.getWorldLocation();
			if (!Reachable.isInteractable(target))
			{
				if (Movement.isWalking())
				{
					return -4;
				}

				Movement.walkTo(target);
				return -4;
			}

			Player local = Players.getLocal();
			if (local.getGraphic() == 245 && !Dialog.isOpen())
			{
				return -1;
			}

			target.interact("Pickpocket");
			return Rand.nextInt(222, 333);
		}

		if (Movement.isWalking())
		{
			return -4;
		}

		if (lastNpcPosition != null)
		{
			Movement.walkTo(lastNpcPosition);
			return -4;
		}

		log.info("Idle");
		return -1;
	}

	@Provides
	HootPickpocketConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootPickpocketConfig.class);
	}
}
