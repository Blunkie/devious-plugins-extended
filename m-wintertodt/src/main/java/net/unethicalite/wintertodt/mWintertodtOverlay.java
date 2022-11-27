package net.unethicalite.wintertodt;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class mWintertodtOverlay extends Overlay
{
	private final Client client;
	private final mWintertodtPlugin plugin;
	private final mWintertodtConfig config;

	@Inject
	private mWintertodtOverlay(Client client, mWintertodtPlugin plugin, mWintertodtConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isScriptStarted() && config.overlayEnabled())
		{
			graphics.setColor(Color.RED);
			graphics.drawString("Time running: " + plugin.getTimeRunning(), 10, 190);
			graphics.drawString("State: " + plugin.getCurrentState(), 10, 205);
			graphics.drawString("Games won: " + plugin.getWon(), 10, 220);
			graphics.drawString("Games lost: " + plugin.getLost(), 10, 235);
		}
		return null;
	}
}
