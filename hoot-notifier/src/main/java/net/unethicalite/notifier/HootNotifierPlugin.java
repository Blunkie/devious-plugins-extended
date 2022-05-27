package net.unethicalite.notifier;

import com.google.inject.Provides;
import net.unethicalite.api.coords.ScenePoint;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.scene.Tiles;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.TileObject;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(name = "Hoot Notifier")
@Slf4j
public class HootNotifierPlugin extends Plugin
{
	private static final Set<MenuAction> GAME_OBJECT_OPCODES = Set.of(
			MenuAction.GAME_OBJECT_FIRST_OPTION,
			MenuAction.GAME_OBJECT_SECOND_OPTION,
			MenuAction.GAME_OBJECT_THIRD_OPTION,
			MenuAction.GAME_OBJECT_FOURTH_OPTION,
			MenuAction.GAME_OBJECT_FIFTH_OPTION,
			MenuAction.ITEM_USE_ON_GAME_OBJECT,
			MenuAction.WIDGET_TARGET_ON_GAME_OBJECT
	);

	@Inject
	private HootNotifierConfig config;

	@Inject
	private Client client;

	@Inject
	private Notifier notifier;

	private long currentObjectTag = -1;
	private long currentNpcTag = -1;

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (currentObjectTag == -1)
		{
			return;
		}

		if (currentObjectTag == event.getGameObject().getTag())
		{
			notifier.notify("Idle");
			currentObjectTag = -1;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (GAME_OBJECT_OPCODES.contains(event.getMenuAction()))
		{
			List<Integer> ids = Arrays.stream(config.objectIds().split(","))
					.mapToInt(Integer::parseInt)
					.boxed()
					.collect(Collectors.toList());
			int id = event.getId();
			if (!ids.contains(id))
			{
				return;
			}

			ScenePoint scene = new ScenePoint(event.getParam0(), event.getParam1(), client.getPlane());
			TileObject object = TileObjects.getFirstAt(Tiles.getAt(scene), id);
			if (object != null)
			{
				currentObjectTag = object.getTag();
			}
		}
	}

	@Provides
	HootNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootNotifierConfig.class);
	}
}
