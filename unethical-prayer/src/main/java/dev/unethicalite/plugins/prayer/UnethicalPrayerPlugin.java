package dev.unethicalite.plugins.prayer;

import dev.unethicalite.api.widgets.Prayers;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.Prayer;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(name = "Unethical Prayer", enabledByDefault = false)
public class UnethicalPrayerPlugin extends Plugin
{
	public static final int JALTOK_JAD_MAGE_ATTACK = 7592;
	public static final int JALTOK_JAD_RANGE_ATTACK = 7593;

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();

		if (actor == null)
		{
			return;
		}

		switch (actor.getAnimation())
		{
			case AnimationID.TZTOK_JAD_MAGIC_ATTACK:
			case JALTOK_JAD_MAGE_ATTACK:
				if (!Prayers.isEnabled(Prayer.PROTECT_FROM_MAGIC))
				{
					Prayers.toggle(Prayer.PROTECT_FROM_MAGIC);
				}
				break;
			case AnimationID.TZTOK_JAD_RANGE_ATTACK:
			case JALTOK_JAD_RANGE_ATTACK:
				if (!Prayers.isEnabled(Prayer.PROTECT_FROM_MISSILES))
				{
					Prayers.toggle(Prayer.PROTECT_FROM_MISSILES);
				}
				break;
		}
	}
}
