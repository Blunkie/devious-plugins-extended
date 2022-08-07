package net.unethicalite.plugins.prayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class PrayerSchedule
{
	private final PrayerConfig prayerConfig;
	private int nextAttackTick = -1;
}
