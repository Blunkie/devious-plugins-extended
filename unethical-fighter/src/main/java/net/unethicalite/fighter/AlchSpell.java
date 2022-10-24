package net.unethicalite.fighter;

import net.unethicalite.api.game.Skills;
import net.unethicalite.api.magic.Rune;
import net.unethicalite.api.magic.RuneRequirement;
import net.unethicalite.api.magic.SpellBook;
import net.runelite.api.Skill;

import java.util.Arrays;

public enum AlchSpell
{
	HIGH(SpellBook.Standard.HIGH_LEVEL_ALCHEMY, new RuneRequirement(1, Rune.NATURE), new RuneRequirement(5, Rune.FIRE)),
	LOW(SpellBook.Standard.LOW_LEVEL_ALCHEMY, new RuneRequirement(1, Rune.NATURE), new RuneRequirement(1, Rune.FIRE));

	private final SpellBook.Standard spell;
	private final RuneRequirement[] requirements;

	AlchSpell(SpellBook.Standard spell, RuneRequirement... requirements)
	{
		this.spell = spell;
		this.requirements = requirements;
	}

	public SpellBook.Standard getSpell()
	{
		return spell;
	}

	public boolean canCast()
	{
		return Skills.getLevel(Skill.MAGIC) >= spell.getLevel()
				&& Arrays.stream(requirements).allMatch(RuneRequirement::meetsRequirements);
	}
}
