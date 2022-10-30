package net.unethicalite.plugins.prayer;

import lombok.Getter;
import net.runelite.api.NpcID;
import net.runelite.api.Prayer;

import java.util.Arrays;
import java.util.List;

@Getter
public enum PrayerNpc
{
	FIRE_GIANT(
			new Attack(Prayer.PROTECT_FROM_MELEE, 4667, 5, NpcID.FIRE_GIANT_2081, NpcID.FIRE_GIANT_2082),
			new Attack(Prayer.PROTECT_FROM_MELEE, 4666, 5, NpcID.FIRE_GIANT_2083)
	),

	HELLHOUND(new Attack(Prayer.PROTECT_FROM_MELEE, 6562, 4, NpcID.HELLHOUND, NpcID.HELLHOUND_105)),

	ABERRANT_SPECTRE(new Attack(Prayer.PROTECT_FROM_MAGIC, 1507, 4, NpcID.ABERRANT_SPECTRE)),

	TZTOK_JAD(
			new Attack(Prayer.PROTECT_FROM_MAGIC, 2656, 8, NpcID.TZTOKJAD),
			new Attack(Prayer.PROTECT_FROM_MISSILES, 2652, 8, NpcID.TZTOKJAD)
	),

	SUQAH(
			new Attack(Prayer.PROTECT_FROM_MELEE, 4388, 6, NpcID.SUQAH_791),
			new Attack(Prayer.PROTECT_FROM_MELEE, 4387, 6, NpcID.SUQAH_792)
	),

	MUT_BLOODVELD(new Attack(Prayer.PROTECT_FROM_MELEE, 1552, 4, NpcID.MUTATED_BLOODVELD)),

	BLACK_DEMON(new Attack(Prayer.PROTECT_FROM_MELEE, 64, 4, MonsterID.BLACK_DEMONS)),

	ANKOU(new Attack(Prayer.PROTECT_FROM_MELEE, 422, 4, NpcID.ANKOU_2517, NpcID.ANKOU_2518, NpcID.ANKOU_2519)),

	SPIRITUAL_RANGER(new Attack(Prayer.PROTECT_FROM_MISSILES, 426, 4, NpcID.ANKOU_2517, NpcID.ANKOU_2518, NpcID.SPIRITUAL_RANGER_3160)),

	KET_ZEK(new Attack(Prayer.PROTECT_FROM_MAGIC, 2647, 4, NpcID.KETZEK, NpcID.KETZEK_3126)),

	TOK_XIL(new Attack(Prayer.PROTECT_FROM_MISSILES, 2633, 4, NpcID.TOKXIL, NpcID.TOKXIL_2193, NpcID.TOKXIL_2194, NpcID.TOKXIL_3121, NpcID.TOKXIL_3122)),

	YT_MEJKOT(new Attack(Prayer.PROTECT_FROM_MELEE, 2637, 4, NpcID.YTMEJKOT, NpcID.YTMEJKOT_3124)),

	WYRM(
			new Attack(Prayer.PROTECT_FROM_MAGIC, 8271, 4, NpcID.WYRM, NpcID.WYRM_8611),
			new Attack(Prayer.PROTECT_FROM_MAGIC, 8271, 4, NpcID.SHADOW_WYRM, NpcID.SHADOW_WYRM_10399)
	);

	private static final List<Integer> JAD_ATTACKS = List.of(7592, 7593, 2656, 2652);

	private final Attack[] attacks;

	PrayerNpc(Attack... attacks)
	{
		this.attacks = attacks;
	}

	public boolean isJad()
	{
		return Arrays.stream(attacks).anyMatch(Attack::isJad);
	}

	@Getter
	public static final class Attack
	{
		private final Prayer protectionPrayer;
		private final int animationId;
		private final int speed;
		private final int[] npcIds;

		public Attack(Prayer protectionPrayer, int animationId, int speed, int... npcIds)
		{
			this.protectionPrayer = protectionPrayer;
			this.animationId = animationId;
			this.speed = speed;
			this.npcIds = npcIds;
		}

		public boolean isJad()
		{
			return JAD_ATTACKS.contains(animationId);
		}
	}
}
