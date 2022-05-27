package net.unethicalite.plugins.agility;

import net.runelite.api.Locatable;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Area
{
	private final Location start;
	private final Location end;
	private final List<Location> tiles;
	private final int floorLevel;

	public Area(Location start, Location end, int floorLevel)
	{
		this.start = start;
		this.end = end;
		this.floorLevel = floorLevel;
		this.tiles = new ArrayList<>();
		int startX = this.getStart().getX();
		int startY = this.getStart().getY();
		int endX = this.getEnd().getX();
		int endY = this.getEnd().getY();

		for (int x = Math.min(startX, endX); x <= Math.max(startX, endX); ++x)
		{
			for (int y = Math.max(startY, endY); y >= Math.min(startY, endY); --y)
			{
				this.tiles.add(new Location(x, y, floorLevel));
			}
		}

	}

	public Area(Location start, Location end)
	{
		this(start, end, 0);
	}

	public Area(int minX, int minY, int maxX, int maxY, int floorLevel)
	{
		this(new Location(minX, minY), new Location(maxX, maxY), floorLevel);
	}

	public Area(int minX, int minY, int maxX, int maxY)
	{
		this(minX, minY, maxX, maxY, 0);
	}

	public List<Location> getLocations()
	{
		return this.tiles;
	}

	public int getPlane()
	{
		return this.floorLevel;
	}

	public Location getStart()
	{
		return this.start;
	}

	public Location getEnd()
	{
		return this.end;
	}

	public int getWidth()
	{
		return 1 + Math.max(this.getStart().getX(), this.getEnd().getX()) - Math.min(this.getStart().getX(), this.getEnd().getX());
	}

	public int getHeight()
	{
		return 1 + Math.max(this.getStart().getY(), this.getEnd().getY()) - Math.min(this.getStart().getY(), this.getEnd().getY());
	}

	public boolean contains(Locatable l)
	{
		WorldPoint location = l.getWorldLocation();
		Iterator var3 = this.getLocations().iterator();

		Location tile;
		do
		{
			if (!var3.hasNext())
			{
				return false;
			}

			tile = (Location) var3.next();
		}
		while (!location.equals(tile.toWorldPoint()));

		return true;
	}

	public Location getCenter()
	{
		List<Location> tiles = this.getLocations();
		int x = 0;
		int y = 0;

		Location t;
		for (Iterator var4 = tiles.iterator(); var4.hasNext(); y += t.getY())
		{
			t = (Location) var4.next();
			x += t.getX();
		}

		x /= tiles.size();
		y /= tiles.size();
		return new Location(x, y);
	}
}
