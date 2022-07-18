package net.unethicalite.plugins.agility;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.widgets.Dialog;
import org.pf4j.Extension;

@PluginDescriptor(
		name = "Unethical Agility",
		enabledByDefault = false
)
@Slf4j
@Extension
public class UnethicalAgilityPlugin extends LoopedPlugin
{
	@Inject
	private UnethicalAgilityConfig config;

	@Inject
	private Client client;

	@Provides
	public UnethicalAgilityConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalAgilityConfig.class);
	}

	@Override
	protected int loop()
	{
		if (Dialog.canContinue())
		{
			Dialog.continueSpace();
			return -1;
		}

		if (Combat.getHealthPercent() <= config.eatHp())
		{
			var itemToEat = Inventory.query().actions("Eat").results().first();
			if (itemToEat == null)
			{
				log.error("Ran out of food");
				Plugins.stopPlugin(this);
				return -1;
			}

			itemToEat.interact("Eat");
			return -1;
		}

		Course course = config.course() == Course.NEAREST ? Course.getNearest() : config.course();
		Player local = Players.getLocal();
		Obstacle obstacle = course.getNext(local);
		if (obstacle == null)
		{
			log.error("No obstacle detected");
			return -1;
		}

		TileObject obs = findProperObstacle(obstacle);

		if (Movement.getRunEnergy() > Rand.nextInt(5, 55) && !Movement.isRunEnabled())
		{
			Movement.toggleRun();
			return -1;
		}

		TileItem mark = TileItems.getFirstSurrounding(Players.getLocal().getWorldLocation(), 10, "Mark of grace");
		if (mark != null && obstacle.getArea().contains(mark.getTile()))
		{
			TileItem gold = TileItems.getFirstAt(mark.getWorldLocation(), ItemID.COINS_995);
			if (gold != null)
			{
				gold.pickup();
				return -1;
			}

			mark.pickup();
			return -1;
		}

		if (obs != null)
		{
			if (local.getAnimation() != -1 || local.isMoving())
			{
				return -1;
			}

			obs.interact(obstacle.getAction());
			return -1;
		}

		log.error("Obstacle was null");
		return -1;
	}

	public TileObject findProperObstacle(Obstacle obstacle)
	{
		try
		{
			return TileObjects.getFirstAt(obstacle.getLocation().getX(), obstacle.getLocation().getY(), obstacle.getLocation().getZ(), obstacle.getName());
		}
		catch (Exception exception)
		{
			return obstacle.getId() != 0 ? TileObjects.getNearest(obstacle.getId())
					: TileObjects.getNearest(x -> x.hasAction(obstacle.getAction()) && x.getName().equals(obstacle.getName()));
		}
	}
}
