package net.unethicalite.plugins.prayer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@RequiredArgsConstructor
@Getter
public enum Weapon
{
	DRAGON_SCIMITAR(ItemID.DRAGON_SCIMITAR, 390, 1892, 4)
	;

	private final int id;
	private final int animation;
	private final int specAnimation;
	private final int speed;
}
