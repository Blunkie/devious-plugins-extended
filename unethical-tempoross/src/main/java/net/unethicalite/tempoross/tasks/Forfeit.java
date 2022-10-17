package net.unethicalite.tempoross.tasks;

import net.runelite.api.NPC;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.tempoross.TemporossPlugin;

public class Forfeit extends TemporossTask
{
	public Forfeit(TemporossPlugin context)
	{
		super(context);
	}

	@Override
	public boolean validate()
	{
		return TemporossPlugin.intensity >= 94 && getScriptState() == TemporossPlugin.State.THIRD_COOK;
	}

	@Override
	public int execute()
	{
		NPC npc = NPCs.getNearest(x -> x.hasAction("Forfeit"));
		if (npc != null)
		{
			npc.interact("Forfeit");
		}
		return 1000;
	}
}
