package net.unethicalite.plugins.cooker;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.AnimationID;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Production;
import org.pf4j.Extension;

import java.util.function.Predicate;

@Extension
@PluginDescriptor(
		name = "Unethical Cooker",
		enabledByDefault = false
)
public class UnethicalCookerPlugin extends LoopedPlugin
{
	@Inject
	private UnethicalCookerConfig config;

	private int cooldown = 0;

	@Override
	protected int loop()
	{
		Item raw = Inventory.getFirst(config.item().getRawId());
		if (raw == null)
		{
			if (Bank.isOpen())
			{
				Predicate<Item> rawPredicate = x -> x.getId() == config.item().getRawId();
				if (Inventory.contains(rawPredicate.negate()))
				{
					Bank.depositInventory();
					return -2;
				}

				Bank.withdrawAll(config.item().getRawId(), Bank.WithdrawMode.ITEM);
				return -2;
			}

			NPC banker = NPCs.getNearest(x -> x.hasAction("Collect"));
			if (banker != null)
			{
				banker.interact("Bank");
				return -3;
			}

			MessageUtils.addMessage("Bank NPC not found.", ChatColorType.HIGHLIGHT);
			return -1;
		}

		Player local = Players.getLocal();
		if (local.getAnimation() == AnimationID.COOKING_RANGE || local.getAnimation() == AnimationID.COOKING_FIRE)
		{
			cooldown = config.item().getCookTicks();
			return -1;
		}

		if (cooldown > 0)
		{
			return -1;
		}

		TileObject cookingObject = TileObjects.getFirstSurrounding(
				local.getWorldLocation(),
				5,
				x -> x.hasAction("Cook")
		);
		if (cookingObject == null)
		{
			MessageUtils.addMessage("Fire/cooking range not found.", ChatColorType.HIGHLIGHT);
			return -1;
		}

		if (Production.isOpen())
		{
			Production.selectItem(config.item().getCookedId());
			return -1;
		}

		cookingObject.interact("Cook");
		return 1000;
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (cooldown > 0)
		{
			cooldown--;
		}
	}

	@Provides
	UnethicalCookerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalCookerConfig.class);
	}
}
