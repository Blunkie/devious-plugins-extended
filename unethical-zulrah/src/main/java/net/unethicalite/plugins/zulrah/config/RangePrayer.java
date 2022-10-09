package net.unethicalite.plugins.zulrah.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Prayer;

@RequiredArgsConstructor
public enum RangePrayer
{
	RIGOUR(Prayer.RIGOUR),
	EAGLE_EYE(Prayer.EAGLE_EYE)
	;

	@Getter
	private final Prayer prayer;
}
