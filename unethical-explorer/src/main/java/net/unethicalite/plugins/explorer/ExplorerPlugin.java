package net.unethicalite.plugins.explorer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.regex.Pattern;

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
	private static final Pattern WORLD_POINT_PATTERN = Pattern.compile("^\\d{4,5} \\d{4,5} \\d$");

	@Inject
	private Client client;

	@Inject
	private ExplorerConfig config;

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

		WorldPoint mouse = client.getRenderOverview().getMouseLocation();
		if (mouse == null)
		{
			return;
		}

		client.createMenuEntry(1)
				.setOption("<col=00ff00>Explorer:</col>")
				.setTarget("Walk here")
				.setType(MenuAction.RUNELITE)
				.onClick(e -> setDestination(mouse));
	}

	@Subscribe
	public void onConfigButtonClicked(ConfigButtonClicked e)
	{
		if (!"unethicalexplorer".equals(e.getGroup()) || !"walk".equals(e.getKey()))
		{
			return;
		}

		String coords = config.coords();
		if (!WORLD_POINT_PATTERN.matcher(coords).matches())
		{
			return;
		}

		String[] split = coords.split(" ");
		setDestination(Walker.nearestWalkableTile(
				new WorldPoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]))
		));
	}

	private void setDestination(WorldPoint wp)
	{
		destination = Walker.nearestWalkableTile(wp);
		log.debug("Walking to {}", destination);
	}

	@Override
	protected int loop()
	{
		if (!Game.isLoggedIn() || client.getLocalPlayer() == null)
		{
			return -1;
		}

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

	@Provides
	ExplorerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExplorerConfig.class);
	}
}
