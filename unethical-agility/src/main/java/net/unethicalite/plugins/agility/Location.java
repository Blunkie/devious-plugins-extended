package net.unethicalite.plugins.agility;

import net.runelite.api.coords.WorldPoint;

public class Location
{
	private final int x;
	private final int y;
	private final int z;

	public Location(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Location(int x, int y)
	{
		this(x, y, 0);
	}

	public int getX()
	{
		return this.x;
	}

	public int getY()
	{
		return this.y;
	}

	public int getZ()
	{
		return this.z;
	}

	public WorldPoint toWorldPoint()
	{
		return new WorldPoint(this.x, this.y, this.z);
	}
}
