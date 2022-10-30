package net.unethicalite.plugins.prayer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@RequiredArgsConstructor
@Getter
public enum Weapon
{
	DRAGON_SCIMITAR(ItemID.DRAGON_SCIMITAR, 390, 1892, 4),

	BONE_CROSSBOW(ItemID.DORGESHUUN_CROSSBOW, 7552, 7552, 4),

	MAGIC_SHORTBOW(ItemID.MAGIC_SHORTBOW, 426, 1074, 3),
	;

	private final int id;
	private final int animation;
	private final int specAnimation;
	private final int speed;
}
