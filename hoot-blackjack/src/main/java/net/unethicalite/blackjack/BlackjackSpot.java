package net.unethicalite.blackjack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldArea;

@RequiredArgsConstructor
@Getter
public enum BlackjackSpot
{
	BANDIT_ONE(new WorldArea(3360, 2977, 3, 3, 0), "Bandit"),
	BANDIT_TWO(new WorldArea(3365, 2982, 5, 5, 0), "Bandit"),
	BANDIT_THREE(new WorldArea(3357, 2991, 4, 5, 0), "Bandit"),
	MENAPHITE_ONE(new WorldArea(3348, 2953, 4, 4, 0), "Menaphite Thug"),
	MENAPHITE_TWO(new WorldArea(3349, 2947, 3, 6, 0), "Menaphite Thug"),
	MENAPHITE_THREE(new WorldArea(3340, 2953, 5, 4, 0), "Menaphite Thug"),
	;

	private final WorldArea area;
	private final String npcName;
}
