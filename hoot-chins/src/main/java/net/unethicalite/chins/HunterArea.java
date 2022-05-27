package net.unethicalite.chins;

import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.movement.pathfinder.GlobalCollisionMap;
import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.util.Collections;
import java.util.stream.Collectors;

@Getter
public enum HunterArea
{
	RED_CHIN(63, "Box trap", new WorldArea(2558, 2916, 3, 3, 0));

	private final int lvl;
	private final String trapName;
	private final WorldArea area;

	private final WorldArea trapArea;
	private final WorldPoint trapAreaBottomLeft;

	HunterArea(int lvl, String trapName, WorldArea area)
	{
		this.lvl = lvl;
		this.trapName = trapName;
		this.area = area;
		GlobalCollisionMap collisionMap = Game.getGlobalCollisionMap();
		var possibleAreas = area.toWorldPointList().stream()
				.filter(x ->
						x.createWorldArea(3, 3).toWorldPointList()
								.stream().noneMatch(collisionMap::fullBlock))
				.collect(Collectors.toList());
		Collections.shuffle(possibleAreas);
		trapArea = possibleAreas.get(0).createWorldArea(3, 3);
		trapAreaBottomLeft = trapArea.toWorldPoint();
	}

	public int getMaxTraps()
	{
		if (Skills.getLevel(Skill.HUNTER) >= 80)
		{
			return 5;
		}

		if (Skills.getLevel(Skill.HUNTER) >= 60)
		{
			return 4;
		}

		if (Skills.getLevel(Skill.HUNTER) >= 40)
		{
			return 3;
		}

		if (Skills.getLevel(Skill.HUNTER) >= 20)
		{
			return 2;
		}

		return 1;
	}
}
