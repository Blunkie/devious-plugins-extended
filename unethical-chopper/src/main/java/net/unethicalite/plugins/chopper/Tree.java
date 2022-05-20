package net.unethicalite.plugins.chopper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum Tree
{
	REGULAR(1, "Tree"),
	OAK(15, "Oak"),
	WILLOW(30, "Willow");

	private final int level;
	private final String name;
}
