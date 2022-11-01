package net.unethicalite.pickpocket;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.widgets.Dialog;
import org.pf4j.Extension;

@PluginDescriptor(name = "Hoot Pickpocket", enabledByDefault = false)
@Extension
@Slf4j
public class HootPickpocketPlugin extends LoopedPlugin
{
	@Inject
	private HootPickpocketConfig config;

	private WorldPoint lastNpcPosition = null;

	private int maxPouches = 5;

	@Override
	protected int loop()
	{
		Item jug = Inventory.getFirst("Jug");
		if (jug != null && config.foodId() == ItemID.JUG_OF_WINE)
		{
			jug.interact("Drop");
			log.debug("Dropping jug");
			return -1;
		}

		Item pouch = Inventory.getFirst("Coin pouch");
		if (pouch != null && pouch.getQuantity() >= maxPouches)
		{
			pouch.interact("Open-all");
			maxPouches = Rand.nextInt(1, 29);
			log.debug("Opening pouches");
			return -1;
		}

		if (config.eat())
		{
			if (Combat.getMissingHealth() >= config.eatHp())
			{
				Item food = Inventory.getFirst(config.foodId());
				if (food != null)
				{
					food.interact(1);
					log.debug("Eating food");
					return -1;
				}

				if (Bank.isOpen())
				{
					Bank.withdraw(config.foodId(), 10, Bank.WithdrawMode.ITEM);
					log.debug("Withdrawing food");
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

			if (local.isMoving() && target.distanceTo(local) > 3)
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
