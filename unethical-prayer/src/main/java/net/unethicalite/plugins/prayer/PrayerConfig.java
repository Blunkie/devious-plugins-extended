package net.unethicalite.plugins.prayer;

import lombok.Value;
import net.runelite.api.Prayer;

import java.util.List;

@Value
public class PrayerConfig
{
	private static final List<Integer> JAD_ATTACKS = List.of(7592, 7593, 2656, 2652);
	String npcName;
	Prayer protectionPrayer;
	int animationId;
	int attackSpeed;

	public boolean isJad()
	{
		return JAD_ATTACKS.contains(animationId);
	}
}
