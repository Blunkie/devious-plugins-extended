package dev.hoot.agility;

import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.hoot.api.commons.Rand;
import dev.hoot.api.entities.Players;
import dev.hoot.api.entities.TileItems;
import dev.hoot.api.entities.TileObjects;
import dev.hoot.api.movement.Movement;
import dev.hoot.api.plugins.LoopedPlugin;
import dev.hoot.api.widgets.Dialog;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@PluginDescriptor(
		name = "Hoot Agility",
		enabledByDefault = false
)
@Slf4j
@Extension
public class HootAgilityPlugin extends LoopedPlugin
{
	@Inject
	private HootAgilityConfig hootAgilityConfig;

	@Inject
	private Client client;
	private Course course;

	@Override
	public void startUp()
	{
		if (course == Course.NEAREST)
		{
			course = Course.getNearest();
		}
		else
		{
			course = hootAgilityConfig.course();
		}
	}

	@Provides
	public HootAgilityConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootAgilityConfig.class);
	}

	@Override
	protected int loop()
	 {
		if (Dialog.canContinue())
		{
			Dialog.continueSpace();
			return -1;
		}

		Player local = Players.getLocal();

		Obstacle obstacle = course.getNext(local);
		if (obstacle == null)
		{
			log.error("No obstacle detected");
			return -1;
		}

		TileObject obs = obstacle.getId() != 0 ? TileObjects.getNearest(obstacle.getId())
				: TileObjects.getNearest(x -> x.hasAction(obstacle.getAction()) && x.getName().equals(obstacle.getName()));

		if (client.getEnergy() > Rand.nextInt(5, 55) && !Movement.isRunEnabled())
		{
			Movement.toggleRun();
			return -1;
		}

		TileItem mark = TileItems.getNearest("Mark of grace");
		if (mark != null && obstacle.getArea().contains(mark.getTile()))
		{
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
}
