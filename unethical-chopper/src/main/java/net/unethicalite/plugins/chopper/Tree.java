package net.unethicalite.plugins.chopper;

import lombok.Getter;

@Getter
public enum Tree
{
	REGULAR(1, "Tree", "Evergreen"),
	OAK(15, "Oak"),
	WILLOW(30, "Willow");

	private final int level;
	private final String[] names;

	Tree(int level, String... names)
	{
		this.level = level;
		this.names = names;
	}
}
