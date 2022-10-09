package net.unethicalite.plugins.zulrah.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Prayer;

@RequiredArgsConstructor
public enum MagePrayer
{
	AUGURY(Prayer.AUGURY),
	MYSTIC_MIGHT(Prayer.MYSTIC_MIGHT),
	;

	@Getter
	private final Prayer prayer;
}
