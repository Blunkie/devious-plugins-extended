package dev.hoot.tempoross;

import com.google.inject.Provides;
import dev.hoot.api.entities.NPCs;
import dev.hoot.api.entities.Players;
import dev.hoot.api.entities.TileObjects;
import dev.hoot.api.items.Inventory;
import dev.hoot.api.movement.Movement;
import dev.hoot.api.plugins.LoopedPlugin;
import dev.hoot.api.widgets.Widgets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
        name = "Hoot Tempoross"
)
@Singleton
@Slf4j
public class HootTempoross extends LoopedPlugin {

    @Inject
    private Client client;

    @Inject
    private HootTemporossConfig config;

    private final static int HARPOON_ID = 311;
    private final static int EMPTY_BUCKET_ID = 1925;
    private final static int WATER_BUCKET_ID = 1929;
    private final static int ROPE_ID = 954;
    private final static int HAMMER_ID = 2347;
    private final static int RAW_FISH_ID = 25564;
    private final static int COOKED_FISH_ID = 25565;


    private final static int SINGLE_FISH_SPOT = 10565;
    private final static int DOUBLE_FISH_SPOT = 10569;

    private final static int COOKING_SHRINE = 41236;

    private final static int FISHING_ANIM_ID = 618;
    private final static int COOKING_ANIM_ID = 896;


    private final static int CLOUD_NPC_ID = 10580;
    private final static int CLOUD_SHADOW_OBJECT_ID = 41006;
    private final static int FIRE_NPC_ID = 8643;
    private final static int FIRE_OBJECT_ID = 37582;

    private final static int AMMO_CRATE_NPC = 10576; // fill
    private final static int AMMO_CRATE = 40968; // fill

    private final static int DAMAGED_TOTEM_OBJECT_ID =41010;
    private final static int DAMAGED_MAST_OBJECT_ID = 40996;
    private final static int MAST_OBJECT_ID = 41352;
    private final static int TOTEM_OBJECT_ID = 41354;

    private final static int POINTS_VARP = 11897;
    private final static int TETHER_VARP = 11895;

    private final static int EXIT_NPC_ID = 10595;

    private final int INIT_WHIRLPOOL = 10570;
    private final int VULN_WHIRLPOOL = 10571;
    private final int VULN_TEMPOROSS = 10574;

    private final static int WAITING_ROOM_LADDER_OBJECT_ID = 41305;
    private final static int WAITING_ROOM_PUMP_OBJECT_ID = 41000;

    private final List<Integer> ESCAPE_NPC = List.of(10593, 10587);
    private final List<Integer> EXIT_NPC = List.of(10595);

    private Point CENTRAL_POINT = new Point(60, 62);

    private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

    private static int DEPOSITED_FISH = 0;

