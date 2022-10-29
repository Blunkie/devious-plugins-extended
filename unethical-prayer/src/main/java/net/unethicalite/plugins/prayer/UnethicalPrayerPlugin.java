package net.unethicalite.plugins.prayer;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.interaction.InteractMethod;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import java.util.LinkedHashMap;
import java.util.Map;

@Extension
@PluginDescriptor(name = "Unethical Prayer", enabledByDefault = false)
@Slf4j
public class UnethicalPrayerPlugin extends Plugin
{
	private static final Map<Integer, Weapon> ATTACK_ANIMATIONS = Map.of(
			390, Weapon.DRAGON_SCIMITAR,
			1892, Weapon.DRAGON_SCIMITAR
	);

	private final Map<Actor, PrayerSchedule> schedules = new LinkedHashMap<>();

	@Inject
	private UnethicalPrayerConfig config;

	@Inject
	private Client client;

	private int ourLastAttack = -1;

	@Subscribe
	private void onInteractingChanged(InteractingChanged event)
	{
		if (!config.turnOnIfTargeted()
				|| event.getTarget() == null)
		{
			return;
		}

		Player local = Players.getLocal();
		if (!event.getTarget().equals(local) && !event.getSource().equals(local))
		{
			return;
		}

		for (PrayerConfig prayerConfig : config.npcs())
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

			for (PrayerConfig.Attack attack : prayerConfig.getAttacks())
			{
				for (int npcId : attack.getNpcIds())
				{
					if (npcId == event.getSource().getId() || npcId == event.getTarget().getId())
					{
						schedules.put(event.getSource(), new PrayerSchedule(attack, npcId, client.getTickCount() + 1));
						return;
					}
				}
			}
		}
	}

	@Subscribe
	private void onAttack(AnimationChanged event)
	{
		Actor actor = event.getActor();
		if (actor == null
				|| actor.getInteracting() == null
				|| config.npcs().isEmpty()
		)
		{
			return;
		}

		int animation = actor.getAnimation();
		if (animation == -1)
		{
			return;
		}

		Player local = Players.getLocal();
		int currentTick = client.getTickCount();
		if (actor == local)
		{
			Weapon weapon = ATTACK_ANIMATIONS.get(animation);
			if (weapon != null)
			{
				ourLastAttack = currentTick;
				return;
			}
		}

		if (!actor.getInteracting().equals(local))
		{
			return;
		}

		PrayerSchedule schedule = schedules.computeIfPresent(actor, (a, s) ->
		{
			if (s.getNpcId() == a.getId()
					&& s.getAttack().isJad()
					&& s.getAttack().getAnimationId() != animation)
			{
				log.info("Jad attack was diff: {} -> {}", s.getAttack().getAnimationId(), animation);
				return null;
			}

			return s;
		});

		if (schedule != null)
		{
			PrayerConfig.Attack attack = schedule.getAttack();

			if (animation == attack.getAnimationId() || (ourLastAttack == currentTick && schedule.getNextAttackTick() == currentTick))
			{
				// Don't toggle off if it's jad, schedule upcoming attack instead
				if (attack.isJad())
				{
					schedule.setNextAttackTick(currentTick + 2);
					return;
				}

				Prayer protectionPrayer = attack.getProtectionPrayer();
				int nextAttack = currentTick + attack.getSpeed();

				schedule.setNextAttackTick(nextAttack);
				log.info("[{}] Scheduling {}'s next attack {} at {}", currentTick, actor.getId(), attack.getAnimationId(),
						nextAttack);

				// Turn off
				if (config.turnOffAfterAttack() && Prayers.isEnabled(protectionPrayer) && !isAttackScheduledNextTick())
				{
					log.info("Turning off {} after attack", protectionPrayer);
					togglePrayer(protectionPrayer);
				}
			}
		}
		else
		{
			for (PrayerConfig prayerConfig : config.npcs())
			{
				for (PrayerConfig.Attack attack : prayerConfig.getAttacks())
				{
					for (int npcId : attack.getNpcIds())
					{
						int animationId = attack.getAnimationId();
						int nextAttack = currentTick + attack.getSpeed();
						Prayer protectionPrayer = attack.getProtectionPrayer();

						if (animationId == animation && npcId == actor.getId())
						{
							if (prayerConfig.isJad())
							{
								nextAttack = currentTick + 2;
							}

							// Schedule next attack
							schedules.put(actor, new PrayerSchedule(attack, npcId, nextAttack));
							log.info("Adding schedule with {}'s next attack at {}", npcId, nextAttack);

							// Don't toggle on if it's jad because its attacks are delayed
							if (prayerConfig.isJad())
							{
								return;
							}

							if (config.turnOnIfTargeted() && !Prayers.isEnabled(protectionPrayer))
							{
								log.info("{} has animation ID {}, so we are enabling {}", npcId,
										animationId, protectionPrayer);
								togglePrayer(protectionPrayer);
								return;
							}
						}
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
			PrayerConfig.Attack attack = schedule.getAttack();
			int attackTick = schedule.getNextAttackTick();

			// Toggle prayer on if next tick will be the attack tick
			if (currentTick + 1 == attackTick)
			{
				if (Prayers.isEnabled(attack.getProtectionPrayer()))
				{
					log.info("{}'s attack scheduled at {}, but {} is already on",
							schedule.getNpcId(), attackTick,
							attack.getProtectionPrayer());
					continue;
				}

				log.info("{} is about to attack, turning on {}",
						schedule.getNpcId(), attack.getProtectionPrayer());
				togglePrayer(attack.getProtectionPrayer());
				return;
			}
		}
	}

	private boolean isAttackScheduledNextTick()
	{
		return schedules.values().stream().anyMatch(s -> s.getNextAttackTick() == client.getTickCount() + 1);
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

	private void removeSchedule(Actor a)
	{
		PrayerSchedule schedule = schedules.get(a);
		if (schedule != null)
		{
			if (Prayers.isEnabled(schedule.getAttack().getProtectionPrayer()))
			{
				togglePrayer(schedule.getAttack().getProtectionPrayer());
			}

			schedules.remove(a);
		}
	}

	@Provides
	UnethicalPrayerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(UnethicalPrayerConfig.class);
	}

	private static void togglePrayer(Prayer prayer)
	{
		Widget widget = Widgets.get(prayer.getWidgetInfo());
		if (widget != null)
		{
			widget.interact(InteractMethod.PACKETS, 0);
		}
	}
}
