package net.unethicalite.plugins.chopper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.scene.Tiles;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
		name = "Unethical Chopper",
		description = "Chops trees",
		enabledByDefault = false
)
@Slf4j
public class ChopperPlugin extends LoopedPlugin
{
	@Inject
	private ChopperConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChopperOverlay chopperOverlay;

	private int fmCooldown = 0;
	private List<Tile> fireArea;
	private WorldPoint startLocation = null;

	@Override
	protected void startUp()
	{
		startLocation = Players.getLocal().getWorldLocation();
		fireArea = generateFireArea(3);

		chopperOverlay.setFireArea(fireArea);

		overlayManager.add(chopperOverlay);
	}

	@Override
	public void stop()
	{
		super.stop();
		overlayManager.remove(chopperOverlay);
	}

	@Override
	protected int loop()
	{
		if (fmCooldown > 0)
		{
			return -1;
		}

		var local = Players.getLocal();
		var tree = TileObjects
				.getSurrounding(startLocation, 8, config.tree().getNames())
				.stream()
				.min(Comparator.comparing(x -> x.distanceTo(local.getWorldLocation())))
				.orElse(null);

		var logs = Inventory.getFirst(x -> x.getName().toLowerCase(Locale.ROOT).contains("logs"));
		if (config.makeFire())
		{
			var tinderbox = Inventory.getFirst("Tinderbox");
			if (logs != null && tinderbox != null)
			{
				if (fireArea.isEmpty())
				{
					fireArea = generateFireArea(3);
					chopperOverlay.setFireArea(fireArea);
					log.debug("Re-Generating fire area");
					return 1000;
				}

				var emptyTile = fireArea.stream()
						.filter(t ->
						{
							Tile tile = Tiles.getAt(t.getWorldLocation());
							return tile != null && tile.isEmpty();
						})
						.min(Comparator.comparingInt(wp -> wp.distanceTo(local)))
						.orElse(null);
				if (emptyTile != null)
				{
					if (!emptyTile.getWorldLocation().equals(local.getWorldLocation()))
					{
						if (local.isMoving())
						{
							return 333;
						}

						Movement.walk(emptyTile);
						return 1000;
					}

					if (local.isAnimating())
					{
						return 333;
					}

					fmCooldown = 4;
					tinderbox.useOn(logs);
					return 500;
				}
			}
		}
		else
		{
			if (logs != null)
			{
				logs.drop();
				return 500;
			}
		}

		if (local.isMoving() || local.isAnimating())
		{
			return 333;
		}

		if (tree == null)
		{
			log.debug("Could not find any trees");
			return 1000;
		}

		tree.interact("Chop down");
		return 1000;
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (fmCooldown > 0)
		{
			fmCooldown--;
		}
	}

	@Provides
	ChopperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ChopperConfig.class);
	}

	private List<Tile> generateFireArea(int radius)
	{
		return Tiles.getSurrounding(Players.getLocal().getWorldLocation(), radius).stream()
				.filter(tile -> tile.isEmpty() && !tile.isObstructed() && Reachable.isWalkable(tile.getWorldLocation()))
				.collect(Collectors.toUnmodifiableList());
	}
}
