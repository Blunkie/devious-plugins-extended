package dev.hoot.oneclick;

import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.hoot.api.EntityNameable;
import dev.hoot.api.Interactable;
import dev.hoot.api.entities.NPCs;
import dev.hoot.api.entities.Players;
import dev.hoot.api.entities.TileItems;
import dev.hoot.api.entities.TileObjects;
import dev.hoot.api.game.Game;
import dev.hoot.api.items.Inventory;
import dev.hoot.api.widgets.Widgets;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PluginDescriptor(
		name = "Hoot One Click",
		description = "Allows you to One-Click interactions",
		enabledByDefault = false
)
@Slf4j
@Extension
public class HootOneClickPlugin extends Plugin
{
	@Inject
	private HootOneClickConfig config;

	@Inject
	private Client client;

	private static final List<Integer> GAME_OBJECT_OPCODES = List.of(1, 2, 3, 4, 5, 6, 1001, 1002);
	private static final List<Integer> NPC_OPCODES = List.of(7, 8, 9, 10, 11, 12, 13, 1003);
	private static final List<Integer> GROUND_ITEM_OPCODES = List.of(18, 19, 20, 21, 22, 1004);
	private static final List<Integer> WIDGET_OPCODES = List.of(24, 25, 26, 28, 29, 30, 39, 40, 41, 42, 43);
	private static final List<Integer> ITEM_OPCODES = List.of(33, 34, 35, 36, 37, 38, 1005);
	private static final List<Integer> PLAYER_OPCODES = List.of(44, 45, 46, 47, 48, 49, 50, 51);

	private final Map<String, String> gameObjectConfigs = new HashMap<>();
	private final Map<String, String> npcConfigs = new HashMap<>();
	private final Map<String, String> groundItemConfigs = new HashMap<>();
	private final Map<String, String> widgetConfigs = new HashMap<>();
	private final Map<String, String> itemConfigs = new HashMap<>();
	private final Map<String, String> playerConfigs = new HashMap<>();

	@Provides
	public HootOneClickConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootOneClickConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (!e.getGroup().equals("hootoneclick"))
		{
			return;
		}

		clearConfigs();

		parseConfigs(config.gameObjectConfig(), gameObjectConfigs);
		parseConfigs(config.npcConfig(), npcConfigs);
		parseConfigs(config.groundItemConfig(), groundItemConfigs);
		parseConfigs(config.widgetConfig(), widgetConfigs);
		parseConfigs(config.itemConfig(), itemConfigs);
		parseConfigs(config.playerConfig(), playerConfigs);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		int opcode = e.getType();

		if (!gameObjectConfigs.isEmpty() && GAME_OBJECT_OPCODES.contains(opcode))
		{
			Tile tile = Game.getClient().getScene().getTiles()[Game.getClient().getPlane()][e.getActionParam0()][e.getActionParam1()];
			TileObject obj = TileObjects.getFirstAt(tile, e.getIdentifier());
			if (replace(gameObjectConfigs, obj))
			{
//				return;
			}
		}

		if (!npcConfigs.isEmpty() && NPC_OPCODES.contains(opcode))
		{
			NPC npc = NPCs.getNearest(x -> x.getIndex() == e.getIdentifier());
			if (replace(npcConfigs, npc))
			{
//				return;
			}
		}

		if (!groundItemConfigs.isEmpty() && GROUND_ITEM_OPCODES.contains(opcode))
		{
			Tile tile = Game.getClient().getScene().getTiles()[Game.getClient().getPlane()][e.getActionParam0()][e.getActionParam1()];
			TileItem item = TileItems.getFirstAt(tile, e.getIdentifier());
			if (replace(groundItemConfigs, item))
			{
//				return;
			}
		}

		if (!itemConfigs.isEmpty() && ITEM_OPCODES.contains(opcode))
		{
			Item item = Inventory.getFirst(e.getIdentifier());
			if (replace(itemConfigs, item))
			{
//				return;
			}
		}

		if (!playerConfigs.isEmpty() && PLAYER_OPCODES.contains(opcode))
		{
			Player player = Players.getNearest(x -> x.getIndex() == e.getIdentifier());
			if (replace(playerConfigs, player))
			{
//				return;
			}
		}

