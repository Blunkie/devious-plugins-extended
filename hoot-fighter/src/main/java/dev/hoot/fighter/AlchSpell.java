package dev.hoot.fighter;

import dev.unethicalite.api.game.Skills;
import dev.unethicalite.api.magic.Regular;
import dev.unethicalite.api.magic.Rune;
import dev.unethicalite.api.movement.pathfinder.RuneRequirement;
import net.runelite.api.Skill;

import java.util.Arrays;

public enum AlchSpell
{
	HIGH(Regular.HIGH_LEVEL_ALCHEMY, new RuneRequirement(1, Rune.NATURE), new RuneRequirement(5, Rune.FIRE)),
	LOW(Regular.LOW_LEVEL_ALCHEMY, new RuneRequirement(1, Rune.NATURE), new RuneRequirement(1, Rune.FIRE));

	private final Regular spell;
	private final RuneRequirement[] requirements;

	AlchSpell(Regular spell, RuneRequirement... requirements)
	{
		this.spell = spell;
		this.requirements = requirements;
	}

	public Regular getSpell()
	{
		return spell;
	}

	public boolean canCast()
	{
		return Skills.getLevel(Skill.MAGIC) >= spell.getLevel()
				&& Arrays.stream(requirements).allMatch(RuneRequirement::meetsRequirements);
	}
}
