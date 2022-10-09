package net.unethicalite.plugins.chopper;

import com.google.inject.Singleton;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

@Singleton
class ChopperOverlay extends Overlay
{
	@Setter
	private List<Tile> fireArea;

	@Inject
	private Client client;

	@Inject
	protected ChopperOverlay()
	{
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics2D)
	{
		for (Tile tile : fireArea)
		{
			if (tile.isEmpty())
			{
				tile.getWorldLocation().outline(client, graphics2D, Color.GREEN, "Empty tile");
			}
		}

		return null;
	}
}