package net.unethicalite.plugins.prayer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrayerSchedule
{
	private PrayerConfig prayerConfig;
	private int nextAttackTick;
}
