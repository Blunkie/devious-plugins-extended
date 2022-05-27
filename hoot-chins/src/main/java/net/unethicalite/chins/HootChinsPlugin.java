package net.unethicalite.chins;

import com.google.inject.Inject;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Item;
import net.runelite.api.ObjectID;
import net.runelite.api.Player;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.hunter.HunterTrap;
import org.pf4j.Extension;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
@Slf4j
@PluginDescriptor(name = "Hoot Chins", enabledByDefault = false)
public class HootChinsPlugin extends LoopedPlugin
{
	private static final List<Integer> TRANSITION_IDS = List.of(
			ObjectID.BOX_TRAP,
			ObjectID.BOX_TRAP_2026,
			ObjectID.BOX_TRAP_2028,
			ObjectID.BOX_TRAP_2029,

			ObjectID.BOX_TRAP_9381,
			ObjectID.BOX_TRAP_9390,
			ObjectID.BOX_TRAP_9391,
			ObjectID.BOX_TRAP_9392,
			ObjectID.BOX_TRAP_9393,

			ObjectID.BOX_TRAP_9386,
			ObjectID.BOX_TRAP_9387,
			ObjectID.BOX_TRAP_9388,

			ObjectID.BOX_TRAP_9394,
			ObjectID.BOX_TRAP_9396,
			ObjectID.BOX_TRAP_9397
	);
	private static final Map<WorldPoint, HunterTrap> MY_TRAPS = new HashMap<>();
	private static final HunterArea hunterArea = HunterArea.RED_CHIN;

	@Inject
	private Client client;

	private boolean laying = false;
	private Instant lastLayed = Instant.now();
	private WorldPoint lastTickLocalPlayerLocation = null;
	private Instant lastActionTime = Instant.ofEpochMilli(0);

