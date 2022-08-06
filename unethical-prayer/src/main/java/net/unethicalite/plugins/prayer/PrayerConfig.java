package net.unethicalite.plugins.prayer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Prayer;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PrayerConfig
{
	private final String npcName;
	private final Prayer protectionPrayer;
	private final int animationId;
	private final int attackDelay;
	private int nextAttackTick = -1;
}
