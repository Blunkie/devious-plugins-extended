package net.unethicalite.plugins.prayer;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.widgets.Prayers;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Extension
@PluginDescriptor(name = "Unethical Prayer", enabledByDefault = false)
@Slf4j
public class UnethicalPrayerPlugin extends Plugin
{
	private final List<PrayerConfig> configs = new CopyOnWriteArrayList<>();

	@Inject
	private UnethicalPrayerConfig config;

	@Inject
	private Client client;

	@Override
	protected void startUp()
	{
		updateConfig();
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();
		if (actor == null
				|| actor.getInteracting() == null
				|| !actor.getInteracting().equals(Players.getLocal())
				|| configs.isEmpty()
		)
		{
			return;
		}

		int animation = actor.getAnimation();

		for (PrayerConfig prayerConfig : configs)
		{
			if (prayerConfig.getAnimationId() == animation && prayerConfig.getNpcName().equals(actor.getName()))
			{
				prayerConfig.setNextAttackTick(client.getTickCount() + prayerConfig.getAttackDelay());

				if (config.turnOffAfterAttack() && Prayers.isEnabled(prayerConfig.getProtectionPrayer()))
				{
					Prayers.toggle(prayerConfig.getProtectionPrayer());
				}
			}
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		int currentTick = client.getTickCount();

		for (PrayerConfig prayerConfig : configs)
		{
			if (prayerConfig.getNextAttackTick() == -1)
			{
				continue;
			}

			if (currentTick + 1 == prayerConfig.getNextAttackTick())
			{
				if (Prayers.isEnabled(prayerConfig.getProtectionPrayer()))
				{
					prayerConfig.setNextAttackTick(-1);
					continue;
				}

				Prayers.toggle(prayerConfig.getProtectionPrayer());
				return;
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged e)
	{
		if (!"unethicalprayer".equals(e.getGroup()))
		{
			return;
		}

		updateConfig();
	}

	private void updateConfig()
	{
		List<PrayerConfig> prayerConfigs = new ArrayList<>();
		String[] split = config.configs().split(",");
		for (String s : split)
		{
			String[] cfgItem = s.split(":");
			prayerConfigs.add(new PrayerConfig(
					cfgItem[0],
					Prayer.valueOf(cfgItem[1]),
					Integer.parseInt(cfgItem[2]),
					Integer.parseInt(cfgItem[3]))
			);
		}

		configs.clear();
		configs.addAll(prayerConfigs);
	}

	@Provides
	UnethicalPrayerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalPrayerConfig.class);
	}
}