	@Override
	protected int loop()
	{
		Player local = Players.getLocal();
		int maxTraps = hunterArea.getMaxTraps();
		Item item = Inventory.getFirst(hunterArea.getTrapName());

		if (MY_TRAPS.size() < maxTraps
				&& hunterArea.getArea().distanceTo(local.getWorldLocation()) < 10
				&& TileObjects.within(hunterArea.getTrapArea(), x -> x.hasAction() && !MY_TRAPS.containsKey(x.getWorldLocation())).isEmpty())
		{
			WorldPoint emptySpot = hunterArea.getTrapArea()
					.toWorldPointList().stream()
					.filter(x -> !MY_TRAPS.containsKey(x) && isXShapeTile(hunterArea.getTrapAreaBottomLeft(), x))
					.findFirst()
					.orElse(null);
			if (emptySpot == null)
			{
				return -1;
			}

			if (emptySpot.distanceTo(local) > 0)
			{
				Movement.walk(emptySpot);
				return -1;
			}

			if (laying)
			{
				return -1;
			}

			item.interact("Lay");
			laying = true;
			lastLayed = Instant.now();
			return -1;
		}

		laying = false;

		TileItem fallen = TileItems.within(hunterArea.getTrapArea(), x -> x.hasAction("Lay")).stream()
				.findFirst().orElse(null);
		if (fallen != null)
		{
			fallen.interact("Take");
			return -1;
		}

		HunterTrap finished = MY_TRAPS.values().stream().filter(x -> x.getState() == HunterTrap.State.FULL || x.getState() == HunterTrap.State.EMPTY)
				.findFirst().orElse(null);
		if (finished != null)
		{
			TileObjects.getFirstAt(finished.getWorldLocation(), x -> x.getId() == finished.getObjectId()).interact(0);
			return -1;
		}

		HunterTrap transitioningTrap = MY_TRAPS.values().stream()
				.filter(x -> x.getState() == HunterTrap.State.TRANSITION)
				.findFirst()
				.orElse(null);
		if (transitioningTrap != null && transitioningTrap.getWorldLocation().distanceTo(local) > 0)
		{
			Movement.walk(transitioningTrap.getWorldLocation());
			return -1;
		}

		log.info("Idle");
		return -1;
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		WorldPoint trapLocation = gameObject.getWorldLocation();
		HunterTrap myTrap = MY_TRAPS.get(trapLocation);

		switch (gameObject.getId())
		{
			case ObjectID.BOX_TRAP_9380:
				if (lastTickLocalPlayerLocation != null && trapLocation.distanceTo(lastTickLocalPlayerLocation) == 0)
				{
					MY_TRAPS.put(trapLocation, new HunterTrap(gameObject));
					laying = false;
				}

				break;

			case ObjectID.SHAKING_BOX:
			case ObjectID.SHAKING_BOX_9382:
			case ObjectID.SHAKING_BOX_9383:
				if (myTrap != null)
				{
					myTrap.setObjectId(gameObject.getId());
					myTrap.setState(HunterTrap.State.FULL);
					myTrap.resetTimer();
					lastActionTime = Instant.now();
				}

				break;

			case ObjectID.BOX_TRAP_9385:
				if (myTrap != null)
				{
					myTrap.setObjectId(gameObject.getId());
					myTrap.setState(HunterTrap.State.EMPTY);
					myTrap.resetTimer();
					lastActionTime = Instant.now();
				}

				break;

			default:
				if (TRANSITION_IDS.contains(gameObject.getId()) && myTrap != null)
				{
					myTrap.setObjectId(gameObject.getId());
					myTrap.setState(HunterTrap.State.TRANSITION);
				}
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (lastLayed.plusMillis(3_000).isBefore(Instant.now()) && laying)
		{
			laying = false;
		}

		var it = MY_TRAPS.entrySet().iterator();
		Instant expire = Instant.now().minus(HunterTrap.TRAP_TIME.multipliedBy(2));

		while (it.hasNext())
		{
			var entry = it.next();
			HunterTrap trap = entry.getValue();
			WorldPoint world = entry.getKey();
			LocalPoint local = LocalPoint.fromWorld(client, world);

			if (local == null)
			{
				if (trap.getPlacedOn().isBefore(expire))
				{
					it.remove();
					continue;
				}

				continue;
			}

			List<TileObject> objects = TileObjects.getAt(world, x -> x.hasAction() || TRANSITION_IDS.contains(x.getId()));
			boolean containsBoulder = false;
			boolean containsAnything = false;
			boolean containsYoungTree = false;

			if (!objects.isEmpty())
			{
				containsAnything = true;
				for (TileObject obj : objects)
				{
					if (obj.getId() == ObjectID.BOULDER_19215 || obj.getId() == ObjectID.LARGE_BOULDER)
					{
						containsBoulder = true;
						break;
					}

					if (obj.getId() == ObjectID.YOUNG_TREE_8732 || obj.getId() == ObjectID.YOUNG_TREE_8990
							|| obj.getId() == ObjectID.YOUNG_TREE_9000 || obj.getId() == ObjectID.YOUNG_TREE_9341
					)
					{
						containsYoungTree = true;
					}
				}
			}

			if (!containsAnything || containsYoungTree)
			{
				it.remove();
			}
			else if (containsBoulder)
			{
				it.remove();
			}
		}

		lastTickLocalPlayerLocation = Players.getLocal().getWorldLocation();
	}

	private boolean isXShapeTile(WorldPoint origin, WorldPoint newTile)
	{
		if (origin.getX() == newTile.getX() && origin.getY() == newTile.getY())
		{
			return true;
		}

		if (newTile.getX() == origin.getX())
		{
			return newTile.getY() - origin.getY() == 2;
		}

		if (newTile.getY() == origin.getY())
		{
			return newTile.getX() - origin.getX() == 2;
		}

		if (newTile.getX() - origin.getX() == 2)
		{
			return newTile.getY() - origin.getY() == 2;
		}

		if (newTile.getX() - origin.getX() == 1)
		{
			return newTile.getY() - origin.getY() == 1;
		}

		return false;
	}
}
