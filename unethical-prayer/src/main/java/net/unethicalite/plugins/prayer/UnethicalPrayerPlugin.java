package net.unethicalite.plugins.prayer;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.widgets.Prayers;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Extension
@PluginDescriptor(name = "Unethical Prayer", enabledByDefault = false)
@Slf4j
public class UnethicalPrayerPlugin extends Plugin
{
	private final List<PrayerConfig> configs = new CopyOnWriteArrayList<>();
	private final Map<Actor, PrayerSchedule> schedules = new HashMap<>();

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
	private void onInteractingChanged(InteractingChanged event)
	{
		if (!config.turnOnIfTargeted()
				|| event.getTarget() == null
				|| !event.getTarget().equals(Players.getLocal()))
		{
			return;
		}

		for (PrayerConfig prayerConfig : configs)
		{
			// Jad's ranged attacks are delayed so check for the animation instead
			if (prayerConfig.isJad())
			{
				continue;
			}

			if (schedules.containsKey(event.getSource()))
			{
				continue;
			}

			if (prayerConfig.getNpcName().equals(event.getSource().getName()))
			{
				schedules.put(event.getSource(), new PrayerSchedule(prayerConfig, client.getTickCount() + 1));
				return;
			}
		}
	}

	@Subscribe
	private void onAttack(AnimationChanged event)
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
		if (!isAnimationConfigured(animation))
		{
			return;
		}

		PrayerSchedule schedule = schedules.computeIfPresent(actor, (a, s) ->
		{
			if (s.getPrayerConfig().getNpcName().equals(a.getName())
					&& s.getPrayerConfig().isJad()
					&& animation != -1
					&& s.getPrayerConfig().getAnimationId() != animation)
			{
				log.debug("Jad attack was diff: {} -> {}", s.getPrayerConfig().getAnimationId(), animation);
				return null;
			}

			return s;
		});

		if (schedule != null)
		{
			PrayerConfig prayerConfig = schedule.getPrayerConfig();

			if (prayerConfig.getAnimationId() != animation)
			{
				return;
			}

			// Don't toggle off if it's jad, schedule upcoming attack instead
			if (prayerConfig.isJad())
			{
				schedule.setNextAttackTick(client.getTickCount() + 2);
				return;
			}

			Prayer protectionPrayer = prayerConfig.getProtectionPrayer();
			int nextAttack = client.getTickCount() + prayerConfig.getAttackSpeed();

			schedule.setNextAttackTick(nextAttack);
			log.debug("Scheduling {}'s next attack at {}", prayerConfig.getNpcName(), nextAttack);

			// Turn off
			if (config.turnOffAfterAttack() && Prayers.isEnabled(protectionPrayer))
			{
				log.debug("Turning off {} after attack", protectionPrayer);
				Prayers.toggle(protectionPrayer);
			}
		}
		else
		{
			for (PrayerConfig prayerConfig : configs)
			{
				int animationId = prayerConfig.getAnimationId();
				int nextAttack = client.getTickCount() + prayerConfig.getAttackSpeed();
				Prayer protectionPrayer = prayerConfig.getProtectionPrayer();

				if (animationId == animation && prayerConfig.getNpcName().equals(actor.getName()))
				{
					if (prayerConfig.isJad())
					{
						nextAttack = client.getTickCount() + 2;
					}

					// Schedule next attack
					schedules.put(actor, new PrayerSchedule(prayerConfig, nextAttack));
					log.debug("Adding schedule with {}'s next attack at {}", prayerConfig.getNpcName(), nextAttack);

					// Don't toggle on if it's jad because its attacks are delayed
					if (prayerConfig.isJad())
					{
						return;
					}

					if (config.turnOnIfTargeted() && !Prayers.isEnabled(protectionPrayer))
					{
						log.debug("{} has animation ID {}, so we are enabling {}", prayerConfig.getNpcName(),
								animationId, protectionPrayer);
						Prayers.toggle(protectionPrayer);
						return;
					}
				}
			}
		}
	}

	// Handle scheduled attacks
	@Subscribe
	private void onGameTick(GameTick e)
	{
		int currentTick = client.getTickCount();
		log.trace("Current tick: {}", currentTick);

		for (PrayerSchedule schedule : schedules.values())
		{
			PrayerConfig prayerConfig = schedule.getPrayerConfig();
			int attackTick = schedule.getNextAttackTick();
			// Skip nonscheduled configs
			if (attackTick == -1)
			{
				continue;
			}

			// Toggle prayer on if next tick will be the attack tick
			if (currentTick + 1 == attackTick)
			{
				// Reset the config
				schedule.setNextAttackTick(-1);

				if (Prayers.isEnabled(prayerConfig.getProtectionPrayer()))
				{
					log.debug("{}'s attack scheduled at {}, but {} is already on",
							prayerConfig.getNpcName(), attackTick,
							prayerConfig.getProtectionPrayer());
					continue;
				}

				log.debug("{} is about to attack, turning on {}",
						prayerConfig.getNpcName(), prayerConfig.getProtectionPrayer());
				Prayers.toggle(prayerConfig.getProtectionPrayer());
				return;
			}
		}
	}

	@Subscribe
	private void onActorDeath(ActorDeath e)
	{
		removeSchedule(e.getActor());
	}

	@Subscribe
	private void onNPCDespawn(NpcDespawned e)
	{
		removeSchedule(e.getActor());
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

	private void removeSchedule(Actor a)
	{
		PrayerSchedule schedule = schedules.get(a);
		if (schedule != null)
		{
			if (Prayers.isEnabled(schedule.getPrayerConfig().getProtectionPrayer()))
			{
				Prayers.toggle(schedule.getPrayerConfig().getProtectionPrayer());
			}

			schedules.remove(a);
		}
	}

	private void updateConfig()
	{
		List<PrayerConfig> prayerConfigs = new ArrayList<>();
		String[] split = config.configs().split("\n");
		for (String s : split)
		{
			String[] cfgItem = s.split(":");
			PrayerConfig prayerConfig = new PrayerConfig(
					cfgItem[0],
					Prayer.valueOf(cfgItem[1]),
					Integer.parseInt(cfgItem[2]),
					Integer.parseInt(cfgItem[3]));
			prayerConfigs.add(prayerConfig);
		}

		configs.clear();
		configs.addAll(prayerConfigs);

		log.info("Loaded {} configs", configs.size());
	}

	private boolean isAnimationConfigured(int animation)
	{
		return configs.stream().anyMatch(prayerConfig -> prayerConfig.getAnimationId() == animation);
	}

	@Provides
	UnethicalPrayerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalPrayerConfig.class);
	}
}
