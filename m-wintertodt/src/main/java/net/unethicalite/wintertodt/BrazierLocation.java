package net.unethicalite.wintertodt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@AllArgsConstructor
@Getter
public enum BrazierLocation
{
	SOUTH(new WorldPoint(1621, 3991, 0)),
	EAST(new WorldPoint(1646, 3997, 0)),
	;

	private WorldPoint worldPoint;
}
