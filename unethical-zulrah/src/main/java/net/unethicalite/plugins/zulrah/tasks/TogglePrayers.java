package net.unethicalite.plugins.zulrah.tasks;

import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.plugins.zulrah.data.Constants;
import net.unethicalite.plugins.zulrah.data.Rotation;
import net.unethicalite.plugins.zulrah.framework.ZulrahTask;

import static net.unethicalite.plugins.zulrah.UnethicalZulrahPlugin.atZulrah;

public class TogglePrayers extends ZulrahTask
{
	private boolean enable = false;

	@Override
	public boolean validate()
	{
		NPC zulrah = NPCs.getNearest(Constants.ZULRAH_NAME);
		if (!atZulrah())
		{
			if (enable)
			{
				enable = false;
			}
		}

		if (getRotation() != null && !enable && !getRotation().equals(Rotation.INITIAL))
		{
			enable = true;
		}

		return zulrah != null
				&& zulrah.getHealthRatio() != 0
				&& atZulrah()
				&& (canToggleOffensive() || canToggleDefensive());
	}

	@Override
	public int execute()
	{
		if (canToggleDefensive())
		{
			Prayer defensive = getRotation().getZulrahType().getDefensivePrayer();
			if (getRotation() != null)
			{
				Prayers.toggle(defensive);
				Time.sleepUntil(() -> Prayers.isEnabled(defensive), 1200);
				return 300;
			}
		}

		if (canToggleOffensive())
		{
			Prayer offensive = getRotation().getZulrahType().getOffensivePrayer();
			if (getRotation() != null)
			{
				Prayers.toggle(offensive);
				Time.sleepUntil(() -> Prayers.isEnabled(offensive), 1200);
				return 300;
			}
		}

		return 100;
	}

	private boolean canToggleOffensive()
	{
		return getRotation() != null
				&& atZulrah()
				&& Prayers.getPoints() > 0
				&& Skills.getLevel(Skill.PRAYER) >= 45
				&& !Prayers.isEnabled(getRotation().getZulrahType().getOffensivePrayer());
	}

	private boolean canToggleDefensive()
	{
		return getRotation() != null
				&& enable
				&& atZulrah()
				&& !getRotation().isJad()
				&& !getRotation().isMelee()
				&& Prayers.getPoints() > 0
				&& !Prayers.isEnabled(getRotation().getZulrahType().getDefensivePrayer());
	}

	@Override
	public boolean isBlocking()
	{
		return false;
	}
}
