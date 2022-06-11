package net.unethicalite.plugins.oneclick;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.EntityNameable;
import net.unethicalite.api.Interactable;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import org.pf4j.Extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PluginDescriptor(
		name = "Unethical One Click",
		description = "Allows you to One-Click interactions",
		enabledByDefault = false
)
@Slf4j
@Extension
public class UnethicalOneClickPlugin extends Plugin
{
	@Inject
	private UnethicalOneClickConfig config;

	@Inject
	private Client client;

	private static final String ONECLICK_MENUOPTION_PREFIX = "<col=00ff00>OC:</col> ";

	private static final List<Integer> GAME_OBJECT_OPCODES = List.of(1, 2, 3, 4, 5, 6, 1001, 1002);
	private static final List<Integer> NPC_OPCODES = List.of(7, 8, 9, 10, 11, 12, 13, 1003);
	private static final List<Integer> GROUND_ITEM_OPCODES = List.of(18, 19, 20, 21, 22, 1004);
	private static final List<Integer> ITEM_OPCODES = List.of(1007, 25, 57);
	private static final List<Integer> PLAYER_OPCODES = List.of(44, 45, 46, 47, 48, 49, 50, 51);

	private final Map<String, String> gameObjectConfigs = new HashMap<>();
	private final Map<String, String> npcConfigs = new HashMap<>();
	private final Map<String, String> groundItemConfigs = new HashMap<>();
	private final Map<String, String> itemConfigs = new HashMap<>();
	private final Map<String, String> playerConfigs = new HashMap<>();

	@Provides
	public UnethicalOneClickConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalOneClickConfig.class);
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
		parseConfigs(config.itemConfig(), itemConfigs);
		parseConfigs(config.playerConfig(), playerConfigs);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (e.getOption().startsWith(ONECLICK_MENUOPTION_PREFIX))
		{
			return;
		}

		int opcode = e.getType();

		if (!gameObjectConfigs.isEmpty() && GAME_OBJECT_OPCODES.contains(opcode))
		{
			Tile tile = client.getScene().getTiles()[client.getPlane()][e.getActionParam0()][e.getActionParam1()];
			TileObject obj = TileObjects.getFirstAt(tile, e.getIdentifier());
			MenuEntry replaced = replace(gameObjectConfigs, obj);
			if (replaced != null)
			{
				client.setMenuEntries(new MenuEntry[]{replaced});
				return;
			}
		}

		if (!npcConfigs.isEmpty() && NPC_OPCODES.contains(opcode))
		{
			NPC npc = NPCs.getNearest(x -> x.getIndex() == e.getIdentifier());
			MenuEntry replaced = replace(npcConfigs, npc);
			if (replaced != null)
			{
				client.setMenuEntries(new MenuEntry[]{replaced});
				return;
			}
		}

		if (!groundItemConfigs.isEmpty() && GROUND_ITEM_OPCODES.contains(opcode))
		{
			Tile tile = client.getScene().getTiles()[client.getPlane()][e.getActionParam0()][e.getActionParam1()];
			TileItem item = TileItems.getFirstAt(tile, e.getIdentifier());
			MenuEntry replaced = replace(groundItemConfigs, item);
			if (replaced != null)
			{
				client.setMenuEntries(new MenuEntry[]{replaced});
				return;
			}
		}

		if (!itemConfigs.isEmpty() && ITEM_OPCODES.contains(opcode))
		{
			Item item = Inventory.getItem(e.getActionParam0());
			MenuEntry replaced = replace(itemConfigs, item);
			if (replaced != null)
			{
				client.setMenuEntries(new MenuEntry[]{replaced});
				return;
			}
		}

		if (!playerConfigs.isEmpty() && PLAYER_OPCODES.contains(opcode))
		{
			Player player = Players.getNearest(x -> x.getIndex() == e.getIdentifier());
			MenuEntry replaced = replace(playerConfigs, player);
			if (replaced != null)
			{
				client.setMenuEntries(new MenuEntry[]{replaced});
			}
		}
	}

	private <T extends Interactable> MenuEntry replace(Map<String, String> replacements, T target)
	{
		if (!(target instanceof EntityNameable))
		{
			return null;
		}

		if ((!config.exactEntityNames() || !replacements.containsKey(((EntityNameable) target).getName()))
				&& replacements.keySet().stream().noneMatch(x -> ((EntityNameable) target).getName().toLowerCase().contains(x.toLowerCase())))
		{
			return null;
		}

		String replacement;
		if (config.exactEntityNames())
		{
			replacement = replacements.get(((EntityNameable) target).getName());
		}
		else
		{
			String key = replacements.keySet().stream()
					.filter(x -> ((EntityNameable) target).getName().toLowerCase().contains(x.toLowerCase()))
					.findFirst()
					.orElse(null);
			replacement = replacements.get(key);
		}

		if (replacement == null)
		{
			log.debug("Replacement was null for {}", target);
			return null;
		}

		if (isUseOn(replacement))
		{
			String itemName = replacement.substring(4);
			if (isId(itemName))
			{
				Item usedItem = Inventory.getFirst(Integer.parseInt(itemName));
				if (usedItem != null)
				{
					return useOn(usedItem, target).setForceLeftClick(true);
				}
			}
			else
			{
				Item usedItem = getUsedItem(replacement);
				if (usedItem != null)
				{
					return useOn(usedItem, target).setForceLeftClick(true);
				}
			}

			log.debug("Used item was null for replacement: {}", replacement);
			return null;
		}

		if (!target.hasAction(replacement))
		{
			return null;
		}

		return target.getMenu(replacement).toEntry(client, 0)
				.setOption(ONECLICK_MENUOPTION_PREFIX + replacement)
				.setTarget(((EntityNameable) target).getName())
				.setForceLeftClick(true);
	}

	private MenuEntry useOn(Item item, Interactable target)
	{
		if (target instanceof TileItem)
		{
			return target.getMenu(0, MenuAction.WIDGET_TARGET_ON_GROUND_ITEM.getId()).toEntry(client, 0)
					.setOption(ONECLICK_MENUOPTION_PREFIX + item.getName() + " ->")
					.setTarget(((TileItem) target).getName())
					.onClick(x -> item.use());
		}

		if (target instanceof TileObject)
		{
			return target.getMenu(0, MenuAction.WIDGET_TARGET_ON_GAME_OBJECT.getId()).toEntry(client, 0)
					.setOption(ONECLICK_MENUOPTION_PREFIX + item.getName() + " ->")
					.setTarget(((TileObject) target).getName())
					.onClick(x -> item.use());
		}

		if (target instanceof Item)
		{
			return target.getMenu(0, MenuAction.WIDGET_TARGET_ON_WIDGET.getId()).toEntry(client, 0)
					.setOption(ONECLICK_MENUOPTION_PREFIX + item.getName() + " ->")
					.setTarget(((Item) target).getName())
					.onClick(x -> item.use());
		}

		if (target instanceof Actor)
		{
			MenuAction menuAction = target instanceof NPC ? MenuAction.WIDGET_TARGET_ON_NPC : MenuAction.WIDGET_TARGET_ON_PLAYER;
			return target.getMenu(0, menuAction.getId()).toEntry(client, 0)
					.setOption(ONECLICK_MENUOPTION_PREFIX + item.getName() + " ->")
					.setTarget(((Actor) target).getName())
					.onClick(x -> item.use());
		}

		return null;
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
		itemConfigs.clear();
		playerConfigs.clear();
	}

	private boolean isId(String text)
	{
		if (text == null)
		{
			return false;
		}
		try
		{
			Integer.parseInt(text);
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}

		return true;
	}
}
