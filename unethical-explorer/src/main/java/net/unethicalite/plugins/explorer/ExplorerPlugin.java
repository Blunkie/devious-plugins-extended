package net.unethicalite.plugins.explorer;

import net.unethicalite.api.entities.Players;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.utils.CoordUtils;
import net.unethicalite.api.widgets.Widgets;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Extension
@PluginDescriptor(
		name = "Unethical Explorer",
		description = "Right click anywhere within the World Map to walk there",
		enabledByDefault = false
)
@Singleton
@Slf4j
public class ExplorerPlugin extends LoopedPlugin
{
	@Inject
	private Client client;

	private WorldPoint destination;

	@Override
	public void shutDown()
	{
		destination = null;
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (destination != null)
		{
			client.createMenuEntry(1)
					.setOption("<col=00ff00>Explorer:</col>")
					.setTarget("Cancel walking")
					.setType(MenuAction.RUNELITE)
					.onClick(e -> destination = null);
			return;
		}

		Widget worldMap = Widgets.get(WidgetInfo.WORLD_MAP_VIEW);
		if (worldMap == null)
		{
			return;
		}

		Point mouse = client.getMouseCanvasPosition();
		if (!worldMap.getBounds().contains(mouse.getX(), mouse.getY()))
		{
			return;
		}

		client.createMenuEntry(1)
				.setOption("<col=00ff00>Explorer:</col>")
				.setTarget("Walk here")
				.setType(MenuAction.RUNELITE)
				.onClick(e -> setDestination(CoordUtils.worldMapToWorldPoint(mouse)));
	}

	private void setDestination(WorldPoint wp)
	{
		destination = Walker.nearestWalkableTile(wp);
		log.debug("Walking to {}", destination);
	}

	@Override
	protected int loop() {
		if (Movement.isWalking())
		{
			return -1;
		}

		if (destination == null
				|| destination.distanceTo(Players.getLocal().getWorldLocation()) <= 2
				|| Objects.equals(Movement.getDestination(), destination)
		)
		{
			destination = null;
			return -1;
		}

		Movement.walkTo(destination);
		return -1;
	}
}
