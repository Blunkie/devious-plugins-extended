package dev.hoot.aerialfishing;

import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.hoot.api.commons.Time;
import dev.hoot.api.entities.NPCs;
import dev.hoot.api.items.Inventory;
import dev.hoot.api.plugins.LoopedPlugin;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(name = "Hoot Aerial Fishing", enabledByDefault = false)
public class HootAerialFishingPlugin extends LoopedPlugin {
    @Inject
    private Client client;

    @Override
    protected int loop() {
        NPC arrowFishSpot = NPCs.getNearest(x -> x.getName() != null && x.getName().equals("Fishing spot")
                && client.getHintArrowNpc() == x);
        NPC fishSpot = NPCs.getNearest(x -> x.getName() != null && x.getName().equals("Fishing spot"));
        Item fish = Inventory.getFirst("Bluegill", "Common tench", "Mottled eel", "Greater siren");
        if (fish != null) {
            fish.useOn(Inventory.getFirst("Knife"));
            Time.sleep(100);
        }

        if (arrowFishSpot != null) {
            arrowFishSpot.interact("Catch");
            return -1;
        }

        if (fishSpot != null) {
            fishSpot.interact("Catch");
            return -1;
        }

        return -1;
    }

    @Provides
    HootAerialFishingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HootAerialFishingConfig.class);
    }
}
