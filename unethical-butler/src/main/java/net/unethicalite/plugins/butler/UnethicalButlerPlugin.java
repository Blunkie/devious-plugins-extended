package net.unethicalite.plugins.butler;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.DialogOption;
import net.runelite.api.Item;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.DialogPackets;
import net.unethicalite.api.widgets.Dialog;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@Slf4j
@PluginDescriptor(name = "Unethical Butler", enabledByDefault = false)
public class UnethicalButlerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private UnethicalButlerConfig config;

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!client.isKeyPressed(getHotkey(config.keyBind())))
		{
			return;
		}

		if (event.getType() != MenuAction.NPC_FIRST_OPTION.getId())
		{
			return;
		}

		NPC butler = NPCs.getNearest(config.butler());
		if (butler == null)
		{
			return;
		}

		LogType logType = config.logType();
		// Set oneclick menuentry on butler based on inventory state
		oneClick(
				logType.getPlankNotedId(),
				logType.getLogId(),
				logType.getLogNotedId()
		);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() != MenuAction.WIDGET_TARGET_ON_NPC)
		{
			return;
		}

		// The dialog needs to be open in order for the packets to work
		if (!Dialog.canContinue())
		{
			return;
		}

		int selectedItem = client.getSelectedSpellItemId();
		log.debug("Selected item id: {}", selectedItem);

		LogType logType = config.logType();
		if (selectedItem == logType.getPlankNotedId())
		{
			// todo
			log.debug("Unnoting planks");
			return;
		}

		if (selectedItem == logType.getLogId())
		{
			Dialog.invokeDialog(
					DialogOption.NPC_CONTINUE,
					DialogOption.CHAT_OPTION_ONE
			);
			DialogPackets.sendNumberInput(config.amount());
			log.debug("Sending logs to sawmill");
			Dialog.invokeDialog(
					DialogOption.NPC_CONTINUE,
					DialogOption.CHAT_OPTION_ONE,
					DialogOption.NPC_CONTINUE
			);
			return;
		}

		if (selectedItem == logType.getLogNotedId())
		{
			Dialog.invokeDialog(
					DialogOption.NPC_CONTINUE,
					DialogOption.CHAT_OPTION_ONE
			);
			DialogPackets.sendNumberInput(config.amount());
			Dialog.invokeDialog(
					DialogOption.NPC_CONTINUE,
					DialogOption.CHAT_OPTION_ONE,
					DialogOption.NPC_CONTINUE
			);
			log.debug("Sending noted logs to butler");
		}
	}

	private int getHotkey(Keybind keybind)
	{
		// For some dumb reason, RL decided to give these Keybinds undefined keycodes
		if (keybind.equals(Keybind.SHIFT))
		{
			return KeyCode.KC_SHIFT;
		}

		if (keybind.equals(Keybind.CTRL))
		{
			return KeyCode.KC_CONTROL;
		}

		if (keybind.equals(Keybind.ALT))
		{
			return KeyCode.KC_ALT;
		}

		return keybind.getKeyCode();
	}

	private void oneClick(int... items)
	{
		NPC butler = NPCs.getNearest("Demon butler");
		if (butler == null)
		{
			return;
		}

		for (int id : items)
		{
			Item item = Inventory.getFirst(id);
			if (item != null)
			{
				String noted = item.isNoted() ? " (N)" : "";
				String target = item.getName() + noted + " -> " + butler.getName();
				MenuEntry entry = butler.getMenu(0, MenuAction.WIDGET_TARGET_ON_NPC.getId())
						.toEntry(client, "<col=00ff00>CONS:</col> ", target, m -> item.use());

				client.setMenuEntries(new MenuEntry[]{entry});
				return;
			}
		}
	}

	@Provides
	UnethicalButlerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalButlerConfig.class);
	}
}
