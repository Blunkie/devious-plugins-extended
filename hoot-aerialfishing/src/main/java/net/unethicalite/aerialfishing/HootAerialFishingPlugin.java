package net.unethicalite.aerialfishing;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.Projectiles;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.function.Predicate;

@Extension
@PluginDescriptor(name = "Hoot Aerial Fishing", enabledByDefault = false)
public class HootAerialFishingPlugin extends LoopedPlugin
{
	@Inject
	private Client client;

	private static final Predicate<NPC> VALID_SPOT = x -> x.getName() != null && x.getName().equals("Fishing spot")
			&& Players.getNearest(p -> p.getInteracting() != null && p.getInteracting().equals(x)) == null
			&& Projectiles.getNearest(p -> p.getTarget() != null && p.getTarget().equals(x.getLocalLocation())) == null;

	@Override
	protected int loop()
	{
		NPC arrowFishSpot = NPCs.getNearest(x -> VALID_SPOT.test(x) && client.getHintArrowNpc() == x);
		NPC fishSpot = NPCs.getNearest(VALID_SPOT);
		Item fish = Inventory.getFirst("Bluegill", "Common tench", "Mottled eel", "Greater siren");
		if (fish != null)
		{
			fish.useOn(Inventory.getFirst("Knife"));
			Time.sleep(100);
		}

		if (arrowFishSpot != null)
		{
			arrowFishSpot.interact("Catch");
			return -1;
		}

		if (fishSpot != null)
		{
			fishSpot.interact("Catch");
			return -1;
		}

		return -1;
	}

	@Provides
	HootAerialFishingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootAerialFishingConfig.class);
	}
}
