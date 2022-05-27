package net.unethicalite.tempoross;

import com.google.inject.Provides;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Locatable;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.unethicalite.tempoross.TemporossID.ANIMATION_COOK;
import static net.unethicalite.tempoross.TemporossID.ANIMATION_INTERACTING;
import static net.unethicalite.tempoross.TemporossID.GRAPHIC_TETHERED;
import static net.unethicalite.tempoross.TemporossID.GRAPHIC_TETHERING;
import static net.unethicalite.tempoross.TemporossID.ITEM_COOKED_FISH;
import static net.unethicalite.tempoross.TemporossID.ITEM_EMPTY_BUCKET;
import static net.unethicalite.tempoross.TemporossID.ITEM_HAMMER;
import static net.unethicalite.tempoross.TemporossID.ITEM_HARPOON;
import static net.unethicalite.tempoross.TemporossID.ITEM_RAW_FISH;
import static net.unethicalite.tempoross.TemporossID.ITEM_ROPE;
import static net.unethicalite.tempoross.TemporossID.ITEM_WATER_BUCKET;
import static net.unethicalite.tempoross.TemporossID.NPC_DOUBLE_FISH_SPOT;
import static net.unethicalite.tempoross.TemporossID.NPC_EXIT;
import static net.unethicalite.tempoross.TemporossID.NPC_FIRE;
import static net.unethicalite.tempoross.TemporossID.NPC_SINGLE_FISH_SPOT;
import static net.unethicalite.tempoross.TemporossID.NPC_SINGLE_FISH_SPOT_SECOND;
import static net.unethicalite.tempoross.TemporossID.NPC_VULN_WHIRLPOOL;
import static net.unethicalite.tempoross.TemporossID.OBJECT_CLOUD_SHADOW;
import static net.unethicalite.tempoross.TemporossID.OBJECT_DAMAGED_MAST;
import static net.unethicalite.tempoross.TemporossID.OBJECT_FIRE;
import static net.unethicalite.tempoross.TemporossID.OBJECT_LOBBY_LADDER;
import static net.unethicalite.tempoross.TemporossID.OBJECT_LOBBY_PUMP;

@Extension
@PluginDescriptor(
		name = "Hoot Tempoross",
		enabledByDefault = false
)
@Slf4j
public class HootTemporossPlugin extends LoopedPlugin
{

	@Inject
	private Client client;

	private int waves = 0;
	private TemporossWorkArea workArea = null;

	private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

