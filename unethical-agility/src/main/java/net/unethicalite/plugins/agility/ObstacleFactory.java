package net.unethicalite.plugins.agility;

import java.util.ArrayList;
import java.util.List;

public class ObstacleFactory
{

	private List<Obstacle> obstacles = new ArrayList<>();

	public ObstacleFactory()
	{

	}

	public static ObstacleFactory newInstance(boolean bool)
	{
		return new ObstacleFactory();
	}

	public ObstacleFactory append(Area area, String name, String action, Location tile)
	{
		Obstacle obs = new Obstacle(area, name, action, tile);
		obstacles.add(obs);
		return this;
	}

	public ObstacleFactory append(Area area, String name, String action, int id)
	{
		Obstacle obs = new Obstacle(area, name, action, false, null, id);
		obstacles.add(obs);
		return this;
	}

	public ObstacleFactory append(Area area, String name, String action)
	{
		Obstacle obs = new Obstacle(area, name, action);
		obstacles.add(obs);
		return this;
	}

	public Obstacle[] array()
	{
		return obstacles.toArray(new Obstacle[0]);
	}


}