		if (!widgetConfigs.isEmpty() && WIDGET_OPCODES.contains(opcode))
		{
			String action = Text.removeTags(e.getOption()) + " " + Text.removeTags(e.getTarget());
			Widget widget = Widgets.fromId(e.getActionParam1());
			if (widget != null && widgetConfigs.containsKey(action))
			{
				String replaced = widgetConfigs.get(action);

				if (isUseOn(replaced))
				{
					Item usedItem = getUsedItem(replaced);
					if (usedItem != null)
					{
						useOn(usedItem, widget);
					}

					return;
				}

				widget.getMenu(action).toEntry(client);
			}
		}
	}

	private <T extends Interactable> boolean replace(Map<String, String> replacements, T t)
	{
		if (!(t instanceof EntityNameable))
		{
			return false;
		}

		if ((!config.exactEntityNames() || !replacements.containsKey(((EntityNameable) t).getName()))
				&& replacements.keySet().stream().noneMatch(x -> ((EntityNameable) t).getName().toLowerCase().contains(x.toLowerCase())))
		{
			return false;
		}

		String replacement;
		if (config.exactEntityNames())
		{
			replacement = replacements.get(((EntityNameable) t).getName());
		}
		else
		{
			String key = replacements.keySet().stream()
					.filter(x -> ((EntityNameable) t).getName().toLowerCase().contains(x.toLowerCase()))
					.findFirst()
					.orElse(null);
			replacement = replacements.get(key);
		}

		if (replacement == null)
		{
			return false;
		}

		if (isUseOn(replacement))
		{
			Item usedItem = getUsedItem(replacement);
			if (usedItem != null)
			{
				useOn(usedItem, t);
			}

			return true;
		}

		t.getMenu(replacement).toEntry(client, "<col=00ff00>" + replacement, ((EntityNameable) t).getName(), null);
		return true;
	}

	private void useOn(Item item, Interactable target)
	{
		if (target instanceof TileItem)
		{
			target.getMenu(0, MenuAction.ITEM_USE_ON_GROUND_ITEM.getId()).toEntry(client,
					"<col=00ff00>" + item.getName() + " ->",
					((TileItem) target).getName(),
					x ->
			{
				client.setSelectedItemWidget(item.getWidgetId());
				client.setSelectedItemSlot(item.getSlot());
				client.setSelectedItemID(item.getId());
			});

			return;
		}

		if (target instanceof TileObject)
		{
			target.getMenu(0, MenuAction.ITEM_USE_ON_GAME_OBJECT.getId()).toEntry(client,
					"<col=00ff00>" + item.getName() + " ->",
					((TileObject) target).getName(),
					x ->
			{
				client.setSelectedItemWidget(item.getWidgetId());
				client.setSelectedItemSlot(item.getSlot());
				client.setSelectedItemID(item.getId());
			});

			return;
		}

		if (target instanceof Item)
		{
			item.getMenu(0, MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId()).toEntry(client,
					"<col=00ff00>" + item.getName() + " ->",
					((Item) target).getName(),
					x ->
			{
				client.setSelectedItemWidget(item.getWidgetId());
				client.setSelectedItemSlot(((Item) target).getSlot());
				client.setSelectedItemID(((Item) target).getId());
			});

			return;
		}

		if (target instanceof Actor)
		{
			MenuAction menuAction = target instanceof NPC ? MenuAction.ITEM_USE_ON_NPC : MenuAction.ITEM_USE_ON_PLAYER;
			target.getMenu(0, menuAction.getId()).toEntry(client,
					"<col=00ff00>" + item.getName() + " ->",
					((Actor) target).getName(),
					x ->
			{
				client.setSelectedItemWidget(item.getWidgetId());
				client.setSelectedItemSlot(item.getSlot());
				client.setSelectedItemID(item.getId());
			});

			return;
		}

		if (target instanceof Widget)
		{
			int widgetId = ((Widget) target).getId();
			target.getMenu(0, MenuAction.ITEM_USE_ON_WIDGET.getId()).toEntry(client,
					"<col=00ff00>" + item.getName() + " ->",
					"Widget[" + WidgetInfo.TO_GROUP(((Widget) target).getId()) + ", "
							+ WidgetInfo.TO_CHILD(widgetId) + "]",
					x ->
			{
				client.setSelectedItemWidget(item.getWidgetId());
				client.setSelectedItemSlot(item.getSlot());
				client.setSelectedItemID(item.getId());
			});
		}
	}

	private Item getUsedItem(String action)
	{
		return Inventory.getFirst(x ->
		{
			if (config.exactItemNames())
			{
				return x.getName().equals(action.substring(4));
			}

			return x.getName().toLowerCase().contains(action.substring(4).toLowerCase());
		});
	}

	private boolean isUseOn(String action)
	{
		return action.contains("Use ") && action.split(" ").length >= 2;
	}

	private void parseConfigs(String text, Map<String, String> configs)
	{
		if (text.isBlank())
		{
			return;
		}

		String[] items = text.split(",");

		for (String i : items)
		{
			String[] pairs = i.split(":");
			if (pairs.length < 2)
			{
				continue;
			}

			configs.put(pairs[0], pairs[1]);
		}
	}

	private boolean isConfigured(String entityName, Map<String, String> configs)
	{
		if (config.exactEntityNames())
		{
			return configs.containsKey(entityName);
		}

		return configs.keySet().stream().anyMatch(x -> entityName.toLowerCase().contains(x.toLowerCase()));
	}

	private void clearConfigs()
	{
		gameObjectConfigs.clear();
		npcConfigs.clear();
		groundItemConfigs.clear();
		widgetConfigs.clear();
		itemConfigs.clear();
		playerConfigs.clear();
	}
}
