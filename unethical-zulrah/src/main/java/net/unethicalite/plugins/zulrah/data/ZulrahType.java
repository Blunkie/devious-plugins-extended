package net.unethicalite.plugins.zulrah.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Prayer;

@RequiredArgsConstructor
@Getter
public enum ZulrahType
{
	RANGE(2042),
	MELEE(2043),
	MAGIC(2044),
	JAD_MAGIC_FIRST(2042),
	JAD_RANGE_FIRST(2042);

	public static Prayer rangePray;
	public static Prayer magePray;
	private final int id;
	private Gear setup;

	public static void setRangedMeleePhaseGear(Gear gear)
	{
		RANGE.setSetup(gear);
		MELEE.setSetup(gear);
		JAD_MAGIC_FIRST.setSetup(gear);
		JAD_RANGE_FIRST.setSetup(gear);
		magePray = Prayer.MYSTIC_MIGHT;
	}

	public static void setMagePhaseGear(Gear gear)
	{
		MAGIC.setSetup(gear);
		rangePray = Prayer.EAGLE_EYE;
	}

	public int id()
	{
		return id;
	}

	public Prayer getOffensivePrayer()
	{
		if (this == ZulrahType.MAGIC)
		{
			return rangePray;
		}

		return magePray;
	}

	public Prayer getDefensivePrayer()
	{
		switch (this)
		{
			case MAGIC:
			case JAD_MAGIC_FIRST:
				return Prayer.PROTECT_FROM_MAGIC;

			default:
				return Prayer.PROTECT_FROM_MISSILES;
		}
	}

	public Gear getSetup()
	{
		return setup;
	}

	public void setSetup(Gear setup)
	{
		this.setup = setup;
	}
}
