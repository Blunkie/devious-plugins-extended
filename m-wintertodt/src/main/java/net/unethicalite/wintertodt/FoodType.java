package net.unethicalite.wintertodt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

@AllArgsConstructor
@Getter
public enum FoodType
{
	TUNA("Tuna", ItemID.TUNA, "Eat"),
	SHARK("Shark", ItemID.SHARK, "Eat"),
	JUG_OF_WINE("Jug of wine", ItemID.JUG_OF_WINE, "Drink"),
	;

	private String name;
	private int id;
	private String action;
}
