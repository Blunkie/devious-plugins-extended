package net.unethicalite.plugins.agility;

import net.runelite.api.TileObject;

public class Obstacle
{

	private final Area area;
	private final String name;
	private final String action;
	private final Location tile; // exact location for obstacle, only needed if script tries 2 do prev obstacle agen
	private final boolean npc;
	private int id;
	private TileObject object;

	public Obstacle(Area area, String name, String action, boolean npc, Location tile)
	{
		this.area = area;
		this.name = name;
		this.action = action;
		this.npc = npc;
		this.tile = tile;
	}

	public Obstacle(Area area, String name, String action, boolean npc, Location tile, int id)
	{
		this.area = area;
		this.name = name;
		this.action = action;
		this.npc = npc;
		this.tile = tile;
		this.id = id;
	}

	public Obstacle(Area location, String name, String action, Location tile)
	{
		this(location, name, action, false, tile);
	}

	public Obstacle(Area location, String name, String action)
	{
		this(location, name, action, false, null);
	}

	public Area getArea()
	{
		return area;
	}

	public String getAction()
	{
		return action;
	}

	public String getName()
	{
		return name;
	}

	public Location getLocation()
	{
		return tile;
	}

	public boolean isNpc()
	{
		return npc;
	}

	public int getId()
	{
		return id;
	}

	public TileObject getObject()
	{
		return object;
	}

	public void setObject(TileObject object)
	{
		this.object = object;
	}
}