	@Override
	protected int loop()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return 1000;
		}
		int animation = player.getAnimation();
		if (!client.isInInstancedRegion())
		{
			waves = 0;
			workArea = null;
			incomingWave = false;
			scriptState = State.INITIAL_CATCH;

			if (player.isMoving() || player.isAnimating())
			{
				return -5;
			}

			TileObject startLadder = TileObjects.getFirstAt(3135, 2840, 0, OBJECT_LOBBY_LADDER);
			if (startLadder == null)
			{
				return -1;
			}

			// If east of ladder, we're not in the room.
			if (player.getWorldLocation().getX() > startLadder.getWorldLocation().getX())
			{
				startLadder.interact("Quick-climb");
				return -6;
			}

			int emptyBuckets = Inventory.getCount(ITEM_EMPTY_BUCKET);
			TileObject waterPump = TileObjects.getFirstAt(3135, 2832, 0, OBJECT_LOBBY_PUMP);
			if (waterPump != null && emptyBuckets > 0)
			{
				waterPump.interact("Use");
				return -6;
			}

			return -1;
		}

		if (workArea == null)
		{
			NPC npc = NPCs.getNearest(x -> x.hasAction("Forfeit"));
			NPC ammoCrate = NPCs.getNearest(x -> x.hasAction("Fill") && x.hasAction("Check-ammo"));

			if (npc == null || ammoCrate == null)
			{
				return -1;
			}

			boolean isWest = npc.getWorldLocation().getX() < ammoCrate.getWorldLocation().getX();
			TemporossWorkArea area = new TemporossWorkArea(npc.getWorldLocation(), isWest);
			log.info("Found work area: {}", area);
			workArea = area;
			return -1;
		}

		NPC leave = NPCs.getNearest(x -> x.hasAction("Leave"));
		if (leave != null)
		{
			leave.interact("Leave");
			return -6;
		}

		if (getPhase() >= 2 && needToClearFire(workArea.getClosestTether()))
		{
			return -2;
		}

		int harpoonCount = Inventory.getCount(ITEM_HARPOON);
		if (harpoonCount != 1)
		{
			if (player.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (harpoonCount > 1)
			{
				Inventory.getFirst(ITEM_HARPOON).interact("Drop");
				return -3;
			}

			if (needToClearFire(workArea.getHarpoonCrate()) && getPhase() == 1)
			{
				return -2;
			}

			workArea.getHarpoonCrate().interact("Take");
			return -2;
		}

		int bucketCount = Inventory.getCount(ITEM_EMPTY_BUCKET, ITEM_WATER_BUCKET);
		if (bucketCount != 5)
		{
			if (player.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (bucketCount > 5)
			{
				Inventory.getFirst(ITEM_EMPTY_BUCKET).interact("Drop");
				return -3;
			}

			if (needToClearFire(workArea.getBucketCrate()) && getPhase() == 1)
			{
				return -2;
			}

			workArea.getBucketCrate().interact("Take");
			return -2;
		}

		int ropeCount = Inventory.getCount(ITEM_ROPE);
		if (ropeCount != 1)
		{
			if (player.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (ropeCount > 1)
			{
				Inventory.getFirst(ITEM_ROPE).interact("Drop");
				return -3;
			}

			if (needToClearFire(workArea.getRopeCrate()) && getPhase() == 1)
			{
				return -2;
			}

			workArea.getRopeCrate().interact("Take");
			return -2;
		}

		int hammerCount = Inventory.getCount(ITEM_HAMMER);
		if (hammerCount != 1)
		{
			if (player.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (hammerCount > 1)
			{
				Inventory.getFirst(ITEM_HAMMER).interact("Drop");
				return -3;
			}

			if (needToClearFire(workArea.getHammerCrate()) && getPhase() == 1)
			{
				return -2;
			}

			workArea.getHammerCrate().interact("Take");
			return -2;
		}

		/**
		 * Is in game
		 */
		Widget energyWidget = Widgets.get(437, 35);
		Widget essenceWidget = Widgets.get(437, 45);
		Widget intensityWidget = Widgets.get(437, 55);
		if (!Widgets.isVisible(energyWidget) || !Widgets.isVisible(essenceWidget) || !Widgets.isVisible(intensityWidget))
		{
			return 1000;
		}

		Matcher energyMatcher = DIGIT_PATTERN.matcher(energyWidget.getText());
		Matcher essenceMatcher = DIGIT_PATTERN.matcher(essenceWidget.getText());
		Matcher intensityMatcher = DIGIT_PATTERN.matcher(intensityWidget.getText());
		if (!energyMatcher.find() || !essenceMatcher.find() || !intensityMatcher.find())
		{
			return 1000;
		}

		ENERGY = Integer.parseInt(energyMatcher.group(0));
		ESSENCE = Integer.parseInt(essenceMatcher.group(0));
		INTENSITY = Integer.parseInt(intensityMatcher.group(0));

		/**
		 * Danger tasks
		 */

		TileObject damagedMast = TileObjects.getFirstAt(Tiles.getAt(workArea.getMastPoint()), OBJECT_DAMAGED_MAST);
		if (damagedMast != null && damagedMast.getWorldLocation().distanceToPath(client, player.getWorldLocation()) < 15)
		{
			damagedMast.interact("Repair");
			return 1000;
		}

		TileObject tether = workArea.getClosestTether();
		if (incomingWave)
		{
			if (!isTethered())
			{
				if (tether == null)
				{
					log.warn("Can't find tether object");
					return -1;
				}

				tether.interact("Tether");
				return -3;
			}

			return -2;
		}

		if (tether != null && Players.getLocal().getGraphic() == GRAPHIC_TETHERED)
		{
			tether.interact("Untether");
			return -2;
		}

		NPC exitNpc = NPCs.getNearest(NPC_EXIT);
		if (exitNpc != null)
		{
			exitNpc.interact("Leave");
			return 1000;
		}

		NPC doubleSpot = NPCs.getNearest(NPC_DOUBLE_FISH_SPOT);
		if (scriptState == State.INITIAL_COOK && doubleSpot != null)
		{
			scriptState = scriptState.next;
		}

		if (INTENSITY >= 94 && scriptState == State.THIRD_COOK)
		{
			forfeitMatch();
			return 1000;
		}

		if (scriptState == null)
		{
			scriptState = State.THIRD_CATCH;
		}

		if (scriptState.isComplete.getAsBoolean())
		{
			scriptState = scriptState.next;
			if (scriptState == null)
			{
				scriptState = State.THIRD_CATCH;
			}
		}

		NPC temporossPool = NPCs.getNearest(NPC_VULN_WHIRLPOOL);
		if (temporossPool != null && scriptState != State.ATTACK_TEMPOROSS)
		{
			scriptState = State.ATTACK_TEMPOROSS;
		}

		int rawFishCount = Inventory.getCount(ITEM_RAW_FISH);
		List<WorldPoint> dangerousTiles = TileObjects.getSurrounding(Players.getLocal().getWorldLocation(), 20, OBJECT_CLOUD_SHADOW, OBJECT_FIRE)
				.stream()
				.filter(g -> g instanceof GameObject)
				.flatMap(g -> ((GameObject) g).getWorldArea().toWorldPointList().stream())
				.collect(Collectors.toList());
		final Predicate<NPC> filterDangerousNPCs = (NPC npc) -> !dangerousTiles.contains(npc.getWorldLocation());

		log.info("State: " + scriptState);
		/**
		 * Gather tasks
		 */
		switch (scriptState)
		{
			case INITIAL_CATCH:
			case SECOND_CATCH:
			case THIRD_CATCH:
				NPC fishSpot = NPCs.getNearest(it ->
						NPC_DOUBLE_FISH_SPOT == it.getId()
								&& it.getWorldLocation().distanceTo(workArea.getRangePoint()) <= 20
								&& filterDangerousNPCs.test(it));

				if (fishSpot == null)
				{
					fishSpot = NPCs.getNearest(it ->
							Set.of(NPC_SINGLE_FISH_SPOT, NPC_SINGLE_FISH_SPOT_SECOND).contains(it.getId())
									&& it.getWorldLocation().distanceTo(workArea.getRangePoint()) <= 20
									&& filterDangerousNPCs.test(it));
				}

				if (fishSpot != null)
				{
					if (fishSpot.equals(player.getInteracting()) && !Dialog.isOpen())
					{
						return 1000;
					}

					if (needToClearFire(fishSpot))
					{
						return -2;
					}

					fishSpot.interact("Harpoon");
					return 1000;
				}
				else
				{
					// if fish are null walk to the totem pole since it's in the center of the fish spots.
					Movement.walkTo(workArea.getTotemPoint());
					return 1000;
				}

			case INITIAL_COOK:
			case SECOND_COOK:
			case THIRD_COOK:
				TileObject range = workArea.getRange();
				if (range != null && rawFishCount > 0)
				{
					if ((player.getAnimation() == ANIMATION_COOK || player.isMoving()) && !Dialog.isOpen())
					{
						return 1000;
					}

					if (needToClearFire(range))
					{
						return -2;
					}

					range.interact("Cook-at");
					return 1200;
				}
				else if (range == null)
				{
					Movement.walkTo(workArea.getRangePoint());
					return 1000;
				}

			case EMERGENCY_FILL:
			case SECOND_FILL:
			case INITIAL_FILL:
				NPC ammoCrate = NPCs.getNearest(x -> x.hasAction("Fill")
						&& x.getWorldLocation().distanceTo(workArea.getSafePoint()) <= 10
						&& x.hasAction("Check-ammo")
						&& filterDangerousNPCs.test(x));
				if (ammoCrate != null && (!ammoCrate.equals(player.getInteracting()) || Dialog.isOpen()))
				{
					if (needToClearFire(ammoCrate))
					{
						return -2;
					}

					ammoCrate.interact("Fill");
					return 1000;
				}
				else if (ammoCrate == null)
				{
					log.warn("Can't find the ammo crate");
					if (needToClearFire(workArea.getSafePoint()))
					{
						return -2;
					}

					walkToSafePoint();
				}
				break;

			case ATTACK_TEMPOROSS:
				if (temporossPool != null && (!temporossPool.equals(player.getInteracting()) || Dialog.isOpen()))
				{
					if (needToClearFire(temporossPool))
					{
						return -2;
					}

					temporossPool.interact("Harpoon");
					return 1000;
				}
				else if (temporossPool == null)
				{
					if (needToClearFire(workArea.getSafePoint()))
					{
						return -2;
					}

					if (ENERGY > 0)
					{
						scriptState = null;
						return -1;
					}

					walkToSafePoint();
				}

				break;
		}

		return -1;
	}

	enum State
	{
		ATTACK_TEMPOROSS(() -> ENERGY >= 98, null),
		SECOND_FILL(() -> getAllFish() == 0, ATTACK_TEMPOROSS),
		THIRD_COOK(() -> getRawFish() == 0 || INTENSITY >= 92, SECOND_FILL),
		THIRD_CATCH(() -> getAllFish() >= 17, THIRD_COOK),
		EMERGENCY_FILL(() -> getAllFish() == 0, THIRD_CATCH),
		INITIAL_FILL(() -> getAllFish() == 0, THIRD_CATCH),
		SECOND_COOK(() -> getRawFish() == 0, INITIAL_FILL),
		SECOND_CATCH(() -> getAllFish() >= 17, SECOND_COOK),
		INITIAL_COOK(() -> getRawFish() == 0, SECOND_CATCH),
		INITIAL_CATCH(() -> getRawFish() >= 8 || getAllFish() >= 17, INITIAL_COOK);

		@Getter
		private final BooleanSupplier isComplete;

		@Getter
		private final State next;

		State(BooleanSupplier isComplete, State next)
		{
			this.isComplete = isComplete;
			this.next = next;
		}
	}

	private State scriptState = State.INITIAL_CATCH;

	@Override
	protected void startUp() throws Exception
	{
		super.startUp();
		scriptState = State.INITIAL_CATCH;
	}

	private boolean incomingWave = false;

	private static int ENERGY = 100;
	private static int ESSENCE = 100;
	private static int INTENSITY = 0;

	private void forfeitMatch()
	{
		NPC npc = NPCs.getNearest(x -> x.hasAction("Forfeit"));
		if (npc != null)
		{
			npc.interact("Forfeit");
		}
	}

	private void walkToSafePoint()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		WorldPoint safePoint = workArea.getSafePoint();
		if (safePoint.distanceTo(player.getWorldLocation()) > 3 && !player.isMoving())
		{
			Movement.walk(safePoint);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();
		String message = event.getMessage();

		if (type == ChatMessageType.GAMEMESSAGE)
		{
			if (message.equals("<col=d30b0b>A colossal wave closes in...</col>"))
			{
				waves++;
				incomingWave = true;
			}

			if (message.contains("the rope keeps you securely") || message.contains("the wave slams into you"))
			{
				incomingWave = false;
			}
		}
	}

	private boolean needToClearFire(Locatable locatable)
	{
		return needToClearFire(locatable.getWorldLocation());
	}

	private boolean needToClearFire(WorldPoint destination)
	{
		Player player = Players.getLocal();
		int bucketOfWaterCount = Inventory.getCount(ITEM_WATER_BUCKET);
		List<WorldPoint> path = player.getWorldLocation().pathTo(client, destination);
		List<NPC> firesBlockingPath = NPCs.getAll(x -> x.getId() == NPC_FIRE &&
				x.getWorldArea().toWorldPointList().stream().anyMatch(path::contains));
		NPC fire = firesBlockingPath.stream()
				.min(Comparator.comparing(x -> x.getWorldLocation().distanceTo(player.getWorldLocation())))
				.orElse(null);
		if (fire != null)
		{
			if (bucketOfWaterCount == 0 && Inventory.contains(ITEM_EMPTY_BUCKET))
			{
				if (player.getAnimation() == ANIMATION_INTERACTING)
				{
					return true;
				}

				if (player.isMoving())
				{
					return true;
				}

				workArea.getPump().interact("Use");
				return true;
			}

			if (bucketOfWaterCount > 0)
			{
				fire.interact("Douse");
			}

			return true;
		}

		return false;
	}

	private static boolean isTethered()
	{
		int graphic = Players.getLocal().getGraphic();
		int anim = Players.getLocal().getAnimation();
		return anim != 832 && (graphic == GRAPHIC_TETHERED || graphic == GRAPHIC_TETHERING);
	}

	private static int getRawFish()
	{
		return Inventory.getCount(ITEM_RAW_FISH);
	}

	private static int getCookedFish()
	{
		return Inventory.getCount(ITEM_COOKED_FISH);
	}

	private static int getAllFish()
	{
		return getRawFish() + getCookedFish();
	}

	private int getPhase()
	{
		return 1 + (waves / 3); // every 3 waves, phase increases by 1
	}

	@Provides
	HootTemporossConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootTemporossConfig.class);
	}
}