    @Override
    protected int loop() {
        Player player = Players.getLocal();
        int hammerCount = Inventory.getCount(HAMMER_ID);
        int ropeCount = Inventory.getCount(ROPE_ID);
        if (!client.isInInstancedRegion()) {
            incomingWave = false;
            scriptState = State.INITIAL_CATCH;
            DEPOSITED_FISH = 0;

            if (player.isMoving()) {
                return 1000;
            }

            TileObject startLadder = TileObjects.getFirstAt(3135, 2840, 0, WAITING_ROOM_LADDER_OBJECT_ID);
            if (startLadder == null) {
                return 1000;
            }

            // If east of ladder, we're not in the room.
            if (player.getWorldLocation().getX() > startLadder.getWorldLocation().getX()) {
                startLadder.interact("Quick-climb");
                return 1000;
            }

            int emptyBuckets = Inventory.getCount(EMPTY_BUCKET_ID);
            TileObject waterPump = TileObjects.getFirstAt(3135, 2832, 0, WAITING_ROOM_PUMP_OBJECT_ID);
            if (waterPump != null && emptyBuckets > 0) {
                waterPump.interact("Use");
                return 2000;
            }

            return 1000;
        }

        /**
         * Is in game
         */
        Widget energyWidget = Widgets.get(437, 35);
        Widget essenceWidget = Widgets.get(437, 45);
        Widget intensityWidget = Widgets.get(437, 55);
        if (!Widgets.isVisible(energyWidget) || !Widgets.isVisible(essenceWidget) || !Widgets.isVisible(intensityWidget)) {
            return 1000;
        }

        Matcher energyMatcher = DIGIT_PATTERN.matcher(energyWidget.getText());
        Matcher essenceMatcher = DIGIT_PATTERN.matcher(essenceWidget.getText());
        Matcher intensityMatcher = DIGIT_PATTERN.matcher(intensityWidget.getText());
        if (!energyMatcher.find() || !essenceMatcher.find() || !intensityMatcher.find()) {
            return 1000;
        }

        ENERGY = Integer.parseInt(energyMatcher.group(0));
        ESSENCE = Integer.parseInt(essenceMatcher.group(0));
        INTENSITY = Integer.parseInt(intensityMatcher.group(0));

        /**
         * Danger tasks
         */
        int bucketOfWaterCount = Inventory.getCount(WATER_BUCKET_ID);
        NPC fire = NPCs.getNearest(FIRE_NPC_ID);
        if (fire != null && fire.getWorldLocation().distanceToPath(client, player.getWorldLocation()) < 8) {
            if (bucketOfWaterCount == 0) {
                forfeitMatch();
                return 1000;
            }

            fire.interact("Douse");
            return 1000;
        }

        TileObject damagedMast = TileObjects.getNearest(DAMAGED_MAST_OBJECT_ID);
        if (hammerCount > 0 && damagedMast != null && damagedMast.getWorldLocation().distanceToPath(client, player.getWorldLocation()) < 15) {
            damagedMast.interact("Repair");
            return 1000;
        }

        if (incomingWave) {
            if (!tethered) {
                TileObject tether = TileObjects.getNearest(TOTEM_OBJECT_ID, MAST_OBJECT_ID);
                if (tether != null) {
                    tether.interact("Tether");
                    return 2000;
                } else {
                    forfeitMatch();
                    return 1000;
                }
            }

            return 1000;
        }

        NPC exitNpc = NPCs.getNearest(EXIT_NPC_ID);
        if (exitNpc != null) {
            exitNpc.interact("Leave");
            return 1000;
        }

        if (ESSENCE == 0) {
            return 1000;
        }

        NPC doubleSpot = NPCs.getNearest(DOUBLE_FISH_SPOT);
        if (scriptState.equals(State.INITIAL_COOK) && doubleSpot != null) {
            scriptState = scriptState.next;
        }

        if (INTENSITY >= 94 && scriptState.equals(State.THIRD_COOK)) {
            forfeitMatch();
            return 1000;
        }

        if (scriptState.isComplete.getAsBoolean()) {
            scriptState = scriptState.next;
            if (scriptState == null) {
                DEPOSITED_FISH = 17;
                scriptState = State.THIRD_CATCH;
            }
        }

        int rawFishCount = Inventory.getCount(RAW_FISH_ID);
        List<WorldPoint> dangerousTiles = TileObjects.getSurrounding(Players.getLocal().getWorldLocation(), 20, CLOUD_SHADOW_OBJECT_ID, FIRE_OBJECT_ID)
                .stream()
                .filter(g -> g instanceof GameObject)
                .flatMap(g -> ((GameObject) g).getWorldArea().toWorldPointList().stream())
                .collect(Collectors.toList());
        final Predicate<NPC> filterDangerousNPCs = (NPC npc) -> !dangerousTiles.contains(npc.getWorldLocation());

        /**
         * Gather tasks
         */
        switch (scriptState) {
            case INITIAL_CATCH:
            case SECOND_CATCH:
            case THIRD_CATCH:
                Comparator<NPC> closest = Comparator.comparingInt(spot -> spot.getWorldLocation().distanceToPath(client, player.getWorldLocation()));
                Comparator<NPC> byId = Comparator.comparingInt(NPC::getId);
                Optional<NPC> fishSpot = NPCs.getAll(SINGLE_FISH_SPOT, DOUBLE_FISH_SPOT)
                        .stream()
                        .filter(filterDangerousNPCs)
                        .min(rawFishCount < 15 ? byId.reversed().thenComparing(closest) : closest);
                if (fishSpot.isPresent()) {
                    if (fishSpot.get().equals(player.getInteracting())) {
                        return 1000;
                    }
                    fishSpot.get().interact("Harpoon");
                    return 1000;
                } else {
                    walkToSafePoint();
                }
                break;

            case INITIAL_COOK:
            case SECOND_COOK:
            case THIRD_COOK:
                TileObject range = TileObjects.getNearest(COOKING_SHRINE);
                if (range != null && rawFishCount > 0) {
                    if (player.getAnimation() == COOKING_ANIM_ID || player.isMoving()) {
                        return 1000;
                    }

                    range.interact("Cook-at");
                    return 1200;
                } else if (range == null) {
                    walkToSafePoint();
                }
                break;

            case EMERGENCY_FILL:
            case SECOND_FILL:
            case INITIAL_FILL:
                NPC ammoCrate = NPCs.getNearest(x -> x.getId() == AMMO_CRATE_NPC && filterDangerousNPCs.test(x));
                if (ammoCrate != null && !ammoCrate.equals(player.getInteracting())) {
                    if (Inventory.getCount(COOKED_FISH_ID) == 0) {
                        return 1000;
                    }

                    ammoCrate.interact("Fill");
                    return 1000;
                } else if (ammoCrate == null) {
                    walkToSafePoint();
                }
                break;

            case ATTACK_TEMPOROSS:
                NPC temporossPool = NPCs.getNearest(VULN_WHIRLPOOL);
                if (temporossPool != null && !temporossPool.equals(player.getInteracting())) {
                    temporossPool.interact("Harpoon");
                    return 1000;
                } else if(temporossPool == null) {
                    walkToSafePoint();
                }
                break;
        }

        return 100;
    }

