package net.unethicalite.tempoross.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.tempoross.TemporossPlugin;
import net.unethicalite.tempoross.TemporossWorkArea;

@Slf4j
public class DetermineWorkArea extends TemporossTask
{
	public DetermineWorkArea(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return getWorkArea() == null;
	}

	@Override
	public int execute()
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
		setWorkArea(area);
		return -1;
	}
}
