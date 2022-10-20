package net.unethicalite.plugins.birdhouses.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;

@RequiredArgsConstructor
@Getter
public enum BirdHouseLocation
{
	MEADOW_SOUTH(new WorldPoint(3679, 3814, 0), VarPlayer.BIRD_HOUSE_MEADOW_SOUTH),
	MEADOW_NORTH(new WorldPoint(3677, 3881, 0), VarPlayer.BIRD_HOUSE_MEADOW_NORTH),
	VALLEY_SOUTH(new WorldPoint(3763, 3754, 0), VarPlayer.BIRD_HOUSE_VALLEY_SOUTH),
	VALLEY_NORTH(new WorldPoint(3768, 3760, 0), VarPlayer.BIRD_HOUSE_MEADOW_NORTH);

	private final WorldPoint worldPoint;
	private final VarPlayer varp;
}
