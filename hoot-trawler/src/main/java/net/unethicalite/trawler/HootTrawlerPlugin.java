package net.unethicalite.trawler;

import com.google.inject.Provides;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.plugins.LoopedPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@PluginDescriptor(name = "Hoot Trawler", enabledByDefault = false)
@Extension
@Slf4j
public class HootTrawlerPlugin extends LoopedPlugin
{
	private final WorldArea lobby = new WorldArea(2669, 3165, 5, 16, 1);
	private final WorldPoint entrancePoint = new WorldPoint(2675, 3170, 0);

	private final WorldArea boatBottom = new WorldArea(1881, 4823, 18, 5, 0);
	private final WorldArea boatBottom1 = new WorldArea(2011, 4823, 18, 5, 0);

	private final WorldArea boatDeck = new WorldArea(1881, 4823, 18, 5, 1);
	private final WorldArea boatDeck1 = new WorldArea(2011, 4823, 18, 5, 1);

	private final WorldPoint boatLadderPoint = new WorldPoint(1884, 4826, 0);
	private final WorldPoint boatLadderPoint1 = new WorldPoint(2012, 4826, 0);

	@Override
	protected int loop()
	{
		Player local = Players.getLocal();
		if (boatDeck.contains(local) || boatDeck1.contains(local))
		{
			NPC tentacle = NPCs.getNearest(x -> x.hasAction("Chop") && x.getAnimation() == 8953);
			if (tentacle != null)
			{
				tentacle.interact("Chop");
				return -1;
			}

			log.info("Waiting for tentacle to spawn");
			return -1;
		}

		if (boatBottom.contains(local) || boatBottom1.contains(local))
		{
			TileObject ladder = TileObjects.getFirstAt(boatLadderPoint, x -> x.hasAction("Climb-up"));
			TileObject ladder1 = TileObjects.getFirstAt(boatLadderPoint1, x -> x.hasAction("Climb-up"));
			if (ladder != null)
			{
				ladder.interact("Climb-up");
				return -1;
			}

			if (ladder1 != null)
			{
				ladder1.interact("Climb-up");
				return -1;
			}

			return -1;
		}

		if (lobby.contains(local))
		{
			log.info("Waiting for game start");
			return -1;
		}

		TileObject gangplank = TileObjects.getFirstAt(entrancePoint, x -> x.hasAction("Cross"));
		if (gangplank != null)
		{
			gangplank.interact("Cross");
			return -1;
		}

		log.error("Start plugin near fishing trawler");
		return 1000;
	}

	@Provides
	public HootTrawlerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HootTrawlerConfig.class);
	}
}
