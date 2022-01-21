package dev.hoot.blackjack;

import com.google.inject.Provides;
import dev.hoot.api.entities.NPCs;
import dev.hoot.api.entities.Players;
import dev.hoot.api.entities.TileObjects;
import dev.hoot.api.game.Combat;
import dev.hoot.api.game.Worlds;
import dev.hoot.api.items.Inventory;
import dev.hoot.api.items.Shop;
import dev.hoot.api.movement.Movement;
import dev.hoot.api.movement.Reachable;
import dev.hoot.api.plugins.LoopedPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Comparator;

@Extension
@PluginDescriptor(name = "Hoot Blackjack")
@Slf4j
public class HootBlackjackPlugin extends LoopedPlugin {
    @Inject
    private HootBlackjackConfig config;

    @Override
    protected int loop() {
        BlackjackSpot spot = config.blackjackSpot();
        Item pouch = Inventory.getFirst("Coin pouch");
        Player local = Players.getLocal();
        NPC target = NPCs.getNearest(x ->
                x.getName() != null &&
                        x.getName().equals(spot.getNpcName()) &&
                        x.hasAction("Knock-Out") &&
                        spot.getArea().contains(x)
        );
        if (target != null && target.getInteracting() != null && target.getInteracting() == local) {
            target.interact("Pickpocket");
            return 333;
        }

        if (pouch != null && pouch.getQuantity() > 5) {
            pouch.interact("Open-all");
            return -1;
        }

        Item jug = Inventory.getFirst("Jug");
        if (jug != null) {
            jug.interact("Drop");
            return -1;
        }

        Item food = Inventory.getFirst(config.foodId());
        if (food != null) {
            if (spot.getArea().contains(local)) {
                Player otherPlayer = Players.getNearest(x -> !x.equals(local) && spot.getArea().contains(x));
                if (target == null) {
                    Worlds.hopTo(Worlds.getRandom(x -> x.getActivity().contains("Leagues")));
                    return -3;
                }

                NPC otherNpc = NPCs.getNearest(x ->
                        !x.equals(target) &&
                                x.getName() != null && x.getName().equals(spot.getNpcName()) &&
                                x.hasAction("Knock-Out") &&
                                spot.getArea().contains(x)
                );
                if (otherPlayer != null || otherNpc != null) {
                    Worlds.hopTo(Worlds.getRandom(x -> x.getActivity().contains("Leagues")));
                    return -3;
                }

                if (Combat.getMissingHealth() >= config.eatHp()) {
                    food.interact(0);
                    return -1;
                }

                if (local.getGraphic() == 245) {
                    return -1;
                }

                TileObject curtain = TileObjects.within(spot.getArea().offset(1), x -> x.hasAction("Close"))
                        .stream().min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(local.getWorldLocation())))
                        .orElse(null);
                if (curtain != null) {
                    curtain.interact("Close");
                    return -1;
                }

                if (target.getOverheadText() != null && target.getOverheadText().contains("Zzz")) {
                    log.info("Pickpocketing");
                    target.interact("Pickpocket");
                    return -2;
                }

                log.info("Knocking out");
                target.interact("Knock-Out");
                return 333;
            }

            Movement.walkTo(spot.getArea());
            return -1;
        }

        if (config.buyWines()) {
            if (Movement.isWalking()) {
                return -1;
            }

            if (Shop.isOpen()) {
                Shop.buyFifty(config.foodId());
                return -1;
            }

            NPC shop = NPCs.getNearest("Ali The barman");
            if (shop == null || !Reachable.isInteractable(shop)) {
                Movement.walkTo(3359, 2958, 0);
                return -1;
            }

            shop.interact("Trade");
            return -1;
        }

        log.info("We are idle");
        return -1;
    }

    @Provides
    HootBlackjackConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HootBlackjackConfig.class);
    }
}
