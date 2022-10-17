package net.unethicalite.plugins.explorer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.HotkeyListener;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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

	@Inject
	private KeyManager keyManager;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Override
	protected void startUp()
	{
		keyManager.registerKeyListener(hotkeyListener);
	}

	@Override
	public void shutDown()
	{
		destination = null;
		keyManager.unregisterKeyListener(hotkeyListener);

	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggleKeyBind())
	{
		@Override
		public void hotkeyPressed()
		{
			// If the hotkey is pressed and there is currently a destination, stop walking
			if (destination != null)
			{
				destination = null;
			}
			else
			{
				WorldPoint location = null;

				switch (config.category())
				{
					case QUEST:
						WorldPoint questLocation = getWorldPointLocation("Quest Helper");
						if (questLocation != null)
						{
							location = questLocation;
						}
						break;
					case CLUE:
						WorldPoint clueLocation = getWorldPointLocation("Clue Scroll");
						if (clueLocation != null)
						{
							location = clueLocation;
						}
						break;
					case BANKS:
						location = config.bankLocation().getArea().getCenter();
						break;
					case CUSTOM:
						String coords = config.coords();
						if (!WORLD_POINT_PATTERN.matcher(coords).matches())
						{
							return;
						}
						String[] split = coords.split(" ");
						location = new WorldPoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
						break;
				}
				if (location != null)
				{
					setDestination(Movement.getNearestWalkableTile(location));
				}
				else
				{
					MessageUtils.addMessage("Invalid Selection");
				}
			}
		}
	};

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
				.onClick(e ->
				{
					setDestination(mouse);

					if (config.closeMap())
						closeWorldMap();
				});
	}

	@Subscribe
	public void onConfigButtonClicked(ConfigButtonClicked e)
	{
		if (!"unethicalexplorer".equals(e.getGroup()) || !"walk".equals(e.getKey()))
		{
			return;
		}

		WorldPoint location = null;

		switch (config.category())
		{
			case QUEST:
				WorldPoint questLocation = getWorldPointLocation("Quest Helper");
				if (questLocation != null)
				{
					location = questLocation;
				}
				break;
			case CLUE:
				WorldPoint clueLocation = getWorldPointLocation("Clue Scroll");
				if (clueLocation != null)
				{
					location = clueLocation;
				}
				break;
			case BANKS:
				location = config.bankLocation().getArea().getCenter();
				break;
			case CUSTOM:
				String coords = config.coords();
				if (!WORLD_POINT_PATTERN.matcher(coords).matches())
				{
					return;
				}
				String[] split = coords.split(" ");
				location = new WorldPoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
				break;
		}
		if (location != null)
		{
			setDestination(Movement.getNearestWalkableTile(location));
		}
		else
		{
			MessageUtils.addMessage("Invalid Selection");
		}
	}

	private void setDestination(WorldPoint wp)
	{
		destination = Movement.getNearestWalkableTile(wp);
		log.debug("Walking to {}", destination);
	}

	private void closeWorldMap()
	{
		Widget closeWorldMap = Widgets.get(WidgetID.WORLD_MAP_GROUP_ID, closeButton -> closeButton.hasAction("Close"));
		if (closeWorldMap != null && closeWorldMap.isVisible())
		{
			closeWorldMap.interact("Close");
		}
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

	private WorldPoint getWorldPointLocation(String name)
	{
		List<?> mapPoints = new ArrayList<>();
		try
		{
			Field privateField = worldMapPointManager.getClass().getDeclaredField("worldMapPoints");
			privateField.setAccessible(true);
			mapPoints = (List<?>) privateField.get(worldMapPointManager);
		}
		catch (Exception e)
		{
			log.info("Error: ", e);
		}

		for (Object mapPoint : mapPoints)
		{
			if (mapPoint instanceof WorldMapPoint)
			{
				final WorldMapPoint point = (WorldMapPoint) mapPoint;
				if (point.getName() != null && point.getName().equals(name))
				{
					return point.getWorldPoint();
				}
			}

		}
		return null;
	}

	@Provides
	ExplorerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExplorerConfig.class);
	}
}
