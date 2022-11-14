package net.unethicalite.plugins.agility;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

import javax.swing.SwingUtilities;

import static net.runelite.api.ItemID.HALF_A_SUMMER_PIE;
import static net.runelite.api.ItemID.SUMMER_PIE;

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

	private int energyAmount;

	private int alchCooldown = 0;

	private boolean justAlched = false;

	@Provides
	public UnethicalAgilityConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalAgilityConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		energyAmount = Rand.nextInt(config.minEnergyAmount(), config.maxEnergyAmount());
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (alchCooldown > 0)
		{
			alchCooldown--;
		}
	}

	@Override
	protected int loop()
	{
		if (Dialog.canContinue())
		{
			Dialog.continueSpace();
			return -1;
		}

		if (!Movement.isRunEnabled() && Static.getClient().getEnergy() > 5)
		{
			Movement.toggleRun();
			return -1;
		}

		if (config.useSummerPies() && config.stopWhenOutOfSummerPies()
				&& !Inventory.contains(SUMMER_PIE, HALF_A_SUMMER_PIE))
		{
			MessageUtils.addMessage("Ran out of Summer Pies, stopping plugin");
			SwingUtilities.invokeLater(() -> Plugins.stopPlugin(this));
			return -1;
		}

		if (config.useSummerPies() && Inventory.contains(SUMMER_PIE, ItemID.HALF_A_SUMMER_PIE)
				&& Skills.getBoostedLevel(Skill.AGILITY) - Skills.getLevel(Skill.AGILITY) < config.minBoostAmount())
		{
			var summerPie = Inventory.getFirst(SUMMER_PIE, ItemID.HALF_A_SUMMER_PIE);

			summerPie.interact("Eat");
			return -1;
		}

		if (config.useStaminas() && client.getEnergy() < energyAmount && Vars.getBit(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0
				&& Inventory.contains(item -> item.getName().contains("Stamina")))
		{
			Inventory.getFirst(item -> item.getName().contains("Stamina")).interact("Drink");
			energyAmount = Rand.nextInt(config.minEnergyAmount(), config.maxEnergyAmount());
			return -1;
		}

		if (Combat.getHealthPercent() <= config.eatHp())
		{
			var itemToEat = Inventory.query().actions("Eat").results().first();
			if (itemToEat == null)
			{
				if (config.stopIfNoFood())
				{
					MessageUtils.addMessage("Ran out of food, stopping plugin");
					SwingUtilities.invokeLater(() -> Plugins.stopPlugin(this));
				}

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

		if (config.shouldAlch() && alchCooldown == 0 && SpellBook.Standard.HIGH_LEVEL_ALCHEMY.haveRunesAvailable())
		{
			// if its been 5 ticks since last alch
			// if we have item and nature runes
			Item alchItem = Inventory.query().ids(config.itemToAlch()).results().first();
			if (alchItem != null)
			{
				Magic.cast(SpellBook.Standard.HIGH_LEVEL_ALCHEMY, alchItem);
				alchCooldown = 5;
				justAlched = true;
				return -1;
			}
		}
		TileObject obs = findProperObstacle(obstacle);

		if (Movement.getRunEnergy() > Rand.nextInt(5, 55) && !Movement.isRunEnabled())
		{
			Movement.toggleRun();
			return -1;
		}

		TileItem mark = TileItems.getFirstSurrounding(Players.getLocal().getWorldLocation(), 10, "Mark of grace");
		if (mark != null && obstacle.getArea().contains(mark.getTile()) && !Inventory.isFull())
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
			if (justAlched)
			{
				obs.interact(obstacle.getAction());
				justAlched = false;
				return -1;
			}

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
			return TileObjects.getFirstSurrounding(obstacle.getLocation().getX(), obstacle.getLocation().getY(),
					obstacle.getLocation().getZ(), 3, obstacle.getName());
		}
		catch (Exception exception)
		{
			return obstacle.getId() != 0 ? TileObjects.getNearest(obstacle.getId())
					: TileObjects.getNearest(x -> x.hasAction(obstacle.getAction()) && x.getName().equals(obstacle.getName()));
		}
	}
}
