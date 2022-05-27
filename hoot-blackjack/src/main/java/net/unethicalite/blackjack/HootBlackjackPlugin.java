package net.unethicalite.blackjack;

import com.google.inject.Provides;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Shop;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.LoopedPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.World;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Comparator;

@Extension
@PluginDescriptor(name = "Hoot Blackjack", enabledByDefault = false)
@Slf4j
public class HootBlackjackPlugin extends LoopedPlugin
{
	@Inject
	private HootBlackjackConfig config;

	@Override
	protected int loop()
	{
		BlackjackSpot spot = config.blackjackSpot();
		Item pouch = Inventory.getFirst("Coin pouch");
		Player local = Players.getLocal();
		NPC target = NPCs.getNearest(x ->
				x.getName() != null &&
						x.getName().equals(spot.getNpcName()) &&
						x.hasAction("Knock-Out") &&
						spot.getArea().contains(x)
		);
		if (target != null && ((target.getInteracting() != null && target.getInteracting() == local)
				|| (target.getOverheadText() != null && target.getOverheadText().startsWith("I'll kill you for that"))))
		{
			log.debug("Pickpocketing");
			target.interact("Pickpocket");
			return 222;
		}

		if (pouch != null && pouch.getQuantity() > 5)
		{
			log.debug("Opening pouches");
			pouch.interact("Open-all");
			return -1;
		}

		Item jug = Inventory.getFirst("Jug");
		if (jug != null)
		{
			log.debug("Dropping jug");
			jug.interact("Drop");
			return -1;
		}

		Item food = Inventory.getFirst(config.foodId());
		if (food != null)
		{
			if (spot.getArea().contains(local))
			{
				Player otherPlayer = Players.getNearest(x -> !x.equals(local)
						&& spot.getArea().contains(x)
						&& (x.isAnimating() || x.getGraphic() == 245)
				);
				if (target == null)
				{
					log.debug("Unable to find target");
					Worlds.hopTo(Worlds.getRandom(World::isNormal));
					return -3;
				}

				NPC otherNpc = NPCs.getNearest(x ->
						!x.equals(target) &&
								x.getName() != null && x.getName().equals(spot.getNpcName()) &&
								x.hasAction("Knock-Out") &&
								spot.getArea().contains(x)
				);
				if (otherPlayer != null || otherNpc != null)
				{
					log.debug("Other player/npc present, hopping");
					Worlds.hopTo(Worlds.getRandom(World::isNormal));
					return -3;
				}

				if (Combat.getMissingHealth() >= config.eatHp())
				{
					log.debug("Eating food");
					food.interact(1);
					return -1;
				}

				if (local.getGraphic() == 245)
				{
					log.debug("We are stunned");
					return -1;
				}

				TileObject curtain = TileObjects.within(spot.getArea().offset(1), x -> x.hasAction("Close"))
						.stream().min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(local.getWorldLocation())))
						.orElse(null);
				if (curtain != null)
				{
					log.debug("Closing curtain");
					curtain.interact("Close");
					return -1;
				}

				if (target.getOverheadText() != null && target.getOverheadText().contains("Zzz"))
				{
					log.debug("Pickpocketing");
					target.interact("Pickpocket");
					return -2;
				}

				log.debug("Knocking out");
				target.interact("Knock-Out");
				return 222;
			}

			log.debug("Walking to spot");
			Movement.walkTo(spot.getArea());
			return -1;
		}

		if (config.buyWines())
		{
			if (Movement.isWalking())
			{
				log.debug("On the way to wine shop");
				return -1;
			}

			if (Shop.isOpen())
			{
				log.debug("attempting to buy wine");
				Shop.buyFifty(config.foodId());
				return -1;
			}

			NPC shop = NPCs.getNearest("Ali the Barman");
			if (shop == null || !Reachable.isInteractable(shop))
			{
				log.debug("Walking to wine shop");
				Movement.walkTo(3359, 2958, 0);
				return -1;
			}

			log.debug("Trading with wine npc");
			shop.interact("Trade");
			return -1;
		}

		log.info("We are idle");
		return -1;
	}

	@Provides
	HootBlackjackConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootBlackjackConfig.class);
	}
}
