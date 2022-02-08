package dev.hoot.interactiontester;

import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.hoot.api.entities.NPCs;
import dev.hoot.api.entities.Players;
import dev.hoot.api.entities.TileItems;
import dev.hoot.api.entities.TileObjects;
import dev.hoot.api.items.Bank;
import dev.hoot.api.items.Equipment;
import dev.hoot.api.items.Inventory;
import dev.hoot.api.plugins.LoopedPlugin;
import dev.hoot.api.widgets.Widgets;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@PluginDescriptor(name = "Interaction Tester")
@Extension
@Slf4j
public class InteractionTesterPlugin extends LoopedPlugin
{
	@Inject
	private InteractionTesterConfig config;

	@Override
	protected int loop()
	{
		Player local = Players.getLocal();
		WorldPoint loc = local.getWorldLocation();

		switch (config.entity())
		{
			case NPC:
				NPCs.getNearest(x ->
								x.getId() == config.entityId() || x.getName().equals(config.entityName()))
						.interact(config.action());
				break;
			case PLAYER:
				Players.getNearest(x ->
								x.getId() == config.entityId() || x.getName().equals(config.entityName()))
						.interact(config.action());
				break;
			case TILE_OBJECT:
				TileObjects.getFirstSurrounding(loc, config.scanRadius(), x ->
								x.getId() == config.entityId() || x.getName().equals(config.entityName()))
						.interact(config.action());
				break;
			case TILE_ITEM:
				TileItems.getFirstSurrounding(loc, config.scanRadius(), x ->
								x.getId() == config.entityId() || x.getName().equals(config.entityName()))
						.interact(config.action());
				break;
			case INV_ITEM:
				Inventory.getFirst(x ->
								x.getId() == config.entityId() || x.getName().equals(config.entityName()))
						.interact(config.action());
				break;
			case EQUIP_ITEM:
				Equipment.getFirst(x ->
								x.getId() == config.entityId() || x.getName().equals(config.entityName()))
						.interact(config.action());
				break;
			case BANK_ITEM:
				Bank.getFirst(x ->
								x.getId() == config.entityId() || x.getName().equals(config.entityName()))
						.interact(config.action());
				break;
			case WIDGET:
				Widgets.fromId(config.entityId()).interact(config.action());
				break;
		}

		return config.loopTimeOut();
	}

	@Provides
	InteractionTesterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InteractionTesterConfig.class);
	}
}