    enum State {
        ATTACK_TEMPOROSS(() -> ENERGY >= 98, null),
        SECOND_FILL(() -> DEPOSITED_FISH >= 36, ATTACK_TEMPOROSS),
        THIRD_COOK(() -> Inventory.getCount(RAW_FISH_ID) == 0 || INTENSITY >= 92, SECOND_FILL),
        THIRD_CATCH(() -> Inventory.getCount(RAW_FISH_ID, COOKED_FISH_ID) >= 36 - DEPOSITED_FISH, SECOND_FILL),
        EMERGENCY_FILL(() -> Inventory.getCount(COOKED_FISH_ID) == 0, THIRD_CATCH),
        INITIAL_FILL(() -> DEPOSITED_FISH >= 17, THIRD_CATCH),
        SECOND_COOK(() -> Inventory.getCount(RAW_FISH_ID) == 0, INITIAL_FILL),
        SECOND_CATCH(() -> Inventory.getCount(RAW_FISH_ID, COOKED_FISH_ID) >= 17, SECOND_COOK),
        INITIAL_COOK(() -> Inventory.getCount(RAW_FISH_ID) == 0, SECOND_CATCH),
        INITIAL_CATCH(() -> Inventory.getCount(RAW_FISH_ID) >= 8, INITIAL_COOK);

        @Getter
        private final BooleanSupplier isComplete;

        @Getter
        private final State next;

        State(BooleanSupplier isComplete, State next) {
            this.isComplete = isComplete;
            this.next = next;
        }
    }

    private State scriptState = State.INITIAL_CATCH;

    @Override
    protected void startUp() {
        scriptState = State.INITIAL_CATCH;
    }

    private boolean incomingWave = false;
    private boolean tethered = false;


    private static int ENERGY = 100;
    private static int ESSENCE = 100;
    private static int INTENSITY = 0;

    private void forfeitMatch() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }
        final NPC npc = NPCs.getNearest(10593, 10587);
        final Actor target = player.getInteracting();
        if (npc != null && (target == null || target.equals(npc))) {
            npc.interact("Forfeit");
        }
    }

    private void walkToSafePoint() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }
        WorldPoint safePoint = WorldPoint.fromScene(client, CENTRAL_POINT.getX(), CENTRAL_POINT.getY(), client.getPlane());
        if (safePoint.distanceTo(player.getWorldLocation()) > 3 && !player.isMoving()) {
            Movement.walk(safePoint);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        ChatMessageType type = event.getType();
        String message = event.getMessage();
        System.out.println(event.getType().name() + " " + event.getMessage());

        if (type.equals(ChatMessageType.GAMEMESSAGE)) {
            if (message.equals("<col=d30b0b>A colossal wave closes in...</col>")) {
                incomingWave = true;
                tethered = false;
            }

            if (message.contains("the rope keeps you securely") || message.contains("the wave slames into you")) {
                incomingWave = false;
            }
        }

        if (type.equals(ChatMessageType.SPAM)) {
            if (message.equals("You securely tether yourself to the totem pole.") || message.equals("You securely tether yourself to the mast.")) {
                tethered = true;
            }
        }
    }

    HashMap<Skill, Integer> skillExpTable = new HashMap<>();
    @Subscribe
    public void onStatChanged(StatChanged event) {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        Actor target = player.getInteracting();
        if (target instanceof NPC && ((NPC) target).getId() == VULN_WHIRLPOOL) {
            return;
        }

        int change = event.getXp() - skillExpTable.getOrDefault(event.getSkill(), event.getXp());
        skillExpTable.put(event.getSkill(), event.getXp());
        if (event.getSkill().equals(Skill.FISHING)) {
            // Probably a cannon load but fuck knows
            NPC cannon = NPCs.getNearest(AMMO_CRATE_NPC);
            if (cannon != null && cannon.getWorldLocation().distanceTo(player.getWorldLocation()) <= 4 && change > client.getRealSkillLevel(Skill.FISHING)) {
                System.out.println("Deposited a fish " + DEPOSITED_FISH);
                DEPOSITED_FISH++;
            }
        }
    }

    @Provides
    HootTemporossConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(HootTemporossConfig.class);
    }
}