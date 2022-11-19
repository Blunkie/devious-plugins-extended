package net.unethicalite.plugins.chopper;

import lombok.Getter;

@Getter
public enum Tree
{
	REGULAR(1, "Tree", "Evergreen"),
	OAK(15, "Oak"),
	WILLOW(30, "Willow"),
	TEAK(35, "Teak"),
	MAPLE(45, "Maple tree"),
	MAHOGANY(50, "Mahogany"),
	YEW(60, "Yew"),
	MAGIC(75, "Magic tree");

	private final int level;
	private final String[] names;

	Tree(int level, String... names)
	{
		this.level = level;
		this.names = names;
	}
}
